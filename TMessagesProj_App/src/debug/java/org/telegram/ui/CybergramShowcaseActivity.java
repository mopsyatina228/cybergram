package org.telegram.ui;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.MessageDrawable;
import org.telegram.ui.ActionBar.Theme;

/**
 * DEBUG-ONLY visual showcase for Cybergram UI iteration.
 *
 * Not a user-facing feature. Lives in the debug source set of TMessagesProj_App and is
 * declared in the debug manifests only, so it is never compiled or exported in a release
 * build. Opened via an explicit `adb` intent:
 *
 *   adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity
 *
 * Self-contained and offline: it does not touch accounts, MessagesController, network,
 * storage or preferences, and requires no Telegram login. Message bubbles are drawn with
 * the real production {@link MessageDrawable} so that any change to the production bubble
 * geometry shows up here immediately. Colours are read through {@link Theme#getColor(int)}
 * with the Cybergram .attheme keys; a small, clearly-labelled DEBUG fallback map is used only
 * for keys the Cybergram palette objectively does not define.
 */
public class CybergramShowcaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ShowcaseView(this));
    }

    /** Debug-only fallback colours for theme keys the Cybergram .attheme does not define. */
    private static final class Db {
        static final int TITLE_TEXT = 0xffe6f2f2;      // actionBarTitleColor absent -> header title
        static final int SUBTITLE_MUTED = 0xff7c8a91;  // muted subtitle / time / status text
        static final int ACCENT_CYAN = 0xff00e5ff;     // reply accent line, media glyph
        static final int IN_TEXT = 0xff06090d;         // message text on amber incoming bubble
        static final int OUT_TEXT = 0xffd7f7ff;        // message text on dark outgoing bubble
        static final int MEDIA_INNER = 0xff06090d;     // media placeholder inner background

        private Db() {}
    }

    private static final class ShowcaseView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private float d;

        ShowcaseView(Activity activity) {
            super(activity);
            d = AndroidUtilities.density;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Theme.ThemeInfo theme = Theme.getCurrentTheme();
            boolean dark = theme != null && theme.isDark();
            String themeName = theme != null ? theme.name : "Unknown";

            int headerBg = Theme.getColor(Theme.key_actionBarDefault);
            int headerIcon = Theme.getColor(Theme.key_actionBarDefaultIcon);
            int composerBg = Theme.getColor(Theme.key_chat_messagePanelBackground);
            int sendCol = Theme.getColor(Theme.key_chat_messagePanelSend);
            int winBg = Theme.getColor(Theme.key_windowBackgroundWhite);
            int chatIn = Theme.getColor(Theme.key_chat_inBubble);
            int chatOut = Theme.getColor(Theme.key_chat_outBubble);

            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            // background
            canvas.drawColor(winBg);

            drawHeader(canvas, w, headerBg, headerIcon);
            drawChat(canvas, w, themeName, dark);
            drawComposer(canvas, w, h, composerBg, sendCol);
            drawDebugStrip(canvas, w, themeName, dark, winBg, chatIn, chatOut);
        }

        private void drawHeader(Canvas canvas, int w, int headerBg, int headerIcon) {
            int bar = dp(52);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(headerBg);
            canvas.drawRect(0, 0, w, bar, paint);

            // back arrow
            paint.setStrokeWidth(dp(2.2f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(headerIcon);
            path.reset();
            path.moveTo(dp(20), dp(26));
            path.lineTo(dp(26), dp(20));
            path.lineTo(dp(32), dp(26));
            canvas.drawPath(path, paint);

            // avatar placeholder
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(headerIcon);
            canvas.drawCircle(dp(46), dp(26), dp(16), paint);
            paint.setColor(headerBg);
            canvas.drawCircle(dp(46), dp(26), dp(13.5f), paint);
            paint.setColor(headerIcon);
            canvas.drawCircle(dp(46), dp(26), dp(6.5f), paint);

            // name + subtitle
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Db.TITLE_TEXT);
            paint.setTextSize(dp(16));
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("NIGHT CITY RELAY", dp(72), dp(23), paint);
            paint.setColor(Db.SUBTITLE_MUTED);
            paint.setTextSize(dp(11));
            canvas.drawText("online", dp(72), dp(38), paint);

            // right icons: search + menu
            paint.setStrokeWidth(dp(2.2f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(headerIcon);
            float cx = w - dp(24);
            canvas.drawCircle(cx, dp(20), dp(6), paint);
            canvas.drawLine(cx - dp(4), dp(24), cx - dp(8), dp(28), paint);
            cx = w - dp(54);
            canvas.drawLine(cx - dp(5), dp(19), cx + dp(5), dp(19), paint);
            canvas.drawLine(cx - dp(5), dp(26), cx + dp(5), dp(26), paint);
            canvas.drawLine(cx - dp(5), dp(33), cx + dp(5), dp(33), paint);
        }

        private void drawChat(Canvas canvas, int w, String themeName, boolean dark) {
            float top = dp(52) + dp(26);
            float leftPad = dp(12);
            float rightPad = w - dp(12);

            // service / date label
            String dateLabel = "NIGHT CITY — 09/09/2026";
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Theme.getColor(Theme.key_chat_serviceBackground));
            paint.setTextSize(dp(12));
            float labelW = paint.measureText(dateLabel) + dp(24);
            rect.set((w - labelW) / 2f, top + dp(10), (w + labelW) / 2f, top + dp(30));
            canvas.drawRoundRect(rect, dp(12), dp(12), paint);
            paint.setColor(Theme.getColor(Theme.key_chat_serviceText));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(dateLabel, w / 2f, top + dp(23), paint);

            float y = top + dp(40);
            float maxBubbleW = w - dp(24) - dp(64);

            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.", false, 0);
            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.\nМультистрочный пример,\nчтобы оценить перенос текста.", false, 1);
            y = drawOutgoingBubble(canvas, w, leftPad, rightPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.", false, 0);
            y = drawOutgoingBubble(canvas, w, leftPad, rightPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.\nДлинное исходящее сообщение\nс временем и статусом.", true, 1);
            drawMediaBubble(canvas, w, y);
        }

        private float drawIncomingBubble(Canvas canvas, float leftPad, float y, float maxW,
                                         String text, boolean selected, int kind) {
            paint.setTextSize(dp(14));
            paint.setTextAlign(Paint.Align.LEFT);
            float bw = paint.measureText(text) + dp(24);
            if (bw > maxW) bw = maxW;
            int lines = countLines(text, bw - dp(24));
            float bh = dp(20) * lines + dp(16);

            MessageDrawable d = new MessageDrawable(MessageDrawable.TYPE_TEXT, false, selected);
            d.setBounds(dp(12), (int) y, (int) (dp(12) + bw), (int) (y + bh));
            d.setRoundRadius(dp(12));
            d.draw(canvas);

            if (kind == 1) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Db.ACCENT_CYAN);
                canvas.drawRect(dp(12) + dp(7), y + dp(9), dp(12) + dp(10), y + bh - dp(9), paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Db.IN_TEXT);
            canvas.drawText(text, dp(12) + dp(12), y + dp(22), paint);
            return y + bh + dp(16);
        }

        private float drawOutgoingBubble(Canvas canvas, int w, float leftPad, float rightPad, float y,
                                         float maxW, String text, boolean selected, int withTime) {
            paint.setTextSize(dp(14));
            paint.setTextAlign(Paint.Align.RIGHT);
            float bw = paint.measureText(text) + dp(24);
            if (bw > maxW) bw = maxW;
            int lines = countLines(text, bw - dp(24));
            float bh = dp(20) * lines + (withTime != 0 ? dp(20) : 0) + dp(14);
            float left = w - dp(12) - bw;

            MessageDrawable d = new MessageDrawable(MessageDrawable.TYPE_TEXT, true, selected);
            d.setBounds((int) left, (int) y, (int) (w - dp(12)), (int) (y + bh));
            d.setRoundRadius(dp(12));
            d.draw(canvas);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Db.OUT_TEXT);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(text, w - dp(12) - dp(10), y + dp(20), paint);
            if (withTime != 0) {
                paint.setColor(Db.SUBTITLE_MUTED);
                paint.setTextSize(dp(11));
                canvas.drawText("12:34", w - dp(12) - dp(58), y + bh - dp(8), paint);
                paint.setColor(Db.ACCENT_CYAN);
                canvas.drawText("✓✓", w - dp(12) - dp(24), y + bh - dp(8), paint);
            }
            return y + bh + dp(16);
        }

        private void drawMediaBubble(Canvas canvas, int w, float y) {
            float bw = dp(180);
            float bh = dp(120);
            float left = w - dp(12) - bw;
            MessageDrawable d = new MessageDrawable(MessageDrawable.TYPE_TEXT, true, false);
            d.setBounds((int) left, (int) y, (int) (left + bw), (int) (y + bh));
            d.setRoundRadius(dp(12));
            d.draw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Db.MEDIA_INNER);
            rect.set(left + dp(8), y + dp(8), left + bw - dp(8), y + bh - dp(8));
            canvas.drawRoundRect(rect, dp(8), dp(8), paint);
            // play glyph
            paint.setColor(Db.ACCENT_CYAN);
            path.reset();
            path.moveTo(left + bw / 2f - dp(9), y + bh / 2f - dp(12));
            path.lineTo(left + bw / 2f - dp(9), y + bh / 2f + dp(12));
            path.lineTo(left + bw / 2f + dp(11), y + bh / 2f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(Db.SUBTITLE_MUTED);
            paint.setTextSize(dp(11));
            canvas.drawText("media preview", left + dp(14), y + bh - dp(16), paint);
        }

        private void drawComposer(Canvas canvas, int w, int h, int composerBg, int sendCol) {
            int barH = dp(64);
            int top = h - barH;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(composerBg);
            canvas.drawRect(0, top, w, h, paint);

            // attachment icon
            paint.setStrokeWidth(dp(2.2f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(sendCol);
            canvas.drawCircle(dp(28), top + dp(32), dp(6), paint);
            // placeholder text
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(dp(16));
            paint.setColor(Db.SUBTITLE_MUTED);
            canvas.drawText("Сообщение", dp(48), top + dp(37), paint);
            // emoji glyph
            paint.setTextSize(dp(20));
            canvas.drawText("\u263A", w - dp(88), top + dp(38), paint);
            // send / mic control
            paint.setColor(sendCol);
            canvas.drawCircle(w - dp(34), top + dp(32), dp(17), paint);
            paint.setColor(composerBg);
            canvas.drawCircle(w - dp(34), top + dp(32), dp(9), paint);
        }

        private void drawDebugStrip(Canvas canvas, int w, String themeName, boolean dark,
                                    int winBg, int chatIn, int chatOut) {
            int top = dp(52);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xcc10141a);
            canvas.drawRect(0, top, w, top + dp(26), paint);
            paint.setColor(0xffaef7ff);
            paint.setTextSize(dp(11));
            paint.setTextAlign(Paint.Align.LEFT);
            String label = "Theme: " + themeName + "  dark=" + (dark ? "true" : "false")
                    + "   winBg #" + hexOf(winBg) + "   chatIn #" + hexOf(chatIn) + "   chatOut #" + hexOf(chatOut);
            canvas.drawText(label, dp(8), top + dp(17), paint);
        }

        private String hexOf(int color) {
            return String.format("%08X", color & 0xffffffffL);
        }

        private int countLines(String text, float maxW) {
            String[] parts = text.split("\n");
            int count = 0;
            for (String part : parts) {
                count += Math.max(1, (int) Math.ceil(paint.measureText(part) / Math.max(maxW, 1f)));
            }
            return count;
        }

        private int dp(float value) {
            return (int) (value * d + 0.5f);
        }
    }
}
