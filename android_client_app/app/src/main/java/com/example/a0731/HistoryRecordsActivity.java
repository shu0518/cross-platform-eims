package com.example.a0731;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class HistoryRecordsActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryRecordsAdapter historyRecordsAdapter;
    private List<HistoryRecord> historyRecordsList;
    private List<HistoryRecord> filteredHistoryRecordsList; // 用於搜尋結果
    private EditText searchEditText;
    private ImageButton clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_records);

        // 獲取從 CustomerFunction 傳過來的地址資訊
        String customer_Address = getIntent().getStringExtra("customer_Address");
        String customer_name = getIntent().getStringExtra("customer_name");
        String account = getIntent().getStringExtra("account");

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化RecyclerView
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecordsList = new ArrayList<>();
        historyRecordsAdapter = new HistoryRecordsAdapter(HistoryRecordsActivity.this, historyRecordsList, account,true);
        historyRecyclerView.setAdapter(historyRecordsAdapter);

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

//        // 點擊歷史單據列表中的單筆資料時，跳轉到報價單詳細頁面
//        historyRecordsAdapter.setOnItemClickListener(historyRecord -> {
//            Intent intent = new Intent(HistoryRecordsActivity.this, QuotationDetailActivity.class);
//            startActivity(intent);
//        });
        // 查詢資料庫並顯示窗框數據
        new FetchMeasuredDataTask().execute(customer_Address, "");

        // 新增加單據的按鈕
        FloatingActionButton addDocumentButton = findViewById(R.id.addDocumentButton);
        addDocumentButton.setOnClickListener(v -> showBottomSheetDialog());
    }
    // 自訂搜尋圖示被點擊時的行為
    private void onSearchIconClicked() {
        // 執行搜尋任務
        String customer_Address = getIntent().getStringExtra("customer_Address");
        String searchQuery = searchEditText.getText().toString().trim();
        new FetchMeasuredDataTask().execute(customer_Address, searchQuery);
    }
    private void filter(String text) {
        List<HistoryRecord> filteredList = new ArrayList<>();
        for (HistoryRecord historyRecord : historyRecordsList) {
            if (historyRecord.getLocation().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(historyRecord);
            }
        }
    }
    private void showBottomSheetDialog() {
        String customer_Address = getIntent().getStringExtra("customer_Address");
        String account = getIntent().getStringExtra("account");
        List<String> selectedIds = historyRecordsAdapter.getSelectedIds(); // 獲取選取的 ID

        if (selectedIds.isEmpty()) {
            Toast.makeText(HistoryRecordsActivity.this, "請選擇至少一個項目", Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<String> specifications = new ArrayList<>();
            ArrayList<String> lengths = new ArrayList<>();
            ArrayList<String> widths = new ArrayList<>();
            ArrayList<String> quantities = new ArrayList<>();
            ArrayList<String> spec = new ArrayList<>();

            // 獲取所選的歷史數據，並將資料儲存到 ArrayList
            for (String id : selectedIds) {
                HistoryRecord historyRecord = historyRecordsAdapter.getHistoryRecordById(id);
                if (historyRecord != null) {
                    specifications.add(historyRecord.getSpecification());
                    lengths.add(historyRecord.getLength());
                    widths.add(historyRecord.getWidth());
                    quantities.add(historyRecord.getQuantity());
                    spec.add(historyRecord.getspec());
                }
            }
            String customer_name = getIntent().getStringExtra("customer_name");
            // 準備傳遞到 AddQuotationActivity 的資料
            Intent intent = new Intent(HistoryRecordsActivity.this, QuotationDetailActivity.class);
            intent.putExtra("customer_Address", customer_Address);
            intent.putExtra("customer_name", customer_name);
            intent.putExtra("account", account);
            intent.putStringArrayListExtra("specifications", specifications);
            intent.putStringArrayListExtra("lengths", lengths);
            intent.putStringArrayListExtra("widths", widths);
            intent.putStringArrayListExtra("quantities", quantities);
            intent.putStringArrayListExtra("spec", spec);

            // 啟動 AddQuotationActivity
            startActivity(intent);
        }
    }

//    private void showBottomSheetDialog() {
//        // 創建 BottomSheetDialog
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HistoryRecordsActivity.this);
//
//        // 加載自定義布局
//        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
//                R.layout.layout_add_document_bottom_sheet,
//                null
//        );
//
//        // 綁定報價單按鈕
//        LinearLayout quotationButton = bottomSheetView.findViewById(R.id.quotationButton);
//        quotationButton.setOnClickListener(v -> {
//            // 跳轉到新增報價單的頁面
//            Intent intent = new Intent(HistoryRecordsActivity.this, AddQuotationActivity.class);
//            startActivity(intent);
//            bottomSheetDialog.dismiss();
//        });
//
//        // 設置為擴展模式
//        bottomSheetDialog.setOnShowListener(dialog -> {
//            BottomSheetDialog d = (BottomSheetDialog) dialog;
//            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//            if (bottomSheet != null) {
//                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
//                behavior.setPeekHeight(600);
//                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            }
//        });
//
//        // 設置視圖並顯示 BottomSheetDialog
//        bottomSheetDialog.setContentView(bottomSheetView);
//        bottomSheetDialog.show();
//    }

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
                historyRecordsList = new ArrayList<>();
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
                        historyRecordsList.add(new HistoryRecord(String.valueOf(id), specification, "門", position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), true));
                    } else if (specification.contains("窗")) {
                        String typePiece = new JSONObject(specification).getJSONObject("type").getString("piece");
                        historyRecordsList.add(new HistoryRecord(String.valueOf(id), specification, typePiece, position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), false));
                    }
                }
                historyRecordsAdapter = new HistoryRecordsAdapter(HistoryRecordsActivity.this,historyRecordsList, account,true);
                historyRecyclerView.setAdapter(historyRecordsAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}