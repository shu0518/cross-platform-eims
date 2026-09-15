package com.example.a0731;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WindowFrameAdapter extends RecyclerView.Adapter<WindowFrameAdapter.WindowFrameViewHolder> {

    private List<WindowFrame> windowFrameList;

    public WindowFrameAdapter(List<WindowFrame> windowFrameList) {
        this.windowFrameList = windowFrameList;
    }

    @NonNull
    @Override
    public WindowFrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_window_frame, parent, false);
        return new WindowFrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WindowFrameViewHolder holder, int position) {
        WindowFrame windowFrame = windowFrameList.get(position);
        holder.frameNumberTextView.setText(windowFrame.getFrameNumber());
        holder.floorTextView.setText(windowFrame.getFloor());
        holder.layoutTextView.setText(windowFrame.getLayout());
    }

    @Override
    public int getItemCount() {
        return windowFrameList.size();
    }

    static class WindowFrameViewHolder extends RecyclerView.ViewHolder {
        TextView frameNumberTextView;
        TextView floorTextView;
        TextView layoutTextView;

        public WindowFrameViewHolder(@NonNull View itemView) {
            super(itemView);
            frameNumberTextView = itemView.findViewById(R.id.frameNumberTextView);
            floorTextView = itemView.findViewById(R.id.floorTextView);
            layoutTextView = itemView.findViewById(R.id.layoutTextView);
        }
    }
}
