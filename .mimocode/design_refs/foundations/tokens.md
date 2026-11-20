# Design tokens — JagaMata (from foundations.svg)

Parsed from `.mimocode/design_refs/foundations/foundations.svg` (real `<text>` elements preserved).

## Color tokens

| Label | Hex | Swatch (x,y,w×h) | Radius | Notes |
|-------|-----|------------------|--------|-------|
| Accent/Blush | `#FFB4C2` | 1136,512 172×80 | 14 |  |
| Accent/Coral Alert | `#FF7B6B` | 712,512 172×80 | 14 |  |
| Accent/Cream | `#FFF6E9` | 76,680 172×80 | 14 |  |
| Accent/Lavender | `#B8A7F5` | 924,512 172×80 | 14 |  |
| Accent/Mint | `#5FD4A7` | 288,512 172×80 | 14 |  |
| Accent/Sunny Yellow | `#FFD166` | 500,512 172×80 | 14 |  |
| Brand/Primary Blue | `#4A86E8` | 76,344 172×80 | 14 |  |
| Brand/Primary Deep | `#2D70B7` | 288,344 172×80 | 14 |  |
| Mascot/Inner Wing | `#2D70B7` | 288,848 172×80 | 14 |  |
| Mascot/Iris | `#1B3B6F` | 76,848 172×80 | 14 |  |
| Mode Dark/Background | `#0F1B2D` | 64,2490 1312×140 | 24 | dari teks "Background #0F1B2D" |
| Mode Dark/Surface | `#1A2A42` | 356,2522 358×76 | 20 | dari teks "Surface #1A2A42" |
| Support/Decoration | `#E3E8EE` | 1136,680 172×80 | 14 |  |
| Support/Nav Active | `#E1EFFF` | 924,680 172×80 | 14 |  |
| Surface/Background | `#F2F7FF` | 1136,344 172×80 | 14 |  |
| Surface/Surface | `#FFFFFF` | 76,512 172×80 | 14 |  |
| Text/Ink | `#12263A` | 500,344 172×80 | 14 |  |
| Text/Ink Soft | `#6B7B90` | 712,344 172×80 | 14 | · ikon & teks ≥18 |

## Text styles (named — from Tipografi section annotations)

| Style | Spec (from board) | Sample | Measured on sample |
|-------|-------------------|--------|--------------------|
| Display | Baloo 2 ExtraBold · 48 / 56 | 52 cm | Baloo 2 48/800 fill=#2D70B7 |
| H1 | Baloo 2 Bold · 24 / 32 | Halo, Raka! | Baloo 2 24/700 fill=#12263A |
| H2 | Baloo 2 Bold · 18 / 26 | Statistik Hari Ini | Baloo 2 18/700 fill=#12263A |
| Body | Nunito Medium · 16 / 24 | Ayo jaga jarak supaya matamu tetap segar. | Nunito 16/500 fill=#12263A |
| Body Small | Nunito Medium · 15 / 22 | Kiko menemanimu saat bermain gadget. | Nunito 15/500 fill=#12263A |
| Caption | Nunito SemiBold · 13 / 18 | Jarak rata-rata hari ini | Nunito 13/600 fill=#12263A |
| Micro | Nunito Bold · 11 / 14 · letter-spacing +2% | CHIP & BADGE | Nunito 11/700 fill=#12263A |
| Number | Baloo 2 Bold · 32 / 40 | 12:45 | Baloo 2 32/700 fill=#12263A |
| Button | Nunito Bold 16 / 24 (label on CTA) | Mulai Pantau | Nunito 16/700 fill=#FFFFFF or #2D70B7 |

### Raw distinct font combinations on this board

| Font | Size | Weight | Example |
|------|------|--------|---------|
| Baloo 2 | 48 | 800 | JagaMata — Foundations |
| Baloo 2 | 32 | 700 | Number — 12:45 |
| Baloo 2 | 24 | 700 | Warna (variabel koleksi “Warna”, mode Light / Dark) |
| Baloo 2 | 18 | 700 | H2 — Statistik Hari Ini |
| Nunito | 16 | 500 | Soft morning sky — biru langit yang tenang, krem-kuning ceria, mint se |
| Nunito | 15 | 500 | Body Small — Kiko menemanimu saat bermain gadget. |
| Nunito | 13 | 600 | Brand/Primary Blue |
| Nunito | 11 | 700 | #4A86E8 |

Document text styles (FigCraft): Display, H1, H2, Body, Body Small, Caption, Micro, Number, Button. Effect styles: Shadow Card, Shadow Hero. Paint style: Hero Stage.

## Corner radii

FigCraft exports rounded rects as paths (no `rx`/`ry` attributes). Radii inferred from path geometry:

- `r=14`
- `r=16`
- `r=20`
- `r=22`
- `r=24`
- `r=32`

Board note: **tidak ada radius < 12** (variable collection "Radius").
