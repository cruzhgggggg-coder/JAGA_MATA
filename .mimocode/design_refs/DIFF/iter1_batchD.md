# Visual QA — iter1 Batch D (pengaturan, izin, kalibrasi, privasi)

Compared: `screens/<id>_<slug>.png` (design, 393×852) vs `app/iter1/<slug>.png` (1080×2400).
Copy verified against `copy/<slug>.md`. System status bar / home indicator / dynamic clock / font anti-aliasing / ≤4dp shifts / dummy values with matching format are ignored.
Colors and geometry below were verified by direct pixel sampling of both PNGs (and cross-checked against the design SVGs), not by eyeballing alone.

---

## pengaturan — **GAP**

- **[major] Mascot asset is the wrong drawing** (hero card, kiri). Design/canonical Kiko is **blue-bodied**: in the design mascot region (x=20..175, y=118..273) `#4A86E8` dominates (5,646px vs 2,599px cream) and it carries pink cheeks `#FFB4C2` (176px), navy iris `#1B3B6F` (910px), yellow feet `#FFD166` (196px), deep-blue inner wings `#2D70B7` (907px). The app mascot (bbox x=150..335, y=390..595) is **inverted** — cream `#FFF6E9` dominates (13,970px vs only 3,787px blue), pink cheeks are **0px** and navy iris is **0px** (eyes are drawn in `#12263A` instead), and there are no yellow feet. It reads as a cream owl-face with blue ear tufts rather than the blue Kiko. → Fix: swap in the canonical Kiko asset (`mascot/kiko.png`, or the mascot group in `screens/2823_pengaturan.svg`) so the body is blue-dominant with cream belly, pink cheeks, navy iris and yellow feet.
- Copy OK: title "Pengaturan", speech "Atur aku sesuai / kebutuhan / keluargamu, ya!" (3-line break identical to `copy/pengaturan.md`), rows "Notifikasi" / "Kalibrasi Kamera" / "Bahasa" / "Mode Gelap", value "Bahasa Indonesia", footer "JagaMata versi 1.0 · Jarak aman, mata senang!".
- Colors OK (pixel-verified, exact token matches): page `#F2F7FF`, hero gradient `#D6E6FF→#B7D2FF` (design SVG `paint11`; app samples `#C2D9FF` mid-gradient ✓), title/row labels `#12263A`, icon circles `#E1EFFF`, toggle ON `#4A86E8`, toggle OFF track `#E3E8EE` with white knob, "Bahasa Indonesia" value `#546271` (= `#12263A` @72% over white — exact), footer `#506071` (= `#12263A` @72% over bg — exact).
- Structure OK: back circle + centered H2 title, hero card (mascot + 3-line speech bubble with left tail), settings card with 4 rows in design order (Notifikasi toggle-on → Kalibrasi Kamera chevron → Bahasa value+chevron → Mode Gelap toggle-off), footer caption, bottom decorations (yellow sparkle + white cloud).

## izin — **GAP**

- **[major] "Kamera" toggle state mismatch** (baris 1, kanan). Design shows the toggle **ON** — blue `#4A86E8` pill at design x=303..354, y=309..339 with the knob on the right. The app toggle region (x=830..1010, y=740..870) contains **zero blue pixels** — only an `#E3E8EE` track with a white knob parked left-of-center (knob x≈884..896 inside track x≈854..974), i.e. OFF. → Fix: if this is a runtime permission state, grant camera permission before capture so the row matches the design's granted state; if not, wire row 1's toggle to the permission state so it renders ON as designed.
- **[minor] Row-4 "Kalibrasi Kamera (langkah berikut)" toggle uses disabled styling** (baris 4, kanan). The design's OFF toggle is a white knob (`#FFFFFF`, verified design pixels x=303..354 y=503..533) on an `#E3E8EE` track; the app renders a **dark-gray knob** (`#A5ACB4`, bbox x=869..910 y=1305..1346) on a slightly different track `#E2E5E7` — a disabled look that appears nowhere in the design. → Fix: use the standard off-switch style (white knob on `#E3E8EE`), matching both this design and pengaturan's "Mode Gelap" toggle.
- **[major] Mascot asset is the wrong drawing** (hero card) — same cream-dominant / missing-cheeks / ink-eyes defect as pengaturan.
- Copy OK: title "Izin & Akses", speech "Aku butuh 3 izin ini / supaya bisa menjaga / matamu, ya!", rows "Kamera" / "Tampilan di atas aplikasi" / "Notifikasi" / "Kalibrasi Kamera (langkah / berikut)" (2-line break identical), value "Wajib ketiganya", footer "Semua izin bisa diatur ulang kapan saja." — all match `copy/izin.md`.
- Structure OK: 4 rows in design order with camera / phone / bell / ruler icons matching; chevrons on rows 2–3; toggles on rows 1 & 4; decorations present.

