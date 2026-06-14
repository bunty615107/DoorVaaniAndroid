# DoorVaani - Open Dialer (Sangam Design System)

Native Android app (Kotlin + Jetpack Compose) implementing the DoorVaani PRD with high fidelity to Stitch goldens.

## Features (Phases 1-3 complete as prototype)
- Full call experience (simulated): DialPad, Incoming, Active Call with 8-hex controls, PulseRing, Marcus Aurelius integration.
- Local-first E2EE Knowledge Vault with date-grouped records, Secure/Internal badges, playback stubs, backup/restore, journal export.
- Advanced Focus: Dharma Mode (UI quieting + mantra dialog), Vastu Dialing (aligned recents + interactive calendar suggestions).
- Premium on-device AI insights (local generator for themes/mantras/summaries).
- Community Shield (animated threat gauge + federation).
- Full localization: English + Hindi.
- Custom shaders (mandala/yantra from GLSL ports), Architectural design system tokens.
- Optimizations: LazyColumn with keys, frame-synced shaders, semantics/a11y.

## Build the APK

### Recommended: Android Studio (easiest, handles everything)
1. Install latest Android Studio.
2. Open this `DoorVaaniAndroid` folder.
3. Let Gradle sync complete (it will download Gradle + Android SDK if needed).
4. Go to menu: **Build > Build Bundle(s)/APK(s) > Build APK(s)**.
5. After success, click the "locate" link in the build notification.

The debug APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Command line (PowerShell on Windows)
First, generate the Gradle wrapper (requires Gradle installed globally, or use AS above):

```powershell
cd "D:\Door Vaani\DoorVaaniAndroid"
gradle wrapper
```

Then build:

```powershell
cd "D:\Door Vaani\DoorVaaniAndroid"
.\gradlew clean assembleDebug
```

APK location:
`app\build\outputs\apk\debug\app-debug.apk`

## Run & Test
- Install the APK on emulator or device (API 26+).
- Test key flows:
  - Home → Vastu toggle → tap hint for calendar suggestion + "Add to Calendar".
  - Dial or Contacts → call → record in Active Call → end → Vault (enable Premium AI in Settings to see insights).
  - Settings → toggle themes (DoorVaani light ↔ Sangam dark), sync, federation.
  - Change device language to Hindi for full localization.

## Notes
- This is a high-fidelity prototype. Real telephony, audio recording, and on-device ML are stubbed for demo (per PRD phases).
- All UI matches Stitch goldens + DESIGN.md tokens (8px rhythm, ArchitecturalCard, shaders, hex controls, etc.).
- See `implementation-notes.md` (in parent folder) for full history, fidelity notes, and Phase details.

Built following the MultiAgent Prompt + PRD.

For issues or to extend (real Calendar provider, STT hook, etc.), share build logs or describe what you need.