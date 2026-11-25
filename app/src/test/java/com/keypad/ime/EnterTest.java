package com.keypad.ime;

import static org.junit.Assert.assertEquals;

import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public class EnterTest {
    @Test
    public void enterUsesTheAppsOwnActionWhenThereIsOne() {
        int[] actions = {EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT};
        for (int a : actions) {
            FieldRules.Result r = FieldRules.enter(a);
            assertEquals(FieldRules.Kind.EDITOR_ACTION, r.kind);
            assertEquals(a, r.actionId);
        }
    }

    @Test
    public void whatsAppEnterIsSendCountsAsSend() {
        FieldRules.Result r = FieldRules.enter(EditorInfo.IME_ACTION_SEND);
        assertEquals(FieldRules.Kind.EDITOR_ACTION, r.kind);
        assertEquals(EditorInfo.IME_ACTION_SEND, r.actionId);
    }

    @Test
    public void normalMessageBoxesStartANewLine() {
        assertEquals(FieldRules.Kind.KEY_ENTER, FieldRules.enter(EditorInfo.IME_ACTION_NONE).kind);
        assertEquals(FieldRules.Kind.KEY_ENTER, FieldRules.enter(EditorInfo.IME_ACTION_UNSPECIFIED).kind);
        assertEquals(FieldRules.Kind.KEY_ENTER, FieldRules.enter(
                EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_ENTER_ACTION).kind);
        assertEquals(FieldRules.Kind.KEY_ENTER, FieldRules.enter(
                EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION).kind);
    }

    @Test
    public void otherFlagsDoNotChangeTheAction() {
        int options = EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        assertEquals(FieldRules.Kind.EDITOR_ACTION, FieldRules.enter(options).kind);
        assertEquals(EditorInfo.IME_ACTION_SEARCH, FieldRules.enter(options).actionId);
    }

    @Test
    public void thereIsOnlyOneEnterKeyAndPressingItCallsTheEditorOnce() {
        for (Page p : Page.values()) {
            assertEquals(1, Fakes.count(p, KeyType.ENTER));
        }
        Fakes.FakeEditor ed = new Fakes.FakeEditor();
        KeyboardController c = Fakes.plainField(ed, new Fakes.FakeSettings());
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.ENTER));
        assertEquals(1, ed.enters);
        Fakes.tap(c, Fakes.byType(Page.LETTERS, KeyType.NUMBERS));
        Fakes.tap(c, Fakes.byType(Page.NUMBERS, KeyType.ENTER));
        assertEquals(2, ed.enters);
        assertEquals("", ed.visible());
    }

    @Test
    public void enterIsWiderThanBackspaceAndSitsInTheRightHandCorner() {
        java.util.List<Key> bottom = Layouts.rows(Page.LETTERS).get(3);
        assertEquals(KeyType.ENTER, bottom.get(bottom.size() - 1).type);
        assertEquals(2f, bottom.get(bottom.size() - 1).weight, 0f);
    }
}
