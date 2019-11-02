package com.example.sokol.monitor.EasyCatsDialog;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

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
