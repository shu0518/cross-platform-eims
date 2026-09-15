package com.example.a0731;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageView;

public class ResizableImageView extends AppCompatImageView {
    private ResizeControlPoint resizeControl;
    private Paint borderPaint;
    private boolean isPreviewMode = false;
    private static final int TOUCH_PADDING = 40;  // 觸摸檢測的額外範圍

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // 檢查是否在擴展的觸摸範圍內，包括控制點區域
        if (x >= -TOUCH_PADDING && x <= getWidth() + TOUCH_PADDING &&
                y >= -TOUCH_PADDING && y <= getHeight() + TOUCH_PADDING) {

            // 調用父類的 onTouchEvent 來處理事件
            return super.onTouchEvent(event);
        }

        return false;
    }
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }
    public ResizableImageView(Context context) {
        super(context);
        init();
    }

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResizableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        resizeControl = new ResizeControlPoint();

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        setClipToOutline(false);
    }

    public void setPreviewMode(boolean previewMode) {
        this.isPreviewMode = previewMode;
        resizeControl.setPreviewMode(previewMode);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isPreviewMode) {
            resizeControl.drawControlPoints(canvas, this);
        }
    }

    public ResizeControlPoint getResizeControl() {
        return resizeControl;
    }
}