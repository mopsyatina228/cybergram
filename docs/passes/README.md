# Cybergram bounded passes

These files are handoff-ready execution specifications derived from `docs/EXECUTION_BACKLOG.md` and the static source map in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`.

They are deliberately narrower than the overall project roadmap. A local coding/validation agent may be given one pass file and instructed to execute exactly that pass, after reading the repository authority documents named inside it.

Available passes:

- `B0_FILTER_TABS_VALIDATION.md` — validation-only closure of the final landed filter/folder tabs result. No production fixes allowed during the pass.
- `B1_MAIN_TABS_FLAT.md` — high-risk but bounded Cybergram bottom-navigation presentation pass, limited to `MainTabsActivity`, `MainTabsLayout` and `GlassTabView` unless new evidence explicitly expands scope.
- `B2_MESSAGE_STATE_AUDIT.md` — production-fix-free evidence matrix for message states, replies, reactions, forwards and `TYPE_PREVIEW` ownership.
- `B5_SERVICE_DATE_AUDIT.md` — service/date ordinary-vs-rich ownership/runtime audit and geometry decision before any production `ChatActionCell` restyle.

Pass numbering intentionally leaves room for conditional follow-up implementation specs (`B3/B4` message fixes and `B6` service/date implementation) that must be generated from audit evidence rather than guessed in advance.

Execution of one pass does not authorize the next. Repository-only planning/documentation is not permission to start local agents, builds or runtime sessions automatically.