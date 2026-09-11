# Cybergram emulator validation runbook

Purpose: establish a reproducible local build/install/runtime baseline for the current Cybergram `dev` branch without requiring a physical Android phone or an authenticated Telegram account.

This is a machine-validation runbook, not an implementation task. Do not modify product code, refactor, fix unrelated issues, commit, push or merge while executing it.

## Repository preflight

Repository: `mopsyatina228/cybergram`

Read first:

- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- the relevant latest section of `docs/WORK_STATE.md`

Inspect remotes, current branch, `git status` and current SHA.

Do not destroy or overwrite local work. If the worktree is dirty, stop and report all modified/untracked files. Do not use `checkout`, `reset`, `clean` or any equivalent destructive recovery.

If the worktree is safe, run `git fetch` and update local `dev` to current `origin/dev` using fast-forward only.

The repository state may have advanced since this runbook was written. Treat fresh `origin/dev` as authoritative. Record the exact SHA used for validation.

## Emulator target

Physical device validation is not required for this run.

Use Android Emulator with the following preferred target:

- Android 16 / API 36
- ABI: `x86_64`
- Google APIs x86_64 system image when available
- preferred AVD name: `Cybergram_API36`

First inspect the installed Android SDK and available `sdkmanager`, `avdmanager`, `emulator` and existing AVDs.

If a suitable API 36 x86_64 AVD already exists, reuse it. Do not create duplicate AVDs merely to match the preferred name.

If the required system image is missing, it may be installed through the normal Android SDK tooling and an AVD may then be created with `avdmanager`. Do not modify Cybergram source code to accommodate the emulator.

Start the AVD and wait for completed Android boot. Verify through `adb` that the emulator is connected and report its Android API level and primary ABI. The expected target is API 36 / `x86_64`.

## Build

Cybergram explicitly supports single-ABI developer builds. For the emulator use `x86_64`, not the historical physical-device `arm64-v8a` target.

On Windows run:

```text
gradlew.bat :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64
```

On a Unix-like host use the equivalent `./gradlew` command.

Use the existing local Cybergram Telegram API credentials from the gitignored `local.properties`. Never print, copy into reports, commit or otherwise expose `CYBERGRAM_API_ID` or `CYBERGRAM_API_HASH`.

If the build fails, do not modify source code. Record:

- exact Git SHA;
- exact build command;
- first causal error rather than only the final Gradle summary;
- enough surrounding log context for diagnosis.

Stop the run after collecting that evidence.

If the build succeeds, record:

- `BUILD SUCCESSFUL`;
- exact Git SHA;
- full APK path;
- APK byte size;
- APK SHA-256.

## Install and launch

Install the generated debug APK on the emulator with `adb install -r`.

The development package is `org.telegram.messenger.beta`.

Do not interact with or uninstall the official `org.telegram.messenger` package if it happens to exist on the host or emulator.

After installation, launch the normal Cybergram application and confirm that the process/activity starts and remains alive without a FATAL EXCEPTION or ANR.

An authenticated Telegram session is not expected and must not be created as part of this run.

Capture one screenshot of the normal application launch and keep it as a local validation artifact. Do not add screenshots to Git.

## DEBUG showcase

Launch the existing DEBUG-only showcase activity explicitly:

```text
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity
```

Confirm that `CybergramShowcaseActivity` becomes the resumed activity and remains alive without FATAL EXCEPTION or ANR.

Capture one screenshot of the showcase and keep it as a local validation artifact. Do not add it to Git.

## Validation semantics

This emulator run establishes only:

- current-HEAD Gradle buildability for the x86_64 developer variant;
- APK installation on API 36 x86_64;
- basic process/activity startup;
- DEBUG showcase startup/rendering;
- absence of immediately observed FATAL/ANR during those checks.

It does not replace future physical-device validation and does not prove authenticated Telegram screens, real account state, networking, push behaviour, OEM-specific rendering or Samsung-specific behaviour.

In particular, an unauthenticated emulator cannot by itself fully validate production `FilterTabsView`, `MainTabsLayout`, chat history or other account-dependent screens. Those surfaces may later receive dedicated DEBUG showcase scenes for account-independent visual validation.

## Final report

Return a concise factual report containing:

- starting and final Git SHA;
- worktree status;
- AVD name and system-image/API/ABI details;
- Gradle command and result;
- APK path, size and SHA-256 when successful;
- install result;
- normal app launch result;
- `CybergramShowcaseActivity` launch result;
- observed FATAL/ANR status;
- local screenshot paths;
- any environmental blocker encountered.

Do not claim device-smoke or authenticated-screen validation from this emulator-only run.