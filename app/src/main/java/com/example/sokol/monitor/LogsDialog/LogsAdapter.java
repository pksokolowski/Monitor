package com.example.sokol.monitor.LogsDialog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    List<Log> mItems;
    private boolean mWasDataChanged = false;

    public boolean isDataChanged() {
        return mWasDataChanged;
    }

    public LogsAdapter(List<Log> data_to_show){
        mItems = data_to_show;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_dialog_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Log getLogAt(int pos){
        return mItems.get(pos);
    }

    public void move(int fromPos, int toPos){
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                Collections.swap(mItems, i, i + 1);
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(mItems, i, i - 1);
            }
        }
        notifyItemMoved(fromPos, toPos);
        mWasDataChanged = true;
    }

    public void remove(int pos){
        mItems.remove(pos);
        notifyItemRemoved(pos);
        mWasDataChanged = true;
    }

    public void addACat(Log log) {
        mItems.add(log);
        notifyItemInserted(mItems.size() - 1);
        mWasDataChanged = true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView initialView;
        public TextView durationview;
        public TextView startTimeView;
        public ViewHolder(View itemView) {
            super(itemView);
            initialView = itemView.findViewById(R.id.initial);
            durationview = itemView.findViewById(R.id.duration);
            startTimeView = itemView.findViewById(R.id.start_time);
        }
    }
}