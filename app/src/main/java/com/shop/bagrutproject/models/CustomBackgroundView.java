package com.shop.bagrutproject.models;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class CustomBackgroundView extends View {
    private Paint gradientPaint;
    private Paint patternPaint;
    private int tileSize = 200; // גודל המרצפות

    public CustomBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // צבע הרקע עם גרדיאנט עדין
        gradientPaint = new Paint();

        // צבע התבנית
        patternPaint = new Paint();
        patternPaint.setColor(0x40BDBDBD); // אפור שקוף ומעודן
        patternPaint.setStrokeWidth(4);
        patternPaint.setStyle(Paint.Style.STROKE);
        patternPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // יצירת גרדיאנט רקע
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h,
                new int[]{0xFFF5F5F5, 0xFFE0E0E0},
                null, Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // ציור הרקע
        canvas.drawRect(0, 0, width, height, gradientPaint);

        // ציור דפוסי המרצפות על כל הרקע
        for (int x = 0; x < width; x += tileSize) {
            for (int y = 0; y < height; y += tileSize) {
                drawTilePattern(canvas, x, y);
            }
        }
    }

    // יצירת "מרצפת" עם דפוסים גיאומטריים יוקרתיים
    private void drawTilePattern(Canvas canvas, int x, int y) {
        Path path = new Path();
        int centerX = x + tileSize / 2;
        int centerY = y + tileSize / 2;

        // ציור יהלום
        path.moveTo(centerX, y);
        path.lineTo(x + tileSize, centerY);
        path.lineTo(centerX, y + tileSize);
        path.lineTo(x, centerY);
        path.close();
        canvas.drawPath(path, patternPaint);

        // ציור כוכב באמצע במקום העיגול
        drawStar(canvas, centerX, centerY, tileSize / 5, tileSize / 2.8f);
    }

    // פונקציה שמציירת כוכב עם 8 קודקודים
    private void drawStar(Canvas canvas, float cx, float cy, float innerRadius, float outerRadius) {
        Path path = new Path();
        double angle = Math.PI / 4; // 8 קודקודים (45 מעלות הפרש)

        for (int i = 0; i < 8; i++) {
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = (float) (cx + r * Math.cos(i * angle));
            float y = (float) (cy + r * Math.sin(i * angle));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, patternPaint);
    }
}
