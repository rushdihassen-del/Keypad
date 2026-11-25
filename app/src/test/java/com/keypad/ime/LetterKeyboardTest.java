package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LetterKeyboardTest {
    private static String row(int i) {
        StringBuilder sb = new StringBuilder();
        for (Key k : Layouts.rows(Page.LETTERS).get(i)) {
            sb.append(k.type == KeyType.CHAR ? k.label : "|");
        }
        return sb.toString();
    }

    @Test
    public void lettersAreStandardQwertyWithNoNumberRowAndNoRightColumn() {
        List<List<Key>> rows = Layouts.rows(Page.LETTERS);
        assertEquals(4, rows.size());
        assertEquals("qwertyuiop", row(0));
        assertEquals("|asdfghjkl|", row(1));
        assertEquals("|zxcvbnm|", row(2));
        for (List<Key> r : rows) {
            for (Key k : r) {
                assertFalse("digit on letter page: " + k.label, k.label.matches("[0-9]"));
                assertFalse(k.label.equals("?") || k.label.equals("-") || k.label.equals(","));
            }
        }
    }

    @Test
    public void shiftAndBackspaceSitAtTheEndsOfTheBottomLetterRow() {
        List<Key> r3 = Layouts.rows(Page.LETTERS).get(2);
        assertEquals(KeyType.SHIFT, r3.get(0).type);
        assertEquals(KeyType.BACKSPACE, r3.get(r3.size() - 1).type);
    }

    @Test
    public void onlyTijfxbAreMarkerKeys() {
        List<String> anchors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (Key k : Layouts.rows(Page.LETTERS).get(i)) {
                if (k.anchor) {
                    anchors.add(k.label);
                }
            }
        }
        java.util.Collections.sort(anchors);
        assertEquals(Arrays.asList("b", "f", "i", "j", "t", "x"), anchors);
    }

    @Test
    public void bottomRowIsInTheRightOrderAndSizes() {
        List<Key> b = Layouts.rows(Page.LETTERS).get(3);
        KeyType[] order = {KeyType.SYMBOLS, KeyType.NUMBERS, KeyType.AT, KeyType.SPACE,
            KeyType.PERIOD, KeyType.ENTER};
        assertEquals(order.length, b.size());
        for (int i = 0; i < order.length; i++) {
            assertEquals(order[i], b.get(i).type);
        }
        assertEquals(1f, b.get(1).weight, 0f);
        assertEquals(1f, b.get(4).weight, 0f);
        assertEquals(3f, b.get(3).weight, 0f);
        assertEquals(2f, b.get(5).weight, 0f);
    }

    @Test
    public void lettersTypeLowercaseByDefaultAndBackspaceDeletes() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.plainField(ed, new Fakes.FakeSettings());
        Fakes.tap(c, "q");
        Fakes.tap(c, "w");
        assertEquals("qw", ed.visible());
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.BACKSPACE));
        assertEquals("q", ed.visible());
    }

    @Test
    public void atTypesAtAndFullStopTypesFullStopOnTap() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.plainField(ed, new Fakes.FakeSettings());
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.AT));
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.PERIOD));
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SPACE));
        assertEquals("@. ", ed.visible());
        assertEquals(0, ed.voice);
    }

    @Test
    public void removedPartsAreGone() {
        for (KeyType t : KeyType.values()) {
            String n = t.name();
            assertFalse(n.equals("SEND") || n.equals("COLOUR") || n.equals("LAYOUT") || n.equals("MULTITAP"));
        }
        assertEquals(3, Page.values().length);
    }
}
