# D4 — Cybergram default replaceable backdrop (owner ruling 2026-09-15)

Status: `IMPLEMENTED / E BUILD+RUNTIME PASS / A PENDING`

Type: bounded implementation pass derived directly from an owner ruling
(`docs/OWNER_DECISIONS_2026-09-15.md` §2 D4 — the owner's own label `D3`: "это «обои по умолчанию»,
которые могут быть заменены. Но минимальное обрамление необходимо").

Production scope: one new file
(`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramBackdropDrawable.java`) plus a
wiring change inside the existing `ChatActivity.ChatActivityFragmentView.getNewDrawable()` override.
No `Theme.java`, no `SizeNotifierFrameLayout`, no new child View and no draw-order change.

## Mission

Ship a Cybergram **default** background layer — a very faint technical grid plus minimal thin edge
framing — that reads as "обои по умолчанию", is replaceable by any user wallpaper, and never
competes with message text.

## Ruling constraints (hard)

From `docs/OWNER_DECISIONS_2026-09-15.md` §2 D4:

- the user's own wallpaper choice must **always** override it;
- it must **not** crop, tint away, re-scale or obscure a user wallpaper;
- it must **not** intercept touch;
- it must not violate the existing B7 rule that decoration never sits over message text.

## Why the texture is not shipped through the theme system

Reconnaissance (2026-09-16, this pass) established that Telegram's theme system cannot express a
"default drawn backdrop" for a built-in asset theme:

- `applyTheme` passes `wallpaperLink = null` whenever `assetName != null` (`Theme.java:5676-5677`,
  `:5897-5898`), so a `WLS=` line inside `cybergram.attheme` is silently dropped;
- the asset `WPS` raster branch is the trailing `else if` in `createBackgroundDrawable`
  (`Theme.java:9534`) and is unreachable while the theme sets `key_chat_wallpaper`
  (`cybergram.attheme` sets it to `#080A0F`), and a `WPS` blob is copied into any theme the user
  later re-saves (`Theme.java:6844-6850`) and breaks `tools/validate_cybergram_theme.py`;
- setting `themeInfo.slug` would make `MessagesController.installTheme` (`:9084-9095`) upload the
  wallpaper to the user's account — explicitly excluded;
- a seam in `Theme.java` or `SizeNotifierFrameLayout.java` would leak into every screen (`Theme`
  wallpaper consumers) or into ~20 `SizeNotifierFrameLayout` host classes.

## Seam chosen

A `ColorDrawable` subclass returned from the existing
`ChatActivity.ChatActivityFragmentView.getNewDrawable()` override
(`ChatActivity.java:18677`), which already exists upstream to prefer
`themeDelegate.getWallpaperDrawable()`.

`getNewDrawable()` is the single place `SizeNotifierFrameLayout.BackgroundView.onDraw` consults for
the wallpaper each frame (`SizeNotifierFrameLayout.java:176-187`). Returning a decorated drawable
there means:

- **no child-view insertion**, so no z-order change: `videoPlayerContainer` (hard-coded index 1),
- `emptyViewContainer` (index 3), `topUndoView` (index 17) and the
  `1 + indexOfChild(chatListView)` `thanosEffect` anchor are all untouched. This is the decisive
  advantage over the sibling-View design recorded in `docs/passes/B7_CHAT_CANVAS_HUD.md`;
- the layer is the background, i.e. **below `chatListView`**, so it can never sit over message text;
- it is a `Drawable`, not a `View`, so it can never intercept touch, focus or accessibility;
- nothing is written to preferences and nothing is uploaded: the layer is not a `ThemeInfo`
  wallpaper, is not an `OverrideWallpaperInfo`, and `Theme.saveCurrentTheme` only persists a
  wallpaper when it is a `BitmapDrawable`.

### Eligibility gate — `CybergramBackdropDrawable.isEligible(...)`

All three conditions must hold, otherwise the upstream drawable is returned **verbatim**:

1. `CybergramTheme.isCybergramPresentation(provider)` is true;
2. `Theme.getActiveTheme().overrideWallpaper == null` — the user has no wallpaper of their own, so
   the default is replaceable and a user wallpaper always wins;
3. the source drawable is a plain `ColorDrawable` — i.e. the built-in Cybergram flat background
   colour, not a user photo, motion, gradient or colour-picker wallpaper.

Keeping the object a `ColorDrawable` subclass is deliberate: `BackgroundView.onDraw` branches on the
wallpaper type (`SizeNotifierFrameLayout.java:257-267` calls `setBounds` + `draw`), so the existing
branch is reused unchanged and `AndroidUtilities.calcDrawableColor` keeps reading a real colour.

The decorated instance is cached in the fragment view and keyed by the base colour, so the
`newDrawable != backgroundDrawable` identity check at `SizeNotifierFrameLayout.java:178` cannot
enter a crossfade loop.

## What is drawn

- a **grid** of 1 px hairlines on a `dp(28)` cell, alpha 10/255 (~4 %), built once into an
  `ALPHA_8` tile and applied as a `BitmapShader(REPEAT)` — one `drawRect` per frame, no per-frame
  allocation;
- **minimal framing**: one thin `CybergramTheme.DANGER` hairline rectangle inset `dp(5)` from the
  background bounds at alpha 24/255 (~9 %). No text, no labels, no live data, no security claim —
  `docs/CYBERGRAM_UI_SPEC.md` line 117 and the D3 copy ruling are respected by drawing no copy at all.

## Explicit non-goals

- no text/HUD labels (that layer is B7 and B7 is **not authorized**);
- no `Theme.java`, `SizeNotifierFrameLayout.java`, preference, wallpaper-pipeline or child-index change;
- no change to the user's wallpaper, to motion wallpapers, to `skipBackgroundDrawing` or to either
  blur path;
- no non-Cybergram presentation change.

## Required evidence matrix

| tier | required evidence |
|---|---|
| E | `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` BUILD SUCCESSFUL; install/launch on `Cybergram_API36`; no FATAL/ANR/process death; screenshot of the chat canvas showing the faint grid and edge framing under the message list; message text and media unaffected; scroll/typing regression check; non-Cybergram control proving the layer draws nothing; argument (or measurement) that a user wallpaper suppresses the layer |
| A | authenticated chat with a real wallpaper set by the user, proving the backdrop disappears and the user wallpaper is not cropped, tinted or re-scaled |
| P | not required unless a device/OEM-specific defect appears |

Never promote E evidence into A or P.

## Stop conditions

Stop and report rather than proceeding if the layer cannot be suppressed for a user wallpaper, if it
requires a new child View or any `Theme`/`SizeNotifierFrameLayout` edit, if it becomes captured into
a blur/background pass incorrectly, or if any non-Cybergram path changes.
