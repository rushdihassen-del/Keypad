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
 * so near-misses still count. Backspace repeats while held.
 */
final class KeyView extends View {
    static final int OUTLINE = 0xFFFFD600;

    private static final long REPEAT_DELAY_MS = 450;
    private static final long REPEAT_INTERVAL_MS = 70;

    private final KeyType type;
    private final String label;
    private final String sub;
    private final int keyColor;
    private final int letterColor;
    private final boolean active;
    private final boolean locked;
    private final Runnable onPress;
    private final float density;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();

    private boolean down;
    private boolean repeated;

    private final Runnable repeat = new Runnable() {
        @Override
        public void run() {
            repeated = true;
            onPress.run();
            postDelayed(this, REPEAT_INTERVAL_MS);
        }
    };

    KeyView(Context context, Key key, String shownLabel, int keyColor, int letterColor,
            boolean active, boolean locked, Runnable onPress) {
        super(context);
        this.type = key.type;
        this.label = shownLabel;
        this.sub = key.sub;
        this.keyColor = keyColor;
        this.letterColor = letterColor;
        this.active = active;
        this.locked = locked;
        this.onPress = onPress;
        this.density = context.getResources().getDisplayMetrics().density;
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        setClickable(true);
        setContentDescription(key.description);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        onPress.run();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                down = true;
                repeated = false;
                invalidate();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (type == KeyType.BACKSPACE) {
                    postDelayed(repeat, REPEAT_DELAY_MS);
                }
                return true;
            case MotionEvent.ACTION_UP:
                down = false;
                removeCallbacks(repeat);
                invalidate();
                if (!repeated) {
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                down = false;
                removeCallbacks(repeat);
                invalidate();
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(repeat);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth();
        float h = getHeight();
        float stroke = 3 * density;
        float radius = 14 * density;
        boolean inverted = down != active;
        int bg = inverted ? letterColor : keyColor;
        int fg = inverted ? keyColor : letterColor;

        rect.set(stroke / 2, stroke / 2, w - stroke / 2, h - stroke / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bg);
        c.drawRoundRect(rect, radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(OUTLINE);
        c.drawRoundRect(rect, radius, radius, paint);

        text.setColor(fg);
        float padW = w * 0.9f;
        boolean hasIcon = type == KeyType.SHIFT || type == KeyType.BACKSPACE || type == KeyType.COLOUR;
        if (hasIcon) {
            if (label.isEmpty()) {
                drawIcon(c, w / 2, h / 2, Math.min(w, h) * 0.55f, fg, bg);
            } else {
                drawIcon(c, w / 2, h * 0.36f, Math.min(w, h * 0.5f) * 0.5f, fg, bg);
                drawLines(c, label.split("\n"), w, h * 0.62f, h * 0.95f, padW, 0.8f);
            }
        } else if (!sub.isEmpty()) {
            drawLines(c, new String[] {label}, w, h * 0.04f, h * 0.58f, padW, 0.9f);
            drawLines(c, new String[] {sub}, w, h * 0.58f, h * 0.96f, padW, 0.62f);
        } else {
            drawLines(c, label.split("\n"), w, 0, h, padW, 0.72f);
        }
    }

    private float fitWidth(String s, float maxW) {
        text.setTextSize(100);
        float tw = text.measureText(s);
        return tw > 0 ? 100 * maxW / tw : Float.MAX_VALUE;
    }

    private void drawLines(Canvas c, String[] lines, float w, float top, float bottom,
            float maxW, float heightFraction) {
        float regionH = bottom - top;
        float size = regionH * heightFraction / lines.length;
        for (String line : lines) {
            size = Math.min(size, fitWidth(line, maxW));
        }
        text.setTextSize(size);
        float lineH = size * 1.15f;
        float y0 = top + (regionH - lineH * lines.length) / 2;
        for (int i = 0; i < lines.length; i++) {
            float center = y0 + (i + 0.5f) * lineH;
            c.drawText(lines[i], w / 2, center + 0.36f * size, text);
        }
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
            case COLOUR:
                rect.set(cx - s * 0.5f, cy - s * 0.5f, cx + s * 0.5f, cy + s * 0.5f);
                c.drawArc(rect, 90, 180, true, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(s * 0.1f);
                c.drawOval(rect, paint);
                break;
            default:
                break;
        }
    }
}
