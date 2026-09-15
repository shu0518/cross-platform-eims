package com.example.a0731;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> {

    // 保存所有模型圖片的 Bitmap 列表
    private List<Bitmap> modelImages;
    private OnImageClickListener imageClickListener; // 點擊事件的處理器

    // 定義一個接口，用於處理圖片點擊事件
    public interface OnImageClickListener {
        void onImageClick(Bitmap bitmap);
    }

    // 更新構造函數，接受圖片列表和點擊事件的處理器
    public ModelAdapter(List<Bitmap> modelImages, OnImageClickListener imageClickListener) {
        this.modelImages = modelImages;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 從 item_model_image.xml 加載每個項目的視圖
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_model, // 項目佈局資源
                parent, // 父佈局
                false // 不立即附加到父佈局
        );
        return new ModelViewHolder(view); // 返回新的 ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {
        Bitmap image = modelImages.get(position);
        holder.modelImageView.setImageBitmap(image);

        // 設置圖片點擊事件
        holder.itemView.setOnClickListener(v -> {
            if (imageClickListener != null) {
                imageClickListener.onImageClick(image); // 傳遞點擊的圖片
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelImages.size(); // 返回圖片列表的大小
    }

    // ModelViewHolder 內部類，用於持有每個項目的視圖
    static class ModelViewHolder extends RecyclerView.ViewHolder {
        ImageView modelImageView; // 顯示模型圖片的 ImageView

        public ModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // 從項目視圖中找到 modelImageView
            modelImageView = itemView.findViewById(R.id.modelImageView);
        }
    }
}
