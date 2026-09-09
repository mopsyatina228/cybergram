package org.telegram.ui.ActionBar;

/**
 * Cybergram-owned visual constants.
 *
 * Keep project-specific styling values here instead of scattering literals
 * through Telegram's upstream UI classes. The initial .attheme prototype uses
 * the same palette. Geometry-specific constants will be added only when the
 * custom message drawable work starts.
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

    public static final int OUT_BUBBLE = 0xFF0A1A21;
    public static final int OUT_BUBBLE_SELECTED = 0xFF10313C;
    public static final int OUT_TEXT = 0xFFD7FCFF;

    public static final int DIVIDER = 0xFF1B2B32;
    public static final int HINT = 0xFF66777E;

    /** Built-in Cybergram theme name; used only for the angular-geometry activation gate. */
    public static final String THEME_NAME = "Cybergram";

    /** Corner chamfer cut, in dp, for the Cybergram message silhouette (45-degree corners). */
    public static final float BUBBLE_CORNER_CUT_DP = 6f;

    /** Chamfer cut, in dp, for the "near" corners of a grouped Cybergram bubble. */
    public static final float BUBBLE_NEAR_CORNER_CUT_DP = 2f;

    /** Cybergram message outline stroke width, in dp. */
    public static final float BUBBLE_BORDER_WIDTH_DP = 1f;

    /**
     * Marker {@link Theme.ResourcesProvider} that opts a renderer into Cybergram
     * angular message geometry. The debug showcase implements this so it can exercise
     * the Cybergram silhouette even while the global user theme stays "Day".
     */
    public interface GeometryProvider extends Theme.ResourcesProvider {
    }

    /**
     * Centralised activation gate for Cybergram angular message geometry.
     *
     * True when:
     *   - {@code provider} is a {@link GeometryProvider} (the debug showcase), OR
     *   - the active {@link Theme#getCurrentTheme()} is the built-in Cybergram theme
     *     (a real client with Cybergram active usually has {@code provider == null}).
     *
     * False in every other case. It never infers Cybergram from colour values.
     */
    public static boolean useAngularMessageGeometry(Theme.ResourcesProvider provider) {
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

    private CybergramTheme() {
        // Utility class.
    }
}
