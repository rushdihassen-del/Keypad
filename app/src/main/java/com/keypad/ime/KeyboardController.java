package com.keypad.ime;

import java.util.Locale;

/**
 * All keyboard behaviour, with no Android dependencies so it can be tested on its own.
 * Holds only keyboard state: colour scheme, page and shift. Typed text goes straight to the
 * Editor. The one word being typed is held in memory only until the word ends, so it can be
 * checked for autocorrect; it is never saved, logged or sent.
 */
final class KeyboardController {
    static final long DOUBLE_TAP_MS = 500;
    static final long HOLD_LETTER_MS = 500;
    static final long HOLD_SHIFT_MS = 1000;

    interface Listener {
        void onStateChanged();
    }

    /** The last autocorrection, kept only until the next key so Backspace can undo it. */
    private static final class Correction {
        final String original;
        final String corrected;
        final String separator;
        final boolean wasSentenceStart;

        Correction(String original, String corrected, String separator, boolean wasSentenceStart) {
            this.original = original;
            this.corrected = corrected;
            this.separator = separator;
            this.wasSentenceStart = wasSentenceStart;
        }
    }

    private final Editor editor;
    private final SettingsStore settings;
    private final Autocorrect autocorrect;
    private Listener listener;

    private Scheme scheme;
    private Page page = Page.LETTERS;
    private ShiftState shift = ShiftState.OFF;
    private long lastShiftTime = -1_000_000L;
    private boolean autocorrectOn;

    private final StringBuilder word = new StringBuilder();
    private boolean wordTrusted = true;
    private boolean wordAtSentenceStart;
    private boolean sentenceStart = true;
    private boolean noRecorrect;
    private Correction lastCorrection;

