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
- Primary amber/yellow: `#E8D93A`
- Highlight amber/yellow: `#F2E75B`
- Warning/red accent: `#FF2E46`
- Main light text: `#E6F2F2`
- Muted text: `#7C8A91`
- Incoming bubble surface: `#282715`
- Incoming selected surface: `#3E3C19`
- Outgoing bubble surface: `#07252C`
- Outgoing selected surface: `#063A44`
- Outgoing light text: `#D7FCFF`

The incoming/outgoing surface colours above are stable opaque composites chosen to reproduce a restrained translucent tint over the Cybergram background without relying on Telegram preserving per-bubble colour alpha through every drawable path.

The checked-in `TMessagesProj/src/main/assets/cybergram.attheme` is the executable approximation of this palette.

## Chat screen target

The screen remains a conventional Telegram chat structurally: Android status bar, back affordance, avatar/contact identity, status, search/menu actions, message history and composer.

The header becomes a dark technical panel with cyan foreground elements and a restrained thin red structural/state rail. Large light glass pills behind identity/call/menu controls do not belong to the Cybergram target. Optional HUD marks may occupy unused edge space, but must remain sparse.

Incoming text messages use a dark warm/amber-tinted panel with a thin amber outline and light text. Outgoing messages use a dark cyan-tinted panel with a thin cyan outline and bright cyan/light text. Accent colour identifies direction through outline, metadata and subtle surface tint rather than an opaque bright fill.

Selected/pressed states increase surface contrast without changing semantic side colours.

Delivery checks and outgoing metadata use cyan. Incoming timestamps/views use muted warm-light values; incoming links use cyan, while forwarded/reply/site semantic accents may use amber.

Date separators and service messages use compact dark plates with amber/cyan accents rather than Telegram's translucent default service bubbles.

The composer is a dark angular panel with cyan cursor/icons and a cyan-framed send state. Rounded/glass Material backgrounds should not reappear in Cybergram-specific composer presentation. Recording/destructive states may use the red accent.

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

`MessageDrawable.java` is the primary rendering surface. `ChatMessageCell.java` must be validated for grouped messages, media and message metadata before bubble bounds are changed.

## Dialog list target

Keep Telegram's information hierarchy: avatar, title, message preview, date/status and unread state.

Use a dark list background. Selected/pinned rows should use a raised dark panel rather than a light overlay. Names use light text; unread/active state is cyan; destructive/error state is red; optional amber can indicate special or system states.

Avoid adding decorative separators that reduce visible conversation density. Repeated HUD accents should remain subtle enough that the list does not become a visual picket fence.

Dialogs top filters and main bottom navigation should eventually use the same dark/angular structural language. Large rounded/glass pills are transitional upstream chrome, not the final Cybergram target.

## Typography

Do not bundle or rip proprietary game fonts.

Initial implementation should use Telegram/Android typography while geometry and palette are stabilized. A redistributable open font may be evaluated later for headings or compact HUD labels, but message body text should remain highly readable.

## HUD decoration

Decorative lines, corner marks, IDs and micro-labels must be drawn by a dedicated Cybergram layer or reusable component rather than hard-coded independently into screens.

Thin cyan rails represent neutral/interactive structure. Thin red rails may be used for strong framing/state emphasis. Amber is primarily semantic/identity/attention colour. None of these accents should become large opaque surfaces without a functional reason.

Do not add fake security claims such as `SECURE CHAT` or `END-TO-END` to ordinary chats. Decorative labels must not misrepresent Telegram's actual security properties.

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
