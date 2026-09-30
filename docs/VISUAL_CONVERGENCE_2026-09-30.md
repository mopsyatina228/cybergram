# Visual convergence pass — 2026-09-30

Status: first implementation pass complete on `feature/visual-convergence-20260930`; runtime visual review pending.

Authority: the owner re-supplied the correct target concept and explicitly asked that the next pass treat colour composition, fill, transparency and restrained neon as first-class parts of the match, alongside typography and bubble geometry.

## Direction

The target is not a flat `dark fill + coloured stroke` skin. It uses a near-black canvas, subtly translucent message material, cyan/amber edge light, red structural rails, condensed technical typography and sparse background instrumentation. Glow is an edge hierarchy, not a blanket blur.

## Implemented in this pass

- message surfaces are now dark translucent materials rather than fully opaque fills;
- the existing thin message outline remains the bright core, with a wider low-alpha pass on the exact same cached path for restrained neon bloom;
- the outer message spur is lengthened while the measured 5 dp chamfer remains unchanged;
- message body, metadata, reply/name and service/date typography now use the Cybergram condensed family, with stock faces restored outside Cybergram;
- composer input typography follows the same family; its chamfered frame now has a dim halo under the thin core;
- the chat canvas gains a very low-alpha cyan/red structural grid plus red edge rails behind the message list;
- custom date separators gain an amber outline + halo on the existing B6 angular path;
- non-Cybergram rendering remains runtime-gated and keeps upstream geometry/material paths.

## Validation

`tools/validate_cybergram_theme.py`: `unknown=0 duplicates=0 malformed=0`.
`git diff --check`: clean (apart from the expected Windows `.attheme` EOL warning).
`TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline`: **BUILD SUCCESSFUL**. Final APK: 69,442,384 bytes, SHA-256 `DDEBEF017D7AD1B2CD124CEE0787F2E2DE63E8E25C70BB1B49201B0E3E8EF795`. Runtime visual review remains pending: no device is attached, and the known AVD recipe requires freeing the host Gradle daemon / memory first; do not disturb unrelated active work merely to manufacture a screenshot.
