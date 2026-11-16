#!/usr/bin/env python3
"""metrics.py — hitung KPI K1/K2/K3 dari CSV (PRD BAB 4, DEV-26).

K1  Sensitivitas jarak <30 cm  : target >= 90 %
K2  FPR jarak >=35 cm          : target <= 10 %
K3  MAE estimasi jarak         : target <= 6 cm

Pemakaian:
    python metrics.py --labels ml/ground_truth/labels_template.csv \
                      --predictions ml/ground_truth/predictions_template.csv

Format labels.csv     : file,distance_cm,light,lux,device
Format predictions.csv: file,predicted_cm

Model sudah pretrained — file ini hanya evaluasi/bukti, bukan training.
"""

from __future__ import annotations

import argparse
import csv
import sys
from dataclasses import dataclass
from pathlib import Path

# Ambang teknis PRD §2.3
CLOSE_THRESHOLD_CM = 30.0
EXIT_THRESHOLD_CM = 35.0

# Target KPI PRD BAB 4
K1_MIN_SENSITIVITY = 0.90
K2_MAX_FPR = 0.10
K3_MAX_MAE_CM = 6.0


@dataclass
class Sample:
    file: str
    truth_cm: float
    predicted_cm: float


def read_csv(path: Path) -> dict[str, dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as fh:
        rows = list(csv.DictReader(fh))
    keyed: dict[str, dict[str, str]] = {}
    for row in rows:
        name = (row.get("file") or "").strip()
        if not name:
            continue
        keyed[name] = row
    return keyed


def build_samples(labels_path: Path, predictions_path: Path) -> list[Sample]:
    labels = read_csv(labels_path)
    predictions = read_csv(predictions_path)
    samples: list[Sample] = []
    for name, pred_row in predictions.items():
        label_row = labels.get(name)
        if label_row is None:
            continue
        try:
            truth = float(label_row["distance_cm"])
            pred = float(pred_row["predicted_cm"])
        except (KeyError, ValueError):
            continue
        samples.append(Sample(file=name, truth_cm=truth, predicted_cm=pred))
    return samples


def sensitivity(samples: list[Sample]) -> tuple[float, int, int]:
    """Porsi sampel dengan truth <30 cm yang benar diprediksi <30 cm."""
    positives = [s for s in samples if s.truth_cm < CLOSE_THRESHOLD_CM]
    if not positives:
        return 0.0, 0, 0
    true_pos = sum(1 for s in positives if s.predicted_cm < CLOSE_THRESHOLD_CM)
    return true_pos / len(positives), true_pos, len(positives)


def false_positive_rate(samples: list[Sample]) -> tuple[float, int, int]:
    """Porsi sampel dengan truth >=35 cm yang salah diprediksi <30 cm."""
    negatives = [s for s in samples if s.truth_cm >= EXIT_THRESHOLD_CM]
    if not negatives:
        return 0.0, 0, 0
    false_pos = sum(1 for s in negatives if s.predicted_cm < CLOSE_THRESHOLD_CM)
    return false_pos / len(negatives), false_pos, len(negatives)


def mae(samples: list[Sample]) -> tuple[float, int]:
    if not samples:
        return 0.0, 0
    total = sum(abs(s.predicted_cm - s.truth_cm) for s in samples)
    return total / len(samples), len(samples)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--labels", required=True, type=Path)
    parser.add_argument("--predictions", required=True, type=Path)
    parser.add_argument("--out", type=Path, default=None, help="Tulis hasil ke file")
    args = parser.parse_args(argv)

    if not args.labels.exists():
        print(f"ERROR: labels tidak ditemukan: {args.labels}", file=sys.stderr)
        return 2
    if not args.predictions.exists():
        print(f"ERROR: predictions tidak ditemukan: {args.predictions}", file=sys.stderr)
        return 2

    samples = build_samples(args.labels, args.predictions)
    if not samples:
        print("ERROR: tidak ada pasangan label/prediksi yang cocok", file=sys.stderr)
        return 3

    sens, tp, n_pos = sensitivity(samples)
    fpr, fp, n_neg = false_positive_rate(samples)
    err, n_all = mae(samples)

    lines = [
        "LAPORAN KPI K1/K2/K3 — JagaMata",
        f"Sampel terpakai : {n_all}",
        "",
        f"K1 Sensitivitas (<{CLOSE_THRESHOLD_CM:.0f} cm) : {sens * 100:.1f} %  ({tp}/{n_pos})  "
        f"target >= {K1_MIN_SENSITIVITY * 100:.0f} %  -> "
        f"{'LULUS' if sens >= K1_MIN_SENSITIVITY else 'GAGAL'}",
        f"K2 FPR (truth >= {EXIT_THRESHOLD_CM:.0f} cm)   : {fpr * 100:.1f} %  ({fp}/{n_neg})  "
        f"target <= {K2_MAX_FPR * 100:.0f} %  -> "
        f"{'LULUS' if fpr <= K2_MAX_FPR else 'GAGAL'}",
        f"K3 MAE jarak                : {err:.2f} cm  "
        f"target <= {K3_MAX_MAE_CM:.0f} cm  -> "
        f"{'LULUS' if err <= K3_MAX_MAE_CM else 'GAGAL'}",
    ]
    report = "\n".join(lines) + "\n"
    print(report, end="")
    if args.out:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(report, encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
