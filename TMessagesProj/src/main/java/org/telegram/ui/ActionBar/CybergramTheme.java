package org.telegram.ui.ActionBar;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.View;
import android.view.ViewOutlineProvider;

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

    /**
     * Approved D2 hue palette (owner ruling 2026-09-15, docs/OWNER_DECISIONS_2026-09-15.md),
     * restored globally by the explicit owner override of 2026-10-13 (owner option "а", chat
     * 264566925) after the 2026-09-30 pass-2 experiment desaturated the hue cores. These five
     * values are the single source of truth for the hue roles; cybergram.attheme carries exactly
     * the same five.
     */
    public static final int CYAN = 0xFF00E5FF;
    public static final int CYAN_SECONDARY = 0xFF33D6FF;
    /** Ruled 2026-09-15: reference amber (was #E8D93A). See docs/OWNER_DECISIONS_2026-09-15.md. */
    public static final int AMBER = 0xFFFFB300;
    public static final int AMBER_HIGHLIGHT = 0xFFFFC94D;
    /** Ruled 2026-09-15: reference service red (was #FF2E46). */
    public static final int DANGER = 0xFFFF003C;

    public static final int TEXT = 0xFFE6F7FF;
    /**
     * Secondary/muted text and the muted counter numeral.
     *
     * Raised from #6B7A8A (audit 2026-10-11, P1 "preview/timestamp secondary text too
     * low-contrast"): the old value measured 4.31:1 on the dialogs row plate (IN_BUBBLE over the
     * window) and 4.10:1 on the muted counter plate (PANEL_RAISED at DIALOGS_BADGE_MUTED_FILL_ALPHA
     * over the row plate), both below the audit's WCAG 4.5:1 acceptance bar. This value holds
     * >= 4.7:1 on all three surfaces it is used on, staying a desaturated blue-grey.
     * Regression gate: Tools/validate_cybergram_theme.py, "WCAG contrast" section.
     */
    public static final int TEXT_MUTED = 0xFF78858F;
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
    /**
     * Round-3 FriendCall verdict 4, "secondary-text palette deviations".
     *
     * The unread title and the preview were the only two dialogs text literals outside the approved
     * palette (#E5D8B8 cream / #A39D83 olive-grey), so two unrelated hues carried the single
     * secondary / emphasis role of one screen. Both are now derived from the palette:
     *
     * <ul>
     *   <li>{@code DIALOGS_TEXT_UNREAD} takes the amber attention accent, the same hue family as
     *       the unread counter and the unread row stroke, so "unread" reads as one state.</li>
     *   <li>{@code DIALOGS_TEXT_PREVIEW} is secondary text drawn on the incoming olive row plate,
     *       where the round-3 guidance allows a muted warm tint, so it takes the palette's own muted
     *       warm metadata tint ({@link #IN_TIME}).</li>
     * </ul>
     *
     * Neither role is interactive, so neither uses cyan; the red and cyan semantics are untouched.
     * The neutral secondary {@code #6B7A8A} is deliberately NOT used on these WCAG-checked roles: it
     * is this gate's negative control and measures 4.10-4.50:1 on the dialogs plates, below the
     * 4.5:1 bar the audit set (see {@link #TEXT_MUTED}).
     */
    public static final int DIALOGS_TEXT_UNREAD = AMBER_HIGHLIGHT;
    public static final int DIALOGS_TEXT_PREVIEW = IN_TIME;
    public static final int DIALOGS_TEXT_META = IN_TIME;
    public static final int DIALOGS_TEXT_SENDER = IN_TEXT;
    /** Database entry grammar: independent olive plate and quiet state-dependent frame. */
    public static final int DIALOGS_ROW_SURFACE = IN_BUBBLE;
    public static final int DIALOGS_ROW_SURFACE_ACTIVE = IN_BUBBLE_SELECTED;
    public static final float DIALOGS_ROW_CUT_DP = 5f;
    /** Dialog-list avatar silhouette: square body with small 45-degree corner chamfers. */
    public static final float DIALOGS_AVATAR_CUT_DP = 6f;
    /**
     * Dialog-list avatar frame, owner ruling 2026-10-12 (round 7): the frame must be LEGIBLE.
     *
     * Rounds 3-6 progressively reduced this frame — round 3 thinned it, round 4 lowered the alpha to
     * 72 and washed the fill, round 6 kept a 0.4dp hairline — and the owner rejected the result
     * ("убранная обводка ошибочна: аватаркам она нужна — нужно ориентироваться на интерфейс
     * киберпанк 2077"). At emulator-5554's 440dpi the old pair covered
     * 0.4dp * 2.75 * 72/255 = 0.31 device px of full-opacity ink, i.e. it anti-aliased below the
     * visibility floor and read as no frame at all.
     *
     * The CP2077 codex entry grammar is a frame LAYER with two states, not one fixed alpha:
     * {@code codex_style.inkstyle} gives {@code EntryItem.frameOpacity} a quiet resting
     * {@code 0.0707399994} and raises it to {@code 1.0} on Hover and on Active, while
     * {@code codex_main.inkwidget} (handle 1165, name "frame") binds a dedicated 32x32 frame sprite
     * ({@code filter1_fg}, {@code atlas_shapes_sync.inkatlas}) to that opacity with an accent tint
     * ({@code CodexFilter.frameColor} / {@code MainColors.Red}) — a separate layer, never the plate.
     * Cybergram keeps that Rest -> Raised separation, but raises the resting opacity to the
     * legibility floor (CP2077's 0.07 is deliberately below it) while keeping the palette accent.
     *
     * The frame stays secondary chrome: its resting alpha is under every raised state, and the
     * raised states mirror the row frame's own unread/selected alphas. The gutter, the avatar rect,
     * the chamfer cut, the row height and every touch target are unchanged.
     */
    public static final float DIALOGS_AVATAR_FRAME_WIDTH_DP = 0.8f;
    /** Resting (read, no attention) frame: legible thin stroke — 0.8dp * 2.75 * 168/255 = 1.45 px. */
    public static final int DIALOGS_AVATAR_FRAME_ALPHA = 168;
    /** CP2077 Hover raised frame ({@code EntryItem.frameOpacity} = 1.0): unread / mention attention. */
    public static final int DIALOGS_AVATAR_FRAME_UNREAD_ALPHA = 232;
    /** CP2077 Active raised frame ({@code EntryItem.frameOpacity} = 1.0): selected row, full accent. */
    public static final int DIALOGS_AVATAR_FRAME_SELECTED_ALPHA = 255;
    /** Visual gap (dp) between the avatar gutter and the chamfered text plate; the old gap was 4dp. */
    public static final float DIALOGS_ROW_PANEL_INSET_DP = 2f;
    /**
     * Avatar / plate luminance reconciliation (round-4 FriendCall finding 1, still PARTIAL).
     *
     * Making the frame thinner and quieter did not close the gap: a saturated avatar fill (Saved
     * Messages, or a peer whose colour comes from its region/id) is far brighter than the olive
     * {@link #DIALOGS_ROW_SURFACE} plate, so the pair still read as a bright chip next to a dark
     * slab and the 2dp seam stayed visible. The avatar now carries a restrained wash of the
     * row-plate material, clipped to its own chamfer, which pulls its fill toward the plate's
     * luminance; the frame then draws on top of that wash as a legible stroke (round 7). This is an
     * overlay,
     * not a destructive filter: neither the gutter, the avatar rect, the chamfer cut nor any touch
     * target changes, and the row still separates avatar and plate (no merged capsule).
     */
    public static final int DIALOGS_AVATAR_SCRIM_COLOR = DIALOGS_ROW_SURFACE;
    public static final int DIALOGS_AVATAR_SCRIM_ALPHA = 88;
    /**
     * Avatar / plate JOINT material (round-6 FriendCall finding, "still two detached blocks").
     *
     * Round-3 thinned the avatar frame, round-4 washed the avatar fill, and neither closed the
     * seam: the 2dp gutter between the avatar and the text plate kept painting the window, so a
     * dark vertical sliver ran the full height of every row, the avatar's right chamfer and the
     * plate's left chamfer formed a double joint, and the two fills still read as different
     * materials. The gutter is therefore no longer a gap at all — the row surface is painted
     * BEHIND it, starting on the far side of the avatar's own chamfer, so avatar + plate are one
     * continuous row surface and the avatar is a chamfered inset ON that surface (it stays
     * distinct through its fill, its scrim and its single legible frame).
     *
     * These two tokens deliberately alias the row-plate surface tokens: the gutter must carry the
     * SAME material as the plate, never the window. They exist as named tokens so the geometry gate
     * can assert the alias instead of trusting a duplicated literal.
     */
    public static final int DIALOGS_ROW_GUTTER_FILL = DIALOGS_ROW_SURFACE;
    public static final int DIALOGS_ROW_GUTTER_FILL_ACTIVE = DIALOGS_ROW_SURFACE_ACTIVE;
    /**
     * Dialogs row frame, round-3 FriendCall verdict 2 ("decorative triple borders").
     *
     * Every dialogs shape is drawn with ONE main stroke. Selected / unread attention strengthens
     * that same stroke instead of stacking extra glow passes: the old {@code drawCybergramAmberOutline}
     * painted a 6.2dp bloom (alpha 11) + a 2.4dp mid halo (alpha 30) + a 0.5dp core on every row and
     * on every avatar, so each row read as three competing decorative borders. The chamfer cut and
     * the amber semantics are unchanged.
     */
    public static final float DIALOGS_ROW_FRAME_WIDTH_DP = BUBBLE_BORDER_WIDTH_DP;
    public static final int DIALOGS_ROW_FRAME_ALPHA = 140;
    public static final int DIALOGS_ROW_FRAME_UNREAD_ALPHA = 230;
    public static final int DIALOGS_ROW_FRAME_SELECTED_ALPHA = 250;
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
     * Dialog-list right guard (round-3 FriendCall verdict 3, "unread-counter crowding").
     *
     * The counter cluster is anchored this far inside the chamfered row frame and the preview text
     * stops this far before the frame — and before the nearest counter. The upstream BADGE_MARGIN
     * anchored the badge only ~5.66dp from the frame and the preview's layout box reached past the
     * counter's left edge, so a wide value such as 11175 crowded the text. The counter keeps its
     * state rim; the rim alphas stay below {@link #DIALOGS_ROW_FRAME_UNREAD_ALPHA}.
     */
    public static final float DIALOGS_RIGHT_GUARD_DP = 8f;

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

    /**
     * Emoji/sticker/GIF search field: the stock 18dp corner radius is replaced by the shared
     * chamfer. {@link #buildInteractionPanelPath} clamps the cut to 22% of the shorter side, so a
     * 36dp-tall field keeps a proportionally reduced cut and the polygon stays convex.
     */
    public static final float SEARCH_FIELD_CUT_DP = 18f;

    /**
     * Profile surface, Stage F round 8 (owner order профиль -> настройки -> вложения).
     *
     * <p>Two Profile-only seams carried the stock Material look: the hero avatar
     * ({@code ProfileActivity.AvatarImageView}, a circular ImageReceiver mask) and the action-plate
     * row ({@code ProfileActionsView}, three rounded capsules). Both are drawn by views that serve
     * no other surface, so {@link #isCybergramPresentation} is the whole opt-in.
     *
     * <p>The avatar keeps the round-7 Rest -> Raised grammar (see the
     * {@code DIALOGS_AVATAR_FRAME_*} block): one legible cyan frame on the shared eight-segment
     * chamfer. Resting is {@link #DIALOGS_AVATAR_FRAME_ALPHA}; the raised alpha is applied while
     * the avatar is touched, so the frame is a state layer and never a second decorative border.
     * The image itself loses its circular mask — the chamfer becomes the silhouette — but the
     * gutter, the layout box, the stories inset and every touch target stay upstream.
     */
    public static final float PROFILE_AVATAR_CUT_DP = 14f;
    public static final float PROFILE_AVATAR_FRAME_WIDTH_DP = 1.1f;
    public static final int PROFILE_AVATAR_FRAME_ALPHA = DIALOGS_AVATAR_FRAME_ALPHA;
    public static final int PROFILE_AVATAR_FRAME_RAISED_ALPHA = 255;

    /**
     * Action-plate row ({@code ProfileActionsView}). Same grammar as the inline message buttons
     * ({@link #BOT_BUTTON_CUT_DP}): a near-black {@link #PANEL_RAISED} fill with ONE thin cyan rail
     * and no glow passes, so the three plate shapes read as one HUD strip instead of three
     * peer-tinted capsules. The rect each action already uses for hit-testing is untouched.
     */
    public static final float PROFILE_ACTION_CUT_DP = 7f;
    public static final float PROFILE_ACTION_BORDER_WIDTH_DP = 0.65f;
    public static final int PROFILE_ACTION_BORDER_ALPHA = 126;
    public static final int PROFILE_ACTION_FILL_ALPHA = 236;

    /**
     * Settings surface, Stage F round 9 (owner order профиль -> настройки -> вложения).
     *
     * <p>{@code SettingsActivity} is the root settings screen — the third main tab, created at
     * {@code MainTabsActivity.java:884}. Its stock Material shapes were the hero avatar (a 90dp
     * circular ImageReceiver mask), the account rows ({@code AccountCell}, 28dp circular avatars),
     * the setting rows ({@code SettingCell}, 28dp rounded gradient chips) and the settings
     * search-result icon plates ({@code Cells/SettingsSearchCell}, 48dp bare icons). Everything
     * else on the root (headers, shadow sections, plain text) is already flat.
     *
     * <p>The hero avatar and the two cells nested in {@code SettingsActivity} are reached by no
     * other surface, so {@link #isCybergramPresentation} is their whole opt-in. {@code
     * SettingsSearchCell} also renders ProfileActivity's own settings-search list, so it takes an
     * explicit LOCAL opt-in instead: only its {@code Factory} — reached exclusively from the
     * settings root through {@code UniversalAdapter} — enables the chamfer, while the direct
     * constructor keeps the upstream shape. No global theme change (backlog B8 rule).
     *
     * <p>Grammar: avatars keep the round-7 Rest -> Raised rail
     * ({@link #DIALOGS_AVATAR_FRAME_ALPHA} -> {@link #SETTINGS_AVATAR_FRAME_RAISED_ALPHA}); the
     * chips/plates use the inline-button grammar ({@link #BOT_BUTTON_CUT_DP}) — ONE thin cyan rail
     * over the existing category fill, no glow passes, no new hues.
     */
    public static final float SETTINGS_AVATAR_CUT_DP = 12f;
    public static final float SETTINGS_AVATAR_FRAME_WIDTH_DP = 1.1f;
    public static final int SETTINGS_AVATAR_FRAME_ALPHA = DIALOGS_AVATAR_FRAME_ALPHA;
    public static final int SETTINGS_AVATAR_FRAME_RAISED_ALPHA = 255;

    /** 28dp account avatar / setting-row icon chip: shared cut, one thin cyan rail. */
    public static final float SETTINGS_CHIP_CUT_DP = 5f;
    public static final float SETTINGS_CHIP_BORDER_WIDTH_DP = BOT_BUTTON_BORDER_WIDTH_DP;
    public static final int SETTINGS_CHIP_BORDER_ALPHA = BOT_BUTTON_BORDER_ALPHA;
    public static final int SETTINGS_CHIP_BORDER_RAISED_ALPHA = 255;
    public static final int SETTINGS_CHIP_FILL_ALPHA = BOT_BUTTON_FILL_ALPHA;
    /** 48dp settings search-result icon plate: same grammar, slightly larger cut. */
    public static final float SETTINGS_SEARCH_PLATE_CUT_DP = 6f;

    /**
     * Round-11 Stage F attach surface, surface 3 of 3 (owner order 2026-10-13:
     * profile -> settings -> attachments): the paperclip attach-alert menu owned by
     * {@code ui/Components/ChatAttachAlert}.
     *
     * The alert carried two large stock Material capsules: the 70dp attach-type tab strip (a
     * translucent 28dp-radius glass capsule behind the Gallery/File/Music/Location/Contact/Poll
     * tabs) and the 28dp-tall "Create" plate on its action bar. {@code docs/CYBERGRAM_UI_SPEC.md}
     * line 21 rules large rounded/glass Material capsules an anti-target inside Cybergram
     * presentation, so both are replaced through narrow gated seams with the shared eight-segment
     * chamfer plus ONE thin cyan rail. The rail/fill tokens alias the round-9 settings chip values
     * (themselves the inline-button grammar), so the attach surface speaks the already-accepted
     * chip grammar instead of inventing a second visual language.
     *
     * Geometry, hit rects and touch targets are untouched: the strip keeps its 70dp wrapper, its
     * dp(11) tab padding and its dp(11)/dp(28) content clip, and the round-11 plate is drawn inside
     * the very same dp(7) inset the glass capsule used; the action plate keeps its 28dp box.
     */
    public static final float ATTACH_TAB_STRIP_CUT_DP = 10f;
    public static final float ATTACH_TAB_STRIP_BORDER_WIDTH_DP = BOT_BUTTON_BORDER_WIDTH_DP;
    public static final int ATTACH_TAB_STRIP_BORDER_ALPHA = BOT_BUTTON_BORDER_ALPHA;
    public static final int ATTACH_TAB_STRIP_FILL_ALPHA = BOT_BUTTON_FILL_ALPHA;
    /** 28dp-tall attach-alert action plate ("Create"): the same chamfer grammar as the chips. */
    public static final float ATTACH_ACTION_CUT_DP = 6f;

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
     * Fill-only drawable whose silhouette is the shared Cybergram chamfer
     * ({@link #buildInteractionPanelPath}) instead of a round rect. Drop-in replacement for
     * {@link Theme#createRoundRectDrawable(int, int)} on gated surfaces.
     *
     * <p>The result is a {@link ShapeDrawable}, so existing colour refreshes that go through
     * {@link Theme#setDrawableColor(Drawable, int)} keep working unchanged.
     *
     * @param color ARGB fill; the caller keeps its own colour lookup untouched.
     * @param cutDp corner cut in dp, clamped inside {@link #buildInteractionPanelPath}.
     */
    public static Drawable createChamferedPanelDrawable(int color, float cutDp) {
        final ShapeDrawable drawable = new ShapeDrawable(new InteractionPanelShape(cutDp));
        drawable.getPaint().setAntiAlias(true);
        drawable.getPaint().setStyle(Paint.Style.FILL);
        drawable.getPaint().setColor(color);
        return drawable;
    }

    /**
     * Ripple selector whose press shape is the shared Cybergram chamfer
     * ({@link #buildInteractionPanelPath}) instead of the stock circular mask. Keeps a real
     * {@link RippleDrawable} — only the mask silhouette changes — so the stock press feedback
     * survives. Gated callers pass their own themed colour unchanged.
     *
     * @param color ripple colour, normally the stock selector colour.
     * @param cutDp corner cut in dp, clamped inside {@link #buildInteractionPanelPath}.
     */
    public static Drawable createChamferedRippleDrawable(int color, float cutDp) {
        return new RippleDrawable(ColorStateList.valueOf(color), null, createChamferedPanelDrawable(Color.WHITE, cutDp));
    }

    /**
     * Outline provider clipping a view (and its children) to the same chamfer polygon
     * {@link #createChamferedPanelDrawable(int, float)} paints, so the clip mask and the fill
     * agree. Pair it with {@code view.setClipToOutline(true)}.
     */
    public static ViewOutlineProvider chamferOutlineProvider(float cutDp) {
        return new ViewOutlineProvider() {
            private final Path path = new Path();
            private final RectF rect = new RectF();

            @Override
            public void getOutline(View view, Outline outline) {
                final int width = view.getWidth();
                final int height = view.getHeight();
                if (width <= 0 || height <= 0) {
                    outline.setEmpty();
                    return;
                }
                rect.set(0f, 0f, width, height);
                buildInteractionPanelPath(path, rect, cutDp);
                outline.setConvexPath(path);
            }
        };
    }

    /**
     * Bordered sibling of {@link #createChamferedPanelDrawable(int, float)}: the shared
     * eight-segment chamfer painted as an optional fill plus ONE optional stroke rail. It exists so
     * the round-9 settings chips/plates carry their rail without each cell re-implementing a
     * {@link Drawable}. Both colours are Cybergram-owned constants, so this drawable needs no
     * themed refresh ({@code Theme.setDrawableColor} is not used on it).
     *
     * @param fillColor    ARGB fill, or 0 to skip the fill
     * @param fillAlpha    0..255 fill alpha
     * @param borderColor  ARGB rail, or 0 to skip the rail
     * @param borderAlpha  0..255 rail alpha
     * @param borderWidthDp rail stroke width in dp (inset by half its own width)
     * @param cutDp        corner cut in dp, clamped inside {@link #buildInteractionPanelPath}
     */
    public static Drawable createChamferedPlateDrawable(
            int fillColor, int fillAlpha,
            int borderColor, int borderAlpha,
            float borderWidthDp, float cutDp
    ) {
        return createChamferedPlateDrawable(fillColor, fillAlpha, borderColor, borderAlpha,
                borderWidthDp, cutDp, 0);
    }

    /**
     * Same plate as {@link #createChamferedPlateDrawable(int, int, int, int, float, float)}, but
     * the whole silhouette (fill AND rail) is drawn inside a uniform {@code insetPx} inset, so a
     * gated caller that REPLACES an existing drawable can keep that drawable's painted footprint.
     * The round-11 attach tab strip uses it to take over from the stock glass capsule, which paints
     * inside its own {@code dp(7)} padding: without the inset the chamfer plate would be 14dp wider
     * and taller than the capsule it replaces. The inset is a drawing-time inset only - it is never
     * reported through {@link Drawable#getPadding(Rect)} - so it cannot move a child or a hit rect.
     *
     * @param cutDp        corner cut in dp, clamped inside {@link #buildInteractionPanelPath}
     * @param insetPx      uniform drawing-time inset in px (0 = paint the full bounds)
     */
    public static Drawable createChamferedPlateDrawable(
            int fillColor, int fillAlpha,
            int borderColor, int borderAlpha,
            float borderWidthDp, float cutDp, int insetPx
    ) {
        return new ChamferPlateDrawable(fillColor, fillAlpha, borderColor, borderAlpha,
                borderWidthDp, cutDp, insetPx);
    }

    /** Fill + single-stroke chamfer plate; see {@link #createChamferedPlateDrawable}. */
    private static final class ChamferPlateDrawable extends Drawable {
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int fillColor, fillAlpha, borderColor, borderAlpha;
        private final float borderWidthDp, cutDp;
        /** Drawing-time inset (px) reproducing the footprint of the drawable this one replaces. */
        private final int insetPx;

        ChamferPlateDrawable(
                int fillColor, int fillAlpha,
                int borderColor, int borderAlpha,
                float borderWidthDp, float cutDp, int insetPx
        ) {
            this.insetPx = insetPx;
            this.fillColor = fillColor;
            this.fillAlpha = fillAlpha;
            this.borderColor = borderColor;
            this.borderAlpha = borderAlpha;
            this.borderWidthDp = borderWidthDp;
            this.cutDp = cutDp;
        }

        @Override
        public void draw(Canvas canvas) {
            final Rect bounds = getBounds();
            if (bounds.isEmpty()) {
                return;
            }
            if (fillColor != 0 && fillAlpha > 0) {
                rect.set(bounds);
                rect.inset(insetPx, insetPx);
                if (rect.width() > 0f && rect.height() > 0f) {
                    buildInteractionPanelPath(path, rect, cutDp);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(fillColor);
                    paint.setAlpha(fillAlpha);
                    canvas.drawPath(path, paint);
                }
            }
            if (borderColor != 0 && borderAlpha > 0 && borderWidthDp > 0f) {
                final float stroke = AndroidUtilities.dpf2(borderWidthDp);
                rect.set(bounds);
                rect.inset(insetPx, insetPx);
                rect.inset(stroke / 2f, stroke / 2f);
                if (rect.width() <= 0f || rect.height() <= 0f) {
                    return;
                }
                buildInteractionPanelPath(path, rect, cutDp);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.MITER);
                paint.setStrokeWidth(stroke);
                paint.setColor(borderColor);
                paint.setAlpha(borderAlpha);
                canvas.drawPath(path, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /** Fill-only {@link Shape} painting the shared eight-segment interaction-panel chamfer. */
    private static final class InteractionPanelShape extends Shape {
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final float cutDp;

        InteractionPanelShape(float cutDp) {
            this.cutDp = cutDp;
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final float width = getWidth();
            final float height = getHeight();
            if (width <= 0f || height <= 0f) {
                return;
            }
            rect.set(0f, 0f, width, height);
            buildInteractionPanelPath(path, rect, cutDp);
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean hasAlpha() {
            return true;
        }
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
