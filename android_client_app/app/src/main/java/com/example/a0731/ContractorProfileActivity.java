package com.example.a0731;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContractorProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, addressEditText, emailEditText, busnumEditText;
    private String contractorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_profile);

        // 獲取從前一頁傳遞過來的 contractor_name
        Intent intent = getIntent();
        contractorName = intent.getStringExtra("contractorName");

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 返回上一頁

        // 初始化視圖
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        emailEditText = findViewById(R.id.emailEditText);
        busnumEditText = findViewById(R.id.busnumEditText);

        // 查詢承包商資料並顯示
        new FetchContractorDataTask().execute(contractorName);
    }

    // 異步任務查詢承包商資料
    private class FetchContractorDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String contractorName = params[0];
            String result = "";
            try {
                // 設置 URL 和連接
                URL url = new URL("http://163.17.135.120/api/fetch_contractor_profile.php");  // 替換為你的 PHP 伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 傳送 contractor_name 到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("contractor_name=" + contractorName);
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
                // 解析 JSON 並設置到對應的 EditText 中
                JSONObject jsonObject = new JSONObject(result);
                nameEditText.setText(jsonObject.getString("contract_name"));
                phoneEditText.setText(jsonObject.getString("contract_phone"));
                addressEditText.setText(jsonObject.getString("contract_address"));
                emailEditText.setText(jsonObject.getString("contract_mail"));
                busnumEditText.setText(jsonObject.getString("contract_business_number"));
                if (jsonObject.getString("contract_address")=="null")
                    addressEditText.setVisibility(View.GONE);
                if (jsonObject.getString("contract_mail")=="null")
                    emailEditText.setVisibility(View.GONE);
                if (jsonObject.getString("contract_business_number")=="null")
                    busnumEditText.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ContractorProfileActivity.this, "查詢失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
