# Visual QA — iter1 Batch C (hadiah, profil_anak, dashboard_ortu, kunci)

Design ref 393×852 vs app screenshot 1080×2400. System status bar / gesture bar / clock tolerated throughout (not listed as gaps). Copy ground truth: `copy/<slug>.md`; tokens per `foundations/tokens.md` (Baloo 2 headings/numbers, Nunito body).

---

## hadiah — `screens/2264_hadiah.png` vs `app/iter1/hadiah.png`

**Verdict: GAP**

- **[GAP · High · Kiko growth card (~y258–362 design) + all 4 badge medallions (~y412–748)]** Mascot illustration is a **clearly different character**: design shows the blue owl "Kiko" (round glasses, pointed ear tufts, pink cheeks, cream belly, full body); the app shows a plain cream chick face on a solid blue disc — no glasses, no owl body, wrong silhouette. Same wrong character is repeated on all four badges ("Mata Jagoan", "Rajin Istirahat", "Jarak Juara", "Super Streak"). *Fix: swap in the Kiko mascot asset (see `mascot/kiko.png`) everywhere the mascot appears on this screen.*
- **[GAP · Medium · Unlocked badge medallions "Mata Jagoan" & "Rajin Istirahat"]** Badge art is incomplete: design has a gold blob (#FFD166) with a cream ring (#FFF6E9 stroke 4) **plus multi-color confetti** (purple #B8A7F5 / coral #FF7B6B / mint #5FD4A7 / blue #4A86E8 / gold bars & dots) around the mascot; the app has only a plain flat gold circle and no confetti. *Fix: render the badge as gold blob + cream ring + confetti layer, per the SVG badge group.*
- **[GAP · Medium · Locked badges "Jarak Juara" & "Super Streak"]** Design: grey blob (#E3E8EE) with #F2F7FF ring, faded Kiko at 0.4 opacity, navy (#12263A) lock chip at the medallion's bottom-right. App: plain light-grey circle + chick + lock chip roughly in place — medallion shape/ring and mascot both off. *Fix: use the grey blob + ring treatment and the faded Kiko art.*
- **[GAP · Minor · Header right, ~(241,65) & (353,73) design]** Decorative sparkles are wrong shape: design = two gold (#FFD166) **4-point sparkle stars ✦**; app = two plain gold **plus signs "+ +"**. *Fix: replace with the 4-point sparkle drawable (path in the SVG, `clip63`/`clip64`).*
- **[GAP · Minor · Flame icons: header "5 hari" (~250,129) and current-day circle (~284,204)]** Both flames are rendered as the **multicolor 🔥 emoji**; design uses a line-art flame (fill #FFD166 + 2px #12263A stroke). *Fix: use the outlined flame vector from the SVG.*
- **[GAP · Minor · Current-day (6th) streak circle]** Missing the 2px **#12263A ring** that the design draws around the gold #FFD166 circle (app circle has no outline). *Fix: add the navy ring stroke.*
- **[GAP · Minor · Bottom nav icons]** Icon style mismatch (systematic): design icons are **outline strokes** (#6B7B90 inactive / #2D70B7 active); app icons are **solid filled** dark navy / blue. *Fix: switch nav icons to the outline (Material-style stroke) set.*
- Copy matches `copy/hadiah.md` exactly: "Hadiah & Streak", "Streak minggu ini", "5 hari", day row S S R K J S M, "Kiko tumbuh!", "Tahap 2 · Kiko Penjaga berkacamata", "3 hari lagi menuju Tahap 3", "Koleksi Lencana", "Mata Jagoan" / "7 hari jarak aman", "Rajin Istirahat" / "5 kali istirahat", "Jarak Juara" / "Terkunci", "Super Streak" / "Terkunci", nav labels with **Hadiah** active (light-blue pill + blue label). Green day circles with dark navy checkmarks match the design (checks are ink #12263A, not white). Card radii, white-on-#F2F7FF surfaces, and the blue Kiko-card gradient (#D6E6FF→#B7D2FF) match tokens.

---

## profil_anak — `screens/2581_profil_anak.png` vs `app/iter1/profil_anak.png`

**Verdict: GAP**

- **[GAP · High · "Target harian" blue card, right side (~y355–460 design)]** Mascot is the **wrong character**: design = blue owl Kiko with glasses; app = plain cream chick face on a solid blue disc. *Fix: replace with the Kiko mascot asset.*
- **[GAP · Medium · Info pills "8 tahun" & "Kelas 3 SD" (~y285–315 design)]** Wrong pill fill **and** star icon: design = cream **#FFF6E9** background with an **outline star ☆** and ink text; app = saturated gold **#FFD166** background with a **filled star ★**. *Fix: set pill bg to #FFF6E9 and use the outline star glyph (SVG pill group).*
- **[GAP · Medium · Bottom CTA "Buka Pengaturan Orang Tua", ~y708–758 design]** CTA is **not bottom-anchored** — it sits ~45–50 dp too high, leaving a large empty area below (design keeps the outlined pill near the bottom, just above the cloud decoration). *Fix: anchor the CTA to the bottom of the content column with the design's spacing (~40–90 px bottom margin).*
- **[GAP · Minor · Decorations beside avatar (~(310,190) & (105,155) design)]** Wrong decoration shape: design = two gold (#FFD166) **4-point sparkles ✦**; app = two plain gold **dots**. *Fix: use the sparkle drawable.*
- **[GAP · Minor · Header "Profil Anak"]** Title renders at a lighter weight than the design's bold (Baloo 2 700, size 18). *Fix: apply the H2/heading bold style.*
- Copy matches `copy/profil_anak.md` including the line break "Khusus Orang Tua — tahan 3 detik untuk / membuka": back chevron, "Profil Anak", avatar "R" on gold circle, "Raka Pratama", "8 tahun", "Kelas 3 SD", "Target harian", "60 menit", "waktu main gadget per hari", "Pengaturan Sensitif", "Batas waktu bermain" / "60 menit", "Jarak minimum" / "50 cm", "Buka Pengaturan Orang Tua". Lock chip (navy circle + white lock), row greys, outlined CTA (blue border, blue label), and the bottom-right cloud decoration all match.

---

## dashboard_ortu — `screens/2669_dashboard_ortu.png` vs `app/iter1/dashboard_ortu.png`

**Verdict: GAP**

- **[GAP · High · "Laporan Mingguan" chart, ~y265–345 design]** Weekly line chart is **flat**: the app draws a straight horizontal line with all 7 points at the same height, sitting right on the green dashed "50 cm" target line. The design shows a varying trend (dip at S, rise to peak at K, dip at J, higher peak at S, slight fall at M) mostly **above** the dashed line, with a smooth curve. This is missing chart data, not dummy-value variance. *Fix: plot 7 distinct daily distances (design-like trend) with the smooth blue #4A86E8 curve and ring dots.*
- **[GAP · Medium · "Siklus" card line 2, ~y433 design]** Copy mismatch: design/`copy` reads "Siklus 14:32 · 52 cm · Aman"; the app renders "Siklus 14:32 · **—** · Aman" (em-dash placeholder instead of a distance value). Format differs from the ground truth, so this is not a tolerated dummy value. *Fix: bind the live distance string so the middle segment is "NN cm".*
- **[GAP · Medium · Header top-right, ~(300–370, 60–110) design]** Mascot is the **wrong character** (cream chick on blue disc vs the blue owl Kiko with glasses) and it is placed on a circular avatar chip; the design shows the mascot directly on the background beside the title. *Fix: use the Kiko asset without the circle backing, per design.*
- **[GAP · Minor · Header, next to title (~241,65 design)]** Missing decorative gold sparkle ✦ (app has none). *Fix: add the #FFD166 4-point sparkle.*
- **[GAP · Minor · Tips card body, "Ajak Raka bermain…"]** Line break differs: copy/design breaks after "setelah 20 " → "menit di depan layar."; the app breaks after "20 menit" → "di depan layar." *Fix: force the copy line break.*
- **[GAP · Minor · "Jarak minimum" stepper label, ~y671–698 design]** Line break differs: design stacks "Jarak / minimum" on two lines (two text runs in copy); the app renders "Jarak minimum" on one line. *Fix: keep the two-line stack.*
- **[GAP · Minor · Tips card icon & bottom nav icons]** Tips bulb is a **💡 emoji** on the gold circle (design = line-art bulb stroke #12263A); bottom-nav icons are **solid filled** where the design uses **outline strokes** (#6B7B90 / #2D70B7 active). *Fix: swap both to the line-icon set.*
- Copy otherwise matches `copy/dashboard_ortu.md`: "Dashboard Orang Tua", "Ringkasan kebiasaan Raka minggu ini", "Laporan Mingguan", "Baik" green pill, "Jarak rata-rata 7 hari", "naik 6 cm dari minggu lalu", dashed "50 cm" target + label, day row S S R K J S M, "⏱ Siklus 1 menit — aktif", "1.440 pemeriksaan/hari · log 30 hari", "Tips minggu ini", "Bagikan Laporan Mingguan (PDF / WhatsApp)" (mint #5FD4A7 pill, ink label), stepper "− 50 cm +", bottom nav with **Orang Tua** active (light-blue pill + blue label).
- Tolerated (not gaps): the big "54 cm" in the design vs "50 cm" in the app — dummy value, same format; and the green "Baik" pill / share-button colors match the mint token.

---

## kunci — `screens/3445_kunci.png` vs `app/iter1/kunci.png`

**Verdict: GAP**

- **[GAP · Medium · Layout / bottom anchoring, subtitle ~y637–661 → CTA ~y766–812 design]** Content distribution is off: the illustration card is shorter and the whole text block sits much higher than the design, leaving a **large empty void** (~15–19 % of screen height) between the PIN hint and the page-indicator/CTA, while the "Buka" CTA floats at ~80 % height instead of being bottom-anchored (design keeps ~40 px bottom margin per `components/specs.md`). *Fix: enlarge the illustration card toward the design proportion and bottom-anchor the page dots + CTA block.*
- **[GAP · Medium · PIN hint "Masukkan PIN 4 digit: …", ~y637 design]** Placeholder dots are wrong: design/`copy` uses **filled ● dots** (ink #12263A, 4×); the app renders 4 small **hollow grey rings ○**. *Fix: use filled dot glyphs/spacers.*
- **[GAP · Minor · PIN hint line break]** Copy/design breaks after "Anak cukup " → "tahan 3 detik."; the app breaks after "tahan 3" → "detik." *Fix: force the copy line break.*
- **[GAP · Minor · "Batal" top-right, ~(325,81) design]** Weight and color are off: design = Nunito **Bold 700** #4A86E8; app = lighter weight and a lighter/washed blue. *Fix: apply Button/link style (bold, #4A86E8).*
- **[GAP · Minor · Illustration card decorations, ~(35,75) top-left + 3 white sparkles inside the card]** Missing sparkle set: design has a gold ✦ top-left and **three white 4-point sparkles** around the mascot (plus the cloud); the app has only a gold **dot** and the cloud. *Fix: add the sparkle decorations.*
- **[GAP · Minor · Mascot in illustration card]** Same character concept and palette (blue body, cream belly, pink cheeks, yellow beak/feet) but a visibly different rendition: design Kiko has **pointed owl ear tufts** and large glossy multi-highlight eyes; the app version has **round bear-like ears** and simpler flat eyes. *Fix: use the canonical Kiko art (this is a style/shape mismatch, not a wrong character like the other screens).*
- Copy matches `copy/kunci.md` including the speech-bubble line break "PIN ini menjaga pengaturan tetap di / tangan orang tua.": "Batal", "Masuk Mode Orang Tua", "Masukkan PIN 4 digit: … · Anak cukup tahan 3 detik." (wording), page indicator (blue elongated pill + 2 grey dots), and the bottom "Buka" CTA (dark navy #12263A pill, white label). Speech bubble, gradient card, and cloud decoration are present and correctly placed.

---

## Summary

| slug | verdict | # gaps | key issue |
|---|---|---|---|
| hadiah | **GAP** | 7 | Wrong mascot character (chick vs Kiko owl) on growth card + all 4 badges; badge art missing blob/ring/confetti |
| profil_anak | **GAP** | 5 | Wrong mascot; pills wrong color (#FFD166 vs #FFF6E9) + filled star; CTA not bottom-anchored |
| dashboard_ortu | **GAP** | 7 | Weekly chart renders flat (no trend); "52 cm" shows as "—" in Siklus line; wrong mascot |
| kunci | **GAP** | 6 | Large layout void / CTA not bottom-anchored; PIN dots hollow vs filled ●; copy line-break diffs |

Batch totals: 0 PASS, 0 PASS-MINOR, 4 GAP (25 concrete defects).

Cross-screen patterns worth fixing once:
1. **Kiko mascot asset is wrong on hadiah / profil_anak / dashboard_ortu** (cream chick face on a blue disc instead of the blue owl with glasses). `kunci` uses an approximate Kiko but with round ears instead of pointed tufts.
2. **Sparkle decorations ✦ render as plus signs or plain dots** (hadiah, profil_anak, dashboard_ortu, kunci).
3. **Emoji stand-ins for line icons** (🔥 flame on hadiah, 💡 bulb on dashboard_ortu) — design uses stroke vectors.
4. **Bottom-nav icons are solid-filled; design uses outline strokes** (hadiah, dashboard_ortu).
5. **Typography**: app uses a rounded geometric font throughout; tokens specify **Baloo 2** (H1/H2/Number/Display) + **Nunito** (Body/Caption/Micro/Button). Also several headings render lighter than the spec's 700 weight.
6. **Bottom-anchored CTAs float too high** on profil_anak and kunci, leaving dead space below.
