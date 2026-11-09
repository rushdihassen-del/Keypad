package com.keypad.ime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class NumberPadTest {
    private static int[] find(List<List<Key>> rows, String label) {
        for (int r = 0; r < rows.size(); r++) {
            for (int col = 0; col < rows.get(r).size(); col++) {
                Key k = rows.get(r).get(col);
                if (k.type == KeyType.CHAR && k.label.equals(label)) {
                    return new int[] {r, col};
                }
            }
        }
        throw new AssertionError("Missing " + label);
    }

    private void checkPositions(LayoutMode layout) {
        List<List<Key>> rows = Layouts.rows(layout, Page.NUMBERS);
        String[][] grid = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}};
        for (int r = 0; r < 3; r++) {
            for (int col = 0; col < 3; col++) {
                int[] pos = find(rows, grid[r][col]);
                assertEquals(grid[r][col] + " row", r, pos[0]);
                assertEquals(grid[r][col] + " column", col, pos[1]);
            }
        }
        int[] zero = find(rows, "0");
        int[] eight = find(rows, "8");
        assertEquals("0 is on the row under 8", eight[0] + 1, zero[0]);
        assertEquals("0 is directly below 8", eight[1], zero[1]);
    }

    @Test
    public void numbersAreInPhoneOrderWithZeroUnderEight() {
        checkPositions(LayoutMode.STANDARD);
    }

    @Test
    public void numbersAreInTheSameSpotsWhateverLayoutIsChosen() {
        checkPositions(LayoutMode.PHONE);
    }

    @Test
    public void everyKeyOnTheNumberPadHasEqualWidth() {
        for (int r = 0; r < 4; r++) {
            for (Key k : Layouts.rows(LayoutMode.STANDARD, Page.NUMBERS).get(r)) {
                assertEquals(1f, k.weight, 0f);
            }
        }
    }

    @Test
    public void numberKeysTypeTheirDigits() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        Fakes.FakeSettings st = new Fakes.FakeSettings();
        KeyboardController c = new KeyboardController(ed, st);
        c.press(Fakes.byType(c, KeyType.SYMBOLS), 0);
        c.press(Fakes.byType(c, KeyType.NUMBERS), 0);
        String digits = "1234567890";
        for (int i = 0; i < digits.length(); i++) {
            c.press(Fakes.byLabel(c, String.valueOf(digits.charAt(i))), 0);
        }
        assertEquals(digits, ed.text.toString());
    }

    @Test
    public void numberPadHasBackspaceAndAWayBackToLetters() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = new KeyboardController(ed, new Fakes.FakeSettings());
        c.startField(true);
        c.press(Fakes.byLabel(c, "5"), 0);
        c.press(Fakes.byType(c, KeyType.BACKSPACE), 0);
        assertEquals("", ed.text.toString());
        c.press(Fakes.byType(c, KeyType.LETTERS), 0);
        assertEquals(Page.LETTERS, c.getPage());
    }
}
