package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;

/**
 * B7 — the dedicated Cybergram chat-canvas HUD layer (owner-authorized 2026-09-19).
 *
 * <p>Non-interactive decoration drawn between the wallpaper and the message list: four sparse
 * chamfered corner brackets that live in the canvas edge space. It is never a surface, never carries
 * microtext or claims, and it draws nothing unless Cybergram presentation is active (runtime gate in
 * {@link #onDraw}), so a Day &lt;-&gt; Cybergram theme switch is reflected without recreating the
 * activity. The corner cut is the shared {@link CybergramTheme#BUBBLE_CORNER_CUT_DP} and the stroke is
 * the shared {@link CybergramTheme#BUBBLE_BORDER_WIDTH_DP}, so no second chamfer or stroke language is
 * introduced.
 *
 * <p>It matches {@link CybergramHeaderDecorationView}: clickable/focusable false, no accessibility
 * node, {@code onTouchEvent} returns false, and it is installed as a full-size sibling. Message
 * readability is preserved because every mark sits within {@link #EDGE_INSET_DP} of an edge.
 */
public class CybergramChatCanvasHudView extends View {

    /** Distance of the bracket line from the canvas edges, in dp — the reserved edge band. */
    private static final float EDGE_INSET_DP = 7f;
    /** Length of the straight bracket arm beside the chamfer, in dp. */
    private static final float ARM_DP = 13f;
    /** Very low mark alpha: sparse structural framing, never a competing surface. */
    private static final int MARK_ALPHA = 44;

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
