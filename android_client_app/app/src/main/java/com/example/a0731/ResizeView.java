package com.example.a0731;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ResizeView extends FrameLayout {
    private static final int CONTROL_POINT_SIZE = 20;
    private ImageView mainImageView;
    private View[] controlPoints;
    private ResizeViewListener listener;

    // 控制點位置枚舉
    private enum ControlPosition {
        TOP_LEFT(0),
        TOP_CENTER(1),
        TOP_RIGHT(2),
        RIGHT_CENTER(3),
        BOTTOM_RIGHT(4),
        BOTTOM_CENTER(5),
        BOTTOM_LEFT(6),
        LEFT_CENTER(7);

        final int index;
        ControlPosition(int index) {
            this.index = index;
        }
    }

    public interface ResizeViewListener {
        void onDragStart();
        void onDragEnd();
        void onDeleteRequested(ResizeView view);
    }

    // 觸控相關變數
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int RESIZE = 2;
    private static final int ROTATE = 3;

    private int mode = NONE;
    private float lastTouchX, lastTouchY;
    private float initialX, initialY;
    private float initialRotation;
    private ControlPosition activeControl = null;
    private View trashView;
    private View modelSelectionView;
    private boolean isInTrashArea = false;

    public ResizeView(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 初始化主圖片視圖
        mainImageView = new ImageView(getContext());
        mainImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(mainImageView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        // 初始化控制點
        controlPoints = new View[8];
        for (int i = 0; i < 8; i++) {
            View point = new View(getContext());
            point.setBackgroundColor(Color.BLUE);
            LayoutParams params = new LayoutParams(CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
            addView(point, params);
            controlPoints[i] = point;

            final int pointIndex = i;
            point.setOnTouchListener((v, event) -> handleControlPointTouch(pointIndex, event));
        }

        // 設置主視圖的觸控監聽
        mainImageView.setOnTouchListener(this::handleMainViewTouch);

        updateControlPoints();
    }

    // 其他方法待續...
    private boolean handleMainViewTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                initialX = getX();
                initialY = getY();
                showTrashAndHideSelection(true);
                if (listener != null) {
                    listener.onDragStart();
                }
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                float dist = spacing(event);
                if (dist > 10f) {
                    mode = ROTATE;
                    initialRotation = rotation(event);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    float dx = event.getRawX() - lastTouchX;
                    float dy = event.getRawY() - lastTouchY;
                    setX(initialX + dx);
                    setY(initialY + dy);
                    checkTrashIntersection(event.getRawX(), event.getRawY());
                }
                else if (mode == ROTATE) {
                    float newRotation = rotation(event) - initialRotation;
                    setRotation(getRotation() + newRotation);
                    initialRotation = rotation(event);
                }
                return true;

            case MotionEvent.ACTION_UP:
                handleActionUp(event.getRawX(), event.getRawY());
                return true;
        }
        return false;
    }

    private boolean handleControlPointTouch(int pointIndex, MotionEvent event) {
        ControlPosition control = ControlPosition.values()[pointIndex];

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode = RESIZE;
                activeControl = control;
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mode == RESIZE && activeControl != null) {
                    float dx = event.getRawX() - lastTouchX;
                    float dy = event.getRawY() - lastTouchY;
                    handleResize(activeControl, dx, dy);
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                }
                return true;

            case MotionEvent.ACTION_UP:
                mode = NONE;
                activeControl = null;
                return true;
        }
        return false;
    }

    private void handleResize(ControlPosition position, float dx, float dy) {
        ViewGroup.LayoutParams params = getLayoutParams();
        int newWidth = getWidth();
        int newHeight = getHeight();
        float newX = getX();
        float newY = getY();

        switch (position) {
            case RIGHT_CENTER:
                newWidth += dx;
                break;
            case LEFT_CENTER:
                newWidth -= dx;
                newX += dx;
                break;
            case TOP_CENTER:
                newHeight -= dy;
                newY += dy;
                break;
            case BOTTOM_CENTER:
                newHeight += dy;
                break;
            case TOP_LEFT:
                newWidth -= dx;
                newHeight -= dy;
                newX += dx;
                newY += dy;
                break;
            case TOP_RIGHT:
                newWidth += dx;
                newHeight -= dy;
                newY += dy;
                break;
            case BOTTOM_LEFT:
                newWidth -= dx;
                newHeight += dy;
                newX += dx;
                break;
            case BOTTOM_RIGHT:
                newWidth += dx;
                newHeight += dy;
                break;
        }

        // 設置最小尺寸
        newWidth = Math.max(100, newWidth);
        newHeight = Math.max(100, newHeight);

        params.width = newWidth;
        params.height = newHeight;
        setLayoutParams(params);
        setX(newX);
        setY(newY);

        updateControlPoints();
    }
    private void handleActionUp(float rawX, float rawY) {
        mode = NONE;
        if (isInTrashArea(rawX, rawY)) {
            handleDelete();
        }
        showTrashAndHideSelection(false);
        isInTrashArea = false;
        if (listener != null) {
            listener.onDragEnd();
        }
    }

    private void handleDelete() {
        animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    if (listener != null) {
                        listener.onDeleteRequested(this);
                    }
                })
                .start();
    }

    private void updateControlPoints() {
        int width = getWidth();
        int height = getHeight();

        // 更新控制點位置
        LayoutParams[] positions = {
                createPointParams(0, 0),                    // TOP_LEFT
                createPointParams(width/2, 0),              // TOP_CENTER
                createPointParams(width, 0),                // TOP_RIGHT
                createPointParams(width, height/2),         // RIGHT_CENTER
                createPointParams(width, height),           // BOTTOM_RIGHT
                createPointParams(width/2, height),         // BOTTOM_CENTER
                createPointParams(0, height),               // BOTTOM_LEFT
                createPointParams(0, height/2)              // LEFT_CENTER
        };

        for (int i = 0; i < controlPoints.length; i++) {
            controlPoints[i].setLayoutParams(positions[i]);
        }
    }

    private LayoutParams createPointParams(int x, int y) {
        LayoutParams params = new LayoutParams(CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
        params.leftMargin = x - CONTROL_POINT_SIZE / 2;
        params.topMargin = y - CONTROL_POINT_SIZE / 2;
        return params;
    }

    private void showTrashAndHideSelection(boolean isDragging) {
        if (trashView != null) {
            trashView.setVisibility(isDragging ? View.VISIBLE : View.GONE);
        }
        if (modelSelectionView != null) {
            modelSelectionView.setVisibility(isDragging ? View.GONE : View.VISIBLE);
        }
    }

    // 輔助方法
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0;
        double dx = event.getX(1) - event.getX(0);
        double dy = event.getY(1) - event.getY(0);
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private boolean isInTrashArea(float x, float y) {
        if (trashView == null || trashView.getVisibility() != View.VISIBLE) return false;
        int[] location = new int[2];
        trashView.getLocationOnScreen(location);
        return x >= location[0] && x <= location[0] + trashView.getWidth() &&
                y >= location[1] && y <= location[1] + trashView.getHeight();
    }

    private void checkTrashIntersection(float x, float y) {
        boolean wasInTrash = isInTrashArea;
        isInTrashArea = isInTrashArea(x, y);
        if (isInTrashArea != wasInTrash && trashView != null) {
            trashView.animate()
                    .scaleX(isInTrashArea ? 1.2f : 1.0f)
                    .scaleY(isInTrashArea ? 1.2f : 1.0f)
                    .setDuration(100)
                    .start();
        }
    }

    // Setter 方法
    public void setImage(Bitmap bitmap) {
        mainImageView.setImageBitmap(bitmap);
    }

    public void setListener(ResizeViewListener listener) {
        this.listener = listener;
    }

    public void setTrashView(View trashView) {
        this.trashView = trashView;
    }

    public void setModelSelectionView(View modelSelectionView) {
        this.modelSelectionView = modelSelectionView;
    }
}
