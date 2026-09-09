package org.telegram.ui.ActionBar;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Small Cybergram-owned drawing primitive for angular HUD-style panels.
 *
 * This class is intentionally independent from Telegram's MessageDrawable for
 * now. It lets the project stabilize the Cybergram silhouette before wiring it
 * into the considerably more complex grouped-message/media rendering path.
 */
public final class CybergramBubbleDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private float cornerCutPx;
    private float strokeWidthPx;

    public CybergramBubbleDrawable(
            @ColorInt int fillColor,
            @ColorInt int strokeColor,
            float cornerCutPx,
            float strokeWidthPx
    ) {
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
        strokePaint.setStrokeCap(Paint.Cap.SQUARE);
        strokePaint.setColor(strokeColor);

        this.cornerCutPx = Math.max(0f, cornerCutPx);
        this.strokeWidthPx = Math.max(0f, strokeWidthPx);
        strokePaint.setStrokeWidth(this.strokeWidthPx);
    }

    public void setFillColor(@ColorInt int color) {
        if (fillPaint.getColor() == color) {
            return;
        }
        fillPaint.setColor(color);
        invalidateSelf();
    }

    public void setStrokeColor(@ColorInt int color) {
        if (strokePaint.getColor() == color) {
            return;
        }
        strokePaint.setColor(color);
        invalidateSelf();
    }

    public void setCornerCut(float cornerCutPx) {
        float value = Math.max(0f, cornerCutPx);
        if (this.cornerCutPx == value) {
            return;
        }
        this.cornerCutPx = value;
        rebuildPath(getBounds());
        invalidateSelf();
    }

    public void setStrokeWidth(float strokeWidthPx) {
        float value = Math.max(0f, strokeWidthPx);
        if (this.strokeWidthPx == value) {
            return;
        }
        this.strokeWidthPx = value;
        strokePaint.setStrokeWidth(value);
        rebuildPath(getBounds());
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        rebuildPath(bounds);
    }

    private void rebuildPath(Rect bounds) {
        if (bounds.isEmpty()) {
            path.reset();
            return;
        }

        float inset = strokeWidthPx * 0.5f;
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset;

        if (right <= left || bottom <= top) {
            path.reset();
            return;
        }

        buildPath(path, left, top, right, bottom, cornerCutPx);
    }

    /**
     * Shared Cybergram clipped-corner silhouette. Four straight sides with four
     * 45-degree chamfered corners (no arcs, no round radius). The corner cut is
     * clamped so it never exceeds half of the smaller side.
     *
     * Used by BOTH the standalone {@link CybergramBubbleDrawable} and Telegram's
     * {@link MessageDrawable} so there is a single implementation of the Cybergram
     * polygon. Resets {@code path} before building.
     *
     * @param path   destination path (reset first)
     * @param left   body left edge
     * @param top    body top edge
     * @param right  body right edge
     * @param bottom body bottom edge
     * @param cut    desired corner chamfer cut in px (clamped internally)
     */
    public static void buildPath(Path path, float left, float top, float right, float bottom, float cut) {
        path.reset();
        if (right <= left || bottom <= top) {
            return;
        }
        float safeCut = Math.min(cut, Math.min(right - left, bottom - top) * 0.5f);
        path.moveTo(left + safeCut, top);
        path.lineTo(right - safeCut, top);
        path.lineTo(right, top + safeCut);
        path.lineTo(right, bottom - safeCut);
        path.lineTo(right - safeCut, bottom);
        path.lineTo(left + safeCut, bottom);
        path.lineTo(left, bottom - safeCut);
        path.lineTo(left, top + safeCut);
        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (path.isEmpty()) {
            return;
        }
        canvas.drawPath(path, fillPaint);
        if (strokeWidthPx > 0f && strokePaint.getAlpha() > 0) {
            canvas.drawPath(path, strokePaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
        invalidateSelf();
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
}
