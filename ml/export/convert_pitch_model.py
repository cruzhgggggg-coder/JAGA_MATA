#!/usr/bin/env python3
"""Konversi model pitch: pitch_best.pt -> ONNX -> TFLite (PRD §5.5, DEV-34).

Dijalankan DI LUAR folder app/ supaya APK tidak bengkak saat model belum ada.
Kontrak: rekan tim ML menyerahkan `pitch_best.pt` (MAE <= 5 derajat) berdasarkan
`PANDUAN_TRAINING_ML_JagaMata.md`. Skrip ini mengubahnya menjadi
`app/src/main/assets/pitch_model.tflite` untuk dipakai TflitePitchBackend.

Pemakaian:
    python ml/export/convert_pitch_model.py --pt ml/export/pitch_best.pt

Prasyarat:  pip install torch onnx onnx-tf tensorflow
            (atau ai-edge-linter untuk verifikasi)

Setelah file .tflite ada di assets/, aktifkan backend lewat BuildConfig:
    buildConfigField("boolean", "USE_TFLITE_PITCH", "true")
Tanpa file itu, aplikasi otomatis memakai LandmarkPitchBackend (F9).
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ASSETS_DIR = REPO_ROOT / "app" / "src" / "main" / "assets"
EXPORT_DIR = REPO_ROOT / "ml" / "export"
TARGET_TFLITE = ASSETS_DIR / "pitch_model.tflite"


def run(cmd: list[str]) -> None:
    print("$", " ".join(str(c) for c in cmd))
    subprocess.run(cmd, check=True)


def convert(pt_path: Path) -> Path:
    if not pt_path.exists():
        print(f"ERROR: file .pt tidak ditemukan: {pt_path}", file=sys.stderr)
        return sys.exit(2)

    EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    onnx_path = EXPORT_DIR / "pitch_best.onnx"

    # 1. PyTorch -> ONNX
    try:
        import torch  # type: ignore
    except ImportError:
        print("ERROR: torch belum terpasang (pip install torch)", file=sys.stderr)
        return sys.exit(2)

    model = torch.load(pt_path, map_location="cpu")
    if hasattr(model, "eval"):
        model.eval()
    dummy = torch.randn(1, 3, 224, 224)
    torch.onnx.export(
        model,
        dummy,
        onnx_path,
        input_names=["input"],
        output_names=["pitch"],
        dynamic_axes={"input": {0: "batch"}, "pitch": {0: "batch"}},
        opset_version=17
    )
    print(f"ONNX ditulis: {onnx_path}")

    # 2. ONNX -> TF SavedModel -> TFLite
    try:
        import onnx  # type: ignore  # noqa: F401
        from onnx_tf.backend import prepare  # type: ignore
    except ImportError:
        print(
            "ERROR: onnx / onnx-tf belum terpasang (pip install onnx onnx-tf)",
            file=sys.stderr
        )
        return sys.exit(2)

    saved_model_dir = EXPORT_DIR / "saved_model"
    tf_rep = prepare(onnx.load(str(onnx_path)))
    tf_rep.export_graph(str(saved_model_dir))

    try:
        import tensorflow as tf  # type: ignore
    except ImportError:
        print("ERROR: tensorflow belum terpasang (pip install tensorflow)", file=sys.stderr)
        return sys.exit(2)

    converter = tf.lite.TFLiteConverter.from_saved_model(str(saved_model_dir))
    tflite_bytes = converter.convert()

    tmp_tflite = EXPORT_DIR / "pitch_model.tflite"
    tmp_tflite.write_bytes(tflite_bytes)
    print(f"TFLite ditulis: {tmp_tflite} ({len(tflite_bytes)} byte)")

    # 3. Salin ke assets/
    ASSETS_DIR.mkdir(parents=True, exist_ok=True)
    shutil.copy2(tmp_tflite, TARGET_TFLITE)
    print(f"Model siap pakai: {TARGET_TFLITE}")
    print("Aktifkan dengan: buildConfigField(\"boolean\", \"USE_TFLITE_PITCH\", \"true\")")
    return TARGET_TFLITE


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--pt",
        type=Path,
        default=EXPORT_DIR / "pitch_best.pt",
        help="Path ke model PyTorch hasil training (pitch_best.pt)"
    )
    args = parser.parse_args()
    convert(args.pt)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
