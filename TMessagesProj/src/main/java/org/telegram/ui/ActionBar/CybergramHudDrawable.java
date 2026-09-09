package org.telegram.ui.ActionBar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * Reusable Cybergram HUD panel primitive.
 *
 * Draws a rectangular panel with 45-degree chamfered corners (no arcs, no round radius —
 * the exact same {@link CybergramBubbleDrawable#buildPath} polygon used by the message
 * silhouette, so the shared implementation is never duplicated). Fill and stroke are
 * independently optional, corner cuts can be set per-corner, and the stroke is MITER /
 * SQUARE. It is a pure {@link Drawable}: no touch/event handling, no layout, no Cyberpunk
 * assets anywhere.
 *
 * The drawable alpha contract is order-independent: base colours are stored separately and
 * every colour change re-applies the saved drawable alpha, so
 * {@code setAlpha(96); setStrokeColor(0xff00e5ff)} and
 * {@code setStrokeColor(0xff00e5ff); setAlpha(96)} yield the same paint.
 *
 * Intended to be composed by surfaces that want a Cybergram frame — e.g. the composer's
 * text-field frame and the chat header's structural decoration — rather than each surface
 * re-implementing the chamfered polygon.
 */
public class CybergramHudDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private boolean fillEnabled;
    private boolean strokeEnabled;
    private int fillColor;      // base ARGB (its own alpha preserved)
    private int strokeColor;    // base ARGB (its own alpha preserved)
    private float cornerCut;
    private float topLeftCut = -1;
    private float topRightCut = -1;
    private float bottomRightCut = -1;
    private float bottomLeftCut = -1;
    private float strokeWidthPx;
    private int alpha = 255;    // drawable alpha, clamped 0..255

    public CybergramHudDrawable() {
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
        strokePaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    /**
     * Recomputes the fill/stroke paint colours so the drawable alpha is always applied on
     * top of the base colour's own alpha, regardless of the order of {@code set*} /
     * {@code setAlpha} calls. {@code effectiveAlpha = Color.alpha(baseColor) * alpha / 255}.
     */
    private void updatePaintColors() {
        int fa = alpha <= 0 ? 0 : (Color.alpha(fillColor) * alpha / 255);
        fillPaint.setColor((fa << 24) | (fillColor & 0x00FFFFFF));
        int sa = alpha <= 0 ? 0 : (Color.alpha(strokeColor) * alpha / 255);
        strokePaint.setColor((sa << 24) | (strokeColor & 0x00FFFFFF));
        invalidateSelf();
    }

    /** Enables the fill and sets its base colour. */
    public CybergramHudDrawable setFillColor(@ColorInt int color) {
        this.fillColor = color;
        this.fillEnabled = true;
        updatePaintColors();
        return this;
    }

    /** Enables the stroke and sets its base colour. */
    public CybergramHudDrawable setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        this.strokeEnabled = true;
        updatePaintColors();
        return this;
    }

    /** Sets / disables the fill (pass 0 to disable). */
    public CybergramHudDrawable setFill(@ColorInt int color, boolean enabled) {
        this.fillColor = color;
        this.fillEnabled = enabled;
        updatePaintColors();
        return this;
    }

    /** Sets / disables the stroke. */
    public CybergramHudDrawable setStroke(@ColorInt int color, float widthPx, boolean enabled) {
        this.strokeColor = color;
        this.strokeWidthPx = Math.max(0f, widthPx);
        strokePaint.setStrokeWidth(this.strokeWidthPx);
        this.strokeEnabled = enabled;
        updatePaintColors();
        return this;
    }

    /** Sets a uniform corner chamfer cut (px); resets any per-corner overrides. */
    public CybergramHudDrawable setCornerCut(float cutPx) {
        this.cornerCut = Math.max(0f, cutPx);
        this.topLeftCut = this.topRightCut = this.bottomRightCut = this.bottomLeftCut = -1;
        invalidateSelf();
        return this;
    }

    /** Sets independent per-corner chamfer cuts (px). Any value &lt; 0 uses the uniform cut. */
    public CybergramHudDrawable setCornerCuts(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.topLeftCut = topLeft;
        this.topRightCut = topRight;
        this.bottomRightCut = bottomRight;
        this.bottomLeftCut = bottomLeft;
        invalidateSelf();
        return this;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        updatePaintColors();
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        float tl = topLeftCut < 0 ? cornerCut : topLeftCut;
        float tr = topRightCut < 0 ? cornerCut : topRightCut;
        float br = bottomRightCut < 0 ? cornerCut : bottomRightCut;
        float bl = bottomLeftCut < 0 ? cornerCut : bottomLeftCut;
        CybergramBubbleDrawable.buildPath(path, bounds.left, bounds.top, bounds.right, bounds.bottom, tl, tr, br, bl);
        if (fillEnabled && fillPaint.getAlpha() > 0) {
            canvas.drawPath(path, fillPaint);
        }
        if (strokeEnabled && strokeWidthPx > 0 && strokePaint.getAlpha() > 0) {
            canvas.drawPath(path, strokePaint);
        }
    }
}
