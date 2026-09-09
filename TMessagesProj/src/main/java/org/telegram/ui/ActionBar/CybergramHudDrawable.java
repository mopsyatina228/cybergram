package org.telegram.ui.ActionBar;

import android.graphics.Canvas;
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
    private int fillColor;
    private int strokeColor;
    private float cornerCut;
    private float topLeftCut = -1;
    private float topRightCut = -1;
    private float bottomRightCut = -1;
    private float bottomLeftCut = -1;
    private float strokeWidthPx;
    private int alpha = 255;

    public CybergramHudDrawable() {
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
        strokePaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    /** Enables the fill and sets its colour. */
    public CybergramHudDrawable setFillColor(@ColorInt int color) {
        this.fillColor = color;
        this.fillEnabled = true;
        fillPaint.setColor(color);
        invalidateSelf();
        return this;
    }

    /** Enables the stroke and sets its colour. */
    public CybergramHudDrawable setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        this.strokeEnabled = true;
        strokePaint.setColor(color);
        invalidateSelf();
        return this;
    }

    /** Sets / disables the fill (pass 0 to disable). */
    public CybergramHudDrawable setFill(@ColorInt int color, boolean enabled) {
        this.fillColor = color;
        this.fillEnabled = enabled;
        fillPaint.setColor(color);
        invalidateSelf();
        return this;
    }

    /** Sets / disables the stroke. */
    public CybergramHudDrawable setStroke(@ColorInt int color, float widthPx, boolean enabled) {
        this.strokeColor = color;
        this.strokeWidthPx = Math.max(0f, widthPx);
        strokePaint.setStrokeWidth(this.strokeWidthPx);
        strokePaint.setColor(color);
        this.strokeEnabled = enabled;
        invalidateSelf();
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
        this.alpha = alpha;
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
        invalidateSelf();
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
        if (fillEnabled && fillPaint.getAlpha() > 0 && alpha > 0) {
            canvas.drawPath(path, fillPaint);
        }
        if (strokeEnabled && strokeWidthPx > 0 && strokePaint.getAlpha() > 0 && alpha > 0) {
            canvas.drawPath(path, strokePaint);
        }
    }
}
