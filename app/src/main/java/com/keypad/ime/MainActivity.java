package com.keypad.ime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A plain help screen: two big buttons to switch the keyboard on and a box to try it in. */
public class MainActivity extends Activity {
    private static final int YELLOW = 0xFFFFD600;
    private static final int BLACK = 0xFF000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final float d = getResources().getDisplayMetrics().density;
        int pad = (int) (20 * d);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(pad, pad, pad, pad);
        col.setBackgroundColor(BLACK);

        addText(col, "Keypad", 40, true, d);
        addText(col, "A big-key keyboard. It has no internet access, and nothing you type is ever saved or sent.",
                22, false, d);

        addButton(col, "1. Switch Keypad on", d, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        addButton(col, "2. Choose Keypad", d, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
            }
        });

        addText(col, "Try it here:", 22, false, d);
        EditText box = new EditText(this);
        box.setTextSize(28);
        box.setTextColor(YELLOW);
        box.setHintTextColor(0xFFAA9200);
        box.setHint("Type here");
        box.setMinLines(3);
        col.addView(box);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BLACK);
        sv.addView(col);
        setContentView(sv);
    }

    private void addText(LinearLayout col, String s, float sp, boolean bold, float d) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(YELLOW);
        if (bold) {
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
        t.setPadding(0, (int) (8 * d), 0, (int) (8 * d));
        col.addView(t);
    }

    private void addButton(LinearLayout col, String s, float d, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(26);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(BLACK);
        b.setBackgroundColor(YELLOW);
        b.setMinHeight((int) (96 * d));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) (12 * d), 0, (int) (12 * d));
        col.addView(b, lp);
    }
}
