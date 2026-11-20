# Visual QA — iter2 Batch C (hadiah, profil_anak, dashboard_ortu, kunci)

Design ref 393×852 vs app screenshot 1080×2400 (2.625×). System status bar / gesture bar / clock tolerated throughout. Copy ground truth: `copy/<slug>.md`; dummy values tolerated only when format matches. All 25 iter1 Batch C defects re-checked against `screens/*.png` vs `app/iter2/*.png`.

---

## hadiah — `screens/2264_hadiah.png` vs `app/iter2/hadiah.png`

**Verdict: PASS-MINOR**

Iter1 category status (7/7 addressed):

| iter1 category | status |
|---|---|
| Wrong mascot (growth card + all 4 badges) | **FIXED** — Kiko owl with glasses, pink cheeks, cream belly everywhere |
| Unlocked badge art (blob/ring/confetti) | **FIXED** — gold medallion + cream ring + multi-color confetti (purple/coral/mint/blue) |
| Locked badge treatment | **FIXED** — grey medallion + faded Kiko + navy lock chip at bottom-right |
| Header sparkles (were "+ +") | **FIXED** — two gold 4-point ✦ |
| Flame icons (were 🔥 emoji) | **FIXED** — line-art flame (gold fill + navy stroke) on "5 hari" and current-day circle |
| Current-day 6th circle ring | **FIXED** — 2px navy ring on the gold circle |
| Bottom-nav icons (were solid) | **FIXED** — outline stroke set, Hadiah active with light-blue pill |

Copy: full match to `copy/hadiah.md` (all strings, day row S S R K J S M, navy checkmarks, nav labels with Hadiah active). Progress bar, card radii, #F2F7FF surfaces, and the blue Kiko-card gradient match.

Remaining gaps:

- **[GAP · Minor · Unlocked badge medallions "Mata Jagoan" & "Rajin Istirahat"]** The gold medallion is rendered as a clean circle; the design uses an organic **blob** shape (slightly wobbly outline). *Fix: use the blob path from the SVG badge group instead of a perfect circle.*
- **[GAP · Minor · Kiko art, growth card + badges]** Ear tufts read as short rounded nubs vs the design's pointed triangular owl tufts (eye gloss slightly flatter). Subtle at this scale. *Fix: same canonical-Kiko asset swap as kunci (shared item below).*

---

## profil_anak — `screens/2581_profil_anak.png` vs `app/iter2/profil_anak.png`

**Verdict: PASS-MINOR**

Iter1 category status (5/5 addressed):

| iter1 category | status |
|---|---|
| Wrong mascot on "Target harian" card | **FIXED** — Kiko owl with glasses, placed directly on the card (no blue disc) |
| Info pills (gold fill + filled star) | **FIXED** — cream #FFF6E9 fill + outline star ☆ + ink text |
| CTA not bottom-anchored | **FIXED** — "Buka Pengaturan Orang Tua" now sits at ~83–91% height, matching the design's bottom-anchored position (verified against iter1, which floated at ~75–83%) |
| Avatar decorations (were plain dots) | **FIXED** — gold 4-point ✦ beside avatar |
| Header title weight | **FIXED** — "Profil Anak" renders bold |

Copy: full match to `copy/profil_anak.md`, including the line break "Khusus Orang Tua — tahan 3 detik untuk / membuka". Lock chip, row greys, outlined CTA (blue border + blue label), and bottom-right cloud all match.

Remaining gaps:

- **[GAP · Minor · Vertical rhythm, settings card → CTA]** The empty band between the "Pengaturan Sensitif" card and the CTA is ~2× the design's gap (~9.5% of screen height vs ~4%): the CTA is correctly bottom-anchored but the cards don't expand to close the mid-lower void. *Fix: grow card padding/heights (or add a spacer distribution) so the content groups with the CTA as tightly as the design.*
- **[GAP · Minor · Kiko art on target card]** Ear tufts still rounder than the design's pointed tufts. *Fix: canonical Kiko asset (shared item).*

---

## dashboard_ortu — `screens/2669_dashboard_ortu.png` vs `app/iter2/dashboard_ortu.png`

**Verdict: PASS**

Iter1 category status (7/7 fixed):

| iter1 category | status |
|---|---|
| Flat weekly chart | **FIXED** — 7-point smooth curve with the design's trend shape (low S → mid S → dip R → peak K → dip J → highest S → slight fall M), ring dots, dashed 50 cm line |
| "—" placeholder in Siklus line | **FIXED** — renders "Siklus 14:32 · 52 cm · Aman" exactly per copy |
| Wrong mascot + circular chip | **FIXED** — Kiko owl with glasses placed directly on the background beside the title |
| Missing header sparkle | **FIXED** — gold 4-point ✦ next to the title |
| Tips body line break | **FIXED** — breaks after "setelah 20 " → "menit di depan layar." per copy |
| "Jarak minimum" one line | **FIXED** — stacked "Jarak / minimum" two-line label |
| 💡 emoji + solid nav icons | **FIXED** — line-art bulb on gold circle; outline nav icons with Orang Tua active |

