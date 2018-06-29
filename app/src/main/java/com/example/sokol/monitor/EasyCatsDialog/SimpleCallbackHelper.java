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

    public static ItemTouchHelper attachSimpleItemTouchHelper(RecyclerView recycler, final EasyCatsAdapter adapter, final ArrayAdapter<String> suggestionsAdapter){
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getSimpleCallback(adapter, suggestionsAdapter));
        itemTouchHelper.attachToRecyclerView(recycler);

        return itemTouchHelper;
    }

    private static ItemTouchHelper.SimpleCallback getSimpleCallback(EasyCatsAdapter adapter, final ArrayAdapter<String> suggestionsAdapter)
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(UP | DOWN, LEFT | RIGHT) {
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
                int pos = viewHolder.getAdapterPosition();

                if (direction == LEFT || direction == RIGHT) {
                    // add to suggested cats:
                    suggestionsAdapter.add(adapter.getCatAt(pos).getTitle());

                    // remove
                    adapter.remove(pos);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };

        return simpleCallback;
    }
}
