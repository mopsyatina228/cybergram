# Cybergram UI specification

Status: evolving design baseline

Upstream base: `DrKLO/Telegram` 12.10.1 (7038), commit `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`

## Goal

Build a working Android Telegram client whose interaction model remains recognizably Telegram while its presentation uses an original cyberpunk/HUD visual system.

The primary visual reference is the generated chat concept discussed at project start: a direct Android screenshot, Telegram-like structure, dark technical background, warm/amber incoming accents, cyan outgoing accents, angular panel geometry, thin structural rails and restrained HUD details.

This is not intended to reproduce Cyberpunk 2077 assets pixel-for-pixel. Do not copy game logos, fonts, textures, icons or proprietary screen compositions. Recreate the design language with original project assets.

## Product principles

Telegram behaviour wins over decoration. Every screen must remain readable and usable at normal phone scale.

Cyber styling should be structural rather than noisy. Strong silhouettes, angular cuts, thin borders, high-contrast status colours and sparse technical decoration are preferred over covering the screen with meaningless microtext.

Large rounded/glass Material capsules are an anti-target inside Cybergram-specific presentation. Existing Telegram touch targets and state machines should be preserved while their visual backgrounds are flattened, darkened or replaced by angular Cybergram surfaces through narrow theme-specific seams.

Animation should be short and functional. Glitch effects may appear on transitions or state changes, but should never interfere with typing, scrolling or message reading.

Accessibility and dynamic content remain important. Message length, localization, large fonts, media, reactions and reply blocks must survive the new geometry.

## Base palette

The palette is deliberately small so it can be revised centrally.

- App/background black: `#080A0F`
- Header/panel black: `#0B0D12`
- Raised panel: `#111820`
- Primary cyan: `#00E5FF`
- Secondary cyan: `#33D6FF`
- Primary amber/yellow: `#FFB300`
- Highlight amber/yellow: `#FFC94D`
- Warning/red accent: `#FF003C`
- Main light text: `#E6F7FF`
- Muted text: `#6B7A8A`
- Incoming bubble surface: `#2B220D`
- Incoming selected surface: `#43330B`
- Outgoing bubble surface: `#07252C`
- Outgoing selected surface: `#063A44`
- Outgoing light text: `#D7FCFF`
- Hint/placeholder: `#5F6E7C`

Amended 2026-09-16: the amber, red, light-text, muted and hint values above were ruled by the owner
on 2026-09-15 ("палитру — подгоняем под референс", decision D2 in
`docs/OWNER_DECISIONS_2026-09-15.md`) and are already implemented in `CybergramTheme` and
`cybergram.attheme`; the incoming surfaces are the stable opaque composites recomputed from the ruled
amber over `#080A0F`. Cyan, background, panel and raised values are unchanged because the reference
agrees with them.

The incoming/outgoing surface colours above are stable opaque composites chosen to reproduce a restrained translucent tint over the Cybergram background without relying on Telegram preserving per-bubble colour alpha through every drawable path.

The checked-in `TMessagesProj/src/main/assets/cybergram.attheme` is the executable approximation of this palette.

Amended 2026-09-17 (owner round 3, `docs/OWNER_DECISIONS_2026-09-17.md` §1 D9.2/D9.5/D9.8): the
message surfaces were refined away from the warm/cyan *tint* toward near-black surfaces. Incoming is
now a near-black olive; outgoing is a dark blue-green. The bright turquoise fill is gone on both
sides. Body text is light grey on incoming and pale blue on outgoing; time/metadata is smaller and
dimmer than the body text. Saturated colour survives only on thin outlines, checks, the cursor and
discrete controls. Red is promoted to the structural/technical accent (separators, technical markup,
small decorative accents).

## Chat screen target

The screen remains a conventional Telegram chat structurally: Android status bar, back affordance, avatar/contact identity, status, search/menu actions, message history and composer.

The header becomes a dark technical panel with cyan foreground elements and a restrained thin red structural/state rail. Large light glass pills behind identity/call/menu controls do not belong to the Cybergram target. Optional HUD marks may occupy unused edge space, but must remain sparse.

