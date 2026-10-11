#!/usr/bin/env python3
"""Regression check for the Cybergram .attheme palette.

Statics that must hold:
  - every key in the .attheme is a recognised upstream theme key
    (i.e. present as the string name in ThemeColors.createColorKeysMap() -> "name");
  - no duplicate keys;
  - no malformed (non 'key=value') lines;
  - values are sane ARGB (flag alpha == 0x00 for review, never auto-change);
  - the dialogs secondary-text roles keep WCAG 2.1 >= 4.5:1 on the window, the dialogs row plate
    and the muted counter plate (section [f]), with a negative control on the pre-fix value.

Unlike the in-app parser, this is a local, deterministic cross-check that requires
no Android SDK and no Java AST parsing. It works purely on the string literals in
ThemeColors.java (the same set stringKeyToInt() uses) and on the .attheme text.

Usage:
    python3 tools/validate_cybergram_theme.py
Exit code is non-zero if unknown/duplicate/malformed lines are found.
"""

import pathlib
import re
import sys

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent

THEME_COLORS = REPO_ROOT / "TMessagesProj/src/main/java/org/telegram/ui/ActionBar/ThemeColors.java"
ATTHEME = REPO_ROOT / "TMessagesProj/src/main/assets/cybergram.attheme"
CYBERGRAM_THEME = REPO_ROOT / "TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java"

# WCAG 2.1 relative-luminance contrast, used by the secondary-text gate below.
WCAG_MIN_NORMAL_TEXT = 4.5


def _srgb_channel(value):
    c = value / 255.0
    return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4


def _luminance(rgb):
    r, g, b = rgb
    return 0.2126 * _srgb_channel(r) + 0.7152 * _srgb_channel(g) + 0.0722 * _srgb_channel(b)


def contrast_ratio(fg_rgb, bg_rgb):
    la, lb = _luminance(fg_rgb), _luminance(bg_rgb)
    hi, lo = max(la, lb), min(la, lb)
    return (hi + 0.05) / (lo + 0.05)


def argb_to_rgb(value):
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF)


def alpha_of(value):
    return (value >> 24) & 0xFF


def composite(fg_argb, bg_rgb, alpha=None):
    """Source-over composite of an ARGB colour onto an opaque background."""
    a = alpha_of(fg_argb) if alpha is None else alpha
    f = a / 255.0
    fg = argb_to_rgb(fg_argb)
    return tuple(int(round(f * fg[i] + (1.0 - f) * bg_rgb[i])) for i in range(3))


def extract_int_tokens(text):
    """Resolve `static final int NAME = <0xhex|int|other token>;` to ints."""
    raw = dict(re.findall(
        r"static final int ([A-Za-z_][A-Za-z0-9_]*)\s*=\s*([^;]+);", text))
    values = {}
    for _ in range(len(raw) + 2):
        changed = False
        for key, expr in raw.items():
            expr = expr.strip()
            if re.fullmatch(r"0[xX][0-9A-Fa-f]+[lL]?", expr) or re.fullmatch(r"-?\d+", expr):
                resolved = int(expr.rstrip("lL"), 0)
            else:
                resolved = values.get(expr)
            if resolved is not None and values.get(key) != resolved:
                values[key] = resolved
                changed = True
        if not changed:
            break
    return values


def extract_recognized_keys(text: str) -> set:
    """Return the set of theme key names that stringKeyToInt() can resolve.

    These are the string literals in createColorKeysMap().put(key_..., "NAME").
    """
    pattern = re.compile(r'colorKeysMap\.put\(key_[A-Za-z0-9_]+,\s*"([A-Za-z0-9_]+)"\)')
    return set(pattern.findall(text))


def parse_attheme(text: str):
    """Yield (lineno, key, raw_value) for key=value lines; track malformed/dupes."""
    entries = []          # list of (lineno, key, raw_value)
    malformed = []        # list of (lineno, line)
    seen = {}
    duplicates = list()
    for lineno, raw in enumerate(text.splitlines(), 1):
        line = raw.strip("\r")
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "=" not in line:
            malformed.append((lineno, line))
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        if key in seen:
            duplicates.append((lineno, key))
        seen[key] = lineno
        entries.append((lineno, key, value))
    return entries, malformed, duplicates


def to_argb_hex(raw: str):
    """Parse an .attheme numeric value (signed int, '#'hex or plain int) -> (hex, alpha)."""
    try:
        if raw.startswith("#"):
            v = int(raw[1:], 16)
        else:
            v = int(raw) & 0xFFFFFFFF
    except ValueError:
        return None, None
    return ("0x%08X" % v, (v >> 24) & 0xFF)


