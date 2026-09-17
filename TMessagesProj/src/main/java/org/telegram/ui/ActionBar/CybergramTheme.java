package org.telegram.ui.ActionBar;

/**
 * Cybergram-owned visual constants.
 *
 * Keep project-specific styling values here instead of scattering literals
 * through Telegram's upstream UI classes. The checked-in .attheme uses the
 * same core palette; screen-specific semantic keys may refine it further.
 */
public final class CybergramTheme {

    public static final int BACKGROUND = 0xFF080A0F;
    public static final int PANEL = 0xFF0B0D12;
    public static final int PANEL_RAISED = 0xFF111820;

    public static final int CYAN = 0xFF00E5FF;
    public static final int CYAN_SECONDARY = 0xFF33D6FF;
    /** Ruled 2026-09-15: reference amber (was #E8D93A). See docs/OWNER_DECISIONS_2026-09-15.md. */
    public static final int AMBER = 0xFFFFB300;
    public static final int AMBER_HIGHLIGHT = 0xFFFFC94D;
    /** Ruled 2026-09-15: reference service red (was #FF2E46). */
    public static final int DANGER = 0xFFFF003C;

    public static final int TEXT = 0xFFE6F7FF;
    public static final int TEXT_MUTED = 0xFF6B7A8A;
    public static final int TEXT_ON_AMBER = 0xFF101216;

    /**
     * Low-saturation pale blue for header/composer line icons (owner ruling D9.6/D9.8,
     * docs/OWNER_DECISIONS_2026-09-17.md). The saturated cyan above stays reserved for thin
     * semantic accents (outgoing outline, checks, cursor), not for large icon masses.
     */
    public static final int ICON_PALE = 0xFF8FBFCC;

    /**
     * Refined message surfaces (owner ruling D9.2): incoming is a near-black olive, outgoing is a
     * dark blue-green. The saturated turquoise fill is gone — direction is carried by the thin
     * outline and the metadata colour, not by an opaque bright surface.
     */
    public static final int IN_BUBBLE = 0xFF0B0C07;
    public static final int IN_BUBBLE_SELECTED = 0xFF16180D;
    public static final int OUT_BUBBLE = 0xFF0A1B1D;
    public static final int OUT_BUBBLE_SELECTED = 0xFF0E282B;

    /** Body text: incoming light grey, outgoing pale blue (owner ruling D9.5). */
    public static final int IN_TEXT = 0xFFD3D6CE;
    public static final int OUT_TEXT = 0xFFCFEAF2;

    /** Time/metadata: smaller and dimmer than the body text on both sides (owner ruling D9.5). */
    public static final int IN_TIME = 0xFF8F8B78;
    public static final int OUT_TIME = 0xFF7E9AA3;

    /** Red structural rail for separators and technical framing (owner ruling D9.1). */
    public static final int SEPARATOR = DANGER;

    public static final int DIVIDER = 0xFF2A0A12;
    public static final int HINT = 0xFF5F6E7C;

    /** Built-in Cybergram theme name; used only for the presentation activation gate. */
    public static final String THEME_NAME = "Cybergram";

    /** Corner chamfer cut, in dp, for the Cybergram message silhouette (45-degree corners). */
    public static final float BUBBLE_CORNER_CUT_DP = 5f;

    /**
     * Length, in dp, of the small angular corner protrusion ("выступ") on the outer top corner of a
     * Cybergram message bubble (owner ruling D9.4). It is drawn inside the reserved 8dp tail region,
     * so the polygon stays within the drawable bounds. The other three corners keep the 45° chamfer.
     */
    public static final float BUBBLE_TAIL_DP = 4.5f;

    /** Chamfer cut, in dp, for the "near" corners of a grouped Cybergram bubble. */
    public static final float BUBBLE_NEAR_CORNER_CUT_DP = 2f;

    /** Cybergram message outline stroke width, in dp (reduced by owner ruling D9.3). */
    public static final float BUBBLE_BORDER_WIDTH_DP = 0.75f;

    /**
     * Extra vertical inset, in dp, applied to each *unjoined* vertical edge of a Cybergram
     * TYPE_TEXT bubble body (owner ruling D6, docs/OWNER_DECISIONS_2026-09-15.md §2 D6).
     *
     * It is a paint-only inset inside {@code MessageDrawable.generateCybergramPath(...)}: no
     * bounds, measurement, scroll or metadata position changes, and edges joined to a
     * neighbouring bubble of the same run ({@code isTopNear}/{@code isBottomNear}) keep the
     * upstream inset, so grouped joins are preserved. Distinct bubbles therefore gain
     * {@code 2 * BUBBLE_GAP_EXTRA_DP} of clear space between them.
     *
     * Owner ruling D10.2 (round 4c) resolved R-D6 by lowering this from 3f to
     * {@link #BUBBLE_GAP_MAX_DP}: the measured clearance showed 3f left exactly 0 px at the
     * binding bottom edge. Measured geometry (density 420, outline stroke dp(0.75) with
     * {@code Paint.Style.STROKE}): the drawable padding is dp(2) (MessageDrawable.java:564/630/907)
     * and {@code bounds.top} is dp(1) (ChatMessageCell.java:20560/20643), so the painted top is
     * dp(3) + E. The top edge never binds (plain text starts at dp(10.5)); the binding edge is the
     * time cluster at {@code layoutHeight - dp(6.5)} ({@code - dp(7.5)} when grouped,
     * ChatMessageCell.java:24505-24509), which needs E at or below {@link #BUBBLE_GAP_MAX_DP} to
     * keep a clear margin. {@code MessageDrawable} clamps to that maximum, so a future raise cannot
     * silently reintroduce the collision.
     * TYPE_MEDIA is deliberately excluded: a lowered media outline would expose the photo,
     * whose y is set independently of the drawable bounds.
     */
    public static final float BUBBLE_GAP_EXTRA_DP = 2.5f;

