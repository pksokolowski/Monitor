package com.example.sokol.monitor.EasyCatsDialog;

import android.annotation.SuppressLint;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.sokol.monitor.model.CatData;
import com.example.sokol.monitor.R;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sokol on 23.03.2018.
 */

public class EasyCatsAdapter extends RecyclerView.Adapter<EasyCatsAdapter.ItemViewHolder> {

    // this list holds the data.
    private List<CatData> mItems;

    private OnReorderDragListener mStartDragListener;
    private OnCatCheckedChange mCatCheckedChangeListener;

    public EasyCatsAdapter(List<CatData> data_to_show, OnReorderDragListener dragListener, OnCatCheckedChange catCheckedChangeListener){
        mItems = data_to_show;
        mStartDragListener = dragListener;
        mCatCheckedChangeListener = catCheckedChangeListener;
    }

    public void addACat(CatData cat, int index) {
        // search for collisions:
        for(int i =0; i<mItems.size();i++){
            CatData c = mItems.get(i);
            if(c.getTitle().equals(cat.getTitle())){
                // kolizja - cat already exists
                return;
            }
        }

        mItems.add(index, cat);
        notifyItemInserted(index);
    }

    public CatData getCatAt(int pos){
        return mItems.get(pos);
    }

    @Override
    public EasyCatsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cats_recyclerview_item, parent, false);
        return new EasyCatsAdapter.ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final EasyCatsAdapter.ItemViewHolder holder, int position) {
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
                mCatCheckedChangeListener.onCatChackerChange(kitten, holder.getAdapterPosition());
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                int adapterPos = holder.getAdapterPosition();
                fireItemSelectedEvent(adapterPos, mItems.get(adapterPos));
            }
        });

        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mStartDragListener.onStartReorderDrag(holder);
                }
                return false;
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
                mStartDragListener.onEndReorderDrag(mItems.get(i).getID(), mItems.get(i+1).getID());
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(mItems, i, i - 1);
                mStartDragListener.onEndReorderDrag(mItems.get(i).getID(), mItems.get(i-1).getID());
            }
        }
        notifyItemMoved(fromPos, toPos);
    }

    public void remove(int pos){
        mItems.remove(pos);
        notifyItemRemoved(pos);
    }

    public void change(int pos){
        notifyItemChanged(pos);
    }

    public void replace(int pos, CatData cat){
        mItems.set(pos, cat);
        notifyItemChanged(pos);
    }

    public List<CatData> getAllCats() {
        return mItems;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleView;
        public final TextView initialView;
        public final CheckBox statusView;
        public ConstraintLayout layout;
        public ImageView dragHandle;

        public ItemViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title);
            initialView = itemView.findViewById(R.id.initial);
            statusView = itemView.findViewById(R.id.status);
            layout = itemView.findViewById(R.id.layout);
            dragHandle = itemView.findViewById(R.id.drag_handle);
        }
    }

    OnItemSelectedListener mListener;

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    private void fireItemSelectedEvent(int i, CatData cat) {
        if (mListener == null) return;
        mListener.onItemSelected(i, cat);
    }

    interface OnItemSelectedListener {
        void onItemSelected(int i, CatData cat);
    }
}
