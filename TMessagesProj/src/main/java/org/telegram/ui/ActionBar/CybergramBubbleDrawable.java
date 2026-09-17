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
     * Shared Cybergram clipped-corner silhouette with an equal cut on all four corners.
     *
     * @param path   destination path (reset first)
     * @param left   body left edge
     * @param top    body top edge
     * @param right  body right edge
     * @param bottom body bottom edge
     * @param cut    desired corner chamfer cut in px (clamped internally)
     */
    public static void buildPath(Path path, float left, float top, float right, float bottom, float cut) {
        buildPath(path, left, top, right, bottom, cut, cut, cut, cut);
    }

    /**
     * Shared Cybergram clipped-corner silhouette with four straight sides and four
     * 45-degree chamfered corners, each with its own cut. The cut is clamped so it never
     * exceeds half of the smaller side. No arcs, no round radius. Resets {@code path}.
     *
     * Used by BOTH the standalone {@link CybergramBubbleDrawable} and Telegram's
     * {@link MessageDrawable} so there is a single implementation of the Cybergram polygon.
     *
     * @param path          destination path (reset first)
     * @param left          body left edge
     * @param top           body top edge
     * @param right         body right edge
     * @param bottom        body bottom edge
     * @param topLeftCut    top-left cut in px (clamped internally)
     * @param topRightCut   top-right cut in px (clamped internally)
     * @param bottomRightCut bottom-right cut in px (clamped internally)
     * @param bottomLeftCut bottom-left cut in px (clamped internally)
     */
    public static void buildPath(Path path, float left, float top, float right, float bottom,
                                 float topLeftCut, float topRightCut, float bottomRightCut, float bottomLeftCut) {
        path.reset();
        if (right <= left || bottom <= top) {
            return;
        }
        float maxCut = Math.min(right - left, bottom - top) * 0.5f;
        float tl = Math.min(topLeftCut, maxCut);
        float tr = Math.min(topRightCut, maxCut);
        float br = Math.min(bottomRightCut, maxCut);
        float bl = Math.min(bottomLeftCut, maxCut);
        path.moveTo(left + tl, top);
        path.lineTo(right - tr, top);
        path.lineTo(right, top + tr);
        path.lineTo(right, bottom - br);
        path.lineTo(right - br, bottom);
        path.lineTo(left + bl, bottom);
        path.lineTo(left, bottom - bl);
        path.lineTo(left, top + tl);
        path.close();
    }

    /**
     * Cybergram message silhouette with the concept's small angular corner protrusion
     * ("выступ", owner ruling D9.4, docs/OWNER_DECISIONS_2026-09-17.md).
     *
     * Three corners keep the 45-degree chamfer; the outer top corner of the speaking side grows a
     * short triangular spur that points away from the bubble (left for incoming, right for
     * outgoing), exactly as in design/references/design-target-hex-chat.jpg. The spur is limited to
     * the reserved tail region, so the polygon still stays inside the drawable bounds.
     *
     * When neither tail flag is set this is identical to the plain-chamfer overload, so callers
     * that do not want a spur are unaffected.
     *
     * @param tailLeft  add the spur to the top-left corner (incoming)
     * @param tailRight add the spur to the top-right corner (outgoing)
     * @param tailLen   spur length in px, kept inside the reserved tail region by the caller
     */
    public static void buildTailedPath(Path path, float left, float top, float right, float bottom,
                                       float topLeftCut, float topRightCut, float bottomRightCut,
                                       float bottomLeftCut,
                                       boolean tailLeft, boolean tailRight, float tailLen) {
        path.reset();
        if (right <= left || bottom <= top) {
            return;
        }
        float maxCut = Math.min(right - left, bottom - top) * 0.5f;
        float tl = Math.min(topLeftCut, maxCut);
        float tr = Math.min(topRightCut, maxCut);
        float br = Math.min(bottomRightCut, maxCut);
        float bl = Math.min(bottomLeftCut, maxCut);
        float tail = Math.max(0f, tailLen);
        if (tail <= 0f || (!tailLeft && !tailRight)) {
            buildPath(path, left, top, right, bottom, tl, tr, br, bl);
            return;
        }

        path.moveTo(left + tl, top);

        // Top edge to the top-right corner, adding the outgoing spur when requested.
        path.lineTo(right - tr, top);
        if (tailRight) {
            path.lineTo(right - tr * 0.35f, top);
            path.lineTo(right + tail, top + tr * 0.25f);
        }
        path.lineTo(right, top + tr);

        // Right, bottom and left edges keep the plain 45-degree chamfers.
        path.lineTo(right, bottom - br);
        path.lineTo(right - br, bottom);
        path.lineTo(left + bl, bottom);
        path.lineTo(left, bottom - bl);
        path.lineTo(left, top + tl);

        // Close through the top-left corner, adding the incoming spur when requested.
        if (tailLeft) {
            path.lineTo(left - tail, top + tl * 0.25f);
            path.lineTo(left + tl * 0.35f, top);
        }
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
