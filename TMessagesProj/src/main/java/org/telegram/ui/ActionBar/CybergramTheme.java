package org.telegram.ui.ActionBar;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import org.telegram.messenger.AndroidUtilities;

/**
 * Cybergram-owned visual constants.
 *
 * Keep project-specific styling values here instead of scattering literals
 * through Telegram's upstream UI classes. The checked-in .attheme uses the
 * same core palette; screen-specific semantic keys may refine it further.
 */
public final class CybergramTheme {

    public static final int BACKGROUND = 0xFF080A0F;
    public static final int PANEL = 0xFF0B0D12;
    public static final int PANEL_RAISED = 0xFF111820;

    public static final int CYAN = 0xFF6FD9E8;
    public static final int CYAN_SECONDARY = 0xFF8EEAF2;
    /** Ruled 2026-09-15: reference amber (was #E8D93A). See docs/OWNER_DECISIONS_2026-09-15.md. */
    public static final int AMBER = 0xFFE6C955;
    public static final int AMBER_HIGHLIGHT = 0xFFF0DB7A;
    /** Ruled 2026-09-15: reference service red (was #FF2E46). */
    public static final int DANGER = 0xFFE01C47;

    public static final int TEXT = 0xFFE6F7FF;
    public static final int TEXT_MUTED = 0xFF6B7A8A;
    public static final int TEXT_ON_AMBER = 0xFF101216;

    /**
     * Low-saturation pale blue for header/composer line icons (owner ruling D9.6/D9.8,
     * docs/OWNER_DECISIONS_2026-09-17.md). The saturated cyan above stays reserved for thin
     * semantic accents (outgoing outline, checks, cursor), not for large icon masses.
     */
    public static final int ICON_PALE = 0xFF91BCC6;

    /**
     * Refined message surfaces (owner ruling D9.2): incoming is a near-black olive, outgoing is a
     * dark blue-green. The saturated turquoise fill is gone — direction is carried by the thin
     * outline and the metadata colour, not by an opaque bright surface.
     */
    public static final int IN_BUBBLE = 0xD012120D;
    public static final int IN_BUBBLE_SELECTED = 0xE81E2015;
    public static final int OUT_BUBBLE = 0xD0031116;
    public static final int OUT_BUBBLE_SELECTED = 0xE807252D;

    /** Body text: incoming light grey, outgoing pale blue (owner ruling D9.5). */
    public static final int IN_TEXT = 0xFFD4C39F;
    public static final int OUT_TEXT = 0xFFB7D5DA;

    /** Time/metadata: smaller and dimmer than the body text on both sides (owner ruling D9.5). */
    public static final int IN_TIME = 0xFFB1A76F;
    public static final int OUT_TIME = 0xFF86AAB2;

    /** Direction-aware semantic accents. Incoming lives in the amber family, outgoing in cyan. */
    public static final int IN_LINK = AMBER_HIGHLIGHT;
    public static final int OUT_LINK = CYAN_SECONDARY;
    public static final int IN_COMMENT = AMBER_HIGHLIGHT;
    public static final int OUT_COMMENT = CYAN_SECONDARY;

    /** Floating unread/page-down badge: readable state marker, not a stock Telegram blue pill. */
    public static final int PAGE_DOWN_BADGE_FILL = AMBER;
    public static final int PAGE_DOWN_BADGE_BORDER = AMBER_HIGHLIGHT;
    public static final int PAGE_DOWN_BADGE_TEXT = TEXT_ON_AMBER;
    public static final float PAGE_DOWN_BADGE_CUT_DP = 5f;
    public static final float PAGE_DOWN_BADGE_BORDER_WIDTH_DP = 0.8f;

    /** Red structural rail for separators and technical framing (owner ruling D9.1). */
    public static final int SEPARATOR = DANGER;

    public static final int DIVIDER = 0xFF2A0A12;
    public static final int HINT = 0xFF5F6E7C;

    /** Built-in Cybergram theme name; used only for the presentation activation gate. */
    public static final String THEME_NAME = "Cybergram";

    /** Corner chamfer cut, in dp, for the Cybergram message silhouette (45-degree corners). */
    public static final float BUBBLE_CORNER_CUT_DP = 5f;

