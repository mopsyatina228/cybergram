#!/usr/bin/env python3
"""Regression check for the Cybergram bubble-frame / media / story geometry.

The three owner-reported regressions (screenshots in
`.local-artifacts/cybergram-task/`, fixes in commit 43b5c3fd1) all had the same
root cause: two implementations of "where the frame is" disagreed.

  1. the video/media raster painted a few px past the angular bubble frame;
  2. story avatars kept a circular ImageReceiver mask inside a chamfered frame;
  3. forum topic tabs kept the Telegram-blue round pill / round counter.

The fix made one token authoritative (`CybergramTheme.BUBBLE_FRAME_PADDING_DP`)
and derived every other inset / clip from it. This script is a deterministic,
emulator-free cross-check of those invariants:

  [A] the derived float tokens still satisfy their defining relationships;
  [B] `MessageDrawable.generateCybergramPath` still builds the silhouette from
      the single frame-padding token (no hardcoded second frame);
  [C] `ChatMessageCell` derives the media safe-rect from the same token, only
      reshapes real attachments, and clamps the media chamfer;
  [D] story avatars still route through the shared chamfer path, and the round
      ImageReceiver radius is suppressed under Cybergram;
  [E] the topic tab strip / counters still use the chamfered Cybergram chrome;
  [F] independently re-implements the chamfer polygon and proves the media rect
      stays inside the bubble silhouette across a density / size matrix, with a
      negative control so the check is provably sensitive.

This is a source + geometry check. It does NOT replace the on-device visual
pass documented in `.local-artifacts/cybergram-state.md`.

Usage:
    python3 Tools/validate_cybergram_geometry.py
Exit code is non-zero if any invariant is broken.
"""

import math
import pathlib
import re
import sys

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
JAVA = REPO_ROOT / "TMessagesProj/src/main/java/org/telegram"

THEME = JAVA / "ui/ActionBar/CybergramTheme.java"
MESSAGE_DRAWABLE = JAVA / "ui/ActionBar/MessageDrawable.java"
CHAT_MESSAGE_CELL = JAVA / "ui/Cells/ChatMessageCell.java"
DIALOG_CELL = JAVA / "ui/Cells/DialogCell.java"
STORIES_UTILITIES = JAVA / "ui/Stories/StoriesUtilities.java"
DIALOG_STORIES_CELL = JAVA / "ui/Stories/DialogStoriesCell.java"
TOPICS_TABS = JAVA / "ui/Components/TopicsTabsView.java"
MAIN_TABS_ACTIVITY = JAVA / "ui/MainTabsActivity.java"
MAIN_TABS_LAYOUT = JAVA / "ui/MainTabsLayout.java"
AVATAR_DRAWABLE = JAVA / "ui/Components/AvatarDrawable.java"

FLOAT_DECL = re.compile(
    r"static final (?:float|double)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*([^;]+);"
)

_results = []


def check(label, ok, detail=""):
    _results.append((label, bool(ok)))
    print("[%s] %s%s" % ("PASS" if ok else "FAIL", label, ("  -- " + detail) if detail else ""))
    return bool(ok)


# --------------------------------------------------------------------------- #
# Java float-token parsing
# --------------------------------------------------------------------------- #

def raw_constants(text):
    return {m.group(1): m.group(2).strip() for m in FLOAT_DECL.finditer(text)}


def _eval_expr(expr, values, name):
    for other, val in sorted(values.items(), key=lambda kv: -len(kv[0])):
        if other == name or val is None:
            continue
        expr = re.sub(r"\b%s\b" % re.escape(other), "(%r)" % val, expr)
    expr = re.sub(r"(?<=[0-9.])f\b", "", expr)
    expr = expr.replace("(float)", "").replace("(int)", "")
    if re.search(r"[A-Za-z_]", expr):
        return None
    try:
        return float(eval(expr, {"__builtins__": {}}, {}))
    except Exception:
        return None


def resolve_all(text):
    """Resolve numeric float tokens, following references to other tokens."""
    raw = raw_constants(text)
    values = {}
    for _ in range(len(raw) + 2):
        changed = False
        for key, expr in raw.items():
            resolved = _eval_expr(expr, values, key)
            if resolved is not None and values.get(key) != resolved:
                values[key] = resolved
                changed = True
        if not changed:
            break
    return values


INT_DECL = re.compile(
    r"static final int\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*([^;]+);"
)


def resolve_ints(text):
    """Resolve integer tokens (alphas, counts) declared in CybergramTheme."""
    raw = {m.group(1): m.group(2).strip() for m in INT_DECL.finditer(text)}
    values = {}
    for _ in range(len(raw) + 2):
        changed = False
        for key, expr in raw.items():
            e = expr.strip()
            if re.fullmatch(r"0[xX][0-9A-Fa-f]+[lL]?", e) or re.fullmatch(r"-?\d+", e):
                resolved = int(e.rstrip("lL"), 0)
            else:
                resolved = values.get(e)
            if resolved is not None and values.get(key) != resolved:
                values[key] = resolved
                changed = True
        if not changed:
            break
    return values


# --------------------------------------------------------------------------- #
# Independent geometry model (mixture of CybergramBubbleDrawable.buildPath and
# AndroidUtilities.dp -> (int) Math.ceil(density * value))
# --------------------------------------------------------------------------- #

