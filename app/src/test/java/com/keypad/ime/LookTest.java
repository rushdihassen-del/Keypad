package com.keypad.ime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class LookTest {
    private static final float EPS = 0.0001f;

    private static Key letter(Page page, String label) {
        return Fakes.byLabel(page, label);
    }

    @Test
    public void landmarkKeysAreBlackWithWhiteLettersOnTheYellowScheme() {
        for (String s : new String[] {"t", "i", "j", "f", "x", "b"}) {
            int[] c = Look.colours(letter(Page.LETTERS, s), true);
            assertEquals(s, Look.BLACK, c[0]);
            assertEquals(s, Look.WHITE, c[1]);
            assertEquals("outline keeps the key clear of the black page: " + s, Look.WHITE, c[2]);
        }
    }

    @Test
    public void landmarkKeysStayLightOrangeOnTheBlackScheme() {
        for (String s : new String[] {"t", "i", "j", "f", "x", "b"}) {
            int[] c = Look.colours(letter(Page.LETTERS, s), false);
            assertArrayEquals(s, new int[] {Look.LIGHT_ORANGE, Look.BLACK, Look.LIGHT_ORANGE}, c);
        }
    }

    @Test
    public void onlyTheSixLandmarkKeysAreLandmarks() {
        int n = 0;
        for (List<Key> row : Layouts.rows(Page.LETTERS)) {
            for (Key k : row) {
                if (k.anchor) {
                    n++;
                }
            }
        }
        assertEquals(6, n);
    }

    @Test
    public void backspaceAndEnterAreTheSameInBothSchemes() {
        for (boolean yellow : new boolean[] {true, false}) {
            assertArrayEquals(new int[] {Look.BLUE, Look.BLACK, Look.BLUE},
                    Look.colours(Fakes.byType(Page.LETTERS, KeyType.BACKSPACE), yellow));
            assertArrayEquals(new int[] {Look.ORANGE, Look.BLACK, Look.ORANGE},
                    Look.colours(Fakes.byType(Page.LETTERS, KeyType.ENTER), yellow));
        }
    }

    @Test
    public void ordinaryKeysKeepTheirColours() {
        Key a = letter(Page.LETTERS, "a");
        assertArrayEquals(new int[] {Look.YELLOW, Look.BLACK, Look.YELLOW}, Look.colours(a, true));
        assertArrayEquals(new int[] {Look.BLACK, Look.YELLOW, Look.YELLOW}, Look.colours(a, false));
    }

    @Test
    public void letterAndSymbolKeysAreAThirdShorterInBothOrientations() {
        for (boolean landscape : new boolean[] {false, true}) {
            float old = landscape ? 0.78f : 0.64f;
            assertEquals(old * 2f / 3f, Look.pageHeightShare(Page.LETTERS, landscape), EPS);
            assertEquals(Look.pageHeightShare(Page.LETTERS, landscape),
                    Look.pageHeightShare(Page.SYMBOLS, landscape), EPS);
        }
    }

    @Test
    public void lettersAndSymbolsHaveTheSameRowCountSoTheBottomRowStaysPut() {
        assertEquals(4, Layouts.rows(Page.LETTERS).size());
        assertEquals(Layouts.rows(Page.LETTERS).size(), Layouts.rows(Page.SYMBOLS).size());
        List<Key> a = Layouts.rows(Page.LETTERS).get(3);
        List<Key> b = Layouts.rows(Page.SYMBOLS).get(3);
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).weight, b.get(i).weight, EPS);
        }
    }

    @Test
    public void shorterKeysKeepTheirWidths() {
        // Widths come from the unchanged row weights: ten units across the letter rows.
        List<List<Key>> rows = Layouts.rows(Page.LETTERS);
        float top = 0;
        for (Key k : rows.get(0)) {
            top += k.weight;
        }
        assertEquals(10f, top, EPS);
        float bottom = 0;
        for (Key k : rows.get(3)) {
            bottom += k.weight;
        }
        assertEquals(9f, bottom, EPS);
    }

    @Test
    public void numberPadHeightIsUnchanged() {
        assertEquals(0.64f, Look.pageHeightShare(Page.NUMBERS, false), EPS);
        assertEquals(0.78f, Look.pageHeightShare(Page.NUMBERS, true), EPS);
        assertTrue(Look.pageHeightShare(Page.NUMBERS, false)
                > Look.pageHeightShare(Page.LETTERS, false));
    }

    @Test
    public void numberPadLayoutIsUnchanged() {
        StringBuilder sig = new StringBuilder();
        for (List<Key> row : Layouts.rows(Page.NUMBERS)) {
            for (Key k : row) {
                sig.append(k.type).append(':').append(k.label).append(':').append(k.weight).append(' ');
            }
            sig.append('/');
        }
        assertEquals(
                "CHAR:1:1.0 CHAR:2:1.0 CHAR:3:1.0 SPACER::0.3 CHAR:+:1.0 BACKSPACE::1.0 /"
                        + "CHAR:4:1.0 CHAR:5:1.0 CHAR:6:1.0 SPACER::0.3 CHAR:-:1.0 CHAR:*:1.0 /"
                        + "CHAR:7:1.0 CHAR:8:1.0 CHAR:9:1.0 SPACER::0.3 CHAR:#:1.0 CHAR:,:1.0 /"
                        + "NUMBERS:ABC:1.0 CHAR:0:1.0 SPACER::1.0 SPACER::0.3 CHAR:.:1.0 ENTER:Enter:1.0 /",
                sig.toString());
    }

    @Test
    public void numberPadColoursAreUnchanged() {
        // Black keys with yellow numbers when the letters are yellow (pad scheme is the opposite).
        Key five = Fakes.byLabel(Page.NUMBERS, "5");
        assertArrayEquals(new int[] {Look.BLACK, Look.YELLOW, Look.YELLOW}, Look.colours(five, false));
        assertArrayEquals(new int[] {Look.YELLOW, Look.BLACK, Look.YELLOW}, Look.colours(five, true));
        assertNotEquals(Look.colours(five, true)[0], Look.colours(five, false)[0]);
    }
}
