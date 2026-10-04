package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;

public class SenderSelectView extends View {
    private final static float SPRING_MULTIPLIER = 100f;
    private final static FloatPropertyCompat<SenderSelectView> MENU_PROGRESS = new SimpleFloatPropertyCompat<SenderSelectView>("menuProgress", obj -> obj.menuProgress, (obj, value) -> {
        obj.menuProgress = value;
        obj.invalidate();
    }).setMultiplier(SPRING_MULTIPLIER);

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private Drawable selectorDrawable;
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint menuPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cybergramFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path cybergramPath = new Path();
    private final RectF cybergramRect = new RectF();

    private SpringAnimation menuSpring;
    private ValueAnimator menuAnimator;
    private float menuProgress;
    private boolean scaleOut;
    private boolean scaleIn;

    public SenderSelectView(Context context) {
        super(context);
        avatarImage.setRoundRadius(CybergramTheme.isCybergramPresentation(null) ? 0 : AndroidUtilities.dp(28));
        menuPaint.setStrokeWidth(AndroidUtilities.dp(2));
        menuPaint.setStrokeCap(Paint.Cap.ROUND);
        menuPaint.setStyle(Paint.Style.STROKE);
        updateColors();
        setContentDescription(LocaleController.formatString("AccDescrSendAsPeer", R.string.AccDescrSendAsPeer, ""));
    }

    private void updateColors() {
        backgroundPaint.setColor(Theme.getColor(Theme.key_chat_messagePanelVoiceBackground));
        menuPaint.setColor(Theme.getColor(Theme.key_chat_messagePanelVoicePressed));
        selectorDrawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(18), Color.TRANSPARENT, Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhite), 0.2f));
        selectorDrawable.setCallback(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        avatarImage.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        avatarImage.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(getLayoutParams().width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getLayoutParams().height, MeasureSpec.EXACTLY));
        avatarImage.setImageCoords(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        float sc;
        if (scaleOut) {
            sc = 1f - menuProgress;
        } else if (scaleIn) {
            sc = menuProgress;
        } else {
            sc = 1f;
        }
        canvas.scale(sc, sc, getWidth() / 2f, getHeight() / 2f);

        super.onDraw(canvas);

        final boolean cybergram = CybergramTheme.isCybergramPresentation(null);
        if (cybergram) {
            cybergramRect.set(0, 0, getWidth(), getHeight());
            CybergramTheme.buildInteractionPanelPath(
                    cybergramPath, cybergramRect, CybergramTheme.MESSAGE_AVATAR_CUT_DP);
            final int avatarSave = canvas.save();
            canvas.clipPath(cybergramPath);
            avatarImage.draw(canvas);
            CybergramTheme.drawAnalogDisplayOverlay(canvas, cybergramRect, cybergramPath);
            canvas.restoreToCount(avatarSave);

            cybergramFramePaint.setStyle(Paint.Style.STROKE);
            cybergramFramePaint.setStrokeJoin(Paint.Join.MITER);
            cybergramFramePaint.setStrokeWidth(AndroidUtilities.dpf2(CybergramTheme.MESSAGE_AVATAR_FRAME_WIDTH_DP));
            cybergramFramePaint.setColor(CybergramTheme.CYAN);
            cybergramFramePaint.setAlpha(CybergramTheme.MESSAGE_AVATAR_FRAME_ALPHA);
            canvas.drawPath(cybergramPath, cybergramFramePaint);
        } else {
            avatarImage.draw(canvas);
        }

        int alpha = (int) (menuProgress * 0xFF);
        backgroundPaint.setAlpha(alpha);
        if (cybergram) {
            canvas.drawPath(cybergramPath, backgroundPaint);
        } else {
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, Math.min(getWidth(), getHeight()) / 2f, backgroundPaint);
        }

        canvas.save();
        menuPaint.setAlpha(alpha);
        float padding = AndroidUtilities.dp(10) + menuPaint.getStrokeWidth();
        canvas.drawLine(padding, padding, getWidth() - padding, getHeight() - padding, menuPaint);
        canvas.drawLine(padding, getHeight() - padding, getWidth() - padding, padding, menuPaint);
        canvas.restore();

        selectorDrawable.setBounds(0, 0, getWidth(), getHeight());
        selectorDrawable.draw(canvas);

        canvas.restore();
    }

    /**
     * Sets new User or Chat to be bound as the avatar
     * @param obj User or chat
     */
    public void setAvatar(TLObject obj) {
        String objName = "";
        if (obj instanceof TLRPC.User) {
            objName = UserObject.getFirstName((TLRPC.User) obj);
        } else if (obj instanceof TLRPC.Chat) {
            objName = ((TLRPC.Chat) obj).title;
        } else if (obj instanceof TLRPC.ChatInvite) {
            objName = ((TLRPC.ChatInvite) obj).title;
        }
        setContentDescription(LocaleController.formatString("AccDescrSendAsPeer", R.string.AccDescrSendAsPeer, objName));
        avatarDrawable.setInfo(obj);
        avatarImage.setForUserOrChat(obj, avatarDrawable);
    }

    /**
     * Sets new animation progress
     * @param progress New progress
     */
    public void setProgress(float progress) {
        setProgress(progress, true);
    }

    /**
     * Sets new animation progress
     * @param progress New progress
     * @param animate If we should animate
     */
    public void setProgress(float progress, boolean animate) {
        setProgress(progress, animate, progress != 0f);
    }

    /**
     * Sets new animation progress
     * @param progress New progress
     * @param animate If we should animate
     * @param useSpring If we should use spring instead of ValueAnimator
     */
    public void setProgress(float progress, boolean animate, boolean useSpring) {
        if (animate) {
            if (menuSpring != null) {
                menuSpring.cancel();
            }
            if (menuAnimator != null) {
                menuAnimator.cancel();
            }
            scaleIn = false;
            scaleOut = false;

            if (useSpring) {

                float startValue = menuProgress * SPRING_MULTIPLIER;
                menuSpring = new SpringAnimation(this, MENU_PROGRESS).setStartValue(startValue);
                boolean reverse = progress < menuProgress;
                float finalPos = progress * SPRING_MULTIPLIER;

                scaleIn = reverse;
                scaleOut = !reverse;

                menuSpring.setSpring(new SpringForce(finalPos)
                        .setFinalPosition(finalPos)
                        .setStiffness(450f)
                        .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY));
                menuSpring.addUpdateListener((animation, value, velocity) -> {
                    if (reverse ? value <= startValue / 2f && scaleIn : value >= finalPos / 2f && scaleOut) {
                        scaleIn = !reverse;
                        scaleOut = reverse;
                    }
                });
                menuSpring.addEndListener((animation, canceled, value, velocity) -> {
                    scaleIn = false;
                    scaleOut = false;

                    if (!canceled) {
                        animation.cancel();
                    }
                    if (animation == menuSpring) {
                        menuSpring = null;
                    }
                });
                menuSpring.start();
            } else {
                menuAnimator = ValueAnimator.ofFloat(menuProgress, progress).setDuration(200);
                menuAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                menuAnimator.addUpdateListener(animation -> {
                    menuProgress = (float) animation.getAnimatedValue();
                    invalidate();
                });
                menuAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animation == menuAnimator) {
                            menuAnimator = null;
                        }
                    }
                });
                menuAnimator.start();
            }
        } else {
            menuProgress = progress;
            invalidate();
        }
    }

    /**
     * @return Current animation progress
     */
    public float getProgress() {
        return menuProgress;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || selectorDrawable == who;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        selectorDrawable.setState(getDrawableState());
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        selectorDrawable.jumpToCurrentState();
    }
}
