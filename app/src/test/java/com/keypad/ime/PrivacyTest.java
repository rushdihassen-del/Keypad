package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Guards the privacy promises: no permissions, no logging, no network, nothing typed stored. */
public class PrivacyTest {
    private static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static List<Path> mainJava() throws IOException {
        try (Stream<Path> s = Files.walk(Paths.get("src/main/java"))) {
            return s.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    @Test
    public void manifestRequestsNoPermissionsAtAll() throws IOException {
        String manifest = read(Paths.get("src/main/AndroidManifest.xml"));
        assertFalse(manifest.contains("uses-permission"));
        assertFalse(manifest.contains("INTERNET"));
        assertFalse(manifest.contains("RECORD_AUDIO"));
        assertTrue(manifest.contains("android:allowBackup=\"false\""));
    }

    @Test
    public void noLoggingNetworkOrFileWritingInTheApp() throws IOException {
        String[] banned = {
            "android.util.Log", "System.out", "System.err", "printStackTrace",
            "java.net", "javax.net", "HttpURLConnection", "Socket", "WebView", "okhttp",
            "FileOutputStream", "FileWriter", "openFileOutput", "getExternalFilesDir",
            "ClipboardManager", "SQLite", "getSelectedText", "getTextAfterCursor",
            "getExtractedText",
        };
        List<Path> files = mainJava();
        assertTrue(files.size() >= 8);
        for (Path p : files) {
            String src = read(p);
            for (String b : banned) {
                assertFalse(p.getFileName() + " must not use " + b, src.contains(b));
            }
        }
    }

    @Test
    public void onlyTheServiceReadsFieldTextAndOnlyTheWordJustTyped() throws IOException {
        for (Path p : mainJava()) {
            String src = read(p);
            if (p.getFileName().toString().equals("KeypadService.java")) {
                assertEquals(1, src.split("getTextBeforeCursor", -1).length - 1);
                assertTrue(src.contains("getTextBeforeCursor(count, 0)"));
            } else {
                assertFalse(p.getFileName() + " must not read field text", src.contains("getTextBeforeCursor"));
            }
        }
    }

    @Test
    public void voiceTypingIsOnlyAHandOverToTheOtherKeyboard() throws IOException {
        String service = read(Paths.get("src/main/java/com/keypad/ime/KeypadService.java"));
        for (String b : new String[] {"RecognizerIntent", "SpeechRecognizer", "AudioRecord",
            "MediaRecorder", "AudioManager", "startActivity"}) {
            assertFalse("voice hand-off must not use " + b, service.contains(b));
        }
        assertTrue(service.contains("\"voice\".equals(st.getMode())"));
    }

    @Test
    public void onlyTheSettingsClassWritesToStorageAndOnlyTheColourChoice() throws IOException {
        List<String> writers = new ArrayList<>();
        for (Path p : mainJava()) {
            String src = read(p);
            if (src.contains("SharedPreferences") || src.contains(".edit()")) {
                writers.add(p.getFileName().toString());
            }
        }
        assertEquals(1, writers.size());
        assertEquals("PrefsSettings.java", writers.get(0));
        String prefs = read(Paths.get("src/main/java/com/keypad/ime/PrefsSettings.java"));
        assertEquals(1, prefs.split("putString", -1).length - 1);
        assertEquals(0, prefs.split("putBoolean|putInt|putLong|putStringSet", -1).length - 1);
    }

    @Test
    public void autocorrectListIsAFixedFileInsideTheApp() throws IOException {
        assertTrue(Files.exists(Paths.get("src/main/res/raw/words.txt")));
        String ac = read(Paths.get("src/main/java/com/keypad/ime/Autocorrect.java"));
        assertFalse(ac.contains("android."));
        assertFalse(ac.contains("rank.put(typed") || ac.contains("learn"));
        // The list is only ever filled once, in the constructor.
        assertEquals(1, ac.split("rank\\.put\\(", -1).length - 1);
    }

    @Test
    public void appHasNoOutsideLibraries() throws IOException {
        String gradle = read(Paths.get("build.gradle"));
        for (String line : gradle.split("\n")) {
            String t = line.trim();
            assertFalse("Unexpected dependency: " + t,
                    t.startsWith("implementation") || t.startsWith("api ")
                            || t.startsWith("compileOnly") || t.startsWith("runtimeOnly")
                            || t.startsWith("kapt") || t.startsWith("annotationProcessor"));
        }
        assertTrue(gradle.contains("testImplementation 'junit:junit"));
    }
}
