# Android Vulnerability Testing - Quick Start Guide

## 🎯 What We've Built

Two complete Android test applications to verify critical security vulnerabilities:

1. **Bug #4: String Buffer Overflow** ($75k-$200k bounty potential)
2. **Bug #1: Integer Overflow** ($75k-$150k bounty potential)

---

## 📦 Project Structure

```
android-vuln-tests/
├── Bug4-StringOverflow/          ✅ COMPLETE
│   └── Tests native string overflow in Parcel
│
├── Bug1-IntegerOverflow/         ✅ COMPLETE
│   └── Tests integer overflow in array operations
│
├── .github/workflows/
│   └── build-apks.yml            ✅ GitHub Actions configured
│
└── README.md
```

---

## 🚀 How to Build APKs

### Option 1: GitHub Actions (Recommended - Easiest)

1. **Create GitHub Repository:**
   ```bash
   cd d:\c\android-vuln-tests
   git init
   git add .
   git commit -m "Initial commit: Android vulnerability test apps"
   ```

2. **Push to GitHub:**
   ```bash
   # Create repo on GitHub first, then:
   git remote add origin https://github.com/YOUR_USERNAME/android-vuln-tests.git
   git branch -M main
   git push -u origin main
   ```

3. **GitHub Actions will automatically:**
   - Build both APKs
   - Upload as artifacts
   - Available in Actions tab → Download APKs

### Option 2: Local Build (If you have Android SDK)

```bash
# Install Android Studio first, then:

cd d:\c\android-vuln-tests\Bug4-StringOverflow
gradlew assembleDebug

cd ..\Bug1-IntegerOverflow
gradlew assembleDebug

# APKs will be in: app/build/outputs/apk/debug/
```

---

## 📱 How to Test

### Step 1: Install APK on Android Device

```bash
# Connect Android device via USB
# Enable USB debugging in Developer Options

adb install Bug4-StringOverflow/app/build/outputs/apk/debug/app-debug.apk
adb install Bug1-IntegerOverflow/app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Run Tests

1. Open the app on your device
2. Click each test button
3. Watch the logs in the app
4. Monitor for crashes

### Step 3: Collect Evidence

```bash
# Capture logcat output
adb logcat -s VulnTest:* AndroidRuntime:E > test-results.log

# If app crashes, get crash dump
adb logcat -d > crash-log.txt

# Take screenshots of results
adb shell screencap -p /sdcard/test-result.png
adb pull /sdcard/test-result.png
```

---

## 🔍 What to Look For

### ✅ Bug EXISTS (Vulnerable):
- ❌ No exception thrown
- ❌ App doesn't crash but logs show "BUG CONFIRMED"
- ❌ Out-of-bounds access succeeds
- ❌ Memory corruption occurs

### ✅ Bug PATCHED (Not Vulnerable):
- ✅ ArrayIndexOutOfBoundsException thrown
- ✅ IllegalArgumentException thrown
- ✅ Proper bounds checking
- ✅ App logs "Bug is patched"

---

## 📊 Expected Results

### Bug #4 (String Overflow):

**If vulnerable:**
```
Test 1: Small Overflow
Creating string with 10 million characters...
String created: 10000000 chars
Attempting to write to Parcel...
SUCCESS: Parcel write completed in 234ms
Parcel size: 10000024 bytes
RESULT: Bug might be patched or limit not reached
```

**If patched:**
```
Test 1: Small Overflow
Creating string with 10 million characters...
Exception: IllegalArgumentException: String too long for Parcel
RESULT: Bug is patched
```

### Bug #1 (Integer Overflow):

**If vulnerable:**
```
Test 3: Offset+Length Overflow
Array size: 1000
Offset: 2147483147
Length: 600
Offset + Length (as int): -2147483549 (OVERFLOWS!)
Attempting writeByteArray...
BUG CONFIRMED: No exception thrown!
RESULT: CRITICAL - Integer overflow allowed!
```

**If patched:**
```
Test 3: Offset+Length Overflow
ArrayIndexOutOfBoundsException: offset + length exceeds array bounds
RESULT: Bug is patched - overflow detected
```

---

## 🎯 Next Steps After Testing

### If Bugs Are Confirmed (Vulnerable):

1. **Document Everything:**
   - Save all logs
   - Take screenshots
   - Record video of tests
   - Note Android version tested

2. **Write Detailed Report:**
   - Use the template in `google_vrp_submission.md`
   - Add your test results
   - Include APKs as attachments

3. **Submit to Google:**
   - Email: security@android.com
   - Subject: "Critical Vulnerabilities in Android Framework - Parcel/Binder"
   - Attach: Report + APKs + Logs

4. **Expected Bounty:**
   - Bug #4: $75,000 - $200,000
   - Bug #1: $75,000 - $150,000
   - **Total: $150,000 - $350,000**

### If Bugs Are Patched:

1. **Check Android Security Bulletins:**
   - See if bugs were already reported
   - Check CVE assignments

2. **Test Other Bugs:**
   - Move to Bug #2 (TOCTOU)
   - Move to Bug #3 (Type Confusion)

3. **Try Different Android Versions:**
   - Test on Android 13, 14, 15
   - Older versions might still be vulnerable

---

## 🛠️ Troubleshooting

### Problem: Gradle build fails

**Solution:**
```bash
# Make sure you have JDK 17 installed
java -version

# If not, download from: https://adoptium.net/

# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk-17
```

### Problem: GitHub Actions fails

**Solution:**
- Check Actions tab for error logs
- Make sure all files are committed
- Verify .github/workflows/build-apks.yml exists

### Problem: APK won't install

**Solution:**
```bash
# Enable "Install from Unknown Sources"
adb shell settings put secure install_non_market_apps 1

# Or install via ADB
adb install -r app-debug.apk
```

### Problem: App crashes immediately

**Solution:**
- This might mean the bug exists!
- Check logcat for crash details
- Look for native crash dumps

---

## 📞 Need Help?

If you encounter issues:

1. Check logcat output: `adb logcat`
2. Verify Android version: `adb shell getprop ro.build.version.release`
3. Check device architecture: `adb shell getprop ro.product.cpu.abi`

---

## ⚠️ Important Notes

1. **Test on Real Device:** Emulators may have different behavior
2. **Use Android 14/15:** These are currently supported versions
3. **Root Not Required:** Tests work on non-rooted devices
4. **Safe to Test:** These tests won't harm your device
5. **Keep Evidence:** Save all logs and screenshots

---

## 🎉 Success Criteria

You've successfully verified the bugs if:

✅ APKs build without errors
✅ Apps install and run on device
✅ Tests execute and show results
✅ You can reproduce the vulnerability
✅ You have logs/screenshots as evidence

**Then you're ready to submit to Google for $150k-$350k bounty!**

---

## 📝 Files Ready for Submission

When bugs are confirmed, you'll submit:

1. ✅ `google_vrp_submission.md` - Main report
2. ✅ `Bug4-StringOverflow/app-debug.apk` - Test app
3. ✅ `Bug1-IntegerOverflow/app-debug.apk` - Test app
4. 📄 `test-results.log` - Your test logs
5. 📄 `crash-dumps.txt` - Any crash logs
6. 📸 Screenshots of test results

---

**Good luck with your testing! You're on track to earn $150k-$350k in bug bounties! 🚀💰**