    /**
     * Length, in dp, of the small angular corner protrusion ("выступ") on the outer top corner of a
     * Cybergram message bubble (owner ruling D9.4). It is drawn inside the reserved 8dp tail region,
     * so the polygon stays within the drawable bounds. The other three corners keep the 45° chamfer.
     */
    public static final float BUBBLE_TAIL_DP = 5.25f;

    /** Chamfer cut, in dp, for the "near" corners of a grouped Cybergram bubble. */
    public static final float BUBBLE_NEAR_CORNER_CUT_DP = 2f;

    /**
     * Tail allowance, in dp, that the Cybergram silhouette keeps from Telegram's bubble drawable on
     * the speaking side of a <b>text</b> bubble. Media placement must subtract it, otherwise a
     * photo that sits inside a text-shaped bubble (single attachment with a caption below, link
     * previews) is stretched across the gutter and paints over the frame.
     */
    public static final float BUBBLE_TAIL_GUTTER_DP = 8f;

    /**
     * Distance, in dp, between the drawable bounds and the Cybergram silhouette.
     *
     * {@code MessageDrawable} builds every Cybergram bubble polygon on this inset, so it is the
     * single source of truth for "where the frame is". Content that must reach the frame — media
     * rasters in particular — derives its own inset from this value instead of guessing.
     */
    public static final float BUBBLE_FRAME_PADDING_DP = 2f;

    /** Cybergram message outline stroke width, in dp (the bright neon core). */
    public static final float BUBBLE_BORDER_WIDTH_DP = 0.5f;
    public static final int BUBBLE_BORDER_ALPHA = 198;
    /** Two soft passes under the core: broad bloom plus a tighter mid halo. */
    public static final float BUBBLE_GLOW_WIDTH_DP = 6.2f;
    public static final int BUBBLE_GLOW_ALPHA = 11;
    public static final int BUBBLE_GLOW_SELECTED_ALPHA = 18;
    public static final float BUBBLE_MID_GLOW_WIDTH_DP = 2.4f;
    public static final int BUBBLE_MID_GLOW_ALPHA = 30;
    public static final int BUBBLE_MID_GLOW_SELECTED_ALPHA = 44;

    public static final int CANVAS_GRID_ALPHA = 3;
    public static final int CANVAS_RAIL_ALPHA = 46;
    public static final int CANVAS_RAIL_GLOW_ALPHA = 14;
    /** Sparse decorative HUD annotations. Neutral labels only, never security/status claims. */
    public static final int CANVAS_TECH_ALPHA = 46;
    public static final int CANVAS_MICRO_ALPHA = 30;
    public static final float CANVAS_MICRO_TEXT_DP = 6.25f;
    public static final float CANVAS_GRID_X_DP = 48f;
    public static final float CANVAS_GRID_Y_DP = 36f;

    /** Physical-pass spacing: visual row clearance without changing bubble/time geometry. */
    public static final float MESSAGE_ROW_GAP_DP = 7f;
    public static final float MESSAGE_TIME_BOTTOM_INSET_DP = 2.5f;
    public static final float MESSAGE_TEXT_SIZE_REDUCTION_DP = 1f;
    public static final float MESSAGE_TIME_TEXT_SIZE_DP = 10f;
    public static final float COMPOSER_TEXT_SIZE_DP = 15f;
    public static final int COMPOSER_DIVIDER_ALPHA = 170;
    /** Pull bubbles away from the red HUD rails while preserving their measured width. */
    public static final float MESSAGE_SIDE_INSET_DP = 6f;
    /** Reply plates should read as glass/tint, not as a second opaque card. */
    public static final float REPLY_PLATE_ALPHA_SCALE = 0.38f;
    public static final int DATE_GLOW_ALPHA = 12;

