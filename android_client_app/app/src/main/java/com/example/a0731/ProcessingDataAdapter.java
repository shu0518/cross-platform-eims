package com.example.a0731;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProcessingDataAdapter extends RecyclerView.Adapter<ProcessingDataAdapter.ViewHolder> /*implements Filterable*/ {

    private List<ProcessingData> processingDataList;
    //private List<ProcessingData> processingDataListFull;
    private String account;
    private Context context;
    private boolean showCheckBox;

    public ProcessingDataAdapter(Context context, List<ProcessingData> processingDataList, String account, boolean showCheckBox) {
        this.context = context;
        this.processingDataList = processingDataList;
        //this.processingDataListFull = new ArrayList<>(processingDataList);
        this.account=account;
        this.showCheckBox = showCheckBox;

        // 初始化全选框的逻辑
//        if (selectAllCheckBox != null) {
//            selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                for (ProcessingData data : processingDataList) {
//                    data.setSelected(isChecked);
//                }
//                notifyDataSetChanged();
//            });
//        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_processing_data, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProcessingData data = processingDataList.get(position);
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
            holder.itemView.setOnClickListener(null); // 禁用 itemView 的點擊事件
        } else {
            holder.selectCheckBox.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProcessingDataDetailActivity.class);
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

        // 設置 CheckBox 的點擊事件
//        holder.processingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            data.setSelected(isChecked);
//            // 檢查是否需要取消全选框的勾选状态
//            if (!isChecked && selectAllCheckBox != null && selectAllCheckBox.isChecked()) {
//                selectAllCheckBox.setOnCheckedChangeListener(null);
//                selectAllCheckBox.setChecked(false);
//                selectAllCheckBox.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
//                    for (ProcessingData item : processingDataList) {
//                        item.setSelected(isChecked1);
//                    }
//                    notifyDataSetChanged();
//                });
//            } else if (isChecked) {
//                // 如果所有項目都被選中，則勾選全選框
//                boolean allSelected = true;
//                for (ProcessingData item : processingDataList) {
//                    if (!item.isSelected()) {
//                        allSelected = false;
//                        break;
//                    }
//                }
//                if (allSelected && selectAllCheckBox != null && !selectAllCheckBox.isChecked()) {
//                    selectAllCheckBox.setOnCheckedChangeListener(null);
//                    selectAllCheckBox.setChecked(true);
//                    selectAllCheckBox.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
//                        for (ProcessingData item : processingDataList) {
//                            item.setSelected(isChecked1);
//                        }
//                        notifyDataSetChanged();
//                    });
//                }
//            }
//        });
    }


    @Override
    public int getItemCount() {
        return processingDataList.size();
    }

//    @Override
//    public Filter getFilter() {
//        return processingDataFilter;
//    }
//
//    private Filter processingDataFilter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            List<ProcessingData> filteredList = new ArrayList<>();
//            if (constraint == null || constraint.length() == 0) {
//                filteredList.addAll(processingDataListFull);
//            } else {
//                String filterPattern = constraint.toString().toLowerCase().trim();
//                for (ProcessingData item : processingDataListFull) {
//                    if (item.getFrameNumber().toLowerCase().contains(filterPattern) ||
//                            item.getFloor().toLowerCase().contains(filterPattern) ||
//                            item.getLayout().toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item);
//                    }
//                }
//            }
//            FilterResults results = new FilterResults();
//            results.values = filteredList;
//            return results;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            processingDataList.clear();
//            processingDataList.addAll((List<ProcessingData>) results.values);
//            notifyDataSetChanged();
//        }
//    };

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
