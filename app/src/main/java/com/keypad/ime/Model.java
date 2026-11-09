package com.keypad.ime;

/** Small value types shared by the keyboard logic. */
enum Scheme { YELLOW_ON_BLACK, BLACK_ON_YELLOW }

enum LayoutMode { STANDARD, PHONE }

enum Page { LETTERS, SYMBOLS1, SYMBOLS2, NUMBERS }

enum ShiftState { OFF, ONCE, LOCK }

enum KeyType {
    CHAR, MULTITAP, SHIFT, BACKSPACE, SPACE, ENTER, SEND,
    SYMBOLS, SYMBOLS_MORE, LETTERS, NUMBERS, COLOUR, LAYOUT, SPACER
}

/** Where the user's own settings live. Never anything typed. */
interface SettingsStore {
    Scheme getScheme();

    void setScheme(Scheme scheme);

    LayoutMode getLayout();

    void setLayout(LayoutMode layout);
}

/** What the keyboard can do to the app it is typing into. */
interface Editor {
    void commit(String text);

    void setComposing(String text);

    void finishComposing();

    void backspace();

    void enter();

    void send();
}
