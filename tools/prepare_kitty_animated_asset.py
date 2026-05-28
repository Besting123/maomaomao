"""Prepare a runtime-ready companion-cat asset from a static kitty GLB.

Two-stage pipeline producing a single output GLB:

1. Texture compression: every image bufferView is decoded with Pillow,
   downscaled (LANCZOS) to TARGET_TEXTURE_SIZE on the long edge, and
   re-encoded as optimised PNG. The bin chunk is rebuilt in place with
   updated bufferView offsets/lengths. Mesh accessor bufferViews are
   preserved verbatim.

2. Procedural animation injection: a `CatMotionRoot` node is inserted as
   the new scene root, wrapping all original top-level nodes, and six
   named clips (Idle, Observe, Pet, Drink, Eat, Happy) are written as
   keyframe samplers driving its translation/rotation/scale.

The input file is expected to have:
- a single skinless mesh, no skins, no animations (the kitty.glb shape);
- PNG bufferView images; large textures (4096) are typical sources.

Usage:
    python tools/prepare_kitty_animated_asset.py input.glb output.glb
"""

from __future__ import annotations

import copy
import io
import json
import math
import struct
import sys
from pathlib import Path
from typing import Any

from PIL import Image


JSON_CHUNK = 0x4E4F534A
BIN_CHUNK = 0x004E4942

TARGET_TEXTURE_SIZE = 1024


def align4(data: bytes, pad: bytes = b"\x00") -> bytes:
    return data + pad * ((4 - len(data) % 4) % 4)


def quat_from_euler_deg(x_deg: float, y_deg: float, z_deg: float) -> tuple[float, float, float, float]:
    x = math.radians(x_deg) / 2.0
    y = math.radians(y_deg) / 2.0
    z = math.radians(z_deg) / 2.0
    cx, sx = math.cos(x), math.sin(x)
    cy, sy = math.cos(y), math.sin(y)
    cz, sz = math.cos(z), math.sin(z)
    return (
        sx * cy * cz - cx * sy * sz,
        cx * sy * cz + sx * cy * sz,
        cx * cy * sz - sx * sy * cz,
        cx * cy * cz + sx * sy * sz,
    )


def read_glb(path: Path) -> tuple[dict[str, Any], bytes]:
    data = path.read_bytes()
    magic, version, declared_length = struct.unpack_from("<4sII", data, 0)
    if magic != b"glTF" or version != 2 or declared_length != len(data):
        raise RuntimeError(f"Invalid GLB: {path}")
    offset = 12
    json_doc: dict[str, Any] | None = None
    bin_chunk = b""
    while offset < len(data):
        chunk_length, chunk_type = struct.unpack_from("<II", data, offset)
        offset += 8
        chunk = data[offset : offset + chunk_length]
        offset += chunk_length
        if chunk_type == JSON_CHUNK:
            json_doc = json.loads(chunk.decode("utf-8").rstrip("\x00 "))
        elif chunk_type == BIN_CHUNK:
            bin_chunk = chunk
    if json_doc is None:
        raise RuntimeError(f"Missing JSON chunk: {path}")
    return json_doc, bin_chunk


def write_glb(path: Path, doc: dict[str, Any], bin_chunk: bytes) -> None:
    doc.setdefault("buffers", [{}])[0]["byteLength"] = len(bin_chunk)
    json_bytes = align4(json.dumps(doc, ensure_ascii=False, separators=(",", ":")).encode("utf-8"), b" ")
    bin_bytes = align4(bin_chunk, b"\x00")
    total_length = 12 + 8 + len(json_bytes) + 8 + len(bin_bytes)
    out = bytearray()
    out.extend(struct.pack("<4sII", b"glTF", 2, total_length))
    out.extend(struct.pack("<II", len(json_bytes), JSON_CHUNK))
    out.extend(json_bytes)
    out.extend(struct.pack("<II", len(bin_bytes), BIN_CHUNK))
    out.extend(bin_bytes)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(out)