    /**
     * Extra vertical inset, in dp, applied to each *unjoined* vertical edge of a Cybergram
     * TYPE_TEXT bubble body (owner ruling D6, docs/OWNER_DECISIONS_2026-09-15.md §2 D6).
     *
     * It is a paint-only inset inside {@code MessageDrawable.generateCybergramPath(...)}: no
     * bounds, measurement, scroll or metadata position changes, and edges joined to a
     * neighbouring bubble of the same run ({@code isTopNear}/{@code isBottomNear}) keep the
     * upstream inset, so grouped joins are preserved. Distinct bubbles therefore gain
     * {@code 2 * BUBBLE_GAP_EXTRA_DP} of clear space between them.
     *
     * Owner ruling D10.2 (round 4c) resolved R-D6 by lowering this from 3f to
     * {@link #BUBBLE_GAP_MAX_DP}: the measured clearance showed 3f left exactly 0 px at the
     * binding bottom edge. Measured geometry (density 420, outline stroke dp(0.75) with
     * {@code Paint.Style.STROKE}): the drawable padding is dp(2) (MessageDrawable.java:564/630/907)
     * and {@code bounds.top} is dp(1) (ChatMessageCell.java:20560/20643), so the painted top is
     * dp(3) + E. The top edge never binds (plain text starts at dp(10.5)); the binding edge is the
     * time cluster at {@code layoutHeight - dp(6.5)} ({@code - dp(7.5)} when grouped,
     * ChatMessageCell.java:24505-24509), which needs E at or below {@link #BUBBLE_GAP_MAX_DP} to
     * keep a clear margin. {@code MessageDrawable} clamps to that maximum, so a future raise cannot
     * silently reintroduce the collision.
     * TYPE_MEDIA is deliberately excluded: a lowered media outline would expose the photo,
     * whose y is set independently of the drawable bounds.
     */
    public static final float BUBBLE_GAP_EXTRA_DP = 2.5f;

    /**
     * Largest safe value of {@link #BUBBLE_GAP_EXTRA_DP}, in dp (owner ruling D10.2, R-D6). At this
     * value the painted bottom edge still clears the time cluster; above it the outline intrudes
     * into the time box (measured at density 420: 3f gave 0 px standalone and -1 px grouped).
     * {@code MessageDrawable.generateCybergramPath(...)} clamps to it defensively.
     */
    public static final float BUBBLE_GAP_MAX_DP = 2.5f;

    /** Reference-style header rail: restrained warning red under normal Cybergram chrome. */
    public static final int HEADER_RULE_ALPHA = 120;

    /** Alpha of secondary cyan header ticks/identity segment. */
    public static final int HEADER_TECH_ALPHA = 180;

    /** Alpha (0..255) of the thin red separator above the composer (owner ruling D9.1). */
    public static final int COMPOSER_RULE_ALPHA = 90;

    /** Alpha (0..255) of the soft neon bloom under the red rails (owner ruling D9.8). */
    public static final int HEADER_GLOW_ALPHA = 30;
    public static final int COMPOSER_GLOW_ALPHA = 28;

    /** Round-video recorder: keep the circular media format, but use Cybergram HUD chrome. */
    public static final float RECORDER_CONTROL_SIZE_DP = 40f;
    public static final float RECORDER_CONTROL_CUT_DP = 8f;
    public static final float RECORDER_CONTROL_BORDER_WIDTH_DP = 0.85f;
    public static final int RECORDER_CONTROL_BORDER_ALPHA = 196;
    public static final int RECORDER_CONTROL_GLOW_ALPHA = 28;
    public static final float RECORDER_PREVIEW_TRACK_WIDTH_DP = 1.15f;
    public static final float RECORDER_PREVIEW_PROGRESS_WIDTH_DP = 2.35f;
    public static final int RECORDER_PREVIEW_TRACK_ALPHA = 132;

    /** Radius, in dp, of the red-rail neon bloom (owner ruling D9.8). */
    public static final float RED_GLOW_RADIUS_DP = 4f;

    /** Extra vertical inner padding, in dp, for Cybergram text bubbles (owner ruling D9.4). */
    public static final float BUBBLE_INNER_PAD_DP = 2.5f;

    /** Extra horizontal text inset, in dp, for Cybergram bubbles (owner ruling D9.4). */
    public static final float BUBBLE_TEXT_INSET_DP = 2f;

    /** Extra leftward shift, in dp, of the outgoing delivery checks toward the time (D9.5). */
    public static final float CHECK_INSET_DP = 3f;

    /** Optical scale of outgoing delivery checks. Raj makes the metadata cluster visually smaller. */
    public static final float CHECK_SCALE = 0.84f;

