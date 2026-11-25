package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class AutocorrectTest {
    private final Fakes.FakeEditor ed = new Fakes.FakeEditor();
    private final Fakes.FakeSettings settings = new Fakes.FakeSettings();
    private final KeyboardController c = Fakes.plainField(ed, settings);

    private Key space() {
        return Fakes.byType(Page.LETTERS, KeyType.SPACE);
    }

    private Key period() {
        return Fakes.byType(Page.LETTERS, KeyType.PERIOD);
    }

    private Key backspace() {
        return Fakes.byType(Page.LETTERS, KeyType.BACKSPACE);
    }

    @Test
    public void fixesANeighbouringKeySlipOnSpace() {
        Fakes.typeText(c, "pleade come ");
        assertEquals("please come ", ed.visible());
    }

    @Test
    public void fixesLettersSwappedOnSpace() {
        Fakes.typeText(c, "teh ");
        assertEquals("the ", ed.visible());
        Fakes.typeText(c, "wrod ");
        assertEquals("the word ", ed.visible());
    }

    @Test
    public void fixesTheWordOnFullStopToo() {
        Fakes.typeText(c, "see you tomorrpw.");
        assertEquals("see you tomorrow.", ed.visible());
    }

    @Test
    public void doesNotFixOnComma() {
        Fakes.typeText(c, "wrod");
        Fakes.hold(c, period());
        assertEquals("wrod,", ed.visible());
    }

    @Test
    public void leavesRealWordsAndUnknownFarOffWordsAlone() {
        Fakes.typeText(c, "hello zzzzzz xqxqxq ");
        assertEquals("hello zzzzzz xqxqxq ", ed.visible());
    }

    @Test
    public void keepsTheCapitalAtTheStartOfASentence() {
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.SHIFT));
        Fakes.typeText(c, "teh ");
        assertEquals("The ", ed.visible());
    }

    @Test
    public void leavesNamesInTheMiddleOfASentenceAlone() {
        Fakes.typeText(c, "i met ");
        Fakes.hold(c, Fakes.byLabel(Page.LETTERS, "p"));
        Fakes.typeText(c, "leade ");
        assertEquals("i met Pleade ", ed.visible());
    }

    @Test
    public void addsAMissingApostrophe() {
        Fakes.typeText(c, "i dont know ");
        assertEquals("i don't know ", ed.visible());
    }

    @Test
    public void backspaceStraightAfterACorrectionUndoesIt() {
        Fakes.typeText(c, "teh ");
        assertEquals("the ", ed.visible());
        Fakes.tap(c, backspace());
        assertEquals("teh", ed.visible());
        Fakes.typeText(c, " ");
        assertEquals("teh ", ed.visible());
    }

    @Test
    public void undoOnFullStopRestoresTheOriginalWord() {
        Fakes.typeText(c, "teh.");
        assertEquals("the.", ed.visible());
        Fakes.tap(c, backspace());
        assertEquals("teh", ed.visible());
    }

    @Test
    public void backspaceOnlyUndoesWhenItComesStraightAfter() {
        Fakes.typeText(c, "teh ");
        Fakes.typeText(c, "a");
        Fakes.tap(c, backspace());
        assertEquals("the ", ed.visible());
    }

    @Test
    public void nothingIsCorrectedWhenTheWordOnScreenIsNotTheWordTyped() {
        Fakes.typeText(c, "teh");
        ed.text.setLength(0);
        ed.text.append("xxx");
        Fakes.typeText(c, " ");
        assertEquals("xxx ", ed.visible());
    }

    @Test
    public void autocorrectIsOffInPasswordEmailWebAddressAndNumberBoxes() {
        int text = InputType.TYPE_CLASS_TEXT;
        assertTrue(FieldRules.autocorrectAllowed(text));
        assertTrue(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_VARIATION_URI));
        assertFalse(FieldRules.autocorrectAllowed(InputType.TYPE_CLASS_NUMBER));
        assertFalse(FieldRules.autocorrectAllowed(InputType.TYPE_CLASS_PHONE));
        assertFalse(FieldRules.autocorrectAllowed(text | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        assertFalse(FieldRules.autocorrectAllowed(0));
    }

    @Test
    public void controllerDoesNotCorrectWhenSwitchedOffForTheField() {
        c.startField(false, false);
        Fakes.typeText(c, "teh ");
        assertEquals("teh ", ed.visible());
    }

    @Test
    public void wordListIsBritishEnglish() {
        Autocorrect a = Fakes.words();
        assertTrue(a.knows("colour"));
        assertTrue(a.knows("favourite"));
        assertTrue(a.knows("centre"));
        assertFalse(a.knows("color"));
        assertFalse(a.knows("favorite"));
        assertFalse(a.knows("center"));
        assertEquals("colour", a.correct("colouur"));
    }

    @Test
    public void autocorrectCorrectsOnlyFromTheFixedListAndNeverGrowsIt() throws Exception {
        Autocorrect a = Fakes.words();
        int before = sizeOfMaps(a);
        for (int i = 0; i < 50; i++) {
            a.correct("zzqx" + i);
            a.correct("teh");
        }
        assertEquals(before, sizeOfMaps(a));
        assertNull(a.correct("rushdi"));
        assertFalse(a.knows("rushdi"));
        for (Field f : Autocorrect.class.getDeclaredFields()) {
            assertTrue(f.getName() + " must be final", Modifier.isFinal(f.getModifiers()));
        }
    }

    private static int sizeOfMaps(Autocorrect a) throws Exception {
        int n = 0;
        for (Field f : Autocorrect.class.getDeclaredFields()) {
            f.setAccessible(true);
            Object v = f.get(a);
            if (v instanceof Map) {
                n += ((Map<?, ?>) v).size();
            }
        }
        return n;
    }

    @Test
    public void typedWordsAreNotKeptAnywhereOnceTheyAreDone() throws Exception {
        Fakes.typeText(c, "teh quick brwn fox ");
        Fakes.typeText(c, "teh ");
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.ENTER));
        for (Field f : KeyboardController.class.getDeclaredFields()) {
            f.setAccessible(true);
            Object v = f.get(c);
            if (v instanceof StringBuilder) {
                assertEquals(f.getName(), 0, ((StringBuilder) v).length());
            }
            if (v instanceof String) {
                assertEquals("no stored string " + f.getName(), "", v);
            }
            if (v instanceof Collection || v instanceof Map) {
                throw new AssertionError("controller must not hold collections: " + f.getName());
            }
            if (f.getName().equals("lastCorrection")) {
                assertNull(v);
            }
        }
        assertEquals(0, settings.writes);
    }

    @Test
    public void leavingTheFieldForgetsTheWordAndTheUndo() throws Exception {
        Fakes.typeText(c, "teh ");
        c.reset();
        Fakes.typeText(c, "ab");
        Fakes.tap(c, backspace());
        assertEquals("the a", ed.visible());
    }

    @Test
    public void onlyTheColourChoiceEverReachesSettings() {
        Fakes.typeText(c, "teh word ");
        Fakes.tap(c, backspace());
        assertEquals(0, settings.writes);
        assertEquals(Scheme.YELLOW_ON_BLACK, settings.scheme);
    }
}
