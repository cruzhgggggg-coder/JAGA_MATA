# Visual QA — iter2 Batch A (onboarding1–3, peringatan, overlay_ditolak)

Compared: `screens/<id>_<slug>.png` (design, 393×852) vs `app/iter2/<slug>.png` (1080×2400). Copy verified against `copy/<slug>.md`. Tolerated: system status bar / clock, anti-aliasing, ≤4dp shifts, dummy values with same format.

---

## onboarding1 — **PASS-MINOR**

Iter1 defects:
- **[major] mascot overflows illustration card → FIXED.** Wings, body, head all sit inside the card now (pixel-exact Kiko asset).
- **[minor] pointy ears vs rounded nubs → FIXED.** Ear geometry now matches the design.
- **[minor] title weight lighter → FIXED.** Headline renders in the rounded extra-bold display face.

Remaining:
- **[minor] Kiko feet touch/clip at the card's bottom edge** (bawah kartu ilustrasi). Design keeps the yellow feet fully inside with a ~12–15dp margin below them; app places the asset so the feet are cut by the card's bottom rounded edge. → Fix: add ~12dp bottom inset (or scale the mascot ~5% down) inside the card so the whole asset clears the bounds.

Copy OK (bubble 2 lines, title, body 3 lines, "Lanjut" exact vs `copy/onboarding1.md`). Dot 1 active ✓. "Lewati" saturated blue ✓. Rounded font ✓.

## onboarding2 — **PASS**

Iter1 defects:
- **[major] mascot wings detached → FIXED.** Single composed Kiko asset, wings attached at body sides.
- **[minor] first stat icon was a stopwatch → FIXED.** Now the design's clock face in the yellow circle.

Remaining: none. Stat trio (clock / eye / pause), 2-line middle value, decorations (cloud kiri-bawah, sparkle kanan), dot 2, rounded bold title all match. Copy exact vs `copy/onboarding2.md`.

## onboarding3 — **PASS-MINOR**

Iter1 defects:
- **[major] mascot wings detached → FIXED.**
- **[minor] glasses temple arms protrude past head → FIXED.** Compact round frames with short arms, matching design.
- **[minor] ruler icon horizontal vs diagonal → FIXED.** Now the design's diagonal ruler in the green circle.

Remaining:
- **[minor] "50 cm" pill sits too low** (bawah kartu ilustrasi). Design gap pill-top ↔ mascot-feet ≈ 25dp (pill hugs the feet at ~50–56% of card height); app gap ≈ 60–65dp, leaving a visible empty band. → Fix: move the pill up to its design y (reduce spacing between mascot and pill to ~25dp).
- **[trivial] "cm" renders smaller than "50"** in the pill; design sets "50 cm" at one uniform 32sp size. → Fix: use a single text node / equal font size for "50 cm".

Copy OK ("50 cm", "jarak aman", title, body 3 lines, "Ayo Mulai!"). Dot 3 active ✓.

## peringatan — **PASS-MINOR**

Iter1 defects:
- **[major] missing dashed face-detection frame → FIXED.** Dashed rounded-square box wraps the smiley circle.
- **[major] handset icon on distance row → FIXED.** Now the smartphone/device glyph in the right circle (face → screen, correct semantics).
- **[major] gauge mispositioned/misshapen → FIXED (position + shape).** Semicircle red→yellow→green is centered in the pink card with the knob on the red segment. (Size residual below.)
- **[major] bottom-sheet Kiko wings detached → FIXED.**
- **[minor] coral bar peeking above sheet → FIXED.** Sheet top now at ~54% height, occludes the hidden banner as in design.
- **[minor] ruler-Kiko detached wing → FIXED.** Purple recolor keeps attached wings and the horizontal ruler.

Remaining:
- **[minor] gauge arc undersized** (tengah kartu pink). Arc diameter ≈ 60% of the pink-card width vs design ≈ 80%; the gauge reads noticeably smaller than the reference. → Fix: increase gauge radius ~30%, keeping the current center point and proportional knob placement on the red segment.

Copy OK for visible text ("Pantau Jarak", pill "Terlalu dekat", "Kamera depan", "24 cm", sheet "Terlalu dekat!", body 2 lines, "Aku Mundur!"). Hidden layers (banner, "Waktu bermain", "12:45", bottom nav) occluded in both shots — not judged.

## overlay_ditolak — **PASS-MINOR**

Iter1 defects:
- **[major] mascot overflows illustration card → FIXED** for wings/head; residual foot clip below.
- **[minor] pointy ears → FIXED.**

Remaining:
- **[minor] Kiko feet touch/clip at the card's bottom edge** (bawah kartu ilustrasi) — same residual as onboarding1 (design keeps full feet with a small margin). → Fix: same bottom inset / slight scale-down.

Copy OK (bubble 2 lines, title, body 2 lines, "Buka Pengaturan", "Nanti"). Dot 1 active ✓.

---

## Cross-screen (all 5 screens)

- **[major] wrong font family → FIXED.** Rounded friendly display face now applied to titles, body, and buttons ("Lanjut", "Pantau Jarak", "Terlalu dekat!", etc.).
- **[minor] top-right link color too light → FIXED.** "Lewati" / "Nanti" now use the saturated design blue.
- **[minor] headline weight lighter → FIXED** (resolved with font + bold weight).
- No new cross-screen defects. Tolerated and not reported: status bar/clock, home indicator, dummy numbers in matching format, ≤4dp shifts, dash-length variance.

---

## Summary

| slug | verdict | iter1 defects fixed | remaining gaps |
|---|---|---|---|
| onboarding1 | PASS-MINOR | 3/3 FIXED | feet clip at card bottom (minor) |
| onboarding2 | PASS | 2/2 FIXED | — |
| onboarding3 | PASS-MINOR | 3/3 FIXED | "50 cm" pill gap too large (minor); "cm" size (trivial) |
| peringatan | PASS-MINOR | 6/6 FIXED | gauge arc undersized (minor) |
| overlay_ditolak | PASS-MINOR | 2/2 FIXED | feet clip at card bottom (minor) |
| **cross-screen** | — | 3/3 FIXED | — |

Copy accuracy vs `copy/*.md`: **5/5 screens exact** (wording + line breaks). All CTAs bottom-anchored dark pills ✓. Pagination dot states correct ✓.

All 13 iter1 defects (7 major + 6 minor) are resolved; no NOT-FIXED items. Remaining work is 3 small polish items:
1. onboarding1 + overlay_ditolak: keep Kiko feet fully inside the illustration card (~12dp bottom inset).
2. onboarding3: raise the "50 cm" pill to sit just under the mascot (~25dp gap).
3. peringatan: enlarge the gauge arc ~30% to match the design's card fill.
