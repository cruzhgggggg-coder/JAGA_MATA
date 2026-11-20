# Component specs — JagaMata (from components.svg)

Measured from `.mimocode/design_refs/components/components.svg`. Frame: **1600×3314**. Mobile viewport reference: **393×852**.

## Horizontal page padding

- Section cards & content start at **x=64**; section cards end at x=1536 → right padding **64 px** on the 1600-wide sheet.
- On the 393-wide phone screens, content padding is measured separately: **20 px** left/right (from screen copy x-coordinates: e.g. beranda text x=20; status cards x=20).

## Corner radii (containers on this sheet)

- `r=5`
- `r=11`
- `r=13`
- `r=16`
- `r=17`
- `r=18`
- `r=20`
- `r=22`
- `r=24`
- `r=26`
- `r=27`
- `r=28`
- `r=30`
- `r=54`
- `r=98.998`

## Buttons (label Nunito 16/700 inside filled rounded box, h 40–72)

| Label | Box x,y | Size w×h | Radius | Fill | Text fill |
|-------|---------|----------|--------|------|-----------|
| Mulai Pantau | 113,651 | 154×56 | 28 | `#12263A` | `#FFFFFF` |
| Mulai Pantau | 283,651 | 154×56 | 28 | `#2D70B7` | `#FFFFFF` |
| Mulai Pantau | 453,651 | 154×56 | 28 | `#12263A` | `#FFFFFF` |
| Mulai Pantau | 623,649 | 158×60 | 30 | `#FFFFFF` | `#2D70B7` |
| Mulai Pantau | 797,649 | 158×60 | 30 | `#E1EFFF` | `#2D70B7` |
| Mulai Pantau | 971,649 | 158×60 | 30 | `#FFFFFF` | `#2D70B7` |

**Button heights (distinct): 56, 60 px** — primary CTA pill is 56 h (r=28); secondary/outline variant 60 h (r=30).

On-screen primary CTA (e.g. onboarding): **353×56 @ (20,756), r=28, fill `#12263A`** within 852 → bottom margin 40 px.

## Pill / status chips (h 20–40, w 40–260)

| Text | Box x,y | Size w×h | Radius | Fill |
|------|---------|----------|--------|------|
| — | 115,773 | 52×32 | 16 | `#4A86E8` |
| — | 187,773 | 52×32 | 16 | `#E3E8EE` |
| — | 323,787 | 40×40 | 20 | `#5FD4A7` |
| — | 379,787 | 40×40 | 20 | `#FFD166` |
| — | 435,787 | 40×40 | 20 | `#FFFFFF` |
| Aman | 129,1151 | 67×22 | 11 | `#5FD4A7` |
| Waspada | 319,1151 | 86×22 | 11 | `#FFD166` |
| Bahaya | 509,1151 | 75×22 | 11 | `#FF7B6B` |
| Jarakmu sudah pas! 👍 | 194,1417 | 191×34 | 17 | `#5FD4A7` |
| — | 133,1465 | 40×40 | 20 | `#E1EFFF` |
| — | 406,1465 | 40×40 | 20 | `#E1EFFF` |
| Agak dekat, mundur sedikit ya | 545.5,1417 | 234×34 | 17 | `#FFD166` |
| — | 506,1465 | 40×40 | 20 | `#E1EFFF` |
| — | 779,1465 | 40×40 | 20 | `#E1EFFF` |
| Terlalu dekat! Mata perlu istirahat | 908,1417 | 255×34 | 17 | `#FF7B6B` |
| — | 879,1465 | 40×40 | 20 | `#E1EFFF` |
| — | 1152,1465 | 40×40 | 20 | `#E1EFFF` |
| — | 116,1800 | 40×40 | 20 | `#5FD4A7` |
| — | 161.5,1800 | 40×40 | 20 | `#5FD4A7` |
| — | 207,1800 | 40×40 | 20 | `#5FD4A7` |
| — | 252.5,1800 | 40×40 | 20 | `#5FD4A7` |
| — | 298,1800 | 40×40 | 20 | `#5FD4A7` |
| — | 343.5,1800 | 40×40 | 20 | `#FFD166` |
| — | 389,1800 | 40×40 | 20 | `#FFFFFF` |
| — | 945.13,1733 | 56×32 | 16 | `#E1EFFF` |
| — | 1037.38,1849 | 56×32 | 16 | `#E1EFFF` |
| — | 1129.63,1965 | 56×32 | 16 | `#E1EFFF` |
| — | 1221.88,2081 | 56×32 | 16 | `#E1EFFF` |
| Baik | 831,2335 | 50×26 | 13 | `#5FD4A7` |
| Perlu Perhatian | 765,2607 | 116×26 | 13 | `#FFD166` |
| Kurang | 814,2879 | 67×26 | 13 | `#FF7B6B` |

