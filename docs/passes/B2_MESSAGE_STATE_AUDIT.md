# B2 — message-state coverage and ownership audit

Status: READY TO EXECUTE AFTER CURRENT PRIMARY-CHROME VALIDATION PRIORITIES

Type: evidence/audit

Production fixes: FORBIDDEN IN THIS PASS

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

## Mission

Turn the vague backlog item "remaining message states/edge cases" into a deterministic evidence matrix. Every visible mismatch must be assigned to the component that actually owns it before any production correction is authorized.

Do not fix what you discover during B2.

## Startup

1. Fetch current `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` and the message-geometry section of `docs/CYBERGRAM_UI_SPEC.md`.
3. Record exact tested SHA and local `git status`.
4. Do not reset/discard unknown local work.

## Allowed repository changes

No production rendering file may be changed.

Allowed only when needed for deterministic evidence:

- `TMessagesProj_App/src/debug/java/org/telegram/ui/CybergramShowcaseActivity.java`
- debug-only manifests already used by the showcase;
- a dedicated dated audit document or `docs/WORK_STATE.md` after evidence is complete.

If the showcase needs a new case, it must remain debug-only and must not apply/change the user's saved theme preferences.

## Known ownership boundaries

Start from these hypotheses and confirm each defect rather than ignoring them:

- base text/media body geometry: `MessageDrawable`;
- cell layout/composition/clipping: `ChatMessageCell`;
- reply line/background: `ReplyMessageLine`;
- reaction buttons/layout/touch: `ReactionsLayoutInBubble`;
- service/date action cell: `ChatActionCell`.

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
- actual product surfaces, if any, using `MessageDrawable.TYPE_PREVIEW`.

Do not invent synthetic defects. Record cases that are actually rendered.

## Per-case evidence schema

For every mismatch record:

- case name;
- reproduction steps/data;
- screenshot/artifact;
- expected result under current `CYBERGRAM_UI_SPEC.md`;
- actual result;
- owner file/class;
- defect category: colour, geometry, clipping, layout, state transition, animation or interaction;
- whether non-Cybergram presentation is affected;
- smallest plausible future production file scope;
- severity: blocker / primary-flow inconsistency / polish.

For cases that look correct, record `PASS` rather than omitting them. The matrix must distinguish untested from tested-good.

## `TYPE_PREVIEW` rule

`MessageDrawable.TYPE_PREVIEW` is NOT automatically a bug because it remains upstream-rounded.

Before proposing any change, identify:

- where `TYPE_PREVIEW` is instantiated;
- whether the surface is part of normal messaging UI or a theme/editor preview;
- whether Cybergram presentation should apply there;
- what its special density/scaling/global-theme colour behaviour protects.

Only evidence may promote `TYPE_PREVIEW` into a later implementation pass.

## Reply/reaction rule

Do not describe reply or reaction defects as generic `ChatMessageCell` defects unless the layout owner is proven there.

`ReplyMessageLine` and `ReactionsLayoutInBubble` own independent path/paint/animation state and must receive separate future tasks if they are the owners.

## Build/device evidence

If debug showcase code is changed, rebuild/install the established debug variant and prove:

- showcase launches;
- no FATAL/ANR;
- active saved user theme is unchanged before/after;
- added cases use real production renderers/components where practical.

For live-chat cases, record app/device revision and screenshots.

## Stop conditions

Stop rather than convert B2 into implementation if:

- a mismatch tempts an immediate production fix;
- owner is uncertain;
- reproducing the state would require protocol/data changes;
- test setup begins modifying release manifests or user credentials.

## Success condition

B2 succeeds when every tested state is marked PASS or has a concrete owner and bounded candidate scope.

The result must be durable in the repository, either as a dated audit document or a clearly delimited `docs/WORK_STATE.md` section.

Future B3/B4 production tasks are derived only from confirmed B2 findings.

## Required handoff report

Return:

- exact tested SHA;
- any debug-only changed files/commit;
- build/device evidence if debug code changed;
- evidence-matrix document path;
- counts of PASS / defect / untested cases;
- owner list for all defects;
- proposed future bounded pass IDs/titles;
- explicit statement that no production rendering fix was made during B2.