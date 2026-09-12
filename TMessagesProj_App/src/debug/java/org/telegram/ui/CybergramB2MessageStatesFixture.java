package org.telegram.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.MessageDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.ReplyMessageLine;

/**
 * B2 DEBUG-ONLY message-state probe (audit evidence, no production effect).
 *
 * Lives in the TMessagesProj_App debug source set only. It renders a deterministic bitmap from
 * REAL production renderers so message-body/reply/reaction geometry can be inspected on an
 * unauthenticated emulator:
 *
 *  - {@link MessageDrawable} — body silhouette/fill/border for TYPE_TEXT / TYPE_MEDIA / TYPE_PREVIEW,
 *    plain, selected and grouped near-corner inputs, driven through the same
 *    {@code setBounds + setTop(..., topNear, bottomNear) + draw} contract ChatMessageCell uses;
 *  - {@link ReplyMessageLine} — the real reply/quote plate + bar renderer
 *    ({@code check(null, ...)} + {@code drawBackground} + {@code drawLine});
 *  - {@link ReactionsLayoutInBubble} / {@link ReactionsLayoutInBubble.ReactionButton} — the real
 *    reaction pill layout (production {@code measure}) and drawing (production {@code drawRoundRect}).
 *
 * What this fixture is NOT: it does not compose a {@link org.telegram.ui.Cells.ChatMessageCell}.
 * It therefore cannot prove cell-level composition — background caching/clipping, reply layout,
 * time/check/view metadata, group slicing, bot buttons, forwards. Those states stay UNTESTED in the
 * B2 matrix and require an authenticated surface.
 *
 * The Cybergram presentation gate is active because the caller passes the showcase's
 * {@link org.telegram.ui.ActionBar.CybergramTheme.GeometryProvider} palette, so TYPE_TEXT/TYPE_MEDIA
 * exercise the angular path while TYPE_PREVIEW exercises the upstream theme-preview path.
 *
 * All data (text, emoji, counts) is synthesized; only the renderers are production code.
 * Geometry is expressed in dp throughout; {@link #dp(float)} is applied at the draw calls only.
 */
public final class CybergramB2MessageStatesFixture {

    /** Probe bitmap width in dp. */
    private static final int WIDTH_DP = 396;
    /** Probe bitmap height in dp (must cover the rows below). */
    private static final int HEIGHT_DP = 268;
    /** Incoming column left edge, dp. */
    private static final int IN_X_DP = 14;
    /** Outgoing column right margin, dp. */
    private static final int OUT_MARGIN_DP = 6;

    private final Theme.ResourcesProvider provider;
    private final View parentView;

    private final Bitmap bitmap;
    private final Canvas canvas;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    /** Running vertical cursor, in dp. */
    private float y;

    private CybergramB2MessageStatesFixture(Activity activity, Theme.ResourcesProvider provider) {
        this.provider = provider;
        this.parentView = new View(activity);

        bitmap = Bitmap.createBitmap(dp(WIDTH_DP), dp(HEIGHT_DP), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Theme.getColor(Theme.key_windowBackgroundWhite, provider));
    }

    /** Renders the whole probe once; the caller blits the bitmap into the showcase canvas. */
    public static Bitmap render(Activity activity, Theme.ResourcesProvider provider) {
        final CybergramB2MessageStatesFixture f = new CybergramB2MessageStatesFixture(activity, provider);
        try {
            f.drawAll();
        } catch (Throwable t) {
            // Debug harness only: record the failure on the bitmap instead of killing the showcase,
            // so the audit can report exactly which production renderer failed.
            f.drawFailure(t);
        }
        return f.bitmap;
    }

    private void drawAll() {
        legend();
        rowPlain();
        rowSelected();
        rowGrouped();
        rowMedia();
        rowPreview();
        rowReply();
        rowReactions();
    }

    private void legend() {
        text("B2 probe (production renderers): 1 plain  2 selected  3 grouped  4 media/preview  5 reply  6 reactions",
                dp(6), dp(y + 8), dp(9), Theme.getColor(Theme.key_chat_messagePanelSend, provider));
        y += 12;
    }

