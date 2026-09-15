package com.example.a0731;

import android.graphics.Path;
import android.graphics.PathMeasure;
import java.io.Serializable;

public class WindowInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    // 窗戶的座標資訊
    public int x1, y1;      // 左上角座標
    public int x2, y2;      // 右下角座標

    // 儲存窗戶形狀的路徑點
    public float[] pathPoints;  // 用於儲存窗戶輪廓的點陣列

    // 建構函數 - 用於創建新的窗戶資訊物件
    public WindowInfo(int x1, int y1, int x2, int y2, Path path) {
        // 儲存窗戶的基本座標
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        // 將 Path 物件轉換為點陣列
        if (path != null) {
            PathMeasure pathMeasure = new PathMeasure(path, false);
            // 每 5 個像素取一個點來描述窗戶形狀
            int pointCount = (int) (pathMeasure.getLength() / 5);
            pathPoints = new float[pointCount * 2];  // x,y 各佔一個位置
            float[] point = new float[2];

            // 取樣路徑上的點
            for (int i = 0; i < pointCount; i++) {
                pathMeasure.getPosTan(i * 5, point, null);
                pathPoints[i*2] = point[0];     // x 座標
                pathPoints[i*2+1] = point[1];   // y 座標
            }
        }
    }
}