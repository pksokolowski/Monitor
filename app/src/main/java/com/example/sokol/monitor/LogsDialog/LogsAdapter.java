package com.example.sokol.monitor.LogsDialog;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private List<Log> mItems;
    private int lastItemTouched = -1;

    public void unselectAll(){
        if(lastItemTouched == -1) return;
        int oldSelection = lastItemTouched;
        lastItemTouched = -1;
        notifyItemChanged(oldSelection);
    }

    // TODO: 10.06.2018 remove the wasDataChanged thingy, will not be used anyway, as LogsDialogFrag has it's own change tracking capabilities
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log log = mItems.get(position);

        holder.initialView.setText(log.getCatTitle());
        holder.durationview.setText(log.getDurationString());
        holder.startTimeView.setText(log.getStartTimeString());

        holder.layout.setSelected(position == lastItemTouched);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                int adapterPos = holder.getAdapterPosition();
                fireItemSelectedEvent(adapterPos, mItems.get(adapterPos));

                // select item in a visible way
                if (adapterPos == lastItemTouched) return;
                int oldSelection = lastItemTouched;
                lastItemTouched = adapterPos;
                notifyItemChanged(adapterPos);
                if (oldSelection != -1) notifyItemChanged(oldSelection);
            }
        });
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

    public void change(int pos){
        notifyItemChanged(pos);
        mWasDataChanged = true;
    }

    public void addALog(Log log) {
        int pos = 0;
        mItems.add(pos, log);
        notifyItemInserted(pos);
        mWasDataChanged = true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView initialView;
        public TextView durationview;
        public TextView startTimeView;
        public ConstraintLayout layout;
        public ViewHolder(View itemView) {
            super(itemView);
            initialView = itemView.findViewById(R.id.category_displayed_identifier);
            durationview = itemView.findViewById(R.id.duration);
            startTimeView = itemView.findViewById(R.id.start_time);
            layout = itemView.findViewById(R.id.item);
        }
    }

    OnItemSelectedListener mListener;
    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        mListener = listener;
    }
    private void fireItemSelectedEvent(int i, Log log){
        if(mListener==null) return;
        mListener.onItemSelected(i, log);
    }
    interface OnItemSelectedListener{
        void onItemSelected(int i, Log log);
    }
}