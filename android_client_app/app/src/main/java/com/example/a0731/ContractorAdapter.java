package com.example.a0731;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContractorAdapter extends RecyclerView.Adapter<ContractorAdapter.ContractorViewHolder> {

    private List<Contractor> contractorList;
    private Context context;
    private String account;
    public ContractorAdapter(Context context, List<Contractor> contractorList, String account) {
        this.context = context;
        this.contractorList = contractorList;
        this.account=account;
    }

    @NonNull
    @Override
    public ContractorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contractor, parent, false);
        return new ContractorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractorViewHolder holder, int position) {
        Contractor contractor = contractorList.get(position);

        // 設置承包商名稱和電話
        holder.nameTextView.setText(contractor.getName());
        holder.phoneTextView.setText(contractor.getPhone());

        // 點擊事件，跳轉到 CustomerActivity 並傳遞承包商名字
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerActivity.class);
                intent.putExtra("contractor_name", contractor.getName());  // 傳遞承包商名字
                intent.putExtra("account", account);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return contractorList.size();
    }

    public static class ContractorViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView phoneTextView;

        public ContractorViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.contractorNameTextView);
            phoneTextView = view.findViewById(R.id.contractorPhoneTextView);
        }
    }
}
