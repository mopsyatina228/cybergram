# Cybergram — durable state (repo root)

Авторитетный журнал раундов Cybergram UI. Раунды 1–10 записаны в
`.local-artifacts/cybergram-state.md`, в `docs/` и в истории коммитов ветки
`feature/dialogs-incoming-palette`; этот файл ведётся с раунда 11.

## ROUND 11 — ATTACHMENTS (Stage F, поверхность 3 из 3): paperclip attach-alert menu

### Вход
- repo `C:\Users\mopsy\Documents\projects\cybergram`, ветка `feature/dialogs-incoming-palette`
- entry HEAD `93d6595a7b6017ead3fda338c9428c65607e182c` (R10B), worktree чист
- эмулятор `emulator-5554` (AVD Cybergram_API36), пакет `org.telegram.messenger.beta`

### Скоуп (жёстко ограничен поверхностью attach-alert)
- `ChatAttachAlert.java` и только собственный chrome этого окна (меню «скрепки»).
- НЕ трогались: геометрия пузырей, touch targets, высоты строк, существующие
  chamfer-значения и рамки раундов 6–9; типографика профиля R10; палитра D2 (R10B)
  и `TEXT_MUTED`.

### Изменения (applied)
- `CybergramTheme.java`: новый документированный блок токенов `ATTACH_*` —
  `ATTACH_TAB_STRIP_CUT_DP = 10f`, `ATTACH_TAB_STRIP_BORDER_WIDTH_DP` /
  `ATTACH_TAB_STRIP_BORDER_ALPHA` / `ATTACH_TAB_STRIP_FILL_ALPHA` (алиасы
  `BOT_BUTTON_*`, т.е. принятая грамматика inline-button/чипов раунда 9),
  `ATTACH_ACTION_CUT_DP = 6f`; плюс перегрузка
  `createChamferedPlateDrawable(..., int insetPx)`. 6-аргументный контракт раунда 9
  сохранён делегированием (вызовы настроек не менялись); `insetPx` — только
  отрисовочный инсет, через `getPadding()` не отдаётся, поэтому не может сдвинуть
  дочерние вью или хит-ректы.
- `ChatAttachAlert.java`:
  1. полоса типов вложений (табы Gallery/File/Music/Location/Contact/Poll):
     стеклянная капсула-фон (blur, radius 28dp) заменяется под гейтом на
     chamfer-плиту `PANEL_RAISED` + одна тонкая `CYAN`-рейка, нарисованную в том же
     `dp(7)` инсете, в котором рисовала капсула. 70dp-обёртка, `dp(11)` паддинг
     табов, clip `dp(11)/dp(28)` и все хит-ректы табов не изменены; стеклянный
     drawable остаётся non-Cybergram веткой;
  2. плашка «Create» (`doneItem`) на action bar окна: гейт читается в draw-time,
     силуэт строится общим `CybergramTheme.buildInteractionPanelPath` с
     `ATTACH_ACTION_CUT_DP`; коробка 28dp (`getHeight()/2 ± dp(14)`), ширина и
     хит-рект не изменены, upstream round-rect остаётся fallback.
- `Tools/validate_cybergram_geometry.py`: новая секция `[P]` (round-11 attach
  surface) — проверки источника + плотностная геометрическая модель + отрицательные
  контроли (pre-fix капсула, pre-fix плашка, неограниченный cut, отброшенный inset).
- Осознанно вне скоупа (границы поверхности): `emojiViewChildBg` (неодинаковые
  радиусы top 29 / bottom 0 + отдельный in-app-keyboard путь отрисовки),
  caption-плиты (`captionContainerBg` / `drawable2` видны только после выбора и
  несут второй внутренний round-clip `dp(20)` по контенту), grabber `dp(2)`
  (высота 4dp, кламп 22% = 0.88dp — визуально неотличим), selected-plate табов
  (`GlassTabView` — shared-компонент, его opt-in `cybergramMainTabsPresentation`
  принадлежит B1 и для attach-табов не используется).

### Гейты (перед сборкой)
```
python Tools/validate_cybergram_geometry.py -> exit 0
  == RESULT == | checks=192 failed=0 | OK
python Tools/validate_cybergram_theme.py -> exit 0
  == RESULT == | unknown=0 duplicates=0 malformed=0 contrast_issues=0 palette_issues=0 | OK
```

### Дальше
1. Сборка `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline`.
2. Установка на `emulator-5554`, открыть attach-меню, скриншот
   `.local-artifacts/r11_attach_after.png`, logcat FATAL/ANR = 0.
3. Коммиты локально, БЕЗ push.
