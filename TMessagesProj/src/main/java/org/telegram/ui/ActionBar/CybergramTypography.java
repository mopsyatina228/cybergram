package org.telegram.ui.ActionBar;

import android.graphics.Typeface;

/**
 * Cybergram chrome typography.
 *
 * Cybergram gives primary chrome (chat header identity, dialogs rows and their timestamps, dialog
 * filter tabs and the dialogs search field) a narrower, more technical voice using the Android
 * system family {@code sans-serif-condensed}. Nothing is bundled and no proprietary/game font is
 * referenced; message body typography and every out-of-scope surface stay on the upstream path.
 *
 * The two chrome typefaces are created once and cached, and every resolver here is two-way: it
 * returns the condensed chrome typeface only while
 * {@link CybergramTheme#isCybergramPresentation(Theme.ResourcesProvider)} is true and otherwise
 * returns the caller's upstream typeface verbatim. Call sites therefore need no separate restore
 * path, and a non-Cybergram (Day/stock) presentation keeps upstream typography by construction.
 *
 * The gate is re-evaluated on every call, so recycled cells and re-used views flip back
 * automatically when the active theme changes.
 */
public final class CybergramTypography {

    /** Android system family used for Cybergram chrome. Not bundled, not proprietary. */
    private static final String CHROME_FAMILY = "sans-serif-condensed";
    private static final String CHROME_MEDIUM_FAMILY = "sans-serif-condensed-medium";
    private static final String MESSAGE_LIGHT_FAMILY = "sans-serif-condensed-light";

    private static Typeface chromeRegular;
    private static Typeface chromeBold;
    private static Typeface messageLight;

    /** Condensed chrome typeface, normal weight. Created once, then cached. */
    public static Typeface chromeRegular() {
        if (chromeRegular == null) {
            chromeRegular = Typeface.create(CHROME_FAMILY, Typeface.NORMAL);
        }
        return chromeRegular;
    }

    /** Condensed chrome typeface, medium emphasis. Created once, then cached. */
    public static Typeface chromeBold() {
        if (chromeBold == null) {
            chromeBold = Typeface.create(CHROME_MEDIUM_FAMILY, Typeface.NORMAL);
        }
        return chromeBold;
    }

    /** Lighter condensed body face used by Cybergram chat layouts. */
    public static Typeface messageRegular() {
        if (messageLight == null) {
            messageLight = Typeface.create(MESSAGE_LIGHT_FAMILY, Typeface.NORMAL);
        }
        return messageLight;
    }

    /** Message metadata/name face used by Cybergram chat layouts. */
    public static Typeface messageMedium() {
        return chromeBold();
    }

    /** True when Cybergram presentation is active for {@code provider}. */
    public static boolean isChrome(Theme.ResourcesProvider provider) {
        return CybergramTheme.isCybergramPresentation(provider);
    }

    /** {@link #chromeRegular()} under Cybergram, {@code upstream} unchanged otherwise. */
    public static Typeface chromeRegular(Theme.ResourcesProvider provider, Typeface upstream) {
        return isChrome(provider) ? chromeRegular() : upstream;
    }

    /** {@link #messageRegular()} under Cybergram, {@code upstream} unchanged otherwise. */
    public static Typeface messageRegular(Theme.ResourcesProvider provider, Typeface upstream) {
        return isChrome(provider) ? messageRegular() : upstream;
    }

    /** {@link #chromeBold()} under Cybergram, {@code upstream} unchanged otherwise. */
    public static Typeface chromeBold(Theme.ResourcesProvider provider, Typeface upstream) {
        return isChrome(provider) ? chromeBold() : upstream;
    }

    private CybergramTypography() {
        // Utility class.
    }
}
