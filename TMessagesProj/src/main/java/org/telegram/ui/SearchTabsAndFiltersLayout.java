package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;

public class SearchTabsAndFiltersLayout extends FrameLayout implements Theme.Colorable {
    private final Path clipPath = new Path();
    private final Theme.ResourcesProvider resourcesProvider;
    private BlurredBackgroundDrawable blurredBackgroundDrawable;

    public SearchTabsAndFiltersLayout(@NonNull Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
    }

    private boolean useCybergramPresentation() {
        return CybergramTheme.isCybergramPresentation(resourcesProvider);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildClipPath(w, h);
    }

    private void rebuildClipPath(int w, int h) {
        clipPath.rewind();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (useCybergramPresentation()) {
            clipPath.addRect(0, 0, w, h, Path.Direction.CW);
        } else {
            clipPath.addRoundRect(dp(9), dp(9), w - dp(9), h - dp(9),
                    dp(16), dp(16), Path.Direction.CW);
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public void setBlurredBackground(BlurredBackgroundDrawable drawable) {
        blurredBackgroundDrawable = drawable;
        applyPresentationBackground();
    }

    private void applyPresentationBackground() {
        setBackground(useCybergramPresentation() ? null : blurredBackgroundDrawable);
    }

    @Override
    public void updateColors() {
        if (blurredBackgroundDrawable != null) {
            blurredBackgroundDrawable.updateColors();
        }
        applyPresentationBackground();
        rebuildClipPath(getWidth(), getHeight());
        invalidate();
    }
}
