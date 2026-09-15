package com.example.a0731;

import android.content.Context;
import android.content.Intent;
import java.util.Arrays;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TestCameraActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Module module;
    private ImageView originalImageView;
    private ImageView segmentedImageView;
    private TextView resultTextView;
    private static final int MODEL_INPUT_SIZE = 640;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);

        initializeViews();
        loadPyTorchModel();
        openGallery();
    }

    private void initializeViews() {
        originalImageView = findViewById(R.id.originalImageView);
        segmentedImageView = findViewById(R.id.segmentedImageView);
        resultTextView = findViewById(R.id.resultTextView);
    }

    private void loadPyTorchModel() {
        try {
            String modelPath = assetFilePath(this, "model_1119.torchscript.pt");
            Log.d("TestCameraActivity", "Model path: " + modelPath);
            if (new File(modelPath).exists()) {
                module = Module.load(modelPath);
                resultTextView.setText("模型載入成功");
            } else {
                resultTextView.setText("找不到模型文件");
            }
        } catch (Exception e) {
            resultTextView.setText("模型載入失敗: " + e.getMessage());
            Log.e("TestCameraActivity", "Error loading model", e);
        }
    }

    private String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            processSelectedImage(data.getData());
        }
    }

    private void processSelectedImage(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

            originalImageView.setImageBitmap(originalBitmap);
            resultTextView.setText("圖片處理中...");

            if (module != null) {
                runInference(resizedBitmap, originalBitmap);
            } else {
                resultTextView.setText("模型未正確載入");
            }
        } catch (IOException e) {
            resultTextView.setText("圖片載入失敗");
            Log.e("TestCameraActivity", "Error processing image", e);
        }
    }

    //  runInference 方法
    private void runInference(Bitmap inputBitmap, Bitmap originalBitmap) {
        try {
            Bitmap resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(originalBitmap, 0, 0, null);

            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    inputBitmap,
                    new float[]{0,0,0},
                    new float[]{1,1,1}
            );

            IValue[] outputs = module.forward(IValue.from(inputTensor)).toTuple();
            float[] predictionData = outputs[0].toTensor().getDataAsFloatArray();
            float[] protoData = outputs[1].toTensor().getDataAsFloatArray();

            float maxConf = 0;
            int bestIndex = -1;
            for (int i = 0; i < 8400; i++) {
                float confidence = predictionData[4 * 8400 + i];
                if (confidence > maxConf && confidence > 0.6f) {
                    maxConf = confidence;
                    bestIndex = i;
                }
            }

            if (bestIndex >= 0) {
                float[] maskCoeffs = new float[32];
                for (int j = 0; j < 32; j++) {
                    maskCoeffs[j] = predictionData[(5 + j) * 8400 + bestIndex];
                }

                int maskSize = 640;
                float[][] mask = new float[maskSize][maskSize];

                for (int y = 0; y < maskSize; y++) {
                    for (int x = 0; x < maskSize; x++) {
                        float maskValue = 0;
                        float protoX = x * 160f / maskSize;
                        float protoY = y * 160f / maskSize;

                        int px1 = (int)protoX;
                        int py1 = (int)protoY;
                        int px2 = Math.min(px1 + 1, 159);
                        int py2 = Math.min(py1 + 1, 159);

                        float dx = protoX - px1;
                        float dy = protoY - py1;

                        for (int j = 0; j < 32; j++) {
                            float v11 = protoData[j * 160 * 160 + py1 * 160 + px1];
                            float v12 = protoData[j * 160 * 160 + py2 * 160 + px1];
                            float v21 = protoData[j * 160 * 160 + py1 * 160 + px2];
                            float v22 = protoData[j * 160 * 160 + py2 * 160 + px2];

                            float interpolated =
                                    v11 * (1 - dx) * (1 - dy) +
                                            v21 * dx * (1 - dy) +
                                            v12 * (1 - dx) * dy +
                                            v22 * dx * dy;

                            maskValue += maskCoeffs[j] * interpolated;
                        }
                        mask[y][x] = sigmoid(maskValue);
                    }
                }

                float cx = predictionData[bestIndex];
                float cy = predictionData[8400 + bestIndex];
                float w = predictionData[2 * 8400 + bestIndex];
                float h = predictionData[3 * 8400 + bestIndex];

                int imgWidth = originalBitmap.getWidth();
                int imgHeight = originalBitmap.getHeight();

                int x1 = (int)((cx - w/2) * imgWidth / 640);
                int y1 = (int)((cy - h/2) * imgHeight / 640);
                int x2 = (int)((cx + w/2) * imgWidth / 640);
                int y2 = (int)((cy + h/2) * imgHeight / 640);

                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setAlpha(150);

                int margin = 15;
                float baseThreshold = 0.45f;
                float strictThreshold = 0.65f;
                float localStdDevThreshold = 0.2f;
                int kernelSize = 1;  // 用於 3x3 局部區域

                // 首先計算整體遮罩區域的統計信息
                float maxMaskValue = 0;
                float avgMaskValue = 0;
                int maskCount = 0;

                for (int y = y1; y < y2; y++) {
                    for (int x = x1; x < x2; x++) {
                        float maskX = (float)x / imgWidth * maskSize;
                        float maskY = (float)y / imgHeight * maskSize;

                        int mx = Math.min(Math.max(0, (int)maskX), maskSize-1);
                        int my = Math.min(Math.max(0, (int)maskY), maskSize-1);

                        maxMaskValue = Math.max(maxMaskValue, mask[my][mx]);
                        avgMaskValue += mask[my][mx];
                        maskCount++;
                    }
                }
                avgMaskValue /= maskCount; // 避免除以零

                // 計算自適應閾值
                float adaptiveThreshold = (avgMaskValue + maxMaskValue) / 2;

                // 應用遮罩
                for (int y = Math.max(0, y1-margin); y < Math.min(imgHeight, y2+margin); y++) {
                    for (int x = Math.max(0, x1-margin); x < Math.min(imgWidth, x2+margin); x++) {
                        float maskX = (float)x / imgWidth * maskSize;
                        float maskY = (float)y / imgHeight * maskSize;

                        int mx = Math.min(Math.max(0, (int)maskX), maskSize-1);
                        int my = Math.min(Math.max(0, (int)maskY), maskSize-1);

                        // 計算到邊界的距離
                        float distToEdgeX = Math.min(Math.abs(x - x1), Math.abs(x - x2));
                        float distToEdgeY = Math.min(Math.abs(y - y1), Math.abs(y - y2));
                        float distToEdge = Math.min(distToEdgeX, distToEdgeY);

                        // 根據到邊界的距離調整閾值
                        float dynamicThreshold = baseThreshold;
                        if (distToEdge < margin) {
                            dynamicThreshold = baseThreshold +
                                    (strictThreshold - baseThreshold) *
                                            (1 - distToEdge / margin);
                        }

                        // 計算3x3區域的平均值和標準差
                        float localAvg = 0;
                        float localStdDev = 0;
                        int count = 0;
                        float[] values = new float[9];

                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                int nmx = mx + dx;
                                int nmy = my + dy;
                                if (nmx >= 0 && nmx < maskSize &&
                                        nmy >= 0 && nmy < maskSize) {
                                    values[count] = mask[nmy][nmx];
                                    localAvg += values[count];
                                    count++;
                                }
                            }
                        }
                        localAvg /= count;

                        for (int i = 0; i < count; i++) {
                            localStdDev += Math.pow(values[i] - localAvg, 2);
                        }
                        localStdDev = (float)Math.sqrt(localStdDev / count);

                        // 使用多個條件來決定是否繪製像素
                        boolean shouldDraw =
                                mask[my][mx] > dynamicThreshold &&  // 基本閾值檢查
                                        mask[my][mx] > adaptiveThreshold && // 自適應閾值檢查
                                        localAvg > baseThreshold &&         // 局部平均值檢查
                                        localStdDev < 0.2;                 // 局部一致性檢查

                        if (shouldDraw) {
                            canvas.drawPoint(x, y, paint);
                        }
                    }
                }
            }

            segmentedImageView.setImageBitmap(resultBitmap);

        } catch (Exception e) {
            Log.e("TestCameraActivity", "Error:", e);
        }
    }

    private float sigmoid(float x) {
        return (float)(1.0 / (1.0 + Math.exp(-x)));
    }
    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // 添加 floodFill 輔助方法
    private int floodFill(float[][] mask, boolean[][] visited, int[][] labels,
                          int x, int y, int label, float threshold) {
        if (x < 0 || x >= mask[0].length || y < 0 || y >= mask.length ||
                visited[y][x] || mask[y][x] <= threshold) {
            return 0;
        }

        visited[y][x] = true;
        labels[y][x] = label;
        int size = 1;

        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            size += floodFill(mask, visited, labels, x + dx[i], y + dy[i], label, threshold);
        }

        return size;
    }

    private void processModelOutput(Tensor predictions, Tensor proto, Bitmap originalBitmap) {
        float[] predictionData = predictions.getDataAsFloatArray();
        float[] protoData = proto.getDataAsFloatArray();

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        List<Detection> detections = parseDetections(predictionData, width, height);
        Bitmap maskBitmap = createSegmentationMask(detections, protoData, width, height);

        Bitmap resultBitmap = overlayMaskOnImage(originalBitmap, maskBitmap);
        segmentedImageView.setImageBitmap(resultBitmap);
        resultTextView.setText("辨識完成");
    }

    private static class Detection {
        float[] bbox;  // [x1, y1, x2, y2]
        float confidence;
        float[] maskCoeffs;

        Detection(float[] bbox, float confidence, float[] maskCoeffs) {
            this.bbox = bbox;
            this.confidence = confidence;
            this.maskCoeffs = maskCoeffs;
        }
    }

    private List<Detection> parseDetections(float[] predictions, int width, int height) {
        List<Detection> detections = new ArrayList<>();
        int numPredictions = 8400;
        float confidenceThreshold = 0.5f;

        for (int i = 0; i < numPredictions; i++) {
            float confidence = predictions[4 * numPredictions + i];
            if (confidence > confidenceThreshold) {
                // 提取邊界框座標
                float[] bbox = new float[4];
                float cx = predictions[i];
                float cy = predictions[numPredictions + i];
                float w = predictions[2 * numPredictions + i];
                float h = predictions[3 * numPredictions + i];

                // 轉換為座標
                bbox[0] = (cx - w/2) * width;  // x1
                bbox[1] = (cy - h/2) * height; // y1
                bbox[2] = (cx + w/2) * width;  // x2
                bbox[3] = (cy + h/2) * height; // y2

                // 提取遮罩係數
                float[] maskCoeffs = new float[32];
                for (int j = 0; j < 32; j++) {
                    maskCoeffs[j] = predictions[(5 + j) * numPredictions + i];
                }

                detections.add(new Detection(bbox, confidence, maskCoeffs));
            }
        }

        return detections;
    }

    // 修改 createSegmentationMask 方法
    private Bitmap createSegmentationMask(List<Detection> detections, float[] proto,
                                          int width, int height) {
        Bitmap maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(128);

        int protoH = 160, protoW = 160;

        for (Detection detection : detections) {
            float[] bbox = detection.bbox;
            float[] maskCoeffs = detection.maskCoeffs;

            int x1 = Math.max(0, (int)bbox[0]);
            int y1 = Math.max(0, (int)bbox[1]);
            int x2 = Math.min(width-1, (int)bbox[2]);
            int y2 = Math.min(height-1, (int)bbox[3]);

            // 直接在每個像素上計算遮罩值
            for (int y = y1; y < y2; y++) {
                for (int x = x1; x < x2; x++) {
                    float px = (float)(x - x1) / (x2 - x1) * protoW;
                    float py = (float)(y - y1) / (y2 - y1) * protoH;

                    int protoX = Math.min(Math.max(0, (int)px), protoW-1);
                    int protoY = Math.min(Math.max(0, (int)py), protoH-1);

                    float maskValue = 0;
                    for (int j = 0; j < 32; j++) {
                        int protoIdx = j * protoH * protoW + protoY * protoW + protoX;
                        if (protoIdx < proto.length) {
                            maskValue += maskCoeffs[j] * proto[protoIdx];
                        }
                    }

                    if (sigmoid(maskValue) > 0.5f) {
                        maskBitmap.setPixel(x, y, paint.getColor());
                    }
                }
            }
        }

        return maskBitmap;
    }

    // 修改 computeMaskValue 方法
    private float computeMaskValue(int x, int y, int x1, int y1, int x2, int y2,
                                   float[] maskCoeffs, float[] proto,
                                   int protoW, int protoH) {
        float px = (float)(x - x1) / (x2 - x1) * protoW;
        float py = (float)(y - y1) / (y2 - y1) * protoH;

        int protoX = Math.min(Math.max(0, (int)px), protoW-1);
        int protoY = Math.min(Math.max(0, (int)py), protoH-1);

        float maskValue = 0;
        for (int j = 0; j < 32; j++) {
            int protoIdx = j * protoH * protoW + protoY * protoW + protoX;
            if (protoIdx < proto.length) {
                maskValue += maskCoeffs[j] * proto[protoIdx];
            }
        }
        return sigmoid(maskValue);
    }

    private Bitmap overlayMaskOnImage(Bitmap original, Bitmap mask) {
        Bitmap resultBitmap = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setAlpha(100);
        canvas.drawBitmap(mask, 0, 0, paint);
        return resultBitmap;
    }
}