# Visual QA — iter2 Batch B (beranda, pantau_live, istirahat, statistik_harian)

Design ref 393×852 vs app screenshot 1080×2400. System status bar / gesture bar / clock tolerated throughout (not listed as gaps). Both iter1 defects verified against `app/iter2` at native resolution (3× zoom on clipped regions).

---

## beranda — `screens/1577_beranda.png` vs `app/iter2/beranda.png`

**Verdict: GAP** · **Iter1 defect (streak subtitle clipped): FIXED**

- **Iter1 fix verified:** streak subtitle "streak berturut-turut, pertahankan ya!" now renders fully inside the streak card — all glyphs and descenders visible, matching the design's two-line stack under "5 hari". Font-padding fix landed. Chevron ">" on the streak card present. KikoImage swap landed (brows, open smile with tongue, wavy belly lines match design).
- **[GAP · Medium · Header, directly under "Halo, Raka!" ~y103 dp design coords]** Subtitle "Siap main sehat hari ini?" is bottom-clipped in the app: every glyph is sliced flat at the baseline and the 'p' descender is missing (verified at 3× zoom). Pre-existing — also present in `app/iter1/beranda.png`, so this was a miss in iter1 QA, not a regression. *Fix: apply the same font-padding / line-height clip fix used on the streak subtitle to this header-subtitle text style so descenders render.*
- Everything else matches the design and `copy/beranda.md`: header "Halo, Raka!" + sparkle + bell button, Kiko mascot card with 3-line speech bubble ("Kamu hebat! Kemarin / jarak matamu sudah / pas. Yuk ulangi lagi!") and sparkles, stat cards ("Jarak rata-rata" 52 cm / "Waktu main" 35 mnt, pen chips, progress bars, green ✓ Aman pills), streak "5 hari" + flame icon + chevron, dark CTA "Mulai Pantau" with play icon, sparkle+cloud decorations, bottom nav with **Beranda** active. Colors/radii consistent with tokens.

---

## pantau_live — `screens/1705_pantau_live.png` vs `app/iter2/pantau_live.png`

**Verdict: PASS-MINOR** · **Iter1 defects: none**

- **[Known · Minor · Camera card, bottom-right]** Mascot is the deliberately kept Canvas ruler mascot (not the design's vector Kiko-with-ruler): same character, pose, and yellow-ruler prop, but visibly different line art — navy ruler tick marks (design: red-orange) and slanted "determined" eyebrows (design: thin curved brows). Accepted deviation per plan; no action required unless the KikoImage swap is later extended to this pose.
- All other elements match the design and `copy/pantau_live.md`: header "Pantau Jarak" + sparkle + mint "● Sedang memantau" pill; camera card ("Kamera depan" chip, sparkle, dashed face-oval with smiley, dotted connector, white "52 cm" pill, phone circle); gauge card (red/yellow/green segmented arc, knob at green-zone entry, "52 cm", "jarak mata ke layar", green pill "✓ Jarakmu sudah pas! 👍", timeline smiley — dotted — yellow pen circle — dotted — phone); timer card (clock chip, "Waktu bermain", "12:45", dark pause FAB); bottom nav with **Pantau** active. Gauge knob/bar values dummy-equivalent ("52 cm" format unchanged).

---

## istirahat — `screens/2023_istirahat.png` vs `app/iter2/istirahat.png`

**Verdict: PASS** · **Iter1 defect (missing chevron): FIXED**

- **Iter1 fix verified:** small ink ">" chevron now present, centered between the sleeping Kiko and the "Segar lagi!" mini-card, matching design placement. Chevron-size bug fix landed (stroke slightly bolder than design's thin glyph — within rendering tolerance).
- Everything else matches the design and `copy/istirahat.md`: header "Waktunya Istirahat" + lavender "20-20-20" pill; blue timer card with clouds + sparkle; circular countdown ring (green arc with white gap upper-left, same as design); "14" / "detik"; sleeping Kiko with "z z" marks and droopy eyelids (KikoImage swap landed); white "Segar lagi!" mini-card with awake Kiko head; instruction card (green eye icon + "Lihat benda sejauh 6 meter selama / 20 detik" with the design's line break); dark bottom-anchored CTA "Selesai Istirahat". No bottom nav (correct — break overlay). Colors/radii match tokens.

---

## statistik_harian — `screens/2130_statistik_harian.png` vs `app/iter2/statistik_harian.png`

**Verdict: PASS** · **Iter1 defects: none**

- All elements match the design and `copy/statistik_harian.md`: header "Statistik Hari Ini" + "Minggu, 1 November" + two yellow sparkles; chart card ("Jarak per jam", legend ● Aman / ● Waspada / ● Bahaya in mint/yellow/coral, 7 rounded bars with the design's exact pattern — 08 green tall, 10 yellow mid, 12 green tallest, 14 coral short, 16 green tall, 18 yellow mid, 20 green tall, x-axis 08–20); stat cards ("Jarak rata-rata" 52 cm / "Durasi aman" 85 menit, pen chips, progress bars, green ✓ Aman pills); health card (happy full-body Kiko left — KikoImage swap landed, "Mata kamu sehat!", body "80% waktumu hari ini ada di jarak / aman. Hebat, pertahankan!" with the design's line break); bottom nav with **Pantau** active. Bar heights dummy-equivalent; no copy/color/radius/hierarchy deviations.

---

## Summary

| slug | verdict | iter1 defect status | remaining gaps | key issue |
|---|---|---|---|---|
| beranda | **GAP** | streak subtitle clip: **FIXED** | 1 (Medium) | Header subtitle "Siap main sehat hari ini?" bottom-clipped (pre-existing, missed in iter1) |
| pantau_live | **PASS-MINOR** | — | 0 new (1 known) | Canvas ruler mascot kept by design (tick color + brow expression differ) |
| istirahat | **PASS** | missing chevron: **FIXED** | 0 | — |
| statistik_harian | **PASS** | — | 0 | — |

Batch totals: 2 PASS, 1 PASS-MINOR, 1 GAP (1 concrete defect remaining, 1 known deviation). **Both iter1 defects fixed (2/2).** The single remaining defect is the header-subtitle clip on beranda — same text-clipping class as the iter1 streak bug, so the font-padding fix was applied to the streak subtitle style but not to the header-subtitle style.
