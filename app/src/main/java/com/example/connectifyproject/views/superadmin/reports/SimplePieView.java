package com.example.connectifyproject.views.superadmin.reports;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class SimplePieView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private int v1, v2, v3;
    private int c1, c2, c3;

    public SimplePieView(Context c) { super(c); init(); }
    public SimplePieView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public SimplePieView(Context c, @Nullable AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        // colores por defecto
        c1 = 0xFF26A69A; // teal
        c2 = 0xFF6A1B9A; // purple
        c3 = 0xFFE53935; // red
    }

    public void setValues(int v1, int v2, int v3){
        this.v1 = Math.max(0, v1);
        this.v2 = Math.max(0, v2);
        this.v3 = Math.max(0, v3);
        invalidate();
    }

    public void setColors(@ColorInt int c1, @ColorInt int c2, @ColorInt int c3){
        this.c1 = c1; this.c2 = c2; this.c3 = c3;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        float pad = Math.min(w, h) * 0.06f;
        oval.set(pad, pad, w - pad, h - pad);

        int sum = v1 + v2 + v3;
        if (sum <= 0) {
            paint.setColor(0xFFE0E0E0);
            canvas.drawArc(oval, 0, 360, true, paint);
            return;
        }

        float a1 = 360f * v1 / sum;
        float a2 = 360f * v2 / sum;
        float start = -90f;

        paint.setColor(c1);
        canvas.drawArc(oval, start, a1, true, paint);
        start += a1;

        paint.setColor(c2);
        canvas.drawArc(oval, start, a2, true, paint);
        start += a2;

        paint.setColor(c3);
        canvas.drawArc(oval, start, 360f - a1 - a2, true, paint);
    }
}