def compress_textures(doc: dict[str, Any], bin_chunk: bytes, target_size: int) -> tuple[dict[str, Any], bytes]:
    """Decode each image bufferView, resize to target_size, rebuild bin chunk.

    Non-image bufferViews retain their original bytes. Image bufferViews are
    replaced with re-encoded PNG. All bufferView byteOffsets/byteLengths are
    rewritten so the layout stays valid; the order is preserved (non-image
    first if they appear first in the original layout, then images).
    """
    image_bv_indices = {img["bufferView"] for img in doc.get("images", []) if "bufferView" in img}
    if not image_bv_indices:
        return doc, bin_chunk

    new_image_bytes: dict[int, bytes] = {}
    for bv_idx in image_bv_indices:
        bv = doc["bufferViews"][bv_idx]
        start = bv.get("byteOffset", 0)
        length = bv["byteLength"]
        png_bytes = bin_chunk[start : start + length]

        with Image.open(io.BytesIO(png_bytes)) as src:
            src.load()
            mode = src.mode
            if max(src.size) > target_size:
                ratio = target_size / float(max(src.size))
                new_size = (max(1, int(round(src.size[0] * ratio))), max(1, int(round(src.size[1] * ratio))))
                resized = src.resize(new_size, Image.Resampling.LANCZOS)
            else:
                resized = src.copy()

        if mode not in {"RGB", "RGBA", "L", "LA", "P"}:
            resized = resized.convert("RGBA")
        out = io.BytesIO()
        resized.save(out, format="PNG", optimize=True)
        new_image_bytes[bv_idx] = out.getvalue()
        print(f"  image bv[{bv_idx}]: {length // 1024} KB -> {len(new_image_bytes[bv_idx]) // 1024} KB ({src.size[0]}x{src.size[1]} -> {resized.size[0]}x{resized.size[1]})")

    # Rebuild bin chunk preserving original bufferView ORDER (by current byteOffset).
    bv_order = sorted(range(len(doc["bufferViews"])), key=lambda i: doc["bufferViews"][i].get("byteOffset", 0))

    new_bin = bytearray()
    new_meta: dict[int, tuple[int, int]] = {}
    for idx in bv_order:
        # 4-byte align between bufferViews
        while len(new_bin) % 4:
            new_bin.append(0)
        offset = len(new_bin)
        bv = doc["bufferViews"][idx]
        if idx in new_image_bytes:
            data = new_image_bytes[idx]
        else:
            start = bv.get("byteOffset", 0)
            data = bytes(bin_chunk[start : start + bv["byteLength"]])
        new_bin.extend(data)
        new_meta[idx] = (offset, len(data))

    for idx, (off, length) in new_meta.items():
        doc["bufferViews"][idx]["byteOffset"] = off
        doc["bufferViews"][idx]["byteLength"] = length

    return doc, bytes(new_bin)


def append_accessor(doc: dict[str, Any], bin_data: bytearray, name: str, values: list[tuple[float, ...]], accessor_type: str) -> int:
    while len(bin_data) % 4:
        bin_data.append(0)
    offset = len(bin_data)
    flat = [component for row in values for component in row]
    bin_data.extend(struct.pack("<" + "f" * len(flat), *flat))
    byte_length = len(bin_data) - offset

    buffer_view_index = len(doc.setdefault("bufferViews", []))
    doc["bufferViews"].append({"buffer": 0, "byteOffset": offset, "byteLength": byte_length, "name": name})

    accessor: dict[str, Any] = {
        "bufferView": buffer_view_index,
        "componentType": 5126,
        "count": len(values),
        "type": accessor_type,
        "name": name,
    }
    if accessor_type == "SCALAR":
        scalars = [row[0] for row in values]
        accessor["min"] = [min(scalars)]
        accessor["max"] = [max(scalars)]
    accessor_index = len(doc.setdefault("accessors", []))
    doc["accessors"].append(accessor)
    return accessor_index