## kalibrasi — **GAP**

- **[major] Gauge arcs do not form the design's semicircle** (kartu gauge, atas). The design draws one continuous arc band of radius ≈100 centered at (196.5, 495): coral `#FF7B6B` left segment (bbox x=88..154 y=400..503), yellow `#FFD166` top segment (x≈158..235 y≈390..408), mint `#5FD4A7` right segment (x=238..304 y=400..503) — mirror-symmetric 66×103 side segments with the yellow bridging the top of the same band. In the app the yellow segment is **detached and floats above** the side arcs (app yellow bbox y=968..1024 vs coral/mint tops at y=1100 — a 76px vertical gap) and the sides are not symmetric (coral 172×124 vs mint 176×176). → Fix: draw the three segments as spans of one circular-arc band (e.g. `drawArc` over a shared oval with three colored sweep ranges) so their ends align on the same circle.
- **[major] Gauge labels overlap the mint arc** (kartu gauge, tengah). The ink of "Target 30 cm" extends to x=732, inside the mint arc's bbox (x=572..748), and "posisikan sesuai penggaris" crosses it as well; the design keeps both lines centered in the open area inside the arc with clean separation. → Fix: keep the arc band outside the text block (larger inner radius / smaller text) so the labels sit fully inside the arc's open area.
- **[minor] Gauge knob at the wrong position** (kartu gauge). Design knob (white circle + dark ring, 26px, center (273, 431)) sits on the **mint arc's upper-right** — consistent with the shown "52 cm" reading being past the 30 cm target; the app knob (bbox x=164..237 y=1217..1290) sits at the **lower-left tip of the coral arc**, contradicting the 52 cm value shown in the hero card. → Fix: map the current distance to the arc angle and place the knob on the band proportionally.
- **[minor] Mascot is missing its ruler prop** (hero card, kanan-bawah). Design Kiko holds a yellow ruler (`#FFD166`, 452px in bbox x=296..351 y=292..344); the app mascot shows only a small yellow beak (50px, x=886..906 y=760..774) — no ruler. → Fix: add the ruler prop to the kalibrasi mascot illustration.
- **[major] Mascot asset is the wrong drawing** (hero card) — shared cream-dominant defect (see pengaturan).
- Tolerated and NOT counted as gaps: "0%" vs design "67%" (dummy value, format `N%` matches) and the play ▶ vs design pause ⏸ icon on the dark circular button (follows the not-started state).
- Copy OK: "Kalibrasi Kamera", "Pegang HP 30 cm dari wajah", "Pakai penggaris, ya", "52 cm", "Target 30 cm", "posisikan sesuai penggaris", "Stabil 1,5 detik — tersimpan otomatis", "Progres kalibrasi" — all match `copy/3323_kalibrasi.md`.
- Structure OK: left-aligned H1 title + green status pill (with dark dot) + yellow sparkle; hero card (white "Pakai penggaris, ya" pill with camera icon, dashed target square with smiley, "52 cm" pill with dotted connectors, phone circle, sparkle); mint status pill with check; stepper row (smiley — ruler — phone with dotted lines; circles `#E1EFFF` ✓); progress card (clock circle, label, percentage, dark circular button).

