package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.CybergramBubbleDrawable;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;

/**
 * Production non-interactive presentation overlay that draws Cybergram structural
 * decoration over the chat {@link ActionBar}: a thin cyan bottom rule, a short amber accent
 * segment near the identity/title zone, and a couple of very restrained chamfered corner
 * ticks.
 *
 * It never intercepts touch, never draws when Cybergram presentation is not active (a
 * runtime gate in {@link #onDraw}), and it uses the shared
 * {@link CybergramBubbleDrawable#buildPath} polygon for the angular marks (no duplicated
 * polygon implementation). It reads the cyan colour from a themed action-bar/icon key and
 * takes amber from {@link CybergramTheme#AMBER}. It adds no microtext and no fake
 * "SECURE"/ID badges.
 *
 * It is always present in the view hierarchy so a Day -&gt; Cybergram (or back) theme switch is
 * reflected without recreating the activity: the gate re-evaluates on every draw. When idle
 * it is an inert, non-clickable, non-focusable, accessibility-hidden overlay.
 *
 * Placed as a full-size sibling over the action bar (top z-order); its onDraw measures the
 * action bar's window position so it follows the header regardless of hierarchy.
 */
public class CybergramHeaderDecorationView extends View {

    private final ActionBar actionBar;
    private final Theme.ResourcesProvider resourcesProvider;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final int[] actionBarLoc = new int[2];
    private final int[] selfLoc = new int[2];

    public CybergramHeaderDecorationView(Context context, ActionBar actionBar, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.actionBar = actionBar;
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

    private int cyanColor() {
        if (resourcesProvider != null) {
            return resourcesProvider.getColor(Theme.key_actionBarDefaultIcon);
        }
        return Theme.getColor(Theme.key_actionBarDefaultIcon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (actionBar == null || !CybergramTheme.isCybergramPresentation(resourcesProvider)) {
            return;
        }
        actionBar.getLocationInWindow(actionBarLoc);
        getLocationInWindow(selfLoc);
        float ax = actionBarLoc[0] - selfLoc[0];
        float ay = actionBarLoc[1] - selfLoc[1];
        float aw = actionBar.getWidth();
        float ah = actionBar.getHeight();
        if (aw <= 0 || ah <= 0) {
            return;
        }
        float bottom = ay + ah;
        int ruleH = dp(1);
        float ruleTop = bottom - ruleH;

        // 1dp cyan bottom rule
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cyanColor());
        canvas.drawRect(ax, ruleTop, ax + aw, bottom, paint);

        // short amber accent segment near the identity/title zone (sits below the title text)
        paint.setColor(CybergramTheme.AMBER);
        canvas.drawRect(ax + dp(52), ruleTop, ax + dp(52) + dp(40), bottom, paint);

        // two very restrained chamfered corner ticks in the lower corners of the header
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(cyanColor());
        int tick = dp(8);
        drawTick(canvas, ax + dp(24), ruleTop - tick, ax + dp(24) + tick, ruleTop);
        drawTick(canvas, ax + aw - dp(24) - tick, ruleTop - tick, ax + aw - dp(24), ruleTop);
    }

    private void drawTick(Canvas canvas, float left, float top, float right, float bottom) {
        CybergramBubbleDrawable.buildPath(path, left, top, right, bottom, dp(3), dp(3), dp(3), dp(3));
        canvas.drawPath(path, paint);
    }

    private int dp(float value) {
        return (int) (AndroidUtilities.density * value + 0.5f);
    }
}
