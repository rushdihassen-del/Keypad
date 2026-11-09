package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ColourSwitchTest {
    @Test
    public void startsYellowKeysWithBlackLetters() {
        KeyboardController c = new KeyboardController(new Fakes.FakeEditor(), new Fakes.FakeSettings());
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getScheme());
    }

    @Test
    public void colourKeySwitchesInstantlyAndBack() {
        Fakes.FakeSettings st = new Fakes.FakeSettings();
        KeyboardController c = new KeyboardController(new Fakes.FakeEditor(), st);
        c.press(Fakes.byType(c, KeyType.COLOUR), 0);
        assertEquals(Scheme.BLACK_ON_YELLOW, c.getScheme());
        c.press(Fakes.byType(c, KeyType.COLOUR), 0);
        assertEquals(Scheme.YELLOW_ON_BLACK, c.getScheme());
    }

    @Test
    public void lastChoiceIsRemembered() {
        Fakes.FakeSettings st = new Fakes.FakeSettings();
        KeyboardController c = new KeyboardController(new Fakes.FakeEditor(), st);
        c.press(Fakes.byType(c, KeyType.COLOUR), 0);
        assertEquals(Scheme.BLACK_ON_YELLOW, st.scheme);
        KeyboardController later = new KeyboardController(new Fakes.FakeEditor(), st);
        assertEquals(Scheme.BLACK_ON_YELLOW, later.getScheme());
    }

    @Test
    public void colourKeyIsOnEveryPageOfBothLayoutsAndTypesNothing() {
        for (LayoutMode l : LayoutMode.values()) {
            for (Page p : Page.values()) {
                assertTrue(Fakes.hasType(l, p, KeyType.COLOUR));
            }
        }
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = new KeyboardController(ed, new Fakes.FakeSettings());
        c.press(Fakes.byType(c, KeyType.COLOUR), 0);
        assertEquals("", ed.visible());
    }

    @Test
    public void listenerHearsAboutTheSwitch() {
        KeyboardController c = new KeyboardController(new Fakes.FakeEditor(), new Fakes.FakeSettings());
        final int[] calls = {0};
        c.setListener(new KeyboardController.Listener() {
            @Override
            public void onStateChanged() {
                calls[0]++;
            }
        });
        c.press(Fakes.byType(c, KeyType.COLOUR), 0);
        assertEquals(1, calls[0]);
    }
}