Incoming text messages use a dark warm/amber-tinted panel with a thin amber outline and light text. Outgoing messages use a dark cyan-tinted panel with a thin cyan outline and bright cyan/light text. Accent colour identifies direction through outline, metadata and subtle surface tint rather than an opaque bright fill.

Selected/pressed states increase surface contrast without changing semantic side colours.

Delivery checks and outgoing metadata use cyan. Incoming timestamps/views use muted warm-light values; incoming links use cyan, while forwarded/reply/site semantic accents may use amber.

Date separators and service messages use compact dark plates with amber/cyan accents rather than Telegram's translucent default service bubbles.

The composer is a dark angular panel with cyan cursor/icons and a cyan-framed send state. Rounded/glass Material backgrounds should not reappear in Cybergram-specific composer presentation. Recording/destructive states may use the red accent.

Amended 2026-09-17 (owner round 3, D9.1/D9.6/D9.7): thin red rules separate the header, the message
area and the composer. The header background is a single almost-black surface; avatar, name and
status lose visual weight in a more compact block; header icons are one thin linear low-saturation
pale-blue style; bright accents and decoration around the avatar are removed (this supersedes the
short cyan identity segment kept by D8). The composer field is dark and nearly merges with the app
background; the microphone/voice control is a dark surface with a thin blue outline, never a bright
teal fill (the state also covers recording); the smiley, paperclip and microphone share one thin
linear style; panel height and outline are reduced. The Cybergram composer height is
`COMPOSER_HEIGHT_DP` (40 dp) and the microphone plate fills its container, so its frame height equals
the composer field height. The chat identity block uses `HEADER_AVATAR_DP` (36 dp) and the smaller
title/subtitle sizes. The red header rail and composer separator carry a soft neon bloom that stays on
the chrome side of the boundary and never covers message text.

Amended 2026-09-17 (owner round 4, D10.1–D10.3): the 24 dp microphone glyph is centred in its plate
frame (the plate fills the 40 dp container per D9.7; the glyph view is centred inside it, so the glyph
centre coincides with the frame centre up to the 0.5 px density-rounding floor). The enabled
microphone — and the video-mode camera, which shares the same view — uses the same low-saturation
pale-blue key as the smiley and paperclip (`key_glass_defaultIcon` → `chat_messagePanelIcons`, i.e.
`CybergramTheme.ICON_PALE` `#8FBFCC`) instead of the upstream white; outside Cybergram all of this is
a provable no-op. Items 16/21 are accepted as **PARTIAL**: colour and thin-stroke intent are met and
the microphone now shares the smiley/paperclip colour, but the header back/overflow glyphs and the
paperclip remain upstream raster assets and the search glyph is a filled-path vector, so asset
provenance is deliberately not unified (a Cybergram-only icon redraw is not authorized). The clear
gap between distinct replies uses `BUBBLE_GAP_EXTRA_DP` (2.5 dp, defensively clamped by
`BUBBLE_GAP_MAX_DP` in `MessageDrawable`) so the outline keeps a clear margin at the binding bottom
edge (R-D6 resolved).

## Message geometry

Cybergram replaces rounded Telegram message silhouettes with an angular drawable. Desired properties:

- clipped/angled corners rather than uniform radii;
- subtle 1 px-ish cyan outline for outgoing messages;
- subtle 1 px-ish amber outline for incoming messages;
- Telegram tail geometry removed or reduced;
- media messages must preserve clipping and touch targets;
- grouped messages must still join cleanly;
- selection/pressed overlays must follow the new path;
- shadows should be minimal or absent.

