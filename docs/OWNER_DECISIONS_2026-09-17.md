# Owner decision log — 2026-09-17 (round 3: chat-surface refinement)

Status: `AUTHORITATIVE OWNER RULINGS — RECORDED`. This file records the binding owner design
refinement issued on 2026-09-17 for the chat surface (header, message area, composer). It extends the
round-2 log (`docs/OWNER_DECISIONS_2026-09-15.md`) with repository ids **D9.x** so it does not collide
with the already-landed `D1`–`D8`.

Base at recording time: `dev` HEAD `edb9354ed`, working tree clean, no stash (verified in this
session). This record performs no push.

## 0. Owner instruction, verbatim (2026-09-17, RU)

> 1. Добавь тонкие красные горизонтальные разделители между шапкой, областью сообщений и нижней
>    панелью композера.
>
> Пузыри сообщений
> 2. Сделай фон входящих сообщений практически чёрным, с лёгким оливковым оттенком.
> 3. Сделай фон исходящих сообщений тёмным сине-зелёным, без яркой бирюзовой заливки.
> 4. Сохрани тонкую жёлтую обводку входящих сообщений и голубую обводку исходящих.
> 5. Уменьши толщину и свечение обводок, сохрани неоновый эффект.
> 6. Приведи форму пузырей к геометрии концепта: скошенные углы, небольшие угловые выступы,
>    чёткие прямые грани.
> 7. Увеличь внутренние отступы пузырей.
> 8. Сделай радиусы и срезы углов единообразными для всех сообщений.
> 9. Сохрани выравнивание входящих сообщений по левому краю, исходящих — по правому.
>
> Текст и метаданные
> 10. Используй светло-серый текст для входящих сообщений и бледно-голубой для исходящих.
> 11. Сделай время сообщений мельче основного текста и менее контрастным.
> 12. Перенеси галочки доставки ближе к времени сообщения.
> 13. Приведи интервалы между сообщениями к заметным промежуткам между отдельными репликами.
>
> Шапка чата
> 14. Сделай фон шапки однородным, почти чёрным.
> 15. Уменьши визуальный вес аватара, имени и статуса. Используй более компактную композицию.
> 16. Приведи иконки к единому тонкому линейному стилю бледно-голубого цвета, слабонасыщенно.
> 17. Убери лишние яркие акценты и декоративные элементы вокруг аватара.
> 18. Добавь тонкую красную линию под шапкой.
>
> Нижняя панель ввода (композер)
> 19. Сделай поле ввода тёмным, практически сливающимся с фоном.
> 20. Замени яркую бирюзовую заливку кнопки микрофона на тёмный фон с тонкой голубой обводкой.
> 21. Приведи иконки смайлика, скрепки и микрофона к единому тонкому линейному стилю.
> 22. Уменьши высоту панели ввода и толщину её обводки.
>
> Общая цветовая схема
> 23. Активно используй красный для разделителей, технической разметки и небольших декоративных
>     акцентов.
> 24. Уменьши общую насыщенность интерфейса. Сохрани яркие акценты только на тонких линиях и
>     отдельных элементах управления.

Follow-up owner instruction, same date (round 3b):

> Выполни задачи по внутренним отступам пузырей, сдвиг галочек, компактность шапки, высота
> композера. Соверши коммит - у тебя есть на это право. Выровняй высоту рамки значка микрофона
> относительно высоты рамки композера. Добавь красным линиям эффект неона.

## 1. Rulings

### D9.1 — red horizontal separators (items 1, 18, 23)

Thin red rules are required at both structural boundaries of the message area:

- **header / message list** — the existing red header rail (`CybergramHeaderDecorationView`, `DANGER`
  at `HEADER_RULE_ALPHA`) is kept and remains a *thin* 1 dp rule; it must not become a glow or a
  filled bar;
- **message list / composer** — a new thin red rule is added at the composer's top edge through the
  existing Cybergram composer seam (no upstream geometry change, no new view in the hierarchy);
- red is promoted to the structural/technical accent: separators, technical markup and small
  decorative accents. Red is never a large fill.

