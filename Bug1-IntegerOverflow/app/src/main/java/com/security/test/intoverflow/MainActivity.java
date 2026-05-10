package com.security.test.intoverflow;

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
 * Test application for Bug #1: Integer Overflow in Parcel Array Operations
 *
 * This app tests for integer overflow vulnerabilities in android_os_Parcel.cpp
 * when handling array operations with crafted offset and length values.
 *
 * Vulnerable code locations:
 * - writeByteArray (lines 198-223): memcpy(dest, ar + offset, length)
 * - readByteArray (lines 394-419): len <= (int32_t)parcel->dataAvail()
 *
 * Expected behavior if bug exists:
 * - Out-of-bounds memory read/write
 * - Memory corruption
 * - Potential crash or information disclosure
 *
 * Expected behavior if patched:
 * - ArrayIndexOutOfBoundsException
 * - Proper bounds checking
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
        title.setText("Bug #1: Integer Overflow Test");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        // Test buttons
        addTestButton(layout, "Test 1: Offset Overflow", () -> testOffsetOverflow());
        addTestButton(layout, "Test 2: Length Overflow", () -> testLengthOverflow());
        addTestButton(layout, "Test 3: Offset+Length Overflow", () -> testOffsetPlusLengthOverflow());
        addTestButton(layout, "Test 4: Negative Values", () -> testNegativeValues());
        addTestButton(layout, "Test 5: Read Overflow", () -> testReadOverflow());
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

        log("=== Integer Overflow Vulnerability Test ===");
        log("Target: frameworks/base/core/jni/android_os_Parcel.cpp");
        log("Lines: 198-223 (writeByteArray), 394-419 (readByteArray)");
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
     * Test 1: Offset overflow
     * Tests if offset validation is missing
     */
    private void testOffsetOverflow() {
        log("Testing offset overflow...");
        log("Creating small array with large offset");

        try {
            byte[] smallArray = new byte[100];
            for (int i = 0; i < smallArray.length; i++) {
                smallArray[i] = (byte) i;
            }

            Parcel parcel = Parcel.obtain();

            // Try to write with offset that exceeds array bounds
            int maliciousOffset = Integer.MAX_VALUE - 50;
            int length = 100;

            log("Array size: " + smallArray.length);
            log("Offset: " + maliciousOffset + " (0x" + Integer.toHexString(maliciousOffset) + ")");
            log("Length: " + length);
            log("Offset + Length: " + ((long)maliciousOffset + length) + " (overflows to negative)");

            log("Attempting writeByteArray...");
            parcel.writeByteArray(smallArray, maliciousOffset, length);

            log("BUG CONFIRMED: No exception thrown!");
            log("RESULT: Out-of-bounds read occurred!");
            log("This is a CRITICAL vulnerability!");

            parcel.recycle();

        } catch (ArrayIndexOutOfBoundsException e) {
            log("ArrayIndexOutOfBoundsException: " + e.getMessage());
            log("RESULT: Bug is patched - proper bounds checking");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Some protection exists");
        }
    }

    /**
     * Test 2: Length overflow
     * Tests if length validation is missing
     */
    private void testLengthOverflow() {
        log("Testing length overflow...");

        try {
            byte[] array = new byte[1000];

            Parcel parcel = Parcel.obtain();

            // Try to write with length that exceeds array bounds
            int offset = 0;
            int maliciousLength = Integer.MAX_VALUE;

            log("Array size: " + array.length);
            log("Offset: " + offset);
            log("Length: " + maliciousLength + " (0x" + Integer.toHexString(maliciousLength) + ")");

            log("Attempting writeByteArray...");
            parcel.writeByteArray(array, offset, maliciousLength);

            log("BUG CONFIRMED: No exception thrown!");
            log("RESULT: Out-of-bounds read occurred!");

            parcel.recycle();

        } catch (ArrayIndexOutOfBoundsException e) {
            log("ArrayIndexOutOfBoundsException: " + e.getMessage());
            log("RESULT: Bug is patched");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Some protection exists");
        }
    }

    /**
     * Test 3: Offset + Length overflow
     * Tests the critical case where offset + length overflows
     */
    private void testOffsetPlusLengthOverflow() {
        log("Testing offset + length overflow...");
        log("This is the most dangerous case!");

        try {
            byte[] array = new byte[1000];

            Parcel parcel = Parcel.obtain();

            // Craft values where offset + length overflows to a small positive number
            int offset = Integer.MAX_VALUE - 500;
            int length = 600;
            long sum = (long)offset + length;

            log("Array size: " + array.length);
            log("Offset: " + offset);
            log("Length: " + length);
            log("Offset + Length (as long): " + sum);
            log("Offset + Length (as int): " + (offset + length) + " (OVERFLOWS!)");

            if (sum > Integer.MAX_VALUE) {
                log("Integer overflow detected in calculation!");
            }

            log("Attempting writeByteArray...");
            parcel.writeByteArray(array, offset, length);

            log("BUG CONFIRMED: No exception thrown!");
            log("RESULT: CRITICAL - Integer overflow allowed!");
            log("This can lead to out-of-bounds memory access!");

            parcel.recycle();

        } catch (ArrayIndexOutOfBoundsException e) {
            log("ArrayIndexOutOfBoundsException: " + e.getMessage());
            log("RESULT: Bug is patched - overflow detected");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Some protection exists");
        }
    }

    /**
     * Test 4: Negative values
     * Tests if negative offset/length are properly rejected
     */
    private void testNegativeValues() {
        log("Testing negative offset and length...");

        try {
            byte[] array = new byte[100];
            Parcel parcel = Parcel.obtain();

            // Test negative offset
            log("Test 4a: Negative offset");
            try {
                parcel.writeByteArray(array, -100, 50);
                log("BUG: Negative offset accepted!");
            } catch (Exception e) {
                log("Good: Negative offset rejected - " + e.getClass().getSimpleName());
            }

            // Test negative length
            log("Test 4b: Negative length");
            try {
                parcel.writeByteArray(array, 0, -50);
                log("BUG: Negative length accepted!");
            } catch (Exception e) {
                log("Good: Negative length rejected - " + e.getClass().getSimpleName());
            }

            // Test both negative
            log("Test 4c: Both negative");
            try {
                parcel.writeByteArray(array, -10, -20);
                log("BUG: Both negative values accepted!");
            } catch (Exception e) {
                log("Good: Negative values rejected - " + e.getClass().getSimpleName());
            }

            parcel.recycle();
            log("RESULT: Negative value tests completed");

        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Test 5: Read overflow
     * Tests readByteArray for integer overflow in length check
     */
    private void testReadOverflow() {
        log("Testing readByteArray overflow...");
        log("Target: len <= (int32_t)parcel->dataAvail()");

        try {
            Parcel parcel = Parcel.obtain();

            // Write a normal array first
            byte[] writeArray = new byte[100];
            for (int i = 0; i < writeArray.length; i++) {
                writeArray[i] = (byte) i;
            }
            parcel.writeByteArray(writeArray);

            // Reset position to read
            parcel.setDataPosition(0);

            // Try to read with a destination array that's too small
            byte[] readArray = new byte[50];
            log("Wrote: " + writeArray.length + " bytes");
            log("Reading into: " + readArray.length + " byte array");

            try {
                parcel.readByteArray(readArray);
                log("BUG: readByteArray succeeded despite size mismatch!");
                log("RESULT: Potential buffer overflow!");
            } catch (Exception e) {
                log("Good: readByteArray threw exception: " + e.getClass().getSimpleName());
            }

            // Now test with crafted length value
            parcel.setDataPosition(0);

            // Manually write a large length value
            Parcel maliciousParcel = Parcel.obtain();
            maliciousParcel.writeInt(Integer.MAX_VALUE);
            maliciousParcel.writeByteArray(new byte[100]);

            maliciousParcel.setDataPosition(0);
            byte[] destArray = new byte[10];

            log("Testing with MAX_VALUE length...");
            try {
                maliciousParcel.readByteArray(destArray);
                log("BUG: Accepted MAX_VALUE length without exception!");
            } catch (Exception e) {
                log("Good: Rejected MAX_VALUE length - threw " + e.getClass().getSimpleName());
            }

            parcel.recycle();
            maliciousParcel.recycle();

        } catch (OutOfMemoryError e) {
            log("OutOfMemoryError: " + e.getMessage());
            log("RESULT: OOM during test");
        } catch (Exception e) {
            log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            log("RESULT: Exception thrown");
        }
    }
}
