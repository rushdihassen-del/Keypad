package com.keypad.ime;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Locale;

/** Draws the current page as rows of big keys with clear gaps between them. */
final class KeyboardView extends LinearLayout {
    interface OnKey {
        void onKey(Key key);

        void onHold(Key key);
    }

    private final KeyboardController controller;
    private final OnKey onKey;
    private final float density;

    KeyboardView(Context context, KeyboardController controller, OnKey onKey) {
        super(context);
        this.controller = controller;
        this.onKey = onKey;
        this.density = context.getResources().getDisplayMetrics().density;
        setOrientation(VERTICAL);
        refresh();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        boolean landscape = dm.widthPixels > dm.heightPixels;
        int height = (int) (dm.heightPixels * Look.pageHeightShare(controller.getPage(), landscape));
        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    void refresh() {
        removeAllViews();
        boolean yellowKeys = controller.getPageScheme() == Scheme.YELLOW_ON_BLACK;
        setBackgroundColor(yellowKeys ? Look.BLACK : Look.DARK);
        int edge = (int) (2 * density);
        setPadding(edge, edge, edge, (int) (8 * density));

        int hGap = (int) (5 * density);
        int vGap = (int) (7 * density);
        boolean upper = controller.getShift() != ShiftState.OFF;

        List<List<Key>> rows = Layouts.rows(controller.getPage());
        for (List<Key> row : rows) {
            LinearLayout r = new LinearLayout(getContext());
            r.setOrientation(HORIZONTAL);
            addView(r, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
            for (final Key k : row) {
                if (k.type == KeyType.SPACER) {
                    r.addView(new View(getContext()),
                            new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, k.weight));
                    continue;
                }
                String shown = k.label;
                if (upper && k.isLetter()) {
                    shown = k.label.toUpperCase(Locale.ROOT);
                }
                int[] look = Look.colours(k, yellowKeys);
                int keyColor = look[0];
                int letterColor = look[1];
                int outline = look[2];
                boolean shiftKey = k.type == KeyType.SHIFT;
                Runnable hold = null;
                if (KeyboardController.canHold(k)) {
                    hold = new Runnable() {
                        @Override
                        public void run() {
                            onKey.onHold(k);
                        }
                    };
                }
                KeyView kv = new KeyView(getContext(), k, shown, keyColor, letterColor, outline,
                        shiftKey && upper, shiftKey && controller.getShift() == ShiftState.LOCK,
                        new Runnable() {
                            @Override
                            public void run() {
                                onKey.onKey(k);
                            }
                        }, hold, KeyboardController.holdDelayMs(k));
                if (controller.getPage() != Page.NUMBERS) {
                    kv.setLetterFraction(Look.SHORT_KEY_LETTER_FRACTION);
                }
                if (shiftKey) {
                    kv.setContentDescription(controller.getShift() == ShiftState.LOCK
                            ? "Caps lock on. Hold to change colours"
                            : upper ? "Shift on. Hold to change colours" : k.description);
                }
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, k.weight);
                lp.setMargins(hGap, vGap, hGap, vGap);
                r.addView(kv, lp);
            }
        }
    }
}
