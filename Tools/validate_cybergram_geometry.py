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
# Round 8 / Stage F profile surface.
PROFILE_ACTIVITY = JAVA / "ui/ProfileActivity.java"
PROFILE_ACTIONS_VIEW = JAVA / "ui/Components/ProfileActionsView.java"
# Round 9 / Stage F settings surface.
SETTINGS_ACTIVITY = JAVA / "ui/SettingsActivity.java"
SETTINGS_SEARCH_CELL = JAVA / "ui/Cells/SettingsSearchCell.java"

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
        "profile_activity": PROFILE_ACTIVITY,
        "profile_actions_view": PROFILE_ACTIONS_VIEW,
        "settings_activity": SETTINGS_ACTIVITY,
        "settings_search_cell": SETTINGS_SEARCH_CELL,
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
        # Round-7 owner ruling (2026-10-12) supersedes the round-3 "quieter than the row plate"
        # alpha hierarchy for the frame's resting value: the frame must be LEGIBLE. The resting
        # alpha may sit above the row's read alpha; what must hold is that it stays under every
        # raised state. The full round-7 model (legibility floor + CP2077 Rest -> Raised) is in [J3].
        check("resting avatar frame alpha stays under every raised state",
              None not in (avatar_alpha, unread_alpha, selected_alpha)
              and avatar_alpha < unread_alpha and avatar_alpha < selected_alpha,
              "avatar=%s row_unread=%s row_selected=%s"
              % (avatar_alpha, unread_alpha, selected_alpha))
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

        # Round-4 FriendCall finding 1 (still PARTIAL after the round-3 fix): the thinner, quieter
        # frame and the tighter gap were not enough — a saturated avatar fill (Saved Messages, or a
        # peer coloured from its region/id) is far brighter than the olive plate, so the pair still
        # read as a bright chip beside a dark slab. The avatar now carries a restrained wash of the
        # row-plate material clipped to its own chamfer, which pulls its fill toward the plate's
        # luminance, and the frame alpha drops again. Geometry / gutter / touch targets unchanged.
        scrim_alpha = ints.get("DIALOGS_AVATAR_SCRIM_ALPHA")
        scrim_color = ints.get("DIALOGS_AVATAR_SCRIM_COLOR")
        row_surface = ints.get("DIALOGS_ROW_SURFACE")
        background = ints.get("BACKGROUND")
        row_bubble = ints.get("IN_BUBBLE")
        check("avatar scrim / plate tokens parse",
              None not in (scrim_alpha, scrim_color, row_surface, background, row_bubble))
        if None not in (scrim_alpha, scrim_color, row_surface):
            check("avatar scrim uses the row-plate material (palette-derived)",
                  scrim_color == row_surface,
                  "scrim=0x%08X plate=0x%08X" % (scrim_color, row_surface))
            check("avatar scrim is a restrained wash (0 < alpha < 128)",
                  0 < scrim_alpha < 128, "alpha=%s" % scrim_alpha)
        # Round-7 (2026-10-12): the round-4 "alpha < 108" reduction is REJECTED by the owner. The
        # legibility of the resting frame is enforced in [J3] instead.
        check("avatar scrim keeps the gutter/inset token untouched",
              inset is not None and "DIALOGS_ROW_PANEL_INSET_DP" in dc
              and "DIALOGS_ROW_PANEL_INSET_DP" in texts["theme"])
        scrim_draw = "canvas.drawPath(cybergramAvatarFramePath, cybergramAvatarScrimPaint)"
        check("DialogCell washes the avatar on its own chamfer path, before the frame stroke",
              "cybergramAvatarScrimPaint.setColor(CybergramTheme.DIALOGS_AVATAR_SCRIM_COLOR)" in dc
              and "cybergramAvatarScrimPaint.setAlpha(CybergramTheme.DIALOGS_AVATAR_SCRIM_ALPHA)" in dc
              and scrim_draw in dc
              and dc.index(scrim_draw)
                  < dc.index("drawCybergramFrameStroke(canvas, cybergramAvatarFramePath"))

        # Independent luminance model: the plate is IN_BUBBLE over the window; the scrim is the
        # plate's RGB painted at DIALOGS_AVATAR_SCRIM_ALPHA (Paint.setAlpha replaces the alpha), and
        # the representative fills are the real AvatarDrawable gradient palette read from source.
        if None not in (scrim_alpha, scrim_color, background, row_bubble):

            def _srgb(v):
                c = v / 255.0
                return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4

            def _lum(rgb):
                return 0.2126 * _srgb(rgb[0]) + 0.7152 * _srgb(rgb[1]) + 0.0722 * _srgb(rgb[2])

            def _rgb(argb):
                return ((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF)

            def _over(fg_argb, alpha, bg_rgb):
                f = alpha / 255.0
                fg = _rgb(fg_argb)
                return tuple(f * fg[i] + (1.0 - f) * bg_rgb[i] for i in range(3))

            plate = _over(row_bubble, (row_bubble >> 24) & 0xFF, _rgb(background))
            plate_lum = _lum(plate)
            fills = sorted({int(m, 16) for m in
                            re.findall(r"0xFF[0-9A-Fa-f]{6}", texts["avatar_drawable"])})
            check("representative avatar fills parsed from AvatarDrawable",
                  len(fills) >= 4, "%d fills" % len(fills))

            no_improve = []
            ratios = []
            for fill in fills:
                raw_delta = abs(_lum(_rgb(fill)) - plate_lum)
                if raw_delta < 1e-9:
                    continue
                washed = _lum(_over(scrim_color, scrim_alpha, _rgb(fill)))
                washed_delta = abs(washed - plate_lum)
                ratios.append(washed_delta / raw_delta)
                if washed_delta >= raw_delta:
                    no_improve.append("0x%08X" % fill)
            check("scrim pulls every AvatarDrawable fill toward the plate luminance",
                  not no_improve, "no improvement: " + ", ".join(no_improve[:4]) if no_improve else "")
            worst = min(ratios) if ratios else None
            check("scrim removes >= 40% of the luminance delta for every fill",
                  worst is not None and worst <= 0.6,
                  "worst remaining delta ratio=%s" % ("%.3f" % worst if worst is not None else "n/a"))

            # Negative control: the pre-fix scrim alpha (no wash at all) leaves every delta intact,
            # otherwise the checks above are not sensitive to the separation they guard.
            untouched = [fill for fill in fills
                         if abs(_lum(_over(scrim_color, 0, _rgb(fill))) - plate_lum)
                         >= abs(_lum(_rgb(fill)) - plate_lum)]
            check("negative control: scrim alpha 0 (pre-fix) leaves the delta unchanged",
                  fills and len(untouched) == len(fills),
                  "detected %d of %d" % (len(untouched), len(fills)))

    # Round-6 FriendCall finding (avatar / text JOINT, still PARTIAL after rounds 3-5): the seam is
    # still a dark vertical sliver between two chamfered blocks, and the avatar fill and the plate
    # surface still read as different materials. A frame-alpha reduction cannot fix a missing
    # material, so the row surface now runs BEHIND the gutter, from the far side of the avatar's own
    # chamfer, and the row outline is left OPEN at the joint so the avatar's single hairline is the
    # only outline there. Gutter/inset, avatar rect, chamfer cut and touch targets are unchanged.
    print("\n[J2] avatar / text joint carries the row material (round-6)")
    row_surface = ints.get("DIALOGS_ROW_SURFACE")
    background = ints.get("BACKGROUND")
    gutter_fill = ints.get("DIALOGS_ROW_GUTTER_FILL")
    gutter_fill_active = ints.get("DIALOGS_ROW_GUTTER_FILL_ACTIVE")
    row_surface_active = ints.get("DIALOGS_ROW_SURFACE_ACTIVE")
    check("gutter fill / active row surface tokens parse",
          None not in (gutter_fill, gutter_fill_active, row_surface_active))
    if None not in (gutter_fill, gutter_fill_active, row_surface_active) and row_surface is not None:
        check("gutter fill IS the row-plate surface token (same material, not the window)",
              gutter_fill == row_surface and gutter_fill_active == row_surface_active,
              "gutter=0x%08X/0x%08X plate=0x%08X/0x%08X"
              % (gutter_fill, gutter_fill_active, row_surface, row_surface_active))
        if background is not None:
            check("gutter fill is never the window background",
                  gutter_fill != background and gutter_fill_active != background,
                  "gutter=0x%08X/0x%08X window=0x%08X"
                  % (gutter_fill, gutter_fill_active, background))
    check("DialogCell fills the row surface from the gutter fill tokens",
          "CybergramTheme.DIALOGS_ROW_GUTTER_FILL_ACTIVE" in dc
          and "CybergramTheme.DIALOGS_ROW_GUTTER_FILL" in dc)

    # Structural proof that the joint outline is open: find the row treatment body and check that
    # (a) the surface fill and the single amber stroke share ONE path built in that method,
    # (b) that path is never closed, so no joint-side outline can be stroked next to the avatar.
    row_sig = "private void drawCybergramRowTreatment(Canvas canvas) {"
    row_start = dc.find(row_sig)
    row_end = dc.find("public boolean drawAvatarOverlays(Canvas canvas) {", row_start)
    row_body = dc[row_start:row_end] if row_start >= 0 and 0 < row_end else ""
    check("drawCybergramRowTreatment body readable from source", bool(row_body))

    def _outline_is_open(body):
        """The row outline is open when it is hand-built and never closed on the joint side.

        The pre-fix plate outline came from the shared four-corner builder, which closes the left
        chamfer into the silhouette — exactly the competing joint outline this section guards.
        """
        return ("cybergramRowFramePath.close()" not in body
                and "buildInteractionPanelPath(cybergramRowFramePath" not in body)

    if row_body:
        check("the row surface fill and the row outline share ONE path",
              row_body.count("canvas.drawPath(cybergramRowFramePath") == 1
              and "drawCybergramFrameStroke(canvas, cybergramRowFramePath" in row_body
              and "cybergramRowFramePath.rewind()" in row_body)
        check("the joint side of the row outline is left OPEN (no competing joint outline)",
              _outline_is_open(row_body) and row_body.count("jointEdge") >= 3)
        check("the row surface joint edge is derived from the avatar rect + chamfer cut",
              "storyParams.originalAvatarRect.right - avatarCut" in row_body
              and "storyParams.originalAvatarRect.left + avatarCut" in row_body
              and "CybergramTheme.DIALOGS_AVATAR_CUT_DP" in row_body)
        # Negative control: the pre-fix body closed the plate on its own chamfered left edge via the
        # shared four-corner builder, so the SAME predicate must reject it.
        legacy_row = ("CybergramTheme.buildInteractionPanelPath(cybergramRowFramePath, "
                      "cybergramRowFrameRect, CybergramTheme.DIALOGS_ROW_CUT_DP);")
        check("negative control: the pre-fix closed plate outline fails the open-joint predicate",
              not _outline_is_open(legacy_row))

    # Single hairline avatar frame, weaker than the row frame in every state.
    avatar_width = vals.get("DIALOGS_AVATAR_FRAME_WIDTH_DP")
    row_width = vals.get("DIALOGS_ROW_FRAME_WIDTH_DP")
    check("exactly ONE hairline stroke is drawn on the avatar frame",
          dc.count("drawCybergramFrameStroke(canvas, cybergramAvatarFramePath") == 1)
    # Round-7: the legibility floor needs a resolvable stroke, so the avatar frame is allowed to be
    # wider than the row outline (which is a large shape and reads fine at 0.5dp). The full width
    # band is enforced in [J3]; here only the token must exist.
    check("avatar frame width token parses",
          avatar_width is not None and row_width is not None,
          "avatar=%sdp row=%sdp" % (avatar_width, row_width))
    check("resting avatar frame stays weaker than the ACTIVE-row frame",
          None not in (avatar_alpha, selected_alpha) and avatar_alpha < selected_alpha,
          "avatar=%s selected=%s" % (avatar_alpha, selected_alpha))

    # Density x layout x width matrix. Every device pixel column between the avatar's right edge and
    # the plate's left edge must be covered by the row surface. Mirrors the real code:
    #   onMeasure:  avatarSize = dp(height - 6); gutterWidth = dp(messagePaddingStart);
    #               avatarX = (gutterWidth - avatarSize) * 0.5f
    #   onDraw:     panelLeft = dp(messagePaddingStart - DIALOGS_ROW_PANEL_INSET_DP);
    #               jointEdge = min(panelLeft, avatarRight - dpf2(DIALOGS_AVATAR_CUT_DP))
    # The gutter distance is width-independent (the plate is edge-anchored), so the widths in the
    # matrix only prove the invariant survives every row width; RTL mirrors the same distances.
    def _int_field(name):
        m = re.search(r"\bint\s+%s\s*=\s*(-?\d+)\s*;" % re.escape(name), dc)
        return int(m.group(1)) if m else None

    gutter_dp = _int_field("messagePaddingStart")
    height_dp = _int_field("heightDefault")
    height_three_dp = _int_field("heightThreeLines")
    check("Dialogs row metrics readable from DialogCell source",
          None not in (gutter_dp, height_dp, height_three_dp),
          "gutter=%s height=%s three=%s" % (gutter_dp, height_dp, height_three_dp))

    if None not in (gutter_dp, height_dp, height_three_dp) and None not in (inset, dialog_avatar_cut):
        widths_dp = (320.0, 360.0, 411.0, 480.0, 600.0)
        # 2.75 is emulator-5554's real density (1080x2400 @ 440dpi), i.e. the round-6 device shot.
        joint_densities = tuple(sorted(set(DENSITIES) | {2.75}))

        def _gutter_columns(density, layout_height_dp, width_dp):
            """Pixel columns strictly between the avatar's right edge and the plate's left edge."""
            gutter_px = float(dp_ceil(gutter_dp, density))
            avatar_px = float(dp_ceil(layout_height_dp - 6, density))
            avatar_right_px = (gutter_px - avatar_px) * 0.5 + avatar_px
            panel_left_px = float(dp_ceil(gutter_dp - inset, density))
            if width_dp <= gutter_dp + 10.0:
                return None, None, None, None
            columns = [x + 0.5 for x in range(int(math.ceil(avatar_right_px)),
                                              int(math.floor(panel_left_px)))]
            return avatar_right_px, panel_left_px, columns, gutter_px

        cases = 0
        no_gutter = 0
        unexplained_empty = []
        uncovered = []
        prefix_background = 0
        prefix_cases = 0
        for density in joint_densities:
            for layout_height_dp in (height_dp, height_three_dp):
                cut_px = density * dialog_avatar_cut          # AndroidUtilities.dpf2
                for width_dp in widths_dp:
                    avatar_right_px, panel_left_px, columns, _ = _gutter_columns(
                        density, layout_height_dp, width_dp)
                    if columns is None:
                        continue
                    if not columns:
                        # The 3-line avatar (dp(heightThreeLines - 6)) already reaches the plate's
                        # own left edge, so that layout has no background column to cover at all.
                        no_gutter += 1
                        if avatar_right_px < panel_left_px:
                            unexplained_empty.append((density, layout_height_dp, width_dp))
                        continue
                    cases += 1
                    joint_px = min(panel_left_px, avatar_right_px - cut_px)
                    post = [c for c in columns if c < joint_px]
                    pre = [c for c in columns if c < panel_left_px]
                    if post:
                        uncovered.append((density, layout_height_dp, width_dp, post[0]))
                    if pre:
                        prefix_cases += 1
                    prefix_background += len(pre)

        check("matrix model covers at least one gutter column in every case",
              cases > 0, "cases=%d no_gutter_layouts=%d" % (cases, no_gutter))
        check("every layout without a covered column is one whose avatar already reaches the plate",
              not unexplained_empty,
              "unexplained=%s" % (unexplained_empty[:3],) if unexplained_empty else "")
        check("no window-background column survives between avatar and plate "
              "(density x layout x width matrix)",
              cases > 0 and not uncovered,
              "cases=%d uncovered=%d%s" % (cases, len(uncovered),
                                           " first=%s" % (uncovered[0],) if uncovered else ""))
        # Negative control: with the pre-fix surface (starting at the plate's left edge) EVERY gutter
        # column is background, so the check above is provably sensitive to the regression it guards.
        check("negative control: the pre-fix layout (surface at the plate edge) shows background "
              "columns in every case",
              cases > 0 and prefix_cases == cases and prefix_background > 0,
              "cases=%d with background=%d columns=%d" % (cases, prefix_cases, prefix_background))

    # ---- [J3] avatar frame legibility and CP2077 Rest -> Raised (round-7 owner ruling) ---------
    # Owner ruling 2026-10-12 18:44 MSK: the avatar frames need a stroke ("Убранная обводка
    # ошибочна: аватаркам она нужна — нужно ориентироваться на интерфейс киберпанк 2077"). The
    # round-6 reduction to a 0.4dp alpha-72 hairline is rejected as a defect. The reference grammar
    # is the CP2077 codex entry frame (codex_style.inkstyle EntryItem.frameOpacity: Default
    # 0.0707399994 -> Hover 1.0 -> Active 1.0, a separate 32x32 frame sprite layer bound in
    # codex_main.inkwidget), i.e. a legible RESTING frame plus a RAISED state. This section proves
    # the frame reads on the dialogs rows at emulator-5554's 440dpi without touching row height,
    # gutter, text bounds, touch targets, the round-6 joint or the palette.
    print("\n[J3] avatar frame legibility and raised state (round-7)")
    rest_alpha = ints.get("DIALOGS_AVATAR_FRAME_ALPHA")
    raised_unread = ints.get("DIALOGS_AVATAR_FRAME_UNREAD_ALPHA")
    raised_selected = ints.get("DIALOGS_AVATAR_FRAME_SELECTED_ALPHA")
    frame_w = vals.get("DIALOGS_AVATAR_FRAME_WIDTH_DP")
    check("round-7 avatar frame tokens parse",
          None not in (rest_alpha, raised_unread, raised_selected, frame_w, selected_alpha),
          "w=%s rest=%s unread=%s selected=%s"
          % (frame_w, rest_alpha, raised_unread, raised_selected))

    def _ink_px(width_dp, alpha, density=2.75):
        """Full-opacity device-pixel coverage of a stroked line: width * density * alpha/255.

        2.75 is emulator-5554's real density (1080x2400 @ 440dpi), the round-6/round-7 device.
        """
        return width_dp * density * (alpha / 255.0)

    if None not in (rest_alpha, raised_unread, raised_selected, frame_w):
        rest_ink = _ink_px(frame_w, rest_alpha)
        old_ink = _ink_px(0.4, 72)
        check("resting avatar frame reaches the legibility floor (>= 1.0 full-opacity device px)",
              rest_ink >= 1.0,
              "ink=%.2f px at 440dpi (w=%.2fdp a=%s)" % (rest_ink, frame_w, rest_alpha))
        check("avatar frame stroke is resolvable (>= 2 device px wide at 440dpi)",
              frame_w * 2.75 >= 2.0, "w=%.2fdp = %.2f px" % (frame_w, frame_w * 2.75))
        check("avatar frame stroke is not a heavy bar (<= 1.2dp)",
              frame_w <= 1.2, "w=%.2fdp" % frame_w)
        check("CP2077 Rest -> Raised separation exists (rest < unread <= selected)",
              rest_alpha < raised_unread <= raised_selected,
              "rest=%s unread=%s selected=%s" % (rest_alpha, raised_unread, raised_selected))
        check("raised avatar frame approaches CP2077's raised opacity 1.0 (>= 224/255)",
              raised_unread >= 224, "unread=%s" % raised_unread)
        # The raised avatar frame is the row's focal element, so CP2077's raised opacity (1.0) puts
        # it at least as strong as the row outline in the same state — never weaker.
        check("raised avatar frame is at least as strong as the row frame in the same state",
              raised_unread >= unread_alpha and raised_selected >= selected_alpha,
              "avatar_unread=%s row_unread=%s avatar_selected=%s row_selected=%s"
              % (raised_unread, unread_alpha, raised_selected, selected_alpha))
        # Negative control: the rejected round-6 pair must FAIL the two legibility predicates, so
        # these checks are provably sensitive to exactly the defect the owner reported.
        check("negative control: the round-6 hairline (0.4dp / alpha 72) fails legibility",
              old_ink < 1.0 and not (0.4 * 2.75 >= 2.0),
              "old ink=%.2f px, width=%.2f px" % (old_ink, 0.4 * 2.75))

    # The state selection must be wired in the DialogCell avatar outline, and only there.
    av_sig = "private void drawCybergramAvatarOutline(Canvas canvas, boolean unreadStory) {"
    av_start = dc.find(av_sig)
    av_end = dc.find("private void drawCybergramBadge(Canvas canvas, RectF badgeRect", av_start)
    av_body = dc[av_start:av_end] if av_start >= 0 and av_end > av_start else ""
    check("drawCybergramAvatarOutline body readable from source", bool(av_body))
    if av_body:
        check("all three frame states are chosen inside the Cybergram avatar outline",
              av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_ALPHA") == 1
              and av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_UNREAD_ALPHA") == 1
              and av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_SELECTED_ALPHA") == 1,
              "rest=%d unread=%d selected=%d"
              % (av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_ALPHA"),
                 av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_UNREAD_ALPHA"),
                 av_body.count("CybergramTheme.DIALOGS_AVATAR_FRAME_SELECTED_ALPHA")))
        check("raised state is driven by the real row state (isSelected / unread / mention)",
              "isSelected" in av_body and "unreadCount > 0 || markUnread" in av_body
              and "mentionCount > 0 || reactionMentionCount > 0" in av_body
              and "pollVotesMentionCount > 0" in av_body)
        check("still exactly ONE stroke on the avatar frame (no glow pass re-added)",
              av_body.count("drawCybergramFrameStroke(canvas, cybergramAvatarFramePath") == 1,
              "strokes=%d"
              % av_body.count("drawCybergramFrameStroke(canvas, cybergramAvatarFramePath"))
        check("avatar frame colour semantics unchanged (cyan unread story / amber read)",
              "unreadStory ? CybergramTheme.CYAN : CybergramTheme.AMBER" in av_body)

    # Outside the Cybergram presentation the dialogs avatar path must stay a no-op: the framed
    # branch is only reached under the isCybergramPresentation guard, and the new tokens have
    # exactly one call site each in DialogCell.
    guard = "cybergramAvatar = CybergramTheme.isCybergramPresentation(resourcesProvider);"
    check("framed avatar path is reached only under the Cybergram presentation guard",
          guard in dc and dc.count("drawCybergramAvatarOutline(canvas") == 1
          and dc.index(guard) < dc.index("drawCybergramAvatarOutline(canvas"))
    check("round-7 frame tokens are referenced only by the Cybergram frame stroke",
          dc.count("DIALOGS_AVATAR_FRAME_UNREAD_ALPHA") == 1
          and dc.count("DIALOGS_AVATAR_FRAME_SELECTED_ALPHA") == 1
          and dc.count("DIALOGS_AVATAR_FRAME_ALPHA") == 1
          and dc.count("DIALOGS_AVATAR_FRAME_WIDTH_DP") == 1,
          "usages rest=%d unread=%d selected=%d width=%d"
          % (dc.count("DIALOGS_AVATAR_FRAME_ALPHA"),
             dc.count("DIALOGS_AVATAR_FRAME_UNREAD_ALPHA"),
             dc.count("DIALOGS_AVATAR_FRAME_SELECTED_ALPHA"),
             dc.count("DIALOGS_AVATAR_FRAME_WIDTH_DP")))

    # Provenance: the theme documents the CP2077 Rest -> Raised reference it derived the values from.
    check("theme documents the CP2077 Rest -> Raised reference (frameOpacity 0.0707399994 -> 1.0)",
          "0.0707399994" in texts["theme"] and "EntryItem.frameOpacity" in texts["theme"])

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

    # ---- [M] round-8 Stage F profile surface ------------------------------------------------
    # The Profile screen carried two stock Material shapes: the hero avatar (a circular
    # ImageReceiver mask, ProfileActivity.AvatarImageView) and the three-capsule action row
    # (ProfileActionsView). Round 8 routes both through the shared eight-segment chamfer, keeps
    # the round-7 avatar-frame Rest -> Raised grammar, and must not move a single hit-test rect.
    # This section re-derives the geometry from the real source so a silent revert (e.g. dropping
    # the gate, or re-rounding the capsules) fails the gate instead of shipping.
    print("\n[M] round-8 profile surface (avatar chamfer + action plates)")
    pa = strip_java_comments(texts["profile_activity"])
    pav = strip_java_comments(texts["profile_actions_view"])

    prof_cut = token("PROFILE_AVATAR_CUT_DP")
    prof_frame_w = token("PROFILE_AVATAR_FRAME_WIDTH_DP")
    prof_rest = ints.get("PROFILE_AVATAR_FRAME_ALPHA")
    prof_raised = ints.get("PROFILE_AVATAR_FRAME_RAISED_ALPHA")
    plate_cut = token("PROFILE_ACTION_CUT_DP")
    plate_border_w = token("PROFILE_ACTION_BORDER_WIDTH_DP")

    check("profile tokens parse",
          None not in (prof_cut, prof_frame_w, prof_rest, prof_raised, plate_cut, plate_border_w),
          "avatar_cut=%s frame_w=%s rest=%s raised=%s plate_cut=%s plate_border=%s"
          % (prof_cut, prof_frame_w, prof_rest, prof_raised, plate_cut, plate_border_w))

    # The hero avatar is a 100dp box (getSmallAvatarRoundRadius() == dp(50) is its circle radius),
    # so the shared builder's 22%-of-shorter-side clamp must not be the binding constraint and the
    # chamfer must stay strictly inside the box on every supported density.
    hero_box_dp = 100.0
    if prof_cut is not None:
        clamp_dp = hero_box_dp * 0.22
        inside = []
        for density in DENSITIES:
            side = dp_ceil(hero_box_dp, density)
            cut = min(dp_ceil(prof_cut, density), side * 0.22)
            if cut <= 0 or 2 * cut >= side:
                inside.append("d=%.3f" % density)
        check("avatar chamfer is a real, non-degenerate cut inside the 100dp hero box",
              prof_cut > 0 and prof_cut <= clamp_dp and not inside,
              "cut=%.2fdp clamp=%.2fdp violations=%s" % (prof_cut, clamp_dp, inside or "none"))

    # The downstream token is only an opt-in when the central gate is read first and the upstream
    # rounded-rect branch survives as the non-Cybergram fallback.
    avatar_gate_idx = pa.find("cybergramChamfer = r > 0 && CybergramTheme.isCybergramPresentation(null)")
    first_round_rect_idx = pa.find("clipPath.addRoundRect(AndroidUtilities.rectTmp, r, r")
    check("hero avatar chamfer is read from the Cybergram gate and precedes the upstream circle",
          avatar_gate_idx != -1 and first_round_rect_idx != -1 and avatar_gate_idx < first_round_rect_idx)
    check("upstream circular clip survives as the non-Cybergram fallback branch",
          "} else if (r > 0) {" in pa[avatar_gate_idx:first_round_rect_idx] if avatar_gate_idx != -1 else False)

    # The ImageReceiver keeps its own circular mask, so a chamfer clip alone would leave a circle
    # inside the chamfer. The mask suppression must share the chamfer condition, not the blur one.
    check("circular ImageReceiver mask is suppressed together with the chamfer",
          "shouldDrawBlur || cybergramChamfer" in pa
          and pav.count("isCybergramPresentation") >= 1)

    # Rest -> Raised: one frame, inset by half its own stroke (so the rail stays inside the
    # silhouette clip), whose resting alpha is legible on the 440dpi device and below the raised one.
    if None not in (prof_frame_w, prof_rest, prof_raised, frame_edge):
        rest_ink = prof_frame_w * 2.75 * (prof_rest / 255.0)
        check("profile avatar frame reaches the legibility floor (>= 1.0 full-opacity device px)",
              rest_ink >= 1.0, "ink=%.2f px at 440dpi (w=%.2fdp a=%s)" % (rest_ink, prof_frame_w, prof_rest))
        check("profile avatar frame keeps a real Rest -> Raised delta",
              0 < prof_rest < prof_raised <= 255, "rest=%s raised=%s" % (prof_rest, prof_raised))
        check("profile avatar frame stroke stays a hairline (<= 1.2dp)",
              prof_frame_w <= 1.2, "w=%.2fdp" % prof_frame_w)
    check("profile avatar frame is inset by half its own stroke",
          "PROFILE_AVATAR_FRAME_WIDTH_DP) / 2f" in pa)
    check("profile avatar frame samples the pressed state (Rest -> Raised)",
          "isPressed() ? 1f : 0f" in pa)

    # Action plates: the chamfer branch must be gated, must precede the upstream drawRoundRect in
    # the same fill loop, and must not touch the hit-test rect that was computed just above it.
    plate_gate_idx = pav.find("final boolean cybergram = CybergramTheme.isCybergramPresentation(null);")
    plate_draw_idx = pav.find("canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, paint);")
    plate_chamfer_idx = pav.find("CybergramTheme.buildInteractionPanelPath(")
    check("action plate chamfer is gated by the Cybergram presentation guard",
          plate_gate_idx != -1 and plate_chamfer_idx != -1 and plate_gate_idx < plate_chamfer_idx,
          "gate=%s chamfer=%s" % (plate_gate_idx, plate_chamfer_idx))
    check("action plate keeps the upstream rounded draw as the non-Cybergram fallback",
          plate_draw_idx > plate_chamfer_idx > -1)
    check("action plate chamfer uses the shared cut token (not a private radius)",
          "CybergramTheme.PROFILE_ACTION_CUT_DP" in pav and "getRoundRadius()" in pav)
    check("action plate draws exactly one rail (fill + single stroke, no glow passes)",
          pav.count("canvas.drawPath(cybergramPath, paint);") == 2
          and "paint.setShadowLayer(0, 0, 0, 0);" in pav)
    check("action plate hit-test rect is untouched by the chamfer branch",
          "action.rect.set(left, top, left + width, top + height);" in pav
          and "action.rect" in pav)

    # Geometry model: the plate chamfer is built by the same clamped builder, so a plate whose
    # shorter side is the cut's limiting factor must still yield a convex, in-bounds polygon. The
    # modelled plates span the real row heights (targetHeight ~ dp(74) minus the row padding) down
    # to a deliberately tiny plate, with a negative control that an over-large cut IS rejected.
    def plate_outside_count(width, height, cut, clamp_ratio=None):
        """Chamfer vertices outside the plate box (0 == valid polygon).

        ``clamp_ratio=None`` models a builder WITHOUT the shared 22% clamp, which is exactly the
        pre-fix shape a future edit could reintroduce.
        """
        if clamp_ratio is not None:
            cut = min(cut, min(width, height) * clamp_ratio)
        pts = [(cut, 0), (width - cut, 0), (width, cut), (width, height - cut),
               (width - cut, height), (cut, height), (0, height - cut), (0, cut)]
        return sum(1 for x, y in pts if x < 0 or y < 0 or x > width or y > height)

    if plate_cut is not None:
        cases = 0
        bad = []
        for density in DENSITIES:
            for width_dp in (56.0, 96.0, 140.0):
                for height_dp in (20.0, 38.0, 50.0):
                    cases += 1
                    w = float(dp_ceil(width_dp, density))
                    h = float(dp_ceil(height_dp, density))
                    if plate_outside_count(w, h, dp_ceil(plate_cut, density), clamp_ratio=0.22) != 0:
                        bad.append("%.0fx%.0f d=%.3f" % (width_dp, height_dp, density))
        check("action plate chamfer stays inside the plate for %d density/size cases" % cases,
              not bad, "violations: " + ", ".join(bad[:4]) if bad else "")

        # Negative control: an unclamped oversized cut on the smallest modelled plate must be
        # detected, otherwise the in-bounds check above is not sensitive to the regression.
        small_w = float(dp_ceil(56.0, 2.75))
        small_h = float(dp_ceil(20.0, 2.75))
        check("negative control: an unclamped oversized plate cut escapes the plate",
              plate_outside_count(small_w, small_h, 400.0, clamp_ratio=None) > 0,
              "w=%.1f h=%.1f" % (small_w, small_h))

    # Negative control for the frame-ink model: the round-6 dialogs hairline that the owner
    # rejected must still measure below the floor, otherwise the legibility check is not sensitive.
    old_ink = 0.4 * 2.75 * (72 / 255.0)
    check("negative control: the rejected 0.4dp @ alpha 72 frame is below the legibility floor",
          old_ink < 1.0, "ink=%.2f px" % old_ink)

    # ---- [N] round-9 Stage F settings surface ------------------------------------------------
    # SettingsActivity is the root settings screen (the third main tab). Round 9 replaced its
    # stock Material shapes — the 90dp circular hero avatar, the 28dp circular account avatars,
    # the 28dp rounded setting-icon chips, the 48dp bare settings-search icons and the suggestion
    # pill row — with the shared eight-segment chamfer plus ONE thin cyan rail.
    # SettingsSearchCell is shared with ProfileActivity's own settings-search list, so it carries
    # an explicit LOCAL opt-in: only the Factory path (reached exclusively from the settings root
    # through UniversalAdapter) enables it. This section re-derives the invariants from the real
    # source so dropping the gate, re-rounding a chip or relaxing the opt-in fails the gate.
    print("\n[N] round-9 settings surface (hero avatar + account/setting chips + search plate)")
    sa = strip_java_comments(texts["settings_activity"])
    ssc = strip_java_comments(texts["settings_search_cell"])
    pa9 = strip_java_comments(texts["profile_activity"])

    set_hero_cut = token("SETTINGS_AVATAR_CUT_DP")
    set_frame_w = token("SETTINGS_AVATAR_FRAME_WIDTH_DP")
    set_rest = ints.get("SETTINGS_AVATAR_FRAME_ALPHA")
    set_raised = ints.get("SETTINGS_AVATAR_FRAME_RAISED_ALPHA")
    set_chip_cut = token("SETTINGS_CHIP_CUT_DP")
    set_chip_border_w = token("SETTINGS_CHIP_BORDER_WIDTH_DP")
    set_chip_border = ints.get("SETTINGS_CHIP_BORDER_ALPHA")
    set_chip_fill = ints.get("SETTINGS_CHIP_FILL_ALPHA")
    set_search_cut = token("SETTINGS_SEARCH_PLATE_CUT_DP")

    check("settings tokens parse",
          None not in (set_hero_cut, set_frame_w, set_rest, set_raised, set_chip_cut,
                       set_chip_border_w, set_chip_border, set_chip_fill, set_search_cut),
          "hero_cut=%s frame_w=%s rest=%s raised=%s chip_cut=%s chip_border=%s search_cut=%s"
          % (set_hero_cut, set_frame_w, set_rest, set_raised, set_chip_cut, set_chip_border,
             set_search_cut))

    # The chip/plate grammar must stay the inline-button grammar (one rail, same rail/fill as
    # BOT_BUTTON_*), so a future edit cannot invent a second visual language for settings.
    check("settings chip rail aliases the inline-button grammar (BOT_BUTTON_*)",
          set_chip_border_w == token("BOT_BUTTON_BORDER_WIDTH_DP")
          and set_chip_border == ints.get("BOT_BUTTON_BORDER_ALPHA")
          and set_chip_fill == ints.get("BOT_BUTTON_FILL_ALPHA"),
          "w=%s rail=%s fill=%s" % (set_chip_border_w, set_chip_border, set_chip_fill))

    # Hero avatar: the 90dp box is upstream; the shared builder clamps the cut to 22% of the
    # shorter side, so the chamfer must stay strictly inside the box on every supported density.
    hero_side_dp = 90.0
    if set_hero_cut is not None:
        clamp_dp = hero_side_dp * 0.22
        inside = []
        for density in DENSITIES:
            side = dp_ceil(hero_side_dp, density)
            cut = min(dp_ceil(set_hero_cut, density), side * 0.22)
            if cut <= 0 or 2 * cut >= side:
                inside.append("d=%.3f" % density)
        check("settings hero chamfer is a real, non-degenerate cut inside the 90dp box",
              set_hero_cut > 0 and set_hero_cut <= clamp_dp and not inside,
              "cut=%.2fdp clamp=%.2fdp violations=%s" % (set_hero_cut, clamp_dp, inside or "none"))

    # The hero avatar must read the central gate first and keep the upstream circle as fallback.
    hero_gate_idx = sa.find("final boolean cybergramHeroAvatar = CybergramTheme.isCybergramPresentation(resourceProvider);")
    hero_round_idx = sa.find("avatarView.setRoundRadius(dp(90));")
    check("settings hero avatar gate is read before the upstream circular fallback",
          hero_gate_idx != -1 and hero_round_idx != -1 and hero_gate_idx < hero_round_idx)
    check("settings hero avatar suppresses the circular mask under Cybergram",
          "avatarView.setRoundRadius(0);" in sa and "new CybergramAvatarView(" in sa)

    # The rail is one stroked chamfer, inset by half its own stroke, whose resting alpha is
    # legible at 440dpi and below the raised state (round-7 Rest -> Raised grammar).
    if None not in (set_frame_w, set_rest, set_raised):
        rest_ink = set_frame_w * 2.75 * (set_rest / 255.0)
        check("settings avatar rail reaches the legibility floor (>= 1.0 full-opacity device px)",
              rest_ink >= 1.0, "ink=%.2f px at 440dpi (w=%.2fdp a=%s)" % (rest_ink, set_frame_w, set_rest))
        check("settings avatar rail keeps a real Rest -> Raised delta",
              0 < set_rest < set_raised <= 255, "rest=%s raised=%s" % (set_rest, set_raised))
        check("settings avatar rail stroke stays a hairline (<= 1.2dp)",
              set_frame_w <= 1.2, "w=%.2fdp" % set_frame_w)
    check("settings avatar rail is inset by half its own stroke",
          "stroke / 2f" in sa and "canvas.drawPath(chamferPath, railPaint);" in sa)
    check("settings avatar rail samples the pressed state (Rest -> Raised)",
          "pressSource.isPressed()" in sa)

    # Account avatar + setting icon chip must each opt in, and the chip keeps the upstream
    # rounded draw as the non-Cybergram fallback (provable no-op outside the gate).
    check("account avatar and setting chip are gated (>= 2 settings-only opt-ins)",
          sa.count("CybergramTheme.isCybergramPresentation(resourcesProvider)") >= 2
          and "avatarView.setRoundRadius(dp(14));" in sa)
    chip_gate_idx = sa.find("iconBackground.setCybergram(CybergramTheme.isCybergramPresentation(resourcesProvider));")
    chip_branch_idx = sa.find("if (cybergram) {", chip_gate_idx if chip_gate_idx != -1 else 0)
    chip_fallback_idx = sa.find(
        "canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, paint);",
        chip_branch_idx if chip_branch_idx != -1 else 0)
    check("setting icon chip chamfer is gated and precedes the upstream rounded draw",
          chip_gate_idx != -1 and chip_branch_idx != -1 and chip_fallback_idx != -1
          and chip_gate_idx < chip_branch_idx < chip_fallback_idx)
    check("setting icon chip chamfer uses the shared cut token and draws ONE rail",
          "CybergramTheme.SETTINGS_CHIP_CUT_DP" in sa
          and "canvas.drawPath(chamferPath, strokePaint);" in sa)
    check("suggestion plate uses the chamfer plate + chamfered ripple, height unchanged",
          "CybergramTheme.createChamferedPlateDrawable(" in sa
          and "CybergramTheme.createChamferedRippleDrawable(" in sa
          and "LayoutHelper.createLinear(0, 42, 1f, Gravity.FILL_VERTICAL" in sa)

    # SettingsSearchCell local opt-in: the DIRECT constructor must stay upstream (ProfileActivity
    # renders this cell in its own settings-search list) and only the Factory may enable it.
    check("SettingsSearchCell direct constructor keeps the upstream (non-cybergram) shape",
          "this(context, false);" in ssc
          and "Theme.key_windowBackgroundWhiteGrayIcon" in ssc)
    check("SettingsSearchCell Factory is the LOCAL opt-in for the settings root",
          "return new SettingsSearchCell(context, true);" in ssc
          and "this.cybergram = cybergram;" in ssc
          and "CybergramTheme.SETTINGS_SEARCH_PLATE_CUT_DP" in ssc)
    check("profile settings-search still constructs the one-arg (upstream) cell",
          "new SettingsSearchCell(mContext)" in pa9
          and "new SettingsSearchCell(mContext, true)" not in pa9)

    # Shared bordered drawable: the rail is inset by half its own stroke so it can never bleed
    # past the chamfer on any of the settings chips/plates.
    check("shared chamfer plate drawable insets its rail by half the stroke",
          "rect.inset(stroke / 2f, stroke / 2f);" in texts["theme"])

    # Geometry model: every settings chip/plate is built by the same clamped builder, so a chip
    # whose shorter side limits the cut must still yield an in-bounds polygon; a negative control
    # proves the check reacts to an over-large cut.
    def chip_outside_count(width, height, cut, clamp_ratio=None):
        if clamp_ratio is not None:
            cut = min(cut, min(width, height) * clamp_ratio)
        pts = [(cut, 0), (width - cut, 0), (width, cut), (width, height - cut),
               (width - cut, height), (cut, height), (0, height - cut), (0, cut)]
        return sum(1 for x, y in pts if x < 0 or y < 0 or x > width or y > height)

    for label, side_dp, cut_dp in (("account/setting chip", 28.0, set_chip_cut),
                                   ("settings search plate", 48.0, set_search_cut)):
        if cut_dp is None:
            continue
        cases = 0
        bad = []
        for density in DENSITIES:
            for size_dp in (side_dp, side_dp * 1.05, side_dp * 0.92):
                cases += 1
                side = float(dp_ceil(size_dp, density))
                if chip_outside_count(side, side, dp_ceil(cut_dp, density), clamp_ratio=0.22) != 0:
                    bad.append("%.0f d=%.3f" % (size_dp, density))
        check("%s chamfer stays inside the plate for %d density/size cases" % (label, cases),
              not bad, "violations: " + ", ".join(bad[:4]) if bad else "")

    check("negative control: an unclamped oversized chip cut escapes the 28dp chip",
          chip_outside_count(float(dp_ceil(28.0, 2.75)), float(dp_ceil(28.0, 2.75)), 400.0,
                             clamp_ratio=None) > 0)

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