    /**
     * Largest safe value of {@link #BUBBLE_GAP_EXTRA_DP}, in dp (owner ruling D10.2, R-D6). At this
     * value the painted bottom edge still clears the time cluster; above it the outline intrudes
     * into the time box (measured at density 420: 3f gave 0 px standalone and -1 px grouped).
     * {@code MessageDrawable.generateCybergramPath(...)} clamps to it defensively.
     */
    public static final float BUBBLE_GAP_MAX_DP = 2.5f;

    /** Reference-style header rail: restrained warning red under normal Cybergram chrome. */
    public static final int HEADER_RULE_ALPHA = 190;

    /** Alpha of secondary cyan header ticks/identity segment. */
    public static final int HEADER_TECH_ALPHA = 180;

    /** Alpha (0..255) of the thin red separator above the composer (owner ruling D9.1). */
    public static final int COMPOSER_RULE_ALPHA = 170;

    /** Alpha (0..255) of the soft neon bloom under the red rails (owner ruling D9.8). */
    public static final int HEADER_GLOW_ALPHA = 70;
    public static final int COMPOSER_GLOW_ALPHA = 80;

    /** Radius, in dp, of the red-rail neon bloom (owner ruling D9.8). */
    public static final float RED_GLOW_RADIUS_DP = 3f;

    /** Extra vertical inner padding, in dp, for Cybergram text bubbles (owner ruling D9.4). */
    public static final float BUBBLE_INNER_PAD_DP = 2.5f;

    /** Extra horizontal text inset, in dp, for Cybergram bubbles (owner ruling D9.4). */
    public static final float BUBBLE_TEXT_INSET_DP = 2f;

    /** Extra leftward shift, in dp, of the outgoing delivery checks toward the time (D9.5). */
    public static final float CHECK_INSET_DP = 3f;

    /** Cybergram composer field height, in dp (owner ruling D9.7). Upstream is 44. */
    public static final int COMPOSER_HEIGHT_DP = 40;

    /** Cybergram chat-header avatar size, in dp (owner ruling D9.6). Upstream is 42. */
    public static final int HEADER_AVATAR_DP = 36;

    /** Alpha (0..255) of the thin cyan-dark separator on a dialogs row bottom edge. */
    public static final int DIALOGS_ROW_SEPARATOR_ALPHA = 66;

    /** Alpha (0..255) of the short left-edge cyan accent tab on a dialogs row. */
    public static final int DIALOGS_ROW_ACCENT_ALPHA = 150;

    /** Alpha (0..255) of the small 45-degree HUD tick on a dialogs row. */
    public static final int DIALOGS_ROW_TICK_ALPHA = 110;

    /**
     * Marker {@link Theme.ResourcesProvider} that opts a renderer into Cybergram
     * angular message geometry. The debug showcase implements this so it can exercise
     * the Cybergram silhouette even while the global user theme stays "Day".
     */
    public interface GeometryProvider extends Theme.ResourcesProvider {
    }

    /**
     * Centralised activation gate for Cybergram presentation.
     *
     * True when:
     *   - {@code provider} is a {@link GeometryProvider} (the debug showcase), OR
     *   - the active {@link Theme#getActiveTheme()} is the built-in Cybergram theme
     *     (a real client with Cybergram active usually has {@code provider == null}).
     *
     * The gate reads the <b>active</b> theme, not the day slot: {@link Theme#getCurrentTheme()}
     * returns {@code currentDayTheme}, so with Cybergram selected as the night theme the gate
     * was false while Cybergram was in fact the visible theme. {@link CybergramBackdropDrawable}
     * already reads {@code getActiveTheme()}; both sites must agree.
     *
     * False in every other case. It never infers Cybergram from colour values.
     */
    public static boolean isCybergramPresentation(Theme.ResourcesProvider provider) {
        if (provider instanceof GeometryProvider) {
            return true;
        }
        try {
            Theme.ThemeInfo info = Theme.getActiveTheme();
            if (info != null && THEME_NAME.equals(info.name)) {
                return true;
            }
        } catch (Throwable ignore) {
            // read-only gate; never propagate
        }
        return false;
    }

    /**
     * Activation gate for the Cybergram angular message geometry.
     *
     * Thin delegation to {@link #isCybergramPresentation(Theme.ResourcesProvider)} so the
     * message geometry and general HUD decoration share a single Cybergram detection path.
     */
    public static boolean useAngularMessageGeometry(Theme.ResourcesProvider provider) {
        return isCybergramPresentation(provider);
    }

    private CybergramTheme() {
        // Utility class.
    }
}
