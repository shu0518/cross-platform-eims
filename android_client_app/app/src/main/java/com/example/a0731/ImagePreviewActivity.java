package com.example.a0731;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePreviewActivity extends AppCompatActivity {
    private static final String TAG = "ImagePreviewActivity";
    private static final int WINDOW_DETECTION_REQUEST = 2;

    private ImageView previewImageView;
    private ResizableImageView styleImageView;
    private FrameLayout imageContainer;
    private RecyclerView modelRecyclerView;
    private ModelAdapter modelAdapter;
    private List<Bitmap> modelImages;
    private TouchHandler touchHandler;
    private LinearLayout modelSelectionLayout;
    private FrameLayout trashContainer;
    private View headerLayout;
    private ImageButton previewButton;
    private boolean isPreviewMode = false;
    private List<View> viewsToHide;
    private MaskInfo maskInfo;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        executor = Executors.newSingleThreadExecutor();
        initializeViews();
        setupModelSelection();
        startWindowDetection();
    }

    private void initializeViews() {
        previewImageView = findViewById(R.id.previewImageView);
        imageContainer = findViewById(R.id.imageContainer);
        modelRecyclerView = findViewById(R.id.modelRecyclerView);
        modelSelectionLayout = findViewById(R.id.modelSelectionLayout);
        trashContainer = findViewById(R.id.trashContainer);
        headerLayout = findViewById(R.id.headerLayout);
        previewButton = findViewById(R.id.previewButton);

        modelSelectionLayout.setVisibility(View.GONE);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // 初始化需要隱藏的視圖列表
        viewsToHide = new ArrayList<>();
        viewsToHide.add(headerLayout);
        viewsToHide.add(modelSelectionLayout);
        viewsToHide.add(trashContainer);

        // 設置預覽按鈕點擊事件
        previewButton.setOnClickListener(v -> togglePreviewMode());
    }

    private void setupModelSelection() {
        modelImages = new ArrayList<>();
        modelAdapter = new ModelAdapter(modelImages, this::applyStyle);
        modelRecyclerView.setAdapter(modelAdapter);
        modelRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        findViewById(R.id.style2Button).setOnClickListener(v -> loadStyles("2la"));
        findViewById(R.id.style3Button).setOnClickListener(v -> loadStyles("3la"));
        findViewById(R.id.style4Button).setOnClickListener(v -> loadStyles("4la"));
    }

    private void togglePreviewMode() {
        isPreviewMode = !isPreviewMode;

        // 更新按鈕圖標
        previewButton.setImageResource(isPreviewMode ?
                R.drawable.ic_eye_off :
                R.drawable.ic_eye);

        // 切換視圖可見性
        for (View view : viewsToHide) {
            if (view != null && view != previewButton) {
                view.animate()
                        .alpha(isPreviewMode ? 0f : 1f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            view.setVisibility(isPreviewMode ? View.GONE : View.VISIBLE);
                        })
                        .start();
            }
        }

        // 更新 ResizableImageView 的預覽模式
        if (styleImageView != null) {
            styleImageView.setPreviewMode(isPreviewMode);
        }

        // 禁用/啟用觸控
        if (touchHandler != null) {
            touchHandler.setEnabled(!isPreviewMode);
        }

        // 確保預覽按鈕始終可見
        previewButton.setVisibility(View.VISIBLE);
        previewButton.setEnabled(true);
        previewButton.bringToFront();
    }

    private void startWindowDetection() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Intent detectionIntent = new Intent(this, WindowDetectionActivity.class);
            detectionIntent.putExtra("imageUri", imageUriString);
            startActivityForResult(detectionIntent, WINDOW_DETECTION_REQUEST);
        } else {
            Toast.makeText(this, "No image URI provided", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);  // 取消返回時的動畫
    }

    private void loadMaskInfo() {
        try {
            File infoFile = new File(getExternalCacheDir(), "window_info.txt");
            if (!infoFile.exists()) {
                Log.e(TAG, "Mask info file does not exist");
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(infoFile))) {
                maskInfo = (MaskInfo) ois.readObject();
                if (maskInfo != null) {
                    Log.d(TAG, "Loaded window info: position=(" + maskInfo.x1 + "," + maskInfo.y1 +
                            "), size=" + maskInfo.width + "x" + maskInfo.height);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading mask info", e);
            maskInfo = null;
        }
    }

    private void applyStyle(Bitmap styleBitmap) {
        if (maskInfo == null) {
            loadMaskInfo();
        }

        if (maskInfo == null) {
            Toast.makeText(this, "無法取得窗戶位置資訊", Toast.LENGTH_SHORT).show();
            return;
        }

        if (styleImageView != null) {
            imageContainer.removeView(styleImageView);
        }

        styleImageView = new ResizableImageView(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                maskInfo.width,
                maskInfo.height
        );
        params.leftMargin = maskInfo.x1;
        params.topMargin = maskInfo.y1;

        styleImageView.setLayoutParams(params);
        styleImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Bitmap scaledStyle = Bitmap.createScaledBitmap(
                styleBitmap,
                maskInfo.width,
                maskInfo.height,
                true
        );

        styleImageView.setImageBitmap(scaledStyle);
        imageContainer.addView(styleImageView);

        touchHandler = new TouchHandler(trashContainer, modelSelectionLayout, styleImageView);
        styleImageView.setOnTouchListener(touchHandler);

        // 如果當前是預覽模式，需要立即設置預覽狀態
        if (isPreviewMode) {
            styleImageView.setPreviewMode(true);
            touchHandler.setEnabled(false);
        }
    }

    private void loadStyles(String folder) {
        modelImages.clear();
        try {
            String[] files = getAssets().list("models/" + folder);
            for (String file : files) {
                try (InputStream is = getAssets().open("models/" + folder + "/" + file)) {
                    modelImages.add(BitmapFactory.decodeStream(is));
                }
            }
            modelAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            Log.e(TAG, "Error loading styles from " + folder, e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WINDOW_DETECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            String processedImageUri = data.getStringExtra("processedImageUri");
            if (processedImageUri != null) {
                displayImage(Uri.parse(processedImageUri));
                loadMaskInfo();
                Log.d(TAG, "Processing completed, maskInfo loaded: " + (maskInfo != null));
                modelSelectionLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void displayImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                previewImageView.setImageBitmap(bitmap);
                is.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error displaying image", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}