def add_animation(doc: dict[str, Any], bin_data: bytearray, node_index: int, name: str, keys: list[dict[str, Any]]) -> None:
    times = [(key["time"],) for key in keys]
    translations = [key.get("translation", (0.0, 0.0, 0.0)) for key in keys]
    rotations = [quat_from_euler_deg(*key.get("rotation", (0.0, 0.0, 0.0))) for key in keys]
    scales = [key.get("scale", (1.0, 1.0, 1.0)) for key in keys]

    input_accessor = append_accessor(doc, bin_data, f"{name}_time", times, "SCALAR")
    translation_accessor = append_accessor(doc, bin_data, f"{name}_translation", translations, "VEC3")
    rotation_accessor = append_accessor(doc, bin_data, f"{name}_rotation", rotations, "VEC4")
    scale_accessor = append_accessor(doc, bin_data, f"{name}_scale", scales, "VEC3")

    samplers = []
    channels = []
    for path, output_accessor in [
        ("translation", translation_accessor),
        ("rotation", rotation_accessor),
        ("scale", scale_accessor),
    ]:
        sampler_index = len(samplers)
        samplers.append({"input": input_accessor, "output": output_accessor, "interpolation": "LINEAR"})
        channels.append({"sampler": sampler_index, "target": {"node": node_index, "path": path}})

    doc.setdefault("animations", []).append({"name": name, "samplers": samplers, "channels": channels})