def main():
    colors_text = THEME_COLORS.read_text(encoding="utf-8", errors="replace")
    recognized = extract_recognized_keys(colors_text)

    attheme_text = ATTHEME.read_text(encoding="utf-8", errors="replace")
    entries, malformed, duplicates = parse_attheme(attheme_text)

    unknown = [(ln, k) for ln, k, _ in entries if k not in recognized]

    print("== cybergram.attheme audit ==")
    print(f"recognized upstream keys present in ThemeColors: {len(recognized)}")
    print(f"cybergram.attheme key=value entries: {len(entries)}")

    print("\n[a] recognized keys:", len([e for e in entries if e[1] in recognized]))
    if unknown:
        print(f"\n[b] UNKNOWN keys ({len(unknown)}):")
        for ln, k in unknown:
            print(f"    line {ln}: {k}")
    else:
        print("\n[b] UNKNOWN keys: 0")

    if duplicates:
        print(f"\n[c] DUPLICATE keys ({len(duplicates)}):")
        for ln, k in duplicates:
            print(f"    line {ln}: {k}")
    else:
        print("\n[c] DUPLICATE keys: 0")

    if malformed:
        print(f"\n[d] MALFORMED lines (no '=' , {len(malformed)}):")
        for ln, line in malformed:
            print(f"    line {ln}: {line!r}")
    else:
        print("\n[d] MALFORMED lines: 0")

    # [e] suspected legacy/alias keys: report unknowns that look like renames
    alias_like = [k for _, k, _ in entries if k not in recognized and ("Selector" in k or "Dropdown" in k or "SDK21" in k)]
    print("\n[e] alias-suspect keys:", ", ".join(sorted(set(alias_like))) or "none")

    print("\n== ARGB sanity ==")
    for ln, k, v in entries:
        hexv, alpha = to_argb_hex(v)
        if alpha is None:
            print(f"    line {ln}: {k} = {v!r}  (non-numeric)")
        else:
            flag = "  <-- alpha=00" if alpha == 0 else ""
            print(f"    line {ln}: {k} = {v} -> {hexv}{flag}")

    # [f] WCAG contrast of the dialogs secondary-text roles.
    #
    # Audit 2026-10-11 P1: "preview/timestamp secondary text too low-contrast on dark background",
    # acceptance 4.5:1. The chat-list row paints its own plate (IN_BUBBLE over BACKGROUND) and the
    # counters a raised plate (PANEL_RAISED at DIALOGS_BADGE_MUTED_FILL_ALPHA over the row plate),
    # so secondary text has to clear 4.5:1 on those, not only on the window background.
    print("\n[f] WCAG contrast of dialogs secondary-text roles")
    tokens = extract_int_tokens(CYBERGRAM_THEME.read_text(encoding="utf-8", errors="replace"))

    contrast_bad = []
    try:
        window = argb_to_rgb(tokens["BACKGROUND"])
        row_plate = composite(tokens["IN_BUBBLE"], window)
        badge_plate = composite(tokens["PANEL_RAISED"], row_plate,
                                tokens["DIALOGS_BADGE_MUTED_FILL_ALPHA"])
    except KeyError as exc:
        print("    missing token: %s" % exc)
        contrast_bad.append("tokens")

    if not contrast_bad:
        surfaces = [
            ("window", window),
            ("dialogs row plate", row_plate),
            ("muted counter plate", badge_plate),
        ]
        roles = [
            ("TEXT_MUTED", "muted preview / timestamp / muted counter numeral"),
            ("DIALOGS_TEXT_PREVIEW", "dialogs preview"),
            ("DIALOGS_TEXT_META", "dialogs timestamp"),
            ("DIALOGS_TEXT_UNREAD", "unread dialogs title"),
            ("DIALOGS_TEXT_READ", "read dialogs title"),
        ]
        for role, what in roles:
            if role not in tokens:
                print("    %-22s MISSING" % role)
                contrast_bad.append(role)
                continue
            fg = argb_to_rgb(tokens[role])
            worst = None
            for surface_name, surface_rgb in surfaces:
                ratio = contrast_ratio(fg, surface_rgb)
                worst = ratio if worst is None else min(worst, ratio)
                ok = ratio >= WCAG_MIN_NORMAL_TEXT
                print("    %-22s on %-20s %5.2f:1  %s   (%s)"
                      % (role, surface_name, ratio, "PASS" if ok else "FAIL", what))
            if worst < WCAG_MIN_NORMAL_TEXT:
                contrast_bad.append(role)

        # Negative control: the pre-fix muted value must be detected as failing, otherwise this
        # section is not actually sensitive to the regression.
        old_muted = (0x6B, 0x7A, 0x8A)
        old_ratios = [contrast_ratio(old_muted, s) for _, s in surfaces]
        print("    negative control: #6B7A8A  %s  (min %.2f:1)"
              % (" ".join("%.2f" % r for r in old_ratios), min(old_ratios)))
        if min(old_ratios) >= WCAG_MIN_NORMAL_TEXT:
            print("    negative control FAILED: pre-fix value is not detected as too low")
            contrast_bad.append("negative-control")

    summary_bad = bool(unknown) or bool(duplicates) or bool(malformed) or bool(contrast_bad)
    print("\n== RESULT ==")
    print(f"unknown={len(unknown)} duplicates={len(duplicates)} malformed={len(malformed)}"
          f" contrast_issues={len(contrast_bad)}")
    if summary_bad:
        print("FAIL")
        sys.exit(1)
    print("OK")


if __name__ == "__main__":
    main()
