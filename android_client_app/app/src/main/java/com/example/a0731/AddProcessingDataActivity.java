package com.example.a0731;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
public class AddProcessingDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MeasuredDataAdapter measuredDataAdapter;
    private List<MeasuredData> measuredDataList;
    private CheckBox selectAllCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_processing_data);

        String customer_Address = getIntent().getStringExtra("customer_Address");
        String account = getIntent().getStringExtra("account");

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化窗框數據列表
        measuredDataList = new ArrayList<>();

        // 設置適配器
        measuredDataAdapter = new MeasuredDataAdapter(AddProcessingDataActivity.this,measuredDataList, account,true);
        recyclerView.setAdapter(measuredDataAdapter);

        // 初始化全選 Checkbox
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            measuredDataAdapter.setAllChecked(isChecked); // 只需要刷新選擇狀態
        });

        // 新增按鈕的點擊事件
        ImageButton generateDataButton = findViewById(R.id.generateDataButton);
        generateDataButton.setOnClickListener(v -> {
            List<String> selectedIds = measuredDataAdapter.getSelectedIds(); // 獲取選取的 ID

            if (selectedIds.isEmpty()) {
                Toast.makeText(AddProcessingDataActivity.this, "請選擇至少一個項目", Toast.LENGTH_SHORT).show();
            } else {
                // 在這裡處理選取的 ID
                // 例如，你可以將它們傳遞給其他 Activity 或者進行其他操作
                for (String id : selectedIds) {
                    // 處理每個選取的 ID
                    MeasuredData measuredData = measuredDataAdapter.getMeasuredDataById(id); // 獲取對應的 MeasuredData
                    if (measuredData != null) {
                        String specification = measuredData.getSpecification(); // 獲取 specification
                        boolean isdoor = measuredData.isDoor();
                        String spec = measuredData.getspec();
                        float length = Float.parseFloat(measuredData.getLength());
                        float width = Float.parseFloat(measuredData.getWidth());
                        float[] results=new float[14];
                        if(isdoor){
                            try {
                                String piece=new JSONObject(specification).getJSONObject("type").getString("piece");
                                switch (new JSONObject(specification).getJSONObject("type").getString("frame")){
                                    case "L型兩孔門框":
                                        results = DoorCal.calculate1(width,length,piece);
                                        break;
                                    case "7公分":
                                        results = DoorCal.calculate2(width,length,piece);
                                        break;
                                    case "3×10框偏領":
                                        results = DoorCal.calculate3(width,length,piece);
                                        break;
                                    case "3×10框中領":
                                        results = DoorCal.calculate4(width,length);
                                        break;
                                    case "4.5×10框中領":
                                        results = DoorCal.calculate5(width,length);
                                        break;
                                }
                                String model=new JSONObject(specification).getJSONObject("type").getString("model");
                                String top=new JSONObject(specification).getJSONObject("type").getJSONObject("option").getString("up");
                                String bot=new JSONObject(specification).getJSONObject("type").getJSONObject("option").getString("down");
                                results = DoorCal.calculateitem(model, top, bot, results);
                                // 準備要傳送的資料
                                String params = "customer_Address=" + customer_Address +
                                        "&account=" + account +
                                        "&measure_id=" + id +
                                        "&model=" + model +
                                        "&top=" + top +
                                        "&bot=" + bot +
                                        "&length=" + length +
                                        "&results=" + Arrays.toString(results);

                                // 呼叫 savedoor.php
                                new SaveDoorTask().execute(params); // 執行保存操作
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else{
                            try {
                                String piece=new JSONObject(specification).getJSONObject("type").getString("piece");
                                String option=new JSONObject(specification).getJSONObject("type").getString("option");
                                String thick = new JSONObject(specification).getJSONObject("type").getString("thick");
                                switch (piece){
                                    case "2拉":
                                        switch (option){
                                            case "866":
                                                results=Calculator.calculate1(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "866水泥窗":
                                                results=Calculator.calculate2(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1066":
                                                results=Calculator.calculate3(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1068":
                                                results=Calculator.calculate4(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1066-866流理台反裝":
                                                results=Calculator.calculate5(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1066加1200型鎖":
                                                results=Calculator.calculate6(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                        }
                                    case "3拉":
                                        switch (option){
                                            case "866":
                                                results=cal3la.calculate1(width,length);
                                                results=windowglass.glasscal3la(option,thick, results);
                                                break;
                                            case "1066":
                                                results=cal3la.calculate2(width,length);
                                                results=windowglass.glasscal3la(option,thick, results);
                                                break;
                                            case "1068":
                                                results=cal3la.calculate3(width,length);
                                                results=windowglass.glasscal3la(option,thick, results);
                                                break;
                                        }
                                    case "4拉":
                                        switch (option){
                                            case "866":
                                                results=Calculator4la.calculate1(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1066":
                                                results=Calculator4la.calculate2(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                            case "1068":
                                                results=Calculator4la.calculate3(width,length);
                                                results=windowglass.glasscal(option,thick, results);
                                                break;
                                        }
                                }
                                // 準備要傳送的資料
                                String params = "customer_Address=" + customer_Address +
                                        "&account=" + account +
                                        "&measure_id=" + id +
                                        "&piece=" + piece +
                                        "&option=" + option +
                                        "&thick=" + thick +
                                        "&results=" + Arrays.toString(results);
                                // 呼叫 savedoor.php
                                new SaveWindowTask().execute(params); // 執行保存操作
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                //Toast.makeText(AddProcessingDataActivity.this, w.toString(), Toast.LENGTH_SHORT).show();
            }
            // 跳轉到加工數據詳細頁面
//            Intent intent = new Intent(AddProcessingDataActivity.this, ProcessingDataDetailActivity.class);
//            startActivity(intent);
        });

        new FetchMeasuredDataTask().execute(customer_Address, "");
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
                measuredDataList = new ArrayList<>();
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
                        measuredDataList.add(new MeasuredData(String.valueOf(id), specification,"門", position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), true));
                    } else if (specification.contains("窗")) {
                        String typePiece = new JSONObject(specification).getJSONObject("type").getString("piece");
                        measuredDataList.add(new MeasuredData(String.valueOf(id), specification, typePiece, position, String.format("%.2f", length), String.format("%.2f", width), String.valueOf(quantity), false));
                    }
                }
                measuredDataAdapter = new MeasuredDataAdapter(AddProcessingDataActivity.this,measuredDataList, account, true);
                recyclerView.setAdapter(measuredDataAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class SaveDoorTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/savedoor.php"); // 替換為你的 PHP 伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送參數到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params[0]);
                writer.flush();
                writer.close();

                // 讀取伺服器回應
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
            return result; // 返回伺服器的回應
        }

        @Override
        protected void onPostExecute(String result) {
            // 可以在這裡處理伺服器回應
            Toast.makeText(AddProcessingDataActivity.this, "數據已保存", Toast.LENGTH_SHORT).show();
            // 返回上一頁
            finish();
        }
    }
    private class SaveWindowTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/savewindow.php"); // 替換為你的 PHP 伺服器地址
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送參數到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params[0]);
                writer.flush();
                writer.close();

                // 讀取伺服器回應
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
            return result; // 返回伺服器的回應
        }

        @Override
        protected void onPostExecute(String result) {
            // 可以在這裡處理伺服器回應
            Toast.makeText(AddProcessingDataActivity.this, "數據已保存", Toast.LENGTH_SHORT).show();
            // 返回上一頁
            finish();
        }
    }
}
