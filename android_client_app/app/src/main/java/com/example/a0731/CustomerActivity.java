package com.example.a0731;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
public class CustomerActivity extends AppCompatActivity {

    private RecyclerView customerRecyclerView;
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList;
    private ImageButton profileButton, clearButton;
    private EditText searchEditText;
    private TextView saveaccount;
    private static final int PICK_IMAGE = 1;  // 定義請求碼
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一頁
            }
        });

        // 獲取傳遞過來的承包商名字
        TextView contractorNameTextView = findViewById(R.id.contractorNameTextView);
        String contractorName = getIntent().getStringExtra("contractor_name");
        contractorNameTextView.setText(contractorName);  // 將承包商名稱設置到 TextView 中
        Intent intent = getIntent();
        String account = intent.getStringExtra("account");
        saveaccount=findViewById(R.id.account);
        saveaccount.setText(account);

        // 設置 profileButton 的點擊事件，導向承包商資料頁面
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerActivity.this, ContractorProfileActivity.class);
                intent.putExtra("contractorName", contractorName);
                startActivity(intent);
            }
        });
        // 查找並設置查詢按鈕的點擊事件
        searchEditText = findViewById(R.id.searchEditText);
        clearButton = findViewById(R.id.clearButton);
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
        // 點擊清除按鈕清空搜尋框
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");  // 清空文字
            }
        });
        // 初始化 RecyclerView
        customerRecyclerView = findViewById(R.id.customerRecyclerView);
        customerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 查詢資料庫並顯示客戶資料
        new FetchCustomerDataTask().execute(contractorName, "");


    }
    // 自訂搜尋圖示被點擊時的行為
    private void onSearchIconClicked() {
        // 執行搜尋任務
        TextView contractorNameTextView = findViewById(R.id.contractorNameTextView);
        String contractorName = contractorNameTextView.getText().toString();
        String searchQuery = searchEditText.getText().toString().trim();
        new FetchCustomerDataTask().execute(contractorName, searchQuery);
    }
    private void filter(String text) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : customerList) {
            if (customer.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(customer);  // 匹配的客戶
            }
        }

    }
    // 異步任務查詢客戶資料
    private class FetchCustomerDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String contractorName = params[0];
            String searchQuery = params[1];
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/fetch_clients.php");  // 替換為你的PHP伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送 contractor_name 和 searchQuery 到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("contractor_name=" + contractorName + "&searchQuery=" + searchQuery);
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
                customerList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject customerObject = jsonArray.getJSONObject(i);
                    String clientName = customerObject.getString("client_name");
                    String address = customerObject.getString("client_address");
                    String phone = customerObject.getString("client_phone");
                    customerList.add(new Customer(clientName, address, phone));
                }
                String account=saveaccount.getText().toString();
                customerAdapter = new CustomerAdapter(CustomerActivity.this, customerList,account);
                customerRecyclerView.setAdapter(customerAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
