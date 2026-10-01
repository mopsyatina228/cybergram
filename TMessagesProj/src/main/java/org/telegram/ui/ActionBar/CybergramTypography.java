package org.telegram.ui.ActionBar;

import android.graphics.Typeface;

import org.telegram.messenger.ApplicationLoader;

/**
 * Cybergram chrome typography.
 *
 * Cybergram gives primary chrome (chat header identity, dialogs rows and their timestamps, dialog
 * filter tabs and the dialogs search field) a narrower, more technical voice using the Android
 * system family {@code sans-serif-condensed}. Chat-body typography has a separate optional private
 * asset seam for local licensed-game comparison; the repository itself contains no proprietary font.
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
    private static final String MESSAGE_FALLBACK_FAMILY = "sans-serif";
    private static final String MESSAGE_MEDIUM_FALLBACK_FAMILY = "sans-serif-medium";
    private static final String PRIVATE_MESSAGE_REGULAR = "fonts/cybergram_private/raj_rus_regular.ttf";
    private static final String PRIVATE_MESSAGE_MEDIUM = "fonts/cybergram_private/raj_rus_medium.ttf";

    private static Typeface chromeRegular;
    private static Typeface chromeBold;
    private static Typeface messageRegular;
    private static Typeface messageMedium;
    private static Typeface messageItalic;
    private static Typeface messageMediumItalic;

    /** Cybergram chrome face. Uses local Raj Medium when available, condensed Android fallback otherwise. */
    public static Typeface chromeRegular() {
        if (chromeRegular == null) {
            chromeRegular = loadOptionalPrivateTypeface(PRIVATE_MESSAGE_REGULAR, CHROME_FAMILY);
        }
        return chromeRegular;
    }

    /** Cybergram emphasized chrome face. Uses local Raj SemiBold when available. */
    public static Typeface chromeBold() {
        if (chromeBold == null) {
            chromeBold = loadOptionalPrivateTypeface(PRIVATE_MESSAGE_MEDIUM, CHROME_MEDIUM_FAMILY);
        }
        return chromeBold;
    }

    private static Typeface loadOptionalPrivateTypeface(String assetPath, String fallbackFamily) {
        if (ApplicationLoader.applicationContext != null) {
            try {
                return Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath);
            } catch (RuntimeException ignored) {
                // Optional private asset: local licensed-game extraction only, never required at runtime.
            }
        }
        return Typeface.create(fallbackFamily, Typeface.NORMAL);
    }

    /** Regular body face. Prefers the local private CP2077-derived test asset when present. */
    public static Typeface messageRegular() {
        if (messageRegular == null) {
            messageRegular = loadOptionalPrivateTypeface(PRIVATE_MESSAGE_REGULAR, MESSAGE_FALLBACK_FAMILY);
        }
        return messageRegular;
    }

    /** Message metadata/name face with an optional local medium-weight asset. */
    public static Typeface messageMedium() {
        if (messageMedium == null) {
            messageMedium = loadOptionalPrivateTypeface(PRIVATE_MESSAGE_MEDIUM, MESSAGE_MEDIUM_FALLBACK_FAMILY);
        }
        return messageMedium;
    }

    public static Typeface messageItalic() {
        if (messageItalic == null) {
            messageItalic = Typeface.create(messageRegular(), Typeface.ITALIC);
        }
        return messageItalic;
    }

    public static Typeface messageMediumItalic() {
        if (messageMediumItalic == null) {
            messageMediumItalic = Typeface.create(messageMedium(), Typeface.ITALIC);
        }
        return messageMediumItalic;
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
