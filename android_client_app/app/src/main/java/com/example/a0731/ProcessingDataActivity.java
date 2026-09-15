package com.example.a0731;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProcessingDataActivity extends AppCompatActivity {

    private RecyclerView processingRecyclerView;
    private ProcessingDataAdapter processingAdapter;
    private List<ProcessingData> processingDataList;
    private EditText searchEditText;
    private ImageButton clearButton; // 清除按鈕
    private FloatingActionButton addProcessingDataButton; // 新增數據按鈕

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_data);

        // 獲取從 CustomerFunction 傳過來的地址資訊
        String customer_Address = getIntent().getStringExtra("customer_Address");
        String account = getIntent().getStringExtra("account");

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化 RecyclerView
        processingRecyclerView = findViewById(R.id.processingDataRecyclerView);
        processingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        processingDataList = new ArrayList<>();
        // 設置適配器，傳遞 Context 和數據列表
        //CheckBox selectAllCheckBox = findViewById(R.id.selectAllCheckBox); // 獲取全選框
        processingAdapter = new ProcessingDataAdapter(ProcessingDataActivity.this, processingDataList, account,false);
        processingRecyclerView.setAdapter(processingAdapter);

        // 初始化搜尋欄和清除按鈕
        searchEditText = findViewById(R.id.searchEditText);
        clearButton = findViewById(R.id.clearButton);

        // 搜尋功能
        searchEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 檢查是否點擊在 drawableStart 的範圍內
                if (event.getX() <= (searchEditText.getCompoundDrawables()[0].getBounds().width() + searchEditText.getPaddingStart())) {
                    // 在此處處理圖示的點擊事件
                    onSearchIconClicked();
                    return true;  // 表示事件已處理，不再傳遞
                }
            }
            return false;  // 繼續傳遞事件
        });
        // 監聽搜尋框文字變化
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearButton.setVisibility(View.VISIBLE);  // 顯示清除按鈕
                    filter(s.toString());  // 過濾列表
                } else {
                    clearButton.setVisibility(View.GONE);  // 隱藏清除按鈕
                    filter("");  // 顯示所有數據
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 清除按鈕的邏輯
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");  // 清空文字
            }
        });

        // 查詢資料庫並顯示窗框數據
        new FetchMeasuredDataTask().execute(customer_Address, "");

        // 新增數據按鈕的點擊事件
        addProcessingDataButton = findViewById(R.id.addProcessingDataButton);
        addProcessingDataButton.setOnClickListener(v -> {
            // 跳轉到新增加工數據頁面
            Intent intent = new Intent(ProcessingDataActivity.this, AddProcessingDataActivity.class);
            intent.putExtra("customer_Address", customer_Address);
            intent.putExtra("account", account);
            startActivity(intent);
        });
    }
    // 自訂搜尋圖示被點擊時的行為
    private void onSearchIconClicked() {
        // 執行搜尋任務
        String customer_Address = getIntent().getStringExtra("customer_Address");
        String searchQuery = searchEditText.getText().toString().trim();
        new FetchMeasuredDataTask().execute(customer_Address, searchQuery);
    }
    private void filter(String text) {
        List<ProcessingData> filteredList = new ArrayList<>();
        for (ProcessingData processingData : processingDataList) {
            if (processingData.getLocation().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(processingData);
            }
        }

    }

    // 異步任務查詢窗框數據
    private class FetchMeasuredDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String customerAddress = params[0];
            String searchQuery = params[1];
            String result = "";

            try {
                URL url = new URL("http://163.17.135.120/api/fetch_measured_data.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送客戶地址到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("customer_address=" + customerAddress + "&searchQuery=" + searchQuery);
                writer.flush();
                writer.close();

                // 讀取伺服器的回應
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // 將客戶資料設置到 RecyclerView
                processingDataList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(result);
                String account = getIntent().getStringExtra("account");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject measureObject = jsonArray.getJSONObject(i);

                    // 取得需要的欄位
                    String specification = measureObject.getString("specification");
                    String position = measureObject.getString("position");
                    float length = (float) measureObject.getDouble("length");
                    float width = (float) measureObject.getDouble("width");
                    int quantity = measureObject.getInt("quantity");
                    int id = measureObject.getInt("measure_id");

                    // 根據 specification 的內容設置 MeasuredData*
                    if (specification.contains("門")) {
                        processingDataList.add(new ProcessingData(String.valueOf(id), specification, "門", position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), true));
                    } else if (specification.contains("窗")) {
                        String typePiece = new JSONObject(specification).getJSONObject("type").getString("piece");
                        processingDataList.add(new ProcessingData(String.valueOf(id), specification, typePiece, position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), false));
                    }
                }
                processingAdapter = new ProcessingDataAdapter(ProcessingDataActivity.this,processingDataList, account,false);
                processingRecyclerView.setAdapter(processingAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
