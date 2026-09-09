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
        path.reset();
        if (bounds.isEmpty()) {
            return;
        }

        float inset = strokeWidthPx * 0.5f;
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset;

        if (right <= left || bottom <= top) {
            return;
        }

        float cut = Math.min(cornerCutPx, Math.min(right - left, bottom - top) * 0.5f);

        path.moveTo(left + cut, top);
        path.lineTo(right - cut, top);
        path.lineTo(right, top + cut);
        path.lineTo(right, bottom - cut);
        path.lineTo(right - cut, bottom);
        path.lineTo(left + cut, bottom);
        path.lineTo(left, bottom - cut);
        path.lineTo(left, top + cut);
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
