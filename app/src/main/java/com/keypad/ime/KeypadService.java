package com.keypad.ime;

import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class KeypadService extends InputMethodService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private KeyboardController controller;
    private KeyboardView view;
    private EditorInfo info;

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new KeyboardController(new AndroidEditor(), new PrefsSettings(this), loadWords());
        controller.setListener(new KeyboardController.Listener() {
            @Override
            public void onStateChanged() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (view != null) {
                            view.refresh();
                        }
                    }
                });
            }
        });
    }

    /** The fixed word list that ships inside the app. Read once, never written to. */
    private Autocorrect loadWords() {
        try (InputStream in = getResources().openRawResource(R.raw.words)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return new Autocorrect(new String(out.toByteArray(), StandardCharsets.UTF_8));
        } catch (java.io.IOException | RuntimeException e) {
            return null; // no autocorrect, everything else still works
        }
    }

    @Override
    public View onCreateInputView() {
        view = new KeyboardView(this, controller, new KeyboardView.OnKey() {
            @Override
            public void onKey(Key key) {
                controller.press(key, SystemClock.uptimeMillis());
            }

            @Override
            public void onHold(Key key) {
                controller.hold(key, SystemClock.uptimeMillis());
            }
        });
        return view;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        info = attribute;
        int type = attribute == null ? 0 : attribute.inputType;
        controller.startField(FieldRules.prefersNumberPad(type), FieldRules.autocorrectAllowed(type));
    }

    @Override
    public void onFinishInput() {
        controller.reset();
        super.onFinishInput();
    }

    private int imeOptions() {
        return info == null ? 0 : info.imeOptions;
    }

    private void showPicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    /**
     * Hands over to the phone's own voice typing (Google voice typing). Keypad never listens or goes
     * online itself. If no voice typing keyboard is switched on, the keyboard list opens instead.
     */
    @SuppressWarnings("deprecation")
    private void startVoice() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        try {
            for (InputMethodInfo ime : imm.getEnabledInputMethodList()) {
                if (ime.getPackageName().equals(getPackageName())) {
                    continue;
                }
                for (InputMethodSubtype st : imm.getEnabledInputMethodSubtypeList(ime, true)) {
                    if ("voice".equals(st.getMode())) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            switchInputMethod(ime.getId(), st);
                        } else {
                            imm.setInputMethodAndSubtype(getWindow().getWindow().getAttributes().token,
                                    ime.getId(), st);
                        }
                        return;
                    }
                }
            }
        } catch (RuntimeException e) {
            // fall through to the keyboard list
        }
        imm.showInputMethodPicker();
    }

    private final class AndroidEditor implements Editor {
        @Override
        public void commit(String text) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.commitText(text, 1);
            }
        }

        @Override
        public void replace(int deleteBefore, String text) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.beginBatchEdit();
                ic.deleteSurroundingText(deleteBefore, 0);
                ic.commitText(text, 1);
                ic.endBatchEdit();
            }
        }

        @Override
        public String textBefore(int count) {
            InputConnection ic = getCurrentInputConnection();
            CharSequence s = ic == null ? null : ic.getTextBeforeCursor(count, 0);
            return s == null ? "" : s.toString();
        }

        @Override
        public void backspace() {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }

        @Override
        public void enter() {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) {
                return;
            }
            FieldRules.Result r = FieldRules.enter(imeOptions());
            if (r.kind == FieldRules.Kind.EDITOR_ACTION) {
                ic.performEditorAction(r.actionId);
            } else {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
            }
        }

        @Override
        public void showKeyboardList() {
            showPicker();
        }

        @Override
        public void startVoiceTyping() {
            startVoice();
        }
    }
}