## privasi — **GAP**

- **[major] Mascot asset is the wrong drawing** (hero card) — shared defect (app mascot bbox: cream 13,970px vs blue 3,787px, pink cheeks 0px, navy iris 0px — same asset as pengaturan/izin).
- Copy OK: title "Privasi & Data", speech "Semua data tetap di / HP-mu, tidak dikirim ke / mana-mana.", rows "0 foto tersimpan" / "Tanpa internet & server" / "Hapus semua / data" (2-line break identical) / "Kebijakan privasi", value "Sesuai UU PDP", footer "JagaMata v1.0 · Privasi by design — 0 foto keluar HP." — all match `copy/privasi.md`.
- Toggles OK: row 1 ON (blue `#4A86E8` detected at y=760..840 ✓) and row 4 OFF (gray at y=1300..1380 ✓) — both match the design.
- Structure OK: 4 rows in design order with camera / globe / sliders / lock icons; "Sesuai UU PDP" value + chevron on row 3; footer; bottom decorations (sparkle + cloud).

---

## Cross-screen diffs (all 4 screens)

- **[minor] Display text renders lighter than the design's Baloo 2 Bold** (semua judul, "Target 30 cm", angka persen). Measured stem/cap-height ratio on "Target 30 cm": design 0.23 (Baloo 2 Bold 24) vs app 0.135 — the app's titles and display numbers are too light, consistent with a system-sans fallback or Baloo 2 applied at Regular instead of Bold (design SVGs specify `font-family="Baloo 2" font-weight="700"`). Corroborates batch A's cross-screen font-family finding on the other screens. → Fix: bundle Baloo 2 (700) + Nunito (500/600/700) and map them to the design's Display/H1/H2/Number/Button and Body/Caption/Micro text styles.
- Tolerated and NOT reported: system status bar (10:15 + shield icon), home indicator bar, clock values, dummy numbers with matching format ("0%" vs "67%", "52 cm"), ≤4dp shifts.

---

## Summary

| slug | verdict | major | minor | headline issue |
|---|---|---|---|---|
| pengaturan | GAP | 1 | 0 | mascot asset is the wrong drawing (cream-dominant, no cheeks) |
| izin | GAP | 2 | 1 | Kamera toggle shows OFF + wrong mascot |
| kalibrasi | GAP | 3 | 2 | gauge arcs detached/misshapen + labels overlap arc |
| privasi | GAP | 1 | 0 | mascot asset is the wrong drawing |
| **cross-screen** | — | 0 | 1 | display text lighter than Baloo 2 Bold |

Copy accuracy vs `copy/*.md`: **4/4 screens exact** (wording + line breaks). Color token accuracy: **exact on every sampled token** (`#F2F7FF`, `#D6E6FF→#B7D2FF`, `#12263A`, `#12263A@72%`, `#E1EFFF`, `#4A86E8`, `#E3E8EE`, `#5FD4A7`, `#FFD166`). Row order, icons, chevrons and bottom decorations all match the design on every screen.

Highest-leverage fixes for the next round, in order:
1. Replace the mascot with the canonical Kiko asset (blue body, cream belly, pink cheeks `#FFB4C2`, navy iris `#1B3B6F`, yellow feet) — fixes one major item on all 4 screens; add the ruler prop on kalibrasi.
2. Rebuild the kalibrasi gauge as one semicircular arc band with the three segments on a shared circle, move the knob onto the mint segment per the distance value, and keep the labels inside the arc — fixes 2 major + 1 minor.
3. izin: render the Kamera toggle ON (or capture after granting the permission) and use the normal off-switch style (white knob on `#E3E8EE`) for row 4.
4. Apply the design's Baloo 2 Bold / Nunito font families and weights (shared with batch A).