Zone chips: Aman `#5FD4A7` 67×22 r=11 · Waspada `#FFD166` 86×22 r=11 · Bahaya `#FF7B6B` 75×22 r=11.
Toast/status pill: h=34, r=17 (e.g. `#5FD4A7` 191×34).

## Cards

| Text (first) | Box x,y | Size w×h | Radius | Fill |
|--------------|---------|----------|--------|------|
| Hai! Aku Kiko. Yuk jaga  | 64,2266 | 1472×984 | 24 | `#FFFFFF` |
| Status Card · Distance Gauge — zona: Aman  | 64,924 | 1472×650 | 24 | `#F2F7FF` |
| Streak Calendar · Timer Picker · Bottom Na | 64,1622 | 1472×596 | 24 | `#F2F7FF` |
| CTA Button · Category Chip · Toggle · Day  | 64,550 | 1472×326 | 24 | `#FFFFFF` |
| Laporan Mingguan | 531,2298 | 387×834 | 5 | `#F2F7FF` |
| Dekorasi — maksimal 2–4 per layar | 64,350 | 1472×152 | 24 | `#D6E6FF` |
| Hai! Aku Kiko. Yuk jaga  | 96,2298 | 387×564.8 | 5 | `#F2F7FF` |
| Beranda | 898,1704 | 427×482 | 5 | `#F2F7FF` |
| Ikon — garis membulat, stroke 2, round cap | 64,168 | 1472×134 | 24 | `#FFFFFF` |
| Hai! Aku Kiko. Yuk jaga jarak matamu  | 113,2485.4 | 353×360.4 | 24 | `url(#paint243)` |
| 52 cm | 113,1247 | 353×278 | 24 | `#FFFFFF` |
| 40 cm | 486,1247 | 353×278 | 24 | `#FFFFFF` |
| 24 cm | 859,1247 | 353×278 | 24 | `#FFFFFF` |
| Laporan Mingguan | 548,2315 | 353×256 | 24 | `#FFFFFF` |
| Laporan Mingguan | 548,2587 | 353×256 | 24 | `#FFFFFF` |
| Laporan Mingguan | 548,2859 | 353×256 | 24 | `#FFFFFF` |
| Batas waktu bermain | 497,1704 | 353×252 | 24 | `#FFFFFF` |
| Mata Jagoan | 966,2298 | 390×196 | 5 | `#F2F7FF` |
| Streak minggu ini | 96,1704 | 353×156 | 24 | `#FFFFFF` |
| Hai! Aku Kiko. Yuk jaga  | 113,2315 | 353×154.4 | 24 | `url(#paint236)` |
| Motion — Confetti | 966,2510 | 240×206 | 20 | `#FFF6E9` |
| Motion — Progress | 728,1006 | 260×184 | 20 | `#FFF6E9` |
| — | 190.5,2621.4 | 198×215.6 | 98.998 | `#4A86E8` |
| Beranda | 915,1721 | 393×100 | 24 | `#FFFFFF` |
| Beranda | 915,1837 | 393×100 | 24 | `#FFFFFF` |

