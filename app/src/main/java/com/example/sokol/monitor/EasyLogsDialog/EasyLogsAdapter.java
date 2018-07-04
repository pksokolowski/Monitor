package com.example.sokol.monitor.EasyLogsDialog;


import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sokol.monitor.Log;
import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class EasyLogsAdapter extends RecyclerView.Adapter<EasyLogsAdapter.ViewHolder> {

    private List<Log> mItems;

    public EasyLogsAdapter(List<Log> data_to_show) {
        mItems = data_to_show;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_dialog_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log log = mItems.get(position);

        holder.durationview.setText(log.getDurationString());
        holder.initialView.setText(log.getCatInitial());
        holder.titleView.setText(log.getCatTitle());
        holder.startTimeView.setText(log.getStartTimeString());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                int adapterPos = holder.getAdapterPosition();
                fireItemSelectedEvent(adapterPos, mItems.get(adapterPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Log getLogAt(int pos) {
        return mItems.get(pos);
    }

    public void move(int fromPos, int toPos) {
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
    }

    public void remove(int pos) {
        mItems.remove(pos);
        notifyItemRemoved(pos);
    }

    public void change(int pos) {
        notifyItemChanged(pos);
    }

    public void addALog(Log log) {
        int pos = 0;
        mItems.add(pos, log);
        notifyItemInserted(pos);
    }

    public void addLogs(List<Log> logs) {
        int pos = 0;
        mItems.addAll(pos, logs);
        notifyItemRangeInserted(pos, logs.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView durationview;
        public TextView initialView;
        public TextView titleView;
        public TextView startTimeView;
        public ConstraintLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            durationview = itemView.findViewById(R.id.duration);
            initialView = itemView.findViewById(R.id.category_initial);
            titleView = itemView.findViewById(R.id.category_displayed_identifier);
            startTimeView = itemView.findViewById(R.id.start_time);
            layout = itemView.findViewById(R.id.item);
        }
    }

    OnItemSelectedListener mListener;

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    private void fireItemSelectedEvent(int i, Log log) {
        if (mListener == null) return;
        mListener.onItemSelected(i, log);
    }

    interface OnItemSelectedListener {
        void onItemSelected(int i, Log log);
    }
}