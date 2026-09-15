//package com.example.a0731;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageButton;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import java.util.ArrayList;
//import java.util.List;
//
//public class FrontpageActivity extends AppCompatActivity {
//
//    private RecyclerView contractorRecyclerView;
//    private ContractorAdapter contractorAdapter;
//    private List<Contractor> contractorList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_frontpage);
//
//        // 初始化 RecyclerView
//        contractorRecyclerView = findViewById(R.id.contractorRecyclerView);
//        contractorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // 初始化數據
//        contractorList = new ArrayList<>();
//        contractorList.add(new Contractor("大老", "0912345678"));
//        contractorList.add(new Contractor("大黃", "0912345678"));
//        contractorList.add(new Contractor("小王", "0912345678"));
//        // 添加更多承包商...
//
//        // 設置適配器
//        contractorAdapter = new ContractorAdapter(this, contractorList);
//        contractorRecyclerView.setAdapter(contractorAdapter);
//
//        // 查找並設置新增按鈕的點擊事件
//        ImageButton addButton = findViewById(R.id.addButton);
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(FrontpageActivity.this, AddContractorActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        // 查找並設置 profileButton 的點擊事件
//        ImageButton profileButton = findViewById(R.id.profileButton);
//        profileButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(FrontpageActivity.this, UserProfileActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        // 查找並設置登出按鈕的點擊事件
//        ImageButton logoutButton = findViewById(R.id.logoutButton);
//        logoutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logout();  // 調用登出方法
//            }
//        });
//    }
//
//    private void logout() {
//        // 清除用戶數據或執行其他登出邏輯
//        Intent intent = new Intent(FrontpageActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();  // 結束當前的活動
//    }
//}
