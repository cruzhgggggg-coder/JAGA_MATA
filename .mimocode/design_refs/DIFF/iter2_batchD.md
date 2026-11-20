# Visual QA — iter2 Batch D (pengaturan, izin, kalibrasi, privasi)

Compared: `screens/<id>_<slug>.png` (design, 393×852) vs `app/iter2/<slug>.png` (1080×2400). Copy verified against `copy/<slug>.md`. Tolerated: status bar/clock, font anti-aliasing, ≤4dp shifts, dummy values with matching format ("0%" vs "67%"), play ▶ vs pause ⏸ (follows the not-started state). Colors and geometry below verified by direct pixel sampling of both PNGs.

---

## pengaturan — **PASS-MINOR**

- **iter1 defect (wrong mascot) — FIXED.** App hero mascot is now blue-dominant Kiko: blue `#4A86E8` 32,037px vs cream 14,487px (ratio 2.21 = design's 5,723:2,592), pink cheeks `#FFB4C2` 1,075px (bbox 144,464–338,482), navy iris 6,133px, yellow feet 1,281px, deep-blue wings 5,216px, zero ink-colored eyes. Matches the design's token set.
- Copy OK: "Pengaturan", "Atur aku sesuai / kebutuhan / keluargamu, ya!", rows "Notifikasi" / "Kalibrasi Kamera" / "Bahasa" / "Mode Gelap", "Bahasa Indonesia", footer "JagaMata versi 1.0 · Jarak aman, mata senang!" — exact vs `copy/pengaturan.md`.
- Toggles: Notifikasi ON (track `#4A86E8`, knob right — scanline y=795: blue 849–910 + white knob 912–973). Mode Gelap OFF (track `#E3E8EE`, knob left). States match design.
- Structure/colors OK: back circle + centered H2, hero gradient card (mascot + 3-line speech bubble with left tail), 4-row card in design order, footer, sparkle + cloud decorations.
- **[minor] OFF-state switch knob is undersized** (baris 4 "Mode Gelap"). Design knob is 27px on a 53×30px track (fills 90% of track height); the app knob is only ~45px on a 137×68px track (66% of track height ≈ 17dp vs design ≈ 26dp) — note the app's own ON knob is 62px/24dp, so OFF and ON are inconsistent with each other. → Fix: give the OFF state the same knob diameter as ON (~24–26dp, track height minus ~2dp padding).

## izin — **PASS-MINOR**

- **iter1 defect (Kamera toggle OFF) — FIXED.** Row-1 toggle is ON: 6,715 blue `#4A86E8` px in the toggle region (iter1: 0), scanline y=795 shows blue pill 849–983 with white knob parked right (912–973). Matches design's granted state.
- **iter1 defect (row-4 disabled knob) — FIXED.** Row-4 knob is now pure white `#FFFFFF` (1,022px in knob box) on `#E3E8EE` (9,163px track); `#A5ACB4` dark-gray count dropped from 1,348px (iter1) to **0**. Standard off-switch styling restored.
- **iter1 defect (wrong mascot) — FIXED.** Identical corrected Kiko asset as pengaturan (blue 32,037 / cream 14,487 / pink 1,075 / navy 6,133 / yellow-feet 1,281).
- Copy OK: "Izin & Akses", "Aku butuh 3 izin ini / supaya bisa menjaga / matamu, ya!", rows "Kamera" / "Tampilan di atas aplikasi" / "Notifikasi" / "Kalibrasi Kamera (langkah / berikut)", "Wajib ketiganya", footer "Semua izin bisa diatur ulang kapan saja." — exact vs `copy/izin.md`.
- Structure OK: 4 rows in design order with camera / phone / bell / ruler icons, chevrons on rows 2–3, toggles on rows 1 & 4, decorations present.
- **[minor] OFF-state switch knob is undersized** (baris 4) — same component defect as pengaturan (white knob ~17dp vs design ~26dp). → Fix: match ON-state knob diameter.

## kalibrasi — **PASS**

- **iter1 defect (gauge arcs detached/misshapen) — FIXED.** Segments now lie on one shared circle: coral `#FF7B6B` bbox (254,1003–431,1276) = 177×273 and mint `#5FD4A7` bbox (648,1003–825,1276) = 177×273 are mirror-identical (iter1: 172×124 vs 176×176), yellow `#FFD166` bbox (440,968–639,1025) = 199×57 bridges the top of the same band (scales to design's 74×21). Segment gaps 9px (≈3dp) match the design's 5px gaps. No floating yellow.
- **iter1 defect (labels overlap mint arc) — FIXED.** Per-row scan: "Target 30 cm" ink max x=717 vs mint arc min x=756 (39px gap); "posisikan sesuai penggaris" ink max x=741 vs mint min x=780 (39px gap). Both lines sit fully inside the arc's open area.
- **iter1 defect (gauge knob wrong position) — FIXED.** Knob (white interior 684px + dark ring 501px in probe box) sits on the **mint segment's upper-right** (center ≈ (762,1104), on-band radius 278 ≈ design's 100×2.75=275, angle ≈37° vs design 40°) — consistent with the shown distance past the 30 cm target.
- **iter1 defect (missing ruler prop) — FIXED.** Kalibrasi mascot holds a yellow `#FFD166` ruler: 3,385px yellow in mascot region bbox (847,724–973,843) vs iter1's 50px beak-only; design ruler is 452px at the same relative spot.
- **iter1 defect (wrong mascot) — FIXED.** Blue-dominant Kiko (14,799 blue / 4,174 cream / 487 pink / 1,903 navy).
- Copy OK: "Kalibrasi Kamera", "Pegang HP 30 cm dari wajah", "Pakai penggaris, ya", "52 cm", "Target 30 cm", "posisikan sesuai penggaris", "Stabil 1,5 detik — tersimpan otomatis", "Progres kalibrasi" — exact vs `copy/3323_kalibrasi.md`. Tolerated: "0%" vs "67%" (dummy, format `N%`), ▶ vs ⏸ (not-started state).
- Structure OK: H1 + green status pill + sparkle; hero card (camera pill, dashed target square + smiley, "52 cm" + dotted connectors, phone circle); mint check pill; stepper (smiley — ruler in yellow active circle — phone, dotted lines); progress card (clock circle, label, percent, dark button).
- No remaining gaps on this screen.

## privasi — **PASS-MINOR**

- **iter1 defect (wrong mascot) — FIXED.** Corrected Kiko asset (blue 32,037 / cream 14,487 / pink cheeks 1,075 / navy iris 6,133 / yellow feet 1,281) — same as pengaturan/izin.
- Copy OK: "Privasi & Data", "Semua data tetap di / HP-mu, tidak dikirim ke / mana-mana.", rows "0 foto tersimpan" / "Tanpa internet & server" / "Hapus semua / data" / "Kebijakan privasi", "Sesuai UU PDP", footer "JagaMata v1.0 · Privasi by design — 0 foto keluar HP." — exact vs `copy/privasi.md`.
- Toggles OK: row 1 ON (blue 6,715px ✓) and row 4 OFF (blue 0px ✓) — states match design.
- Structure OK: 4 rows with camera / globe / sliders / lock icons, value + chevron on row 3, footer, sparkle + cloud.
- **[minor] OFF-state switch knob is undersized** (baris 4 "Kebijakan privasi") — same shared component defect. → Fix: match ON-state knob diameter.

---

## Cross-screen diffs (all 4 screens)

- **iter1 defect (display text lighter than Baloo 2 Bold) — FIXED.** Stem/cap-height ratios now match the design: "Target 30 cm" T-glyph design 0.200 (stem 3 / cap 15) vs app 0.179 (stem 7 / cap 39 = 2.7dp vs 3.0dp — within tolerance); "Pengaturan" P-glyph design 0.182 vs app 0.207. Iter1's app ratio was 0.135.
- **[minor] Switch OFF knob diameter** (pengaturan / izin / privasi — the three OFF toggles). Shared defect, reported per screen above. ON knobs are consistent with the design (app 24dp vs design 26dp).
- Tolerated and NOT reported: system status bar, home indicator, clock values, dummy numbers with matching format, ≤4dp shifts.

---

## Summary

| slug | verdict | iter1 defects | fixed | remaining | headline |
|---|---|---|---|---|---|
| pengaturan | PASS-MINOR | 1 major | 1/1 | 0 major, 1 minor | mascot fixed; OFF knob undersized |
| izin | PASS-MINOR | 2 major, 1 minor | 3/3 | 0 major, 1 minor | Kamera ON + white knob + mascot fixed; OFF knob undersized |
| kalibrasi | PASS | 3 major, 2 minor | 5/5 | 0 | gauge/knob/ruler/mascot all fixed |
| privasi | PASS-MINOR | 1 major | 1/1 | 0 major, 1 minor | mascot fixed; OFF knob undersized |
| **cross-screen** | — | 1 minor | 1/1 | 1 minor (OFF knob) | Baloo 2 Bold weight fixed; switch knob sizing |

Copy accuracy vs `copy/*.md`: **4/4 screens exact** (wording + line breaks). Color token accuracy: **exact on every sampled token** (`#4A86E8`, `#E3E8EE`, `#FF7B6B`, `#FFD166`, `#5FD4A7`, `#FFB4C2`, `#1B3B6F`, `#2D70B7`).

Highest-leverage fix for the next round:
1. Switch OFF knob: use the same knob diameter as the ON state (~24–26dp = track height minus ~2dp padding) on every off-switch — one shared component fix clears the last minor item on 3 screens.
