package com.example.sokol.monitor.EasyCatsDialog;

import android.support.v7.widget.RecyclerView;

import com.example.sokol.monitor.CatData;

public interface OnReorderDragListener {
    void onStartReorderDrag(RecyclerView.ViewHolder viewHolder);
    void onEndReorderDrag(CatData cat, int i);
}
