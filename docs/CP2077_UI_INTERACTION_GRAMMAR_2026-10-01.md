# CP2077 interaction UI grammar study — 2026-10-01

Source: locally installed Cyberpunk 2077 resources, extracted and serialized with WolvenKit.

## Relevant source resources

- `widgets/interactions/interaction.inkwidget`
- `widgets/interactions/dialog.inkwidget`
- `widgets/interactions/dialogchoicesstyle.inkstyle`
- `widgets/interactions/atlas_dialog_nineslice.inkatlas`
- `widgets/radial_wheel/radial_wheel.inkwidget`
- `widgets/radial_wheel/radial_styles.inkstyle`
- `widgets/quickhacks/quickhacks.inkwidget`
- `widgets/quickhacks/quickhack_style.inkstyle`
- `common/dialogs_popups.inkstyle`
- `common/buttons/base_buttons.inkwidget`
- `widgets/notifications/notification.inkstyle`
- `widgets/notifications/quest_update.inkwidget`
- `widgets/notifications/phone_message_popup.inkwidget`
- `fullscreen/game_notifications/game_notifications.*`

## Findings

Popup menus are built as a darkest plate plus separate low-alpha frame/fluff layers, not a rounded Material card.
Dialog choices use Raj SemiBold; state is communicated through color and frame intensity rather than changing to a generic heavy face.

Radial/quickhack slots are intentionally translucent. Default slots keep weak background/fluff; hover/active raises border and accent intensity.

Quickhack panels layer very low-alpha background, frame and status elements. Glow is not the primary separator.

Notifications use separate plate, stroke/bracket and accent assets. This is the closest source analogue for Cybergram's pinned-message strip.

## Adaptation rules for Cybergram

1. Concept composition has priority over exact in-game silhouettes.
2. Game resources define grammar: cut corners, layered alpha, frame/fluff hierarchy, Raj weights.
3. Telegram defines functional constraints: dynamic media, reactions, menus, pinned messages and rich text.
4. Media content must inherit the same angular clip language as its enclosing bubble.
5. Context menus and reaction rails should read as technical overlays, not rounded Android cards.
6. Pinned messages should use notification/bracket grammar without inventing security or connectivity claims.
7. Bold semantics must remain flag-based even when the actual Typeface is replaced with Raj SemiBold.
