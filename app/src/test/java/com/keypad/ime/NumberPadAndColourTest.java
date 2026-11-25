package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class NumberPadAndColourTest {
    private static final float EPS = 0.001f;

    /** Left edge of the key as a share of the row width. */
    private static float left(List<Key> row, Key key) {
        float total = 0;
        float x = -1;
        for (Key k : row) {
            if (k == key) {
                x = total;
            }
            total += k.weight;
        }
        return x / total;
    }

    private static final List<List<Key>> ROWS = Layouts.rows(Page.NUMBERS);

    private static List<Key> rowWith(String label) {
        for (List<Key> row : ROWS) {
            for (Key k : row) {
                if (k.label.equals(label) && k.type == KeyType.CHAR) {
                    return row;
                }
            }
        }
        throw new AssertionError(label);
    }

    private static Key key(String label) {
        for (Key k : rowWith(label)) {
            if (k.label.equals(label)) {
                return k;
            }
        }
        throw new AssertionError(label);
    }

    @Test
    public void digitsSitInThreeRowsOfThreeAndZeroIsUnderTheEight() {
        List<List<Key>> rows = ROWS;
        assertEquals(4, rows.size());
        assertEquals(rows.get(0), rowWith("1"));
        assertEquals(rows.get(0), rowWith("2"));
        assertEquals(rows.get(0), rowWith("3"));
        assertEquals(rows.get(1), rowWith("4"));
        assertEquals(rows.get(1), rowWith("6"));
        assertEquals(rows.get(2), rowWith("7"));
        assertEquals(rows.get(2), rowWith("9"));
        assertEquals(rows.get(3), rowWith("0"));
        assertEquals(left(rowWith("8"), key("8")), left(rowWith("0"), key("0")), EPS);
        assertEquals(left(rowWith("2"), key("2")), left(rowWith("5"), key("5")), EPS);
        assertEquals(left(rowWith("1"), key("1")), left(rowWith("4"), key("4")), EPS);
        assertEquals(left(rowWith("3"), key("3")), left(rowWith("9"), key("9")), EPS);
        assertTrue(left(rowWith("1"), key("1")) < left(rowWith("2"), key("2")));
        assertTrue(left(rowWith("2"), key("2")) < left(rowWith("3"), key("3")));
    }

    @Test
    public void extrasAreAllThereAndSitRightOfTheNumbers() {
        float rightOfDigits = left(rowWith("3"), key("3")) + 1f / 5.3f;
        for (String s : new String[] {".", ",", "+", "-", "*", "#"}) {
            assertTrue(s + " missing", rowWith(s) != null);
            assertTrue(s + " must be clear of the numbers",
                    left(rowWith(s), key(s)) >= rightOfDigits - EPS);
        }
        assertEquals(1, Fakes.count(Page.NUMBERS, KeyType.BACKSPACE));
        assertEquals(1, Fakes.count(Page.NUMBERS, KeyType.ENTER));
        assertEquals(1, Fakes.count(Page.NUMBERS, KeyType.NUMBERS));
    }

    @Test
    public void numberKeysTypeDigits() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.controller(ed, new Fakes.FakeSettings());
        c.startField(true, false);
        for (String s : new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"}) {
            Fakes.tap(c, s);
        }
        assertEquals("1234567890", ed.visible());
    }

    @Test
    public void numberAndPhoneBoxesOpenOnTheNumberPad() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.controller(ed, new Fakes.FakeSettings());
        c.startField(true, false);
        assertEquals(Page.NUMBERS, c.getPage());
        c.startField(false, true);
        assertEquals(Page.LETTERS, c.getPage());
        assertTrue(FieldRules.prefersNumberPad(android.text.InputType.TYPE_CLASS_NUMBER));
        assertTrue(FieldRules.prefersNumberPad(android.text.InputType.TYPE_CLASS_PHONE));
        assertTrue(!FieldRules.prefersNumberPad(android.text.InputType.TYPE_CLASS_TEXT));
    }

    @Test
    public void numberPadIsTheOppositeColourToTheLetters() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.plainField(ed, new Fakes.FakeSettings());
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getPageScheme());
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SYMBOLS));
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getPageScheme());
        Fakes.tap(c, Fakes.byType(Page.SYMBOLS, KeyType.NUMBERS));
        assertEquals(Scheme.BLACK_ON_YELLOW, c.getPageScheme());
    }

    @Test
    public void holdingShiftSwapsEveryPageTogetherAndTheyStayOpposite() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.plainField(ed, new Fakes.FakeSettings());
        Fakes.hold(c, Fakes.byType(Page.LETTERS, KeyType.SHIFT));
        assertEquals(Scheme.BLACK_ON_YELLOW, c.getPageScheme());
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.NUMBERS));
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getPageScheme());
        assertNotEquals(c.getScheme(), c.getPageScheme());
    }

    @Test
    public void colourChoiceIsRememberedAndIsTheOnlyThingSaved() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        Fakes.FakeSettings settings = new Fakes.FakeSettings();
        KeyboardController c = Fakes.plainField(ed, settings);
        Fakes.typeText(c, "hello world");
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.NUMBERS));
        assertEquals(0, settings.writes);
        Fakes.tap(c, Fakes.byType(Page.NUMBERS, KeyType.NUMBERS));
        Fakes.hold(c, Fakes.byType(Page.LETTERS, KeyType.SHIFT));
        assertEquals(1, settings.writes);
        assertEquals(Scheme.BLACK_ON_YELLOW, settings.scheme);
        KeyboardController again = Fakes.controller(new Fakes.FakeEditor(), settings);
        assertEquals(Scheme.BLACK_ON_YELLOW, again.getScheme());
    }
}
