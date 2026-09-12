# Cybergram bounded passes

These files are handoff-ready execution specifications derived from `docs/EXECUTION_BACKLOG.md` and the source-ownership map in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`.

Give a local executor one pass file plus the authority documents named inside it. Do not make the executor reconstruct scope from chat history.

Current pass set:

- `B0_FILTER_TABS_VALIDATION.md` — `PARTIAL`: generic API 36 x86_64 emulator build/install/start baseline passed; authenticated production `FilterTabsView` interaction/visual matrix remains pending. Validation only, no opportunistic production fixes.
- `B1_MAIN_TABS_FLAT.md` — `INTEGRATED / STATIC PASS / E PASS / A PENDING / P PENDING`: bounded main bottom-navigation presentation pass, landed on `dev` as `ab314d882b193ec5df9dd188b7e945ea7ef35c98` plus DEBUG fixture `6802e001012f2cad8eddcc89d137c17534e8f1ba`. Important constraint: `GlassTabView` (shared with attach/bot tabs and other `createMainTab` callers) and `MainTabsLayout` (also hosted by `StatisticActivity`) both require explicit per-instance main-tabs opt-in in addition to the central Cybergram gate.
- `B2_MESSAGE_STATE_AUDIT.md` — `READY`: production-fix-free evidence matrix for message body, replies, reactions, forwards, selected/pressed states and `TYPE_PREVIEW` ownership.
- `B5_SERVICE_DATE_AUDIT.md` — `READY`: ordinary-vs-rich `ChatActionCell` ownership/runtime audit and geometry decision before any production service/date restyle.

Pass numbering intentionally reserves conditional implementation work:

- B3 — `MessageDrawable.TYPE_PREVIEW` only if B2 proves a real product requirement;
- B4 — owner-specific reply/reaction/message fixes derived from B2 evidence;
- B6 — ordinary service/date implementation derived from B5;
- B7 — optional later chat-canvas HUD;
- B8 — secondary screens/onboarding after the primary messaging flow is coherent.

Validation tiers are defined in `docs/EXECUTION_BACKLOG.md`: E = emulator baseline, A = authenticated production UI, P = physical-device/OEM confidence. Never promote one tier into another.

Execution of one pass does not authorize the next. Repository planning is not permission to start agents, builds or runtime sessions automatically.