def inject_animations(doc: dict[str, Any], bin_chunk: bytes) -> tuple[dict[str, Any], bytes]:
    """Wrap scene roots with CatMotionRoot and append six named clips."""
    original_scene = doc.get("scene", 0)
    scene_nodes = doc.get("scenes", [{}])[original_scene].get("nodes", [])
    root_index = len(doc.setdefault("nodes", []))
    doc["nodes"].append({
        "name": "CatMotionRoot",
        "children": scene_nodes,
        "translation": [0, 0, 0],
        "rotation": [0, 0, 0, 1],
        "scale": [1, 1, 1],
    })
    doc["scenes"][original_scene]["nodes"] = [root_index]
    doc["animations"] = []
    bin_data = bytearray(bin_chunk)

    add_animation(doc, bin_data, root_index, "Idle", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 1.0, "translation": (0, 0.025, 0), "rotation": (1.0, 0, 1.2), "scale": (1.01, 1.01, 0.995)},
        {"time": 2.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 3.0, "translation": (0, 0.02, 0), "rotation": (-0.8, 0, -1.0), "scale": (1.008, 1.008, 0.997)},
        {"time": 4.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Observe", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, -8), "scale": (1, 1, 1)},
        {"time": 1.25, "translation": (0, 0.015, 0), "rotation": (0, 0, 8), "scale": (1.005, 1.005, 1)},
        {"time": 2.5, "translation": (0, 0, 0), "rotation": (0, 0, -8), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Pet", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 0.7, "translation": (0, -0.035, 0.02), "rotation": (8, 0, -4), "scale": (1.025, 1.025, 0.985)},
        {"time": 1.4, "translation": (0, -0.02, -0.01), "rotation": (5, 0, 4), "scale": (1.018, 1.018, 0.99)},
        {"time": 2.2, "translation": (0, 0.01, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Drink", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 0.75, "translation": (0, -0.08, 0.04), "rotation": (15, 0, 0), "scale": (1, 1, 1)},
        {"time": 1.5, "translation": (0, -0.085, 0.045), "rotation": (18, 0, -2), "scale": (1.004, 1.004, 0.998)},
        {"time": 2.25, "translation": (0, -0.08, 0.04), "rotation": (15, 0, 2), "scale": (1, 1, 1)},
        {"time": 3.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Eat", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 0.7, "translation": (0.02, -0.06, 0.035), "rotation": (12, 0, -5), "scale": (1.006, 1.006, 0.998)},
        {"time": 1.4, "translation": (-0.015, -0.065, 0.04), "rotation": (14, 0, 5), "scale": (1.012, 1.012, 0.992)},
        {"time": 2.1, "translation": (0.015, -0.06, 0.035), "rotation": (12, 0, -4), "scale": (1.006, 1.006, 0.998)},
        {"time": 2.8, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Happy", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 0.45, "translation": (0, 0.06, 0), "rotation": (0, 0, -8), "scale": (1.035, 1.035, 0.98)},
        {"time": 0.9, "translation": (0, 0.01, 0), "rotation": (0, 0, 7), "scale": (1.01, 1.01, 1)},
        {"time": 1.35, "translation": (0, 0.055, 0), "rotation": (0, 0, -6), "scale": (1.03, 1.03, 0.985)},
        {"time": 2.1, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])

    return doc, bytes(bin_data)


def inject_static_placeholders(doc: dict[str, Any], bin_chunk: bytes) -> tuple[dict[str, Any], bytes]:
    """Add 6 named animation slots that do NOTHING (single identity keyframe).

    Required because CatModel3DViewer hard-codes animationName="Idle" etc.
    With no animations, ModelNode.playAnimation may NPE. With identity-only
    keyframes, playback is a no-op visually but the API contract holds.
    """
    original_scene = doc.get("scene", 0)
    scene_nodes = doc.get("scenes", [{}])[original_scene].get("nodes", [])
    root_index = len(doc.setdefault("nodes", []))
    doc["nodes"].append({
        "name": "CatMotionRoot",
        "children": scene_nodes,
        "translation": [0, 0, 0],
        "rotation": [0, 0, 0, 1],
        "scale": [1, 1, 1],
    })
    doc["scenes"][original_scene]["nodes"] = [root_index]
    doc["animations"] = []
    bin_data = bytearray(bin_chunk)

    static_keys = [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 1.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ]
    for clip_name in ("Idle", "Observe", "Pet", "Drink", "Eat", "Happy"):
        add_animation(doc, bin_data, root_index, clip_name, static_keys)

    return doc, bytes(bin_data)


def prepare(doc: dict[str, Any], bin_chunk: bytes, *, compress: bool, animate: bool) -> tuple[dict[str, Any], bytes]:
    doc = copy.deepcopy(doc)
    if compress:
        print(f"[1/2] compressing textures (target {TARGET_TEXTURE_SIZE}px)...")
        doc, bin_chunk = compress_textures(doc, bin_chunk, TARGET_TEXTURE_SIZE)
        print(f"      bin chunk after compression: {len(bin_chunk) // 1024} KB")
    else:
        print("[1/2] skipping texture compression (preserving original fidelity)")
        print(f"      bin chunk preserved: {len(bin_chunk) // 1024} KB")

    if animate:
        print("[2/2] injecting 6 procedural root-node animations...")
        doc, bin_chunk = inject_animations(doc, bin_chunk)
    else:
        print("[2/2] injecting 6 STATIC placeholder animation slots (no movement)")
        doc, bin_chunk = inject_static_placeholders(doc, bin_chunk)
    print(f"      bin chunk after animation step: {len(bin_chunk) // 1024} KB")

    doc.setdefault("asset", {})["generator"] = "tools/prepare_kitty_animated_asset.py"
    doc.setdefault("extras", {})["maomaomao"] = {
        "role": "kitty-companion-asset",
        "source": "kitty.glb",
        "textureCompressed": compress,
        "proceduralAnimation": animate,
        "clips": ["Idle", "Observe", "Pet", "Drink", "Eat", "Happy"],
    }
    return doc, bin_chunk


def main() -> None:
    args = list(sys.argv[1:])
    compress = True
    animate = True
    while args and args[0].startswith("--"):
        flag = args.pop(0)
        if flag == "--no-compress":
            compress = False
        elif flag == "--no-animate":
            animate = False
        elif flag == "--pristine":
            compress = False
            animate = False
        else:
            raise RuntimeError(f"Unknown flag: {flag}")
    if len(args) != 2:
        raise RuntimeError(
            "Usage: python tools/prepare_kitty_animated_asset.py [--no-compress] [--no-animate] [--pristine] input.glb output.glb"
        )
    in_path = Path(args[0])
    out_path = Path(args[1])

    print(f"Reading {in_path}...")
    doc, bin_chunk = read_glb(in_path)
    print(f"  bufferViews: {len(doc.get('bufferViews', []))}, images: {len(doc.get('images', []))}, animations: {len(doc.get('animations', []))}")
    print(f"  bin chunk: {len(bin_chunk) // 1024} KB")
    print(f"  flags: compress={compress}, animate={animate}")

    doc, bin_chunk = prepare(doc, bin_chunk, compress=compress, animate=animate)
    write_glb(out_path, doc, bin_chunk)
    print(f"Wrote {out_path}: {out_path.stat().st_size // 1024} KB total, {len(doc['animations'])} animation clips")


if __name__ == "__main__":
    main()