Rationale: the reference already frames the message canvas in red; the round-3 brief makes that the
explicit separator language.

### D9.2 — bubble surfaces (items 2, 3, 24)

| Role | Landed (round 2) | Ruled (round 3) |
|---|---|---|
| incoming bubble | `#2B220D` | near-black with a light **olive** cast |
| incoming selected | `#43330B` | slightly lifted olive-black |
| outgoing bubble | `#07252C` | dark **blue-green**, no bright teal fill |
| outgoing selected | `#063A44` | slightly lifted blue-green |

The bright turquoise *fill* is removed everywhere in the message surfaces: accent colour identifies
direction through the outline and the metadata, not through an opaque saturated field
(`docs/CYBERGRAM_UI_SPEC.md`: "accent colour identifies direction through outline … rather than an
opaque bright fill"). Overall interface saturation is reduced (D9.8).

### D9.3 — bubble outlines (items 4, 5)

Keep the thin **yellow** outline for incoming and the thin **blue** outline for outgoing. Reduce the
stroke width and any glow. The neon read must survive on the thin line alone. Consequence: the shared
`BUBBLE_BORDER_WIDTH_DP` is reduced; the composer-frame stroke introduced by round-2 `D7`
(`dp(1) → dp(1.5)`) is **superseded** and returns to the thin shared value (items 5 + 22). `D7`'s
intent (a legible frame, not a rounded capsule) is preserved; only its thickness is revised.

### D9.4 — bubble geometry (items 6, 7, 8, 9)

- Corner language: crisp 45-degree chamfers on three corners of every bubble, plus a **small angular
  corner protrusion ("выступ")** — a short triangular spur on the outer top corner of the speaking
  side (left for incoming, right for outgoing), exactly as in
  `design/references/design-target-hex-chat.jpg`. It is drawn from the single shared Cybergram polygon
  (`CybergramBubbleDrawable`) — no second geometry implementation (repository hard boundary). The spur
  is limited to the reserved tail region and is suppressed where a bubble is joined to a same-run
  neighbour above it.
- One chamfer value is used for all four corners of every ungrouped message; the reduced near-corner
  cut is retained **only** where a bubble is genuinely joined to a same-run neighbour, which is
  required for clean grouping (`D6` contract). No per-side or per-state radius variation otherwise.
- Inner padding is increased. This is a *layout-affecting* change and therefore must be done as a
  bounded pass that moves the text bounds and the time/check cluster together; it is **not** applied
  blind as a paint-only inset (the `D6` ceiling note in `docs/passes/D6_BUBBLE_SPACING.md` applies).
- Alignment is unchanged: incoming left, outgoing right.

### D9.5 — text and metadata (items 10, 11, 12, 13)

- Incoming body text: light **grey**; outgoing body text: pale **blue** (desaturated, not neon).
- Message time is smaller and lower-contrast than the body text, on both sides.
- Delivery checks move closer to the time cluster.
- The visible gap between distinct replies becomes a clear gap (extends `D6`, including the
  non-Cybergram-control obligation recorded for `D6`).

Time/check placement and body-text sizing are layout/measurement changes and must ship with build and
device evidence; they are contracted, not applied blind.

### D9.6 — chat header (items 14, 15, 16, 17, 18)

- Header background is a single, homogeneous, almost-black surface.
- Avatar, name and status lose visual weight; the identity block becomes more compact.
- Header icons use one thin, linear, low-saturation pale-blue style (search, back, overflow).
- Bright accents and decorative elements around the avatar are removed. This explicitly supersedes the
  round-2 `D8` decision to keep the short cyan identity segment under the header: the segment is
  removed.
- The thin red rule under the header is kept (D9.1).

### D9.7 — composer (items 19, 20, 21, 22)

- The input field is dark and nearly merges with the app background.
- The microphone/voice control is a dark surface with a thin blue outline, not a bright teal fill.
  The landed Cybergram send/voice plate already draws a dark plate with a cyan outline for the idle
  state; the ruling extends that treatment to the voice/recording state instead of falling back to
  the rounded bright fill.
- The smiley, paperclip and microphone glyphs use one thin linear style.
- The panel height and its outline are reduced.
- (round 3b) The microphone plate fills its container so its frame height matches the composer field
  height.

Panel height and the icon glyph redraws touch upstream layout/vector assets and must ship with build
and device evidence.

### D9.8 — overall palette discipline (items 23, 24)

Reduce global saturation. Saturated colour survives only on thin lines and discrete controls
(outlines, checks, cursor, active states). Red becomes the structural/technical accent (D9.1). The
incoming/outgoing side semantics (peer / self) from `design/DESIGN_TARGET.md` are unchanged.

(round 3b) The red structural rails carry a soft **neon bloom**: a low-alpha red band behind the thin
bright core. The header bloom rises into the header and the composer bloom falls into the composer, so
neither ever covers message text.

## 2. Relationship to earlier rulings

| Earlier ruling | Round-3 effect |
|---|---|
| `D1` keep the chamfer | unchanged; the corner **cut** language is refined (D9.4), the angular silhouette stays |
| `D2` palette hues | incoming/outgoing **surfaces** are refined by D9.2; role semantics unchanged |
| `D6` bubble spacing | confirmed and extended (D9.5); the non-Cybergram control obligation still stands |
| `D7` composer outline (1.5 dp) | **superseded** on thickness (D9.3/D9.7); intent preserved |
| `D8` keep the cyan identity segment | **superseded**: removed (D9.6) |
| owner `флажки` (unresolved) | **resolved as check marks** by item 12: the checks are kept, only moved closer to the time; no literal flag decoration is added |

## 3. Scope

In scope: `CybergramTheme` tokens, `cybergram.attheme`, `CybergramBubbleDrawable` polygon,
`MessageDrawable` Cybergram seam, `CybergramHeaderDecorationView`, the Cybergram composer/send seams in
`ChatActivityEnterView`, `ChatMessageCell` Cybergram layout padding, `ChatAvatarContainer` Cybergram
identity sizing, plus the authority docs. Out of scope: MTProto, storage, encryption, networking,
notifications, non-Cybergram presentation.

## 4. Implementation record

Applied in the working tree in this round (constants, the shared polygon, the two structural
rules and the authority docs — no upstream layout/measurement change):

- `CybergramTheme` — refined incoming/outgoing surfaces, incoming/outgoing body text, time/muted
  metadata, reduced `BUBBLE_BORDER_WIDTH_DP`, unified corner cut, larger inter-bubble gap, new red
  separator/alpha tokens;
- `cybergram.attheme` — matching `chat_inBubble*`, `chat_outBubble*`, `chat_messageTextIn/Out`, time
  and check colours, `divider`, composer field/icon colours;
- `CybergramBubbleDrawable` — a tailed-corner overload of the shared polygon (the concept's small
  triangular corner spur) while the plain-chamfer overload keeps every existing caller
  byte-compatible;
- `MessageDrawable` — the Cybergram message path uses the tailed polygon, the reduced border width
  and the enlarged gap;
- `CybergramHeaderDecorationView` — the bright cyan identity segment is removed; the thin red rule
  stays;
- `ChatActivityEnterView` — the composer frame returns to the thin shared stroke and a thin red rule
  is drawn at the composer's top edge. The microphone/voice control is a separate
  `audioVideoButtonContainer`, not the send button: device verification found it still drawing a solid
  `chat_messagePanelSend` circle, so under Cybergram it now draws a dark angular plate with a thin blue
  outline (D9.7). The non-Cybergram branch keeps the upstream rounded circle.

Contracted at first and implemented in the follow-up round (§6): bubble inner padding (D9.4),
time/check placement (D9.5), header identity compaction (D9.6), composer height and microphone frame
(D9.7), and the red neon rails (D9.8). The smiley/paperclip/microphone glyphs already render as thin
lines, so no upstream vector asset was redrawn.

## 5. Validation boundary

**Machine build evidence — PASSED.** `:TMessagesProj:compileDebugJavaWithJavac` (Gradle wrapper
`gradle/wrapper/gradle-wrapper.jar`, Gradle 8.11.1, AGP 8.10.1, JDK 17 at
`C:/Users/mopsy/AppData/Local/Android/toolchain/jdk-17.0.20.1+1`, `--offline`) reported
`BUILD SUCCESSFUL in 4m 33s` with the round-3 edits in the tree. This proves the edited Java
**compiles**; the APK/device sections below add runtime and visual evidence for the landed items,
while the non-Cybergram control path remains unproven.

**APK build — PASSED.** `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline` →
`BUILD SUCCESSFUL in 4m 31s`. APK `74,289,071` bytes, SHA-256
`2E0C9AC1D679A3832D667A830905B6A7F6ECD01759749E45AB2119E13023AC7F`.

**Device verification — PASSED on `emulator-5554` (AVD `Cybergram_API36`, x86_64, authenticated
account).** `adb install -r` → `Success`; launch → no FATAL/ANR; crash buffer empty. Verified against
captures and pixel probes in `.local-artifacts/run-d9-20260917/`: incoming fill `#0B0C07` with
`#FFB300` outline, outgoing fill `#0A1B1D` with `#5FB8C9` outline, the corner spur on the outer top
corner of both directions through the real production `MessageDrawable`, near-black header with the
thin red rule, thin red composer separator, dark composer field with thin outline and thin line icons,
and the microphone as a dark plate with a thin blue outline. This is E-tier evidence for the landed
items; it is not an A-tier matrix and not P. Only `Saved Messages` was opened — no third-party
conversation.

**Still not covered:** the non-Cybergram control screenshot and the P (physical-device) tier.

**Commit.** The owner granted explicit commit rights for this round; the work is committed on
`cybergram` `dev` with no push. See §6.

## 6. Follow-up implementation (round 3b, same date)

The owner then asked for the contracted layout items plus three additions, with commit rights. All
were implemented and re-verified:

- **Bubble inner padding (D9.4)** — Cybergram text bubbles gain vertical padding in `ChatMessageCell`
  (`totalHeight` + `2 * BUBBLE_INNER_PAD_DP`, `textY` + `BUBBLE_INNER_PAD_DP`) and a symmetric
  horizontal inset through `getExtraTextX()` (it feeds `backgroundWidth` 2x and `textX` 1x). The
  measured height grows with the bubble, so the text stays clear of the outline on all four sides.
- **Checks closer to time (D9.5)** — the outgoing check / half-check bounds shift by
  `CHECK_INSET_DP` (3 dp) toward the time; no overlap.
- **Compact header (D9.6)** — Cybergram-gated avatar `HEADER_AVATAR_DP` (36 dp; upstream 42) and
  smaller title/subtitle (16/12 vs 18/14).
- **Shorter composer (D9.7)** — `ChatActivityEnterView.getComposerHeight()` (40 dp under Cybergram,
  upstream 44) drives the input field and its primary control frames.
- **Microphone frame aligned to the composer frame** (owner addition, D9.7) — the microphone plate
  fills its container, so its frame height equals the composer field height exactly.
- **Neon red rails** (owner addition, D9.8) — the header rail and the composer separator each draw a
  soft red bloom (`RED_GLOW_RADIUS_DP`, `HEADER_GLOW_ALPHA` / `COMPOSER_GLOW_ALPHA`) behind the thin
  bright core. The header bloom rises into the header and the composer bloom falls into the composer,
  so neither ever covers message text.

Every change is gated on `CybergramTheme.isCybergramPresentation(...)`; the non-Cybergram paths keep
the upstream values (44 dp composer, 42 dp avatar, 18/14 sp title, unshifted checks, rounded
microphone circle).

**Re-verified on `emulator-5554`** (AVD `Cybergram_API36`, authenticated): build + install + launch
with an empty crash buffer, plus captures in `.local-artifacts/run-d9-20260917/round3b-*.png` showing
the extra bubble padding, the tightened time/check cluster, the compact header, the shorter composer
with the microphone frame level with the field frame, and the red neon rails.

**Committed** on `dev` at the owner's explicit direction (this record performs no push).
