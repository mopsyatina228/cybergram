#!/usr/bin/env python3
"""Regression check for the Cybergram .attheme palette.

Statics that must hold:
  - every key in the .attheme is a recognised upstream theme key
    (i.e. present as the string name in ThemeColors.createColorKeysMap() -> "name");
  - no duplicate keys;
  - no malformed (non 'key=value') lines;
  - values are sane ARGB (flag alpha == 0x00 for review, never auto-change).

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

    summary_bad = bool(unknown) or bool(duplicates) or bool(malformed)
    print("\n== RESULT ==")
    print(f"unknown={len(unknown)} duplicates={len(duplicates)} malformed={len(malformed)}")
    if summary_bad:
        print("FAIL")
        sys.exit(1)
    print("OK")


if __name__ == "__main__":
    main()
