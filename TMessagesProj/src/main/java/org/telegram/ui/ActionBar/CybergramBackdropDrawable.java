package org.telegram.ui.ActionBar;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/**
 * Cybergram default backdrop (owner ruling D4, docs/OWNER_DECISIONS_2026-09-15.md §2 D4).
 *
 * A *replaceable* default background layer: a very faint technical grid plus minimal edge
 * framing, drawn underneath the message list. It only ever decorates the built-in
 * Cybergram flat background colour, and it is only used while the user has NO wallpaper
 * of their own — {@link #isEligible(Theme.ResourcesProvider, Drawable)} is false in every
 * other case, so the caller returns the untouched upstream drawable and a user wallpaper
 * is never cropped, tinted, re-scaled or obscured.
 *
 * Deliberately a {@link ColorDrawable} subclass: {@code SizeNotifierFrameLayout.BackgroundView}
 * branches on the wallpaper drawable type, so keeping the upstream type means the existing
 * ColorDrawable draw branch (bounds + draw) is used unchanged and
 * {@code AndroidUtilities.calcDrawableColor} keeps reading a real colour.
 *
 * Stateless and allocation-free in {@link #draw(Canvas)}: the grid is a small
 * {@link BitmapShader} built once, so the chat background path gains no per-frame allocation.
 *
 * Non-interactive by construction: it is a background Drawable, not a View, so it can never
 * intercept touch, focus or accessibility.
 */
public class CybergramBackdropDrawable extends ColorDrawable {

    /** Corner of the tiled grid cell, in dp. */
    private static final float GRID_CELL_DP = 28f;
    /** Alpha (0..255) of the faint grid lines — the reference's ~0.04 pattern layer. */
    private static final int GRID_ALPHA = 10;
    /** Alpha (0..255) of the minimal red edge framing. */
    private static final int FRAME_ALPHA = 24;
    /** Inset of the edge framing from the background bounds, in dp. */
    private static final float FRAME_INSET_DP = 5f;

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect frameRect = new Rect();
    private final int frameInset;

    public CybergramBackdropDrawable(int color, float density) {
        super(color);
        float cell = Math.max(8f, GRID_CELL_DP * density);
        int cellPx = Math.max(2, (int) (cell + 0.5f));
        // ARGB tile (not ALPHA_8) so the colour is baked into the shader itself and the
        // paint alpha is a pure, unambiguous modulation on every supported API level.
        Bitmap tile = Bitmap.createBitmap(cellPx, cellPx, Bitmap.Config.ARGB_8888);
        Canvas tileCanvas = new Canvas(tile);
        Paint tilePaint = new Paint();
        // The reference's background grid is the same cyan family as the chrome, at ~0.04.
        tilePaint.setColor(CybergramTheme.CYAN);
        tilePaint.setStrokeWidth(1f);
        // One horizontal and one vertical hairline per cell: a sparse technical grid.
        tileCanvas.drawLine(0f, 0.5f, cellPx, 0.5f, tilePaint);
        tileCanvas.drawLine(0.5f, 0f, 0.5f, cellPx, tilePaint);
        gridPaint.setShader(new BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        gridPaint.setAlpha(GRID_ALPHA);

        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(Math.max(1f, density));
        framePaint.setColor(CybergramTheme.DANGER);
        framePaint.setAlpha(FRAME_ALPHA);
        frameInset = Math.max(1, (int) (FRAME_INSET_DP * density + 0.5f));
    }

    /**
     * True when the Cybergram default backdrop may decorate {@code source}. All of:
     *   - Cybergram presentation is active for {@code provider};
     *   - the active theme carries no user wallpaper override
     *     ({@code ThemeInfo.overrideWallpaper == null}) — so the default is replaceable and
     *     a user wallpaper always wins;
     *   - {@code source} is a plain {@link ColorDrawable}, i.e. the built-in Cybergram flat
     *     background colour rather than a user photo, motion or gradient wallpaper.
     */
    public static boolean isEligible(Theme.ResourcesProvider provider, Drawable source) {
        if (!(source instanceof ColorDrawable) || source instanceof CybergramBackdropDrawable) {
            return false;
        }
        if (!CybergramTheme.isCybergramPresentation(provider)) {
            return false;
        }
        try {
            Theme.ThemeInfo info = Theme.getActiveTheme();
            return info == null || info.overrideWallpaper == null;
        } catch (Throwable ignore) {
            // Read-only gate; never propagate into a draw path.
            return false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        int save = canvas.save();
        canvas.clipRect(bounds);
        canvas.drawRect(bounds, gridPaint);
        frameRect.set(bounds.left + frameInset, bounds.top + frameInset, bounds.right - frameInset, bounds.bottom - frameInset);
        if (frameRect.width() > 0 && frameRect.height() > 0) {
            canvas.drawRect(frameRect, framePaint);
        }
        canvas.restoreToCount(save);
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        float scale = alpha / 255f;
        gridPaint.setAlpha((int) (GRID_ALPHA * scale));
        framePaint.setAlpha((int) (FRAME_ALPHA * scale));
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Flat backdrop: upstream applies no colour filter to the chat background colour.
    }
}
