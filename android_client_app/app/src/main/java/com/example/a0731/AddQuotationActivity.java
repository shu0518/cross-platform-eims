package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AddQuotationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProcessingDataAdapter processingDataAdapter;
    private List<ProcessingData> processingDataList;
    private CheckBox selectAllCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quotation);

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化假加工數據列表
        processingDataList = new ArrayList<>();
//        processingDataList.add(new ProcessingData("A1", "1樓主臥", "主臥"));
//        processingDataList.add(new ProcessingData("A2", "2樓客廳", "客廳"));

        // 初始化全選 CheckBox
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (ProcessingData data : processingDataList) {
                data.setSelected(isChecked);
            }
            processingDataAdapter.notifyDataSetChanged();
        });

        // 初始化適配器並設置給 RecyclerView
//        processingDataAdapter = new ProcessingDataAdapter(this, processingDataList, true, selectAllCheckBox);
//        recyclerView.setAdapter(processingDataAdapter);

        // 新增報價單按鈕的點擊事件
        ImageButton generateQuotationButton = findViewById(R.id.generateQuotationButton);
        generateQuotationButton.setOnClickListener(v -> {
            // 跳轉到報價單詳細頁面
            Intent intent = new Intent(AddQuotationActivity.this, QuotationDetailActivity.class);
            startActivity(intent);
        });
    }
}
