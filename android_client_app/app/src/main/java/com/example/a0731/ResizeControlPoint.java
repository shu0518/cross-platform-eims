package com.example.a0731;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

public class ResizeControlPoint {
    public static final int NONE = 0;
    public static final int TOP_LEFT = 1;
    public static final int TOP = 2;
    public static final int TOP_RIGHT = 3;
    public static final int RIGHT = 4;
    public static final int BOTTOM_RIGHT = 5;
    public static final int BOTTOM = 6;
    public static final int BOTTOM_LEFT = 7;
    public static final int LEFT = 8;

    private int controlPointRadius = 20;
    private int touchRadius = 50;
    private Paint controlPointPaint;
    private Paint strokePaint;
    private boolean isPreviewMode = false;
    // 增加邊緣檢測的閾值
    private static final int EDGE_THRESHOLD = 50;  // 邊緣可點擊區域的寬度

    public ResizeControlPoint() {
        controlPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        controlPointPaint.setColor(Color.WHITE);
        controlPointPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
    }

    public void setPreviewMode(boolean previewMode) {
        this.isPreviewMode = previewMode;
    }

    public int hitTest(float x, float y, View targetView) {
        if (isPreviewMode) return NONE;  // 在預覽模式下不檢測點擊

        float width = targetView.getWidth();
        float height = targetView.getHeight();

        // 控制點位置在邊線上
        PointF[] points = new PointF[8];
        points[0] = new PointF(0, 0);                // 左上
        points[1] = new PointF(width/2, 0);          // 上中
        points[2] = new PointF(width, 0);            // 右上
        points[3] = new PointF(width, height/2);     // 右中
        points[4] = new PointF(width, height);       // 右下
        points[5] = new PointF(width/2, height);     // 下中
        points[6] = new PointF(0, height);           // 左下
        points[7] = new PointF(0, height/2);         // 左中

        // 使用較大的觸摸判定半徑
        float touchRadius = controlPointRadius * 1.5f;  // 增加觸摸判定範圍

        for (int i = 0; i < points.length; i++) {
            if (isPointInCircle(x, y, points[i].x, points[i].y, touchRadius)) {
                return i + 1;
            }
        }

        return NONE;
    }

    public void drawControlPoints(Canvas canvas, View targetView) {
        if (isPreviewMode) return;  // 在預覽模式下不繪製控制點

        float width = targetView.getWidth();
        float height = targetView.getHeight();

        // 控制點在邊線上
        PointF[] points = new PointF[8];
        points[0] = new PointF(0, 0);
        points[1] = new PointF(width/2, 0);
        points[2] = new PointF(width, 0);
        points[3] = new PointF(width, height/2);
        points[4] = new PointF(width, height);
        points[5] = new PointF(width/2, height);
        points[6] = new PointF(0, height);
        points[7] = new PointF(0, height/2);

        for (PointF point : points) {
            canvas.drawCircle(point.x, point.y, controlPointRadius, controlPointPaint);
            canvas.drawCircle(point.x, point.y, controlPointRadius, strokePaint);
        }
    }

    private boolean isPointInCircle(float px, float py, float cx, float cy, float radius) {
        float dx = px - cx;
        float dy = py - cy;
        return (dx * dx + dy * dy) <= radius * radius;
    }
}