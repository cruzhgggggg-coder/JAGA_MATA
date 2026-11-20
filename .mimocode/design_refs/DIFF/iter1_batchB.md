# Visual QA — iter1 Batch B (beranda, pantau_live, istirahat, statistik_harian)

Design ref 393×852 vs app screenshot 1080×2400. System status bar / gesture bar / clock tolerated throughout (not listed as gaps).

---

## beranda — `screens/1577_beranda.png` vs `app/iter1/beranda.png`

**Verdict: GAP**

- **[GAP · High · Streak card, mid-screen ~y480–560 dp]** Streak subtitle "streak berturut-turut, pertahankan ya!" is clipped by the bottom edge of the streak card — only the top slivers of the glyphs are visible and the copy is unreadable. Design shows the full 13sp line under "5 hari". *Fix: increase streak card height (or reduce vertical padding around "5 hari") so the subtitle line fits fully inside the card, matching the design's two-line content stack.*
- All other elements match the design and `copy/beranda.md`: header "Halo, Raka!" + sparkle + bell button, subtitle "Siap main sehat hari ini?", Kiko mascot card with 3-line speech bubble ("Kamu hebat! Kemarin / jarak matamu sudah / pas. Yuk ulangi lagi!"), two stat cards ("Jarak rata-rata" 52 cm / "Waktu main" 35 mnt with progress bars and green ✓ Aman pills), streak "5 hari" + flame icon + chevron, dark CTA "Mulai Pantau" with play icon, sparkle+cloud decorations, bottom nav with **Beranda** active. Colors (ink #12263A, mint pills, #4A86E8 nav-active, surface #FFFFFF on #F2F7FF background) and corner radii are consistent with tokens.

---

## pantau_live — `screens/1705_pantau_live.png` vs `app/iter1/pantau_live.png`

**Verdict: PASS**

- Header "Pantau Jarak" + sparkle + green pill "● Sedang memantau" matches `copy/pantau_live.md`.
- Camera card matches: "Kamera depan" chip (camera icon), sparkle, dashed face-oval with smiley, dotted connector, white "52 cm" pill, phone icon circle, Kiko-with-ruler mascot bottom-right (same character/pose/colors as design).
- Gauge card matches: red/yellow/green segmented arc, knob in the green zone, "52 cm" display number, "jarak mata ke layar", green pill "✓ Jarakmu sudah pas! 👍", bottom timeline (smiley — dotted — yellow pen circle — dotted — phone).
- Timer card matches: clock chip, "Waktu bermain", "12:45", dark navy pause FAB.
- Bottom nav matches with **Pantau** active (blue pill + blue label).
- No copy, color, radius, or hierarchy deviations found. Gauge knob angle and bar values are dummy-data equivalent (same "52 cm" format and green-zone placement).

---

## istirahat — `screens/2023_istirahat.png` vs `app/iter1/istirahat.png`

**Verdict: GAP**

- **[GAP · Medium · Mid-card, between sleeping mascot and "Segar lagi!" mini-card ~y470 dp design coords]** The small ">" chevron arrow shown in the design (transition cue from the sleeping Kiko to the fresh Kiko card) is **missing entirely** in the app. *Fix: add the small ink-colored chevron icon centered between the mascot and the "Segar lagi!" card, matching design placement.*
- Everything else matches the design and `copy/istirahat.md`: header "Waktunya Istirahat" + lavender "20-20-20" pill, blue timer card with clouds/sparkle, circular countdown ring (green arc ≈ 3/4 with white gap upper-left, same as design), "14" / "detik", sleeping Kiko with "z z" marks, white "Segar lagi!" mini-card with awake Kiko head, instruction card (green eye icon + "Lihat benda sejauh 6 meter selama / 20 detik" with the same line break), dark bottom-anchored CTA "Selesai Istirahat". No bottom nav on this screen in either source (correct — break overlay). Colors/radii match tokens.

---

## statistik_harian — `screens/2130_statistik_harian.png` vs `app/iter1/statistik_harian.png`

**Verdict: PASS**

- Header "Statistik Hari Ini" + "Minggu, 1 November" + two yellow sparkles matches `copy/statistik_harian.md`.
- Chart card matches: "Jarak per jam", legend (● Aman / ● Waspada / ● Bahaya in mint / yellow / coral), 7 rounded bars with the design's exact pattern (08 green tall, 10 yellow mid, 12 green tallest, 14 coral short, 16 green tall, 18 yellow mid, 20 green tall), x-axis labels 08–20.
- Stat cards match: "Jarak rata-rata" 52 cm and "Durasi aman" 85 menit with pen chips, progress bars, green ✓ Aman pills.
- Health card matches: happy full-body Kiko left, "Mata kamu sehat!", body "80% waktumu hari ini ada di jarak / aman. Hebat, pertahankan!" with the same line break as copy.
- Bottom nav matches with **Pantau** active (blue pill on eye icon) — same active tab as the design.
- Bar heights are dummy-data equivalents of the design pattern; no copy/color/radius/hierarchy deviations.

---

## Summary

| slug | verdict | # gaps | key issue |
|---|---|---|---|
| beranda | **GAP** | 1 | Streak-card subtitle "streak berturut-turut, pertahankan ya!" clipped at card bottom (unreadable) |
| pantau_live | **PASS** | 0 | — |
| istirahat | **GAP** | 1 | Missing ">" chevron between sleeping mascot and "Segar lagi!" card |
| statistik_harian | **PASS** | 0 | — |

Batch totals: 2 PASS, 0 PASS-MINOR, 2 GAP (2 concrete defects, both fixable without copy changes).
