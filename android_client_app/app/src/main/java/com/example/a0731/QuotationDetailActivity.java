package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.a0731.QuotationAdapter;
import com.example.a0731.Quotation;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class QuotationDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuotationAdapter quotationAdapter;
    private List<Quotation> quotationList;
    private TextView customerNameTextView, dateTextView, addressTextView, contactTextView, totalPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_detail);

        // 接收從 HistoryRecordsActivity 傳遞過來的資料
        Intent intent = getIntent();
        ArrayList<String> specification = intent.getStringArrayListExtra("specifications");
        ArrayList<String> length = intent.getStringArrayListExtra("lengths");
        ArrayList<String> width = intent.getStringArrayListExtra("widths");
        ArrayList<String> quantity = intent.getStringArrayListExtra("quantities");
        ArrayList<String> spec = intent.getStringArrayListExtra("spec");
        String customer_Address = intent.getStringExtra("customer_Address");
        String customer_name = intent.getStringExtra("customer_name");
        String account = intent.getStringExtra("account");

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
//        backButton.setOnClickListener(v -> {
//            // 返回到 HistoryRecordsActivity 並清除中間的 Activity
//            Intent intent = new Intent(QuotationDetailActivity.this, HistoryRecordsActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent);
//        });

        customerNameTextView = findViewById(R.id.customerNameTextView);
        addressTextView = findViewById(R.id.addressTextView);
        contactTextView = findViewById(R.id.contactTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        customerNameTextView.setText(customer_name);
        addressTextView.setText(customer_Address);
        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化假報價單項目列表
        quotationList = new ArrayList<>();
        double price = 0;
        double totalprice = 0;
        double savetotalprice = 0;
        // 使用 for 迴圈將資料存入 quotationList
        if (specification != null && length != null && width != null && quantity != null) {
            for (int i = 0; i < specification.size(); i++) {
                String current = spec != null ? spec.get(i) : "N/A"; // 防止 spec 為 null
                String currentLength = length.get(i);
                String currentWidth = width.get(i);
                String currentspecification = specification.get(i);
                int currentQuantity = Integer.parseInt(quantity.get(i));
                if(current.equals("門")){
                    try {
                        String model = new JSONObject(currentspecification).getJSONObject("type").getString("model");
                        if(model.equals("上下")){
                            price = (Math.ceil((Float.parseFloat(currentWidth) * Float.parseFloat(currentLength)) / 918.08)) * 400 + 4000;
                            totalprice = price * currentQuantity;
                        }else{
                            price = (Math.ceil((Float.parseFloat(currentWidth) * Float.parseFloat(currentLength)) / 918.08)) * 500 + 4500;
                            totalprice = price * currentQuantity;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    price = (Math.ceil((Float.parseFloat(currentWidth) * Float.parseFloat(currentLength)) / 918.08)) * 300 + 3500;
                    totalprice = price * currentQuantity;
                }
                savetotalprice+=totalprice;
                // 添加 Quotation 到列表
                quotationList.add(new Quotation(current, currentLength, currentWidth, currentQuantity, price, totalprice));
            }
        }
        totalPriceTextView.setText(totalPriceTextView.getText().toString() + savetotalprice);
        // 設置適配器
        quotationAdapter = new QuotationAdapter(quotationList);
        recyclerView.setAdapter(quotationAdapter);

        new FetchCustomerProfileTask().execute(customer_name);
    }
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
                contactTextView.setText(jsonObject.getString("client_phone"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
