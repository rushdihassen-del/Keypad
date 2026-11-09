package com.keypad.ime;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

/**
 * Decides what Enter and Send do for the field that is open.
 * Enter always means "new line" where the field allows one.
 * Send uses the app's own send action wherever the app offers one, and never types a new line.
 */
final class EnterSendLogic {
    enum Kind { NEWLINE_TEXT, EDITOR_ACTION, KEY_ENTER, CTRL_ENTER }

    static final class Result {
        final Kind kind;
        final int actionId;

        Result(Kind kind, int actionId) {
            this.kind = kind;
            this.actionId = actionId;
        }
    }

    private EnterSendLogic() {
    }

    static int actionOf(int imeOptions) {
        return imeOptions & EditorInfo.IME_MASK_ACTION;
    }

    private static boolean noEnterAction(int imeOptions) {
        return (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
    }

    static boolean isMultiLine(int inputType) {
        return (inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT
                && (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
    }

    private static boolean isRealAction(int action) {
        return action == EditorInfo.IME_ACTION_SEND
                || action == EditorInfo.IME_ACTION_GO
                || action == EditorInfo.IME_ACTION_SEARCH
                || action == EditorInfo.IME_ACTION_DONE
                || action == EditorInfo.IME_ACTION_NEXT
                || action == EditorInfo.IME_ACTION_PREVIOUS;
    }

    static Result forEnter(int inputType, int imeOptions) {
        if (isMultiLine(inputType)) {
            return new Result(Kind.NEWLINE_TEXT, 0);
        }
        int action = actionOf(imeOptions);
        if (!noEnterAction(imeOptions) && isRealAction(action)) {
            return new Result(Kind.EDITOR_ACTION, action);
        }
        return new Result(Kind.KEY_ENTER, 0);
    }

    static Result forSend(int inputType, int imeOptions) {
        int action = actionOf(imeOptions);
        if (action == EditorInfo.IME_ACTION_SEND) {
            return new Result(Kind.EDITOR_ACTION, action);
        }
        if (!noEnterAction(imeOptions)
                && (action == EditorInfo.IME_ACTION_GO
                || action == EditorInfo.IME_ACTION_SEARCH
                || action == EditorInfo.IME_ACTION_DONE)) {
            return new Result(Kind.EDITOR_ACTION, action);
        }
        return new Result(Kind.CTRL_ENTER, 0);
    }

    /** Number and phone fields (PIN boxes and so on) open straight on the number pad. */
    static boolean prefersNumberPad(int inputType) {
        int cls = inputType & InputType.TYPE_MASK_CLASS;
        return cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE;
    }
}
