package com.keypad.ime;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class KeypadService extends InputMethodService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private KeyboardController controller;
    private KeyboardView view;
    private EditorInfo info;

    private final Runnable flush = new Runnable() {
        @Override
        public void run() {
            controller.flushPending();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new KeyboardController(new AndroidEditor(), new PrefsSettings(this));
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

    @Override
    public View onCreateInputView() {
        view = new KeyboardView(this, controller, new KeyboardView.OnKey() {
            @Override
            public void onKey(Key key) {
                controller.press(key, SystemClock.uptimeMillis());
                handler.removeCallbacks(flush);
                if (controller.hasPending()) {
                    handler.postDelayed(flush, KeyboardController.MULTITAP_TIMEOUT_MS);
                }
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
        handler.removeCallbacks(flush);
        controller.startField(attribute != null && EnterSendLogic.prefersNumberPad(attribute.inputType));
    }

    @Override
    public void onFinishInput() {
        handler.removeCallbacks(flush);
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.finishComposingText();
        }
        controller.reset();
        super.onFinishInput();
    }

    private int inputType() {
        return info == null ? 0 : info.inputType;
    }

    private int imeOptions() {
        return info == null ? 0 : info.imeOptions;
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
        public void setComposing(String text) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.setComposingText(text, 1);
            }
        }

        @Override
        public void finishComposing() {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }

        @Override
        public void backspace() {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }

        @Override
        public void enter() {
            perform(EnterSendLogic.forEnter(inputType(), imeOptions()));
        }

        @Override
        public void send() {
            perform(EnterSendLogic.forSend(inputType(), imeOptions()));
        }

        private void perform(EnterSendLogic.Result r) {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) {
                return;
            }
            switch (r.kind) {
                case NEWLINE_TEXT:
                    ic.commitText("\n", 1);
                    break;
                case EDITOR_ACTION:
                    ic.performEditorAction(r.actionId);
                    break;
                case KEY_ENTER:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                    break;
                case CTRL_ENTER:
                    long t = SystemClock.uptimeMillis();
                    int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
                    ic.sendKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, meta));
                    ic.sendKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, meta));
                    break;
                default:
                    break;
            }
        }
    }
}