    private void rowPlain() {
        final float top = rowTop(1, 26);
        inBubble(IN_X_DP, top, 130, 26, "incoming", false, false, false, MessageDrawable.TYPE_TEXT);
        outBubble(top, 130, 26, "outgoing", false, false, false, MessageDrawable.TYPE_TEXT);
    }

    private void rowSelected() {
        final float top = rowTop(2, 26);
        inBubble(IN_X_DP, top, 130, 26, "selected", true, false, false, MessageDrawable.TYPE_TEXT);
        outBubble(top, 130, 26, "selected", true, false, false, MessageDrawable.TYPE_TEXT);
    }

    /** Top / middle / bottom of one group, using the production near-corner inputs. */
    private void rowGrouped() {
        final float top = rowTop(3, 42);
        for (int i = 0; i < 3; i++) {
            final float t = top + i * 14;
            final boolean topNear = i > 0;
            final boolean bottomNear = i < 2;
            inBubble(IN_X_DP, t, 130, 13, i == 1 ? "middle" : "group", false, topNear, bottomNear, MessageDrawable.TYPE_TEXT);
            outBubble(t, 130, 13, i == 1 ? "middle" : "group", false, topNear, bottomNear, MessageDrawable.TYPE_TEXT);
        }
    }

    private void rowMedia() {
        final float top = rowTop(4, 44);
        // outgoing media without caption
        mediaBubble(150, top, 100, 44, false);
        // outgoing media with caption (caption layout is cell-owned; the fixture draws only the text)
        mediaBubble(252, top, 138, 44, true);
    }

    private void rowPreview() {
        final float top = rowTop(5, 24);
        inBubble(IN_X_DP, top, 120, 24, "preview", false, false, false, MessageDrawable.TYPE_PREVIEW);
        outBubble(top, 120, 24, "preview", false, false, false, MessageDrawable.TYPE_PREVIEW);
    }

    private void rowReply() {
        final float top = rowTop(6, 38);
        replyBubble(IN_X_DP, top, 170, 38, false);
        replyBubble(WIDTH_DP - OUT_MARGIN_DP - 170, top, 170, 38, true);
    }

    private void rowReactions() {
        final float top = rowTop(7, 26);
        reactionStrip(IN_X_DP, top, false, 1, false);
        reactionStrip(96, top, true, 1, false);
        reactionStrip(178, top, false, 3, true);
    }

    /* --- production-renderer helpers (all coordinates below are dp) --- */

    private void inBubble(float x, float top, float w, float h, String label, boolean selected,
                          boolean topNear, boolean bottomNear, int type) {
        bubble(x, top, w, h, type, false, selected, topNear, bottomNear);
        final int color = Theme.getColor(selected ? Theme.key_chat_inBubbleSelected : Theme.key_chat_inBubble, provider);
        text(label, dp(x + 8), dp(top + h / 2f + 3), dp(9), contrastOn(color));
    }

    private void outBubble(float top, float w, float h, String label, boolean selected,
                           boolean topNear, boolean bottomNear, int type) {
        final float x = WIDTH_DP - OUT_MARGIN_DP - w;
        bubble(x, top, w, h, type, true, selected, topNear, bottomNear);
        final int color = Theme.getColor(selected ? Theme.key_chat_outBubbleSelected : Theme.key_chat_outBubble, provider);
        text(label, dp(x + 8), dp(top + h / 2f + 3), dp(9), contrastOn(color));
    }

    /** Production MessageDrawable through the same contract ChatMessageCell uses. */
    private void bubble(float xDp, float topDp, float wDp, float hDp, int type, boolean out, boolean selected,
                        boolean topNear, boolean bottomNear) {
        final int x = dp(xDp);
        final int top = dp(topDp);
        final int w = dp(wDp);
        final int h = dp(hDp);
        final MessageDrawable d = new MessageDrawable(type, out, selected, provider);
        d.setDrawFullBubble(true);
        d.setBounds(x, top, x + w, top + h);
        d.setTop(top, w, h, topNear, bottomNear);
        d.draw(canvas);
    }

