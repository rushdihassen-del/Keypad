package com.keypad.ime;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Locale;

/** Draws the current page as rows of big keys. */
final class KeyboardView extends LinearLayout {
    interface OnKey {
        void onKey(Key key);
    }

    private static final int YELLOW = 0xFFFFD600;
    private static final int BLACK = 0xFF000000;
    private static final int DARK = 0xFF262626;

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
        int height = (int) (dm.heightPixels * (landscape ? 0.78f : 0.64f));
        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    void refresh() {
        removeAllViews();
        boolean yellowKeys = controller.getScheme() == Scheme.YELLOW_ON_BLACK;
        int keyColor = yellowKeys ? YELLOW : BLACK;
        int letterColor = yellowKeys ? BLACK : YELLOW;
        setBackgroundColor(yellowKeys ? BLACK : DARK);
        int edge = (int) (3 * density);
        setPadding(edge, edge, edge, (int) (8 * density));

        int hGap = (int) (3 * density);
        int vGap = (int) (5 * density);
        boolean upper = controller.getShift() != ShiftState.OFF;

        List<List<Key>> rows = Layouts.rows(controller.getLayout(), controller.getPage());
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
                if (upper && k.type == KeyType.CHAR && k.label.length() == 1
                        && Character.isLetter(k.label.charAt(0))) {
                    shown = k.label.toUpperCase(Locale.ROOT);
                }
                boolean shiftKey = k.type == KeyType.SHIFT;
                KeyView kv = new KeyView(getContext(), k, shown, keyColor, letterColor,
                        shiftKey && upper, shiftKey && controller.getShift() == ShiftState.LOCK,
                        new Runnable() {
                            @Override
                            public void run() {
                                onKey.onKey(k);
                            }
                        });
                if (shiftKey) {
                    kv.setContentDescription(controller.getShift() == ShiftState.LOCK ? "Caps lock on"
                            : upper ? "Shift on" : "Shift");
                }
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, k.weight);
                lp.setMargins(hGap, vGap, hGap, vGap);
                r.addView(kv, lp);
            }
        }
    }
}
