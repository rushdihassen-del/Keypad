package com.keypad.ime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

/**
 * One big key. The key you first touch is the key that types, even if your finger slides a little,
 * so near-misses still count. Backspace repeats while held. Keys with a hold action run it once
 * the hold time passes, and then do not type when the finger lifts.
 */
final class KeyView extends View {
    private static final long REPEAT_DELAY_MS = 450;
    private static final long REPEAT_INTERVAL_MS = 70;

    private final KeyType type;
    private final String label;
    private final int keyColor;
    private final int letterColor;
    private final int outline;
    private final boolean active;
    private final boolean locked;
    private final Runnable onPress;
    private final Runnable onHold;
    private final long holdMs;
    private final float density;

    private float letterFraction = 0.78f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();

    private boolean down;
    private boolean repeated;
    private boolean held;

    private final Runnable repeat = new Runnable() {
        @Override
        public void run() {
            repeated = true;
            onPress.run();
            postDelayed(this, REPEAT_INTERVAL_MS);
        }
    };

    private final Runnable holdAction = new Runnable() {
        @Override
        public void run() {
            held = true;
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            onHold.run();
        }
    };

    KeyView(Context context, Key key, String shownLabel, int keyColor, int letterColor, int outline,
            boolean active, boolean locked, Runnable onPress, Runnable onHold, long holdMs) {
        super(context);
        this.type = key.type;
        this.label = shownLabel;
        this.keyColor = keyColor;
        this.letterColor = letterColor;
        this.outline = outline;
        this.active = active;
        this.locked = locked;
        this.onPress = onPress;
        this.onHold = onHold;
        this.holdMs = holdMs;
        this.density = context.getResources().getDisplayMetrics().density;
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        text.setFakeBoldText(true);
        setClickable(true);
        setContentDescription(key.description);
    }

    /** How much of the key's height a single letter may fill. */
    void setLetterFraction(float fraction) {
        letterFraction = fraction;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        onPress.run();
        return true;
    }

    private void stopTimers() {
        removeCallbacks(repeat);
        removeCallbacks(holdAction);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                down = true;
                repeated = false;
                held = false;
                invalidate();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (type == KeyType.BACKSPACE) {
                    postDelayed(repeat, REPEAT_DELAY_MS);
                }
                if (onHold != null) {
                    postDelayed(holdAction, holdMs);
                }
                return true;
            case MotionEvent.ACTION_UP:
                down = false;
                stopTimers();
                invalidate();
                if (!repeated && !held) {
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                down = false;
                stopTimers();
                invalidate();
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopTimers();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth();
        float h = getHeight();
        float radius = 14 * density;
        float stroke = 3 * density;
        boolean inverted = down != active;
        int bg = inverted ? letterColor : keyColor;
        int fg = inverted ? keyColor : letterColor;

        rect.set(stroke / 2, stroke / 2, w - stroke / 2, h - stroke / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bg);
        c.drawRoundRect(rect, radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(outline);
        c.drawRoundRect(rect, radius, radius, paint);

        text.setColor(fg);
        if (type == KeyType.SHIFT || type == KeyType.BACKSPACE) {
            drawIcon(c, w / 2, h / 2, Math.min(w, h) * 0.55f, fg, bg);
        } else if (label.length() == 1) {
            String reference = Character.isLetter(label.charAt(0))
                    ? (Character.isUpperCase(label.charAt(0)) ? "W" : "m") : "W";
            drawText(c, label, reference, w, h, letterFraction);
        } else {
            drawText(c, label, label, w, h, 0.5f);
        }
    }

    /** Size comes from the reference text so every letter key shows the same size. */
    private void drawText(Canvas c, String s, String reference, float w, float h, float heightFraction) {
        text.setTextSize(100);
        float refWidth = text.measureText(reference);
        float size = Math.min(h * heightFraction, refWidth > 0 ? 100 * (w * 0.96f) / refWidth : h);
        text.setTextSize(size);
        c.drawText(s, w / 2, h / 2 + 0.36f * size, text);
    }

    private void drawIcon(Canvas c, float cx, float cy, float s, int fg, int bg) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fg);
        path.reset();
        switch (type) {
            case SHIFT:
                path.moveTo(cx, cy - s * 0.5f);
                path.lineTo(cx + s * 0.5f, cy);
                path.lineTo(cx + s * 0.2f, cy);
                path.lineTo(cx + s * 0.2f, cy + s * 0.5f);
                path.lineTo(cx - s * 0.2f, cy + s * 0.5f);
                path.lineTo(cx - s * 0.2f, cy);
                path.lineTo(cx - s * 0.5f, cy);
                path.close();
                c.drawPath(path, paint);
                if (locked) {
                    c.drawRect(cx - s * 0.3f, cy + s * 0.62f, cx + s * 0.3f, cy + s * 0.74f, paint);
                }
                break;
            case BACKSPACE:
                path.moveTo(cx - s * 0.5f, cy);
                path.lineTo(cx - s * 0.15f, cy - s * 0.35f);
                path.lineTo(cx + s * 0.5f, cy - s * 0.35f);
                path.lineTo(cx + s * 0.5f, cy + s * 0.35f);
                path.lineTo(cx - s * 0.15f, cy + s * 0.35f);
                path.close();
                c.drawPath(path, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(s * 0.1f);
                paint.setColor(bg);
                float a = s * 0.13f;
                float x = cx + s * 0.18f;
                c.drawLine(x - a, cy - a, x + a, cy + a, paint);
                c.drawLine(x - a, cy + a, x + a, cy - a, paint);
                break;
            default:
                break;
        }
    }
}