Card radius standard: **24**. Status cards 353×278 r=24 fill `#FFFFFF` on `#F2F7FF` board.

## Bottom CTA / bottom bar geometry (relative to 852)

| Screen | Box x,y | Size w×h | Radius | Fill | Notes |
|--------|---------|----------|--------|------|-------|
| 1577_beranda.svg | 0,752 | 393×100 | 24 | `#FFFFFF` | bottom nav bar; 852−752−100=0 → flush bottom |
| 1705_pantau_live.svg | 0,752 | 393×100 | 24 | `#FFFFFF` | bottom nav bar; 852−752−100=0 → flush bottom |
| 1353_onboarding1.svg | 20,756 | 353×56 | 28 | `#12263A` | onboarding primary CTA; bottom margin 852−756−56=40 |
| 1860_peringatan.svg | 0,752 | 393×100 | 24 | `#FFFFFF` | bottom nav bar; 852−752−100=0 → flush bottom |
| 1860_peringatan.svg | 24,756 | 345×56 | 28 | `#12263A` | onboarding primary CTA; bottom margin 852−756−56=40 |
| 2023_istirahat.svg | 20,756 | 353×56 | 28 | `#12263A` | onboarding primary CTA; bottom margin 852−756−56=40 |

- **Bottom nav bar:** y=**752**, h=**100**, full width 393, top radius r=24, fill `#FFFFFF` → occupies last 100 px of 852.
- **Bottom CTA (onboarding etc.):** y=**756**, h=**56**, w=353 (x=20), r=**28**, fill `#12263A` → bottom gap 40 px.
- Components sheet CTA row: buttons at y=649–651 (h 56/60); section card `CTA Button · …` at (64,550) 1472×326 r=24.

## Spacing (gaps from section-heading y-coordinates)

- y=217.99: Ikon — garis membulat, stroke 2, round cap & join, bingkai 24×24
- y=399.99 — Δy=182: Dekorasi — maksimal 2–4 per layar
- y=599.99 — Δy=200: CTA Button · Category Chip · Toggle · Day Circle
- y=973.99 — Δy=374: Status Card · Distance Gauge — zona: Aman ≥50 cm · Waspada 30–50 cm · Ba
- y=1043.99 — Δy=70: Motion — Progress
- y=1671.99 — Δy=628: Streak Calendar · Timer Picker · Bottom Nav
- y=1737.99 — Δy=66: Batas waktu bermain
- y=1748.99 — Δy=11: Streak minggu ini
- y=2352.99 — Δy=604: Laporan Mingguan
- y=2434.99 — Δy=82: Mata Jagoan
- y=2434.99 — Δy=0: Mata Jagoan
- y=2547.99 — Δy=113: Motion — Confetti
- y=2624.99 — Δy=77: Laporan Mingguan
- y=2896.99 — Δy=272: Laporan Mingguan

## Shadow hints (filter defs)

- dy=4, blur=6 (feOffset dy + feGaussianBlur stdDeviation)
- dy=6, blur=8 (feOffset dy + feGaussianBlur stdDeviation)

Document effect styles: **Shadow Card**, **Shadow Hero** (see foundations/tokens.md).

## Component hex fills (summary)

- `#FFFFFF` ×26
- `#E1EFFF` ×16
- `#FFD166` ×14
- `#5FD4A7` ×9
- `#F2F7FF` ×6
- `#4A86E8` ×5
- `#12263A` ×3
- `#2D70B7` ×3
- `#FF7B6B` ×3
- `#FFF6E9` ×2
- `#D6E6FF` ×1
- `#E3E8EE` ×1
- `URL(#PAINT236)` ×1
- `URL(#PAINT243)` ×1

## All text labels (annotations) with sizes

