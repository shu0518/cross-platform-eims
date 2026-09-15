package com.example.a0731;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
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

public class UserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, emailEditText, passwordEditText;
    private Button cancelButton, editButton;
    private ImageButton backButton;
    private boolean isEditing = false; // 判斷是否在編輯模式
    private String account;  // 帳號
    private String originalName, originalEmail, originalPassword; // 原始資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 初始化視圖
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        cancelButton = findViewById(R.id.cancelButton);
        editButton = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backButton);

        // 獲取傳遞過來的帳號資訊
        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        phoneEditText.setText(account); // 將帳號顯示在 phoneEditText

        // 禁用所有 EditText
        setEditable(false);
        phoneEditText.setEnabled(false);

        // 查詢並顯示用戶資料
        new FetchUserProfileTask().execute(account);

        // 返回按鈕事件
        backButton.setOnClickListener(v -> finish());

        // 取消按鈕事件
        cancelButton.setOnClickListener(v -> {
            if (isEditing) {
                // 取消編輯並恢復原始資料
                nameEditText.setText(originalName);
                phoneEditText.setText(account);
                emailEditText.setText(originalEmail);
                passwordEditText.setText(originalPassword);

                setEditable(false);
                editButton.setText("編輯");
                isEditing = false;
            } else {
                finish();
            }
        });

        // 編輯按鈕事件
        editButton.setOnClickListener(v -> {
            if (isEditing) {
                String email = emailEditText.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(UserProfileActivity.this, "請輸入正確的電子郵件格式", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 保存資料到資料庫
                new UpdateUserProfileTask().execute(
                        nameEditText.getText().toString(),
                        phoneEditText.getText().toString(),
                        account,  // 帳號不變，儲存在 account 欄位
                        emailEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );

                setEditable(false);
                editButton.setText("編輯");
                isEditing = false;
            } else {
                setEditable(true);
                phoneEditText.setEnabled(false);
                editButton.setText("儲存");
                isEditing = true;
            }
        });
    }

    private void setEditable(boolean editable) {
        nameEditText.setEnabled(editable);
        emailEditText.setEnabled(editable);
        passwordEditText.setEnabled(editable);
    }

    // 查詢用戶資料
    private class FetchUserProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String account = params[0];
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/fetch_user_profile.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("account=" + account);
                writer.flush();
                writer.close();

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
                JSONObject jsonObject = new JSONObject(result);
                originalName = jsonObject.getString("member_name");
                originalEmail = jsonObject.getString("member_mail");
                originalPassword = jsonObject.getString("password");

                nameEditText.setText(originalName);
                emailEditText.setText(originalEmail);
                passwordEditText.setText(originalPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 更新用戶資料
    private class UpdateUserProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String name = params[0];
            String phone = params[1];
            String account = params[2];
            String email = params[3];
            String password = params[4];
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/update_user_profile.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("account=" + account + "&name=" + name + "&phone=" + phone + "&email=" + email + "&password=" + password);
                writer.flush();
                writer.close();

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
            if (result.equals("success")) {
                Toast.makeText(UserProfileActivity.this, "資料已更新", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserProfileActivity.this, "資料更新失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
