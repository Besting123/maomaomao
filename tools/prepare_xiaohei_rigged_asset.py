import copy
import json
import struct
import sys
from pathlib import Path
from typing import Any


JSON_CHUNK = 0x4E4F534A
BIN_CHUNK = 0x004E4942


ACTION_CLIP_SOURCES = {
    "Idle": "Survey",
    "Observe": "Survey",
    "Pet": "Walk",
    "Drink": "Survey",
    "Eat": "Walk",
    "Happy": "Run",
}


def align4(data: bytes, pad: bytes = b"\x00") -> bytes:
    return data + pad * ((4 - len(data) % 4) % 4)


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


def prepare_xiaohei_asset(doc: dict[str, Any]) -> dict[str, Any]:
    prepared = copy.deepcopy(doc)

    animations_by_name = {animation.get("name"): animation for animation in prepared.get("animations", [])}
    missing = sorted(set(ACTION_CLIP_SOURCES.values()) - animations_by_name.keys())
    if missing:
        raise RuntimeError(f"Missing source clips: {', '.join(missing)}")

    prepared["animations"] = []
    for action_name, source_name in ACTION_CLIP_SOURCES.items():
        animation = copy.deepcopy(animations_by_name[source_name])
        animation["name"] = action_name
        animation.setdefault("extras", {})["sourceClip"] = source_name
        prepared["animations"].append(animation)

    for material in prepared.get("materials", []):
        material["name"] = "xiaohei_dark_tabby_material"
        pbr = material.setdefault("pbrMetallicRoughness", {})
        pbr["baseColorFactor"] = [0.22, 0.2, 0.18, 1.0]
        pbr["metallicFactor"] = 0
        pbr["roughnessFactor"] = 0.64

    for mesh in prepared.get("meshes", []):
        mesh["name"] = "xiaohei_low_poly_rigged_mesh"

    for node in prepared.get("nodes", []):
        if node.get("name") == "fox":
            node["name"] = "XiaoheiRiggedCat"

    prepared.setdefault("asset", {})["generator"] = "tools/prepare_xiaohei_rigged_asset.py"
    prepared.setdefault("extras", {})["maomaomao"] = {
        "role": "first-rigged-xiaohei-validation-asset",
        "source": "app/src/main/assets/models/cat.glb",
        "clipMapping": ACTION_CLIP_SOURCES,
    }
    return prepared


def main() -> None:
    if len(sys.argv) != 3:
        raise RuntimeError("Usage: python tools/prepare_xiaohei_rigged_asset.py input.glb output.glb")

    input_path = Path(sys.argv[1])
    output_path = Path(sys.argv[2])
    doc, bin_chunk = read_glb(input_path)
    prepared = prepare_xiaohei_asset(doc)

    if not prepared.get("skins"):
        raise RuntimeError("Prepared asset must contain skins")
    if not prepared.get("animations"):
        raise RuntimeError("Prepared asset must contain named animations")

    write_glb(output_path, prepared, bin_chunk)
    print(f"Prepared rigged Xiaohei GLB: {output_path}")


if __name__ == "__main__":
    main()
