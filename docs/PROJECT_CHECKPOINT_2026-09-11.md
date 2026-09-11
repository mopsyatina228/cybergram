# Cybergram project checkpoint — 2026-09-11

This document is a durable recovery checkpoint for `mopsyatina228/cybergram`. It records the repository state and the implementation/validation boundary immediately before the 2026-09-11 preservation pass.

It is a recovery aid, not a replacement for `AGENTS.md` or `docs/CYBERGRAM_UI_SPEC.md`. For the current concise entrypoint use `docs/CURRENT_STATE.md`.

## Repository cut

- Repository: `mopsyatina228/cybergram`
- Upstream: `DrKLO/Telegram`
- Upstream-aligned `master`: `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`
- Upstream version: Telegram Android 12.10.1 (7038)
- Captured Cybergram `dev`: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`
- Captured `dev` tree: `1610305599d9f72c9c0fa2c8938c29f125a5eee1`
- At capture, `dev` is 38 commits ahead of `master` and 0 behind.

The preservation/documentation commits made after this checkpoint do not alter the captured product-code cut unless explicitly stated.

## Branch policy

`master` is the upstream-aligned baseline. Cybergram product integration belongs on `dev`. Larger isolated work may use `feature/<scope>` or `fix/<scope>`. Keep Cybergram-specific seams narrow and presentation-focused so upstream synchronization remains tractable.

## Canonical recovery order

A fresh session should read, in order:

1. `AGENTS.md` — repository rules and safety boundaries.
2. `docs/CURRENT_STATE.md` — concise current project/recovery state.
3. `docs/CYBERGRAM_UI_SPEC.md` — visual/product design authority.
4. `docs/REFERENCE_ALIGNMENT_2026-09-10.md` — reference-derived visual decisions and the header normalization rationale.
5. `docs/WORK_STATE.md` — detailed chronological implementation and machine/device evidence.
6. This checkpoint when exact 2026-09-11 branch/validation boundaries are needed.

Never reconstruct current state from chat/model memory when these repository sources are available.

## Product boundary

Cybergram is a visual/presentation fork of Telegram Android, not a protocol rewrite. The standing prime directive remains: change presentation before behaviour. No intentional Cybergram work should modify MTProto, account/session handling, encryption, storage/database semantics, networking, media transport or other messaging business logic unless a future task explicitly justifies it.

The visual target is an original dark cyberpunk/HUD language: near-black technical surfaces, cyan interaction/structure, amber identity/attention, restrained red structural/state accents, angular/chamfered geometry, sparse HUD detail, and readable Telegram interaction density. Proprietary Cyberpunk 2077/CD Projekt assets/fonts/branding are not part of the project.

## Implemented and durably present on `dev`

The current tree contains the following major Cybergram layers.

### Foundation and theme

- `cybergram.attheme` and a built-in `Cybergram` theme.
- Fresh-install day/night seeding through Telegram's existing preference pipeline without overwriting an existing user's theme choice.
- CRLF-safe `.attheme` parsing and `.gitattributes` handling.
- `tools/validate_cybergram_theme.py` for deterministic theme-key validation.
- Central Cybergram presentation/theme helpers rather than scattered colour/name checks.

### Message presentation

- Cybergram-owned angular bubble/HUD primitives.
- Cybergram-gated angular `MessageDrawable` geometry for the supported text/media paths.
- Dark warm incoming bubble palette with amber semantic outline/accent and light text.
- Dark cyan outgoing presentation with cyan semantic outline/accent.
- Grouped/near-corner handling and message rendering work recorded in `docs/WORK_STATE.md`.

The message renderer remains deliberately narrow around upstream Telegram behaviour; do not turn this into a replacement rendering stack without a new design decision.

### Chat chrome

- Cybergram structural header decoration.
- Cybergram composer framing and angular send treatment.
- Cybergram-specific flat chat-header seam in `ActionBar`: keeps upstream glass-mode layout/behaviour side effects while suppressing the three visual glass capsules for Cybergram and restoring the opaque action-bar panel.

The flat-header pass was build- and real-device validated. `ChatAvatarContainer.setGlassMode()` was intentionally retained because it owns metrics/layout, not the capsule surfaces.

### Dialogs chrome

- Dense dialog rows retain Telegram hierarchy while adding restrained Cybergram separators/edge accents and a Cybergram selected-state panel.
- Dialogs action-bar structural decoration with state-aware visibility.
- Opt-in angular Cybergram FAB while leaving the shared upstream component unchanged by default.
- Angular Cybergram dialogs search field/presentation work already present in the current tree.
- Primary chrome typography uses Android system `sans-serif-condensed` through `CybergramTypography`; no proprietary/bundled game font is used and message-body typography is untouched.
- Launcher branding has been replaced with a simplified original cyan/amber angular plane derived for small-size readability.

### Latest landed change: filter/folder tabs

`dev` HEAD `52b8e219729d0a90dd3335165cf4ef44acf46e5e` contains `refine(dialogs): flatten Cybergram filter tabs`.

The final patch is limited to `TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java` and adds a Cybergram-only presentation seam:

- dark chamfered panel via `CybergramHudDrawable`;
- dark/cyan chamfered selected plate;
- Cybergram polygon clipping via the shared `CybergramBubbleDrawable.buildPath(...)` helper;
- suppression of the blurred background only when Cybergram presentation is active;
- upstream rounded/blurred behaviour retained when Cybergram presentation is inactive.

This change is **LANDED ON `dev`, VALIDATION PENDING**. There is no GitHub CI/status evidence and no durable post-landing Android build/device record for this exact final HEAD. Do not describe the final filter-tabs patch as build- or runtime-validated until new machine evidence exists.

## Durable validation evidence already recorded

`docs/WORK_STATE.md` contains multiple successful local single-ABI builds using:

` :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a `

with Gradle 8.11.1, AGP 8.10.1, JDK 17, Android SDK 36 and NDK 27.2.12479018.

It also records real-device smoke/visual validation on Samsung SM-A256E / Android 16 for earlier integrated passes, including theme activation/persistence, deterministic debug showcase, bubble/palette work, dialogs rows/chrome, angular FAB, flat chat header, typography and launcher icon. Those historical successes are valid evidence for their recorded revisions, but they must not be silently promoted to validation of later code.

Official `org.telegram.messenger` was intentionally left untouched during the recorded beta/debug validation; the development package was `org.telegram.messenger.beta`.

## Side branches at checkpoint

Four branches existed at capture:

- `master` -> `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`
- `dev` -> `52b8e219729d0a90dd3335165cf4ef44acf46e5e`
- `feature/cybergram-filter-tabs-flat` -> `db255faa1a98733fe18f164fbbe3fde460af4621`
- `automation/filter-tabs-runner` -> `dbdca3e9febed1a18ce87b225b1822061587f582`

The feature branch has the **same final tree** (`1610305599d9f72c9c0fa2c8938c29f125a5eee1`) as captured `dev`; its divergent commits are transient staging/history, not unique product content. Do not merge it merely to "recover" filter-tabs work already present on `dev`.

`automation/filter-tabs-runner` is a temporary runner history whose final tree returns to the pre-final-filter-patch state. It contains no unique product result that must be integrated. It is not an active execution authority.

No current work should be inferred from either side branch simply because it exists.

## Known deferred / incomplete surfaces

The high-value remaining presentation work includes, without implying execution order:

- real build/device validation of the final flat `FilterTabsView` patch;
- bottom navigation (`MainTabsLayout` / `GlassTabView`) normalization; this is a shared complex component and should remain a separate bounded pass;
- remaining message-state coverage such as replies/reactions/forwards/pressed/selection/media edge cases and any still-deferred `TYPE_PREVIEW` geometry work;
- service/date geometry beyond palette alignment;
- optional dedicated non-interactive chat-canvas HUD/background layer after primary chrome is stable;
- secondary client surfaces (settings, profiles, media viewer, calls, etc.) only after the primary messaging flow is coherent;
- release identity/credentials/signing/Firebase/package decisions remain separate from visual development.

Do not treat a deferred item as authorized implementation merely because it appears in this list.

## Historical documentation caveats

`docs/WORK_STATE.md` is a detailed chronological evidence log and is intentionally large. Its final section predates the final `52b8e219...` filter-tabs landing, so it is not by itself a complete statement of HEAD.

`README.md` historically described the project as only being in the early palette/render-surface mapping phase; that wording is stale relative to the current implementation and should be read together with `docs/CURRENT_STATE.md`.

`docs/REFERENCE_ALIGNMENT_2026-09-10.md` contains historical "static-only pending validation" wording for the alignment pass. Later subsections/records document that some of those specific follow-up items, notably the flat chat header, subsequently received build/device validation. Preserve the chronology rather than rewriting history to make every earlier statement sound contemporaneous.

## Current operating mode at preservation time

For the 2026-09-11 preservation session, project work is **repository-only / no orchestration**.

Allowed: inspect GitHub state, preserve/reconcile documentation, perform explicit repository mutations requested by the user, and formulate future bounded work.

Not authorized by this checkpoint: autonomous agents, supervisor loops, Machine-SV/Web-SV equivalents, background runners, automatic continuation, Android runtime/build execution, or starting new implementation merely because a deferred item exists.

This is a session operating boundary, not a permanent architectural rule for Cybergram.

## Residual preservation boundary

GitHub contains the complete captured `dev` product tree and the filter-tabs result. Unlike the `game-test` PLAY candidate situation, no known required Cybergram product patch is represented only by an unpushed task branch in the repository evidence inspected here.

GitHub cannot prove that an arbitrary developer machine has no additional uncommitted files. Therefore local workspace cleanliness must be freshly checked before claiming there is no local-only work. There is currently no repository evidence strong enough to justify invoking a machine agent merely to speculate about such work.