    /**
     * Dialog-list assets are 42px wide versus the chat check assets' 36px.
     * Scale by 6/7 on top of the chat optical scale so both surfaces render
     * delivery status at the same visual size.
     */
    public static final float DIALOG_CHECK_SCALE = 0.72f;

    /** Metadata checks should sit behind the time instead of becoming a cyan badge. */
    public static final float CHECK_ALPHA_SCALE = 0.86f;

    /** Optical upward nudge, in dp, keeping checks aligned with the raised time baseline. */
    public static final float CHECK_Y_OFFSET_DP = 1f;

    /** Pure text messages use a more card-like width than upstream Telegram. */
    public static final float MESSAGE_TEXT_MAX_WIDTH_SCALE = 0.86f;

    /** Extra horizontal breathing room reserved for Cybergram attachment-heavy bubbles. */
    public static final float ATTACHMENT_TEXT_INSET_DP = 2.5f;

    /**
     * Extra right-side guard for long attachment captions. Telegram's caption StaticLayout is
     * biased toward the former tail side; reserving this only on the far edge keeps the left rail
     * where it is while preventing long lines from kissing the Cybergram outline.
     */
    public static final float ATTACHMENT_TEXT_RIGHT_GUARD_DP = 6f;

    /** Extra internal space below attachment/media content before the angular frame closes. */
    public static final float ATTACHMENT_BOTTOM_PAD_DP = 6f;

    /**
     * Media is intentionally edge-to-edge inside the Cybergram frame: the raster must reach the
     * <b>inner</b> edge of the visible outline and never paint past it.
     *
     * The outline is a stroked path on the {@link #BUBBLE_FRAME_PADDING_DP} silhouette, so the
     * inner edge sits at {@code frame padding + half stroke width}. Any smaller inset lets a
     * bright photo cover the frame (the media-overflow regression on channel posts with
     * caption/reactions/comments footers); any larger one leaves a grey seam.
     */
    public static final float ATTACHMENT_MEDIA_INSET_DP =
            BUBBLE_FRAME_PADDING_DP + BUBBLE_BORDER_WIDTH_DP * 0.5f;
    /** Media must never occupy more corner area than the parent bubble outline. */
    public static final float ATTACHMENT_MEDIA_CUT_DP = BUBBLE_CORNER_CUT_DP;

    /**
     * Phosphor/CRT treatment shared by avatars, media and (since the "whole bubble" pass) the
     * message surface itself.
     *
     * Owner ruling 2026-10-07: the wash was too faint and only the media block of a bubble carried
     * it. Both alphas were raised by ~1.7-1.8x so the filter reads as one screen treatment across
     * every surface, and the scanline geometry (step/height) is unchanged so the texture keeps its
     * period and the text baseline rhythm is not shifted.
     */
    public static final int ANALOG_TINT_ALPHA = 32;
    public static final int ANALOG_SCANLINE_ALPHA = 30;
    public static final float ANALOG_SCANLINE_STEP_DP = 3f;
    public static final float ANALOG_SCANLINE_HEIGHT_DP = 0.55f;

    /**
     * Swipe-to-reply control (owner ruling 2026-10-07). The control belongs to the incoming amber
     * family instead of the cyan service accent: the arrow is the accent mark, the ring around it
     * is deliberately quieter so the pair reads as an incoming-bubble affordance.
     */
    public static final int REPLY_SWIPE_ARROW_COLOR = AMBER;
    public static final int REPLY_SWIPE_RING_COLOR = IN_TIME;

    /** Additional top breathing room for forwarded headers and their media payloads. */
    public static final float FORWARDED_TOP_PAD_DP = 3f;

    /** Cybergram composer field height, in dp (owner ruling D9.7). Upstream is 44. */
    public static final int COMPOSER_HEIGHT_DP = 40;

    /** Cybergram chat-header avatar size, in dp (owner ruling D9.6). Upstream is 42. */
    public static final int HEADER_AVATAR_DP = 36;

