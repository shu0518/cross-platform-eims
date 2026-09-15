package com.example.a0731;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WindowDetectionActivity extends AppCompatActivity {
    private static final String TAG = "WindowDetectionActivity";
    private static final int MODEL_INPUT_SIZE = 640;
    private static final String MODEL_NAME = "model_1119.torchscript.pt";

    private Module module;
    private TextView resultTextView;
    private ExecutorService executor;
    private ProgressDialog progressDialog;

    static {
        try {
            System.loadLibrary("pytorch_jni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new View(this);
        view.setBackgroundColor(Color.WHITE);
        setContentView(view);

        // 初始化執行序池
        executor = Executors.newSingleThreadExecutor();

        // 初始化進度對話框
        setupProgressDialog();

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            // 開始檢測流程
            startDetection(Uri.parse(imageUriString));
        } else {
            finishWithError("No image URI provided");
        }
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在處理圖片...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void startDetection(Uri imageUri) {
        executor.execute(() -> {
            try {
                // 步驟1: 載入模型
                updateProgress("載入模型中...");
                String modelPath = assetFilePath(this, MODEL_NAME);
                Log.d(TAG, "Model path: " + modelPath);
                Log.d(TAG, "Model file exists: " + new File(modelPath).exists());
                Log.d(TAG, "Model file size: " + new File(modelPath).length());
                module = Module.load(modelPath);
                if (module == null) {
                    throw new RuntimeException("模型載入失敗");
                }
                Log.d(TAG, "Model loaded successfully");

                // 步驟2: 讀取圖片
                updateProgress("讀取圖片...");
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);
                imageStream.close();

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        MODEL_INPUT_SIZE,
                        MODEL_INPUT_SIZE,
                        true
                );

                // 步驟3: 執行辨識
                updateProgress("執行辨識中...");
                Bitmap resultBitmap = runInference(resizedBitmap, originalBitmap);

                if (resultBitmap != null) {
                    // 保存處理後的圖片
                    Uri processedUri = saveProcessedImage(resultBitmap);

                    // 返回結果
                    runOnUiThread(() -> {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("processedImageUri", processedUri.toString());
                        setResult(RESULT_OK, resultIntent);
                        dismissProgressDialog();
                        finish();
                    });
                } else {
                    throw new RuntimeException("Detection failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in detection", e);
                runOnUiThread(() -> {
                    finishWithError("處理失敗: " + e.getMessage());
                });
            }
        });
    }

    private Bitmap runInference(Bitmap inputBitmap, Bitmap originalBitmap) {
        try {
            if (module == null) {
                Log.e(TAG, "Module is null");
                return null;
            }

            updateProgress("正在分析圖片...");  // 增加更多進度提示

            // 轉換輸入格式
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    inputBitmap,
                    new float[]{0,0,0},
                    new float[]{1,1,1}
            );

            updateProgress("辨識中...");  // 增加更多進度提示

            // 執行模型推論
            IValue[] outputs = module.forward(IValue.from(inputTensor)).toTuple();
            float[] predictionData = outputs[0].toTensor().getDataAsFloatArray();
            float[] protoData = outputs[1].toTensor().getDataAsFloatArray();

            updateProgress("處理檢測結果中...");  // 增加更多進度提示

            // 找到最佳預測
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
                updateProgress("辨識好了...");  // 增加更多進度提示

                // 提取遮罩係數
                float[] maskCoeffs = new float[32];
                for (int j = 0; j < 32; j++) {
                    maskCoeffs[j] = predictionData[(5 + j) * 8400 + bestIndex];
                }

                // 生成遮罩
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

                updateProgress("就快好了...");  // 增加更多進度提示

                // 提取座標
                float cx = predictionData[bestIndex];
                float cy = predictionData[8400 + bestIndex];
                float w = predictionData[2 * 8400 + bestIndex];
                float h = predictionData[3 * 8400 + bestIndex];

                // 轉換到原始圖片尺寸
                int imgWidth = originalBitmap.getWidth();
                int imgHeight = originalBitmap.getHeight();
                int x1 = (int)((cx - w/2) * imgWidth / 640);
                int y1 = (int)((cy - h/2) * imgHeight / 640);
                int x2 = (int)((cx + w/2) * imgWidth / 640);
                int y2 = (int)((cy + h/2) * imgHeight / 640);

                // 創建結果圖片
                Bitmap resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(resultBitmap);
                Paint paint = new Paint();
                paint.setColor(Color.rgb(153, 153, 204));

                // 繪製遮罩點
                ArrayList<MaskInfo.Point> maskPoints = new ArrayList<>();
                int margin = 20;
                float threshold = 0.45f;

                for (int y = Math.max(0, y1-margin); y < Math.min(imgHeight, y2+margin); y++) {
                    for (int x = Math.max(0, x1-margin); x < Math.min(imgWidth, x2+margin); x++) {
                        float maskX = (float)x / imgWidth * maskSize;
                        float maskY = (float)y / imgHeight * maskSize;

                        int mx = Math.min(Math.max(0, (int)maskX), maskSize-1);
                        int my = Math.min(Math.max(0, (int)maskY), maskSize-1);

                        if (mask[my][mx] > threshold) {
                            maskPoints.add(new MaskInfo.Point(x, y));
                            canvas.drawPoint(x, y, paint);
                        }
                    }
                }

                updateProgress("真的要好了...");  // 增加更多進度提示

                // 儲存遮罩資訊
                saveWindowInfo(x1, y1, x2, y2, maskPoints);

                updateProgress("完成辨識！");  // 增加完成提示

                return resultBitmap;
            } else {
                updateProgress("未檢測到窗戶...");  // 增加失敗提示
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in inference", e);
            updateProgress("處理發生錯誤: " + e.getMessage());  // 增加錯誤提示
        }
        return null;
    }

    private float sigmoid(float x) {
        return (float)(1.0 / (1.0 + Math.exp(-x)));
    }

    private String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
            return file.getAbsolutePath();
        }
    }

    private Uri saveProcessedImage(Bitmap bitmap) throws IOException {
        File outputDir = new File(getExternalCacheDir(), "processed_images");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDir, "processed_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        return Uri.fromFile(outputFile);
    }

    private void saveWindowInfo(int x1, int y1, int x2, int y2, ArrayList<MaskInfo.Point> maskPoints) {
        try {
            File infoFile = new File(getExternalCacheDir(), "window_info.txt");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(infoFile))) {
                MaskInfo maskInfo = new MaskInfo(x1, y1, x2, y2, maskPoints);
                oos.writeObject(maskInfo);
                Log.d(TAG, "Saved window info: position=(" + x1 + "," + y1 + "), size=" + (x2-x1) + "x" + (y2-y1));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving mask info", e);
        }
    }

    private void updateProgress(String message) {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.setMessage(message);
            }
            if (resultTextView != null) {
                resultTextView.setText(message);
            }
            Log.d(TAG, "Progress: " + message);  // 添加日誌記錄
        });
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void finishWithError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void processImage(Uri imageUri) {
        try {
            // 讀取圖片
            InputStream is = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(is);
            if (is != null) {
                is.close();
            }

            // 調整圖片大小用於模型輸入
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    MODEL_INPUT_SIZE,
                    MODEL_INPUT_SIZE,
                    true
            );

            // 執行模型推論
            Bitmap resultBitmap = runInference(resizedBitmap, originalBitmap);

            // 保存結果圖片
            if (resultBitmap != null) {
                File outputFile = new File(getExternalCacheDir(), "processed_image.jpg");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }

                // 返回結果
                Intent resultIntent = new Intent();
                resultIntent.putExtra("processedImageUri", Uri.fromFile(outputFile).toString());
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_CANCELED);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            setResult(RESULT_CANCELED);
        } finally {
            finish();
        }
    }
}
