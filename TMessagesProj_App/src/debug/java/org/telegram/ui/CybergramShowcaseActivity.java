package org.telegram.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.SparseIntArray;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.CybergramBubbleDrawable;
import org.telegram.ui.ActionBar.CybergramHudDrawable;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.MessageDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeColors;
import org.telegram.ui.Components.glass.GlassTabView;

/**
 * DEBUG-ONLY visual polygon for Cybergram UI iteration.
 *
 * Not a user-facing feature. Lives in the TMessagesProj_App debug source set and is
 * declared in the debug manifests only, so it is never compiled into a release build.
 * Opened via an explicit adb intent:
 *
 *   adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity
 *
 * The palette is DETERMINISTIC and independent of the saved user theme: all colours are
 * served by {@link Palette}, a DEBUG-only {@link Theme.ResourcesProvider} that reads the
 * cybergram.attheme asset directly and falls back to the upstream design defaults. The
 * active user theme is only READ (and shown on the debug banner) for comparison; it is
 * never applied and preferences are never written.
 *
 * Bubbles are drawn through the real production {@link MessageDrawable} using its full
 * contract: setBounds + setTop (which selects chat_inBubble/chat_outBubble and initialises
 * paint/gradient state) + draw. No setRoundRadius override is used, so the rendered
 * geometry reflects the genuine production SharedConfig.bubbleRadius / MessageDrawable
 * path. {@link Palette} implements {@link CybergramTheme.GeometryProvider}, which enables
 * the Cybergram angular (clipped-corner) silhouette through the production MessageDrawable
 * geometry seam. setDrawFullBubble(true) keeps the bubble as a full standalone bubble.
 */
public class CybergramShowcaseActivity extends Activity {

