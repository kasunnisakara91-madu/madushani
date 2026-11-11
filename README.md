# MADU JARVIS AI 🤖⚡
> **Your Voice. Your Phone. Your AI.**

A complete, production-grade native Android voice assistant built with **Kotlin** and **Jetpack Compose (Material 3)**. MADU JARVIS AI provides seamless voice interaction in **Sinhala (සිංහල)**, **English**, and **Sinhala + English mixed commands**, complete with intelligent phone hardware automation, secure lock-screen assistance, holographic animated UI, and Gemini AI integration.

---

## 🌟 Key Features

### 1. Advanced Voice Processing & Text-To-Speech
- **Native Android SpeechRecognizer**: Supports Sinhala (`si-LK`), English (`en-US`), and mixed multilingual speech.
- **Android Text-To-Speech (TTS)**: Spoken voice responses after every command with adjustable speech rate and pitch.
- **Push-to-Talk & Continuous Voice**: Interactive animated microphone control with live partial transcription and volume amplitude tracking.

### 2. Holographic Futuristic HUD Interface
- **Animated Circular AI Orb**: Concentric rotating orbital rings, sci-fi crosshairs, and a pulsing core reacting dynamically to user voice amplitude.
- **Real-Time Waveform Equalizer**: Smooth animated equalizer bars reflecting microphone audio levels.
- **Dark Navy / Cyber Cyan Aesthetic**: Material 3 design with glowing borders, holographic status badges, and quick command chips.

### 3. Native Phone Hardware Control
- **Open Installed Apps**: YouTube, WhatsApp, Camera, Browser, Maps, Phone Dialer, and SMS.
- **Hardware Flashlight**: Instant on/off torch control via `CameraManager`.
- **System Volume**: Step up, step down, and audio adjustments via `AudioManager`.
- **Battery Status**: Live battery percentage and charging state reporting in Sinhala and English.
- **Current Time & Date**: Accurate localized time reporting.
- **Android System Settings**: Direct shortcuts to Wi-Fi, Bluetooth, Display, Sound, and App Settings.
- **Alarms & Calendar**: Create calendar reminders and trigger system alarms.

### 4. Lock Screen & Screen-Off Assistant
- **ShowWhenLocked & TurnScreenOn**: Displays assistant overlay above the lock screen securely when needed.
- **"Unlock My Phone" Protocol**: When the user says *"Unlock my phone"* or *"මගේ phone එක unlock කරන්න"*, Jarvis displays:
  > *"Please authenticate to continue."*
- **Android Keyguard / Biometric Handoff**: Safely invokes the device's system authentication (PIN, Pattern, Password, Fingerprint, or Face Unlock) using `KeyguardManager`. Never bypasses or intercepts user credentials.
- **Optional Background Service**: Foreground service with microphone notification (`foregroundServiceType="microphone"`) and visible stop control.

### 5. Gemini AI Intelligence
- Seamless conversational queries powered by Google's Gemini models.
- Configured securely through `BuildConfig` and the AI Studio Secrets panel.
- Built-in offline intelligent fallback response engine so the assistant never crashes when disconnected.

### 6. Local Persistence
- **Room Database**: Complete audit history of voice commands, execution timestamps, language badges, and AI responses.
- **Jetpack DataStore**: Safe asynchronous storage of user preferences (language mode, speech rate, pitch, API keys).

---

## 🚀 Getting Started

### 1. Open the Project in Android Studio
1. Clone or download the repository.
2. Launch **Android Studio (Ladybug / Meerkat or later recommended)**.
3. Select **File > Open...** and select the project root folder.
4. Allow Gradle to sync dependencies automatically.

