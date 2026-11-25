package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HoldAndShiftTest {
    private final Fakes.FakeEditor ed = new Fakes.FakeEditor();
    private final Fakes.FakeSettings settings = new Fakes.FakeSettings();
    private final KeyboardController c = Fakes.plainField(ed, settings);

    private Key shift() {
        return Fakes.byType(Page.LETTERS, KeyType.SHIFT);
    }

    @Test
    public void oneTapGivesOneCapital() {
        Fakes.tap(c, shift());
        assertEquals(ShiftState.ONCE, c.getShift());
        Fakes.tap(c, "a");
        Fakes.tap(c, "b");
        assertEquals("Ab", ed.visible());
        assertEquals(ShiftState.OFF, c.getShift());
    }

    @Test
    public void quickDoubleTapIsCapsLockAndAnotherTapTurnsItOff() {
        c.press(shift(), 1000);
        c.press(shift(), 1200);
        assertEquals(ShiftState.LOCK, c.getShift());
        Fakes.tap(c, "a");
        Fakes.tap(c, "b");
        assertEquals("AB", ed.visible());
        Fakes.tap(c, shift());
        assertEquals(ShiftState.OFF, c.getShift());
    }

    @Test
    public void slowSecondTapJustTurnsShiftOff() {
        c.press(shift(), 1000);
        c.press(shift(), 1800);
        assertEquals(ShiftState.OFF, c.getShift());
    }

    @Test
    public void holdingShiftSwapsColoursAndDoesNotTouchCaps() {
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getScheme());
        Fakes.hold(c, shift());
        assertEquals(Scheme.BLACK_ON_YELLOW, c.getScheme());
        assertEquals(ShiftState.OFF, c.getShift());
        Fakes.hold(c, shift());
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getScheme());
        assertEquals(ShiftState.OFF, c.getShift());
    }

    @Test
    public void holdingShiftWhileCapsLockedKeepsCapsLock() {
        c.press(shift(), 1000);
        c.press(shift(), 1200);
        Fakes.hold(c, shift());
        assertEquals(ShiftState.LOCK, c.getShift());
    }

    @Test
    public void shiftHoldIsLongerThanLetterHold() {
        assertTrue(KeyboardController.holdDelayMs(shift()) >= 1000);
        assertEquals(500, KeyboardController.holdDelayMs(Fakes.byLabel(Page.LETTERS, "a")));
    }

    @Test
    public void holdingALetterTypesItsCapitalAndLeavesShiftAlone() {
        Fakes.hold(c, Fakes.byLabel(Page.LETTERS, "k"));
        assertEquals("K", ed.visible());
        assertEquals(ShiftState.OFF, c.getShift());
        Fakes.tap(c, "k");
        assertEquals("Kk", ed.visible());
    }

    @Test
    public void holdingAtHandsOverToVoiceTypingAndTypesNothing() {
        Fakes.hold(c, Fakes.byType(Page.LETTERS, KeyType.AT));
        assertEquals(1, ed.voice);
        assertEquals("", ed.visible());
    }

    @Test
    public void holdingSpaceOpensTheKeyboardList() {
        Fakes.hold(c, Fakes.byType(Page.LETTERS, KeyType.SPACE));
        assertEquals(1, ed.pickers);
        assertEquals("", ed.visible());
    }

    @Test
    public void holdingFullStopTypesAComma() {
        Fakes.hold(c, Fakes.byType(Page.LETTERS, KeyType.PERIOD));
        assertEquals(",", ed.visible());
    }

    @Test
    public void onlySomeKeysCanBeHeld() {
        assertTrue(KeyboardController.canHold(Fakes.byLabel(Page.LETTERS, "a")));
        assertTrue(KeyboardController.canHold(shift()));
        assertTrue(KeyboardController.canHold(Fakes.byType(Page.LETTERS, KeyType.AT)));
        assertTrue(KeyboardController.canHold(Fakes.byType(Page.LETTERS, KeyType.SPACE)));
        assertTrue(KeyboardController.canHold(Fakes.byType(Page.LETTERS, KeyType.PERIOD)));
        assertFalse(KeyboardController.canHold(Fakes.byType(Page.LETTERS, KeyType.ENTER)));
        assertFalse(KeyboardController.canHold(Fakes.byType(Page.LETTERS, KeyType.NUMBERS)));
        assertFalse(KeyboardController.canHold(Fakes.byType(Page.NUMBERS, KeyType.BACKSPACE)));
        assertFalse(KeyboardController.canHold(Fakes.byLabel(Page.NUMBERS, "5")));
    }
}
