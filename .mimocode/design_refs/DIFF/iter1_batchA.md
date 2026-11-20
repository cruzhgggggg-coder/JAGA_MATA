# Visual QA — iter1 Batch A (onboarding1–3, peringatan, overlay_ditolak)

Compared: `screens/<id>_<slug>.png` (design, 393×852) vs `app/iter1/<slug>.png` (1080×2400).
Copy verified against `copy/<slug>.md`. System status bar / home indicator / dynamic clock / font anti-aliasing / ≤4dp shifts are ignored.

---

## onboarding1 — **GAP**

- **[major] Kiko mascot overflows the illustration card** (tengah, kartu ilustrasi). In the design the mascot (wings, feet, body) sits fully inside the light-blue card; in the app two dark-blue wing ovals spill outside the card's left/right edges and the yellow feet overflow the card's bottom edge. The head is also scaled noticeably larger relative to the card. → Fix: render the mascot from a single design-matched asset scaled to fit inside the card and clip it (`ClipRRect`/`clipToOutline`) to the card's rounded bounds.
- **[minor] Mascot ear shape differs** (tengah, kartu ilustrasi). Design has small rounded ear nubs; app shows tall pointed cat-like ears. → Fix: swap in the design's Kiko asset (or match ear geometry) for all onboarding illustrations.
- **[minor] Title weight lighter than design** (judul atas "Kenalan dengan Kiko!"). Design headline is heavy rounded-bold; app headline renders noticeably thinner. → Fix: set title `fontFamily`/`fontWeight` to the design's bold display style (see cross-screen note).
- Copy OK: bubble "Halo! Aku Kiko, burung hantu penjaga matamu.", title, body 3 lines, "Lanjut" all match `copy/onboarding1.md` exactly, incl. line breaks. Pagination dot 1 active ✓. CTA bottom-anchored dark pill ✓.

## onboarding2 — **GAP**

- **[major] Mascot wings detached from body** (atas kartu ilustrasi). Design Kiko has darker-blue wings attached at the body sides; app renders the wings as two floating ovals separated from the body (one lower-left, one mid-right), plus pointy ears vs design's rounded nubs. → Fix: use one composed design asset for Kiko (same fix as onboarding1) instead of separately positioned layers.
- **[minor] First stat-card icon glyph differs** (kartu putih kiri "20 menit"). Design shows a clock face; app shows a stopwatch (top button + side crown). → Fix: replace with the design's clock icon in the yellow circle.
- Copy OK: "20 menit / bermain", "6 meter / (20 kaki) / lihat jauh", "20 detik / istirahat", title "Aturan 20-20-20", body 2 lines, "Lanjut" all match `copy/onboarding2.md`, incl. the 2-line middle value. Dot 2 active ✓. Decorations (cloud kiri-bawah, sparkle kanan) ✓.

## onboarding3 — **GAP**

- **[major] Mascot wings detached from body** (atas kartu ilustrasi) — same defect as onboarding2: floating wing ovals, pointy ears. → Fix: same single-asset Kiko fix.
- **[minor] Glasses temple arms stick out beyond head silhouette** (mata Kiko). Design glasses are compact round frames on the face; app draws long thin arms extending horizontally past the head. → Fix: use the design's glasses geometry (frames + short arms) on the mascot asset.
- **[minor] Ruler icon glyph differs slightly** (pill "50 cm"). Design shows a diagonal ruler; app shows a horizontal ruler with ticks (same semantic, acceptable but easy to align). → Fix: reuse the design's ruler icon.
- Copy OK: "50 cm" + "jarak aman" pill, title "Jaga jarak 50 cm", body 3 lines, "Ayo Mulai!" match `copy/onboarding3.md`. Dot 3 active ✓.

## peringatan — **GAP**

