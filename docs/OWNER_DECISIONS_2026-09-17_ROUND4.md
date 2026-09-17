# Owner decision log — 2026-09-17 (round 4: microphone glyph centring) and next-round task plan

Status: `AUTHORITATIVE OWNER INSTRUCTION — IMPLEMENTED / E-TIER VERIFIED / NOT COMMITTED`.

Base at recording time: `dev` HEAD `5a5026dd3` ("ui: refine the Cybergram chat surface to the round-3
concept"), working tree carrying exactly one uncommitted change (this round). This record performs no
push and no commit.

This file extends the round-3 log (`docs/OWNER_DECISIONS_2026-09-17.md`) with repository id **D10.1**
so it does not collide with the landed `D1`–`D9`.

## 0. Owner instruction, verbatim (2026-09-17, RU)

> Центрируй иконку микрофона относительно её рамки. Наметь дальнейший план задач и пускай воркеров
> в работу

## 1. Ruling D10.1 — the microphone glyph is centred in its plate frame

Round 3b (`D9.7`) made the microphone **plate** fill its container so the frame height equals the
composer field height (`CybergramTheme.COMPOSER_HEIGHT_DP` = 40 dp). The 24 dp **glyph** inside that
plate was not addressed: the icon view is still `DEFAULT_HEIGHT` (44 dp) and was added to the 40 dp
container on `FrameLayout`'s default child gravity (`TOP | START`). Its centre therefore sat at
`(22, 22) dp` against a plate centre of `(20, 20) dp` — **+2 dp right and down**, mirrored to the
opposite side in RTL.

Ruling: the glyph is geometrically centred in the plate. This is presentation-only and Cybergram-gated
by construction (see §2); non-Cybergram geometry must remain untouched.

## 2. Implementation (bounded, single seam)

Owner file: `TMessagesProj/src/main/java/org/telegram/ui/Components/ChatActivityEnterView.java`.

Working-tree diff (line 3489 region):

```java
        padding = dp(10f);
        audioVideoSendButton.setPadding(padding, padding, padding, padding);
-       audioVideoButtonContainer.addView(audioVideoSendButton, LayoutHelper.createFrame(DEFAULT_HEIGHT, DEFAULT_HEIGHT));
+       // Owner ruling (round 4): center the microphone glyph in its plate frame. Under Cybergram the
+       // control container is COMPOSER_HEIGHT_DP (40 dp) while this icon view stays DEFAULT_HEIGHT
+       // (44 dp); the default TOP|START placement therefore put the 24 dp glyph 2 dp right and down of
+       // the plate centre. Gravity.CENTER is a no-op for every other theme (container == 44 dp).
+       audioVideoButtonContainer.addView(audioVideoSendButton, LayoutHelper.createFrame(DEFAULT_HEIGHT, DEFAULT_HEIGHT, Gravity.CENTER));
```

Why this is a no-op outside Cybergram: `audioVideoButtonContainer` is the only child layout at
`ChatActivityEnterView.java:3287` and is always `getComposerHeight()` square (`:6522-6524`) — 44 dp
unless `CybergramTheme.isCybergramPresentation(...)` is true. `FrameLayout.DEFAULT_CHILD_GRAVITY` is
`Gravity.TOP | Gravity.START`; with container == child (44 == 44) `Gravity.CENTER` resolves to
`childLeft = childTop = (44 − 44) / 2 = 0`, pixel-identical to the old placement. No caller reads this
private view's `LayoutParams`; the externally visible tooltips anchor to the container
(`getAudioVideoButtonContainer()`), whose bounds are unchanged.

No other microphone/glyph site needs the same treatment: the recording glyph is a separate
`RecordCircle` overlay that already draws its own `micDrawable` centred at `(cx, cy)`; the
`audioVideoButtonContainerForbidden` draw override centres its `micOutline` at the view centre; the
send/cancel buttons live in `sendButtonContainer`. Repo-wide, `COMPOSER_HEIGHT_DP` /
`CybergramHudDrawable` reach only this plate.

Pixel-exact residual: `AndroidUtilities.dp` rounds up, so at density 420 `dp(40) = 105` and
`dp(44) = 116`; Java truncates `(105 − 116) / 2 = −5`, leaving the glyph **+0.5 px (≈0.19 dp)** from
the plate centre rather than literally 0. The nominal dp result is 0 for both axes.

## 3. Validation

**Build — PASSED.** `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline` →
`BUILD SUCCESSFUL in 7m 13s` (Gradle 8.11.1 / AGP 8.10.1, JDK 17.0.20.1+1). Note for the runbook:
this repository has no `gradlew.bat`; the Windows invocation used here is the POSIX `gradlew` under
Git Bash. The runbook's `gradlew.bat` form is stale.

**APK — PASSED.** Path `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`, 72,920,430 bytes,
SHA-256 `E8862767652B67EBC9BA5917ACECBE7B9750D77DB2C28F22F3138C32DCC966C6`.

**Device — PASSED on `emulator-5554` (AVD `Cybergram_API36`, API 36, x86_64, 1080×2400 @ 420 dpi,
authenticated session).** `adb install -r` → `Success`; launch → no FATAL/ANR; crash-buffer
`FATAL EXCEPTION` count = 0.

**Pixel evidence — PASSED.** Artifacts (git-excluded) in `.local-artifacts/run-r4-20260917/`:
`before-chat.png`, `after-chat.png` (full-res, same device/session/chat — Saved Messages, composer at
rest), and the matching 3× crops `before-composer-zoom.png` / `after-composer-zoom.png` (crop rule:
native `x[0..1079] y[2210..2399]`, ×3 → 3240×570).

Measured by two independent classifiers applied identically to both captures (cyan outline:
`B > 110 && B > R+25 && G > 90`; glyph ink: `min(R,G,B) > 175`), at native 1080×2400 (2.625 px/dp)
and on the 3× zoom crops:

| quantity (native px) | BEFORE | AFTER |
|---|---|---|
| plate bbox (anchor) | `x[955..1060] y[2207..2312]` (106×106 = 40.4 dp) | bit-identical |
| plate centre | (1007.50, 2259.50) | (1007.50, 2259.50) |
| ink bbox | `x[995..1032] y[2242..2293]`, 38×52, 824 px | `x[990..1027] y[2237..2288]`, 38×52, 824 px |
| ink centre | (1013.50, 2267.50) | (1008.50, 2262.50) |
| ink − plate | dx +6.00, dy +8.00 | dx +1.00, dy +3.00 |
| ink-bottom → plate-bottom gap | 19 | 24 |

Differential: a **rigid −5.00 px translation on both axes** — identical ink bbox size and identical
824-pixel count, i.e. nothing was rescaled or re-laid-out (zoom cross-check: −15.00 / −15.00, gap
+15.00). The plate anchor is bit-identical before and after, so the absolute comparison is valid; the
fresh BEFORE reproduces the archived round-3b capture (`round3b-composer-zoom.png`) to 0.0 px on ink
centre, so the A/B is controlled.

The integer-truncation model is confirmed to the pixel: `dp(40) = 105`, `dp(44) = 116`, Java
`(105 − 116) / 2 = −5` (not −5.25). Under `Gravity.CENTER` the 116 px view's centre lands at
`(1008.0, 2260.0)` against the plate centre `(1007.50, 2259.50)` — the 0.5 px bbox-quantisation
floor, i.e. the **view itself is centred exactly**. The residual absolute ink offsets (+1.00, +3.00 px)
are intrinsic to the glyph, not to the layout: `ink centre − view centre = (+0.5, +2.5) px`, identical
before and after, because the microphone's capsule/stand/base ink is asymmetric inside its 24 dp
canvas. An absolute `|ink − plate| ≤ 2 px` gate therefore cannot be met on `dy` even by a perfect fix;
the differential is the authoritative test and it passes.

**Independent adversarial review — `SAFE`.** Confirmed: default child gravity `TOP|START`; container
== child for every non-Cybergram theme; the glyph is not clipped (ink spans 22..84 inside the 0..105
plate); the `s = 1 − expandStickersButton.getAlpha()` scale is applied to the plate only and restored
before `super.dispatchDraw`, so children are not scaled; no other glyph site needs centring.

**Validation boundary.** This is **E-tier** evidence for the round-4 change. The non-Cybergram control
screenshot was **not** taken (it needs a persisted theme mutation, deliberately not performed here);
the non-Cybergram no-op is proven statically. A and P tiers remain open. The largest outstanding item
in the backlog is still A-tier verification debt, not new presentation work.

## 4. Next-round task plan (each task requires explicit owner authorization)

Derived from a read-only audit of D9.1–D9.8 against the landed code. Every item below is DONE on
static evidence except where marked; the two genuine residual gaps are **item 16** (header icon
style: colour is landed, but the back/overflow icons are upstream solid rasters and the search icon is
a filled-path vector, so the "one thin linear style" is only partially true) and **item 21** (the
enabled microphone glyph is tinted `Color.WHITE`, not the ruled pale blue `#8FBFCC`).

| id | Scope (bounded) | File(s) | Risk | Validation | Deps |
|---|---|---|---|---|---|
| **R4-1** | Commit the round-4 mic-centring change + compile/E evidence | `Components/ChatActivityEnterView.java` (already modified) | Low | C + E | owner commit right |
| **R4-2** | Under Cybergram only, tint the enabled mic glyph to the ruled pale blue at the two tint sites (keep `Color.WHITE` otherwise) | `ChatActivityEnterView.java:6633-6634`, `:10466` | Low | C + E (A if session) | R4-1 |
| **R4-3** | Owner decision on items 16/21: accept PARTIAL and amend the authority docs, **or** authorize R4-4; optionally wire/drop the unused `ICON_PALE` token | `docs/OWNER_DECISIONS_2026-09-17.md`, `docs/CYBERGRAM_UI_SPEC.md`; optional `CybergramTheme.java:33` | None | None | — |
| **R4-4** *(only if R4-3 = redraw)* | Cybergram-only thin linear vector back/overflow icons matching the search ring, behind an explicit Cybergram opt-in; do **not** edit the shared `ic_ab_back`/`ic_ab_other` (≈100 screens) | new `res/drawable/cybergram_*.xml`; `ChatActivity.java:4252-4297`; `ActionBar`/`ActionBarMenuItem` (gated) | High (shared ActionBar) | E control + A | R4-3(b) |
| **R4-5** | Resolve **R-D6** now that `BUBBLE_GAP_EXTRA_DP == 3f` equals the documented `< dp(3)` ceiling: clamp/assert strictly below, or update the invariant with measured clearance; verify emoji-only and grouped bubbles | `CybergramTheme.java:78-93`, `MessageDrawable.java:823-831` | Medium (paint-only geometry) | E visual matrix + A | owner authorization (R-series) |
| **R4-6** | Close round-3/3b validation debt: non-Cybergram control screenshot; authenticated (A) round-3 chat-surface matrix; P-tier pass | runbook `docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md` + evidence docs | Medium — opening conversations marks messages read → owner decision | E control + A + P | R4-1 |
| **R4-7** *(optional)* | `getComposerHeight()` is static and calls `isCybergramPresentation(null)`, ignoring the per-view provider (debug `GeometryProvider` only); make provider-aware or document | `ChatActivityEnterView.java:6522-6524` | Low | C | — |

Suggested order: R4-1 → R4-2 → R4-3 → R4-5 → R4-6 → (R4-4 if ruled) → R4-7.

## 5. Standing backlog — authorization status (unchanged by this round)

| Track | Recorded status | Owner authorization unlocks |
|---|---|---|
| **B0 / B0-FIX** | `E PASS / A PARTIAL / P NOT RUN` | remaining authenticated filter-tab matrix + non-Cybergram control re-run |
| **B1** | `INTEGRATED / E PASS / A PARTIAL / P PENDING` | full authenticated main-tabs interaction matrix + P |
| **B2** | `INTEGRATED / AUDIT COMPLETE` (14 PASS / 0 defect / 6 DESIGN-OPEN / 15 UNTESTED) | the 15 account-dependent rows (read-marking side effect) |
| **B5 / B6** | `INTEGRATED / A PENDING` | authenticated ordinary + rich service/date cases; R1 install-compat item |
| **B7** | `SPEC PREPARED / NOT AUTHORIZED` | bounded chat-canvas HUD/backdrop pass |
| **B4** | `DESIGN-OPEN / NOT AUTHORIZED` | design ruling, then separate bounded reply-plate / reaction-pill passes |
| **R-D6 / R-STUB / R-PERF / R-OVERLAY** | recorded findings, **unauthorized** | see R4-5; bounded eligibility/paint-cache/overlay-cost passes |
| **B8** | `DEFERRED / STAGE F` | secondary surfaces, split by surface |
| **R1** | `SEPARATE RELEASE TRACK` | release identity/signing; 4-ABI probe on the Redmi MIUI device |

`R-GATE` is no longer in the unauthorized R-series (fixed `4629e98a3`, re-verified).

## 6. Note on the round-3 record

`docs/OWNER_DECISIONS_2026-09-17.md` §4 states that the smiley/paperclip/microphone glyphs "already
render as thin lines, so no upstream vector asset was redrawn". The second clause is literally true
(git history shows those assets untouched by Cybergram work), but the first clause is **not
established**: the paperclip and the header back/overflow drawables are raster `.webp`, not vectors,
the smiley and microphone are Lottie compositions of different sizes, and the enabled microphone
glyph is tinted white rather than the ruled pale blue. This imprecision is what R4-3/R4-4 exist to
resolve; no claim in the round-3 record is retracted here.
