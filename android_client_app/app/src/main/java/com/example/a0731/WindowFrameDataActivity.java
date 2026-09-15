package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class WindowFrameDataActivity extends AppCompatActivity {

    private RecyclerView windowFrameRecyclerView;
    private WindowFrameAdapter windowFrameAdapter;
    private List<WindowFrame> windowFrameList;
    private List<WindowFrame> filteredWindowFrameList; // 用於搜尋結果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window_frame_data);

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一頁
            }
        });

        // 初始化RecyclerView
        windowFrameRecyclerView = findViewById(R.id.windowFrameRecyclerView);
        windowFrameRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化搜尋欄
        SearchView searchView = findViewById(R.id.searchView);

        // 預設一些窗框數據
        windowFrameList = new ArrayList<>();
        windowFrameList.add(new WindowFrame("A1", "1樓", "主臥", "100cm", "200cm", "2", "窗戶型號A"));
        windowFrameList.add(new WindowFrame("A2", "2樓", "客廳", "150cm", "250cm", "1", "窗戶型號B"));

        // 複製原始數據
        filteredWindowFrameList = new ArrayList<>(windowFrameList);

        // 設置適配器
        windowFrameAdapter = new WindowFrameAdapter(filteredWindowFrameList);
        windowFrameRecyclerView.setAdapter(windowFrameAdapter);

        // 搜尋功能
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterWindowFrameData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterWindowFrameData(newText);
                return false;
            }
        });

        // 新增窗框數據的按鈕
        FloatingActionButton addWindowFrameButton = findViewById(R.id.addWindowFrameButton);
        addWindowFrameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到新增窗框數據的頁面
                Intent intent = new Intent(WindowFrameDataActivity.this, AddMeasuredDataActivity.class);
                startActivity(intent);
            }
        });
    }

    // 搜尋過濾功能
    private void filterWindowFrameData(String query) {
        filteredWindowFrameList.clear();
        if (query.isEmpty()) {
            filteredWindowFrameList.addAll(windowFrameList); // 如果搜尋欄為空，顯示所有數據
        } else {
            for (WindowFrame frame : windowFrameList) {
                if (frame.getFrameNumber().toLowerCase().contains(query.toLowerCase()) ||
                        frame.getFloor().toLowerCase().contains(query.toLowerCase()) ||
                        frame.getLayout().toLowerCase().contains(query.toLowerCase())) {
                    filteredWindowFrameList.add(frame);
                }
            }
        }
        windowFrameAdapter.notifyDataSetChanged(); // 更新RecyclerView顯示
    }
}
