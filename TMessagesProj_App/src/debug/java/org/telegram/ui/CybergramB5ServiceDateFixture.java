package org.telegram.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.lang.reflect.Field;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.CybergramBubbleDrawable;
import org.telegram.ui.ActionBar.CybergramTheme;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatActionCell;

/**
 * B5 DEBUG-ONLY service/date probe (audit evidence, no production effect).
 *
 * Lives in the TMessagesProj_App debug source set only. It renders the REAL production
 * {@link ChatActionCell} ordinary path for deterministic service/date text, so the
 * line-following ordinary silhouette can be inspected on an unauthenticated emulator:
 *
 *  - the real cell is created with the showcase's Cybergram palette provider and driven
 *    only through production public entry points that the chat list itself uses for
 *    floating dates / info views ({@code setCustomText}, {@code setCustomDate},
 *    {@code setVisiblePart}, {@code measure}, {@code layout}, {@code draw});
 *  - {@code currentMessageObject == null}, so {@code isButtonLayout(null) == false},
 *    {@code starGiftLayout.has() == false} and {@code birthdayLayout == null}: the cell
 *    therefore draws exactly the ordinary {@code backgroundPath} pipeline that a plain
 *    one-line / multi-line service action or a date separator uses;
 *  - after the real draw, the fixture overlays a 1dp Strategy-B candidate outline
 *    (single enclosing chamfer, {@link CybergramBubbleDrawable#buildPath}) derived from
 *    the cell's own ordinary {@code backgroundPath} extent, read through debug-only
 *    reflection (see {@link #rawBackgroundBounds}). The outline is a debug annotation
 *    only; it is NOT a production render and NOT a preview of committed geometry.
 *
 * What this fixture is NOT: it does not synthesize rich/special MessageObject states
 * (gifts, offers, wallpapers, community cards, suggested post approval). Those need real
 * message data and stay source-classified + UNTESTED/A in the B5 matrix.
 */
public final class CybergramB5ServiceDateFixture {

    /**
     * Debug-only reflection handles onto {@link ChatActionCell}'s private
     * {@code backgroundLeft}/{@code backgroundRight}.
     *
     * Those fields are the raw horizontal extent of the ordinary {@code backgroundPath} that
     * {@code drawBackground()} has just built. The public {@code getBoundsLeft()} /
     * {@code getBoundsRight()} are the wrong source for this probe: they widen the result by
     * {@code imageReceiver} visibility, and for ordinary {@code customText} rows the receiver can
     * still be marked visible, so the public bounds report a near-full-row plate that was never
     * drawn. The private fields do not include that widening (nor the {@code sideMenuWidth / 2}
     * offset), so they describe the compact plate actually rendered.
     *
     * The lookup is done once and is deliberately defensive: this class only ever compiles into the
     * debug source set, so a reflection failure (renamed field, stripped build, stricter runtime)
     * must degrade the probe, never crash it.
     */
    private static final Field BACKGROUND_LEFT_FIELD = findBackgroundField("backgroundLeft");
    private static final Field BACKGROUND_RIGHT_FIELD = findBackgroundField("backgroundRight");

