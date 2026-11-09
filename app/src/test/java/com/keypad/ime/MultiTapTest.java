package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MultiTapTest {
    private Fakes.FakeEditor ed;
    private KeyboardController c;

    @Before
    public void setUp() {
        ed = new Fakes.FakeEditor();
        Fakes.FakeSettings st = new Fakes.FakeSettings();
        st.layout = LayoutMode.PHONE;
        c = new KeyboardController(ed, st);
    }

    private void tap(String digit, long t) {
        c.press(Fakes.byLabel(c, digit), t);
    }

    @Test
    public void onceGivesFirstLetter() {
        tap("2", 0);
        assertEquals("a", ed.visible());
    }

    @Test
    public void twiceGivesB() {
        tap("2", 0);
        tap("2", 200);
        assertEquals("b", ed.composing);
        c.flushPending();
        assertEquals("b", ed.text.toString());
    }

    @Test
    public void threeTimesGivesC() {
        tap("2", 0);
        tap("2", 200);
        tap("2", 400);
        assertEquals("c", ed.visible());
    }

    @Test
    public void fourthTapWrapsBackToA() {
        for (int i = 0; i < 4; i++) {
            tap("2", i * 200L);
        }
        assertEquals("a", ed.visible());
    }

    @Test
    public void fourLetterKeysCycleThroughAllFour() {
        for (int i = 0; i < 4; i++) {
            tap("7", i * 100L);
        }
        assertEquals("s", ed.visible());
        tap("7", 500);
        assertEquals("p", ed.visible());
        c.flushPending();
        ed.text.setLength(0);
        for (int i = 0; i < 4; i++) {
            tap("9", 10_000 + i * 100L);
        }
        assertEquals("z", ed.visible());
    }

    @Test
    public void differentKeyAcceptsThePreviousLetter() {
        tap("2", 0);
        tap("2", 100);
        tap("3", 200);
        assertEquals("b", ed.text.toString());
        assertEquals("d", ed.composing);
    }

    @Test
    public void aPauseAcceptsTheLetterAndStartsANewOne() {
        tap("2", 0);
        tap("2", KeyboardController.MULTITAP_TIMEOUT_MS + 100);
        assertEquals("a", ed.text.toString());
        assertEquals("a", ed.composing);
    }

    @Test
    public void timeoutFlushCommitsTheLetter() {
        tap("5", 0);
        assertTrue(c.hasPending());
        c.flushPending();
        assertFalse(c.hasPending());
        assertEquals("j", ed.text.toString());
        assertEquals("", ed.composing);
    }

    @Test
    public void spaceAcceptsThePendingLetterFirst() {
        tap("2", 0);
        c.press(Fakes.byType(c, KeyType.SPACE), 100);
        assertEquals("a ", ed.text.toString());
    }

    @Test
    public void backspaceCancelsOnlyThePendingLetter() {
        tap("3", 0);
        c.flushPending();
        tap("2", 2000);
        c.press(Fakes.byType(c, KeyType.BACKSPACE), 2100);
        assertEquals("d", ed.visible());
        c.press(Fakes.byType(c, KeyType.BACKSPACE), 2200);
        assertEquals("", ed.visible());
    }

    @Test
    public void keyOneCyclesPunctuation() {
        tap("1", 0);
        assertEquals(".", ed.visible());
        tap("1", 100);
        assertEquals(",", ed.visible());
        tap("1", 200);
        assertEquals("?", ed.visible());
        tap("1", 300);
        assertEquals("!", ed.visible());
    }

    @Test
    public void everyKeyShowsItsNumberAndLetters() {
        String[][] expected = {
            {"2", "ABC"}, {"3", "DEF"}, {"4", "GHI"}, {"5", "JKL"},
            {"6", "MNO"}, {"7", "PQRS"}, {"8", "TUV"}, {"9", "WXYZ"},
        };
        for (String[] e : expected) {
            Key k = Fakes.byLabel(c, e[0]);
            assertEquals(KeyType.MULTITAP, k.type);
            assertEquals(e[1], k.sub);
        }
    }

    @Test
    public void phonePadHasShiftSymbolsAndNumbersKeys() {
        assertTrue(Fakes.hasType(LayoutMode.PHONE, Page.LETTERS, KeyType.SHIFT));
        assertTrue(Fakes.hasType(LayoutMode.PHONE, Page.LETTERS, KeyType.SYMBOLS));
        assertTrue(Fakes.hasType(LayoutMode.PHONE, Page.LETTERS, KeyType.NUMBERS));
    }
}
