package com.example.a0731;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddMeasuredDataActivity extends AppCompatActivity {

    // UI 元件：基本欄位與規格選擇
    private EditText locationEditText, lengthEditText, widthEditText, quantityEditText;
    private Spinner specSpinner, windowTypeSpinner, windowDetailSpinner, glassCategorySpinner, glassDetailSpinner, doorTypeSpinner, doorPieceSpinner, doorStyleSpinner, topPositionSpinner, middlePositionSpinner, bottomPositionSpinner, topGlassSpinner, bottomGlassSpinner, colorSpinner, replenishSpinner;
    private Button addButton;
    private ImageView selectAccessoriesImage, selectWallImage, WindowRequirementsImage, DoorRequirementsImage;
    private TextView doorAccessoriesTextView,wallTextView, windowRequirementsTextView, doorRequirementsTextView,replenishTextView;
    private List<String> selectedAccessories; // 儲存已選擇的門配件
    private LinearLayout doorAccessoriesLayout, positionLayout, wallLayout, replenishLayout, windowRequirementsLayout, doorRequirementsLayout;
    private ArrayAdapter<CharSequence> glassAdapter; //門樣式
    private List<String> selectedWalls; // 儲存已選擇的牆體
    //private List<String> selectedReplenishes; // 儲存已選擇的現場補料
    private List<String> selectedWindowRequirements; // 儲存已選擇的窗的廠商要求
    private List<String> selectedDoorRequirements; // 儲存已選擇的門的廠商要求


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measured_data); // 綁定 XML 佈局

        Intent intent = getIntent();
        //String account = intent.getStringExtra("account");
        String customer_Address = intent.getStringExtra("customer_Address");

        // 綁定 UI 元件
        locationEditText = findViewById(R.id.location);// 格局輸入
        lengthEditText = findViewById(R.id.measured_length);// 測量長度輸入
        widthEditText = findViewById(R.id.measured_width);// 測量寬度輸入
        quantityEditText = findViewById(R.id.quantity);// 數量輸入
        specSpinner = findViewById(R.id.specification);// 規格選擇
        windowTypeSpinner = findViewById(R.id.windowTypeSpinner);// 窗戶種類選擇
        windowDetailSpinner = findViewById(R.id.windowDetailSpinner);// 窗的詳細種類選擇
        glassCategorySpinner = findViewById(R.id.glassCategorySpinner);//窗戶玻璃規格
        glassDetailSpinner = findViewById(R.id.glassDetailSpinner);//窗戶玻璃詳細規格
        doorAccessoriesLayout = findViewById(R.id.doorAccessoriesLayout);//門配件
        doorAccessoriesTextView = findViewById(R.id.doorAccessoriesTextView);
        selectAccessoriesImage = findViewById(R.id.selectAccessoriesImage);
        selectWallImage = findViewById(R.id.selectWallImage);
        WindowRequirementsImage = findViewById(R.id.WindowRequirementsImage);
        DoorRequirementsImage = findViewById(R.id.DoorRequirementsImage);
        doorTypeSpinner = findViewById(R.id.doorTypeSpinner);//門型
        doorPieceSpinner = findViewById(R.id.doorPieceSpinner);//門片
        doorStyleSpinner = findViewById(R.id.doorStyleSpinner);//門樣式
        positionLayout = findViewById(R.id.positionLayout);
        topPositionSpinner = findViewById(R.id.topPositionSpinner);
        middlePositionSpinner = findViewById(R.id.middlePositionSpinner);
        bottomPositionSpinner = findViewById(R.id.bottomPositionSpinner);
        topGlassSpinner = findViewById(R.id.topGlassSpinner);  // 專屬上欄位的玻璃規格 Spinner
        bottomGlassSpinner = findViewById(R.id.bottomGlassSpinner);  // 專屬下欄位的玻璃規格 Spinner
        wallLayout = findViewById(R.id.wallLayout); // 牆體 Layout
        wallTextView = findViewById(R.id.wallTextView);
        //replenishLayout = findViewById(R.id.replenishSpinnerLayout); // 現場補料 Layout
        replenishSpinner = findViewById(R.id.replenishSpinner); // 現場補料 Spinner
        windowRequirementsLayout = findViewById(R.id.windowRequirementsLayout); // 窗的廠商要求 Layout
        windowRequirementsTextView = findViewById(R.id.windowRequirementsTextView);
        doorRequirementsLayout = findViewById(R.id.doorRequirementsLayout); // 門的廠商要求 Layout
        doorRequirementsTextView = findViewById(R.id.doorRequirementsTextView);
        colorSpinner = findViewById(R.id.colorSpinner); // 顏色選擇 Spinner
        addButton = findViewById(R.id.addButton);//新增按鈕

        selectedAccessories = new ArrayList<>(); // 初始化門配件選擇列表
        selectedWalls = new ArrayList<>(); // 初始化牆體選擇列表
        selectedWindowRequirements = new ArrayList<>();
        selectedDoorRequirements = new ArrayList<>();

        // 初始化所有選擇列表
        setupSpecSpinner(); // 初始化規格選擇
        setupWallSelection(); // 初始化牆體複選
        setupReplenishSelection(); // 初始化現場補料複選
        setupWindowTypeSpinner(); // 初始化窗的種類選擇
        setupGlassCategorySpinner(); // 初始化玻璃種類選擇邏輯
        setupDoorAccessoriesLayout(); // 初始化門配件複選按鈕
        setupDoorTypeSpinner(); //門型門片
        setupDoorStyleSpinner();//門樣式
        setupPositionSpinners();
        setupAddButton();   // 初始化新增按鈕的事件
        setupWindowRequirementsSelection(); // 初始化窗的廠商要求選擇
        setupDoorRequirementsSelection(); // 初始化門的廠商要求選擇
        setupColorSpinner(); // 初始化顏色選擇

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    // 初始化顏色選擇的 Spinner
    private void setupColorSpinner() {
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                this, R.array.colors, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
    }

    // 初始化牆體選擇的複選
    private void setupWallSelection() {
        String[] wallOptions = getResources().getStringArray(R.array.牆體);
        boolean[] checkedItems = new boolean[wallOptions.length];

        wallLayout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("選擇牆體");
            builder.setMultiChoiceItems(wallOptions, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedWalls.add(wallOptions[which]);
                } else {
                    selectedWalls.remove(wallOptions[which]);
                }
            });

            builder.setPositiveButton("確定", (dialog, which) -> {
                wallTextView.setText(String.join(", ", selectedWalls));
                Toast.makeText(this, "已選擇牆體: " + String.join(", ", selectedWalls), Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        });
    }

    //初始化規格選擇的 Spinner。
    private void setupSpecSpinner() {
        specSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 選擇窗後顯示窗的種類
                if (position == 1) { //門
                    doorAccessoriesLayout.setVisibility(View.VISIBLE);  // 顯示門配件按鈕
                    replenishSpinner.setVisibility(View.VISIBLE); // 顯示現場補料欄位
                    doorTypeSpinner.setVisibility(View.VISIBLE); //顯示門型欄位
                    doorStyleSpinner.setVisibility(View.VISIBLE);   //顯示門樣式
                    windowTypeSpinner.setVisibility(View.GONE);          // 隱藏窗戶種類
                    windowDetailSpinner.setVisibility(View.GONE);        // 隱藏窗戶詳細規格
                    glassCategorySpinner.setVisibility(View.GONE);       // 隱藏玻璃種類選項
                    glassDetailSpinner.setVisibility(View.GONE);         // 隱藏玻璃詳細規格
                    doorRequirementsLayout.setVisibility(View.VISIBLE);  // 顯示門的廠商要求
                    windowRequirementsLayout.setVisibility(View.GONE);   // 隱藏窗的廠商要求
                } else if (position == 2) { // 窗
                    doorAccessoriesLayout.setVisibility(View.GONE);
                    windowTypeSpinner.setVisibility(View.VISIBLE);
                    glassCategorySpinner.setVisibility(View.VISIBLE);
                    replenishSpinner.setVisibility(View.GONE); // 隱藏現場補料欄位
                    doorTypeSpinner.setVisibility(View.GONE); //隱藏門型欄位
                    doorStyleSpinner.setVisibility(View.GONE);  //隱藏門樣式
                    doorPieceSpinner.setVisibility(View.GONE); //隱藏門片欄位
                    doorRequirementsLayout.setVisibility(View.GONE);   // 隱藏門的廠商要求
                    windowRequirementsLayout.setVisibility(View.VISIBLE); // 顯示窗的廠商要求
                    positionLayout.setVisibility(View.GONE);
                    topPositionSpinner.setVisibility(View.GONE);
                    middlePositionSpinner.setVisibility(View.GONE);
                    bottomPositionSpinner.setVisibility(View.GONE);
                } else {
                    doorAccessoriesLayout.setVisibility(View.GONE);       // 隱藏門配件按鈕
                    windowTypeSpinner.setVisibility(View.GONE);           // 隱藏窗戶種類
                    windowDetailSpinner.setVisibility(View.GONE);         // 隱藏窗戶詳細規格
                    glassCategorySpinner.setVisibility(View.GONE);        // 隱藏玻璃種類選項
                    glassDetailSpinner.setVisibility(View.GONE);          // 隱藏玻璃詳細規格
                    replenishSpinner.setVisibility(View.GONE); // 隱藏現場補料欄位
                    doorTypeSpinner.setVisibility(View.GONE); //隱藏門型欄位
                    doorPieceSpinner.setVisibility(View.GONE); //隱藏門片欄位
                    doorStyleSpinner.setVisibility(View.GONE);   //隱藏門樣式
                    doorRequirementsLayout.setVisibility(View.GONE);   // 隱藏門的廠商要求
                    windowRequirementsLayout.setVisibility(View.GONE); // 隱藏窗的廠商要求
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 若無選擇任何項目可忽略此部分
            }
        });
    }
    //初始化窗戶種類選擇的 spinner
    private void setupWindowTypeSpinner() {
        windowTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId; // 儲存對應的字串資源 ID
                switch (position) {
                    case 1:
                        arrayId = R.array.window_2_track;
                        break;
                    case 2:
                        arrayId = R.array.window_3_track;
                        break;
                    case 3:
                        arrayId = R.array.window_4_track;
                        break;
                    default:
                        windowDetailSpinner.setVisibility(View.GONE); // 隱藏詳細規格選單
                        return;
                }
                windowDetailSpinner.setVisibility(View.VISIBLE); // 顯示詳細規格選單
                loadWindowDetails(arrayId); // 載入對應的詳細選項
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    //傳遞從 strings.xml 載入窗的詳細種類選項到 Spinner。
    private void loadWindowDetails(int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item); // 創建 ArrayAdapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 設定下拉樣式
        windowDetailSpinner.setAdapter(adapter); // 將 Adapter 綁定到 Spinner
    }

    //初始化窗戶玻璃規格的 spinner
    private void setupGlassCategorySpinner() {
        glassCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arrayId;
                switch (position) {
                    case 1:
                        arrayId = R.array.glass_5mm;
                        break;
                    case 2:
                        arrayId = R.array.glass_8mm;
                        break;
                    case 3:
                        arrayId = R.array.glass_複合玻璃;
                        break;
                    default:
                        glassDetailSpinner.setVisibility(View.GONE);
                        return;
                }
                glassDetailSpinner.setVisibility(View.VISIBLE);
                loadSpinnerData(glassDetailSpinner, arrayId);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    //傳遞從 strings.xml 載入窗戶玻璃的詳細規格選項到 Spinner。
    private void loadSpinnerData(Spinner spinner, int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // 初始化窗的廠商要求選擇的複選
    private void setupWindowRequirementsSelection() {
        String[] windowRequirementsOptions = getResources().getStringArray(R.array.窗的廠商要求);
        boolean[] checkedItems = new boolean[windowRequirementsOptions.length];

        windowRequirementsLayout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("選擇窗的廠商要求");
            builder.setMultiChoiceItems(windowRequirementsOptions, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedWindowRequirements.add(windowRequirementsOptions[which]);
                } else {
                    selectedWindowRequirements.remove(windowRequirementsOptions[which]);
                }
            });

            builder.setPositiveButton("確定", (dialog, which) -> {
                windowRequirementsTextView.setText(String.join(", ", selectedWindowRequirements));
                Toast.makeText(this, "已選擇窗的廠商要求: " + String.join(", ", selectedWindowRequirements), Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        });
    }

    // 初始化門的廠商要求選擇的複選
    private void setupDoorRequirementsSelection() {
        String[] doorRequirementsOptions = getResources().getStringArray(R.array.門的廠商要求);
        boolean[] checkedItems = new boolean[doorRequirementsOptions.length];

        doorRequirementsLayout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("選擇門的廠商要求");
            builder.setMultiChoiceItems(doorRequirementsOptions, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedDoorRequirements.add(doorRequirementsOptions[which]);
                } else {
                    selectedDoorRequirements.remove(doorRequirementsOptions[which]);
                }
            });

            builder.setPositiveButton("確定", (dialog, which) -> {
                doorRequirementsTextView.setText(String.join(", ", selectedDoorRequirements));
                Toast.makeText(this, "已選擇門的廠商要求: " + String.join(", ", selectedDoorRequirements), Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        });
    }

    //初始化門配件的複選按鈕
    private void setupDoorAccessoriesLayout() {
        String[] accessories = getResources().getStringArray(R.array.door_accessories);
        boolean[] checkedItems = new boolean[accessories.length];

        doorAccessoriesLayout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("選擇門配件");
            builder.setMultiChoiceItems(accessories, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedAccessories.add(accessories[which]);
                } else {
                    selectedAccessories.remove(accessories[which]);
                }
            });

            builder.setPositiveButton("確定", (dialog, which) -> {
                doorAccessoriesTextView.setText(String.join(", ", selectedAccessories));
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        });
    }

    // 初始化現場補料選擇的複選
    private void setupReplenishSelection() {
        ArrayAdapter<CharSequence> replenishAdapter = ArrayAdapter.createFromResource(
                this, R.array.replenish_options, android.R.layout.simple_spinner_item);
        replenishAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        replenishSpinner.setAdapter(replenishAdapter);
    }

    //門型
    private void setupDoorTypeSpinner() {
        doorTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: // L型兩孔門框
                        loadDoorPieceSpinner(R.array.door_piece_700);
                        break;
                    case 2: // 7公分
                    case 3: // 3×10框偏領
                        loadDoorPieceSpinner(R.array.door_piece_700_extended);
                        break;
                    case 4: // 3×10框中領
                    case 5: // 4.5×10框中領
                        loadDoorPieceSpinner(R.array.door_piece_1000);
                        break;
                    default:
                        doorPieceSpinner.setVisibility(View.GONE);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //門片
    private void loadDoorPieceSpinner(int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_dropdown_item);
        doorPieceSpinner.setAdapter(adapter);
        doorPieceSpinner.setVisibility(View.VISIBLE);

        // 如果只有一個選項，直接選中並禁用
        if (adapter.getCount() == 1) {
            doorPieceSpinner.setSelection(0);
            doorPieceSpinner.setEnabled(false);
        } else {
            doorPieceSpinner.setEnabled(true);
        }
    }

    private void setupDoorStyleSpinner() {
        doorStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStyle = parent.getItemAtPosition(position).toString();

                if (selectedStyle.equals("上中下")) {
                    // 顯示所有位置欄位
                    positionLayout.setVisibility(View.VISIBLE);
                    topPositionSpinner.setVisibility(View.VISIBLE);
                    middlePositionSpinner.setVisibility(View.VISIBLE);
                    bottomPositionSpinner.setVisibility(View.VISIBLE);

                    // 設置預設選項
                    topPositionSpinner.setSelection(getIndex(topPositionSpinner, "花格網"));
                    middlePositionSpinner.setSelection(getIndex(middlePositionSpinner, "花格網"));
                    bottomPositionSpinner.setSelection(getIndex(bottomPositionSpinner, "花板"));

                    // 禁用 Spinners（通過 TouchListener 拦截事件）
                    setSpinnerTouchable(topPositionSpinner, false);
                    setSpinnerTouchable(middlePositionSpinner, false);
                    setSpinnerTouchable(bottomPositionSpinner, false);

                } else if (selectedStyle.equals("上下")) {
                    // 顯示上、下欄位，隱藏中欄位
                    positionLayout.setVisibility(View.VISIBLE);
                    topPositionSpinner.setVisibility(View.VISIBLE);
                    middlePositionSpinner.setVisibility(View.GONE);
                    bottomPositionSpinner.setVisibility(View.VISIBLE);

                    // 恢復 Spinner 的選項為可用
                    topPositionSpinner.setSelection(0);
                    bottomPositionSpinner.setSelection(0);

                    // 允許 Spinners 點擊
                    setSpinnerTouchable(topPositionSpinner, true);
                    setSpinnerTouchable(bottomPositionSpinner, true);
                } else {
                    // 隱藏所有位置欄位
                    positionLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 工具方法，用於動態控制 Spinner 是否可點擊
    private void setSpinnerTouchable(Spinner spinner, boolean isTouchable) {
        if (isTouchable) {
            // 移除 TouchListener，恢復正常點擊行為
            spinner.setOnTouchListener(null);
        } else {
            // 拦截所有点击事件，使其不可点击
            spinner.setOnTouchListener((v, event) -> true);
        }
    }


    //幫助函數來獲取選項索引
    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i; // 返回找到的索引
            }
        }
        return 0; // 如果沒有找到，返回第一個選項
    }

    //若選上下樣式
    private void setupPositionSpinners() {
        topPositionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTop = parent.getItemAtPosition(position).toString();

                // 若選擇 "混色花板"、"全門花" 或 "全百葉片"
                if (selectedTop.equals("混色花板") || selectedTop.equals("全門花") || selectedTop.equals("全百葉片")) {
                    // 將下欄位設為相同並鎖定
                    bottomPositionSpinner.setSelection(position); // 設定下欄位與上欄位相同
                    setSpinnerTouchable(topPositionSpinner, true);// 啟用上欄位
                    setSpinnerTouchable(bottomPositionSpinner, false);// 鎖定下欄位
                } else {
                    if(doorStyleSpinner.getSelectedItem().toString().equals("上中下")){
                        setSpinnerTouchable(bottomPositionSpinner, false);// 鎖定下欄位
                    }
                    else{
                        setSpinnerTouchable(bottomPositionSpinner, true); // 啟用下欄位
                        bottomPositionSpinner.setSelection(0);
                    }
                }
                // 若上欄位選到 8mm 或 5mm 玻璃，顯示對應的上欄位玻璃規格選單
                if (selectedTop.equals("8mm玻璃") || selectedTop.equals("5mm玻璃")) {
                    loadGlassOptionsForTop(selectedTop);
                } else {
                    topGlassSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        bottomPositionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBottom = parent.getItemAtPosition(position).toString();

                // 若下欄位選擇 "混色花板"、"全門花" 或 "全百葉片"
                if (selectedBottom.equals("混色花板") || selectedBottom.equals("全門花") || selectedBottom.equals("全百葉片")) {
                    // 將上欄位設為相同並鎖定
                    topPositionSpinner.setSelection(position); // 設定上欄位與下欄位相同
                    setSpinnerTouchable(topPositionSpinner, true);// 啟用上欄位
                    setSpinnerTouchable(bottomPositionSpinner, false);// 鎖定下欄位
                }
                // 若下欄位選到 8mm 或 5mm 玻璃，顯示對應的下欄位玻璃規格選單
                if (selectedBottom.equals("8mm玻璃") || selectedBottom.equals("5mm玻璃")) {
                    loadGlassOptionsForBottom(selectedBottom);
                } else {
                    bottomGlassSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //同步上下欄位的選擇
    private void syncTopAndBottomSelection(int position) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) topPositionSpinner.getAdapter();
        bottomPositionSpinner.setAdapter(adapter);
        bottomPositionSpinner.setSelection(position);
        bottomPositionSpinner.setEnabled(false);  // 禁用下欄位
    }

    // 加載上欄位的玻璃詳細規格選項
    private void loadGlassOptionsForTop(String glassType) {
        int arrayId;
        // 根據所選擇的玻璃類型設定對應的資料來源
        if (glassType.equals("8mm玻璃")) {
            arrayId = R.array.door_top_glass_8mm; // 8mm玻璃的上欄位選項
        } else {
            arrayId = R.array.door_top_glass_5mm; // 5mm玻璃的上欄位選項
        }

        // 創建 ArrayAdapter 並將資料綁定到上欄位玻璃 Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_dropdown_item);
        topGlassSpinner.setAdapter(adapter);
        topGlassSpinner.setVisibility(View.VISIBLE); // 顯示上欄位玻璃選項
    }

    // 加載下欄位的玻璃詳細規格選項
    private void loadGlassOptionsForBottom(String glassType) {
        int arrayId;
        // 根據所選擇的玻璃類型設定對應的資料來源
        if (glassType.equals("8mm玻璃")) {
            arrayId = R.array.door_bottom_glass_8mm; // 8mm玻璃的下欄位選項
        } else {
            arrayId = R.array.door_bottom_glass_5mm; // 5mm玻璃的下欄位選項
        }

        // 創建 ArrayAdapter 並將資料綁定到下欄位玻璃 Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_dropdown_item);
        bottomGlassSpinner.setAdapter(adapter);
        bottomGlassSpinner.setVisibility(View.VISIBLE); // 顯示下欄位玻璃選項
    }

    // 顯示玻璃選擇對話框
    private void showGlassSelectionDialog(String position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position + "欄位玻璃規格");

        // 根據位置選擇玻璃選項
        String[] glassOptions;
        if (position.equals("上")) {
            // 根據上欄位選擇的類型選擇對應的玻璃選項
            glassOptions = getResources().getStringArray(R.array.door_top_glass_8mm); // 可調整
        } else {
            glassOptions = getResources().getStringArray(R.array.door_bottom_glass_8mm); // 可調整
        }

        builder.setItems(glassOptions, (dialog, which) -> {
            String selectedGlass = glassOptions[which];
            // 根據選擇的玻璃更新相關的 UI
            Toast.makeText(AddMeasuredDataActivity.this, "選擇了：" + selectedGlass, Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }

    //儲存測量數據，檢查所有欄位是否填寫完成。
    private void saveMeasuredData() {
        String location = locationEditText.getText().toString();
        String length = lengthEditText.getText().toString();
        String width = widthEditText.getText().toString();
        String quantity = quantityEditText.getText().toString();

        // 驗證是否有未填寫的欄位
        if (location.isEmpty() || length.isEmpty() || width.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 將資料儲存至資料庫或進行其他操作

        Toast.makeText(this, "測量數據已成功儲存", Toast.LENGTH_SHORT).show();
        finish(); // 返回上一頁
    }
    //初始化新增按鈕的點擊事件。
    private void setupAddButton() {
        addButton.setOnClickListener(v -> {
            try {
                String clientAddress = getIntent().getStringExtra("customer_Address");
                String position = locationEditText.getText().toString().trim();
                float length = Float.parseFloat(lengthEditText.getText().toString().trim());
                float width = Float.parseFloat(widthEditText.getText().toString().trim());
                int quantity = Integer.parseInt(quantityEditText.getText().toString().trim());

                if (position.isEmpty()) {
                    Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject specification = new JSONObject();
                JSONArray walls = new JSONArray();
                JSONArray requests = new JSONArray();
                JSONArray assets = new JSONArray();
                if (specSpinner.getVisibility() == View.VISIBLE) {
                    if(specSpinner.getSelectedItem().toString().equals("門窗選擇")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        specification.put("windoor", specSpinner.getSelectedItem().toString());
                    }
                }
                if (colorSpinner.getVisibility() == View.VISIBLE) {
                    if(colorSpinner.getSelectedItem().toString().equals("顏色選擇")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        specification.put("color", colorSpinner.getSelectedItem().toString());
                    }
                }
                if (windowRequirementsLayout.getVisibility() == View.VISIBLE) {
                    if(windowRequirementsTextView.getText().toString().equals("") || windowRequirementsTextView.getText().toString().equals("選擇廠商要求")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        String[] requestItems = windowRequirementsTextView.getText().toString().split(",\\s*");  // 根據逗號和可選的空格拆分
                        for (String item : requestItems) {
                            requests.put(item.trim());  // 去除多餘空格並加入 JSONArray
                        }
                        specification.put("request", requests);
                    }
                } else if (doorRequirementsLayout.getVisibility() == View.VISIBLE) {
                    if(doorRequirementsTextView.getText().toString().equals("") ||doorRequirementsTextView.getText().toString().equals("選擇廠商要求")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        String[] requestItems = doorRequirementsTextView.getText().toString().split(",\\s*");  // 根據逗號和可選的空格拆分
                        for (String item : requestItems) {
                            requests.put(item.trim());  // 去除多餘空格並加入 JSONArray
                        }
                        specification.put("request", requests);
                    }
                }
                if (wallLayout.getVisibility() == View.VISIBLE) {
                    if(wallTextView.getText().toString().equals("") ||wallTextView.getText().toString().equals("選擇牆體")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        String[] wallItems = wallTextView.getText().toString().split(",\\s*");  // 根據逗號和可選的空格拆分
                        // 將每個牆體內容存入 walls JSONArray
                        for (String item : wallItems) {
                            walls.put(item.trim());  // 去除多餘空格並加入 JSONArray
                        }
                        // 將 walls JSONArray 存入 specification JSONObject
                        specification.put("wall", walls);
                    }
                }
                if (doorAccessoriesLayout.getVisibility() == View.VISIBLE) {
                    if(doorAccessoriesTextView.getText().toString().equals("") ||doorAccessoriesTextView.getText().toString().equals("選擇門配件")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        String[] assetsItems = doorAccessoriesTextView.getText().toString().split(",\\s*");  // 根據逗號和可選的空格拆分
                        // 將每個牆體內容存入 walls JSONArray
                        for (String item : assetsItems) {
                            assets.put(item.trim());  // 去除多餘空格並加入 JSONArray
                        }
                        specification.put("asset", assets);
                    }
                }
                if (replenishSpinner.getVisibility() == View.VISIBLE) {
                    if(replenishSpinner.getSelectedItem().toString().equals("請選擇補料")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        specification.put("onSite", replenishSpinner.getSelectedItem().toString());
                    }
                }
                if (windowTypeSpinner.getVisibility() == View.VISIBLE) {
                    if(windowTypeSpinner.getSelectedItem().toString().equals("請選擇窗戶種類") || windowDetailSpinner.getSelectedItem().toString().equals("請選擇") || glassCategorySpinner.getSelectedItem().toString().equals("請選擇玻璃規格") || glassDetailSpinner.getSelectedItem().toString().equals("請選擇5mm 玻璃種類") || glassDetailSpinner.getSelectedItem().toString().equals("請選擇 8mm 玻璃種類") || glassDetailSpinner.getSelectedItem().toString().equals("請選擇 複合玻璃種類")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        JSONObject windowType = new JSONObject();
                        JSONObject glass = new JSONObject();
                        windowType.put("piece", windowTypeSpinner.getSelectedItem().toString());
                        windowType.put("option", windowDetailSpinner.getSelectedItem().toString());
                        windowType.put("thick", glassCategorySpinner.getSelectedItem().toString());
                        specification.put("type", windowType);
                        glass.put(glassCategorySpinner.getSelectedItem().toString(), glassDetailSpinner.getSelectedItem().toString());
                        specification.put("glass", glass);
                    }
                } else {
                    if(doorTypeSpinner.getSelectedItem().toString().equals("請選擇門型") || doorStyleSpinner.getSelectedItem().toString().equals("請選擇樣式") || topPositionSpinner.getSelectedItem().toString().equals("請選擇上樣式") || bottomPositionSpinner.getSelectedItem().toString().equals("請選擇下樣式")){
                        Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        JSONObject doorType = new JSONObject();
                        JSONObject glass = new JSONObject();
                        JSONObject updown = new JSONObject();
                        doorType.put("frame", doorTypeSpinner.getSelectedItem().toString());
                        doorType.put("piece", doorPieceSpinner.getSelectedItem().toString());
                        doorType.put("model", doorStyleSpinner.getSelectedItem().toString());
                        updown.put("up", topPositionSpinner.getSelectedItem().toString());
                        if (middlePositionSpinner.getVisibility() == View.VISIBLE){
                            updown.put("middle", middlePositionSpinner.getSelectedItem().toString());
                        }
                        updown.put("down", bottomPositionSpinner.getSelectedItem().toString());
                        doorType.put("option", updown);
                        specification.put("type", doorType);
                        if (topGlassSpinner.getVisibility() == View.VISIBLE || bottomGlassSpinner.getVisibility() == View.VISIBLE){
                            if (topGlassSpinner.getVisibility() == View.VISIBLE){
                                if(topGlassSpinner.getSelectedItem().toString().equals("請選擇上欄位玻璃")){
                                    Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else{
                                    glass.put("up", topGlassSpinner.getSelectedItem().toString());
                                }
                            }
                            if (bottomGlassSpinner.getVisibility() == View.VISIBLE){
                                if(bottomGlassSpinner.getSelectedItem().toString().equals("請選擇下欄位玻璃")){
                                    Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else{
                                    glass.put("down",bottomGlassSpinner.getSelectedItem().toString());
                                }
                            }
                        }
                        else {
                            glass.put("", "");
                        }
                        specification.put("glass", glass);
                    }
                }
                new SaveMeasuredDataTask().execute(
                        clientAddress, length, width, position, quantity, specification.toString()
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "資料格式錯誤", Toast.LENGTH_SHORT).show();
            }
//            String clientAddress = getIntent().getStringExtra("customer_Address");
//            textView1=findViewById(R.id.textView1);
//            textView1.setText(clientAddress);
        });
    }

    private class SaveMeasuredDataTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            String clientAddress = (String) params[0];
            float length = (float) params[1];
            float width = (float) params[2];
            String position = (String) params[3];
            int quantity = (int) params[4];
            String specification = (String) params[5];

            try {
                URL url = new URL("http://163.17.135.120/api/save_measured_data.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("client_address=" + clientAddress +
                        "&length=" + length +
                        "&width=" + width +
                        "&position=" + position +
                        "&quantity=" + quantity +
                        "&specification=" + specification);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                Toast.makeText(AddMeasuredDataActivity.this, "測量數據已成功儲存", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddMeasuredDataActivity.this, "儲存失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
/*編寫setupAddButton的點擊事件:把資料儲存進measure資料表。儲存規則:
要抓資料的物件不可為空值，處於可見狀態的物件才要儲存
customer_Address->client_address，lengthEditText->length(float型態，小數點後兩位)，widthEditText->width(float型態，小數點後兩位)，當天日期(年-月-日)->measure_date，locationEditText->position，quantityEditText->quantity(int型態)
其他的用JSON格式存入specification欄位，如果該物件為不可見的狀態則不儲存
windoor:specSpinner，color:colorSpinner，request:windowRequirementsTextView或doorRequirementsTextView，wall:wallTextView，assets:doorAccessoriesTextView，onSite:replenishSpinner，type(使用dictionary):[doorTypeSpinner,doorPieceSpinner,topPositionSpinner,middlePositionSpinner(獨立判定是否可見),bottomPositionSpinner]或[windowTypeSpinner, windowDetailSpinner]，glass(使用dictionary):[topGlassSpinner,bottomGlassSpinner](進行獨立判定，如果不可見就不儲存)或[glassCategorySpinner, glassDetailSpinner]*/