    /** Dialog-list palette. Saturated colors are reserved for state, not decoration. */
    public static final int DIALOGS_TEXT_READ = IN_TEXT;
    public static final int DIALOGS_TEXT_UNREAD = 0xFFE5D8B8;
    public static final int DIALOGS_TEXT_PREVIEW = 0xFFA39D83;
    public static final int DIALOGS_TEXT_META = IN_TIME;
    public static final int DIALOGS_TEXT_SENDER = IN_TEXT;
    /** Database entry grammar: independent olive plate and quiet state-dependent frame. */
    public static final int DIALOGS_ROW_SURFACE = IN_BUBBLE;
    public static final int DIALOGS_ROW_SURFACE_ACTIVE = IN_BUBBLE_SELECTED;
    public static final float DIALOGS_ROW_CUT_DP = 5f;
    /** Dialog-list avatar silhouette: square body with small 45-degree corner chamfers. */
    public static final float DIALOGS_AVATAR_CUT_DP = 6f;
    public static final int DIALOGS_ROW_FRAME_ALPHA = 18;
    public static final int DIALOGS_ROW_FRAME_UNREAD_ALPHA = 64;
    public static final int DIALOGS_ROW_FRAME_SELECTED_ALPHA = 138;
    public static final int DIALOGS_RULE = 0xFF47121C;
    public static final int DIALOGS_ACTIVE = AMBER_HIGHLIGHT;
    public static final int DIALOGS_ATTENTION = AMBER_HIGHLIGHT;
    public static final int DIALOGS_ALERT = DANGER;

    /** Dialog-list grammar: structure stays quiet; state color appears only when meaningful. */
    public static final int DIALOGS_ROW_SEPARATOR_ALPHA = 168;
    public static final int DIALOGS_ROW_ACCENT_ALPHA = 188;
    public static final int DIALOGS_ROW_TICK_ALPHA = 0;
    public static final int DIALOGS_ROW_UNREAD_WASH_ALPHA = 7;
    public static final int DIALOGS_ROW_MENTION_WASH_ALPHA = 10;
    public static final int DIALOGS_ROW_SELECTED_WASH_ALPHA = 14;
    public static final int DIALOGS_ROW_SELECTED_RULE_ALPHA = 188;
    public static final int DIALOGS_COUNTER_MUTED_ALPHA = 108;
    public static final int DIALOGS_COUNTER_ACTIVE_ALPHA = 220;

    /**
     * Chat-list counters (unread count, mentions, reaction/poll mentions) are Cybergram HUD tags,
     * not stock Telegram pills: the shared eight-segment chamfer with a solid fill, a state-coloured
     * rim and the numeral in the same state colour. The fill is opaque so the numeral keeps a real
     * contrast ratio against the dark row instead of floating on the row background.
     */
    public static final float DIALOGS_BADGE_CUT_DP = 3.5f;
    public static final float DIALOGS_BADGE_BORDER_WIDTH_DP = 0.7f;
    public static final int DIALOGS_BADGE_FILL = PANEL_RAISED;
    public static final int DIALOGS_BADGE_FILL_ALPHA = 255;
    public static final int DIALOGS_BADGE_BORDER_ALPHA = 216;
    public static final int DIALOGS_BADGE_MUTED_BORDER_ALPHA = 132;
    public static final int DIALOGS_BADGE_MUTED_FILL_ALPHA = 214;
    /** Muted counters keep the same hue family as the read row instead of shouting attention amber. */
    public static final int DIALOGS_BADGE_MUTED_ACCENT = TEXT_MUTED;

    /**
     * Forum topic tab strip ({@code TopicsTabsView}). The selected topic used to be a Telegram-blue
     * Material pill with blue text; Cybergram replaces it with the shared chamfer and the cyan
     * accent, so the strip reads as the same HUD family as the dialog rows and the chat chrome.
     */
    public static final float TOPIC_TAB_CUT_DP = 5f;
    public static final int TOPIC_TAB_SELECTED_FILL_ALPHA = 30;
    public static final int TOPIC_TAB_SELECTED_BORDER_ALPHA = 150;
    public static final float TOPIC_TAB_SELECTED_BORDER_WIDTH_DP = 0.8f;

