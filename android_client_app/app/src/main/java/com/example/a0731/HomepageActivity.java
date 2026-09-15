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
import android.widget.ImageView;
import android.widget.TextView;
import com.example.a0731.SessionManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private RecyclerView contractorRecyclerView;
    private ContractorAdapter contractorAdapter;
    private List<Contractor> contractorList;
    private EditText searchEditText;
    private ImageButton clearButton;
    private ImageButton profileButton;
    private ImageButton logoutButton;
    private TextView  saveaccount;
    private SessionManager sessionManager; //檢查登入狀態

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        sessionManager = new SessionManager(this);

        // 檢查登入狀態
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 獲取從 MainActivity 傳過來的帳號資訊
        Intent intent = getIntent();
        String account = intent.getStringExtra("account");

        // 查找並設置登出按鈕的點擊事件
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();  // 調用登出方法
            }
        });

        // 查找並設置 profileButton 的點擊事件
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, UserProfileActivity.class);
                intent.putExtra("account", getIntent().getStringExtra("account"));
                startActivity(intent);
            }
        });

        // 初始化 RecyclerView
        contractorRecyclerView = findViewById(R.id.contractorRecyclerView);
        contractorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化視圖
        searchEditText = findViewById(R.id.searchEditText);
        saveaccount=findViewById(R.id.account);
        saveaccount.setText(account);

        // 查詢 contractor 資料並設置到 RecyclerView
        new FetchContractorDataTask().execute();

        // 查找並設置搜尋框及清除按鈕
        searchEditText = findViewById(R.id.searchEditText);
        clearButton = findViewById(R.id.clearButton);

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

        // 點擊清除按鈕清空搜尋框
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");  // 清空文字
            }
        });
    }

    // 自訂搜尋圖示被點擊時的行為
    private void onSearchIconClicked() {
        // 執行搜尋任務
        String searchQuery = searchEditText.getText().toString().trim();
        new FetchContractorDataTask().execute(searchQuery);
    }

    private void filter(String text) {
        List<Contractor> filteredList = new ArrayList<>();
        for (Contractor contractor : contractorList) {
            if (contractor.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(contractor);  // 匹配的承包商
            }
        }

    }
    // 登出方法
    private void logout() {
        // 使用 SessionManager 清除登入狀態
        sessionManager.logout();

        // 清除用戶數據或執行其他登出邏輯
        Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();  // 結束當前活動
    }
    // 異步任務查詢 contractor 資料
    private class FetchContractorDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String searchQuery = params.length > 0 ? params[0] : ""; // 取得搜尋字串，若無搜尋字串則為空
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/fetch_contractor_data.php");  // 替換為你的PHP伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送搜尋字串到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("searchQuery=" + searchQuery);
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
                // 將 contractor 資料設置到 RecyclerView
                contractorList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contractorObject = jsonArray.getJSONObject(i);
                    String contractorName = contractorObject.getString("contract_name");
                    String phone = contractorObject.getString("contract_phone");
                    contractorList.add(new Contractor(contractorName, phone));
                }

                String account=saveaccount.getText().toString();
                contractorAdapter = new ContractorAdapter(HomepageActivity.this, contractorList, account);
                contractorRecyclerView.setAdapter(contractorAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
