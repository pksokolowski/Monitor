package com.example.sokol.monitor.EasyCatsDialog;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.ArrayAdapter;

import com.example.sokol.monitor.CatsAdapter;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class SimpleCallbackHelper {

    public static ItemTouchHelper attachSimpleItemTouchHelper(RecyclerView recycler, final EasyCatsAdapter adapter){
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getSimpleCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recycler);

        return itemTouchHelper;
    }

    private static ItemTouchHelper.SimpleCallback getSimpleCallback(EasyCatsAdapter adapter)
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(UP | DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                // move item in `fromPos` to `toPos` in adapter.

                adapter.move(fromPos, toPos);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }



            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };

        return simpleCallback;
    }
}
