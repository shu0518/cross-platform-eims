//package com.example.a0731;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class AddWindowFrameActivity extends AppCompatActivity {
//
//    private EditText numberEditText, floorEditText, layoutEditText, lengthEditText, widthEditText, quantityEditText, typeEditText;
//    private Spinner specSpinner, glassTypeSpinner;
//    private Button addButton, cancelButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_measured_data);
//
//        // 初始化視圖
//        numberEditText = findViewById(R.id.numberEditText);
//        floorEditText = findViewById(R.id.floorEditText);
//        layoutEditText = findViewById(R.id.layoutEditText);
//        lengthEditText = findViewById(R.id.lengthEditText);
//        widthEditText = findViewById(R.id.widthEditText);
//        specSpinner = findViewById(R.id.specSpinner);
//        glassTypeSpinner = findViewById(R.id.glassTypeSpinner);
//
//        addButton = findViewById(R.id.addButton);
//        cancelButton = findViewById(R.id.cancelButton);
//
//        // 初始化返回按鈕
//        ImageButton backButton = findViewById(R.id.backButton);
//        backButton.setOnClickListener(v -> finish()); // 返回上一頁
//
//        // 設置規格下拉選單的選項
//        setupSpinner(specSpinner, R.array.spec_options);
//
//        // 設置玻璃型號下拉選單的選項
//        setupSpinner(glassTypeSpinner, R.array.glass_type_options);
//
//        // 設置新增按鈕的點擊事件
//        addButton.setOnClickListener(v -> {
//            String number = numberEditText.getText().toString().trim();
//            String floor = floorEditText.getText().toString().trim();
//            String layout = layoutEditText.getText().toString().trim();
//            String length = lengthEditText.getText().toString().trim();
//            String width = widthEditText.getText().toString().trim();
//            String quantity = quantityEditText.getText().toString().trim();
//            String spec = specSpinner.getSelectedItem().toString();
//            String glassType = glassTypeSpinner.getSelectedItem().toString();
//
//            if (number.isEmpty() || floor.isEmpty() || layout.isEmpty() || length.isEmpty() || width.isEmpty() || quantity.isEmpty()) {
//                Toast.makeText(AddWindowFrameActivity.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // 處理新增窗框數據的邏輯 (這裡應該要將數據傳遞回 WindowFrameDataActivity 或保存到數據庫)
//            Toast.makeText(AddWindowFrameActivity.this, "窗框數據已新增", Toast.LENGTH_SHORT).show();
//            finish();
//        });
//
//        // 設置取消按鈕的點擊事件
//        cancelButton.setOnClickListener(v -> finish()); // 返回上一頁
//    }
//
//    // 初始化 Spinner，並添加提示和選項
//    private void setupSpinner(Spinner spinner, int arrayResId) {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                this, android.R.layout.simple_spinner_item, getResources().getStringArray(arrayResId)) {
//
//            @Override
//            public boolean isEnabled(int position) {
//                // 第一個選項不允許被選擇 (作為提示文字)
//                return position != 0;
//            }
//
//            @Override
//            public View getDropDownView(int position, View convertView, ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                if (position == 0) {
//                    // 設置提示選項的文字顏色
//                    tv.setTextColor(Color.GRAY);
//                } else {
//                    tv.setTextColor(Color.BLACK);
//                }
//                return view;
//            }
//        };
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//    }
//}
