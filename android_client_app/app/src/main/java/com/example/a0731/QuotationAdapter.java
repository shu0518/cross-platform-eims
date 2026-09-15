package com.example.a0731;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.a0731.Quotation;

public class QuotationAdapter extends RecyclerView.Adapter<QuotationAdapter.ViewHolder> {

    private List<Quotation> quotationList;

    public QuotationAdapter(List<Quotation> quotationList) {
        this.quotationList = quotationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quotation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quotation quotation = quotationList.get(position);
        holder.projectNameTextView.setText(quotation.getProjectName());
        String size = quotation.getLength() + " × " + quotation.getWidth(); // 設置尺寸
        holder.sizeTextView.setText(size); // 設置尺寸顯示
        holder.quantityTextView.setText(String.valueOf(quotation.getQuantity()));
        holder.unitPriceTextView.setText(String.valueOf(quotation.getUnitPrice()));
        holder.totalPriceTextView.setText(String.valueOf(quotation.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return quotationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectNameTextView;
        TextView quantityTextView;
        TextView unitPriceTextView;
        TextView totalPriceTextView;
        TextView sizeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitPriceTextView = itemView.findViewById(R.id.unitPriceTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
        }
    }

}