    private static final String SHOWCASE_THEME = "Cybergram";
    private static final boolean SHOWCASE_DARK = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No Theme.applyTheme(), no preference writes, no pm clear. The showcase palette
        // comes entirely from the Cybergram .attheme asset via the DEBUG provider below.
        final Palette palette = new Palette();
        setContentView(new ShowcaseView(this, palette, B1TabsFixture.render(this, palette)));
    }

    /**
     * DEBUG-only deterministic colour source, independent of the active user theme.
     *
     * getColor / getCurrentColor:
     *   - if the key is present in cybergram.attheme -> return that Cybergram value;
     *   - otherwise -> return the deterministic upstream design default
     *     (ThemeColors.createDefaultColors()), never the user's Day/Night theme.
     */
    private static final class Palette implements CybergramTheme.GeometryProvider {

        private final SparseIntArray cybergram;
        private final int[] defaultColors;

        Palette() {
            cybergram = Theme.getThemeFileValues(null, "cybergram.attheme", null);
            defaultColors = ThemeColors.createDefaultColors();
        }

        @Override
        public int getColor(int key) {
            int index = cybergram.indexOfKey(key);
            if (index >= 0) {
                return cybergram.valueAt(index);
            }
            if (key >= 0 && key < defaultColors.length) {
                return defaultColors[key];
            }
            return 0;
        }

        @Override
        public int getCurrentColor(int key) {
            return getColor(key);
        }

        int color(int key) {
            return getColor(key);
        }

        String rgb(int key) {
            return String.format("#%06X", getColor(key) & 0xFFFFFF);
        }
    }

    /**
     * B1 DEBUG-ONLY main-tabs fixture.
     *
     * Renders the real production {@link GlassTabView} selected-plate path twice against the
     * same deterministic Cybergram palette provider ({@link Palette} implements
     * {@link CybergramTheme.GeometryProvider}, so the central Cybergram gate is active for
     * both rows):
     *
     *   row A — the main-navigation path: instances explicitly opted in with
     *           {@code setCybergramMainTabsPresentation(true)}, exactly as MainTabsActivity does;
     *   row B — the attach-tab control path ({@code createAttachTab}), which is never opted in.
     *
     * Expected: the angular selected plate (PANEL_RAISED fill + 1dp restrained cyan outline)
     * appears only in row A. Row B must stay on the upstream rounded translucent path even
     * though Cybergram presentation is active.
     *
     * This is drawing/compilation evidence for the opt-in rule only. It is not A validation:
     * it does not exercise the authenticated MainTabsActivity state machine, ViewPager
     * movement, badges, insets or the outer panel created in MainTabsActivity.createView.
     */
    private static final class B1TabsFixture {

        private static final int TABS = 4;
        private static final int TAB_W_DP = 84;
        private static final int TAB_H_DP = 54;
        private static final int GAP_DP = 6;
        private static final int PAD_DP = 8;
        private static final int SELECTED_INDEX = 1;

        private static final GlassTabView.TabAnimation[] ANIMATIONS = {
                GlassTabView.TabAnimation.CHATS,
                GlassTabView.TabAnimation.CONTACTS,
                GlassTabView.TabAnimation.CALLS,
                GlassTabView.TabAnimation.SETTINGS
        };
        private static final int[] LABEL_RES = {
                R.string.MainTabsChats,
                R.string.MainTabsContacts,
                R.string.MainTabsCalls,
                R.string.Settings
        };

        static Bitmap render(Activity activity, Palette palette) {
            final int tabW = dp(TAB_W_DP);
            final int tabH = dp(TAB_H_DP);
            final int gap = dp(GAP_DP);
            final int pad = dp(PAD_DP);

            final int width = pad * 2 + TABS * tabW + (TABS - 1) * gap;
            final int height = pad * 2 + tabH * 2 + gap;

            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(CybergramTheme.PANEL);

            drawRow(canvas, activity, palette, pad, pad, tabW, tabH, gap, true);
            drawRow(canvas, activity, palette, pad, pad + tabH + gap, tabW, tabH, gap, false);
            return bitmap;
        }

        private static void drawRow(Canvas canvas, Activity activity, Palette palette, int left, int top,
                                    int tabW, int tabH, int gap, boolean optedIn) {
            int x = left;
            for (int i = 0; i < TABS; i++) {
                final GlassTabView view = optedIn
                        ? GlassTabView.createMainTab(activity, palette, ANIMATIONS[i], LABEL_RES[i])
                        : createControlTab(activity, palette, i);
                if (optedIn) {
                    view.setCybergramMainTabsPresentation(true);
                }
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(tabW, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(tabH, View.MeasureSpec.EXACTLY));
                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                // Draw the selected plate over the tab's own measured box, exactly as the
                // production layout does through setVisualWidth().
                view.setVisualWidth(view.getMeasuredWidth());
                view.setSelected(i == SELECTED_INDEX, false);

                final int save = canvas.save();
                canvas.translate(x, top);
                view.draw(canvas);
                canvas.restoreToCount(save);
                x += view.getMeasuredWidth() + gap;
            }
        }

        /** Control row: attach-tab factory, i.e. the path that must never be opted in. */
        private static GlassTabView createControlTab(Activity activity, Palette palette, int index) {
            final GlassTabView view = GlassTabView.createAttachTab(activity, palette);
            view.setText(activity.getString(LABEL_RES[index]));
            return view;
        }

        private static int dp(float value) {
            return (int) (value * AndroidUtilities.density + 0.5f);
        }
    }

    private static final class ShowcaseView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Palette palette;
        private final Bitmap b1TabsFixture;
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final float d;

        ShowcaseView(Activity activity, Palette palette, Bitmap b1TabsFixture) {
            super(activity);
            this.palette = palette;
            this.b1TabsFixture = b1TabsFixture;
            d = AndroidUtilities.density;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) {
                return;
            }
            canvas.drawColor(palette.color(Theme.key_windowBackgroundWhite));
            drawHeader(canvas, w);
            drawDebugBanner(canvas, w);
            drawDialogs(canvas, w);
            drawB1TabsFixture(canvas, w);
            drawComposer(canvas, w, h);
        }

        /**
         * B1 fixture row placement is deliberately fixed and documented so the rendered plate
         * can be located deterministically in an emulator screenshot:
         *   row A (opt-in main tabs) top edge = fixtureTop + dp(8)
         *   row B (attach control)   top edge = fixtureTop + dp(8) + dp(54) + dp(6)
         */
        private void drawB1TabsFixture(Canvas canvas, int w) {
            if (b1TabsFixture == null) {
                return;
            }
            final int fx = dp(8);
            final int fy = dp(400);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(dp(12));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(0xff00e5ff);
            canvas.drawText("B1 fixture \u2014 row A: main tabs (opted in) / row B: attach tabs (control)",
                    fx, fy - dp(6), paint);

            canvas.drawBitmap(b1TabsFixture, fx, fy, null);
        }

        private void drawHeader(Canvas canvas, int w) {
            int bar = dp(52);
            int headerBg = palette.color(Theme.key_actionBarDefault);
            int headerIcon = palette.color(Theme.key_actionBarDefaultIcon);

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

            // name + subtitle (through the Cybergram palette)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(palette.color(Theme.key_actionBarDefaultTitle));
            paint.setTextSize(dp(16));
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("NIGHT CITY RELAY", dp(72), dp(23), paint);
            paint.setColor(palette.color(Theme.key_actionBarDefaultSubtitle));
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

            // Cybergram header structural decoration (mirrors the production header View):
            // 1dp cyan bottom rule, short amber accent near the title zone, two restrained
            // chamfered corner ticks, all via the shared buildPath polygon.
            int ruleTop = bar - dp(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(headerIcon);
            canvas.drawRect(0, ruleTop, w, bar, paint);
            paint.setColor(CybergramTheme.AMBER);
            canvas.drawRect(dp(52), ruleTop, dp(52) + dp(40), bar, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setColor(headerIcon);
            int tick = dp(8);
            CybergramBubbleDrawable.buildPath(path, dp(24), ruleTop - tick, dp(24) + tick, ruleTop, dp(3), dp(3), dp(3), dp(3));
            canvas.drawPath(path, paint);
            CybergramBubbleDrawable.buildPath(path, w - dp(24) - tick, ruleTop - tick, w - dp(24), ruleTop, dp(3), dp(3), dp(3), dp(3));
            canvas.drawPath(path, paint);
        }

        private void drawDebugBanner(Canvas canvas, int w) {
            int top = dp(52);
            int bannerH = dp(56);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xcc10141a);
            canvas.drawRect(0, top, w, top + bannerH, paint);

            paint.setTextSize(dp(12));
            paint.setTextAlign(Paint.Align.LEFT);

            float y = top + dp(18);
            paint.setColor(palette.color(Theme.key_chat_messageTextOut));
            canvas.drawText("Showcase: " + SHOWCASE_THEME + " | dark=" + (SHOWCASE_DARK ? "true" : "false"), dp(10), y, paint);

            y += dp(18);
            paint.setColor(0xff7c8a91);
            canvas.drawText("Active app theme: " + activeThemeName(), dp(10), y, paint);

            y += dp(18);
            paint.setColor(palette.color(Theme.key_chat_messageTextOut));
            canvas.drawText("BG " + palette.rgb(Theme.key_windowBackgroundWhite)
                    + " | IN " + palette.rgb(Theme.key_chat_inBubble)
                    + " | OUT " + palette.rgb(Theme.key_chat_outBubble), dp(10), y, paint);
        }

        private void drawChat(Canvas canvas, int w) {
            float top = dp(52) + dp(56) + dp(18);
            float leftPad = dp(12);
            float maxBubbleW = w - dp(24) - dp(48);

            // service / date label
            String dateLabel = "NIGHT CITY — 09/09/2026";
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(palette.color(Theme.key_chat_serviceBackground));
            paint.setTextSize(dp(12));
            float labelW = paint.measureText(dateLabel) + dp(24);
            rect.set((w - labelW) / 2f, top + dp(8), (w + labelW) / 2f, top + dp(28));
            canvas.drawRoundRect(rect, dp(12), dp(12), paint);
            paint.setColor(palette.color(Theme.key_chat_serviceText));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(dateLabel, w / 2f, top + dp(21), paint);

            float y = top + dp(38);
            // normal incoming
            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.", false, false, false, 8f);
            // multiline incoming
            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Мультистрочное сообщение:\nвторой абзац и\nдлинная строка, которая должна аккуратно переноситься.", false, false, false, 8f);
            // normal outgoing
            y = drawOutgoingBubble(canvas, w, leftPad, y, maxBubbleW,
                    "Привет! Это демо-сообщение Cybergram.", false, false, false, false, 8f);
            // selected multiline outgoing (with time)
            y = drawOutgoingBubble(canvas, w, leftPad, y, maxBubbleW,
                    "Выделенное сообщение\nс временем и статусом.", true, true, false, false, 8f);
            // grouped incoming pair (near flags drive the 2dp near corner; tight 2dp gap)
            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Группа · входящее 1", false, false, true, 2f);
            y = drawIncomingBubble(canvas, leftPad, y, maxBubbleW,
                    "Группа · входящее 2", false, true, false, 8f);
            // grouped outgoing pair (mirror on the right)
            y = drawOutgoingBubble(canvas, w, leftPad, y, maxBubbleW,
                    "Группа · исходящее 1", false, false, false, true, 2f);
            y = drawOutgoingBubble(canvas, w, leftPad, y, maxBubbleW,
                    "Группа · исходящее 2", false, false, true, false, 8f);
            drawMediaBubble(canvas, w, y);
        }

        private float drawIncomingBubble(Canvas canvas, float leftPad, float y, float maxW, String text, boolean selected, boolean topNear, boolean bottomNear, float gapDp) {
            Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
            int textColor = palette.color(Theme.key_chat_messageTextIn);
            float bubbleW = bubbleWidth(text, maxW, align);
            float bubbleH = bubbleHeight(text, bubbleW, align, textColor, 0);
            drawBubble(canvas, MessageDrawable.TYPE_TEXT, false, selected, topNear, bottomNear, leftPad, y, bubbleW, bubbleH);
            drawText(canvas, text, leftPad + dp(10), y + dp(8), bubbleW - dp(20), align, textColor);
            return y + bubbleH + dp(gapDp);
        }

        private float drawOutgoingBubble(Canvas canvas, int w, float leftPad, float y, float maxW, String text, boolean selected, boolean withTime, boolean topNear, boolean bottomNear, float gapDp) {
            Layout.Alignment align = Layout.Alignment.ALIGN_OPPOSITE;
            int textColor = palette.color(Theme.key_chat_messageTextOut);
            float bubbleW = bubbleWidth(text, maxW, align);
            float timeH = withTime ? dp(18) : 0;
            float bubbleH = bubbleHeight(text, bubbleW, align, textColor, timeH);
            float left = w - dp(12) - bubbleW;
            drawBubble(canvas, MessageDrawable.TYPE_TEXT, true, selected, topNear, bottomNear, left, y, bubbleW, bubbleH);
            drawText(canvas, text, left + dp(10), y + dp(8), bubbleW - dp(20), align, textColor);
            if (withTime) {
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(dp(11));
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setColor(0xff7c8a91);
                canvas.drawText("12:34", w - dp(12) - dp(52), y + bubbleH - dp(9), paint);
                paint.setColor(0xff00e5ff);
                canvas.drawText("\u2713\u2713", w - dp(12) - dp(22), y + bubbleH - dp(9), paint);
            }
            return y + bubbleH + dp(gapDp);
        }

        private void drawMediaBubble(Canvas canvas, int w, float y) {
            float bw = dp(180);
            float bh = dp(120);
            float left = w - dp(12) - bw;
            // Production media bubble: TYPE_MEDIA through the same provider/contract.
            drawBubble(canvas, MessageDrawable.TYPE_MEDIA, true, false, false, false, left, y, bw, bh);

            // media placeholder inner + play glyph (debug chrome, deterministic)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xff06090d);
            rect.set(left + dp(8), y + dp(8), left + bw - dp(8), y + bh - dp(8));
            canvas.drawRoundRect(rect, dp(8), dp(8), paint);
            paint.setColor(0xff00e5ff);
            path.reset();
            path.moveTo(left + bw / 2f - dp(9), y + bh / 2f - dp(12));
            path.lineTo(left + bw / 2f - dp(9), y + bh / 2f + dp(12));
            path.lineTo(left + bw / 2f + dp(11), y + bh / 2f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(0xff7c8a91);
            paint.setTextSize(dp(11));
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("media preview", left + dp(14), y + bh - dp(16), paint);
        }

        private void drawComposer(Canvas canvas, int w, int h) {
            int barH = dp(64);
            int top = h - barH;
            int composerBg = palette.color(Theme.key_chat_messagePanelBackground);
            int sendCol = palette.color(Theme.key_chat_messagePanelSend);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(composerBg);
            canvas.drawRect(0, top, w, h, paint);

            // attachment icon
            paint.setStrokeWidth(dp(2.2f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(sendCol);
            canvas.drawCircle(dp(28), top + dp(32), dp(6), paint);
            // placeholder text (palette hint colour)
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(dp(16));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(palette.color(Theme.key_chat_messagePanelHint));
            canvas.drawText("Сообщение", dp(48), top + dp(37), paint);
            // emoji glyph
            paint.setTextSize(dp(20));
            canvas.drawText("\u263A", w - dp(88), top + dp(38), paint);
            // Send control (Stage D pass 2): a chamfered dark angular plate + 1dp cyan stroke
            // behind a light send glyph, replacing Telegram's cyan round send button. Uses the
            // same CybergramHudDrawable primitive (shared CybergramBubbleDrawable.buildPath).
            int cx = w - dp(34);
            int cy = top + dp(32);
            CybergramHudDrawable sendPlate = new CybergramHudDrawable();
            sendPlate.setFillColor(composerBg);
            sendPlate.setStroke(sendCol, dp(1), true);
            sendPlate.setCornerCut(dp(CybergramTheme.BUBBLE_CORNER_CUT_DP));
            sendPlate.setBounds(cx - dp(17), cy - dp(17), cx + dp(17), cy + dp(17));
            sendPlate.draw(canvas);
            // send glyph approximation (paper-plane triangle), drawn on top in light
            Path plane = new Path();
            plane.moveTo(cx - dp(6), cy + dp(6));
            plane.lineTo(cx - dp(6), cy - dp(6));
            plane.lineTo(cx + dp(9), cy);
            plane.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFFFFF);
            canvas.drawPath(plane, paint);

            // Cybergram composer frame: 1dp cyan chamfered outline around the field pill,
            // via the same HUD primitive used by the production composer seam. Sits clear of
            // the send control and leaves the existing (rect) panel background untouched.
            CybergramHudDrawable frame = new CybergramHudDrawable();
            frame.setStroke(sendCol, dp(1), true);
            frame.setCornerCut(dp(CybergramTheme.BUBBLE_CORNER_CUT_DP));
            frame.setBounds(dp(8), top + dp(8), w - dp(60), h - dp(8));
            frame.draw(canvas);
        }

        /** Natural text width (widest line) clamped to the max bubble inner width. */
        private float bubbleWidth(String text, float maxBubbleW, Layout.Alignment align) {
            float txtPad = dp(10);
            float maxInner = maxBubbleW - 2 * txtPad;
            float natural = 0;
            textPaint.setTextSize(dp(14));
            for (String line : text.split("\n", -1)) {
                natural = Math.max(natural, textPaint.measureText(line));
            }
            return Math.min(natural, maxInner) + 2 * txtPad;
        }

        /** Bubble height from a real StaticLayout of the (possibly wrapped) text. */
        private float bubbleHeight(String text, float bubbleW, Layout.Alignment align, int color, float extraH) {
            float txtPad = dp(10);
            float innerW = Math.max(bubbleW - 2 * txtPad, 1f);
            textPaint.setTextSize(dp(14));
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setColor(color);
            StaticLayout sl = new StaticLayout(text, textPaint, (int) Math.ceil(innerW), align, 1f, 0f, false);
            return sl.getHeight() + 2 * dp(8) + extraH;
        }

        private void drawDialogs(Canvas canvas, int w) {
            int top = dp(52) + dp(56) + dp(12);
            int rowH = dp(52);
            int titleX = dp(60);
            int leftPad = dp(4);
            String[] titles = {"ALIAS // open channel", "ВЕСТНИК · УЗЕЛ 07", "DATA RELAY", "CONTACT 07"};
            String[] previews = {"привет, фид виден", "3 новых сообщения", "muted · сетевой инцидент", "выбрано: 1"};
            // dialogs top chrome: dark strip + 1dp cyan rule + amber segment + restrained ticks
            int absBottom = top - dp(8);
            int absTop = absBottom - dp(30);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(palette.color(Theme.key_windowBackgroundWhite));
            canvas.drawRect(0, absTop, w, absBottom, paint);
            paint.setColor(palette.color(Theme.key_chats_name));
            paint.setTextSize(dp(16));
            canvas.drawText("CYBERGRAM", dp(16), absTop + dp(20), paint);
            paint.setColor(palette.color(Theme.key_chat_messagePanelSend));
            canvas.drawRect(0, absBottom - dp(1), w, absBottom, paint);
            paint.setColor(CybergramTheme.AMBER);
            canvas.drawRect(dp(52), absBottom - dp(1), dp(52) + dp(40), absBottom, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            int tk = dp(8);
            paint.setColor(palette.color(Theme.key_chat_messagePanelSend));
            canvas.drawLine(dp(24), absBottom - tk - dp(1), dp(24) + tk, absBottom - dp(1), paint);
            canvas.drawLine(w - dp(24) - tk, absBottom - tk - dp(1), w - dp(24), absBottom - dp(1), paint);
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 4; i++) {
                int y = top + i * rowH;
                boolean unread = i == 1;
                boolean mutedPinned = i == 2;
                boolean selected = i == 3;

                if (selected) {
                    CybergramHudDrawable sel = new CybergramHudDrawable();
                    sel.setFillColor(palette.color(Theme.key_chats_pinnedOverlay));
                    sel.setStroke(palette.color(Theme.key_chat_messagePanelSend), dp(1), true);
                    sel.setCornerCut(dp(6));
                    sel.setBounds(leftPad, y + dp(2), w - leftPad, y + rowH - dp(2));
                    sel.draw(canvas);
                }
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(palette.color(Theme.key_chat_messagePanelSend));
                paint.setAlpha(CybergramTheme.DIALOGS_ROW_SEPARATOR_ALPHA);
                canvas.drawRect(0, y + rowH - dp(1), w, y + rowH, paint);
                paint.setAlpha(CybergramTheme.DIALOGS_ROW_ACCENT_ALPHA);
                float accTop = y + rowH * 0.30f;
                canvas.drawRect(0, accTop, dp(2), accTop + dp(16), paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1));
                paint.setAlpha(CybergramTheme.DIALOGS_ROW_TICK_ALPHA);
                canvas.drawLine(dp(2), accTop, dp(2) + dp(6), accTop - dp(6), paint);
                paint.setStyle(Paint.Style.FILL);

                paint.setColor(palette.color(Theme.key_chats_name));
                paint.setTextSize(dp(15));
                paint.setAlpha(255);
                canvas.drawText(titles[i], titleX, y + dp(21), paint);
                paint.setColor(palette.color(Theme.key_chats_message));
                paint.setTextSize(dp(13));
                canvas.drawText(previews[i], titleX, y + dp(39), paint);
                paint.setColor(palette.color(Theme.key_chats_date));
                paint.setTextSize(dp(11));
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("23:59", w - dp(16), y + dp(19), paint);
                if (unread || mutedPinned) {
                    paint.setColor(palette.color(mutedPinned ? Theme.key_chats_unreadCounterMuted : Theme.key_chats_unreadCounter));
                    paint.setTextSize(dp(15));
                    canvas.drawText(mutedPinned ? "77" : "3", w - dp(16), y + dp(41), paint);
                }
                paint.setTextAlign(Paint.Align.LEFT);
            }

            // angular FAB mock (shared CybergramHudDrawable primitive)
            int fabSize = dp(48);
            int fabLeft = w - dp(16) - fabSize;
            int fabTop = top + 4 * rowH + dp(6);
            CybergramHudDrawable fabPlate = new CybergramHudDrawable();
            fabPlate.setFillColor(palette.color(Theme.key_windowBackgroundWhite));
            fabPlate.setStroke(palette.color(Theme.key_chat_messagePanelSend), dp(1), true);
            fabPlate.setCornerCut(dp(6));
            fabPlate.setBounds(fabLeft, fabTop, fabLeft + fabSize, fabTop + fabSize);
            fabPlate.draw(canvas);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(palette.color(Theme.key_chats_actionIcon));
            int cx = fabLeft + fabSize / 2;
            int cy = fabTop + fabSize / 2;
            int r = dp(8);
            canvas.drawLine(cx - r, cy, cx + r, cy, paint);
            canvas.drawLine(cx, cy - r, cx, cy + r, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        private void drawBubble(Canvas canvas, int type, boolean out, boolean selected, boolean topNear, boolean bottomNear, float left, float y, float w, float h) {
            MessageDrawable d = new MessageDrawable(type, out, selected, palette);
            d.setDrawFullBubble(true);
            d.setBounds((int) left, (int) y, (int) (left + w), (int) (y + h));
            // setTop selects chat_in/outBubble, initialises paint/gradient state and
            // carries the grouping near flags (accepted; Cybergram uses the same corner
            // cut on all four corners for now — grouped-specific treatment deferred).
            d.setTop((int) y, (int) w, (int) h, topNear, bottomNear);
            d.draw(canvas);
        }

        private void drawText(Canvas canvas, String text, float x, float y, float w, Layout.Alignment align, int color) {
            textPaint.setTextSize(dp(14));
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setColor(color);
            StaticLayout sl = new StaticLayout(text, textPaint, (int) Math.ceil(w), align, 1f, 0f, false);
            int save = canvas.save();
            canvas.translate(x, y);
            sl.draw(canvas);
            canvas.restoreToCount(save);
        }

        private String activeThemeName() {
            try {
                Theme.ThemeInfo info = Theme.getCurrentTheme();
                if (info != null && info.name != null) {
                    return info.name;
                }
            } catch (Throwable ignore) {
                // read-only comparison; never propagate
            }
            return "Unknown";
        }

        private int dp(float value) {
            return (int) (value * d + 0.5f);
        }
    }
}
