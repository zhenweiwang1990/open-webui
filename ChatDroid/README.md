# ChatDroid (Totally written by AI Coding Agents with gbox.ai)

ChatDroid is a native Android app for **Open WebUI**. It allows you to run Open WebUI locally (or on a remote server) and interact with it from an Android phone or tablet.

> **Note**: ChatDroid does **not** ship Open WebUI itself. You still need a running backend (local or remote) for the app to connect to.

---

## Getting Started

### Prerequisites

1. **Cursor or Claude Code** Let the AI write codes~~~~~❤️
2. **Gbox MCP configured** Give the AI testing sandbox~~~~~❤️
3. **Android SDK** 33+ (API 33 is used by default; you can change in `build.gradle.kts`)
4. **JDK 17** (bundled with recent Android Studio versions)

### Build & Run from Android Studio

1. Import the `ChatDroid` folder ( _File ▶ Open…_ ).
2. Let Gradle sync the project (first launch may download dependencies).
3. Connect an Android device or start an emulator.
4. Press **Run ▶** to install the _debug_ build on your device.

### Build from the Command Line

```bash
cd ChatDroid
./gradlew assembleDebug   # Output: app/build/outputs/apk/debug/app-debug.apk
```

Install the resulting APK on your device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Configuration

ChatDroid gets its backend endpoint from a compile-time `BuildConfig` value:

```kotlin
BuildConfig.BASE_URL
```

### Overriding the URL

You can point the app at any Open WebUI instance **without touching the source
code**:

1. **Gradle property (recommended)**

   ```bash
   ./gradlew assembleRelease \
       -POPEN_WEBUI_BASE_URL=https://my.server.com/api/v1/
   ```

2. **`gradle.properties`** – add a line:

   ```properties
   OPEN_WEBUI_BASE_URL=https://my.server.com/api/v1/
   ```

3. **Android Studio** – _Run ▶ Edit Configurations… ▶ Gradle_ and set
   `OPEN_WEBUI_BASE_URL` in the **Gradle properties** field.

If you’re running the backend on the Android Emulator’s host machine, use
`http://10.0.2.2:8080/api/v1/`.

---

## Folder Structure

```
ChatDroid/
 ├─ app/                 # Android application module
 ├─ gradle/              # Gradle wrapper files
 ├─ build.gradle.kts     # Top-level Gradle settings
 └─ settings.gradle.kts  # Module declaration
```

---

## License

ChatDroid is distributed under the MIT License. See the root `LICENSE` file for details. 