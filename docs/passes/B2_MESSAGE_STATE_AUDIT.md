# B2 — message-state coverage and ownership audit

Status: READY

Type: evidence/audit

Production fixes: FORBIDDEN IN THIS PASS

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

Validation runbook: `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`

## Mission

Turn the vague backlog item “remaining message states/edge cases” into a deterministic evidence matrix. Every visible mismatch must be marked tested-good or assigned to the component that actually owns it before any production correction is authorized.

Do not fix what B2 discovers.

## Startup

1. Fetch current `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`, this file and the message-geometry section of `docs/CYBERGRAM_UI_SPEC.md`.
3. Record exact tested SHA and local `git status`.
4. Do not reset/discard unknown local work.

## Mandatory local source reconnaissance

Before changing even debug-only code, use the local clone to trace actual owners/callers. At minimum run equivalent searches for:

- `git grep -n "TYPE_PREVIEW" -- '*.java'`
- `git grep -n "new MessageDrawable" -- '*.java'`
- `git grep -n "ReplyMessageLine" -- '*.java'`
- `git grep -n "ReactionsLayoutInBubble" -- '*.java'`

Record relevant caller paths in the audit. Do not rely on GitHub code-search availability; local `git grep` is authoritative for the checked-out tree.

For every `TYPE_PREVIEW` occurrence distinguish declaration/internal handling from actual instantiation/call sites.

## Allowed repository changes

No production rendering file may be changed.

Allowed only when needed for deterministic evidence:

- `TMessagesProj_App/src/debug/java/org/telegram/ui/CybergramShowcaseActivity.java`;
- debug-only manifests already used by the showcase;
- an additional debug-only fixture/activity if extending the existing showcase would become less clear;
- a dedicated dated audit document or a clearly delimited evidence append after the matrix is complete.

Any debug fixture must remain outside release source/manifests and must not change saved user theme preferences.

## Known ownership boundaries

Start from these proven static boundaries and refine them with runtime evidence:

- base text/media body geometry: `MessageDrawable`;
- cell composition/clipping/layout: `ChatMessageCell`;
- reply line/background: `ReplyMessageLine`;
- reaction buttons/layout/touch: `ReactionsLayoutInBubble`;
- service/date action cell: `ChatActionCell`.

Current Cybergram `MessageDrawable` angular path and border explicitly cover `TYPE_TEXT` / `TYPE_MEDIA` and exclude `TYPE_PREVIEW`.

If evidence points elsewhere, record the actual owner.

## Required coverage matrix

At minimum inspect/capture:

- incoming plain text;
- outgoing plain text;
- incoming selected/pressed state;
- outgoing selected/pressed state;
- grouped messages: top, middle and bottom adjacency;
- incoming media;
- outgoing media;
- media with caption;
- incoming reply block;
- outgoing reply block;
- forwarded header/state;
- reactions absent;
- one reaction;
- multiple reactions;
- selected/chosen reaction when reproducible;
- links plus message metadata/time/checks/views where applicable;
- bot-buttons-bottom/body interaction where reproducible;
- service/date cell adjacent to ordinary messages;
- every actual product surface found to instantiate/use `MessageDrawable.TYPE_PREVIEW`.

Do not invent synthetic defects. Record cases that are actually rendered. Debug fixtures may synthesize data/state only to exercise real production renderers/components, not to replace them with mock drawings.

## Per-case evidence schema

For every case record:

- case name;
- reproduction steps/data;
- validation tier: debug E fixture, authenticated A surface, or physical P surface;
- screenshot/artifact;
- expected result under `CYBERGRAM_UI_SPEC.md`;
- actual result;
- `PASS`, `DEFECT`, or `UNTESTED`;
- owner file/class;
- defect category: colour, geometry, clipping, layout, state transition, animation or interaction;
- whether non-Cybergram presentation is affected;
- smallest plausible future production file scope;
- severity: blocker / primary-flow inconsistency / polish.

Do not omit tested-good cases. The matrix must distinguish PASS from UNTESTED.

## `TYPE_PREVIEW` rule

`MessageDrawable.TYPE_PREVIEW` is not automatically a defect because it remains upstream-rounded.

Before proposing any change, identify from local source:

- all instantiation/call sites;
- whether each surface is normal messaging UI, theme/editor preview, debug tooling or another special surface;
- whether Cybergram presentation should apply there;
- what the special density/scaling/global-theme colour behaviour protects.

Only evidence may promote `TYPE_PREVIEW` into B3.

## Reply/reaction rule

Do not describe reply or reaction defects as generic `ChatMessageCell` problems unless source/runtime evidence proves ownership there.

`ReplyMessageLine` and `ReactionsLayoutInBubble` own independent path/paint/animation state and receive separate future tasks when they are the actual owner.

## Validation approach

The existing API 36 x86_64 emulator is the default E environment.

If debug showcase/fixture code changes:

- build the x86_64 afat debug variant using the emulator runbook;
- install/update `org.telegram.messenger.beta`;
- launch the fixture/showcase;
- capture screenshots;
- check FATAL/ANR;
- verify debug tooling does not mutate saved theme preferences.

E fixtures are suitable for deterministic body/geometry/selected-state evidence when they use production renderers.

Use an authenticated A environment for states that depend on real message data/state and cannot be represented faithfully by debug fixtures. Record unavailable cases as UNTESTED rather than guessing.

P physical-device validation is not required for the audit itself unless a defect appears device/OEM-specific.

## Stop conditions

Stop rather than convert B2 into implementation if:

- a mismatch tempts an immediate production fix;
- owner remains uncertain after source tracing;
- reproducing a state would require protocol/data changes;
- test setup begins modifying release manifests, production credentials or message/network behaviour.

## Success condition

B2 succeeds when:

- local source reconnaissance lists the relevant callers/owners;
- every required state is PASS, DEFECT or explicitly UNTESTED;
- every DEFECT has a concrete owner and bounded candidate scope;
- no production rendering fix was made.

Write the matrix durably into the repository. Generate B3/B4 specs only for confirmed production defects.

## Required handoff report

Return:

- exact tested SHA;
- source-recon commands and caller/owner findings;
- debug-only changed files/commit, if any;
- E/A/P evidence used;
- evidence-matrix document path;
- counts of PASS / DEFECT / UNTESTED;
- owner list for all defects;
- proposed conditional pass IDs/titles;
- explicit statement that no production rendering fix was made during B2.
