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

### ROUND 11 — РЕЗУЛЬТАТ (2026-10-14, ВЫПОЛНЕНО)

- Правка применена к репо (`r11_apply.py`, все анкеры сверены на вхождение ровно 1 раз):
  `CybergramTheme.java` +71/-1, `ChatAttachAlert.java` +34/-3,
  `Tools/validate_cybergram_geometry.py` +180, новый `STATE.md`.
  Гейт-прогон в стейджинг-дереве (копия дерева вне репо) прошёл ДО записи в репо.
- Гейты (exit 0 оба): geometry `checks=192 failed=0` (до правки 168; +24 проверки
  секции [P], 4 отрицательных контроля), theme
  `unknown=0 duplicates=0 malformed=0 contrast_issues=0 palette_issues=0`.
- Сборка: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline`
  -> `BUILD SUCCESSFUL`, 82 actionable tasks: 5 executed / 77 up-to-date.
  Тяжёлые задачи (native javac/dex) прошли в первом запуске, который был срезан
  120-секундным лимитом foreground-команды; второй запуск дособрал пакет за 9 s.
  Контроль, что правка вошла в артефакт: .class-файлы `CybergramTheme*.class` /
  `ChatAttachAlert*.class` (01:03:04) новее исходников (00:58:53), dex 01:06:20,
  APK 01:06:48.
- APK: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`
  69 069 674 B, sha256
  `E9CC3A6847C4526F1C66AA35EBC5C9915F9E2FF0494AD01E156AD5A9809614E5`,
  mtime `2026-10-14T01:06:48`.
  badging: package `org.telegram.messenger.beta`, versionCode `70389`,
  versionName `12.10.1`, platformBuildVersionName `16`, `native-code: 'x86_64'`;
  `aapt list lib/` -> только `lib/x86_64/libtmessages.49.so` и
  `lib/x86_64/liblanguage_id_l2c_jni.so`.
- Эмулятор `emulator-5554` (AVD Cybergram_API36): `adb install -r` -> `Success`;
  lastUpdateTime 2026-10-13 21:11:21 -> 22:09:59, codePath сменился,
  PID 4174. При первом открытии attach-меню система спросила CAMERA-разрешение
  (окно перекрывало меню) — выдано «While using the app», после этого меню
  открывается чисто.
- Скриншоты (1080x2400, `.local-artifacts/`):
  `r11_attach_before.png` (сборка R10B, 564 350 B) и `r11_attach_after.png`
  (R11, 596 244 B) — требуемый артефакт; кропы полосы
  `r11_strip_before.png` / `r11_strip_after.png`.
  На after полоса типов вложений — тёмная плашка со срезанными 45° углами и одной
  тонкой циановой рейкой; внешний footprint совпадает с прежней стеклянной капсулой
  (тот же `dp(7)` инсет), хит-ректы табов не сдвинулись.
- logcat: буфер очищен (`logcat -c`) перед открытием меню; строк 8188,
  `FATAL EXCEPTION`=0, `ANR in`=0, `beginning of crash`=0, `E AndroidRuntime`=0.
  (Подстрочных совпадений «AndroidRuntime» — 20, все строки uiautomator-процесса,
  не краши.)
- Коммиты (локально, push НЕ делался): `249fd3c26`
  «feat(attach): chamfer the attach-alert tab strip and action plate»;
  коммит обновления этого файла — следующий в `git log`.
- Не проверено (границы доказательства): RTL-раскладка; не-Cybergram контроль на
  живой сборке (вне гейта ветки кода не исполняются по построению, но устройством это
  не снято); физическое arm64-устройство; состояние выбранной вкладки при скролле
  полосы; смена темы при уже открытом attach-меню (фон полосы выбирается в
  конструкторе окна, плашка «Create» — в draw-time).

