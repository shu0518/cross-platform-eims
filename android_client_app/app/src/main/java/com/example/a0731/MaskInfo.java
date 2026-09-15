package com.example.a0731;

import java.io.Serializable;
import java.util.ArrayList;

public class MaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    // 窗戶的座標資訊
    public final int x1, y1, x2, y2;
    public final int width, height;
    public final ArrayList<Point> maskPoints;

    public MaskInfo(int x1, int y1, int x2, int y2, ArrayList<Point> maskPoints) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.width = x2 - x1;
        this.height = y2 - y1;
        this.maskPoints = maskPoints;
    }

    // Point 類定義為 MaskInfo 的內部類
    public static class Point implements Serializable {
        private static final long serialVersionUID = 1L;

        public float x, y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public String toString() {
        return "MaskInfo{" +
                "position=(" + x1 + "," + y1 + "), " +
                "size=" + width + "x" + height + ", " +
                "points=" + (maskPoints != null ? maskPoints.size() : 0) +
                "}";
    }
}