Amended 2026-09-17 (owner round 3, D9.3/D9.4): the outline uses the reduced shared stroke. Three
corners of every bubble keep the crisp 45-degree chamfer; the outer top corner of the speaking side
carries the concept's small angular protrusion ("выступ") — a short triangular spur, left for
incoming and right for outgoing. It is produced by the single shared
`CybergramBubbleDrawable.buildTailedPath(...)` polygon (the plain-chamfer overload is retained for
non-message callers) and stays inside the reserved tail region. One chamfer value is used on every
corner; the reduced near-corner cut, and the spur, are suppressed where a bubble is genuinely joined
to a same-run neighbour. Inner padding is implemented: `ChatMessageCell` grows the measured text
bubble by `2 * BUBBLE_INNER_PAD_DP` and shifts `textY` by `BUBBLE_INNER_PAD_DP`, while
`getExtraTextX()` adds a symmetric `BUBBLE_TEXT_INSET_DP` horizontal inset, so the text keeps clear of
the outline and the layout moves with it. Outgoing delivery checks sit `CHECK_INSET_DP` closer to the
time.

`MessageDrawable.java` is the primary rendering surface. `ChatMessageCell.java` must be validated for grouped messages, media and message metadata before bubble bounds are changed.

## Dialog list target

Keep Telegram's information hierarchy: avatar, title, message preview, date/status and unread state.

Use a dark list background. Selected/pinned rows should use a raised dark panel rather than a light overlay. Names use light text; unread/active state is cyan; destructive/error state is red; optional amber can indicate special or system states.

Avoid adding decorative separators that reduce visible conversation density. Repeated HUD accents should remain subtle enough that the list does not become a visual picket fence.

Dialogs top filters and main bottom navigation should eventually use the same dark/angular structural language. Large rounded/glass pills are transitional upstream chrome, not the final Cybergram target.

## Typography

Do not bundle or rip proprietary game fonts.

Message body text stays on Telegram's typography for now; it must remain highly readable.

Primary chrome (chat header identity and status, dialogs row titles and timestamps, dialog filter
tabs, the dialogs search field) uses the Android system family `sans-serif-condensed` through one
central Cybergram helper, to give the chrome a narrower and more technical voice without turning the
client into a decorative display-font terminal:

- the family is a system family — no asset is bundled, nothing proprietary is referenced;
- sizes, layout and geometry are unchanged in this pass: only the typeface is isolated;
- no aggressive letter spacing;
- the choice is resolved through the normal Cybergram gate and always falls back to the upstream
  typeface for Day/stock presentation;
- a redistributable open font may still be evaluated later for headings or compact HUD labels, and
  message body text should remain on a highly readable family.

Amended 2026-09-16 (owner ruling D5, `docs/OWNER_DECISIONS_2026-09-15.md` §2 D5): Cyrillic is a
**first-class script, not a fallback**, in both the interface layer and the monospace HUD service
layer. `sans-serif-condensed` was resolved on the target device and its backing
`/system/fonts/Roboto-Regular.ttf` was measured to carry **256/256 Cyrillic U+0400-04FF, 48/48
Cyrillic Supplement and 32/32 Cyrillic Ext-A** — the broadest coverage of any candidate in the tree
— so the landed chrome already satisfies the ruling and nothing is bundled. The reference's own
`Rajdhani` and `Share Tech Mono` are **disqualified**: neither ships Cyrillic. A geometric/OFL face
(e.g. `IBM Plex Sans Condensed`, `Inter`) may replace the chrome family later, but only as an
owner-supplied OFL asset with an APK-size and licence/NOTICE note; measurement and method are
recorded in `docs/D5_TYPOGRAPHY_2026-09-16.md`.

## HUD decoration

Decorative lines, corner marks, IDs and micro-labels must be drawn by a dedicated Cybergram layer or reusable component rather than hard-coded independently into screens.

Thin cyan rails represent neutral/interactive structure. Thin red rails may be used for strong framing/state emphasis. Amber is primarily semantic/identity/attention colour. None of these accents should become large opaque surfaces without a functional reason.

Do not add fake security claims such as `SECURE CHAT` or `END-TO-END` to ordinary chats. Decorative labels must not misrepresent Telegram's actual security properties.

