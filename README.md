# Cybergram

Cybergram is an experimental Android Telegram client fork focused on a distinct cyberpunk/HUD visual language while preserving Telegram's existing messaging behaviour and protocol implementation.

The project is based on the official [Telegram for Android](https://github.com/DrKLO/Telegram) source. `master` is kept as the upstream-aligned baseline; active Cybergram work belongs on `dev` and feature branches.

The visual target is deliberately inspired by late-21st-century game HUDs rather than by copying proprietary game assets or branding. The first vertical slice is the chat experience: dialog list -> chat -> message bubbles -> composer -> replies/reactions/media. Network, storage and protocol logic should remain as close to upstream as possible.

See `docs/CYBERGRAM_UI_SPEC.md` for the current UI direction and `AGENTS.md` for repository rules.

## Upstream requirements

This fork inherits Telegram's API and licensing requirements. Before distributing builds:

1. Obtain your own `api_id` from https://core.telegram.org/api/obtaining_api_id.
2. Do not present the app as the official Telegram client.
3. Do not use Telegram's standard logo as this app's logo.
4. Follow Telegram's security guidelines: https://core.telegram.org/mtproto/security_guidelines.
5. Publish the corresponding source code as required by the repository licence.
6. Replace the dummy signing/Firebase material shipped for reproducible upstream builds with project-owned credentials before publishing APKs.

## Build baseline

Current upstream baseline: Telegram Android 12.10.1 (7038), commit `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Required upstream toolchain:

- Android Studio 2025.1.4
- Android NDK 27.2.12479018
- Android SDK 36

Clone the fork with submodules:

```bash
git clone --recursive --shallow-submodules https://github.com/mopsyatina228/cybergram.git
cd cybergram
git checkout dev
```

If submodules were omitted:

```bash
git submodule init
git submodule update --init --recursive --depth=1
```

For local development, add the official repository as `upstream`:

```bash
git remote add upstream https://github.com/DrKLO/Telegram.git
git fetch upstream
```

Before attempting a release build, follow the original Telegram requirements for `release.keystore`, `gradle.properties`, Firebase configuration and `TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java`.

## Project status

The fork has been bootstrapped. The current phase is UI foundation: establish a Cybergram palette/theme asset and map the existing Telegram rendering surfaces before changing message geometry. No protocol or account-data behaviour is intentionally changed at this stage.

## Telegram API and protocol documentation

Telegram API: https://core.telegram.org/api

MTProto: https://core.telegram.org/mtproto

## Licence

Cybergram remains subject to the upstream GNU GPL v2 or later licence and the notices already present in the source tree.
