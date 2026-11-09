package com.keypad.ime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public class EnterSendTest {
    private static final int MULTI = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    private static final int SINGLE = InputType.TYPE_CLASS_TEXT;

    @Test
    public void enterInAMultiLineFieldTypesANewLine() {
        EnterSendLogic.Result r = EnterSendLogic.forEnter(MULTI, EditorInfo.IME_ACTION_NONE);
        assertEquals(EnterSendLogic.Kind.NEWLINE_TEXT, r.kind);
    }

    @Test
    public void enterStaysANewLineEvenWhenTheAppHasASendAction() {
        EnterSendLogic.Result r = EnterSendLogic.forEnter(MULTI, EditorInfo.IME_ACTION_SEND);
        assertEquals(EnterSendLogic.Kind.NEWLINE_TEXT, r.kind);
    }

    @Test
    public void enterInASingleLineFieldUsesItsAction() {
        EnterSendLogic.Result r = EnterSendLogic.forEnter(SINGLE, EditorInfo.IME_ACTION_SEARCH);
        assertEquals(EnterSendLogic.Kind.EDITOR_ACTION, r.kind);
        assertEquals(EditorInfo.IME_ACTION_SEARCH, r.actionId);
    }

    @Test
    public void enterInASingleLineFieldWithoutAnActionSendsTheEnterKey() {
        assertEquals(EnterSendLogic.Kind.KEY_ENTER, EnterSendLogic.forEnter(SINGLE, 0).kind);
        assertEquals(EnterSendLogic.Kind.KEY_ENTER,
                EnterSendLogic.forEnter(0, EditorInfo.IME_ACTION_NONE).kind);
        assertEquals(EnterSendLogic.Kind.KEY_ENTER,
                EnterSendLogic.forEnter(SINGLE,
                        EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION).kind);
    }

    @Test
    public void sendUsesTheAppsOwnSendAction() {
        EnterSendLogic.Result r = EnterSendLogic.forSend(MULTI, EditorInfo.IME_ACTION_SEND);
        assertEquals(EnterSendLogic.Kind.EDITOR_ACTION, r.kind);
        assertEquals(EditorInfo.IME_ACTION_SEND, r.actionId);
    }

    @Test
    public void sendUsesTheSendActionEvenIfTheAppMarkedEnterAsNewLine() {
        EnterSendLogic.Result r = EnterSendLogic.forSend(MULTI,
                EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        assertEquals(EnterSendLogic.Kind.EDITOR_ACTION, r.kind);
        assertEquals(EditorInfo.IME_ACTION_SEND, r.actionId);
    }

    @Test
    public void sendFallsBackToGoSearchOrDone() {
        int[] actions = {EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_DONE};
        for (int a : actions) {
            EnterSendLogic.Result r = EnterSendLogic.forSend(SINGLE, a);
            assertEquals(EnterSendLogic.Kind.EDITOR_ACTION, r.kind);
            assertEquals(a, r.actionId);
        }
    }

    @Test
    public void sendWithNoActionPressesCtrlEnterAndNeverTypesANewLine() {
        assertEquals(EnterSendLogic.Kind.CTRL_ENTER, EnterSendLogic.forSend(MULTI, 0).kind);
        assertEquals(EnterSendLogic.Kind.CTRL_ENTER,
                EnterSendLogic.forSend(MULTI, EditorInfo.IME_FLAG_NO_ENTER_ACTION).kind);
        assertEquals(EnterSendLogic.Kind.CTRL_ENTER,
                EnterSendLogic.forSend(SINGLE, EditorInfo.IME_ACTION_NEXT).kind);
        assertEquals(EnterSendLogic.Kind.CTRL_ENTER,
                EnterSendLogic.forSend(SINGLE, EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION).kind);
    }

    @Test
    public void numberAndPhoneFieldsPreferTheNumberPad() {
        assertTrue(EnterSendLogic.prefersNumberPad(InputType.TYPE_CLASS_NUMBER));
        assertTrue(EnterSendLogic.prefersNumberPad(InputType.TYPE_CLASS_PHONE));
        assertFalse(EnterSendLogic.prefersNumberPad(InputType.TYPE_CLASS_TEXT));
    }

    @Test
    public void enterAndSendKeysReachTheEditorOnEveryPageOfBothLayouts() {
        for (LayoutMode l : LayoutMode.values()) {
            for (Page p : Page.values()) {
                Fakes.FakeEditor ed = new Fakes.FakeEditor();
                Fakes.FakeSettings st = new Fakes.FakeSettings();
                st.layout = l;
                KeyboardController c = new KeyboardController(ed, st);
                if (p == Page.NUMBERS) {
                    c.press(Fakes.byType(c, KeyType.SYMBOLS), 0);
                    c.press(Fakes.byType(c, KeyType.NUMBERS), 0);
                } else if (p != Page.LETTERS) {
                    c.press(Fakes.byType(c, KeyType.SYMBOLS), 0);
                    if (p == Page.SYMBOLS2) {
                        c.press(Fakes.byType(c, KeyType.SYMBOLS_MORE), 0);
                    }
                }
                assertEquals(p, c.getPage());
                c.press(Fakes.byType(c, KeyType.ENTER), 0);
                assertEquals(1, ed.enters);
                assertEquals(0, ed.sends);
                c.press(Fakes.byType(c, KeyType.SEND), 0);
                assertEquals(1, ed.sends);
                assertEquals("", ed.visible());
            }
        }
    }

    @Test
    public void enterAndSendAcceptAPendingPhonePadLetterFirst() {
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        Fakes.FakeSettings st = new Fakes.FakeSettings();
        st.layout = LayoutMode.PHONE;
        KeyboardController c = new KeyboardController(ed, st);
        c.press(Fakes.byLabel(c, "2"), 0);
        c.press(Fakes.byType(c, KeyType.ENTER), 100);
        assertEquals("a", ed.text.toString());
        c.press(Fakes.byLabel(c, "3"), 200);
        c.press(Fakes.byType(c, KeyType.SEND), 300);
        assertEquals("ad", ed.text.toString());
    }
}