- **[major] Missing dashed face-detection frame** (kiri kartu ungu, around smiley). Design wraps the smiley circle in a dashed rounded-square detection box; app shows only the plain circle. → Fix: add a dashed rounded-rect stroke box around the face circle in the camera card.
- **[major] Wrong icon on the right end of the distance row** (kanan kartu ungu). Design shows a **smartphone/device** glyph (rounded rect + screen); app shows a **telephone handset** glyph. → Fix: replace the handset icon with the phone-device icon from the design (distance is face→screen, not a call).
- **[major] Gauge arc mispositioned and misshapen** (tengah kartu pink below camera card). Design: large centered semicircle (red→yellow→green) with the dark knob on the red segment; app: smaller arc crammed into the card's top-left corner, uneven stroke, knob at the far left tip. → Fix: center the gauge horizontally in the pink card, size it to the design's semicircle, and place the knob proportionally on the red segment.
- **[major] Bottom-sheet Kiko wings detached** (tengah sheet putih). Same floating-oval wing defect as onboarding screens (wings separated from body, pointy ears); design sheet Kiko has attached wings. → Fix: same single-asset Kiko fix.
- **[minor] Coral rounded bar peeks above the bottom sheet** (batas atas sheet). An extra salmon/coral rounded rectangle is visible just above the sheet's top edge; nothing shows there in the design (the design sheet top sits at ~54% height and occludes all pink-card content below the gauge). Likely the hidden "Terlalu dekat! Mata perlu istirahat" banner positioned too high, or the sheet is too short. → Fix: align the sheet's top edge to the design (~54% of frame) so the banner is fully occluded, or move the banner down to its design y (≈63%).
- **[minor] Small ruler-Kiko in the purple card also has a detached wing** (kanan-bawah kartu ungu). → Fix: same asset fix; keep the muted purple recolor.
- Copy OK for visible text: "Pantau Jarak", pill "Terlalu dekat", "Kamera depan", "24 cm", sheet title "Terlalu dekat!", body 2 lines, "Aku Mundur!" match `copy/peringatan.md`. (Hidden layers "Terlalu dekat! Mata perlu istirahat", "Waktu bermain", "12:45", bottom nav are occluded in both shots — not judged.)

## overlay_ditolak — **GAP**

- **[major] Kiko mascot overflows the illustration card** (tengah, kartu ilustrasi). Wing ovals extend past the card's left/right edges and the feet overflow the bottom edge; design keeps the entire mascot inside the card. → Fix: same single-asset + clip-to-card fix as onboarding1.
- **[minor] Pointy ears vs design rounded ears** (tengah kartu). → Fix: use design Kiko asset.
- Copy OK: bubble "Tanpa izin ini, peringatan tak muncul di atas game atau video.", title "Izin 'Tampilan di atas aplikasi'", body 2 lines, "Buka Pengaturan", "Nanti" match `copy/overlay_ditolak.md`. Dot 1 active ✓.

---

## Cross-screen diffs (all 5 screens)

- **[major] Wrong font family everywhere** (semua teks). Design uses a rounded friendly display face (Baloo/Nunito-style — see rounded terminals in "Lanjut", "Pantau Jarak", "Terlalu dekat!"); the app falls back to the system sans (Roboto-like). This alone changes every screen's feel. → Fix: bundle and apply the design font (e.g. `fontFamily` in the theme / Compose `FontFamily`) for titles, body, and buttons.
- **[minor] Top-right link color too light** ("Lewati" / "Nanti", kanan atas). Design uses a saturated blue (#3B6FE0-ish); app renders a desaturated steel blue. → Fix: use the design's link-blue color token for these text buttons.
- **[minor] Headline weight lighter than design** (judul atas / judul sheet). Design titles are extra-bold; app titles look regular/medium. → Fix: set headline `fontWeight` to the design's bold weight (likely resolves with the font family fix).
- Tolerated and NOT reported: system status bar (10:18 etc. + shield icon), home indicator bar, clock values, dummy "24 cm"/"12:45"-style numbers with matching format, ≤4dp shifts, dash-length of connector lines.

---

## Summary

| slug | verdict | major | minor | headline issue |
|---|---|---|---|---|
| onboarding1 | GAP | 1 | 2 | mascot overflows card |
| onboarding2 | GAP | 1 | 2 | mascot wings detached |
| onboarding3 | GAP | 1 | 2 | mascot wings detached |
| peringatan | GAP | 4 | 2 | missing dashed face frame + wrong device icon + gauge mispositioned |
| overlay_ditolak | GAP | 1 | 2 | mascot overflows card |
| **cross-screen** | — | 1 | 2 | wrong font family (fallback sans) |

Copy accuracy vs `copy/*.md`: **5/5 screens exact** (wording + line breaks). All CTAs are correctly bottom-anchored dark pills. Pagination dot states are correct on every screen.

Highest-leverage fixes for the next round, in order:
1. Replace the assembled Kiko illustration with a single design-matched asset (or clip + reposition layers) — fixes 5 major items across all screens.
2. Bundle/apply the design font family — fixes the look of every text node.
3. peringatan camera card: add dashed face box, swap handset → phone-device icon, center the gauge.
