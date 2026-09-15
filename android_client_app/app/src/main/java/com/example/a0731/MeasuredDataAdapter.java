package com.example.a0731;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class MeasuredDataAdapter extends RecyclerView.Adapter<MeasuredDataAdapter.ViewHolder> {

    private List<MeasuredData> measuredDataList;
    private Context context;
    private boolean showCheckBox;
    private String account;

    // 建構函數
    public MeasuredDataAdapter(Context context, List<MeasuredData> measuredDataList, String account, boolean showCheckBox) {
        this.context = context;
        this.measuredDataList = measuredDataList;
        this.account=account;
        this.showCheckBox = showCheckBox;
    }

    // 獲取選取的 MeasuredData ID 列表
    public List<String> getSelectedIds() {
        List<String> selectedIds = new ArrayList<>();
        for (MeasuredData data : measuredDataList) {
            if (data.isSelected()) { // 假設 MeasuredData 有 isSelected() 方法
                selectedIds.add(data.getid());
            }
        }
        return selectedIds;
    }

    // 根據 ID 獲取對應的 MeasuredData
    public MeasuredData getMeasuredDataById(String id) {
        for (MeasuredData data : measuredDataList) {
            if (data.getid().equals(id)) {
                return data;
            }
        }
        return null; // 若未找到則返回 null
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measured_data, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeasuredData data = measuredDataList.get(position);
        holder.specificationTextView.setText(data.getspec()); // 設置規格
        holder.locationTextView.setText(data.getLocation()); // 設置位置
        String size = data.getLength() + " × " + data.getWidth(); // 設置尺寸
        holder.sizeTextView.setText(size); // 設置尺寸顯示
        holder.quantityTextView.setText(data.getQuantity()); // 設置數量

        // 根據 showCheckBox 設置 CheckBox 的可見性
        if (showCheckBox) {
            holder.selectCheckBox.setVisibility(View.VISIBLE);
            holder.selectCheckBox.setChecked(data.isSelected());
            holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> data.setSelected(isChecked));
            //holder.itemView.setOnClickListener(null); // 禁用 itemView 的點擊事件
            holder.itemView.setOnClickListener(v -> {
                // 改變 CheckBox 的選取狀態
                boolean newCheckedState = !data.isSelected();
                holder.selectCheckBox.setChecked(newCheckedState);
                data.setSelected(newCheckedState);
            });
        } else {
            holder.selectCheckBox.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MeasuredDataDetailActivity.class);
                intent.putExtra("id", data.getid());
                intent.putExtra("specification", data.getSpecification());
                intent.putExtra("account", account);
                intent.putExtra("location", data.getLocation());
                intent.putExtra("length", data.getLength());
                intent.putExtra("width", data.getWidth());
                intent.putExtra("quantity", data.getQuantity());
                context.startActivity(intent);
            });
        }
    }

    public void setAllChecked(boolean isChecked) {
        for (MeasuredData data : measuredDataList) {
            data.setSelected(isChecked); // 設定每個項目的選擇狀態
        }
        notifyDataSetChanged(); // 通知適配器更新顯示
    }
    @Override
    public int getItemCount() {
        return measuredDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView specificationTextView, locationTextView, sizeTextView, quantityTextView;
        CheckBox selectCheckBox;

        ViewHolder(View view) {
            super(view);
            specificationTextView = view.findViewById(R.id.specificationTextView);
            locationTextView = view.findViewById(R.id.locationTextView);
            sizeTextView = view.findViewById(R.id.sizeTextView);
            quantityTextView = view.findViewById(R.id.quantityTextView);
            selectCheckBox = view.findViewById(R.id.selectCheckBox);
        }
    }
}
