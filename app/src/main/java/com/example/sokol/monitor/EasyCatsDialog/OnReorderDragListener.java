package com.example.sokol.monitor.EasyCatsDialog;

import androidx.recyclerview.widget.RecyclerView;

public interface OnReorderDragListener {
    void onStartReorderDrag(RecyclerView.ViewHolder viewHolder);
    void onEndReorderDrag(long catA_ID, long catB_ID);
}
