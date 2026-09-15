package com.example.a0731;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText verificationCodeEditText;
    private Button verifyCodeButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        cancelButton = findViewById(R.id.cancelButton);

        // 預設一組驗證碼
        String presetCode = "123456";
        verificationCodeEditText.setText(presetCode);

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCode = verificationCodeEditText.getText().toString().trim();

                // 不論輸入是否正確，都自動通過以便檢視後續頁面
                Toast.makeText(VerifyCodeActivity.this, "驗證成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        // 設置取消按鈕的點擊事件
        cancelButton.setOnClickListener(v -> finish()); // 返回上一頁
    }
}