def dp_ceil(value, density):
    if value == 0:
        return 0
    return int(math.ceil(density * value))


def chamfer_polygon(left, top, right, bottom, tl, tr, br, bl):
    """Port of CybergramBubbleDrawable.buildPath (four 45-degree chamfers)."""
    if right <= left or bottom <= top:
        return []
    max_cut = min(right - left, bottom - top) * 0.5
    tl, tr, br, bl = (min(c, max_cut) for c in (tl, tr, br, bl))
    return [
        (left + tl, top),
        (right - tr, top),
        (right, top + tr),
        (right, bottom - br),
        (right - br, bottom),
        (left + bl, bottom),
        (left, bottom - bl),
        (left, top + tl),
    ]


def point_in_convex(poly, point, eps=1e-6):
    if len(poly) < 3:
        return False
    sign = 0
    for i in range(len(poly)):
        ax, ay = poly[i]
        bx, by = poly[(i + 1) % len(poly)]
        cross = (bx - ax) * (point[1] - ay) - (by - ay) * (point[0] - ax)
        if abs(cross) <= eps:
            continue
        s = 1 if cross > 0 else -1
        if sign == 0:
            sign = s
        elif s != sign:
            return False
    return True


def media_inside(frame_pad, media_inset, media_cut, corner_cut, density, bounds):
    """True when the chamfered media rect lies inside the media-shaped bubble.

    Mirrors:
      MessageDrawable.generateCybergramPath  -> TYPE_MEDIA body inset by
                                                dp(BUBBLE_FRAME_PADDING_DP)
      ChatMessageCell photoImage.draw        -> safe rect inset by
                                                dp(ATTACHMENT_MEDIA_INSET_DP),
                                                chamfer clamped by
                                                dp(ATTACHMENT_MEDIA_CUT_DP)
    """
    left, top, right, bottom = bounds

    pad = dp_ceil(frame_pad, density)
    cut = dp_ceil(corner_cut, density)
    bubble = chamfer_polygon(left + pad, top + pad, right - pad, bottom - pad,
                             cut, cut, cut, cut)

    inset = dp_ceil(media_inset, density)
    ml, mt = left + inset, top + inset
    mr, mb = right - inset, bottom - inset
    media_cut_px = min(dp_ceil(media_cut, density),
                       max(0.0, min(mr - ml, mb - mt) * 0.16))
    media = chamfer_polygon(ml, mt, mr, mb,
                            media_cut_px, media_cut_px, media_cut_px, media_cut_px)

    if not bubble or not media:
        return False
    return all(point_in_convex(bubble, p) for p in media)


DENSITIES = [1.0, 1.5, 2.0, 2.625, 3.0, 4.0]
SIZES_DP = [(60, 40), (120, 80), (200, 150), (300, 220), (480, 340)]


# --------------------------------------------------------------------------- #

