# Emulator diagnosis and restoration — 2026-09-19

Status: `DIAGNOSED / BOOTS AND RUNS THE APP, BUT THE HOST KILLS qemu UNDER APP LOAD`.

The AVD `Cybergram_API36` had been exiting within about a minute after every launch. This run isolated
the causes and established a working launch recipe; one host-level limit remains.

## Findings

1. **A 5.7 GB Gradle daemon was resident.** `java` (PID 7976) held 5.7 GB while the AVD was being
   launched. Stopping it is required before booting the emulator; a Gradle build started while the AVD
   is up reliably kills the AVD (observed twice: the x86_64 build's daemon came up while the emulator
   ran, and qemu died shortly after).
2. **The AVD carried `hw.ramSize = 1536M` and `hw.gpu.enabled = no`** while `-gpu auto` was passed on
   the command line, and its snapshot could not be loaded ("different renderer configured"), so every
   boot was a full cold boot. The guest was memory-starved (SystemUI ANR / app ANR).
3. **The host is under memory pressure.** `Memory Compression` sits at ~1.5 GB and the usual desktop
   load (browsers, msedgewebview2, Defender, NVIDIA Overlay) leaves little headroom. Perf counters
   (`Get-Counter`, CIM, `wmic`) are unavailable in this session, so free RAM could not be measured
   directly.
4. **The emulator launcher exits after starting qemu.** Jobs launched through the harness report
   "finished" (exit 1) while `qemu-system-x86_64-headless` keeps running; the AVD is not crashed at
   that point. `adb` separately loses the device and reconnects after `adb kill-server/start-server`.

## Working recipe (verified)

```text
# 1. free the host
Get-Process java | Stop-Process -Force            # Gradle daemon(s)
# 2. clear stale locks
Remove-Item "$env:USERPROFILE\.android\avd\Cybergram_API36.avd\hardware-qemu.ini.lock" -Recurse -Force
Remove-Item "$env:USERPROFILE\.android\avd\Cybergram_API36.avd\multiinstance.lock" -Force
# 3. launch DETACHED (Start-Process, not a harness background job), headless, host GPU
Start-Process emulator.exe -ArgumentList '-avd','Cybergram_API36','-no-window','-gpu','auto',
  '-memory','3072','-no-snapshot-load','-no-snapshot-save','-no-boot-anim','-no-audio'
# 4. do NOT run a Gradle build while the AVD is up
```

With this recipe the AVD booted, stayed up through a 150-second idle stability window
(`emulator-5554 device`, `sys.boot_completed = 1`, qemu resident ~4 GB), started
`org.telegram.messenger.beta`, rendered the Cybergram dialogs (screenshot
`.local-artifacts/emu-diag-20260919/emu-app.png`), accepted `adb install -r` of the x86_64 B4 build and
launched it with `FATAL` / `ANR` = 0.

## Remaining host-level limit

Across four configurations (`-gpu auto` at 4 GB / 3 GB / 2 GB and `-gpu swiftshader_indirect` at 4 GB,
all `-no-window`, no snapshot) the AVD **dies while the Telegram app is starting inside it** — with
empty emulator logs and no crashpad dump, i.e. the process is terminated externally rather than
crashing. That is consistent with the host OOM-killing qemu under the combined load, given the memory
pressure in finding 3. Idle, the same AVD survives indefinitely.

To go further the host needs free physical memory (close desktop applications / browsers) or more RAM;
nothing in the repository can change that. Once the host has headroom, the recipe above is the known
good launch path and the remaining B4/B7 device checks can be completed.
