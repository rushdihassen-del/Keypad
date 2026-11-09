package com.keypad.ime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ShiftTest {
    private Fakes.FakeEditor ed;
    private Fakes.FakeSettings st;
    private KeyboardController c;

    @Before
    public void setUp() {
        ed = new Fakes.FakeEditor();
        st = new Fakes.FakeSettings();
        c = new KeyboardController(ed, st);
    }

    private void letter(String s) {
        c.press(Fakes.byLabel(c, s), 0);
    }

    private void shift(long t) {
        c.press(Fakes.byType(c, KeyType.SHIFT), t);
    }

    @Test
    public void startsLowercase() {
        letter("a");
        assertEquals("a", ed.text.toString());
    }

    @Test
    public void shiftOnceCapitalisesOnlyTheNextLetter() {
        shift(10_000);
        assertEquals(ShiftState.ONCE, c.getShift());
        letter("a");
        letter("b");
        assertEquals("Ab", ed.text.toString());
        assertEquals(ShiftState.OFF, c.getShift());
    }

    @Test
    public void tappingShiftAgainSlowlyTurnsItOff() {
        shift(10_000);
        shift(12_000);
        assertEquals(ShiftState.OFF, c.getShift());
        letter("a");
        assertEquals("a", ed.text.toString());
    }

    @Test
    public void doubleTapIsCapsLock() {
        shift(10_000);
        shift(10_200);
        assertEquals(ShiftState.LOCK, c.getShift());
        letter("a");
        letter("b");
        letter("c");
        assertEquals("ABC", ed.text.toString());
        assertEquals(ShiftState.LOCK, c.getShift());
        shift(20_000);
        assertEquals(ShiftState.OFF, c.getShift());
        letter("d");
        assertEquals("ABCd", ed.text.toString());
    }

    @Test
    public void shiftDoesNotChangeSymbolsAndStaysUntilALetter() {
        shift(10_000);
        c.press(Fakes.byType(c, KeyType.SYMBOLS), 10_100);
        c.press(Fakes.byLabel(c, "@"), 10_200);
        assertEquals("@", ed.text.toString());
        assertEquals(ShiftState.ONCE, c.getShift());
    }

    @Test
    public void shiftCapitalisesPhonePadLettersOnceAndKeepsCaseWhileCycling() {
        st.layout = LayoutMode.PHONE;
        c = new KeyboardController(ed, st);
        shift(10_000);
        c.press(Fakes.byLabel(c, "2"), 10_100);
        c.press(Fakes.byLabel(c, "2"), 10_200);
        assertEquals("B", ed.composing);
        assertEquals(ShiftState.OFF, c.getShift());
        c.flushPending();
        c.press(Fakes.byLabel(c, "2"), 12_000);
        assertEquals("a", ed.composing);
    }

    @Test
    public void capsLockCapitalisesEveryPhonePadLetter() {
        st.layout = LayoutMode.PHONE;
        c = new KeyboardController(ed, st);
        shift(10_000);
        shift(10_200);
        c.press(Fakes.byLabel(c, "3"), 11_000);
        c.press(Fakes.byLabel(c, "4"), 11_100);
        c.flushPending();
        assertEquals("DG", ed.text.toString());
    }
}
