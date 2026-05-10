# Android Framework Vulnerability Testing Suite

This repository contains proof-of-concept test applications for verifying security vulnerabilities in the Android Framework.

## Vulnerabilities Being Tested

### Bug #4: Buffer Overflow in Native String Serialization (CRITICAL)
- **Severity:** Critical
- **Component:** Parcel Native String Handling
- **Bounty Estimate:** $75,000 - $200,000
- **Files:** `frameworks/base/core/jni/android_os_Parcel.cpp`
- **Status:** Testing in progress

### Bug #1: Integer Overflow in Parcel Array Operations (CRITICAL)
- **Severity:** Critical
- **Component:** Binder IPC / Parcel Serialization
- **Bounty Estimate:** $75,000 - $150,000
- **Files:** `frameworks/base/core/jni/android_os_Parcel.cpp`
- **Status:** Testing in progress

## Project Structure

```
android-vuln-tests/
├── Bug4-StringOverflow/          # Test app for string buffer overflow
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/security/test/
│   │   │   │   └── MainActivity.java
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   └── build.gradle
│   ├── build.gradle
│   └── settings.gradle
│
├── Bug1-IntegerOverflow/         # Test app for integer overflow
│   └── (similar structure)
│
├── .github/
│   └── workflows/
│       └── build-apks.yml        # GitHub Actions for automated builds
│
└── README.md
```

## Building APKs

### Option 1: GitHub Actions (Automated)
1. Push code to GitHub
2. GitHub Actions automatically builds APKs
3. Download from Actions artifacts

### Option 2: Local Build
```bash
cd Bug4-StringOverflow
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Testing Instructions

### Prerequisites
- Android device or emulator with Android 14/15
- USB debugging enabled
- ADB installed

### Install and Run
```bash
# Install APK
adb install app-debug.apk

# Run tests
adb shell am start -n com.security.test/.MainActivity

# Monitor logs
adb logcat -s VulnTest:* AndroidRuntime:E
```

## Expected Results

### If Bug Exists:
- App crashes with native crash
- Logcat shows memory corruption
- System service may crash

### If Bug is Patched:
- Exception thrown before crash
- Proper bounds checking
- No memory corruption

## Security Notice

⚠️ **WARNING:** These test applications are for security research purposes only. Do not use on production devices or distribute maliciously.

## License

This code is provided for security research and responsible disclosure to Google's Android Security Team.
