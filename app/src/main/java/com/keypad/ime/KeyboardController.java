package com.keypad.ime;

import java.util.Locale;

/**
 * All keyboard behaviour, with no Android dependencies so it can be tested on its own.
 * Holds only keyboard state: colour scheme, layout, page, shift and the letter being cycled.
 * Typed text goes straight to the Editor and is never kept here.
 */
final class KeyboardController {
    static final long MULTITAP_TIMEOUT_MS = 900;
    static final long DOUBLE_TAP_MS = 500;

    interface Listener {
        void onStateChanged();
    }

    private final Editor editor;
    private final SettingsStore settings;
    private Listener listener;

    private Scheme scheme;
    private LayoutMode layout;
    private Page page = Page.LETTERS;
    private ShiftState shift = ShiftState.OFF;

    private char pendingDigit = 0;
    private int pendingIndex;
    private boolean pendingUpper;
    private long lastTapTime;
    private long lastShiftTime = -1_000_000L;

    KeyboardController(Editor editor, SettingsStore settings) {
        this.editor = editor;
        this.settings = settings;
        this.scheme = settings.getScheme();
        this.layout = settings.getLayout();
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    Scheme getScheme() {
        return scheme;
    }

    LayoutMode getLayout() {
        return layout;
    }

    Page getPage() {
        return page;
    }

    ShiftState getShift() {
        return shift;
    }

    boolean hasPending() {
        return pendingDigit != 0;
    }

    /** A new field was opened. Number fields start on the number pad. */
    void startField(boolean numberPad) {
        int before = snapshot();
        pendingDigit = 0;
        page = numberPad ? Page.NUMBERS : Page.LETTERS;
        notifyIfChanged(before);
    }

    /** Forget the letter being cycled without typing it (the field went away). */
    void reset() {
        pendingDigit = 0;
    }

    /** Accept the letter being cycled. Called after a pause and before any other key. */
    void flushPending() {
        if (pendingDigit != 0) {
            pendingDigit = 0;
            editor.finishComposing();
        }
    }

    void press(Key key, long now) {
        int before = snapshot();

        if (key.type == KeyType.BACKSPACE && hasPending()) {
            pendingDigit = 0;
            editor.setComposing("");
            editor.finishComposing();
            return;
        }
        if (key.type != KeyType.MULTITAP) {
            flushPending();
        }

        switch (key.type) {
            case CHAR:
                typeChar(key.output);
                break;
            case MULTITAP:
                multiTap(key.output.charAt(0), now);
                break;
            case SHIFT:
                toggleShift(now);
                break;
            case BACKSPACE:
                editor.backspace();
                break;
            case SPACE:
                editor.commit(" ");
                break;
            case ENTER:
                editor.enter();
                break;
            case SEND:
                editor.send();
                break;
            case SYMBOLS:
                page = Page.SYMBOLS1;
                break;
            case SYMBOLS_MORE:
                page = page == Page.SYMBOLS1 ? Page.SYMBOLS2 : Page.SYMBOLS1;
                break;
            case LETTERS:
                page = Page.LETTERS;
                break;
            case NUMBERS:
                page = Page.NUMBERS;
                break;
            case COLOUR:
                scheme = scheme == Scheme.YELLOW_ON_BLACK ? Scheme.BLACK_ON_YELLOW : Scheme.YELLOW_ON_BLACK;
                settings.setScheme(scheme);
                break;
            case LAYOUT:
                layout = layout == LayoutMode.STANDARD ? LayoutMode.PHONE : LayoutMode.STANDARD;
                page = Page.LETTERS;
                settings.setLayout(layout);
                break;
            default:
                break;
        }
        notifyIfChanged(before);
    }

    private void typeChar(String s) {
        if (shift != ShiftState.OFF && s.length() == 1 && Character.isLetter(s.charAt(0))) {
            s = s.toUpperCase(Locale.ROOT);
            if (shift == ShiftState.ONCE) {
                shift = ShiftState.OFF;
            }
        }
        editor.commit(s);
    }

    private void multiTap(char digit, long now) {
        String letters = MultiTap.lettersFor(digit);
        if (letters.isEmpty()) {
            return;
        }
        if (hasPending() && pendingDigit == digit && now - lastTapTime <= MULTITAP_TIMEOUT_MS) {
            pendingIndex = (pendingIndex + 1) % letters.length();
        } else {
            flushPending();
            pendingDigit = digit;
            pendingIndex = 0;
            pendingUpper = shift != ShiftState.OFF;
            if (shift == ShiftState.ONCE) {
                shift = ShiftState.OFF;
            }
        }
        lastTapTime = now;
        String letter = String.valueOf(letters.charAt(pendingIndex));
        if (pendingUpper) {
            letter = letter.toUpperCase(Locale.ROOT);
        }
        editor.setComposing(letter);
    }

    private void toggleShift(long now) {
        switch (shift) {
            case OFF:
                shift = ShiftState.ONCE;
                lastShiftTime = now;
                break;
            case ONCE:
                shift = (now - lastShiftTime <= DOUBLE_TAP_MS) ? ShiftState.LOCK : ShiftState.OFF;
                break;
            default:
                shift = ShiftState.OFF;
                break;
        }
    }

    private int snapshot() {
        return scheme.ordinal() | (layout.ordinal() << 2) | (page.ordinal() << 4) | (shift.ordinal() << 8);
    }

    private void notifyIfChanged(int before) {
        if (before != snapshot() && listener != null) {
            listener.onStateChanged();
        }
    }
}
