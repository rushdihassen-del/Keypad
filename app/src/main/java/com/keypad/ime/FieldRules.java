package com.keypad.ime;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

/** What the open field asks of the keyboard. Reads only the field's settings, never its text. */
final class FieldRules {
    enum Kind { EDITOR_ACTION, KEY_ENTER }

    static final class Result {
        final Kind kind;
        final int actionId;

        Result(Kind kind, int actionId) {
            this.kind = kind;
            this.actionId = actionId;
        }
    }

    private FieldRules() {
    }

    static int actionOf(int imeOptions) {
        return imeOptions & EditorInfo.IME_MASK_ACTION;
    }

    private static boolean isRealAction(int action) {
        return action == EditorInfo.IME_ACTION_SEND
                || action == EditorInfo.IME_ACTION_GO
                || action == EditorInfo.IME_ACTION_SEARCH
                || action == EditorInfo.IME_ACTION_DONE
                || action == EditorInfo.IME_ACTION_NEXT
                || action == EditorInfo.IME_ACTION_PREVIOUS;
    }

    /**
     * Enter as a normal keyboard does it: if the app set an action on the field (Send in WhatsApp
     * with "Enter is send", Search, Go, Done, Next) do that action, otherwise press Enter, which
     * starts a new line in a message box.
     */
    static Result enter(int imeOptions) {
        int action = actionOf(imeOptions);
        boolean noAction = (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
        if (!noAction && isRealAction(action)) {
            return new Result(Kind.EDITOR_ACTION, action);
        }
        return new Result(Kind.KEY_ENTER, 0);
    }

    /** Number and phone fields (PIN boxes and so on) open straight on the number pad. */
    static boolean prefersNumberPad(int inputType) {
        int cls = inputType & InputType.TYPE_MASK_CLASS;
        return cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE;
    }

    /** Autocorrect is off in password, email, web address, number and similar boxes. */
    static boolean autocorrectAllowed(int inputType) {
        if ((inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) {
            return false;
        }
        if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
            return false;
        }
        switch (inputType & InputType.TYPE_MASK_VARIATION) {
            case InputType.TYPE_TEXT_VARIATION_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            case InputType.TYPE_TEXT_VARIATION_URI:
            case InputType.TYPE_TEXT_VARIATION_FILTER:
                return false;
            default:
                return true;
        }
    }
}