    private void mediaBubble(float xDp, float topDp, float wDp, float hDp, boolean withCaption) {
        bubble(xDp, topDp, wDp, hDp, MessageDrawable.TYPE_MEDIA, true, false, false, false);
        final float x = dp(xDp);
        final float top = dp(topDp);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xff06090d);
        rect.set(x + dp(4), top + dp(4), x + dp(wDp) - dp(4), top + dp(hDp) - dp(withCaption ? 20 : 4));
        canvas.drawRect(rect, paint);
        text("media", x + dp(8), top + dp(16), dp(8), 0xff7c8a91);
        if (withCaption) {
            text("caption", x + dp(8), top + dp(hDp) - dp(6), dp(8), Theme.getColor(Theme.key_chat_messageTextOut, provider));
        }
    }

    /** Production ReplyMessageLine plate + bar, exactly the calls ChatMessageCell makes. */
    private void replyBubble(float xDp, float topDp, float wDp, float hDp, boolean out) {
        bubble(xDp, topDp, wDp, hDp, MessageDrawable.TYPE_TEXT, out, false, false, false);
        final ReplyMessageLine line = new ReplyMessageLine(parentView);
        final int nameColor = line.check(null, null, null, provider, ReplyMessageLine.TYPE_REPLY);
        final float x = dp(xDp);
        final float top = dp(topDp);
        rect.set(x + dp(4), top + dp(4), x + dp(wDp) - dp(4), top + dp(18));
        line.drawBackground(canvas, rect, 2f, 2f, 2f, 1f);
        line.drawLine(canvas, rect);
        text("reply", x + dp(12), top + dp(14), dp(9), nameColor);
    }

    private void reactionStrip(float xDp, float topDp, boolean chosen, int count, boolean multi) {
        final ReactionsLayoutInBubble layout = new ReactionsLayoutInBubble(parentView);
        layout.x = dp(xDp);
        layout.y = dp(topDp);
        layout.isEmpty = false;
        layout.reactionButtons.add(reactionButton(count, chosen, "\uD83D\uDC4D"));
        if (multi) {
            layout.reactionButtons.add(reactionButton(12, false, "\uD83D\uDD25"));
            layout.reactionButtons.add(reactionButton(1234, false, "\u2764"));
        }
        layout.measure(dp(WIDTH_DP), Gravity.LEFT);
        layout.draw(canvas, 1f, null);
    }

    private ReactionsLayoutInBubble.ReactionButton reactionButton(int count, boolean chosen, String emoji) {
        final TLRPC.TL_reactionEmoji reaction = new TLRPC.TL_reactionEmoji();
        reaction.emoticon = emoji;
        final TLRPC.TL_reactionCount reactionCount = new TLRPC.TL_reactionCount();
        reactionCount.reaction = reaction;
        reactionCount.count = count;
        reactionCount.chosen = chosen;
        return new ReactionsLayoutInBubble.ReactionButton(
                null, 0, parentView, reactionCount, false, false, provider);
    }

    /* --- fixture-side plumbing (not under audit) --- */

    /** Draws the row index, returns the row top in dp and advances the cursor. */
    private float rowTop(int index, int heightDp) {
        final float top = y;
        text(String.valueOf(index), dp(3), dp(top + heightDp / 2f + 3), dp(9), 0xff7c8a91);
        y += heightDp + 3;
        return top;
    }

    private void text(String value, float xPx, float baselinePx, float sizePx, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(sizePx);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(value, xPx, baselinePx, paint);
    }

    /** Debug harness only: renders the failure text instead of propagating it. */
    private void drawFailure(Throwable t) {
        text("B2 probe failed: " + t.getClass().getSimpleName() + ": " + t.getMessage(),
                dp(6), dp(20), dp(9), 0xffff2e46);
    }

    /** Picks a readable label colour for a synthesized bubble surface. */
    private static int contrastOn(int surface) {
        final double luminance = (0.299 * ((surface >> 16) & 0xff) + 0.587 * ((surface >> 8) & 0xff) + 0.114 * (surface & 0xff));
        return luminance > 140 ? 0xff101216 : 0xffe6f2f2;
    }

    private static int dp(float value) {
        return (int) (value * AndroidUtilities.density + 0.5f);
    }
}
