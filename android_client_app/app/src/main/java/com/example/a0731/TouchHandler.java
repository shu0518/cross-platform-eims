package com.example.a0731;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class TouchHandler implements View.OnTouchListener {
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int RESIZE = 2;
    private static final int ROTATE = 3;

    private int mode = NONE;
    private int activeControlPoint = ResizeControlPoint.NONE;

    private float initialTouchX, initialTouchY;
    private float initialX, initialY;
    private float initialWidth, initialHeight;
    private float initialRotation;
    private float oldRotation;
    private PointF midPoint = new PointF();

    private final ResizableImageView targetView;
    private final View trashView;
    private final View modelSelectionView;
    private final PointF start = new PointF();

    private boolean isInTrashArea = false;
    private boolean isEnabled = true;

    public TouchHandler(View trashView, View modelSelectionView, View targetView) {
        this.trashView = trashView;
        this.modelSelectionView = modelSelectionView;
        this.targetView = (ResizableImageView) targetView;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            showTrashAndHideSelection(false);
            isInTrashArea = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEnabled) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activeControlPoint = targetView.getResizeControl().hitTest(event.getX(), event.getY(), targetView);

                if (activeControlPoint != ResizeControlPoint.NONE) {
                    mode = RESIZE;
                } else {
                    mode = DRAG;
                }

                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                initialX = targetView.getX();
                initialY = targetView.getY();
                initialWidth = targetView.getWidth();
                initialHeight = targetView.getHeight();

                start.set(event.getRawX(), event.getRawY());
                showTrashAndHideSelection(true);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mode = ROTATE;
                    oldRotation = rotation(event);
                    midPoint(midPoint, event);
                    initialRotation = targetView.getRotation();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    handleDrag(event);
                } else if (mode == RESIZE) {
                    handleResize(event);
                } else if (mode == ROTATE && event.getPointerCount() == 2) {
                    handleRotation(event);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() <= 2) {
                    mode = NONE;
                }
                activeControlPoint = ResizeControlPoint.NONE;

                if (isInTrashArea) {
                    targetView.animate()
                            .alpha(0f)
                            .scaleX(0f)
                            .scaleY(0f)
                            .setDuration(200)
                            .withEndAction(() -> {
                                ((ViewGroup) targetView.getParent()).removeView(targetView);
                            })
                            .start();

                    showTrashAnimation();
                }

                showTrashAndHideSelection(false);
                isInTrashArea = false;
                break;
        }

        if ((mode == DRAG || mode == RESIZE) && !isInTrashArea) {
            checkTrashIntersection(event.getRawX(), event.getRawY());
        }

        return true;
    }

    private void handleRotation(MotionEvent event) {
        float newRotation = rotation(event);
        float deltaRotation = newRotation - oldRotation;
        targetView.setRotation(targetView.getRotation() + deltaRotation);
        oldRotation = newRotation;
    }

    private float rotation(MotionEvent event) {
        float deltaX = (event.getX(0) - event.getX(1));
        float deltaY = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        point.set(x, y);
    }

    private void handleDrag(MotionEvent event) {
        float dx = event.getRawX() - start.x;
        float dy = event.getRawY() - start.y;

        targetView.animate()
                .x(initialX + dx)
                .y(initialY + dy)
                .setDuration(0)
                .start();
    }

    private void handleResize(MotionEvent event) {
        float dx = event.getRawX() - initialTouchX;
        float dy = event.getRawY() - initialTouchY;

        ViewGroup.LayoutParams params = targetView.getLayoutParams();
        float newWidth = initialWidth;
        float newHeight = initialHeight;
        float newX = initialX;
        float newY = initialY;

        switch (activeControlPoint) {
            case ResizeControlPoint.TOP_LEFT:
                float scale = Math.min(
                        (initialWidth - dx) / initialWidth,
                        (initialHeight - dy) / initialHeight
                );
                newWidth = initialWidth * scale;
                newHeight = initialHeight * scale;
                newX = initialX + (initialWidth - newWidth);
                newY = initialY + (initialHeight - newHeight);
                break;

            case ResizeControlPoint.TOP:
                newHeight = initialHeight - dy;
                newY = initialY + dy;
                break;

            case ResizeControlPoint.TOP_RIGHT:
                scale = Math.min(
                        (initialWidth + dx) / initialWidth,
                        (initialHeight - dy) / initialHeight
                );
                newWidth = initialWidth * scale;
                newHeight = initialHeight * scale;
                newY = initialY + (initialHeight - newHeight);
                break;

            case ResizeControlPoint.RIGHT:
                newWidth = initialWidth + dx;
                break;

            case ResizeControlPoint.BOTTOM_RIGHT:
                scale = Math.min(
                        (initialWidth + dx) / initialWidth,
                        (initialHeight + dy) / initialHeight
                );
                newWidth = initialWidth * scale;
                newHeight = initialHeight * scale;
                break;

            case ResizeControlPoint.BOTTOM:
                newHeight = initialHeight + dy;
                break;

            case ResizeControlPoint.BOTTOM_LEFT:
                scale = Math.min(
                        (initialWidth - dx) / initialWidth,
                        (initialHeight + dy) / initialHeight
                );
                newWidth = initialWidth * scale;
                newHeight = initialHeight * scale;
                newX = initialX + (initialWidth - newWidth);
                break;

            case ResizeControlPoint.LEFT:
                newWidth = initialWidth - dx;
                newX = initialX + dx;
                break;
        }

        newWidth = Math.max(100, newWidth);
        newHeight = Math.max(100, newHeight);

        params.width = (int) newWidth;
        params.height = (int) newHeight;
        targetView.setLayoutParams(params);
        targetView.setX(newX);
        targetView.setY(newY);
        targetView.invalidate();
    }

    private void showTrashAnimation() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.2f, 1.0f, 1.2f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        trashView.startAnimation(scaleAnimation);
    }

    private void showTrashAndHideSelection(boolean isDragging) {
        if (isEnabled) {
            trashView.setVisibility(isDragging ? View.VISIBLE : View.GONE);
            modelSelectionView.setVisibility(isDragging ? View.GONE : View.VISIBLE);
        }
    }

    private void checkTrashIntersection(float x, float y) {
        boolean wasInTrash = isInTrashArea;
        isInTrashArea = isInTrashArea(x, y);

        if (isInTrashArea != wasInTrash) {
            float scale = isInTrashArea ? 1.2f : 1.0f;
            trashView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(100)
                    .start();
        }
    }

    private boolean isInTrashArea(float x, float y) {
        if (trashView.getVisibility() != View.VISIBLE) return false;

        int[] location = new int[2];
        trashView.getLocationOnScreen(location);
        float trashX = location[0];
        float trashY = location[1];

        return x >= trashX && x <= trashX + trashView.getWidth() &&
                y >= trashY && y <= trashY + trashView.getHeight();
    }
}