Copy: full match to `copy/dashboard_ortu.md` ("50 cm" big number is a tolerated dummy for the design's "54 cm", same format). Mint share pill, "Baik" pill, stepper "− 50 cm +", and day row all match. Chart data is dummy-like but the trend shape now mirrors the design (this is format-equivalent, not a gap).

Remaining gaps: none actionable. (Header Kiko ear tufts slightly rounder than design — negligible at this size; see shared item.)

---

## kunci — `screens/3445_kunci.png` vs `app/iter2/kunci.png`

**Verdict: PASS-MINOR**

Iter1 category status (4 fixed, 2 partial):

| iter1 category | status |
|---|---|
| Layout void / CTA not bottom-anchored | **FIXED** — illustration card enlarged toward design proportion, text block lowered, page dots + "Buka" CTA bottom-anchored (~90–96% height, ~40px bottom margin) |
| PIN placeholder dots (were hollow rings) | **FIXED** — 4 filled ● (ink) |
| PIN hint line break | **FIXED** — breaks after "Anak cukup " → "tahan 3 detik." per copy |
| "Batal" weight/color | **FIXED** — bold #4A86E8 |
| Sparkle set (gold + 3 white) | **PARTIAL** — gold ✦ top-left and in-card both present; only 2 of 3 white in-card sparkles render |
| Kiko hero rendition (round bear ears) | **PARTIAL** — improved (tapered ear tips, eyebrows, glossier eyes) but ears are still wide rounded petals vs the design's pointed triangular tufts |

Copy: full match to `copy/kunci.md`, including the speech-bubble break "PIN ini menjaga pengaturan tetap di / tangan orang tua." Speech-bubble tail is now centered (was right-edge in iter1). Page indicator (blue elongated pill + 2 grey dots) and navy "Buka" CTA match.

Remaining gaps:

- **[GAP · Minor · Hero Kiko in illustration card]** Ears are elongated rounded petals angled outward; the design has short pointed triangular owl tufts. This is the most visible of the shared mascot residuals. *Fix: swap the hero illustration to the canonical Kiko art (`mascot/kiko.svg` / `mascot/kiko.png`).*
- **[GAP · Minor · In-card white sparkles]** Design has three white 4-point sparkles (left-mid, right-upper, right-lower at ~(330,455) design, beside the mascot's right hand); the app renders two — the lower-right one is missing. *Fix: add the third white sparkle.*

---

## Summary

| slug | verdict | iter1 fixed | remaining | key residual |
|---|---|---|---|---|
| hadiah | **PASS-MINOR** | 7/7 | 2 minor | Badge gold shape is a circle, not the design's blob |
| profil_anak | **PASS-MINOR** | 5/5 | 2 minor | Settings→CTA gap ~2× design; Kiko ear tufts |
| dashboard_ortu | **PASS** | 7/7 | 0 actionable | — |
| kunci | **PASS-MINOR** | 4 fixed + 2 partial | 2 minor | Hero Kiko ears; 1 of 3 white sparkles missing |

Batch totals: 1 PASS, 3 PASS-MINOR, 0 GAP. All 25 iter1 defects resolved or substantially resolved (kunci sparkles 2/3 and kunci hero-mascot shape are the only partials).

Iter3 punch list (4 items, one shared asset):
1. **Canonical Kiko asset** — swap in `mascot/kiko.svg` art everywhere the mascot renders (kunci hero most visibly; also profil_anak target card, hadiah growth card/badges, dashboard header). Fixes the shared ear-tuft residual in one change.
2. **kunci** — add the missing third in-card white sparkle (lower-right of the mascot).
3. **kunci** — nothing else; layout/CTA/PIN/"Batal"/copy all good.
4. **profil_anak** — tighten the settings-card→CTA gap (expand cards or redistribute) to the design's ~4% spacing.
5. **hadiah** — replace the circular gold badge medallion with the blob path from the SVG badge group.

Cross-screen notes:
- Typography now reads consistent with the Baloo 2 + Nunito token pairing (iter1 pattern 5 no longer a gap).
- Bottom-nav icons are the outline stroke set on both nav-bearing screens (iter1 pattern 4 closed).
- Emoji stand-ins (🔥 / 💡) are gone; line-art vectors in place (iter1 pattern 3 closed).
