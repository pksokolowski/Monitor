package com.example.sokol.monitor.EasyCatsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.Help.HelpProvider;
import com.example.sokol.monitor.LogsDialog.Log;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class EasyUICatsFragment extends DialogFragment implements EasyCatsAdapter.OnItemSelectedListener, View.OnClickListener, OnStartDragListener {
    private View mView;

    private List<CatData> mCats;
    private EasyCatsAdapter mCatsAdapter;
    private RecyclerView mRecycler;
    private FloatingActionButton mFab;
    private TextView mTitle;

    private ItemTouchHelper mItemTouchHelper;
    private ArrayAdapter<String> mDeletedCatsAdapter;

    OnNeedUserInterfaceUpdate mUserInterfaceUpdater;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the required interfaces. If not, it throws an exception
        try {
            mUserInterfaceUpdater = (OnNeedUserInterfaceUpdate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_dialog, null);

        // obtain a list of categories
        mCats = DbHelper.getInstance(mView.getContext()).getCategories(CatData.CATEGORY_STATUS_INACTIVE);
        mCatsAdapter = new EasyCatsAdapter(mCats, this);

        setupUIElements();

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mCatsAdapter);

        mCatsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }

    private void setupUIElements(){
        mRecycler = mView.findViewById(R.id.easy_ui_recycler);
        mFab = mView.findViewById(R.id.easy_ui_fab);
        mTitle = mView.findViewById(R.id.easy_ui_title);

        mTitle.setText(R.string.easy_ui_cats_title);
        mFab.setOnClickListener(this);

        mItemTouchHelper = SimpleCallbackHelper.attachSimpleItemTouchHelper(mRecycler, mCatsAdapter, mDeletedCatsAdapter);

        ImageView helpImage = mView.findViewById(R.id.easy_ui_help);
        helpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpProvider.requestHelp((AppCompatActivity)getActivity(), HelpProvider.TOPIC_CATS);
            }
        });
    }

    @Override
    public void onItemSelected(int i, CatData cat) {
        displayEditor(cat, i);
    }

    /**
     * fab (floating action button) click
     */
    @Override
    public void onClick(View view) {
        displayEditor(null, 0);
    }

    private void displayEditor(CatData cat, int i){

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        // dragging the cat in the recycler
        mItemTouchHelper.startDrag(viewHolder);
    }
}