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

    private CybergramTheme() {
        // Utility class.
    }
}
