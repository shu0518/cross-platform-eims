package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.json.JSONObject;
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText contactInfoEditText;
    private EditText verificationCodeEditText;
    private Button sendCodeButton;
    private ImageButton closeButton;
    private ImageView backgroundImageView;
    private boolean isVerificationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 初始化 UI 元件
        contactInfoEditText = findViewById(R.id.contactInfoEditText);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText); // 驗證碼輸入框
        sendCodeButton = findViewById(R.id.sendCodeButton); //驗證碼按鈕
        closeButton = findViewById(R.id.closeButton);
        backgroundImageView = findViewById(R.id.backgroundImageView); //background

        // 預設一組電子郵件
//        String presetEmail = "user@example.com";
//        contactInfoEditText.setText(presetEmail);

        // 發送驗證碼按鈕的點擊事件
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVerificationMode) {
                    String contactInfo = contactInfoEditText.getText().toString().trim();
                    if (!contactInfo.isEmpty()) {
                        new SendVerificationCodeTask().execute(contactInfo);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "請輸入電子郵件", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 驗證驗證碼的操作
                    String verificationCode = verificationCodeEditText.getText().toString().trim();

                    if (verificationCode.isEmpty()) {
                        Toast.makeText(ForgotPasswordActivity.this, "請輸入驗證碼", Toast.LENGTH_SHORT).show();
                    }else {
                        String contactInfo = contactInfoEditText.getText().toString().trim();
                        new ForgotPasswordActivity.VerifyCodeTask().execute(contactInfo, verificationCode);
                    }
                }
            }
        });
        // 關閉按鈕的點擊事件
        closeButton.setOnClickListener(v -> finish());  // 點擊後關閉當前活動
    }
    private class SendVerificationCodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String contactInfo = params[0];
            String result;
            try {
                // 設置伺服器的PHP檔案URL
                URL url = new URL("http://163.17.135.120/api/verifycode.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 產生六位數的驗證碼
                String verificationCode = generateVerificationCode();

                // 送出POST請求，包含email與驗證碼
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("email=" + contactInfo + "&code=" + verificationCode);
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
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // 解析伺服器回應的 JSON
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");

                if (status.equals("success")) {
                    // 獲取帳號
                    String account = response.getString("account");
                    Toast.makeText(ForgotPasswordActivity.this, "驗證碼已發送", Toast.LENGTH_LONG).show();
                    // 顯示驗證碼輸入框
                    verificationCodeEditText.setVisibility(View.VISIBLE);

                    // 改變發送驗證碼按鈕為 "驗證"
                    sendCodeButton.setText("驗證");

                    // 開啟驗證模式
                    isVerificationMode = true;
                    // 跳轉到 VerifyCodeActivity，並將帳號帶過去
//                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
//                    intent.putExtra("email", contactInfoEditText.getText().toString().trim());
//                    intent.putExtra("account", account);
//                    startActivity(intent);
                } else {
                    String message = response.getString("message");
                    Toast.makeText(ForgotPasswordActivity.this, "發送失敗: " + message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ForgotPasswordActivity.this, "解析回應失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class VerifyCodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String enteredCode = params[1];
            String result;
            try {
                // 設置伺服器的PHP檔案URL
                URL url = new URL("http://163.17.135.120/api/verifycodecheck.php");  // 這是處理驗證碼比對的PHP檔案
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送 POST 請求，包含 email, enteredCode 和帳號
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("email=" + email + "&code=" + enteredCode);
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
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("ServerResponse", result);
            try {
                // 解析伺服器回應的 JSON 資料
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");

                if (status.equals("success")) {
                    // 跳轉到 ResetPasswordActivity，並將帳號傳遞過去
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("account", response.getString("account"));
                    startActivity(intent);
                } else {
                    String message = response.getString("message");
                    Toast.makeText(ForgotPasswordActivity.this, "驗證失敗: " + message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ForgotPasswordActivity.this, "解析回應失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 產生六位數的英數字驗證碼
    private String generateVerificationCode() {
        String characters = "0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) { // 6位數
            int index = (int) (rnd.nextFloat() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }
}
