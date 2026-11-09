package com.keypad.ime;

import android.content.Context;
import android.content.SharedPreferences;

/** Remembers only the colour scheme and the layout. Nothing typed is ever kept. */
final class PrefsSettings implements SettingsStore {
    private static final String FILE = "keypad_settings";
    private static final String KEY_SCHEME = "scheme";
    private static final String KEY_LAYOUT = "layout";

    private final Context context;

    PrefsSettings(Context context) {
        this.context = context;
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    @Override
    public Scheme getScheme() {
        try {
            return Scheme.valueOf(prefs().getString(KEY_SCHEME, Scheme.YELLOW_ON_BLACK.name()));
        } catch (RuntimeException e) {
            return Scheme.YELLOW_ON_BLACK;
        }
    }

    @Override
    public void setScheme(Scheme scheme) {
        try {
            prefs().edit().putString(KEY_SCHEME, scheme.name()).apply();
        } catch (RuntimeException e) {
            // Storage not ready yet; the choice just won't be remembered this time.
        }
    }

    @Override
    public LayoutMode getLayout() {
        try {
            return LayoutMode.valueOf(prefs().getString(KEY_LAYOUT, LayoutMode.STANDARD.name()));
        } catch (RuntimeException e) {
            return LayoutMode.STANDARD;
        }
    }

    @Override
    public void setLayout(LayoutMode layout) {
        try {
            prefs().edit().putString(KEY_LAYOUT, layout.name()).apply();
        } catch (RuntimeException e) {
            // Storage not ready yet; the choice just won't be remembered this time.
        }
    }
}
