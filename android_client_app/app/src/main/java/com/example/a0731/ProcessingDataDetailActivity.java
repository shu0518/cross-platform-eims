package com.example.a0731;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.view.View;

public class ProcessingDataDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView door_widthTextView;
    private TextView door_heightTextView;
    private TextView door_piece_widthTextView;
    private TextView door_piece_heightTextView;
    private TextView glass_widthTextView;
    private TextView glass_lengthTextView;
    private TextView glass_second_widthTextView;
    private TextView top_flower_net_thing_widthTextView;
    private TextView top_flower_net_thing_heightTextView;
    private TextView top_flower_net_widthTextView;
    private TextView top_flower_net_heightTextView;
    private TextView mid_flower_net_thing_heightTextView;
    private TextView mid_flower_net_heightTextView;
    private TextView bottom_flower_board_widthTextView;
    private TextView bottom_flower_board_heightTextView;
    private TextView flower_board_widthTextView;
    private TextView flower_board_heightTextView;
    private TextView flower_widthTextView;
    private TextView flower_heightTextView;
    private TextView leaf_widthTextView;
    private TextView leaf_heightTextView;
    private TextView leaf_numberTextView;
    private TextView window_horiTextView;
    private TextView out_window_horiTextView;
    private TextView screen_window_horiTextView;
    private TextView screen_window_branchTextView;
    private TextView inter_branchTextView;
    private TextView exter_branchTextView;
    private TextView bottom_line_1TextView;
    private TextView left_lineTextView;
    private TextView inner_lineTextView;
    private TextView bottom_line_2TextView;
    private TextView screen_window_midTextView;
    private TextView flower_grid_widthTextView;
    private TextView flower_grid_heightTextView;
    private TextView flower_net_widthTextView;
    private TextView flower_net_heightTextView;
    private ImageView completedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_data_detail);

        String measure_id = getIntent().getStringExtra("id");
        // 初始化視圖元件
        door_widthTextView = findViewById(R.id.door_widthTextView);
        door_heightTextView = findViewById(R.id.door_heightTextView);
        door_piece_widthTextView = findViewById(R.id.door_piece_widthTextView);
        door_piece_heightTextView = findViewById(R.id.door_piece_heightTextView);
        glass_widthTextView = findViewById(R.id.glass_widthTextView);
        glass_lengthTextView = findViewById(R.id.glass_lengthTextView);
        glass_second_widthTextView = findViewById(R.id.glass_second_widthTextView);
        top_flower_net_thing_widthTextView = findViewById(R.id.top_flower_net_thing_widthTextView);
        top_flower_net_thing_heightTextView = findViewById(R.id.top_flower_net_thing_heightTextView);
        top_flower_net_widthTextView = findViewById(R.id.top_flower_net_widthTextView);
        top_flower_net_heightTextView = findViewById(R.id.top_flower_net_heightTextView);
        mid_flower_net_thing_heightTextView = findViewById(R.id.mid_flower_net_thing_heightTextView);
        mid_flower_net_heightTextView = findViewById(R.id.mid_flower_net_heightTextView);
        bottom_flower_board_widthTextView = findViewById(R.id.bottom_flower_board_widthTextView);
        bottom_flower_board_heightTextView = findViewById(R.id.bottom_flower_board_heightTextView);
        flower_board_widthTextView = findViewById(R.id.flower_board_widthTextView);
        flower_board_heightTextView = findViewById(R.id.flower_board_heightTextView);
        flower_widthTextView = findViewById(R.id.flower_widthTextView);
        flower_heightTextView = findViewById(R.id.flower_heightTextView);
        leaf_widthTextView = findViewById(R.id.leaf_widthTextView);
        leaf_heightTextView = findViewById(R.id.leaf_heightTextView);
        leaf_numberTextView = findViewById(R.id.leaf_numberTextView);
        window_horiTextView = findViewById(R.id.window_horiTextView);
        out_window_horiTextView = findViewById(R.id.out_window_horiTextView);
        screen_window_horiTextView = findViewById(R.id.screen_window_horiTextView);
        screen_window_branchTextView = findViewById(R.id.screen_window_branchTextView);
        inter_branchTextView = findViewById(R.id.inter_branchTextView);
        exter_branchTextView = findViewById(R.id.exter_branchTextView);
        bottom_line_1TextView = findViewById(R.id.bottom_line_1TextView);
        left_lineTextView = findViewById(R.id.left_lineTextView);
        inner_lineTextView = findViewById(R.id.inner_lineTextView);
        bottom_line_2TextView = findViewById(R.id.bottom_line_2TextView);
        screen_window_midTextView = findViewById(R.id.screen_window_midTextView);
        flower_grid_widthTextView = findViewById(R.id.flower_grid_widthTextView);
        flower_grid_heightTextView = findViewById(R.id.flower_grid_heightTextView);
        flower_net_widthTextView = findViewById(R.id.flower_net_widthTextView);
        flower_net_heightTextView = findViewById(R.id.flower_net_heightTextView);
        completedImageView = findViewById(R.id.completedImageView);

        // 初始化返回按鈕
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProcessingDataDetailActivity.this, ProcessingDataActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // 確認上傳圖片的按鈕
        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> openImagePicker()); // 呼叫圖片選擇器

        new FetchmachineTask().execute(measure_id);
    }

    // 開啟圖片選擇器的方法
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "選擇圖片"), PICK_IMAGE_REQUEST);
    }
    // 處理圖片選擇結果的方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // 將選擇的圖片轉換為 Bitmap 並顯示於 ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                completedImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "無法載入圖片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchmachineTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String measure_id = params[0];
            String result = "";
            try {
                URL url = new URL("http://163.17.135.120/api/fetch_machine_data.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("measure_id=" + measure_id);
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
                // 確保 JSON 中的值是否為 "null" 或 "0.00" 並進行相應處理
                if (jsonObject.getString("door_width").equals("null") || jsonObject.getString("door_width").equals("0.00")) {
                    door_widthTextView.setVisibility(View.GONE);
                } else {
                    door_widthTextView.setText(door_widthTextView.getText().toString() + jsonObject.getString("door_width"));
                }

                if (jsonObject.getString("door_height").equals("null") || jsonObject.getString("door_height").equals("0.00")) {
                    door_heightTextView.setVisibility(View.GONE);
                } else {
                    door_heightTextView.setText(door_heightTextView.getText().toString() + jsonObject.getString("door_height"));
                }

                if (jsonObject.getString("door_piece_width").equals("null") || jsonObject.getString("door_piece_width").equals("0.00")) {
                    door_piece_widthTextView.setVisibility(View.GONE);
                } else {
                    door_piece_widthTextView.setText(door_piece_widthTextView.getText().toString() + jsonObject.getString("door_piece_width"));
                }

                if (jsonObject.getString("door_piece_height").equals("null") || jsonObject.getString("door_piece_height").equals("0.00")) {
                    door_piece_heightTextView.setVisibility(View.GONE);
                } else {
                    door_piece_heightTextView.setText(door_piece_heightTextView.getText().toString() + jsonObject.getString("door_piece_height"));
                }

                if (jsonObject.getString("glass_width").equals("null") || jsonObject.getString("glass_width").equals("0.00")) {
                    glass_widthTextView.setVisibility(View.GONE);
                } else {
                    glass_widthTextView.setText(glass_widthTextView.getText().toString() + jsonObject.getString("glass_width"));
                }

                if (jsonObject.getString("glass_height").equals("null") || jsonObject.getString("glass_height").equals("0.00")) {
                    glass_lengthTextView.setVisibility(View.GONE);
                } else {
                    glass_lengthTextView.setText(glass_lengthTextView.getText().toString() + jsonObject.getString("glass_height"));
                }

                if (jsonObject.getString("glass_second_width").equals("null") || jsonObject.getString("glass_second_width").equals("0.00")) {
                    glass_second_widthTextView.setVisibility(View.GONE);
                } else {
                    glass_second_widthTextView.setText(glass_second_widthTextView.getText().toString() + jsonObject.getString("glass_second_width"));
                }

                if (jsonObject.getString("top_flower_net_thing_width").equals("null") || jsonObject.getString("top_flower_net_thing_width").equals("0.00")) {
                    top_flower_net_thing_widthTextView.setVisibility(View.GONE);
                } else {
                    top_flower_net_thing_widthTextView.setText(top_flower_net_thing_widthTextView.getText().toString() + jsonObject.getString("top_flower_net_thing_width"));
                }

                if (jsonObject.getString("top_flower_net_thing_height").equals("null") || jsonObject.getString("top_flower_net_thing_height").equals("0.00")) {
                    top_flower_net_thing_heightTextView.setVisibility(View.GONE);
                } else {
                    top_flower_net_thing_heightTextView.setText(top_flower_net_thing_heightTextView.getText().toString() + jsonObject.getString("top_flower_net_thing_height"));
                }

                if (jsonObject.getString("top_flower_net_width").equals("null") || jsonObject.getString("top_flower_net_width").equals("0.00")) {
                    top_flower_net_widthTextView.setVisibility(View.GONE);
                } else {
                    top_flower_net_widthTextView.setText(top_flower_net_widthTextView.getText().toString() + jsonObject.getString("top_flower_net_width"));
                }

                if (jsonObject.getString("top_flower_net_height").equals("null") || jsonObject.getString("top_flower_net_height").equals("0.00")) {
                    top_flower_net_heightTextView.setVisibility(View.GONE);
                } else {
                    top_flower_net_heightTextView.setText(top_flower_net_heightTextView.getText().toString() + jsonObject.getString("top_flower_net_height"));
                }

                if (jsonObject.getString("mid_flower_net_thing_height").equals("null") || jsonObject.getString("mid_flower_net_thing_height").equals("0.00")) {
                    mid_flower_net_thing_heightTextView.setVisibility(View.GONE);
                } else {
                    mid_flower_net_thing_heightTextView.setText(mid_flower_net_thing_heightTextView.getText().toString() + jsonObject.getString("mid_flower_net_thing_height"));
                }

                if (jsonObject.getString("mid_flower_net_height").equals("null") || jsonObject.getString("mid_flower_net_height").equals("0.00")) {
                    mid_flower_net_heightTextView.setVisibility(View.GONE);
                } else {
                    mid_flower_net_heightTextView.setText(mid_flower_net_heightTextView.getText().toString() + jsonObject.getString("mid_flower_net_height"));
                }

                if (jsonObject.getString("bottom_flower_board_width").equals("null") || jsonObject.getString("bottom_flower_board_width").equals("0.00")) {
                    bottom_flower_board_widthTextView.setVisibility(View.GONE);
                } else {
                    bottom_flower_board_widthTextView.setText(bottom_flower_board_widthTextView.getText().toString() + jsonObject.getString("bottom_flower_board_width"));
                }

                if (jsonObject.getString("bottom_flower_board_height").equals("null") || jsonObject.getString("bottom_flower_board_height").equals("0.00")) {
                    bottom_flower_board_heightTextView.setVisibility(View.GONE);
                } else {
                    bottom_flower_board_heightTextView.setText(bottom_flower_board_heightTextView.getText().toString() + jsonObject.getString("bottom_flower_board_height"));
                }

                if (jsonObject.getString("flower_board_width").equals("null") || jsonObject.getString("flower_board_width").equals("0.00")) {
                    flower_board_widthTextView.setVisibility(View.GONE);
                } else {
                    flower_board_widthTextView.setText(flower_board_widthTextView.getText().toString() + jsonObject.getString("flower_board_width"));
                }

                if (jsonObject.getString("flower_board_height").equals("null") || jsonObject.getString("flower_board_height").equals("0.00")) {
                    flower_board_heightTextView.setVisibility(View.GONE);
                } else {
                    flower_board_heightTextView.setText(flower_board_heightTextView.getText().toString() + jsonObject.getString("flower_board_height"));
                }

                if (jsonObject.getString("flower_width").equals("null") || jsonObject.getString("flower_width").equals("0.00")) {
                    flower_widthTextView.setVisibility(View.GONE);
                } else {
                    flower_widthTextView.setText(flower_widthTextView.getText().toString() + jsonObject.getString("flower_width"));
                }

                if (jsonObject.getString("flower_height").equals("null") || jsonObject.getString("flower_height").equals("0.00")) {
                    flower_heightTextView.setVisibility(View.GONE);
                } else {
                    flower_heightTextView.setText(flower_heightTextView.getText().toString() + jsonObject.getString("flower_height"));
                }

                if (jsonObject.getString("leaf_width").equals("null") || jsonObject.getString("leaf_width").equals("0.00")) {
                    leaf_widthTextView.setVisibility(View.GONE);
                } else {
                    leaf_widthTextView.setText(leaf_widthTextView.getText().toString() + jsonObject.getString("leaf_width"));
                }

                if (jsonObject.getString("leaf_height").equals("null") || jsonObject.getString("leaf_height").equals("0.00")) {
                    leaf_heightTextView.setVisibility(View.GONE);
                } else {
                    leaf_heightTextView.setText(leaf_heightTextView.getText().toString() + jsonObject.getString("leaf_height"));
                }

                if (jsonObject.getString("leaf_number").equals("null") || jsonObject.getString("leaf_number").equals("0")) {
                    leaf_numberTextView.setVisibility(View.GONE);
                } else {
                    leaf_numberTextView.setText(leaf_numberTextView.getText().toString() + jsonObject.getString("leaf_number"));
                }

                if (jsonObject.getString("window_hori").equals("null") || jsonObject.getString("window_hori").equals("0.00")) {
                    window_horiTextView.setVisibility(View.GONE);
                } else {
                    window_horiTextView.setText(window_horiTextView.getText().toString() + jsonObject.getString("window_hori"));
                }

                if (jsonObject.getString("out_window_hori").equals("null") || jsonObject.getString("out_window_hori").equals("0.00")) {
                    out_window_horiTextView.setVisibility(View.GONE);
                } else {
                    out_window_horiTextView.setText(out_window_horiTextView.getText().toString() + jsonObject.getString("out_window_hori"));
                }

                if (jsonObject.getString("screen_window_hori").equals("null") || jsonObject.getString("screen_window_hori").equals("0.00")) {
                    screen_window_horiTextView.setVisibility(View.GONE);
                } else {
                    screen_window_horiTextView.setText(screen_window_horiTextView.getText().toString() + jsonObject.getString("screen_window_hori"));
                }

                if (jsonObject.getString("screen_window_branch").equals("null") || jsonObject.getString("screen_window_branch").equals("0.00")) {
                    screen_window_branchTextView.setVisibility(View.GONE);
                } else {
                    screen_window_branchTextView.setText(screen_window_branchTextView.getText().toString() + jsonObject.getString("screen_window_branch"));
                }

                if (jsonObject.getString("inter_branch").equals("null") || jsonObject.getString("inter_branch").equals("0.00")) {
                    inter_branchTextView.setVisibility(View.GONE);
                } else {
                    inter_branchTextView.setText(inter_branchTextView.getText().toString() + jsonObject.getString("inter_branch"));
                }

                if (jsonObject.getString("exter_branch").equals("null") || jsonObject.getString("exter_branch").equals("0.00")) {
                    exter_branchTextView.setVisibility(View.GONE);
                } else {
                    exter_branchTextView.setText(exter_branchTextView.getText().toString() + jsonObject.getString("exter_branch"));
                }

                if (jsonObject.getString("bottom_line_1").equals("null") || jsonObject.getString("bottom_line_1").equals("0.00")) {
                    bottom_line_1TextView.setVisibility(View.GONE);
                } else {
                    bottom_line_1TextView.setText(bottom_line_1TextView.getText().toString() + jsonObject.getString("bottom_line_1"));
                }

                if (jsonObject.getString("left_line").equals("null") || jsonObject.getString("left_line").equals("0.00")){
                    left_lineTextView.setVisibility(View.GONE);
                } else {
                    left_lineTextView.setText(left_lineTextView.getText().toString() + jsonObject.getString("left_line"));
                }
                if (jsonObject.getString("inner_line").equals("null") || jsonObject.getString("inner_line").equals("0.00")){
                    inner_lineTextView.setVisibility(View.GONE);
                } else {
                    inner_lineTextView.setText(inner_lineTextView.getText().toString() + jsonObject.getString("inner_line"));
                }
                if (jsonObject.getString("bottom_line_2").equals("null") || jsonObject.getString("bottom_line_2").equals("0.00")){
                    bottom_line_2TextView.setVisibility(View.GONE);
                } else {
                    bottom_line_2TextView.setText(bottom_line_2TextView.getText().toString() + jsonObject.getString("bottom_line_2"));
                }
                if (jsonObject.getString("screen_window_mid").equals("null") || jsonObject.getString("screen_window_mid").equals("0.00")){
                    screen_window_midTextView.setVisibility(View.GONE);
                } else {
                    screen_window_midTextView.setText(screen_window_midTextView.getText().toString() + jsonObject.getString("screen_window_mid"));
                }
                if (jsonObject.getString("flower_grid_width").equals("null") || jsonObject.getString("flower_grid_width").equals("0.00")){
                    flower_grid_widthTextView.setVisibility(View.GONE);
                } else {
                    flower_grid_widthTextView.setText(flower_grid_widthTextView.getText().toString() + jsonObject.getString("flower_grid_width"));
                }
                if (jsonObject.getString("flower_grid_height").equals("null") || jsonObject.getString("flower_grid_height").equals("0.00")){
                    flower_grid_heightTextView.setVisibility(View.GONE);
                } else {
                    flower_grid_heightTextView.setText(flower_grid_heightTextView.getText().toString() + jsonObject.getString("flower_grid_height"));
                }
                if (jsonObject.getString("flower_net_width").equals("null") || jsonObject.getString("flower_net_width").equals("0.00")){
                    flower_net_widthTextView.setVisibility(View.GONE);
                } else {
                    flower_net_widthTextView.setText(flower_net_widthTextView.getText().toString() + jsonObject.getString("flower_net_width"));
                }
                if (jsonObject.getString("flower_net_height").equals("null") || jsonObject.getString("flower_net_height").equals("0.00")){
                    flower_net_heightTextView.setVisibility(View.GONE);
                } else {
                    flower_net_heightTextView.setText(flower_net_heightTextView.getText().toString() + jsonObject.getString("flower_net_height"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}