    KeyboardController(Editor editor, SettingsStore settings, Autocorrect autocorrect) {
        this.editor = editor;
        this.settings = settings;
        this.autocorrect = autocorrect;
        this.scheme = settings.getScheme();
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    /** The colours of the letter pages. */
    Scheme getScheme() {
        return scheme;
    }

    /** The number pad always uses the opposite colours to the letters. */
    Scheme getPageScheme() {
        if (page != Page.NUMBERS) {
            return scheme;
        }
        return scheme == Scheme.YELLOW_ON_BLACK ? Scheme.BLACK_ON_YELLOW : Scheme.YELLOW_ON_BLACK;
    }

    Page getPage() {
        return page;
    }

    ShiftState getShift() {
        return shift;
    }

    /** A new field was opened. Number fields start on the number pad. */
    void startField(boolean numberPad, boolean autocorrectAllowed) {
        int before = snapshot();
        page = numberPad ? Page.NUMBERS : Page.LETTERS;
        autocorrectOn = autocorrectAllowed && autocorrect != null;
        sentenceStart = true;
        forgetWord();
        notifyIfChanged(before);
    }

    /** The field went away. */
    void reset() {
        forgetWord();
        sentenceStart = true;
    }

    static boolean canHold(Key key) {
        return key.isLetter() || key.type == KeyType.SHIFT || key.type == KeyType.AT
                || key.type == KeyType.SPACE || key.type == KeyType.PERIOD;
    }

    static long holdDelayMs(Key key) {
        return key.type == KeyType.SHIFT ? HOLD_SHIFT_MS : HOLD_LETTER_MS;
    }

    /** A key was held long enough. The tap that would follow is skipped by the key view. */
    void hold(Key key, long now) {
        int before = snapshot();
        if (key.type != KeyType.SHIFT) {
            lastCorrection = null; // changing colours is not typing; Backspace can still undo
        }
        switch (key.type) {
            case SHIFT:
                scheme = scheme == Scheme.YELLOW_ON_BLACK ? Scheme.BLACK_ON_YELLOW : Scheme.YELLOW_ON_BLACK;
                settings.setScheme(scheme);
                break;
            case AT:
                forgetWord();
                editor.startVoiceTyping();
                break;
            case SPACE:
                editor.showKeyboardList();
                break;
            case PERIOD:
                forgetWord();
                editor.commit(",");
                break;
            case CHAR:
                if (key.isLetter()) {
                    typeLetter(key.output.toUpperCase(Locale.ROOT));
                }
                break;
            default:
                break;
        }
        notifyIfChanged(before);
    }

    void press(Key key, long now) {
        int before = snapshot();
        Correction undo = lastCorrection;
        lastCorrection = null;

        if (key.type == KeyType.BACKSPACE && undo != null) {
            undoCorrection(undo);
            notifyIfChanged(before);
            return;
        }

        switch (key.type) {
            case CHAR:
                if (key.isLetter()) {
                    String s = key.output;
                    if (shift != ShiftState.OFF) {
                        s = s.toUpperCase(Locale.ROOT);
                    }
                    typeLetter(s);
                } else {
                    typeOther(key.output);
                }
                break;
            case AT:
                forgetWord();
                editor.commit("@");
                break;
            case PERIOD:
                endWord(".");
                sentenceStart = true;
                break;
            case SPACE:
                endWord(" ");
                break;
            case ENTER:
                forgetWord();
                editor.enter();
                sentenceStart = true;
                break;
            case BACKSPACE:
                editor.backspace();
                if (word.length() > 0) {
                    word.setLength(word.length() - 1);
                } else {
                    wordTrusted = false;
                }
                noRecorrect = false;
                break;
            case SHIFT:
                toggleShift(now);
                break;
            case SYMBOLS:
                forgetWord();
                page = page == Page.SYMBOLS ? Page.LETTERS : Page.SYMBOLS;
                break;
            case NUMBERS:
                forgetWord();
                page = page == Page.NUMBERS ? Page.LETTERS : Page.NUMBERS;
                break;
            default:
                break;
        }
        notifyIfChanged(before);
    }

    private void typeLetter(String s) {
        if (shift == ShiftState.ONCE) {
            shift = ShiftState.OFF;
        }
        editor.commit(s);
        if (wordTrusted) {
            if (word.length() == 0) {
                wordAtSentenceStart = sentenceStart;
            }
            word.append(s);
        }
        sentenceStart = false;
    }

    private void typeOther(String s) {
        if (s.equals("'") && word.length() > 0) {
            editor.commit(s);
            word.append(s);
            return;
        }
        forgetWord();
        editor.commit(s);
        if (s.equals("!") || s.equals("?")) {
            sentenceStart = true;
        }
    }

    /** Space or full stop: fix the word just finished if it is an obvious slip, then type it. */
    private void endWord(String separator) {
        boolean fixed = false;
        if (autocorrectOn && wordTrusted && !noRecorrect && word.length() > 0) {
            String typed = word.toString();
            boolean name = Character.isUpperCase(typed.charAt(0)) && !wordAtSentenceStart;
            String fix = name ? null : autocorrect.correct(typed);
            if (fix != null && editor.textBefore(typed.length()).equals(typed)) {
                editor.replace(typed.length(), fix + separator);
                lastCorrection = new Correction(typed, fix, separator, wordAtSentenceStart);
                fixed = true;
            }
        }
        if (!fixed) {
            editor.commit(separator);
        }
        forgetWord();
    }

    private void undoCorrection(Correction c) {
        editor.replace(c.corrected.length() + c.separator.length(), c.original);
        word.setLength(0);
        word.append(c.original);
        wordTrusted = true;
        wordAtSentenceStart = c.wasSentenceStart;
        sentenceStart = false;
        noRecorrect = true;
    }

    private void forgetWord() {
        word.setLength(0);
        wordTrusted = true;
        noRecorrect = false;
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
        return scheme.ordinal() | (page.ordinal() << 2) | (shift.ordinal() << 6);
    }

    private void notifyIfChanged(int before) {
        if (before != snapshot() && listener != null) {
            listener.onStateChanged();
        }
    }
}