    /**
     * Shared interaction-shell geometry. Quick reactions and message context menus are separate
     * surfaces, but they should read as one component family rather than two unrelated HUD cards.
     */
    public static final float INTERACTION_PANEL_CUT_DP = 7f;
    public static final float INTERACTION_PANEL_BORDER_WIDTH_DP = 0.65f;
    public static final int INTERACTION_PANEL_BORDER_ALPHA = 120;
    public static final int INTERACTION_PANEL_ACCENT_ALPHA = 62;
    public static final float INTERACTION_PANEL_ACCENT_LENGTH_DP = 16f;

    /** Quick-reaction rail: lighter glass using the shared interaction shell. */
    public static final float REACTION_PANEL_CUT_DP = INTERACTION_PANEL_CUT_DP;
    public static final float REACTION_PANEL_BORDER_WIDTH_DP = INTERACTION_PANEL_BORDER_WIDTH_DP;
    public static final int REACTION_PANEL_BORDER_ALPHA = INTERACTION_PANEL_BORDER_ALPHA;
    public static final int REACTION_PANEL_FILL_ALPHA = 214;
    public static final int REACTION_PANEL_FLUFF_ALPHA = INTERACTION_PANEL_ACCENT_ALPHA;
    public static final float REACTION_EXPAND_CUT_DP = 5f;

    /** Popup/context menu plate: denser glass using the same shell geometry as quick reactions. */
    public static final float MESSAGE_MENU_CUT_DP = INTERACTION_PANEL_CUT_DP;
    public static final float MESSAGE_MENU_RADIUS_DP = 4f;
    public static final float MESSAGE_MENU_BORDER_WIDTH_DP = INTERACTION_PANEL_BORDER_WIDTH_DP;
    public static final int MESSAGE_MENU_FILL_ALPHA = 242;
    public static final int MESSAGE_MENU_BORDER_ALPHA = INTERACTION_PANEL_BORDER_ALPHA;
    public static final int MESSAGE_MENU_FLUFF_ALPHA = INTERACTION_PANEL_ACCENT_ALPHA;
    public static final int MESSAGE_MENU_SELECTOR_ALPHA = 24;

    /** Pinned strip follows the notification plate/bracket grammar rather than a Material card. */
    public static final float PINNED_PANEL_CUT_DP = 8f;
    public static final float PINNED_PANEL_BORDER_WIDTH_DP = 0.65f;
    public static final int PINNED_PANEL_FILL_ALPHA = 232;
    public static final int PINNED_PANEL_BORDER_ALPHA = 72;
    public static final float PINNED_PANEL_BRACKET_WIDTH_DP = 1f;
    public static final float PINNED_PANEL_BRACKET_LENGTH_DP = 13f;
    public static final int PINNED_PANEL_BRACKET_ALPHA = 148;
    public static final int PINNED_PANEL_ACCENT_ALPHA = 186;
    public static final int PINNED_RAIL_IDLE_ALPHA = 72;
    public static final int PINNED_RAIL_ACTIVE_ALPHA = 214;
    /** Compact top strip: dense enough for group chats without becoming a second action bar. */
    public static final int PINNED_PANEL_HEIGHT_DP = 44;
    public static final int PINNED_PANEL_TEXT_SIZE_DP = 13;

    /** Sender avatars inside multi-user chats follow the same clipped-corner grammar as the rest of Cybergram. */
    public static final float MESSAGE_AVATAR_CUT_DP = 6f;
    public static final float MESSAGE_AVATAR_FRAME_WIDTH_DP = 0.65f;
    public static final int MESSAGE_AVATAR_FRAME_ALPHA = 96;

    /** Inline message buttons: compact HUD plates instead of rounded Telegram service buttons. */
    public static final float BOT_BUTTON_CUT_DP = 7f;
    public static final float BOT_BUTTON_BORDER_WIDTH_DP = 0.65f;
    public static final int BOT_BUTTON_BORDER_ALPHA = 126;
    public static final int BOT_BUTTON_FILL_ALPHA = 236;
    public static final int BOT_BUTTON_PRESSED_FILL_ALPHA = 252;

