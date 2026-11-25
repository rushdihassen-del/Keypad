package com.keypad.ime;

/** Small value types shared by the keyboard logic. */

/** Which colours the letter pages use. The number pad always uses the opposite. */
enum Scheme { YELLOW_ON_BLACK, BLACK_ON_YELLOW }

enum Page { LETTERS, SYMBOLS, NUMBERS }

enum ShiftState { OFF, ONCE, LOCK }

enum KeyType {
    CHAR, AT, PERIOD, SHIFT, BACKSPACE, SPACE, ENTER, SYMBOLS, NUMBERS, SPACER
}

/** Where the user's own setting lives. Never anything typed. */
interface SettingsStore {
    Scheme getScheme();

    void setScheme(Scheme scheme);
}

/** What the keyboard can do to the app it is typing into. */
interface Editor {
    void commit(String text);

    /** Delete the given number of characters before the cursor, then type the text. */
    void replace(int deleteBefore, String text);

    /** The last few characters before the cursor, used only to check a word before fixing it. */
    String textBefore(int count);

    void backspace();

    /** Enter, exactly as a normal keyboard does it for the open field. */
    void enter();

    /** Open Android's list of keyboards. */
    void showKeyboardList();

    /** Hand over to Google voice typing, or the keyboard list if it is not there. */
    void startVoiceTyping();
}
