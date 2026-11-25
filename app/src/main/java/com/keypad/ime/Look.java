package com.keypad.ime;

/** The colours and key heights, kept apart from the drawing code so they can be tested. */
final class Look {
    static final int YELLOW = 0xFFFFD600;
    static final int BLACK = 0xFF000000;
    static final int WHITE = 0xFFFFFFFF;
    static final int DARK = 0xFF262626;
    static final int LIGHT_ORANGE = 0xFFFFA64D;
    static final int ORANGE = 0xFFFF6A00;
    static final int BLUE = 0xFF4DA6FF;

    /** Letter and symbol keys are two thirds as tall as they were; the number pad is untouched. */
    static final float KEY_HEIGHT_SCALE = 2f / 3f;
    /** Letters on the shorter keys fill more of the key so they stay big. */
    static final float SHORT_KEY_LETTER_FRACTION = 0.88f;
    private static final float PORTRAIT = 0.64f;
    private static final float LANDSCAPE = 0.78f;

    private Look() {
    }

    /** Colours for one key: {fill, mark, outline}. yellowKeys is true on the yellow scheme. */
    static int[] colours(Key k, boolean yellowKeys) {
        if (k.type == KeyType.ENTER) {
            return new int[] {ORANGE, BLACK, ORANGE};
        }
        if (k.type == KeyType.BACKSPACE) {
            return new int[] {BLUE, BLACK, BLUE};
        }
        if (k.anchor) {
            // Landmark keys: black with white letters on the yellow scheme, light orange on the other.
            return yellowKeys
                    ? new int[] {BLACK, WHITE, WHITE}
                    : new int[] {LIGHT_ORANGE, BLACK, LIGHT_ORANGE};
        }
        return yellowKeys
                ? new int[] {YELLOW, BLACK, YELLOW}
                : new int[] {BLACK, YELLOW, YELLOW};
    }

    /** Share of the screen height a page takes up. */
    static float pageHeightShare(Page page, boolean landscape) {
        float full = landscape ? LANDSCAPE : PORTRAIT;
        return page == Page.NUMBERS ? full : full * KEY_HEIGHT_SCALE;
    }
}
