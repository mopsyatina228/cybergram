# Cybergram current state

Last reconciled: 2026-09-11

This is the concise recovery entrypoint for the current Cybergram repository state. Read `AGENTS.md` first. Use `docs/CYBERGRAM_UI_SPEC.md` for design authority and `docs/WORK_STATE.md` for the detailed chronological implementation/evidence log.

Detailed preservation snapshot: `docs/PROJECT_CHECKPOINT_2026-09-11.md`, created in commit `a85af75f2dc86673259d11ddb2f4533bdafc93ef`.

Executable remaining-work plan: `docs/EXECUTION_BACKLOG.md`. It contains the current architecture map, risk boundaries, executor contract and bounded passes for filter-tabs validation, bottom navigation, message-state coverage, service/date treatment and later work. Read it before authorizing or implementing the next UI pass.

Static source-ownership evidence for those pass boundaries: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`. It records the actual bottom-navigation, message-state and service/date owners found in the current source tree; it is static evidence, not runtime validation.

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

Preservation/planning/documentation commits after that cut do not imply additional product implementation.

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

`docs/EXECUTION_BACKLOG.md` defines validation-only pass `B0` for closing this exact gap without mixing validation and opportunistic production fixes.

## Side branches

At the preservation cut:

- `feature/cybergram-filter-tabs-flat` -> `db255faa1a98733fe18f164fbbe3fde460af4621`
- `automation/filter-tabs-runner` -> `dbdca3e9febed1a18ce87b225b1822061587f582`

The feature branch's final tree is byte-identical at Git tree level to the captured `dev` product tree. It contains no product result missing from `dev` and should not be merged just for recovery.

The automation branch is temporary runner history and returns to the pre-final-filter-patch tree. It is not active work and not an execution authority.

## Highest-value unfinished surfaces

The next implementation choice has **not** been automatically authorized by this file. The executable ordering and exact pass boundaries now live in `docs/EXECUTION_BACKLOG.md`.

Current sequence starts with final filter-tabs build/device validation (`B0`), then a bounded bottom-navigation presentation pass (`B1`). Message-state work begins with an evidence/ownership audit (`B2`) rather than a broad `ChatMessageCell` patch. Service/date work similarly begins with ownership isolation before any `ChatActionCell` restyle. Optional chat-canvas HUD treatment and secondary screens remain later work.

Release identity, real credentials, signing/Firebase and any package/application-ID decision remain separate release work, not UI cleanup.

## Documentation semantics

`docs/EXECUTION_BACKLOG.md` is the execution-planning authority for remaining bounded work. It does not override the design language in `docs/CYBERGRAM_UI_SPEC.md` or validation evidence in `docs/WORK_STATE.md`.

`docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` is the static source-ownership map supporting the backlog. It does not claim new build/device evidence.

`docs/WORK_STATE.md` is the detailed chronological evidence log. It is useful precisely because it preserves what was known at each pass, but its tail predates the final `52b8e219...` filter-tabs landing. Do not use its last paragraph as the sole current-state oracle.

`docs/REFERENCE_ALIGNMENT_2026-09-10.md` is also chronological: its original static-only warning applies to that pass at that time; later recorded passes validate some subsequent implementations. Do not rewrite the old warning into a blanket claim that every current surface is validated.

`docs/CYBERGRAM_UI_SPEC.md` remains the design/product authority. This file describes implementation status, not visual policy.

## Current operating boundary

At this preservation point the user requested **repository-only operation, no orchestration**.

Repository inspection, documentation/preservation and explicit GitHub mutations are allowed. Do not infer permission to start autonomous agents, background runners, supervisor loops, Android builds/runtime sessions or new implementation from this status file.

## Recovery rule

Before making a new repository decision:

1. fresh-fetch `dev` and `master`;
2. read `AGENTS.md`, this file and `docs/EXECUTION_BACKLOG.md`;
3. read `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` for remaining-surface ownership when the task touches bottom navigation, messages or service/date cells;
4. read the relevant part of `docs/WORK_STATE.md` and the UI spec;
5. distinguish repository evidence from local-machine state;
6. never claim a local worktree is clean merely because GitHub is complete.

As of this checkpoint there is no **known** required Cybergram product patch that exists only outside GitHub. A future local-machine inspection may discover additional uncommitted work; treat that as a new fact requiring fresh evidence.
