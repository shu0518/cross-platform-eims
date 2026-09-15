package com.example.a0731;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CustomerProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, addressEditText, emailEditText, business_numberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        Intent intent = getIntent();
        String customer_name = intent.getStringExtra("customer_name");
        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一頁
            }
        });
        // 初始化視圖
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        emailEditText = findViewById(R.id.emailEditText);
        business_numberEditText = findViewById(R.id.business_numberEditText);
        // 不可編輯狀態
        nameEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        addressEditText.setEnabled(false);
        emailEditText.setEnabled(false);
        business_numberEditText.setEnabled(false);
        // 預設資料
        nameEditText.setText(customer_name);
        new FetchCustomerProfileTask().execute(customer_name);
    }
    // 異步任務查詢客戶資料
    private class FetchCustomerProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String customer_name = params[0];
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/fetch_customer_profile.php");  // 替換為你的PHP伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送地址到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("customer_name=" + customer_name);
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
                // 解析並顯示客戶資料
                JSONObject jsonObject = new JSONObject(result);
                addressEditText.setText(jsonObject.getString("client_address"));
                phoneEditText.setText(jsonObject.getString("client_phone"));
                emailEditText.setText(jsonObject.getString("client_mail"));
                business_numberEditText.setText(jsonObject.getString("business_number"));
                if (jsonObject.getString("client_address")=="null")
                    addressEditText.setVisibility(View.GONE);
                if (jsonObject.getString("client_phone")=="null")
                    phoneEditText.setVisibility(View.GONE);
                if (jsonObject.getString("client_mail")=="null")
                    emailEditText.setVisibility(View.GONE);
                if (jsonObject.getString("business_number")=="null")
                    business_numberEditText.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
