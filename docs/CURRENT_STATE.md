# Cybergram current state

Last reconciled: 2026-09-11

This is the concise recovery entrypoint for the current Cybergram repository state. Read `AGENTS.md` first. Use `docs/CYBERGRAM_UI_SPEC.md` for design authority and `docs/WORK_STATE.md` for the detailed chronological implementation/evidence log.

Detailed preservation snapshot: `docs/PROJECT_CHECKPOINT_2026-09-11.md`, created in commit `a85af75f2dc86673259d11ddb2f4533bdafc93ef`.

## Repository authority

Repository: `mopsyatina228/cybergram`

Upstream: `DrKLO/Telegram`

Upstream-aligned baseline:
- branch: `master`
- Telegram Android: 12.10.1 (7038)
- commit: `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`

Cybergram integration branch: `dev`.

The product-code cut captured by the 2026-09-11 checkpoint is:
`52b8e219729d0a90dd3335165cf4ef44acf46e5e`

Preservation/documentation commits after that cut do not imply additional product implementation.

## Current product state

Cybergram is no longer merely a palette/bootstrap experiment. The primary messaging UI has an established Cybergram presentation layer while Telegram behaviour remains intentionally upstream-oriented.

Durably landed on `dev`:

- built-in Cybergram theme and `.attheme` palette;
- fresh-install Cybergram theme default without overwriting existing user choices;
- deterministic theme-key validator;
- shared Cybergram theme/HUD/angular geometry primitives;
- angular text/media message-bubble geometry and Cybergram semantic borders/palette;
- chat header HUD decoration;
- composer frame and angular send control;
- Cybergram flat chat header that suppresses glass capsules without globally disabling Telegram's glass-mode layout behaviour;
- dialogs row treatment and Cybergram selected-state panel;
- dialogs action-bar decoration;
- opt-in angular dialogs FAB;
- Cybergram dialogs search presentation;
- Cybergram primary-chrome typography using system `sans-serif-condensed`;
- original Cybergram launcher icon treatment;
- final Cybergram flattening seam for dialog filter/folder tabs in `FilterTabsView`.

No intentional protocol, encryption, account/session, database/storage or networking redesign is part of this state.

## Validation boundary

Earlier integrated passes have durable successful local-build and Samsung SM-A256E / Android 16 device-smoke evidence in `docs/WORK_STATE.md`.

The **latest final filter-tabs patch** at product-code commit `52b8e219729d0a90dd3335165cf4ef44acf46e5e` is landed but must be treated as:

`LANDED / POST-LANDING BUILD+DEVICE VALIDATION PENDING`

GitHub reports no commit-status checks and no Actions run for that HEAD. Do not inherit validation from an older revision merely because most of the app was previously exercised successfully.

## Side branches

At the preservation cut:

- `feature/cybergram-filter-tabs-flat` -> `db255faa1a98733fe18f164fbbe3fde460af4621`
- `automation/filter-tabs-runner` -> `dbdca3e9febed1a18ce87b225b1822061587f582`

The feature branch's final tree is byte-identical at Git tree level to the captured `dev` product tree. It contains no product result missing from `dev` and should not be merged just for recovery.

The automation branch is temporary runner history and returns to the pre-final-filter-patch tree. It is not active work and not an execution authority.

## Highest-value unfinished surfaces

The next implementation choice has **not** been automatically authorized by this file. Known remaining work includes final filter-tabs build/device validation; bottom-navigation normalization (`MainTabsLayout` / `GlassTabView`); remaining message-state/edge-case coverage; service/date geometry; optional sparse chat-canvas HUD treatment; and later secondary screens.

Release identity, real credentials, signing/Firebase and any package/application-ID decision remain separate release work, not UI cleanup.

## Documentation semantics

`docs/WORK_STATE.md` is the detailed chronological evidence log. It is useful precisely because it preserves what was known at each pass, but its tail predates the final `52b8e219...` filter-tabs landing. Do not use its last paragraph as the sole current-state oracle.

`docs/REFERENCE_ALIGNMENT_2026-09-10.md` is also chronological: its original static-only warning applies to that pass at that time; later recorded passes validate some subsequent implementations. Do not rewrite the old warning into a blanket claim that every current surface is validated.

`docs/CYBERGRAM_UI_SPEC.md` remains the design/product authority. This file describes implementation status, not visual policy.

## Current operating boundary

At this preservation point the user requested **repository-only operation, no orchestration**.

Repository inspection, documentation/preservation and explicit GitHub mutations are allowed. Do not infer permission to start autonomous agents, background runners, supervisor loops, Android builds/runtime sessions or new implementation from this status file.

## Recovery rule

Before making a new repository decision:

1. fresh-fetch `dev` and `master`;
2. read `AGENTS.md` and this file;
3. read the relevant part of `docs/WORK_STATE.md` and the UI spec;
4. distinguish repository evidence from local-machine state;
5. never claim a local worktree is clean merely because GitHub is complete.

As of this checkpoint there is no **known** required Cybergram product patch that exists only outside GitHub. A future local-machine inspection may discover additional uncommitted work; treat that as a new fact requiring fresh evidence.
