# CP2077 messenger source study — 2026-10-01

## Design authority

Cybergram should not reproduce the shipped game messenger literally.
Use three layers of authority:

1. Concept screenshot: composition, message semantics, spacing, header/composer and overall mood.
2. Shipped CP2077 resources: design grammar, typography, palette families, panel construction, opacity and cut-corner geometry.
3. Telegram: functional constraints for media, forwards, reactions, accessibility and dynamic text.

The concept wins when it visibly conflicts with the shipped game.

## Primary game resources

Extracted from `basegame_1_engine.archive`:

- `base/gameplay/gui/fullscreen/phone_quest_menu/messenger.inkstyle`
- `base/gameplay/gui/fullscreen/phone_quest_menu/phone_quest_submenu_sms.inkwidget`
- `base/gameplay/gui/widgets/phone/new_phone_assets.inkatlas`
- `base/gameplay/gui/common/main_colors.inkstyle`
- `base/gameplay/gui/common/masks.inkatlas`
- `base/gameplay/gui/common/shapes/atlas_shapes_sync.inkatlas`

Research workspace: `E:/Tools/cp2077-ui-study`.

## Typography

The shipped messenger uses the Raj family, Medium for message text.
Message body size is 50 game units. Internal message content margin is L24/T20/R20/B30.
The asymmetric bottom padding is deliberate and matches the visual need identified in Cybergram.

## Message material

Default game message:

- border: `MainColors.MildBlue`
- background: `MainColors.FaintBlue`
- text: `MainColors.Blue`
- background opacity: 0.35

Quest message:

- border/text: `MainColors.Yellow`
- background: `MainColors.MildYellow`
- background opacity: 0.15

Normal reply material is much weaker: background opacity 0.005, border 0.20, text 0.70.
This supports soft layered material rather than opaque cards and razor-sharp strokes.

## Palette anchors

Approximate clipped 8-bit equivalents of the game's HDR colors:

- Blue: `#5EF6FF`
- MildBlue: `#349197`
- FaintBlue: `#172C2E`
- Yellow: `#FFD741`
- MildYellow: `#A17E33`
- Red: `#FF6159`

These are hue anchors, not direct Cybergram body-text colors. The concept is less saturated and less bright.
Pixel comparison of the concept indicates warm cream around the `#D8C098` family for incoming text and muted cyan around `#A0C0C8`–`#B0D8E0` for outgoing text.

## Bubble geometry

The game does not construct message panels as rounded Android rectangles. It uses tiny angular atlas assets with nine-slice scaling.
At 1080p the message source tile is about 42x44 px.
Nine-slice guides:

- normal bubble: L15/T15/R18/B18
- reply bubble: L15/T15/R18/B20

The source mask has a roughly 10–11 px diagonal corner cut at 1080p. The fill and border are separate assets.
This is useful grammar for media clipping in Cybergram.

Important divergence: shipped-game bubble tails are bottom-left/bottom-right, while the concept uses the upper-side tail language. Do not import the game's silhouette literally. Preserve the concept silhouette and borrow only its cut-corner/nine-slice discipline.

## Attachment ruling

Telegram media currently uses rounded `ImageReceiver` radii inside an angular Cybergram bubble. This is the strongest remaining shape-language conflict.
Cybergram media should use its own inset angular clipping path, mirrored by message direction, while preserving Telegram's spoiler/video/selection overlays.
The inner cut should be smaller than the outer bubble cut so the frame reads as one nested system.

## Practical balance

Copy from the game: Raj Medium/SemiBold, hue families, low-opacity material, asymmetric padding, cut-corner atlas logic, soft layer hierarchy.
Do not copy: full HDR saturation, green Player state semantics, desktop layout scale, exact bottom-tail bubble silhouette, or fake security/connection claims.
