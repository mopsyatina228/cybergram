# Cybergram bounded passes

These files are handoff-ready execution specifications derived from `docs/EXECUTION_BACKLOG.md` and the source-ownership map in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`.

Give a local executor one pass file plus the authority documents named inside it. Do not make the executor reconstruct scope from chat history.

Current pass set:

- `B0_FILTER_TABS_VALIDATION.md` — `PARTIAL`: generic API 36 x86_64 emulator build/install/start baseline passed; authenticated production `FilterTabsView` interaction/visual matrix remains pending. Validation only, no opportunistic production fixes.
- `B1_MAIN_TABS_FLAT.md` — `INTEGRATED / STATIC PASS / E PASS / A PENDING / P PENDING`: bounded main bottom-navigation presentation pass, landed on `dev` as `ab314d882b193ec5df9dd188b7e945ea7ef35c98` plus DEBUG fixture `6802e001012f2cad8eddcc89d137c17534e8f1ba`. Important constraint: `GlassTabView` (shared with attach/bot tabs and other `createMainTab` callers) and `MainTabsLayout` (also hosted by `StatisticActivity`) both require explicit per-instance main-tabs opt-in in addition to the central Cybergram gate.
- `B2_MESSAGE_STATE_AUDIT.md` — `INTEGRATED / AUDIT COMPLETE`: production-fix-free evidence matrix for message body, replies, reactions, forwards, selected/pressed states and `TYPE_PREVIEW` ownership. Matrix: `docs/B2_MESSAGE_STATE_AUDIT_2026-09-12.md`. Result after semantic correction 14 PASS / 0 CONFIRMED DEFECT / 6 DESIGN-OPEN / 15 UNTESTED; `TYPE_PREVIEW` closed (theme-preview-only callers) so B3 is `CLOSED / NOT REQUIRED`; reply/reaction styling is `B4 = DESIGN-OPEN / NOT AUTHORIZED` — rounded compact internal controls are not a spec violation, so no implementation spec is written.
- `B5_SERVICE_DATE_AUDIT.md` — `INTEGRATED / AUDIT COMPLETE / STATIC OWNERSHIP PASS / E BUILD+RUNTIME+VISUAL-DENSITY PASS / STRATEGY B SELECTED / A RICH+AUTHENTICATED CASES PENDING / P NOT REQUIRED FOR AUDIT`: ordinary-vs-rich `ChatActionCell` ownership audit complete; the ordinary plate owner is `backgroundPath`, and the E visual-density gate selected Strategy B (one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate) for the plain service/date path. Evidence and B6 proposal: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`. No production change.

B6 has no spec file in this directory: it is the bounded implementation of the Strategy B geometry that B5 already specified in `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md` §5. Its implementation/evidence record lives at `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md` (top-level `docs/`, alongside the dated B5 audit).

Pass numbering intentionally reserves conditional implementation work:

- B3 — `MessageDrawable.TYPE_PREVIEW`: `CLOSED / NOT REQUIRED` (B2 proved theme-preview-only callers);
- B4 — reply/reaction styling: `DESIGN-OPEN / NOT AUTHORIZED` (measured, owner-proven, no ruling that compact internal controls must be angular);
- B6 — ordinary service/date implementation derived from B5: `IMPLEMENTED ON FEATURE BRANCH / NOT INTEGRATED ON dev`. Landed as production commit `f86ef812bb585db7c753335c7f573406e5129323` (`ChatActionCell.java` only, +22/−1) plus DEBUG control commit `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` on `feature/cybergram-service-date-angular`. Independent review verdict `SAFE_MINIMAL_SEAM`; narrow scope held exactly — ordinary `ChatActionCell.backgroundPath` only, with rich/special states, reactions and non-Cybergram presentation excluded. `E-build`/`E-install+launch`/`E-geometry`/`E-control` PASS; `E-rich` unavailable pre-auth; `A` pending; `P` not required. The `ThemePreviewActivity` preview-scope question is answered for today's code (foreign app-theme `SCREEN_TYPE_PREVIEW` has no `ChatActionCell`/`contentType == 1` row and the wallpaper/chat-theme preview is current app presentation, so no extra production opt-out is needed now), with a latent caveat recorded for any future foreign-theme preview row. Open non-rendering release/install issue: Redmi Note 10S / MIUI 14.0.4 arm64 install report, unresolved (no `adb` error text). Record: `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`;
- B7 — optional later chat-canvas HUD;
- B8 — secondary screens/onboarding after the primary messaging flow is coherent.

Validation tiers are defined in `docs/EXECUTION_BACKLOG.md`: E = emulator baseline, A = authenticated production UI, P = physical-device/OEM confidence. Never promote one tier into another.

Execution of one pass does not authorize the next. Repository planning is not permission to start agents, builds or runtime sessions automatically.
