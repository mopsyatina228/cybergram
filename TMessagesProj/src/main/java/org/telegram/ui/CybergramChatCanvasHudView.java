package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;

/**
 * B7 — the dedicated Cybergram chat-canvas HUD layer (owner-authorized 2026-09-19).
 *
 * <p>Non-interactive decoration drawn between the wallpaper and the message list: four sparse
 * chamfered corner brackets that live in the canvas edge space. It is never a surface. Sparse neutral
 * micro-labels are allowed as decorative registration marks, but they must never imply encryption,
 * security or connection state. It draws nothing unless Cybergram presentation is active (runtime gate
 * in {@link #onDraw}), so a Day &lt;-&gt; Cybergram theme switch is reflected without recreating the
 * activity. The corner cut is the shared {@link CybergramTheme#BUBBLE_CORNER_CUT_DP} and the stroke is
 * the shared {@link CybergramTheme#BUBBLE_BORDER_WIDTH_DP}, so no second chamfer or stroke language is
 * introduced.
 *
 * <p>It matches {@link CybergramHeaderDecorationView}: clickable/focusable false, no accessibility
 * node, {@code onTouchEvent} returns false, and it is installed as a full-size sibling. Message
 * readability is preserved because every mark sits within {@link #EDGE_INSET_DP} of an edge.
 */
public class CybergramChatCanvasHudView extends View implements NotificationCenter.NotificationCenterDelegate {

