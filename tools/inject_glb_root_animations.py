import json
import math
import struct
import sys
from pathlib import Path
from typing import Any


JSON_CHUNK = 0x4E4F534A
BIN_CHUNK = 0x004E4942


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
    json_doc = None
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
        raise RuntimeError("Missing JSON chunk")
    return json_doc, bin_chunk


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


def write_glb(path: Path, doc: dict[str, Any], bin_data: bytes) -> None:
    doc.setdefault("buffers", [{}])[0]["byteLength"] = len(bin_data)
    json_bytes = align4(json.dumps(doc, ensure_ascii=False, separators=(",", ":")).encode("utf-8"), b" ")
    bin_bytes = align4(bin_data, b"\x00")
    total_length = 12 + 8 + len(json_bytes) + 8 + len(bin_bytes)
    out = bytearray()
    out.extend(struct.pack("<4sII", b"glTF", 2, total_length))
    out.extend(struct.pack("<II", len(json_bytes), JSON_CHUNK))
    out.extend(json_bytes)
    out.extend(struct.pack("<II", len(bin_bytes), BIN_CHUNK))
    out.extend(bin_bytes)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(out)


def create_animated_doc(doc: dict[str, Any], bin_chunk: bytes) -> tuple[dict[str, Any], bytes]:
    doc = json.loads(json.dumps(doc))
    original_scene = doc.get("scene", 0)
    scene_nodes = doc.get("scenes", [{}])[original_scene].get("nodes", [])
    root_index = len(doc.setdefault("nodes", []))
    doc["nodes"].append({"name": "CatMotionRoot", "children": scene_nodes, "translation": [0, 0, 0], "rotation": [0, 0, 0, 1], "scale": [1, 1, 1]})
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
    add_animation(doc, bin_data, root_index, "Pet", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
        {"time": 0.7, "translation": (0, -0.035, 0.02), "rotation": (8, 0, -4), "scale": (1.025, 1.025, 0.985)},
        {"time": 1.4, "translation": (0, -0.02, -0.01), "rotation": (5, 0, 4), "scale": (1.018, 1.018, 0.99)},
        {"time": 2.2, "translation": (0, 0.01, 0), "rotation": (0, 0, 0), "scale": (1, 1, 1)},
    ])
    add_animation(doc, bin_data, root_index, "Observe", [
        {"time": 0.0, "translation": (0, 0, 0), "rotation": (0, 0, -8), "scale": (1, 1, 1)},
        {"time": 1.25, "translation": (0, 0.015, 0), "rotation": (0, 0, 8), "scale": (1.005, 1.005, 1)},
        {"time": 2.5, "translation": (0, 0, 0), "rotation": (0, 0, -8), "scale": (1, 1, 1)},
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


def main():
    if len(sys.argv) != 3:
        raise RuntimeError("Usage: python tools/inject_glb_root_animations.py input.glb output.glb")
    input_path = Path(sys.argv[1])
    output_path = Path(sys.argv[2])
    doc, bin_chunk = read_glb(input_path)
    animated_doc, animated_bin = create_animated_doc(doc, bin_chunk)
    write_glb(output_path, animated_doc, animated_bin)
    print(f"Animated GLB exported: {output_path}")


if __name__ == "__main__":
    main()
