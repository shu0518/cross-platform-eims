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

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private List<Customer> customerList;
    private Context context;
    private String account;
    public CustomerAdapter(Context context, List<Customer> customerList, String account) {
        this.context = context;
        this.customerList = customerList;
        this.account=account;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.customerNameTextView.setText(customer.getName());
        holder.customerAddressTextView.setText(customer.getAddress());
        holder.customerPhoneTextView.setText(customer.getPhone());

        // 設置點擊事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerFunctionActivity.class);
                intent.putExtra("customer_name", customer.getName());
                intent.putExtra("customer_Address", customer.getAddress());
                intent.putExtra("account", account);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView customerNameTextView;
        public TextView customerAddressTextView;
        public TextView customerPhoneTextView;

        public CustomerViewHolder(View view) {
            super(view);
            customerNameTextView = view.findViewById(R.id.customerNameTextView);
            customerAddressTextView = view.findViewById(R.id.customerAddressTextView);
            customerPhoneTextView = view.findViewById(R.id.customerPhoneTextView);
        }
    }
}
