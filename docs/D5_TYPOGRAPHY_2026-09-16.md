# D5 — Cyrillic-first typography: verification and decision record (2026-09-16)

Status: `VERIFIED ON DEVICE / CHROME DECISION MADE / GEOMETRIC UPGRADE BLOCKED ON AN OWNER-SUPPLIED ASSET`

Owner ruling: `docs/OWNER_DECISIONS_2026-09-15.md` §2 D5 — "Шрифт нужно адаптировать под референс,
приоритет за кириллицей": interface = geometric/condensed sans **with Cyrillic**, HUD service layer =
monospace uppercase **with Cyrillic**, only OFL/Apache-class redistributable fonts may be bundled
(`docs/CYBERGRAM_UI_SPEC.md` line 108), no proprietary or game-ripped fonts, message body text stays
highly readable.

This document records the measurement, the resulting decision, and the exact reason the remaining
half is not implemented. It changes no production file.

## 1. What was measured, and how

Method is machine evidence, not recall:

- the `sans-serif-condensed` family was resolved **on the target device** (AVD `Cybergram_API36`,
  `emulator-5554`, Android 16 / API 36, `x86_64`) rather than assumed:
  `adb shell grep -A6 sans-serif-condensed /system/etc/fonts.xml` shows the family maps to
  `/system/fonts/Roboto-Regular.ttf` with a variable-font `wdth=75` axis;
- that device font file was pulled and its `cmap` parsed with a standard-library-only script
  (`.local-artifacts/r2/cmap_coverage.py`, git-excluded). `fontTools` is not installed and the
  network is closed, so no third-party parser or downloaded font was used.

## 2. Result — Cyrillic coverage

| font | source | bytes | Cyrillic U+0400-04FF | Cyrillic Supp | Cyrillic Ext-A | Greek |
|---|---|---|---|---|---|---|
| `Roboto-Regular.ttf` (**what `sans-serif-condensed` actually resolves to on the target device**) | pulled from `/system/fonts` of `Cybergram_API36` | 2 371 712 | **256/256** | **48/48** | **32/32** | 121/144 |
| `rmono.ttf` (Roboto Mono) — already bundled, Apache-2.0 | `TMessagesProj/src/main/assets/fonts/` | 71 864 | 255/256 | 20/48 | 0/32 | 75/144 |
| `rcondensedbold.ttf` (Roboto Condensed Bold) — already bundled, Apache-2.0 | same | 127 548 | 255/256 | 20/48 | 0/32 | 75/144 |

The single missing Cyrillic codepoint in the two bundled assets is `U+0487` (COMBINING CYRILLIC
POKRYTIE, a combining mark, not a letter); every Cyrillic letter, including `Ё Ї Є Ђ Ј Љ Њ Ћ Џ Ў` and
the palochka range `U+04C0-04CF`, is present.

**Conclusion for the landed chrome:** `CybergramTypography`'s `sans-serif-condensed`
(`CybergramTypography.java:25/33/41`) is **Cyrillic-complete on the target device, with the broadest
coverage of every candidate in the tree** (full Cyrillic Supplement and Ext-A, not just the base
block). Cyrillic is therefore first-class, not a fallback, and the D5 half that the owner made
non-negotiable is satisfied — verified from the device's own font binary rather than from
documentation.

## 3. Why the reference's own font names cannot be used

`design/DESIGN_TARGET.md` lines 21 and 23 name `Rajdhani` / `Inter` / `Roboto Condensed` for the
interface and `Share Tech Mono` / `JetBrains Mono` for the HUD layer. Of those:

- **`Rajdhani` ships Latin + Devanagari and has no Cyrillic** — it is disqualified by the D5 ruling
  itself, which makes Cyrillic a first-class script;
- **`Share Tech Mono` ships Latin only and has no Cyrillic** — likewise disqualified;
- `Inter`, `Roboto Condensed` and `JetBrains Mono` are Cyrillic-capable and OFL/Apache-class, so they
  remain valid candidates.

This is stated from upstream coverage knowledge, **not** from a fetched binary (network is closed in
this environment), so it must be re-checked with the `cmap` script at asset-intake time.

## 4. Why no font is bundled in this round

- The environment has **no network** (`raw.githubusercontent.com` and `api.github.com` both fail TLS
  from the sandbox), so no OFL asset can be obtained and verified here.
- The repo already bundles Roboto and Roboto Mono (Apache-2.0) but **no condensed regular**: the only
  condensed asset is `rcondensedbold.ttf`, Bold-only. Pairing it with non-condensed `rmedium.ttf`
  would change metrics and the condensed voice, so it is not a clean substitution.
- Bundling anything new carries an APK-size note and a licence/NOTICE obligation
  (`docs/CYBERGRAM_UI_SPEC.md` line 108), which needs an owner-approved asset rather than an
  improvised one.
- `minSdk` is 21: `Typeface.CustomFallbackBuilder` is API 29 and `<font-family>` XML cannot express a
  cross-script fallback chain, so any new bundled Latin+Cyrillic face would rely on the platform's
  implicit fallback for Thai/Arabic/CJK/Devanagari. That is the same mechanism the app already relies
  on for every bundled asset, but it is a reason to keep a new face confined to chrome — which is
  what `CybergramTypography` already does (`isChrome(provider)` returns the caller's upstream
  typeface otherwise).

## 5. Options carried forward (owner decision)

| # | option | change | APK | risk |
|---|---|---|---|---|
| a | **keep `sans-serif-condensed`** (current, and what this round records) | none | 0 | OEM family substitution on non-AOSP devices; neo-grotesque condensed, not geometric |
| b | add one geometric/condensed OFL face for chrome — e.g. **IBM Plex Sans Condensed** (Regular + Bold, designed with Cyrillic) or **Inter** | new assets + `CybergramTypography` family change + a licence/NOTICE note | ≈250–600 KB disk for 2 weights, roughly 150–350 KB compressed | lowest-fidelity-waste upgrade; needs the owner to supply the TTFs |
| c | additionally back the HUD/monospace service layer with **`rmono.ttf`** (already bundled, 255/256 Cyrillic, Apache-2.0) | a `CybergramTypography` mono resolver, **but there is no consumer today** | 0 | none — deferred until the B7 HUD label layer is authorized, to avoid dead code |

**Blocked item:** option (b) needs an owner-supplied OFL asset (or an explicit instruction to fetch
one from a host with network access, followed by a verified `cmap` check). Until then the interface
stays on the Cyrillic-complete system condensed family and no asset is bundled.

## 6. Not changed by this record

No production file, no bundled asset, no licence file, no `docs/CYBERGRAM_UI_SPEC.md` amendment
(the authority's typography section still describes `sans-serif-condensed` and remains accurate).
