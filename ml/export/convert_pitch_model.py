#!/usr/bin/env python3
"""Konversi model pitch → TFLite (PRD §5.5 / §2.24, DEV-34).

Jalur konversi yang terbukti jalan (diuji 2026-11-19):

    ONNX ──onnx-tf──► TF SavedModel ──TFLiteConverter+quant──► pitch_model.tflite

Dijalankan DI LUAR folder app/ supaya APK tidak bengkak sebelum model siap.

Pemakaian (dari repo JagaMata-ML yang sudah diunduh ke ml/export/):

    uv run --python 3.11 \
      --with "onnx==1.15.0" --with onnx-tf \
      --with "tensorflow-cpu==2.15.1" --with "tensorflow-probability==0.23.0" \
      python ml/export/convert_pitch_model.py --onnx ml/export/jagamata_pitch_tuned.onnx

Jalur alternatif dari checkpoint PyTorch (butuh torch):

    python ml/export/convert_pitch_model.py --pt ml/export/pitch_best.pt

Spesifikasi keluaran (dicek dari export_summary.json rekan tim):
    input : input_face_image  [1,3,224,224] float32 (NCHW, ImageNet norm)
    output: pose_3d_degrees   [1,3] → pitch, yaw, roll (derajat)

Setelah file ada di assets/, aktifkan lewat BuildConfig:
    buildConfigField("boolean", "USE_TFLITE_PITCH", "true")
Tanpa file itu aplikasi otomatis memakai LandmarkPitchBackend (F9).
"""

from __future__ import annotations

import argparse
import shutil
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ASSETS_DIR = REPO_ROOT / "app" / "src" / "main" / "assets"
EXPORT_DIR = REPO_ROOT / "ml" / "export"
TARGET_TFLITE = ASSETS_DIR / "pitch_model.tflite"


def _require(module: str, hint: str) -> object:
    try:
        return __import__(module)
    except ImportError:
        print(f"ERROR: modul '{module}' belum terpasang. {hint}", file=sys.stderr)
        raise SystemExit(2)


def pt_to_onnx(pt_path: Path) -> Path:
    """Tahap opsional: PyTorch → ONNX (bila rekan tim mengirim .pt)."""
    torch = _require("torch", "pip install torch")
    onnx_path = EXPORT_DIR / "pitch_best.onnx"
    model = torch.load(pt_path, map_location="cpu")
    if hasattr(model, "eval"):
        model.eval()
    dummy = torch.randn(1, 3, 224, 224)
    torch.onnx.export(
        model,
        dummy,
        str(onnx_path),
        input_names=["input_face_image"],
        output_names=["pose_3d_degrees"],
        dynamic_axes={
            "input_face_image": {0: "batch_size"},
            "pose_3d_degrees": {0: "batch_size"},
        },
        opset_version=17,
    )
    print(f"ONNX ditulis: {onnx_path}")
    return onnx_path


def onnx_to_saved_model(onnx_path: Path, saved_model_dir: Path) -> None:
    onnx = _require("onnx", "pip install onnx==1.15.0")
    onnx_tf = _require("onnx_tf", "pip install onnx-tf  (dan onnx==1.15.0, tensorflow-cpu==2.15.1)")
    model = onnx.load(str(onnx_path))
    # Cek struktur I/O sesuai kontrak §5.5
    inputs = [(i.name, [d.dim_value for d in i.type.tensor_type.shape.dim]) for i in model.graph.input]
    outputs = [(o.name, [d.dim_value for d in o.type.tensor_type.shape.dim]) for o in model.graph.output]
    print(f"input : {inputs}")
    print(f"output: {outputs}")
    onnx.checker.check_model(model)
    print("onnx.checker: OK")
    rep = onnx_tf.prepare(model)
    rep.export_graph(str(saved_model_dir))
    print(f"SavedModel ditulis: {saved_model_dir}")


def saved_model_to_tflite(saved_model_dir: Path, out_path: Path) -> None:
    tf = _require("tensorflow", "pip install tensorflow-cpu==2.15.1")
    converter = tf.lite.TFLiteConverter.from_saved_model(str(saved_model_dir))
    # Dynamic range quantization → ~5x lebih kecil (8.1 MB → ~1.6 MB)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    data = converter.convert()
    out_path.write_bytes(data)
    print(f"TFLite ditulis: {out_path} ({len(data)} byte)")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    src = parser.add_mutually_exclusive_group(required=True)
    src.add_argument("--onnx", type=Path, help="Model ONNX hasil export rekan tim")
    src.add_argument("--pt", type=Path, help="Checkpoint PyTorch (pitch_best.pt)")
    parser.add_argument(
        "--keep-saved-model",
        action="store_true",
        help="Simpan SavedModel antara di ml/export/saved_model",
    )
    args = parser.parse_args()

    EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    ASSETS_DIR.mkdir(parents=True, exist_ok=True)

    if args.pt:
        if not args.pt.exists():
            print(f"ERROR: file tidak ada: {args.pt}", file=sys.stderr)
            return 2
        onnx_path = pt_to_onnx(args.pt)
    else:
        if not args.onnx.exists():
            print(f"ERROR: file tidak ada: {args.onnx}", file=sys.stderr)
            return 2
        onnx_path = args.onnx

    saved_model_dir = EXPORT_DIR / "saved_model"
    onnx_to_saved_model(onnx_path, saved_model_dir)
    saved_model_to_tflite(saved_model_dir, TARGET_TFLITE)

    if not args.keep_saved_model and saved_model_dir.exists():
        shutil.rmtree(saved_model_dir, ignore_errors=True)

    size = TARGET_TFLITE.stat().st_size
    print(f"Model siap pakai: {TARGET_TFLITE} ({size / 1_048_576:.2f} MB)")
    print('Aktifkan dengan: buildConfigField("boolean", "USE_TFLITE_PITCH", "true")')
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