- [64,105.3] Baloo 2 48/800 fill=#12263A "JagaMata — Components"
- [96,217.99] Baloo 2 18/700 fill=#12263A "Ikon — garis membulat, stroke 2, round cap & join, bingkai 24×24"
- [96,399.99] Baloo 2 18/700 fill=#12263A "Dekorasi — maksimal 2–4 per layar"
- [96,599.99] Baloo 2 18/700 fill=#12263A "CTA Button · Category Chip · Toggle · Day Circle"
- [1251,675.28] Nunito 13/600 fill=#12263A "Belajar"
- [1365,675.28] Nunito 13/600 fill=#FFFFFF "Belajar"
- [141,684.26] Nunito 16/700 fill=#FFFFFF "Mulai Pantau"
- [311,684.26] Nunito 16/700 fill=#FFFFFF "Mulai Pantau"
- [481,684.26] Nunito 16/700 fill=#FFFFFF "Mulai Pantau"
- [653,684.26] Nunito 16/700 fill=#2D70B7 "Mulai Pantau"
- [827,684.26] Nunito 16/700 fill=#2D70B7 "Mulai Pantau"
- [1001,684.26] Nunito 16/700 fill=#2D70B7 "Mulai Pantau"
- [339.5,777.62] Nunito 11/700 fill=#12263A "S"
- [395.5,777.62] Nunito 11/700 fill=#12263A "S"
- [451.5,777.62] Nunito 11/700 fill=#12263A "S"
- [96,973.99] Baloo 2 18/700 fill=#12263A "Status Card · Distance Gauge — zona: Aman ≥50 cm · Waspada 30–50 cm · Bahaya <30 cm"
- [748,1043.99] Baloo 2 18/700 fill=#12263A "Motion — Progress"
- [169,1059.28] Nunito 13/600 fill=#12263A "Jarak rata-rata"
- [359,1059.28] Nunito 13/600 fill=#12263A "Jarak rata-rata"
- [549,1059.28] Nunito 13/600 fill=#12263A "Jarak rata-rata"
- [748,1075.94] Nunito 15/500 fill=#12263A "Isi progress bar Status Card & "
- [748,1097.94] Nunito 15/500 fill=#12263A "penanda busur Distance Gauge: "
- [129,1109.86] Baloo 2 32/700 fill=#12263A "52 cm"
- [319,1109.86] Baloo 2 32/700 fill=#12263A "42 cm"
- [509,1109.86] Baloo 2 32/700 fill=#12263A "24 cm"
- [748,1119.94] Nunito 15/500 fill=#12263A "600 ms, ease-out cubic-"
- [748,1141.94] Nunito 15/500 fill=#12263A "bezier(0.22, 1, 0.36, 1), dari 0 ke "
- [748,1163.94] Nunito 15/500 fill=#12263A "nilai saat kartu muncul."
- [155,1165.62] Nunito 11/700 fill=#12263A "Aman"
- [345,1165.62] Nunito 11/700 fill=#12263A "Waspada"
- [535,1165.62] Nunito 11/700 fill=#12263A "Bahaya"
- [248.33,1365.86] Baloo 2 32/700 fill=#12263A "52 cm"
- [618.91,1365.86] Baloo 2 32/700 fill=#12263A "40 cm"
- [993.48,1365.86] Baloo 2 32/700 fill=#12263A "24 cm"
- [233.6,1392.28] Nunito 13/600 fill=#12263A "jarak mata ke layar"
- [606.6,1392.28] Nunito 13/600 fill=#12263A "jarak mata ke layar"
- [979.6,1392.28] Nunito 13/600 fill=#12263A "jarak mata ke layar"
- [232,1438.28] Nunito 13/600 fill=#12263A "Jarakmu sudah pas! 👍"
- [583.5,1438.28] Nunito 13/600 fill=#12263A "Agak dekat, mundur sedikit ya"
- [946,1438.28] Nunito 13/600 fill=#12263A "Terlalu dekat! Mata perlu istirahat"
- [96,1671.99] Baloo 2 18/700 fill=#12263A "Streak Calendar · Timer Picker · Bottom Nav"
- [513,1737.99] Baloo 2 18/700 fill=#12263A "Batas waktu bermain"
- [116,1748.99] Baloo 2 18/700 fill=#12263A "Streak minggu ini"
- [348,1752.86] Baloo 2 32/700 fill=#12263A "5 hari"
- [950.63,1779.62] Nunito 11/700 fill=#2D70B7 "Beranda"
- [1046.38,1779.62] Nunito 11/700 fill=#12263A "Pantau"
- [1138.63,1779.62] Nunito 11/700 fill=#12263A "Hadiah"
- [1221.88,1779.62] Nunito 11/700 fill=#12263A "Orang Tua"
- [132.5,1790.62] Nunito 11/700 fill=#12263A "S"
- [178,1790.62] Nunito 11/700 fill=#12263A "S"
- [223,1790.62] Nunito 11/700 fill=#12263A "R"
- [268.5,1790.62] Nunito 11/700 fill=#12263A "K"
- [316,1790.62] Nunito 11/700 fill=#12263A "J"
- [360,1790.62] Nunito 11/700 fill=#12263A "S"
- [404,1790.62] Nunito 11/700 fill=#12263A "M"
- [646,1799.3] Baloo 2 48/800 fill=#12263A "20"
- [656.5,1827.28] Nunito 13/600 fill=#12263A "menit"
- [557.5,1870.28] Nunito 13/600 fill=#12263A "5"
- [607.5,1870.28] Nunito 13/600 fill=#12263A "10"
- [663.5,1870.28] Nunito 13/600 fill=#12263A "15"
- [719.5,1870.28] Nunito 13/600 fill=#FFFFFF "20"
- [775.5,1870.28] Nunito 13/600 fill=#12263A "25"
- [950.63,1895.62] Nunito 11/700 fill=#12263A "Beranda"
- [1046.38,1895.62] Nunito 11/700 fill=#2D70B7 "Pantau"
- [1138.63,1895.62] Nunito 11/700 fill=#12263A "Hadiah"
- [1221.88,1895.62] Nunito 11/700 fill=#12263A "Orang Tua"
- [647,1922.28] Nunito 13/600 fill=#12263A "30 menit"
- [950.63,2011.62] Nunito 11/700 fill=#12263A "Beranda"
- [1046.38,2011.62] Nunito 11/700 fill=#12263A "Pantau"
- [1138.63,2011.62] Nunito 11/700 fill=#2D70B7 "Hadiah"
- [1221.88,2011.62] Nunito 11/700 fill=#12263A "Orang Tua"
- [950.63,2127.62] Nunito 11/700 fill=#12263A "Beranda"
- [1046.38,2127.62] Nunito 11/700 fill=#12263A "Pantau"
- [1138.63,2127.62] Nunito 11/700 fill=#12263A "Hadiah"
- [1221.88,2127.62] Nunito 11/700 fill=#2D70B7 "Orang Tua"
- [843,2352.28] Nunito 13/600 fill=#12263A "Baik"
- [568,2352.99] Baloo 2 18/700 fill=#12263A "Laporan Mingguan"
- [273,2374.13] Nunito 15/500 fill=#12263A "Hai! Aku Kiko. Yuk jaga "
- [568,2376.28] Nunito 13/600 fill=#12263A "Jarak rata-rata 7 hari"
- [273,2396.13] Nunito 15/500 fill=#12263A "jarak matamu hari ini!"
- [663,2417.28] Nunito 13/600 fill=#12263A "naik 6 cm dari minggu lalu"
- [568,2421.86] Baloo 2 32/700 fill=#12263A "54 cm"
- [1017.27,2434.99] Baloo 2 18/700 fill=#12263A "Mata Jagoan"
- [1203.27,2434.99] Baloo 2 18/700 fill=#12263A "Mata Jagoan"
- [1017,2460.28] Nunito 13/600 fill=#12263A "7 hari jarak aman"
- [1229,2460.28] Nunito 13/600 fill=#12263A "Terkunci"
- [838,2509.62] Nunito 11/700 fill=#12263A "50 cm"
- [164.41,2539.34] Nunito 15/500 fill=#12263A "Hai! Aku Kiko. Yuk jaga jarak matamu "
- [572,2547.62] Nunito 11/700 fill=#12263A "S"
- [621.33,2547.62] Nunito 11/700 fill=#12263A "S"
- [670.67,2547.62] Nunito 11/700 fill=#12263A "R"
- [721,2547.62] Nunito 11/700 fill=#12263A "K"
- [771.33,2547.62] Nunito 11/700 fill=#12263A "J"
- [817.67,2547.62] Nunito 11/700 fill=#12263A "S"
- [867,2547.62] Nunito 11/700 fill=#12263A "M"
- [986,2547.99] Baloo 2 18/700 fill=#12263A "Motion — Confetti"
- [265.13,2561.34] Nunito 15/500 fill=#12263A "hari ini!"
- [986,2579.94] Nunito 15/500 fill=#12263A "Saat Reward Card terbuka: 5 "
- [986,2601.94] Nunito 15/500 fill=#12263A "confetti menyebar 12–24 px "
- [986,2623.94] Nunito 15/500 fill=#12263A "& memudar, 900 ms, ease-"
- [777,2624.28] Nunito 13/600 fill=#12263A "Perlu Perhatian"
- [568,2624.99] Baloo 2 18/700 fill=#12263A "Laporan Mingguan"
- [986,2645.94] Nunito 15/500 fill=#12263A "out cubic-bezier(0.22, 1, "
- [568,2648.28] Nunito 13/600 fill=#12263A "Jarak rata-rata 7 hari"
- [986,2667.94] Nunito 15/500 fill=#12263A "0.36, 1). Badge bounce scale "
- [663,2689.28] Nunito 13/600 fill=#12263A "naik 6 cm dari minggu lalu"
- [986,2689.94] Nunito 15/500 fill=#12263A "0.8 → 1, 320 ms."
- [568,2693.86] Baloo 2 32/700 fill=#12263A "54 cm"
- [838,2781.62] Nunito 11/700 fill=#12263A "50 cm"
- [572,2819.62] Nunito 11/700 fill=#12263A "S"
- [621.33,2819.62] Nunito 11/700 fill=#12263A "S"
- [670.67,2819.62] Nunito 11/700 fill=#12263A "R"
- [721,2819.62] Nunito 11/700 fill=#12263A "K"
- [771.33,2819.62] Nunito 11/700 fill=#12263A "J"
- [817.67,2819.62] Nunito 11/700 fill=#12263A "S"
- [867,2819.62] Nunito 11/700 fill=#12263A "M"
- [826,2896.28] Nunito 13/600 fill=#12263A "Kurang"
- [568,2896.99] Baloo 2 18/700 fill=#12263A "Laporan Mingguan"
- [568,2920.28] Nunito 13/600 fill=#12263A "Jarak rata-rata 7 hari"
- [663,2961.28] Nunito 13/600 fill=#12263A "naik 6 cm dari minggu lalu"
- [568,2965.86] Baloo 2 32/700 fill=#12263A "54 cm"
- [838,3053.62] Nunito 11/700 fill=#12263A "50 cm"
- [572,3091.62] Nunito 11/700 fill=#12263A "S"
- [621.33,3091.62] Nunito 11/700 fill=#12263A "S"
- [670.67,3091.62] Nunito 11/700 fill=#12263A "R"
- [721,3091.62] Nunito 11/700 fill=#12263A "K"
- [771.33,3091.62] Nunito 11/700 fill=#12263A "J"
- [817.67,3091.62] Nunito 11/700 fill=#12263A "S"
- [867,3091.62] Nunito 11/700 fill=#12263A "M"
- [136,3204.26] Nunito 16/700 fill=#12263A "9:41"
