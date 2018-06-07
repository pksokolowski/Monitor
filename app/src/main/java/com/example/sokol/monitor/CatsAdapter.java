package com.example.sokol.monitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Sokol on 23.03.2018.
 */

public class CatsAdapter extends RecyclerView.Adapter<CatsAdapter.ItemViewHolder> {

    // this list holds the data.
    private List<CatData> mItems;

    public boolean isDataChanged() {
        return mWasDataChanged;
    }

    private boolean mWasDataChanged = false;

    public CatsAdapter(List<CatData> data_to_show){
        mItems = data_to_show;
    }

    public void addACat(CatData cat) {
        // search for collisions:
        for(int i =0; i<mItems.size();i++){
            CatData c = mItems.get(i);
            if(c.getTitle().equals(cat.getTitle())){
                // kolizja - cat already exists
                return;
            }
        }

        mItems.add(cat);
        notifyItemInserted(mItems.size() - 1);
        mWasDataChanged = true;
    }

    public CatData getCatAt(int pos){
        return mItems.get(pos);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cats_recyclerview_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        CatData cat = mItems.get(position);
        holder.titleView.setText(cat.getTitle());
        holder.initialView.setText(cat.getInitial());
        holder.statusView.setChecked(cat.getStatus()==CatData.CATEGORY_STATUS_ACTIVE);

        holder.statusView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CatData kitten = mItems.get(holder.getAdapterPosition());
                if(b){
                    kitten.setStatus(CatData.CATEGORY_STATUS_ACTIVE);
                }else{
                    kitten.setStatus(CatData.CATEGORY_STATUS_INACTIVE);
                }
                mWasDataChanged = true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
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

    public List<CatData> getAllCats() {
        return mItems;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleView;
        public final TextView initialView;
        public final CheckBox statusView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.start_time);
            initialView = itemView.findViewById(R.id.initial);
            statusView = itemView.findViewById(R.id.status);
        }
    }
}