def main():
    print("== Cybergram geometry / clip check ==")

    paths = {
        "theme": THEME,
        "message_drawable": MESSAGE_DRAWABLE,
        "chat_message_cell": CHAT_MESSAGE_CELL,
        "dialog_cell": DIALOG_CELL,
        "stories_utilities": STORIES_UTILITIES,
        "dialog_stories_cell": DIALOG_STORIES_CELL,
        "topics_tabs": TOPICS_TABS,
        "main_tabs_activity": MAIN_TABS_ACTIVITY,
        "main_tabs_layout": MAIN_TABS_LAYOUT,
        "avatar_drawable": AVATAR_DRAWABLE,
    }
    texts = {}
    for name, path in paths.items():
        if not path.is_file():
            check("source file present: %s" % path.relative_to(REPO_ROOT), False, "missing")
            _summary()
            return
        texts[name] = path.read_text(encoding="utf-8", errors="replace")

    vals = resolve_all(texts["theme"])

    def token(name):
        return vals.get(name)

    frame_pad = token("BUBBLE_FRAME_PADDING_DP")
    corner_cut = token("BUBBLE_CORNER_CUT_DP")
    border_w = token("BUBBLE_BORDER_WIDTH_DP")
    tail_gutter = token("BUBBLE_TAIL_GUTTER_DP")
    media_inset = token("ATTACHMENT_MEDIA_INSET_DP")
    media_cut = token("ATTACHMENT_MEDIA_CUT_DP")
    gap_extra = token("BUBBLE_GAP_EXTRA_DP")
    gap_max = token("BUBBLE_GAP_MAX_DP")
    dialog_avatar_cut = token("DIALOGS_AVATAR_CUT_DP")

    # ---- [A] token relationships -----------------------------------------
    print("\n[A] derived token relationships")
    missing = [n for n, v in [
        ("BUBBLE_FRAME_PADDING_DP", frame_pad),
        ("BUBBLE_BORDER_WIDTH_DP", border_w),
        ("BUBBLE_CORNER_CUT_DP", corner_cut),
        ("BUBBLE_TAIL_GUTTER_DP", tail_gutter),
        ("ATTACHMENT_MEDIA_INSET_DP", media_inset),
        ("ATTACHMENT_MEDIA_CUT_DP", media_cut),
        ("BUBBLE_GAP_EXTRA_DP", gap_extra),
        ("BUBBLE_GAP_MAX_DP", gap_max),
        ("DIALOGS_AVATAR_CUT_DP", dialog_avatar_cut),
    ] if v is None]
    check("all geometry tokens parse to numbers", not missing,
          "unparsed: " + ", ".join(missing) if missing else "")

    if missing:
        _summary()
        return

    check("ATTACHMENT_MEDIA_INSET_DP == BUBBLE_FRAME_PADDING_DP + BUBBLE_BORDER_WIDTH_DP/2",
          abs(media_inset - (frame_pad + border_w * 0.5)) < 1e-6,
          "media_inset=%.4f expected=%.4f" % (media_inset, frame_pad + border_w * 0.5))
    check("ATTACHMENT_MEDIA_INSET_DP > BUBBLE_FRAME_PADDING_DP (no media-over-frame)",
          media_inset > frame_pad,
          "media_inset=%.4f frame_pad=%.4f" % (media_inset, frame_pad))
    check("ATTACHMENT_MEDIA_CUT_DP <= BUBBLE_CORNER_CUT_DP (media inside outline)",
          media_cut <= corner_cut + 1e-6,
          "media_cut=%.4f corner_cut=%.4f" % (media_cut, corner_cut))
    check("BUBBLE_GAP_EXTRA_DP <= BUBBLE_GAP_MAX_DP (clamped vertical gap)",
          gap_extra <= gap_max + 1e-6,
          "extra=%.4f max=%.4f" % (gap_extra, gap_max))
    check("BUBBLE_TAIL_GUTTER_DP >= BUBBLE_FRAME_PADDING_DP (tail reserve sane)",
          tail_gutter >= frame_pad,
          "gutter=%.4f frame_pad=%.4f" % (tail_gutter, frame_pad))

    # ---- [B] MessageDrawable single source of truth ----------------------
    print("\n[B] MessageDrawable silhouette source")
    md = texts["message_drawable"]
    sig = "private void generateCybergramPath(Path path, Rect bounds) {"
    start = md.find(sig)
    if start < 0:
        check("generateCybergramPath found", False, "signature not found")
        body = ""
    else:
        end = md.find("\n    private ", start + len(sig))
        body = md[start:end if end > 0 else len(md)]
        check("generateCybergramPath found", True)

    check("silhouette inset uses CybergramTheme.BUBBLE_FRAME_PADDING_DP",
          "dp(CybergramTheme.BUBBLE_FRAME_PADDING_DP)" in body)
    check("silhouette built by shared CybergramBubbleDrawable.buildTailedPath",
          "CybergramBubbleDrawable.buildTailedPath(" in body)
    check("no hardcoded second frame inset (dp(2f)) in the path",
          re.search(r"dp\(\s*2f?\s*\)", body) is None,
          "hardcoded dp(2f) reintroduces a second frame" if re.search(r"dp\(\s*2f?\s*\)", body) else "")
    check("text body keeps BUBBLE_TAIL_GUTTER_DP reserve",
          "dp(CybergramTheme.BUBBLE_TAIL_GUTTER_DP)" in body)

    # ---- [C] ChatMessageCell media safe-rect -----------------------------
    print("\n[C] ChatMessageCell media clip")
    cmc = texts["chat_message_cell"]
    check("media safe-rect derived from ATTACHMENT_MEDIA_INSET_DP",
          "dp(CybergramTheme.ATTACHMENT_MEDIA_INSET_DP)" in cmc)
    check("media chamfer derived from ATTACHMENT_MEDIA_CUT_DP",
          "dp(CybergramTheme.ATTACHMENT_MEDIA_CUT_DP)" in cmc)
    check("link previews (TYPE_TEXT) are clipped, not stretched",
          "currentMessageObject.type != MessageObject.TYPE_TEXT" in cmc)
    check("degenerate safe-rect is rejected before use",
          "safeRight > safeLeft && safeBottom > safeTop" in cmc)

    # ---- [D] Stories chamfer silhouette ----------------------------------
    print("\n[D] story avatar silhouette")
    dsc = texts["dialog_stories_cell"]
    su = texts["stories_utilities"]
    dc = texts["dialog_cell"]
    check("DialogStoriesCell sets avatarChamferCutDp under the Cybergram gate",
          "params.avatarChamferCutDp = cybergramStory" in dsc)
    check("DialogStoriesCell ring uses the shared chamfer path",
          "CybergramTheme.buildInteractionPanelPath(" in dsc)
    check("DialogStoriesCell suppresses the round radius while expanded",
          "avatarImage.setRoundRadius(cybergramStory" in dsc)
    check("StoriesUtilities routes chamfered avatars to drawAngularAvatar",
          "if (params.avatarChamferCutDp > 0f)" in su
          and "drawAngularAvatar(canvas, avatarImage, params, storiesController, scale);" in su)
    check("drawAvatarImage clips the raster by the chamfer path",
          "if (params.avatarChamferCutDp <= 0f)" in su and "canvas.clipPath(params.avatarClipPath)" in su)
    check("DialogCell uses a square avatar radius under Cybergram",
          "avatarRadius = 0" in dc)
    check("DialogCell story params carry the chamfer cut",
          "storyParams.avatarChamferCutDp" in dc)
    check("DialogCell has the chamfered Cybergram badge path",
          "drawCybergramBadge(" in dc)

    # ---- [E] Topic tab strip ---------------------------------------------
    print("\n[E] topic tab strip chrome")
    tt = texts["topics_tabs"]
    check("TopicsTabsView gated by CybergramTheme.isCybergramPresentation",
          "CybergramTheme.isCybergramPresentation(provider)" in tt)
    check("selected/pinned topic plate uses the chamfer path",
          "CybergramTheme.buildInteractionPanelPath(" in tt
          and "CybergramTheme.TOPIC_TAB_CUT_DP" in tt)
    check("topic counters use the chamfered badge chrome",
          "CybergramTheme.DIALOGS_BADGE_CUT_DP" in tt)
    check("blur capsule radius reduced for the angular strip",
          re.search(r"setRadius\(dp\(isCybergramTabs\(\) \? 4 : 16\)\)", tt) is not None
          and re.search(r"setRadius\(dp\(isCybergramTabs\(\) \? 4 : 18\)\)", tt) is not None)

    # ---- [G] main-tabs ribbon reaches the navigation-bar inset -----------
    # Owner-reported P0-1: a dialogs row was visible below the Chats/Contacts/Settings/Profile
    # tabs, inside the navigation-bar strip. The ribbon plate is only
    # MAIN_TABS_HEIGHT_WITH_MARGINS tall and its wrapper reserves the bottom inset as padding,
    # which upstream leaves to the blurred fade. Under the opaque Cybergram ribbon that inset was
    # simply not painted, so the list scrolled out below the tabs. These checks pin the fix.
    print("\n[G] main-tabs ribbon covers the navigation-bar inset")
    mta = texts["main_tabs_activity"]
    mtl = texts["main_tabs_layout"]

    check("MainTabsLayout (plate) is a child of tabsViewWrapper",
          "tabsViewWrapper.addView(tabsView" in mta)
    check("wrapper reserves the navigation-bar inset as bottom padding",
          "tabsViewWrapper.setPadding(systemInsets.left, 0, systemInsets.right, navigationBarHeight)" in mta)
    check("Cybergram paints the shared panel surface across the whole wrapper",
          "tabsViewWrapper.setBackgroundColor(cybergram ? CybergramTheme.PANEL : 0)" in mta)
    after_add = mta.find("contentView.addView(tabsViewWrapper")
    check("the surface is applied after the wrapper exists (not a null no-op)",
          after_add >= 0 and mta.find("applyTabsPresentationBackground();", after_add) > after_add)
    check("the ribbon plate itself keeps MainTabsLayout's panel paint",
          "cybergramRibbonPaint.setColor(CybergramTheme.PANEL)" in mtl)

    # Numeric coverage model: wrapper height = plate + reserved inset. The fix paints the whole
    # wrapper, so painted == reserved; an unpainted wrapper leaves the inset exposed.
    MAIN_TABS_PLATE_DP = 72.0  # DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS

    def ribbon_coverage(nav_h, density, wrapper_painted):
        plate = dp_ceil(MAIN_TABS_PLATE_DP, density)
        reserved = plate + nav_h
        painted = reserved if wrapper_painted else plate
        return reserved, painted

    gaps = []
    for nav_h in (0, 48, 66, 90):
        for density in (2.0, 2.625, 2.75, 3.0):
            reserved, painted = ribbon_coverage(nav_h, density, True)
            if painted != reserved:
                gaps.append("nav=%d dens=%.3f" % (nav_h, density))
    check("painted ribbon height equals the reserved plate + inset region",
          not gaps, "uncovered: " + ", ".join(gaps[:4]) if gaps else "")

    # Negative control: without the wrapper surface the pre-fix geometry must expose a gap on
    # every device that has a navigation-bar inset, otherwise this check is not sensitive.
    exposed = [
        (nav_h, density)
        for nav_h in (48, 66, 90)
        for density in (2.0, 2.625, 2.75, 3.0)
        if ribbon_coverage(nav_h, density, False)[1] < ribbon_coverage(nav_h, density, False)[0]
    ]
    check("negative control: an unpainted wrapper exposes the inset (pre-fix symptom)",
          len(exposed) == 12, "cases detected: %d" % len(exposed))

    # ---- [H] avatar shape system ----------------------------------------
    # Owner-reported P1: the dialogs list mixed circular and square avatars. Bitmap avatars follow
    # the chamfered ImageReceiver clip (see DialogCell "avatarRadius = 0"), but drawable-backed
    # avatars (saved messages, archive, filters, letter avatars) were painted by AvatarDrawable with
    # a plain circle, so both shapes appeared in the same list. These checks pin the single shape.
    print("\n[H] avatar plate uses the shared chamfer")
    av = texts["avatar_drawable"]

    check("AvatarDrawable gates the plate on the Cybergram presentation",
          "cybergramPlate = drawAvatarBackground && !rotate45Background" in av
          and "CybergramTheme.isCybergramPresentation(resourcesProvider)" in av)
    check("avatar plate is built by the shared chamfer builder",
          "CybergramTheme.buildInteractionPanelPath(cybergramPlatePath, cybergramPlateRect," in av
          and "CybergramTheme.DIALOGS_AVATAR_CUT_DP" in av)
    check("avatar plate is drawn as a path, not a circle",
          "canvas.drawPath(cybergramPlatePath, backgroundPaint)" in av)
    check("same cut token as the DialogCell avatar frame",
          "CybergramTheme.DIALOGS_AVATAR_CUT_DP" in dc)
    check("rotate45Background (story avatars) keeps its own plate",
          av.find("if (rotate45Background)") < av.find("if (cybergramPlate)"))
    check("negative control: the circular plate is still reachable when not Cybergram",
          "canvas.drawCircle(size / 2.0f, size / 2.0f, size / 2.0f, backgroundPaint)" in av
          and "} else if (roundRadius > 0) {" in av)

    # The archive row paints a SECOND overlay pass (archivedAvatarProgress) after the plate.
    # That pass must also honour the Cybergram chamfer, or a round archive avatar reappears
    # beside the square ones (FriendCall review 2026-10-12).
    arch = av[av.find("if (avatarType == AVATAR_TYPE_ARCHIVED)"):]
    check("archived avatar overlay is gated on the Cybergram plate",
          "if (cybergramPlate) {" in arch)
    check("archived overlay reuses the chamfer path scaled by its progress",
          "canvas.scale(archivedAvatarProgress, archivedAvatarProgress, size / 2.0f, size / 2.0f)" in arch
          and "canvas.drawPath(cybergramPlatePath, backgroundPaint);" in arch)
    check("negative control: archived overlay stays circular off Cybergram",
          "canvas.drawCircle(size / 2.0f, size / 2.0f, size / 2.0f * archivedAvatarProgress, backgroundPaint)" in arch)

    # Centred glyphs must fit inside the chamfer without an extra clip: the inscribed circle
    # (radius size/2) has to stay within the plate polygon for every dp size the cut can take.
    glyph_failures = []
    for density in DENSITIES:
        for side in (16.0, 24.0, 32.0, 40.0, 54.0, 100.0):
            s = side * density
            cut = min(dp_ceil(dialog_avatar_cut, density), min(s, s) * 0.22)
            plate = chamfer_polygon(0.0, 0.0, s, s, cut, cut, cut, cut)
            for step in range(72):
                angle = step * math.pi / 36.0
                point = (s / 2.0 + math.cos(angle) * s / 2.0, s / 2.0 + math.sin(angle) * s / 2.0)
                if not point_in_convex(plate, point):
                    glyph_failures.append("side=%.0fdp density=%.3f" % (side, density))
                    break
    check("inscribed avatar glyph stays inside the chamfer for %d cases"
          % (len(DENSITIES) * 6), not glyph_failures,
          "outside: " + ", ".join(glyph_failures[:4]) if glyph_failures else "")

    # ---- [F] independent numeric geometry proof --------------------------
    print("\n[F] numeric containment of the media rect inside the bubble silhouette")
    failures = []
    cases = 0
    for density in DENSITIES:
        for w_dp, h_dp in SIZES_DP:
            bounds = (0.0, 0.0, float(w_dp * density), float(h_dp * density))
            cases += 1
            if not media_inside(frame_pad, media_inset, media_cut, corner_cut, density, bounds):
                failures.append("density=%.3f size=%dxdp" % (density, w_dp))
    check("media rect inside bubble silhouette for %d density/size cases" % cases,
          not failures,
          "overflow: " + ", ".join(failures[:6]) if failures else "")

    # Negative control: the pre-fix geometry (media inset == 0) must FAIL,
    # otherwise this check is not actually sensitive to the regression.
    sensitive = any(
        not media_inside(frame_pad, 0.0, media_cut, corner_cut, density,
                         (0.0, 0.0, float(w_dp * density), float(h_dp * density)))
        for density in DENSITIES for w_dp, h_dp in SIZES_DP
    )
    check("negative control: media inset 0 (pre-fix) is detected as overflow", sensitive)

    # ---- [I] dialogs row chrome: one stroke per shape (round-3 FriendCall verdict 2) ---------
    # Every dialogs shape used to be stroked three times (6.2dp bloom + 2.4dp mid halo + 0.5dp core)
    # on both the row plate and the avatar frame, so a list of rows read as stacked decorative
    # borders. The fix keeps ONE stroke per shape and moves the state (selected / unread attention)
    # into that stroke's alpha.
    print("\n[I] dialogs row/avatar chrome uses ONE stroke per shape")
    ints = resolve_ints(texts["theme"])

    check("DialogCell no longer paints the two extra glow passes",
          not re.search(r"BUBBLE_(?:MID_)?GLOW_WIDTH_DP", dc))
    check("the retired three-pass helper is gone",
          "drawCybergramAmberOutline" not in dc)

    stroke_sig = ("private void drawCybergramFrameStroke(Canvas canvas, Path path, int color, "
                  "int alpha, float widthDp) {")
    start = dc.find(stroke_sig)
    if start < 0:
        check("drawCybergramFrameStroke found", False, "signature not found")
        stroke_body = ""
    else:
        end = dc.find("\n    private ", start + len(stroke_sig))
        stroke_body = dc[start:end if end > 0 else len(dc)]
        check("drawCybergramFrameStroke found", True)
    check("the single stroke is the only path draw on a dialogs shape",
          stroke_body.count("drawPath") == 1,
          "drawPath calls: %d" % stroke_body.count("drawPath"))
    check("the single stroke takes its width from the caller",
          "dpf2(widthDp)" in stroke_body)
    check("the row frame passes the row width token",
          "CybergramTheme.DIALOGS_ROW_FRAME_WIDTH_DP" in dc)
    check("the avatar frame also routes through the single-stroke helper",
          "drawCybergramFrameStroke(canvas, cybergramAvatarFramePath" in dc)
    check("avatar frame uses its own (secondary) width and alpha tokens",
          "CybergramTheme.DIALOGS_AVATAR_FRAME_ALPHA" in dc
          and "CybergramTheme.DIALOGS_AVATAR_FRAME_WIDTH_DP" in dc)

    row_alpha = ints.get("DIALOGS_ROW_FRAME_ALPHA")
    unread_alpha = ints.get("DIALOGS_ROW_FRAME_UNREAD_ALPHA")
    selected_alpha = ints.get("DIALOGS_ROW_FRAME_SELECTED_ALPHA")
    badge_alpha = ints.get("DIALOGS_BADGE_BORDER_ALPHA")
    old_core = ints.get("BUBBLE_BORDER_ALPHA")
    check("row frame alpha tokens parse",
          None not in (row_alpha, unread_alpha, selected_alpha, badge_alpha, old_core))
    if None not in (row_alpha, unread_alpha, selected_alpha, badge_alpha, old_core):
        check("unread/selected strengthen the SAME stroke (normal < unread < selected)",
              row_alpha < unread_alpha < selected_alpha,
              "normal=%s unread=%s selected=%s" % (row_alpha, unread_alpha, selected_alpha))
        check("counter rim stays weaker than the attention row frame",
              badge_alpha < unread_alpha,
              "badge=%s frame=%s" % (badge_alpha, unread_alpha))
        check("normal (read) row stroke is quieter than the retired 3-pass core",
              row_alpha < old_core, "normal=%s old_core=%s" % (row_alpha, old_core))
    check("row stroke strengthens on state (selected/unread branches present)",
          "DIALOGS_ROW_FRAME_SELECTED_ALPHA" in dc
          and "DIALOGS_ROW_FRAME_UNREAD_ALPHA" in dc)

    # Negative control: the retired three-pass body must fail the one-stroke check, otherwise the
    # check above is not sensitive to the regression it guards.
    legacy_body = "canvas.drawPath(path, p); canvas.drawPath(path, p); canvas.drawPath(path, p);"
    check("negative control: a legacy three-pass stroke fails the one-stroke check",
          legacy_body.count("drawPath") != 1)

    # ---- [J] avatar gutter / text plate gap (round-3 FriendCall verdict 1) ----------------
    # The complaint was that the avatar and the body read as two detached heavy blocks. The fix
    # keeps the separate pieces but makes the avatar frame secondary (quiet, thin) and pulls the
    # text plate into the gutter so the gap loses weight. Gutter width and touch targets unchanged.
    print("\n[J] avatar gutter and text plate read as one element")
    avatar_alpha = ints.get("DIALOGS_AVATAR_FRAME_ALPHA")
    inset = vals.get("DIALOGS_ROW_PANEL_INSET_DP")
    check("avatar frame alpha / panel inset tokens parse",
          avatar_alpha is not None and inset is not None)
    check("row plate side uses the inset token",
          "CybergramTheme.DIALOGS_ROW_PANEL_INSET_DP" in dc)
    if avatar_alpha is not None and inset is not None and row_alpha is not None:
        check("avatar frame alpha is quieter than the read row frame",
              avatar_alpha < row_alpha, "avatar=%s row=%s" % (avatar_alpha, row_alpha))
        check("text plate is pulled into the gutter (gap shrank but is still > 0)",
              0 < inset < 4, "inset=%.2f old_gap=4" % inset)

        # Density-independent dp model. DialogCell centres the avatar (heightDefault-6 = 64dp) in
        # the 72dp gutter, so its raw-side edge sits at (gutter + side) / 2; the plate starts at
        # gutter - inset. The old plate started at the bare gutter (inset 0, gap 4dp).
        gutter = 72.0
        avatar_side = 70.0 - 6.0
        avatar_right = (gutter + avatar_side) / 2.0
        gap = (gutter - inset) - avatar_right
        check("numeric model: gap equals the panel inset and the shapes stay separate",
              abs(gap - inset) < 1e-6 and gap > 0, "gap=%.2f inset=%.2f" % (gap, inset))
        check("negative control: the pre-fix 4dp gap is detected as heavier",
              abs(((gutter - 0.0) - avatar_right) - 4.0) < 1e-6)

    # ---- [K] dialogs counter / preview right guard (round-3 FriendCall verdict 3) ----------
    # The counter used to anchor at the upstream BADGE_MARGIN (~5.66dp from the frame) while the
    # preview layout box could reach past the counter's left edge, so a wide unread value crowded
    # the preview. Cybergram now anchors the counter cluster DIALOGS_RIGHT_GUARD_DP inside the frame
    # and clamps the preview so it always stops that far before the frame / the nearest counter.
    print("\n[K] dialogs counter / preview right guard")
    guard_dp = vals.get("DIALOGS_RIGHT_GUARD_DP")
    pad_dp = 6.333          # DialogCell BADGE_TEXT_PADDING
    upstream_margin = 15.666  # DialogCell BADGE_MARGIN
    frame_edge = 10.0        # DialogCell row plate is inset 10dp from the window edge

    check("DIALOGS_RIGHT_GUARD_DP parses and is positive",
          guard_dp is not None and guard_dp > 0)
    check("DialogCell anchors the counter with the Cybergram guard token",
          "CybergramTheme.DIALOGS_RIGHT_GUARD_DP" in dc)
    check("DialogCell builds a Cybergram badge margin",
          "final int badgeMargin = cybergram" in dc)
    check("DialogCell clamps the preview against frame and counter",
          "textMaxRight" in dc and "textMinLeft" in dc)

    if guard_dp is not None:
        COUNT_WIDTHS_DP = [8.0, 20.0, 35.0, 60.0]   # 35dp ~ five bold digits ("11175")

        def counter_guard_model(W, density, count_w_dp, margin_dp, clamped):
            frame_inner = W - dp_ceil(frame_edge, density)
            plate_w = dp_ceil(count_w_dp, density) + 2 * dp_ceil(pad_dp, density)
            badge_right = W - dp_ceil(margin_dp, density)
            badge_left = badge_right - plate_w
            if clamped:
                text_max_right = min(frame_inner - dp_ceil(guard_dp, density),
                                     badge_left - dp_ceil(guard_dp, density))
            else:
                # Pre-fix: base box ended 4dp short of the window edge and the reserve was
                # BADGE_GAP = 25 - BADGE_TEXT_MIN_WIDTH = 17dp.
                text_max_right = (frame_inner + dp_ceil(frame_edge, density) - dp_ceil(4.0, density)
                                  - (dp_ceil(count_w_dp, density) + dp_ceil(17.0, density)))
            return frame_inner, badge_left, badge_right, text_max_right

        cases = 0
        failures = []
        for density in DENSITIES:
            for W_dp in (320.0, 360.0, 411.0):
                W = float(dp_ceil(W_dp, density))
                for cw in COUNT_WIDTHS_DP:
                    cases += 1
                    frame_inner, badge_left, badge_right, text_right = counter_guard_model(
                        W, density, cw, frame_edge + guard_dp, True)
                    g = dp_ceil(guard_dp, density)
                    if frame_inner - badge_right < g:
                        failures.append("frame gap d=%.3f cw=%.0f" % (density, cw))
                    if badge_left - text_right < g:
                        failures.append("counter gap d=%.3f cw=%.0f" % (density, cw))
                    if text_right > frame_inner - g:
                        failures.append("text past frame d=%.3f cw=%.0f" % (density, cw))
        check("counter and preview keep the guard for %d density/size/value cases" % cases,
              not failures, "violations: " + ", ".join(failures[:4]) if failures else "")

        # Large values must expand without overlap: the badge grows left, the text follows.
        overlaps = []
        for density in DENSITIES:
            W = float(dp_ceil(411.0, density))
            g = dp_ceil(guard_dp, density)
            for cw in COUNT_WIDTHS_DP:
                _, badge_left, _, text_right = counter_guard_model(
                    W, density, cw, frame_edge + guard_dp, True)
                if text_right > badge_left - g:
                    overlaps.append("d=%.3f cw=%.0f" % (density, cw))
        check("wide values (up to a five-digit counter) never overlap the preview",
              not overlaps, "overlaps: " + ", ".join(overlaps[:4]) if overlaps else "")

        # Negative control: the pre-fix anchoring must be detected as too close / overlapping.
        bad = []
        for density in DENSITIES:
            W = float(dp_ceil(411.0, density))
            g = dp_ceil(guard_dp, density)
            for cw in COUNT_WIDTHS_DP:
                frame_inner, badge_left, badge_right, text_right = counter_guard_model(
                    W, density, cw, upstream_margin, False)
                if frame_inner - badge_right < g or text_right > badge_left - g:
                    bad.append((density, cw))
        check("negative control: the pre-fix margin/gap is detected as crowding",
              len(bad) == len(DENSITIES) * len(COUNT_WIDTHS_DP),
              "detected %d of %d" % (len(bad), len(DENSITIES) * len(COUNT_WIDTHS_DP)))

    # ---- [L] dialogs reaction/poll counter anchors (round-4 FriendCall P1 regression) ------
    # c0df7c78f flipped the LTR reaction/poll anchor sign to `getMeasuredWidth() - dp(BADGE_SIZE)
    # + badgeMargin`: with the new 18dp guard the icon sat from w - 2.67dp to w + 18dp and painted
    # past the right edge. Upstream's `- dp(BADGE_SIZE + BADGE_MARGIN)` semantics must be restored
    # while keeping the new guard value, for BOTH icons identically and only in the LTR branch.
    print("\n[L] dialogs reaction/poll anchors sit inside the right guard")
    badge_size_dp = 20.666                        # DialogCell BADGE_SIZE
    badge_text_min_w_dp = 8.0                     # DialogCell BADGE_TEXT_MIN_WIDTH
    badge_gap_dp = 25.0 - badge_text_min_w_dp     # DialogCell BADGE_GAP = 25 - MIN_WIDTH
    reaction_slot_dp = 25.0                       # DialogCell reaction reserve (dp(25))
    FIXED_FORM = "getMeasuredWidth()-badgeMargin-dp(BADGE_SIZE)"
    PREFIX_FORM = "getMeasuredWidth()-dp(BADGE_SIZE)+badgeMargin"

    def strip_java_comments(src):
        src = re.sub(r"/\*.*?\*/", "", src, flags=re.S)
        return re.sub(r"//[^\n]*", "", src)

    def ltr_anchor_source(src, field):
        exprs = [e.strip() for e in re.findall(r"\b%s\s*=\s*([^;]+);" % field, src)
                 if "getMeasuredWidth" in e]
        return exprs[0] if len(exprs) == 1 else None

    def normalize(expr):
        return re.sub(r"\s+", "", expr)

    def eval_anchor(expr, W, density, badge_margin):
        """Evaluate the ACTUAL LTR anchor expression text with the gate's dp model."""
        e = re.sub(r"\bgetMeasuredWidth\(\)", "W", expr)
        e = re.sub(r"\bbadgeMargin\b", "badge_margin", e)
        e = re.sub(r"\bdp\(([^()]*)\)", r"dp_ceil(\1, density)", e)
        e = re.sub(r"(?<=[0-9.])f\b", "", e)
        e = re.sub(r"\bBADGE_SIZE\b", repr(badge_size_dp), e)
        e = re.sub(r"\bBADGE_MARGIN\b", "15.666", e)
        return float(eval(e, {"__builtins__": {}},
                          {"dp_ceil": dp_ceil, "W": W, "density": density,
                           "badge_margin": badge_margin}))

    clean_dc = strip_java_comments(texts["dialog_cell"])
    anchor_fields = ("reactionMentionLeft", "pollVotesMentionLeft")
    anchors = {f: ltr_anchor_source(clean_dc, f) for f in anchor_fields}
    check("both LTR reaction/poll anchors are readable from DialogCell",
          None not in anchors.values(),
          "reaction=%r poll=%r" % (anchors["reactionMentionLeft"], anchors["pollVotesMentionLeft"]))
    check("both LTR anchors restore the badgeMargin subtraction (guard value kept)",
          all(a is not None and normalize(a) == FIXED_FORM for a in anchors.values()),
          "reaction=%r poll=%r" % (anchors["reactionMentionLeft"], anchors["pollVotesMentionLeft"]))
    check("the pre-fix `+ badgeMargin` anchor is absent from DialogCell",
          "dp(BADGE_SIZE)+badgeMargin" not in normalize(clean_dc))
    check("the RTL branches keep their badgeMargin anchor (not flipped)",
          re.search(r"reactionMentionLeft = badgeMargin;", clean_dc) is not None
          and re.search(r"pollVotesMentionLeft = badgeMargin;", clean_dc) is not None)

    margin_dp = frame_edge + (guard_dp if guard_dp is not None else 8.0)   # DialogCell badgeMargin
    sizes_dp = (320.0, 360.0, 411.0)
    count_widths_dp = (None, 8.0, 20.0, 35.0)      # None = absent, 35dp ~ "11175"
    mention_widths_dp = (None, 8.0, 40.0)

    if None not in anchors.values():
        def anchor_model(field, W, density, count_w_dp, mention_w_dp, reaction_present):
            margin = dp_ceil(margin_dp, density)
            a = eval_anchor(anchors[field], W, density, margin)
            if field == "pollVotesMentionLeft" and reaction_present:
                a -= dp_ceil(reaction_slot_dp, density)
            if mention_w_dp is not None:
                a -= dp_ceil(mention_w_dp, density) + dp_ceil(badge_gap_dp, density)
            if count_w_dp is not None:
                a -= dp_ceil(count_w_dp, density) + dp_ceil(badge_gap_dp, density)
            return a, margin

        cases = 0
        violations = []
        for density in DENSITIES:
            height_px = dp_ceil(badge_size_dp, density)
            for W_dp in sizes_dp:
                W = float(dp_ceil(W_dp, density))
                for count_w_dp in count_widths_dp:
                    for mention_w_dp in mention_widths_dp:
                        for field, reaction_present in (("reactionMentionLeft", False),
                                                        ("pollVotesMentionLeft", False),
                                                        ("pollVotesMentionLeft", True)):
                            cases += 1
                            a, margin = anchor_model(field, W, density, count_w_dp,
                                                     mention_w_dp, reaction_present)
                            if a < margin:
                                violations.append("%s left<badgeMargin d=%.3f" % (field, density))
                            if a + height_px > W - margin:
                                violations.append("%s right>w-badgeMargin d=%.3f" % (field, density))
        check("LTR anchors keep the guard for %d density/size/count/mention cases" % cases,
              not violations, "violations: " + ", ".join(violations[:4]) if violations else "")

        # Negative control: the pre-fix `+ badgeMargin` sign must be detected as right-edge
        # overflow in every case, otherwise this section is not sensitive to the regression.
        bad = 0
        total = 0
        for density in DENSITIES:
            height_px = dp_ceil(badge_size_dp, density)
            for W_dp in sizes_dp:
                total += 1
                W = float(dp_ceil(W_dp, density))
                margin = dp_ceil(margin_dp, density)
                a = eval_anchor(PREFIX_FORM, W, density, margin)
                if a + height_px > W - margin:
                    bad += 1
        check("negative control: the pre-fix `+ badgeMargin` anchor is detected as overflow",
              total > 0 and bad == total, "detected %d of %d" % (bad, total))
    check("badgeMargin model matches the guard token (10dp frame inset + guard)",
          abs(margin_dp - (frame_edge + 8.0)) < 1e-9, "margin=%.2fdp" % margin_dp)

    _summary()


def _summary():
    failed = [label for label, ok in _results if not ok]
    print("\n== RESULT ==")
    print("checks=%d failed=%d" % (len(_results), len(failed)))
    if failed:
        for label in failed:
            print("  FAILED: %s" % label)
        print("FAIL")
        sys.exit(1)
    print("OK")


if __name__ == "__main__":
    main()