### 2. Add Your Gemini API Key Securely
1. In Google AI Studio, obtain your free Gemini API key from [aistudio.google.com](https://aistudio.google.com).
2. Configure it in one of two ways:
   - **Method A (Secrets / .env):** Copy `.env.example` to `.env` in the project root:
     ```bash
     cp .env.example .env
     ```
     Add your key:
     ```properties
     GEMINI_API_KEY=AIzaSy...your_actual_key_here
     ```
   - **Method B (In-App Settings):** Open the app, navigate to the **Settings** tab, scroll to **Gemini API Configuration**, paste your key, and tap **Save Key**.

### 3. Build the APK
- **From Android Studio**:
  - Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
  - The generated APK will be at:
    `app/build/outputs/apk/debug/app-debug.apk`
- **From Command Line**:
  ```bash
  gradle assembleDebug
  ```

### 4. Install the APK on Your Android Device
1. Enable **Developer Options** and **USB Debugging** on your Android device (Android 8.0 Oreo or later).
2. Connect your device via USB or wireless debugging and run:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   *(Or transfer the `app-debug.apk` directly to your phone and tap to install).*

### 5. Enable Voice Assistant Permissions
When first launching MADU JARVIS AI:
1. Tap **Allow** when prompted for **Record Audio** permission.
2. Tap **Allow** for **Notifications** (required on Android 13+).
3. If missed, navigate to the **Security** tab in the app to view and grant any missing permissions with one tap.

### 6. Enable Optional Background Listening
1. In the app, switch to the **Settings** tab.
2. Toggle on **Background Listening Mode**.
3. A persistent notification titled **"MADU JARVIS AI"** with a visible **"Stop Listening"** button will appear in your status bar, ensuring full transparency in accordance with Android and Google Play policies.

---

## 🗣️ Supported Voice Commands

| Category | Sinhala Command | English Command |
|---|---|---|
| **Greetings** | *"හායි මාදු"*, *"හලෝ ජාර්විස්"* | *"Hey Madu"*, *"Hey Jarvis"* |
| **Flashlight** | *"Flashlight on කරන්න"*, *"ටෝච් එක දාන්න"* | *"Flashlight on"*, *"Torch on"* |
| **Flashlight** | *"Flashlight off කරන්න"*, *"ටෝච් එක නිවන්න"* | *"Flashlight off"*, *"Torch off"* |
| **Volume** | *"Volume වැඩි කරන්න"*, *"සද්දෙ වැඩි කරන්න"* | *"Volume up"*, *"Increase volume"* |
| **Volume** | *"Volume අඩු කරන්න"*, *"සද්දෙ අඩු කරන්න"* | *"Volume down"*, *"Decrease volume"* |
| **Battery** | *"මගේ ෆෝන් එකේ බැටරි එක කීයද?"*, *"බැටරි කීයද"* | *"Battery status"*, *"What is my battery level?"* |
| **Time & Date** | *"දැන් වෙලාව කීයද?"*, *"වෙලාව කීයද"* | *"What time is it?"*, *"Tell me the time"* |
| **Unlock Device** | *"මගේ phone එක unlock කරන්න"* | *"Unlock my phone"*, *"Unlock device"* |
| **YouTube** | *"YouTube open කරන්න"*, *"YouTube දාන්න"* | *"Open YouTube"* |
| **WhatsApp** | *"WhatsApp open කරන්න"* | *"Open WhatsApp"* |
| **Camera** | *"Camera open කරන්න"*, *"කැමරා එක දාන්න"* | *"Open Camera"* |
| **Browser** | *"Browser open කරන්න"* | *"Open Browser"*, *"Open Chrome"* |
| **Settings** | *"Wi-Fi open කරන්න"*, *"Settings අරින්න"* | *"Open Wi-Fi settings"*, *"Open Settings"* |
| **Alarms & Calendar** | *"Alarm එකක් තියන්න"* | *"Set an alarm"*, *"Open Calendar"* |
| **AI Knowledge** | *"ශ්‍රී ලංකාවේ අගනුවර කුමක්ද?"* | *"Explain quantum computing briefly"* |

---

## 🛡️ Security & Privacy Architecture
- **Zero Lock-Bypass**: Lock screen authentication relies strictly on Android's `KeyguardManager` and biometric APIs. The app never intercepts, stores, or automates user passwords, PINs, or fingerprints.
- **User Transparency**: Continuous microphone listening is strictly opt-in and bound to a visible foreground notification with an immediate stop control.
- **Allowlisted Execution**: Command dispatching uses a strict Kotlin pattern-matched dispatcher. Arbitrary shell commands or dynamic code loading are prohibited.

---

## 🏗️ Architecture Stack
- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Local Database**: Room SQLite Database
- **Preferences**: Jetpack DataStore Preferences
- **Networking**: OkHttp 4.10 + Retrofit
- **Speech**: Android `SpeechRecognizer` + `TextToSpeech`
- **AI**: Gemini 2.5 Flash REST integration via secure `BuildConfig`
- **Target SDK**: 36 (Android 15+)
- **Minimum SDK**: 26 (Android 8.0 Oreo)

---

## 📜 License
Developed for the Android Ecosystem with Google AI Studio.
**MADU JARVIS AI** — Designed for speed, intelligence, and accessibility.
