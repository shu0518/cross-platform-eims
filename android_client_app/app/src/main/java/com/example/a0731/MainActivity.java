package com.example.a0731;

import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import androidx.activity.EdgeToEdge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewRegister, textViewResult;
    private SessionManager sessionManager; //保存登入帳密

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 初始化視圖
        editTextAccount = findViewById(R.id.editTextAccount);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewResult = findViewById(R.id.textViewResult);

        sessionManager = new SessionManager(this);

        // 檢查是否已經登入
        if (sessionManager.isLoggedIn()) {
            // 如果已登入，直接跳轉到首頁
            String savedAccount = sessionManager.getUserAccount();
            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
            intent.putExtra("account", savedAccount);
            startActivity(intent);
            finish();
            return;
        }

        // 為按鈕設置點擊監聽器
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 獲取輸入的帳號和密碼
                String account = editTextAccount.getText().toString();
                String password = editTextPassword.getText().toString();

                // 呼叫LoginTask執行登入操作
                new LoginTask().execute(account, password);
            }
        });

        // 設置忘記密碼點擊事件
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到 ForgotPasswordActivity
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // 設置註冊點擊事件
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String account = params[0];
            String password = params[1];
            String result = "";

            try {
                // 設置URL和連接
                URL url = new URL("http://163.17.135.120/api/login.php"); // 替換為你的伺服器URL
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);

                // 發送帳號和密碼到伺服器
                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write("account=" + account + "&password=" + password);
                writer.flush();
                writer.close();

                // 接收伺服器的回應
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
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
                if (result.startsWith("Login Successful")) {
                    // 從伺服器回應中獲取帳號
                    String[] parts = result.split(":");
                    String account = parts.length > 1 ? parts[1].trim() : "";

                    // 使用 SessionManager 儲存登入狀態
                    sessionManager.createLoginSession(account);

                    // 跳轉到 HomepageActivity
                    Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                    intent.putExtra("account", account);
                    startActivity(intent);
                    finish();
                } else {
                    textViewResult.setText("帳號或密碼錯誤，請重新輸入");
                }
            } catch (Exception e) {
                e.printStackTrace();
                textViewResult.setText("登入失敗，請稍後重試");
            }
        }
    }
}
