package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class PagesTest {
    private final Fakes.FakeEditor ed = new Fakes.FakeEditor();
    private final Fakes.FakeSettings settings = new Fakes.FakeSettings();
    private final KeyboardController c = Fakes.plainField(ed, settings);

    private static boolean has(Page page, String label) {
        for (List<Key> row : Layouts.rows(page)) {
            for (Key k : row) {
                if (k.label.equals(label)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void specialKeyOpensSymbolsAndTheSameKeyGoesBack() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SYMBOLS));
        assertEquals(Page.SYMBOLS, c.getPage());
        Fakes.tap(c, Fakes.byType(Page.SYMBOLS, KeyType.SYMBOLS));
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void numberKeyOpensPadAndTheSameKeyGoesBack() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.NUMBERS));
        assertEquals(Page.NUMBERS, c.getPage());
        Fakes.tap(c, Fakes.byType(Page.NUMBERS, KeyType.NUMBERS));
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void numberPadReachableFromSymbolsToo() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SYMBOLS));
        Fakes.tap(c, Fakes.byType(Page.SYMBOLS, KeyType.NUMBERS));
        assertEquals(Page.NUMBERS, c.getPage());
    }

    @Test
    public void symbolsPageHasTheRequiredCharactersAndNoCommaOrFullStopKeysAbove() {
        String[] needed = {"/", "\\", "+", "!", ";", "?", "-", ":", "_", "#", "&", "(", ")", "\"", "=", "'"};
        for (String s : needed) {
            assertTrue("missing " + s, has(Page.SYMBOLS, s));
        }
        assertTrue(has(Page.SYMBOLS, "@"));
        List<List<Key>> rows = Layouts.rows(Page.SYMBOLS);
        for (int i = 0; i < rows.size() - 1; i++) {
            for (Key k : rows.get(i)) {
                assertFalse(k.label.equals(".") || k.label.equals(","));
            }
        }
        int total = 0;
        for (List<Key> r : rows) {
            total += r.size();
        }
        assertTrue("keys must stay few", total <= 30);
    }

    @Test
    public void symbolKeysTypeTheirCharacter() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SYMBOLS));
        Fakes.tap(c, "/");
        Fakes.tap(c, "\\");
        Fakes.tap(c, "+");
        Fakes.tap(c, "!");
        Fakes.tap(c, ";");
        Fakes.tap(c, "?");
        Fakes.tap(c, "-");
        assertEquals("/\\+!;?-", ed.visible());
    }

    @Test
    public void bottomRowIsInTheSameSpotsOnLettersAndSymbols() {
        List<Key> a = Layouts.rows(Page.LETTERS).get(3);
        List<Key> b = Layouts.rows(Page.SYMBOLS).get(3);
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).type, b.get(i).type);
            assertEquals(a.get(i).weight, b.get(i).weight, 0f);
        }
    }

    @Test
    public void everyPageHasBackspaceAndEnterAndLettersAndSymbolsHaveSpace() {
        for (Page p : Page.values()) {
            assertEquals(1, Fakes.count(p, KeyType.BACKSPACE));
            assertEquals(1, Fakes.count(p, KeyType.ENTER));
        }
        assertEquals(1, Fakes.count(Page.LETTERS, KeyType.SPACE));
        assertEquals(1, Fakes.count(Page.SYMBOLS, KeyType.SPACE));
    }

    @Test
    public void pageSwitchesTypeNothing() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SYMBOLS));
        Fakes.tap(c, Fakes.byType(Page.SYMBOLS, KeyType.NUMBERS));
        Fakes.tap(c, Fakes.byType(Page.NUMBERS, KeyType.NUMBERS));
        assertEquals("", ed.visible());
    }
}
