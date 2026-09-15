package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetPasswordButton;
    private ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        closeButton = findViewById(R.id.closeButton);

        // 獲取從 VerifyCodeActivity 傳過來的帳號資訊
        Intent intent = getIntent();
        String account = intent.getStringExtra("account");

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "密碼不一致", Toast.LENGTH_SHORT).show();
                } else {
                    // 開始密碼更新的異步任務
                    new ResetPasswordTask().execute(account, newPassword);
                }
            }
        });

        closeButton.setOnClickListener(v -> finish()); // 返回上一頁
    }

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String account = params[0];
            String newPassword = params[1];
            String result;
            try {
                // 設置伺服器的PHP檔案URL
                URL url = new URL("http://163.17.135.120/api/resetpassword.php");  // 這是處理密碼重置的PHP檔案
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 發送 POST 請求，包含帳號與新密碼
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("account=" + account + "&password=" + newPassword);
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
            if (result.equals("success")) {
                Toast.makeText(ResetPasswordActivity.this, "密碼已成功重置", Toast.LENGTH_LONG).show();

                // 跳轉到 FrontpageActivity，並將帳號傳遞過去
                Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                intent.putExtra("account", getIntent().getStringExtra("account"));
                startActivity(intent);
            } else {
                Toast.makeText(ResetPasswordActivity.this, "密碼重置失敗: " + result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
