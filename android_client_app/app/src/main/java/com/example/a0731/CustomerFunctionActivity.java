package com.example.a0731;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;

public class CustomerFunctionActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 100;
    private static final String TAG = "CustomerFunctionActivity";

    private LinearLayout customerInfoEditLayout;
    private CardView windowFrameButton, documentButton, processingDataButton;
    private ImageButton cameraButton, homeButton;
    private FrameLayout exampleItem1, exampleItem2, exampleItem3, exampleItem4;
    private String customer_Address, customer_name, account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_function);

        // 獲取傳入的數據
        Intent intent = getIntent();
        customer_Address = intent.getStringExtra("customer_Address");
        customer_name = intent.getStringExtra("customer_name");
        account = intent.getStringExtra("account");

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化所有按鈕和區塊
        exampleItem1 = findViewById(R.id.exampleItem1);
        exampleItem2 = findViewById(R.id.exampleItem2);
        exampleItem3 = findViewById(R.id.exampleItem3);
        exampleItem4 = findViewById(R.id.exampleItem4);
        cameraButton = findViewById(R.id.cameraButton);
        homeButton = findViewById(R.id.homeButton);
    }

    private void setupClickListeners() {
        // 測量數據點擊事件
        exampleItem1.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerFunctionActivity.this, MeasuredDataActivity.class);
            intent.putExtra("customer_Address", customer_Address);
            intent.putExtra("account", account);
            startActivity(intent);
        });

        // 加工數據點擊事件
        exampleItem3.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerFunctionActivity.this, ProcessingDataActivity.class);
            intent.putExtra("customer_Address", customer_Address);
            intent.putExtra("account", account);
            startActivity(intent);
        });

        // 單據管理點擊事件
        exampleItem2.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerFunctionActivity.this, HistoryRecordsActivity.class);
            intent.putExtra("customer_Address", customer_Address);
            intent.putExtra("customer_name", customer_name);
            intent.putExtra("account", account);
            startActivity(intent);
        });

        // 客戶資訊點擊事件
        exampleItem4.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerFunctionActivity.this, CustomerProfileActivity.class);
            intent.putExtra("customer_name", customer_name);
            startActivity(intent);
        });

        // Camera 按鈕點擊事件 - 直接開啟相簿
        cameraButton.setOnClickListener(v -> {
            // 創建開啟相簿的 Intent
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, "選擇照片"), GALLERY_REQUEST);
        });

        // 返回首頁按鈕點擊事件
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerFunctionActivity.this, HomepageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void openGallery() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Log.d(TAG, "Opening gallery");
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error opening gallery: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted");
            } else {
                Log.d(TAG, "Permission denied");
                Toast.makeText(this, "需要儲存權限才能選擇照片", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    // 跳轉到預覽頁面
                    Intent previewIntent = new Intent(this, ImagePreviewActivity.class);
                    previewIntent.putExtra("imageUri", selectedImage.toString());
                    previewIntent.putExtra("customer_Address", customer_Address);
                    startActivity(previewIntent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling gallery result", e);
                Toast.makeText(this, "無法讀取所選照片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}