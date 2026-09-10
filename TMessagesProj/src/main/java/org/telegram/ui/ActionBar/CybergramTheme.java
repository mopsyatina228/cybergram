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
    public static final int AMBER = 0xFFE8D93A;
    public static final int AMBER_HIGHLIGHT = 0xFFF2E75B;
    public static final int DANGER = 0xFFFF2E46;

    public static final int TEXT = 0xFFE6F2F2;
    public static final int TEXT_MUTED = 0xFF7C8A91;
    public static final int TEXT_ON_AMBER = 0xFF101216;

    /** Stable opaque composites matching restrained translucent-looking message surfaces. */
    public static final int IN_BUBBLE = 0xFF282715;
    public static final int IN_BUBBLE_SELECTED = 0xFF3E3C19;
    public static final int OUT_BUBBLE = 0xFF07252C;
    public static final int OUT_BUBBLE_SELECTED = 0xFF063A44;
    public static final int OUT_TEXT = 0xFFD7FCFF;

    public static final int DIVIDER = 0xFF1B2B32;
    public static final int HINT = 0xFF66777E;

    /** Built-in Cybergram theme name; used only for the presentation activation gate. */
    public static final String THEME_NAME = "Cybergram";

    /** Corner chamfer cut, in dp, for the Cybergram message silhouette (45-degree corners). */
    public static final float BUBBLE_CORNER_CUT_DP = 6f;

    /** Chamfer cut, in dp, for the "near" corners of a grouped Cybergram bubble. */
    public static final float BUBBLE_NEAR_CORNER_CUT_DP = 2f;

    /** Cybergram message outline stroke width, in dp. */
    public static final float BUBBLE_BORDER_WIDTH_DP = 1f;

    /** Reference-style header rail: restrained warning red under normal Cybergram chrome. */
    public static final int HEADER_RULE_ALPHA = 190;

    /** Alpha of secondary cyan header ticks/identity segment. */
    public static final int HEADER_TECH_ALPHA = 180;

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
     *   - the active {@link Theme#getCurrentTheme()} is the built-in Cybergram theme
     *     (a real client with Cybergram active usually has {@code provider == null}).
     *
     * False in every other case. It never infers Cybergram from colour values.
     */
    public static boolean isCybergramPresentation(Theme.ResourcesProvider provider) {
        if (provider instanceof GeometryProvider) {
            return true;
        }
        try {
            Theme.ThemeInfo info = Theme.getCurrentTheme();
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