    /** Distance of the bracket line from the canvas edges, in dp — the reserved edge band. */
    private static final float EDGE_INSET_DP = 7f;
    /** Length of the straight bracket arm beside the chamfer, in dp. */
    private static final float ARM_DP = 13f;
    /** Very low mark alpha: sparse structural framing, never a competing surface. */
    private static final int MARK_ALPHA = 40;
    private static final Typeface HUD_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    private final Theme.ResourcesProvider resourcesProvider;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public CybergramChatCanvasHudView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setWillNotDraw(false);
        updateVisibilityForTheme();
    }

    /**
     * Matches {@link CybergramHeaderDecorationView}: when Cybergram presentation is not active the
     * layer is {@link #GONE}, so it is skipped by the measure/draw traversal instead of being drawn
     * only to be gated away. It is restored on the global {@code didSetNewTheme} notification, so a
     * Day &lt;-&gt; Cybergram switch still needs no activity recreation; the {@link #onDraw} gate
     * stays as a safety net.
     */
    private void updateVisibilityForTheme() {
        setVisibility(CybergramTheme.isCybergramPresentation(resourcesProvider) ? VISIBLE : GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewTheme);
        updateVisibilityForTheme();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didSetNewTheme) {
            updateVisibilityForTheme();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!CybergramTheme.isCybergramPresentation(resourcesProvider)) {
            return;
        }
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        final float inset = dp(EDGE_INSET_DP);
        final float cut = dp(CybergramTheme.BUBBLE_CORNER_CUT_DP);
        final float arm = dp(ARM_DP);
        final float l = inset;
        final float t = inset;
        final float r = w - inset;
        final float b = h - inset;
        if (r - l <= 4 * cut || b - t <= 4 * cut) {
            return;
        }

        // Faint dual-colour field: enough structure to break the flat wallpaper, but deliberately
        // weaker than message outlines and text.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(CybergramTheme.CYAN);
        paint.setAlpha(CybergramTheme.CANVAS_GRID_ALPHA);
        final float gridX = dp(CybergramTheme.CANVAS_GRID_X_DP);
        for (float x = l + gridX; x < r; x += gridX) {
            canvas.drawLine(x, t, x, b, paint);
        }
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(Math.max(1, CybergramTheme.CANVAS_GRID_ALPHA - 2));
        final float gridY = dp(CybergramTheme.CANVAS_GRID_Y_DP);
        for (float y = t + gridY; y < b; y += gridY) {
            canvas.drawLine(l, y, r, y, paint);
        }

        // Reference-density registration details. These are deliberately neutral identifiers rather
        // than fake connection/encryption claims, and remain faint enough to disappear behind text.
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(HUD_TYPEFACE);
        paint.setTextSize(dp(CybergramTheme.CANVAS_MICRO_TEXT_DP));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(CybergramTheme.CYAN);
        paint.setAlpha(CybergramTheme.CANVAS_MICRO_ALPHA);
        canvas.drawText("CG // GRID 03", l + dp(6f), t + dp(15f), paint);
        canvas.drawText("NODE // A7", l + dp(6f), b - dp(15f), paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(Math.max(1, CybergramTheme.CANVAS_MICRO_ALPHA - 6));
        canvas.drawText("SECTOR // C4", r - dp(6f), t + dp(48f), paint);
        canvas.drawText("FRAME // 12", r - dp(6f), b - dp(48f), paint);
        paint.setTypeface(null);
        paint.setTextAlign(Paint.Align.LEFT);

        // Short cyan/red registration strokes break the empty edge band without becoming rails.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(0.45f)));
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(CybergramTheme.CYAN);
        paint.setAlpha(CybergramTheme.CANVAS_TECH_ALPHA);
        canvas.drawLine(l + dp(6f), t + dp(22f), l + dp(38f), t + dp(22f), paint);
        canvas.drawLine(r - dp(46f), b - dp(22f), r - dp(6f), b - dp(22f), paint);
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(Math.max(1, CybergramTheme.CANVAS_TECH_ALPHA - 8));
        canvas.drawLine(l + dp(6f), t + dp(64f), l + dp(20f), t + dp(64f), paint);
        canvas.drawLine(r - dp(24f), b - dp(64f), r - dp(6f), b - dp(64f), paint);

        // Segmented red rails: the reference uses structural fragments, not two continuous bars.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(CybergramTheme.DANGER);
        final float railGlow = Math.max(1f, dp(1.5f));
        final float railCore = Math.max(1f, dp(0.5f));
        final float railStep = dp(82f);
        final float railSegment = dp(46f);
        for (float y = t; y < b; y += railStep) {
            final float y2 = Math.min(b, y + railSegment);
            paint.setAlpha(CybergramTheme.CANVAS_RAIL_GLOW_ALPHA);
            canvas.drawRect(l - railGlow, y, l + railGlow, y2, paint);
            paint.setAlpha(CybergramTheme.CANVAS_RAIL_ALPHA);
            canvas.drawRect(l, y, l + railCore, y2, paint);
        }
        for (float y = t + dp(29f); y < b; y += railStep) {
            final float y2 = Math.min(b, y + railSegment);
            paint.setAlpha(CybergramTheme.CANVAS_RAIL_GLOW_ALPHA);
            canvas.drawRect(r - railGlow, y, r + railGlow, y2, paint);
            paint.setAlpha(CybergramTheme.CANVAS_RAIL_ALPHA);
            canvas.drawRect(r - railCore, y, r, y2, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(CybergramTheme.BUBBLE_BORDER_WIDTH_DP)));
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(MARK_ALPHA);

        path.rewind();
        // Four brackets, each: straight arm -> shared 45-degree chamfer -> straight arm. Edge space only.
        path.moveTo(l, t + cut + arm);
        path.lineTo(l, t + cut);
        path.lineTo(l + cut, t);
        path.lineTo(l + cut + arm, t);

        path.moveTo(r - cut - arm, t);
        path.lineTo(r - cut, t);
        path.lineTo(r, t + cut);
        path.lineTo(r, t + cut + arm);

        path.moveTo(r, b - cut - arm);
        path.lineTo(r, b - cut);
        path.lineTo(r - cut, b);
        path.lineTo(r - cut - arm, b);

        path.moveTo(l + cut + arm, b);
        path.lineTo(l + cut, b);
        path.lineTo(l, b - cut);
        path.lineTo(l, b - cut - arm);

        canvas.drawPath(path, paint);
        paint.setAlpha(255);
    }

    private float dp(float value) {
        return AndroidUtilities.density * value;
    }
}
