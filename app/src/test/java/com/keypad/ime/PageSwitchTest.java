package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PageSwitchTest {
    private Fakes.FakeEditor ed;
    private Fakes.FakeSettings st;
    private KeyboardController c;

    @Before
    public void setUp() {
        ed = new Fakes.FakeEditor();
        st = new Fakes.FakeSettings();
        c = new KeyboardController(ed, st);
    }

    private void press(KeyType t) {
        c.press(Fakes.byType(c, t), 0);
    }

    @Test
    public void standardLettersToSymbolsToNumbersAndBack() {
        assertEquals(Page.LETTERS, c.getPage());
        press(KeyType.SYMBOLS);
        assertEquals(Page.SYMBOLS1, c.getPage());
        press(KeyType.NUMBERS);
        assertEquals(Page.NUMBERS, c.getPage());
        press(KeyType.LETTERS);
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void moreSymbolsTogglesBetweenTheTwoSymbolPages() {
        press(KeyType.SYMBOLS);
        press(KeyType.SYMBOLS_MORE);
        assertEquals(Page.SYMBOLS2, c.getPage());
        press(KeyType.SYMBOLS_MORE);
        assertEquals(Page.SYMBOLS1, c.getPage());
        press(KeyType.SYMBOLS_MORE);
        press(KeyType.LETTERS);
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void symbolsPageHasANumbersButton() {
        press(KeyType.SYMBOLS);
        assertEquals("Numbers", Fakes.byType(c, KeyType.NUMBERS).label);
    }

    @Test
    public void layoutKeySwitchesBetweenStandardAndPhoneAndBack() {
        press(KeyType.LAYOUT);
        assertEquals(LayoutMode.PHONE, c.getLayout());
        assertEquals(Page.LETTERS, c.getPage());
        press(KeyType.LAYOUT);
        assertEquals(LayoutMode.STANDARD, c.getLayout());
    }

    @Test
    public void layoutKeyFromANumberPadGoesToTheOtherLettersPage() {
        press(KeyType.SYMBOLS);
        press(KeyType.NUMBERS);
        press(KeyType.LAYOUT);
        assertEquals(LayoutMode.PHONE, c.getLayout());
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void phonePadReachesSymbolsAndNumbers() {
        press(KeyType.LAYOUT);
        press(KeyType.SYMBOLS);
        assertEquals(Page.SYMBOLS1, c.getPage());
        press(KeyType.LETTERS);
        press(KeyType.NUMBERS);
        assertEquals(Page.NUMBERS, c.getPage());
        press(KeyType.LETTERS);
        assertEquals(Page.LETTERS, c.getPage());
        assertEquals(LayoutMode.PHONE, c.getLayout());
    }

    @Test
    public void layoutChoiceIsRemembered() {
        press(KeyType.LAYOUT);
        assertEquals(LayoutMode.PHONE, st.layout);
        KeyboardController again = new KeyboardController(new Fakes.FakeEditor(), st);
        assertEquals(LayoutMode.PHONE, again.getLayout());
        again.press(Fakes.byType(again, KeyType.LAYOUT), 0);
        assertEquals(LayoutMode.STANDARD, st.layout);
    }

    @Test
    public void numberFieldsOpenOnTheNumberPad() {
        c.startField(true);
        assertEquals(Page.NUMBERS, c.getPage());
        c.startField(false);
        assertEquals(Page.LETTERS, c.getPage());
    }

    @Test
    public void everyPageOfBothLayoutsHasEveryEssentialKey() {
        KeyType[] essential = {
            KeyType.BACKSPACE, KeyType.SPACE, KeyType.ENTER, KeyType.SEND,
            KeyType.COLOUR, KeyType.LAYOUT,
        };
        for (LayoutMode l : LayoutMode.values()) {
            for (Page p : Page.values()) {
                for (KeyType t : essential) {
                    assertTrue(t + " missing on " + l + "/" + p, Fakes.hasType(l, p, t));
                }
            }
        }
    }

    @Test
    public void listenerIsToldOnlyWhenSomethingChanged() {
        final int[] calls = {0};
        c.setListener(new KeyboardController.Listener() {
            @Override
            public void onStateChanged() {
                calls[0]++;
            }
        });
        c.press(Fakes.byLabel(c, "a"), 0);
        assertEquals(0, calls[0]);
        press(KeyType.SYMBOLS);
        assertEquals(1, calls[0]);
    }

    @Test
    public void symbolPagesTypeTheirCharacters() {
        press(KeyType.SYMBOLS);
        c.press(Fakes.byLabel(c, "@"), 0);
        press(KeyType.SYMBOLS_MORE);
        c.press(Fakes.byLabel(c, "€"), 0);
        assertEquals("@€", ed.text.toString());
    }
}