    private static final Paint ANALOG_OVERLAY_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Draw a deliberately restrained analog-display wash: a pale cyan phosphor tint plus fine
     * horizontal scan lines. This is an overlay, not a destructive bitmap filter, so source media
     * keeps its contrast and remains readable.
     */
    public static void drawAnalogDisplayOverlay(Canvas canvas, RectF bounds, Path clipPath) {
        if (canvas == null || bounds == null || bounds.isEmpty()) {
            return;
        }
        final int save = canvas.save();
        if (clipPath != null) {
            canvas.clipPath(clipPath);
        } else {
            canvas.clipRect(bounds);
        }

        ANALOG_OVERLAY_PAINT.setStyle(Paint.Style.FILL);
        ANALOG_OVERLAY_PAINT.setColor(CYAN_SECONDARY);
        ANALOG_OVERLAY_PAINT.setAlpha(ANALOG_TINT_ALPHA);
        canvas.drawRect(bounds, ANALOG_OVERLAY_PAINT);

        ANALOG_OVERLAY_PAINT.setColor(0xFF000000);
        ANALOG_OVERLAY_PAINT.setAlpha(ANALOG_SCANLINE_ALPHA);
        final float step = Math.max(1f, AndroidUtilities.dpf2(ANALOG_SCANLINE_STEP_DP));
        final float line = Math.max(0.5f, AndroidUtilities.dpf2(ANALOG_SCANLINE_HEIGHT_DP));
        for (float y = bounds.top; y < bounds.bottom; y += step) {
            canvas.drawRect(bounds.left, y, bounds.right, Math.min(bounds.bottom, y + line), ANALOG_OVERLAY_PAINT);
        }
        canvas.restoreToCount(save);
    }

    /** Build the shared eight-segment chamfer used by Cybergram interaction surfaces. */
    public static void buildInteractionPanelPath(Path path, RectF bounds, float cutDp) {
        final float cut = Math.min(AndroidUtilities.dp(cutDp),
                Math.min(bounds.width(), bounds.height()) * 0.22f);
        path.rewind();
        path.moveTo(bounds.left + cut, bounds.top);
        path.lineTo(bounds.right - cut, bounds.top);
        path.lineTo(bounds.right, bounds.top + cut);
        path.lineTo(bounds.right, bounds.bottom - cut);
        path.lineTo(bounds.right - cut, bounds.bottom);
        path.lineTo(bounds.left + cut, bounds.bottom);
        path.lineTo(bounds.left, bounds.bottom - cut);
        path.lineTo(bounds.left, bounds.top + cut);
        path.close();
    }

    /**
     * Marker {@link Theme.ResourcesProvider} that opts a renderer into Cybergram
     * angular message geometry. The debug showcase implements this so it can exercise
     * the Cybergram silhouette even while the global user theme stays "Day".
     */
    public interface GeometryProvider extends Theme.ResourcesProvider {
    }

    /**
     * Centralised activation gate for Cybergram presentation.
     *
     * True when:
     *   - {@code provider} is a {@link GeometryProvider} (the debug showcase), OR
     *   - the active {@link Theme#getActiveTheme()} is the built-in Cybergram theme
     *     (a real client with Cybergram active usually has {@code provider == null}).
     *
     * The gate reads the <b>active</b> theme, not the day slot: {@link Theme#getCurrentTheme()}
     * returns {@code currentDayTheme}, so with Cybergram selected as the night theme the gate
     * was false while Cybergram was in fact the visible theme. {@link CybergramBackdropDrawable}
     * already reads {@code getActiveTheme()}; both sites must agree.
     *
     * False in every other case. It never infers Cybergram from colour values.
     */
    public static boolean isCybergramPresentation(Theme.ResourcesProvider provider) {
        if (provider instanceof GeometryProvider) {
            return true;
        }
        try {
            Theme.ThemeInfo info = Theme.getActiveTheme();
            if (info != null && THEME_NAME.equals(info.name)) {
                return true;
            }
        } catch (Throwable ignore) {
            // read-only gate; never propagate
        }
        return false;
    }

    /**
     * Activation gate for the Cybergram angular message geometry.
     *
     * Thin delegation to {@link #isCybergramPresentation(Theme.ResourcesProvider)} so the
     * message geometry and general HUD decoration share a single Cybergram detection path.
     */
    public static boolean useAngularMessageGeometry(Theme.ResourcesProvider provider) {
        return isCybergramPresentation(provider);
    }

    private CybergramTheme() {
        // Utility class.
    }
}
