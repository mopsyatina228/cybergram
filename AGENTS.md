# Cybergram repository guidance

This repository is a fork of `DrKLO/Telegram` and should remain easy to rebase/sync with upstream.

## Branch model

- `master`: upstream-aligned baseline. Do not place Cybergram product work directly here.
- `dev`: integration branch for Cybergram changes.
- Feature branches: use `feature/<scope>` or `fix/<scope>` when work is large enough to review independently.

## Prime directive

Change presentation before behaviour. Cybergram is initially a visual fork, not a Telegram protocol rewrite.

Avoid modifying MTProto, account/session handling, database/storage, encryption, networking, media transport, notifications, or business logic unless the task explicitly requires it and the change is independently justified.

Prefer small additive seams around Telegram's existing UI over broad refactors. Upstream mergeability matters.

## UI architecture priorities

The first vertical slice is:

1. dialogs list;
2. chat header;
3. message bubbles and message metadata;
4. composer/input panel;
5. replies, reactions and common media states.

Known high-value upstream surfaces include:

- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/Theme.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/ThemeColors.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatMessageCell.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/ChatActivityEnterView.java`

Do not assume all visual behaviour comes from XML resources. Telegram draws substantial UI directly through custom Views, Canvas and Drawable classes.

## Visual direction

Use `docs/CYBERGRAM_UI_SPEC.md` as the current source of truth.

Do not add copyrighted Cyberpunk 2077/CD Projekt assets, logos, fonts ripped from the game, character art, or branded UI textures. The target is an original cyberpunk/HUD visual language, not a reskin made from proprietary files.

## Security and credentials

Never commit real API IDs/hashes, signing passwords, production keystores, Firebase secrets, personal session material or user data.

Upstream ships dummy/reproducible-build configuration. Keep real local credentials outside version control.

## Build discipline

Before broad UI changes, establish that the unmodified fork builds locally with the upstream toolchain. After code changes, prefer the smallest relevant build/test first, then a complete debug build when practical.

When a build cannot be executed in the current environment, state that explicitly in the commit/PR notes. Do not claim a build passed without machine evidence.

## Upstream sync

Treat `DrKLO/Telegram` as authoritative upstream. Keep Cybergram-specific code clustered and clearly named where possible so upstream updates produce localized conflicts rather than repository-wide archaeology.

Do not remove upstream licence notices. Cybergram remains GPL-licensed under the terms inherited from Telegram's source tree.