Amended 2026-09-16 (owner ruling D3, `docs/OWNER_DECISIONS_2026-09-15.md` §2 D3): the *copy* policy
for that layer is now explicit. **Allowed:** original, non-deceptive technical inscriptions in Latin
script, uppercase, monospace/HUD style — explicitly including deliberately meaningless technical
"abracadabra" (e.g. `CH 1.0.3.7`-class channel/index/frequency-shaped strings). Labels stay
decorative, edge-anchored, non-interactive and never over text. **Still banned, hard:** `SECURE CHAT`,
`END-TO-END`, lock/security iconography, invented IDs or status that misrepresent Telegram, any live
network/connection claim (including `CONNECTION STABLE` and its signal bars), third-party
brands/slogans, and any text drawn over message content. The paragraph above is unchanged by this
amendment.

### Chat default backdrop

Amended 2026-09-16 (owner ruling D4, `docs/OWNER_DECISIONS_2026-09-15.md` §2 D4): a **default,
replaceable** Cybergram background layer is allowed and expected — a faint technical grid (the same
cyan family, ~0.04 alpha) plus *minimal* thin edge framing. Hard constraints: a user's own wallpaper
always overrides it; it must never crop, tint away, re-scale or obscure a user wallpaper; it must not
intercept touch; and it never sits over message text. It is not a wallpaper shipped through the theme
system, so it is never persisted into a user-saved theme and never uploaded to an account — see
`docs/passes/D4_DEFAULT_BACKDROP.md`. The chat-canvas *label* layer remains
`docs/passes/B7_CHAT_CANVAS_HUD.md`, which is still `NOT AUTHORIZED`.

### Inter-bubble spacing

Amended 2026-09-16 (owner ruling D6, `docs/OWNER_DECISIONS_2026-09-15.md` §2 D6): distinct
(unjoined) text bubbles carry a small extra vertical inset so that the clear space between separate
messages moves toward the reference's ~12 px gap. The inset is paint-only inside
`MessageDrawable.generateCybergramPath(...)`: message measurement, layout, scroll and the
time/check cluster are unchanged, and edges joined to a neighbour of the same run keep the upstream
inset so grouped bubbles still join cleanly. `TYPE_MEDIA` is excluded because its image position is
independent of the drawable bounds. Contract and ceiling: `docs/passes/D6_BUBBLE_SPACING.md`.

Amended 2026-09-17 (owner round 3, D9.5): the inter-bubble inset was increased further so distinct
replies read with a clearly noticeable gap. The paint-only, join-aware `D6` mechanism and its ceiling
are unchanged.

## Implementation stages

### Stage A: build baseline

Build the untouched fork locally with the upstream toolchain and project-owned development credentials. Record the exact command, APK variant and result.

### Stage B: palette prototype

Load/test `cybergram.attheme` and tune the core chat/list/action-bar colours.

### Stage C: chat geometry

Introduce Cybergram-specific message drawable/path code with the smallest possible seam around upstream `MessageDrawable` behaviour. Validate text, media, replies, reactions, grouped messages, forwards and selection.

### Stage D: composer and header

Restyle `ChatActivityEnterView` and the chat action bar while preserving all existing input modes. Header work includes removing or suppressing upstream glass/rounded presentation through Cybergram-only seams while preserving the original controls and hit targets.

### Stage E: dialog list

Apply the same design system to the main conversation list, search and row states. Filters and main tabs should be normalized after ownership is established rather than globally restyled.

### Stage F: rest of client

Extend the system to settings, profiles, media viewers, calls and secondary surfaces only after the primary messaging flow is coherent.

## Initial component map

High-value files found in the upstream tree:

- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/Theme.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/ThemeColors.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatMessageCell.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/ChatActivityEnterView.java`

The existing theme system exposes separate keys for incoming/outgoing bubble colours, text, link colours, checks, timestamps, panel colours and many related states. Use those keys before introducing new global styling hooks.

## Non-goals for the first iteration

Do not rewrite Telegram networking or MTProto.

Do not change message/database formats.

Do not change encryption semantics.

Do not add analytics, advertising or telemetry.

Do not rename every Java package before a clean baseline build exists.

Do not make broad shared-component restyles when a Cybergram-specific opt-in seam can preserve upstream behaviour.
