package com.example.a0731;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class ProcessingDetailActivity extends AppCompatActivity {

    private TextView pageNumberTextView;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_data_detail);

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProcessingDetailActivity.this, ProcessingDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent); // 返回到 ProcessingDataActivity
            }
        });

        // 初始化翻頁邏輯
        pageNumberTextView = findViewById(R.id.pageNumberTextView);

        ImageButton previousButton = findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 1) {
                    currentPage--;
                    updatePage();
                }
            }
        });

        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage++;
                updatePage();
            }
        });
    }

    private void updatePage() {
        pageNumberTextView.setText("第" + currentPage + "筆");
        // 這裡需要根據頁碼更新顯示的數據
    }
}