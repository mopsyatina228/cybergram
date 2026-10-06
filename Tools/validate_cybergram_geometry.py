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
