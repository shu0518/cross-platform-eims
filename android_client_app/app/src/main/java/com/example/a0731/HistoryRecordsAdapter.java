package com.example.a0731;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryRecordsAdapter extends RecyclerView.Adapter<HistoryRecordsAdapter.ViewHolder> {

    private List<HistoryRecord> historyRecordsList;
    private String account;
    private Context context;
    private boolean showCheckBox;

//    public interface OnItemClickListener {
//        void onItemClick(HistoryRecord historyRecord);
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.onItemClickListener = listener;
//    }

    public HistoryRecordsAdapter(Context context, List<HistoryRecord> historyRecordsList, String account, boolean showCheckBox) {
        this.context = context;
        this.historyRecordsList = historyRecordsList;
        this.account=account;
        this.showCheckBox = showCheckBox;
    }

    // 獲取選取的 MeasuredData ID 列表
    public List<String> getSelectedIds() {
        List<String> selectedIds = new ArrayList<>();
        for (HistoryRecord data : historyRecordsList) {
            if (data.isSelected()) { // 假設 MeasuredData 有 isSelected() 方法
                selectedIds.add(data.getid());
            }
        }
        return selectedIds;
    }

    // 根據 ID 獲取對應的 MeasuredData
    public HistoryRecord getHistoryRecordById(String id) {
        for (HistoryRecord data : historyRecordsList) {
            if (data.getid().equals(id)) {
                return data;
            }
        }
        return null; // 若未找到則返回 null
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryRecord data = historyRecordsList.get(position);
        holder.specificationTextView.setText(data.getspec()); // 設置規格
        holder.locationTextView.setText(data.getLocation()); // 設置位置
        String size = data.getLength() + " × " + data.getWidth(); // 設置尺寸
        holder.sizeTextView.setText(size); // 設置尺寸顯示
        holder.quantityTextView.setText(data.getQuantity()); // 設置數量

        // 控制 CheckBox 是否顯示
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
        }
    }

    @Override
    public int getItemCount() {
        return historyRecordsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView specificationTextView, locationTextView, sizeTextView, quantityTextView;
        CheckBox selectCheckBox;

        public ViewHolder(View view) {
            super(view);
            specificationTextView = view.findViewById(R.id.specificationTextView);
            locationTextView = view.findViewById(R.id.locationTextView);
            sizeTextView = view.findViewById(R.id.sizeTextView);
            quantityTextView = view.findViewById(R.id.quantityTextView);
            selectCheckBox = view.findViewById(R.id.selectCheckBox);
        }
    }
}