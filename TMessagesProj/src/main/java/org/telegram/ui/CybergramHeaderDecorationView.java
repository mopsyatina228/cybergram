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
 * decoration over an {@link ActionBar}. Normal Cybergram chrome uses a restrained red
 * structural rail, a short cyan identity segment and small technical ticks/edge rails.
 * Action-mode callers can request a neutral cyan rule-only state.
 *
 * It never intercepts touch, never draws when Cybergram presentation is not active (a
 * runtime gate in {@link #onDraw}), and it uses the shared
 * {@link CybergramBubbleDrawable#buildPath} polygon for the angular marks (no duplicated
 * polygon implementation). It adds no microtext and no fake security/network claims.
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

    /** Decor states: full language, neutral cyan rule only, or nothing at all. */
    public static final int DECOR_FULL = 0;
    public static final int DECOR_RULE_ONLY = 1;
    public static final int DECOR_NONE = 2;
    private int decorState = DECOR_FULL;
    private DecorStateProvider decorStateProvider;

    /** Optional per-draw decor-state resolver (e.g. dialogs host suppresses on search/action-mode). */
    public interface DecorStateProvider {
        int get();
    }

    public void setDecorStateProvider(DecorStateProvider provider) {
        this.decorStateProvider = provider;
        invalidate();
    }

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
        int state = decorStateProvider != null ? decorStateProvider.get() : decorState;
        if (state == DECOR_NONE) {
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

        paint.setStyle(Paint.Style.FILL);
        if (state == DECOR_RULE_ONLY) {
            paint.setColor(cyanColor());
            paint.setAlpha(CybergramTheme.HEADER_TECH_ALPHA);
            canvas.drawRect(ax, ruleTop, ax + aw, bottom, paint);
            paint.setAlpha(255);
            return;
        }

        // Reference language: a restrained red structural rail under the dark header.
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(CybergramTheme.HEADER_RULE_ALPHA);
        canvas.drawRect(ax, ruleTop, ax + aw, bottom, paint);

        // Short cyan segment anchors the identity/title zone without turning the full rail neon.
        paint.setColor(cyanColor());
        paint.setAlpha(255);
        canvas.drawRect(ax + dp(52), ruleTop, ax + dp(52) + dp(42), bottom, paint);

        // Small red edge rails echo the vertical technical framing in the reference.
        paint.setColor(CybergramTheme.DANGER);
        paint.setAlpha(120);
        canvas.drawRect(ax, ruleTop - dp(10), ax + ruleH, bottom, paint);
        canvas.drawRect(ax + aw - ruleH, ruleTop - dp(10), ax + aw, bottom, paint);

        // Restrained cyan chamfered ticks keep the existing shared polygon language.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(cyanColor());
        paint.setAlpha(CybergramTheme.HEADER_TECH_ALPHA);
        int tick = dp(8);
        drawTick(canvas, ax + dp(24), ruleTop - tick, ax + dp(24) + tick, ruleTop);
        drawTick(canvas, ax + aw - dp(24) - tick, ruleTop - tick, ax + aw - dp(24), ruleTop);
        paint.setAlpha(255);
    }

    private void drawTick(Canvas canvas, float left, float top, float right, float bottom) {
        CybergramBubbleDrawable.buildPath(path, left, top, right, bottom, dp(3), dp(3), dp(3), dp(3));
        canvas.drawPath(path, paint);
    }

    private int dp(float value) {
        return (int) (AndroidUtilities.density * value + 0.5f);
    }
}