    private static Field findBackgroundField(String name) {
        try {
            final Field field = ChatActionCell.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Probe bitmap width in dp. */
    private static final int WIDTH_DP = 396;
    /** Probe bitmap height in dp. */
    private static final int HEIGHT_DP = 336;

    /** Fixed date used for the real {@code LocaleController.formatDateChat} path. */
    private static final int DATE_TS = 1768000000;

    private final Theme.ResourcesProvider provider;
    private final Activity activity;
    private final Bitmap bitmap;
    private final Canvas canvas;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint candidatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path candidatePath = new Path();

    /** Running vertical cursor, in dp. */
    private float y;

    private CybergramB5ServiceDateFixture(Activity activity, Theme.ResourcesProvider provider) {
        this.activity = activity;
        this.provider = provider;
        bitmap = Bitmap.createBitmap(dp(WIDTH_DP), dp(HEIGHT_DP), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Theme.getColor(Theme.key_windowBackgroundWhite, provider));

        candidatePaint.setStyle(Paint.Style.STROKE);
        candidatePaint.setStrokeWidth(Math.max(1f, dp(1)));
        candidatePaint.setStrokeJoin(Paint.Join.MITER);
        candidatePaint.setStrokeCap(Paint.Cap.SQUARE);
        candidatePaint.setColor(CybergramTheme.DANGER);
    }

    /** Renders the whole probe once; the caller blits the bitmap into the showcase canvas. */
    public static Bitmap render(Activity activity, Theme.ResourcesProvider provider) {
        final CybergramB5ServiceDateFixture f = new CybergramB5ServiceDateFixture(activity, provider);
        try {
            f.drawAll();
        } catch (Throwable t) {
            f.drawFailure(t);
        }
        return f.bitmap;
    }

    private void drawAll() {
        legend();
        dateRow();
        oneLineShortRow();
        oneLineLongRow();
        twoLineUnevenRow();
        threeLineRow();
        longWrappedRow();
    }

    private void legend() {
        text("B5 probe: real ChatActionCell ordinary backgroundPath (magenta = Strategy B candidate outline)",
                dp(6), dp(y + 9), dp(9), Theme.getColor(Theme.key_chat_messagePanelSend, provider));
        y += 13;
    }

    /** Real {@code setCustomDate(...)} date-separator path. */
    private void dateRow() {
        cell(1, "date separator (setCustomDate)", null, DATE_TS);
    }

    private void oneLineShortRow() {
        cell(2, "one-line short service", "Alice joined the group", -1);
    }

    private void oneLineLongRow() {
        cell(3, "one-line long service",
                "Encryption is not available for this secret chat", -1);
    }

    private void twoLineUnevenRow() {
        cell(4, "two-line uneven service",
                "Alice pinned a message to the chat\nby Alice", -1);
    }

    private void threeLineRow() {
        cell(5, "three-line service",
                "Alice changed the group name to Night City Relay\nand updated the description for all members\nto explain the new rules", -1);
    }

    private void longWrappedRow() {
        cell(6, "long wrapped service",
                "You can now send messages in this chat. Please be respectful and follow the community guidelines at all times.",
                -1);
    }

    /**
     * Creates, measures, lays out and draws one real {@link ChatActionCell}, then overlays the
     * Strategy-B candidate outline using the cell's own ordinary {@code backgroundPath} extent.
     */
    private void cell(int index, String label, String text, int customDate) {
        final ChatActionCell cell = new ChatActionCell(activity, false, provider);
        if (customDate >= 0) {
            cell.setCustomDate(customDate, false, false);
        } else {
            cell.setCustomText(text);
        }
        cell.setVisiblePart(0f, dp(HEIGHT_DP));
        cell.measure(
                View.MeasureSpec.makeMeasureSpec(dp(WIDTH_DP), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        cell.layout(0, 0, cell.getMeasuredWidth(), cell.getMeasuredHeight());

        final float top = y;
        final int save = canvas.save();
        canvas.translate(0, dp(top));
        cell.draw(canvas);

        // Raw ordinary backgroundPath extent, read AFTER the real draw populated it. The public
        // getBounds*() must not be used here: they include imageReceiver visibility and would
        // outline a near-full-row plate that was never drawn for ordinary customText rows.
        final int[] bounds = rawBackgroundBounds(cell);
        final int left = bounds[0];
        final int right = bounds[1];
        final boolean reflected = bounds[2] == 1;
        final int plateTop = dp(4);
        final int plateBottom = cell.getMeasuredHeight() - dp(4);
        final boolean hasPlate = right > left && plateBottom > plateTop;
        if (hasPlate) {
            CybergramBubbleDrawable.buildPath(candidatePath, left, plateTop, right, plateBottom,
                    dp(CybergramTheme.BUBBLE_CORNER_CUT_DP));
            canvas.drawPath(candidatePath, candidatePaint);
        }

        // Row label + measured width, drawn to the right of the plate.
        text(index + " " + label, dp(6), dp(11), dp(8), 0xff7c8a91);
        text(hasPlate
                        ? "plate " + (right - left) + "px" + (reflected ? "" : " (public-bounds fallback)")
                        : "plate none" + (reflected ? "" : " (public-bounds fallback)"),
                dp(6), dp(21), dp(8), 0xff7c8a91);
        canvas.restoreToCount(save);

        y += (cell.getMeasuredHeight() / AndroidUtilities.density) + 6f;
    }

    /**
     * Reads the real ordinary {@code backgroundPath} horizontal extent from the cell's private
     * {@code backgroundLeft}/{@code backgroundRight} fields after {@code drawBackground()} has run.
     *
     * Returns {@code {left, right, source}} where {@code source} is {@code 1} when the private
     * fields were read and {@code 0} when the safety fallback was used. The fallback is the public
     * {@code getBoundsLeft()}/{@code getBoundsRight()} pair: it can be too wide, but it keeps the
     * probe rendering instead of dying if reflection is unavailable. An inverted/extent-less pair
     * (no lines laid out) is returned as-is and is filtered by the caller via {@code right > left}.
     */
    private static int[] rawBackgroundBounds(ChatActionCell cell) {
        if (BACKGROUND_LEFT_FIELD != null && BACKGROUND_RIGHT_FIELD != null) {
            try {
                return new int[] {
                        BACKGROUND_LEFT_FIELD.getInt(cell),
                        BACKGROUND_RIGHT_FIELD.getInt(cell),
                        1};
            } catch (Throwable ignored) {
                // Degrade to the public fallback below rather than failing the debug probe.
            }
        }
        return new int[] {cell.getBoundsLeft(), cell.getBoundsRight(), 0};
    }

    /* --- fixture-side plumbing (not under audit) --- */

    private void text(String value, float xPx, float baselinePx, float sizePx, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(sizePx);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(value, xPx, baselinePx, paint);
    }

    /** Debug harness only: renders the failure text instead of propagating it. */
    private void drawFailure(Throwable t) {
        text("B5 probe failed: " + t.getClass().getSimpleName() + ": " + t.getMessage(),
                dp(6), dp(20), dp(9), 0xffff2e46);
    }

    private static int dp(float value) {
        return (int) (value * AndroidUtilities.density + 0.5f);
    }
}
