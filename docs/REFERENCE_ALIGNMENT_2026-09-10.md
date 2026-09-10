# Cybergram reference-alignment pass — 2026-09-10

Reference: the project-owned generated chat concept used for the Cybergram visual direction.

This pass is deliberately limited to changes that can be reviewed statically while no Android build/device is available. Runtime/build validation is pending and must be performed before the affected presentation is considered frozen.

## Reference-derived rules now treated as authoritative

- Message surfaces are dark/tinted, not opaque bright slabs. Incoming uses a restrained warm/amber tint with an amber outline; outgoing uses a restrained cyan tint with a cyan outline.
- Message body text remains light on both sides. Accent colour identifies direction through outline/metadata rather than by flooding the whole bubble.
- Chat chrome is a dark technical strip. Large light glass/Material capsules are an anti-target for Cybergram.
- Header actions remain cyan/light on the dark bar. Structural red is used as a thin rail/state line, not as a large fill.
- Composer chrome remains dark and angular with cyan controls; stock rounded/glass treatment should not reappear inside Cybergram-specific presentation.
- Decorative HUD marks must stay sparse and structural. No fake security/network claims.

## Changes in this static pass

- Aligned `CybergramTheme` bubble constants with the current stable tinted theme colours.
- Adjusted the existing header decoration from a full cyan rail + amber segment toward the reference: restrained red structural rail, short cyan identity segment and small edge/tick geometry.
- Corrected incoming special-content colours left over from the old opaque-amber scheme (links, views, via/site labels, contacts, files/audio metadata) so they remain readable on the new dark warm bubble body.
- Brought composer secondary icons and chat subtitle/status accent closer to the reference cyan treatment.
- Changed service/date plate palette toward a compact dark plate with amber text/accent instead of a generic translucent Telegram service bubble.
- Updated `CYBERGRAM_UI_SPEC.md` so it no longer describes opaque amber incoming bubbles as the target.

## Still visibly divergent from the reference

1. Chat header glass capsules around identity and action controls. Ownership is in the existing ActionBar/ChatAvatarContainer glass pipeline. The safest implementation seam is to suppress `ActionBar.setupGlass(...)` and `ChatAvatarContainer.setGlassMode()` only for Cybergram at the ChatActivity call site, leaving upstream/Day behaviour untouched. This needs a normal source edit + build/runtime pass before landing.
2. Dialog filter/folder pill and main bottom-navigation glass pill. Their shared owners are `FilterTabsView` and `MainTabsLayout`/`GlassTabView`; these require isolated Cybergram presentation seams rather than global restyles.
3. Service/date geometry is still Telegram-owned. The palette now matches the reference more closely, but the compact angular outline/plate shape remains a later geometry pass.
4. The reference contains subtle background/HUD rails and sparse red technical marks. Cybergram currently has only header/dialog structural decorations; a dedicated non-interactive chat-canvas HUD layer can be added later after primary chrome is normalized.

## Validation status

No Gradle build, APK install, screenshot or runtime validation was performed for this pass because the development machine/device was not available. Treat all Java/resource presentation changes from this pass as **static-only pending validation**. Do not claim runtime success until the normal single-ABI build and real-device review are completed.
