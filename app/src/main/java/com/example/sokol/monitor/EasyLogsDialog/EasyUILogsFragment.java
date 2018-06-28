package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.LogsDialog.Log;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class EasyUILogsFragment extends DialogFragment implements EasyLogsAdapter.OnItemSelectedListener, View.OnClickListener, EasyLogsEditor.OnInteractionEnded {
    private View mView;

    private List<CatData> mCats;
    private EasyLogsAdapter mLogsAdapter;
    private RecyclerView mRecycler;
    private FloatingActionButton mFab;
    private ImageButton mHelpButton;
    private TextView mTitle;

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

        setupUIElements();

        // obtain a list of categories
        mCats = DbHelper.getInstance(mView.getContext()).getCategories(CatData.CATEGORY_STATUS_INACTIVE);

        List<Log> data = Log.getLogsList(getActivity(), Long.MIN_VALUE, Long.MAX_VALUE);
        Collections.reverse(data);
        mLogsAdapter = new EasyLogsAdapter(data);

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mLogsAdapter);

        mLogsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }

    private void setupUIElements(){
        mRecycler = mView.findViewById(R.id.easy_ui_recycler);
        mFab = mView.findViewById(R.id.easy_ui_fab);
        mHelpButton = mView.findViewById(R.id.easy_ui_help_button);
        mTitle = mView.findViewById(R.id.easy_ui_title);

        mFab.setOnClickListener(this);
    }

    @Override
    public void onItemSelected(int i, Log log) {
        displayEditor(log, i);
    }

    /**
     * fab (floating action button) click
     */
    @Override
    public void onClick(View view) {
        displayEditor(null, 0);
    }

    private void displayEditor(Log log, int i){
        EasyLogsEditor editor = new EasyLogsEditor();
        editor.show(getActivity().getSupportFragmentManager(), "LOGS editor");
    }

    // callbacks from EasyLogsEditor
    @Override
    public void onLogCreated(Log log) {
        mLogsAdapter.addALog(log);
        mRecycler.scrollToPosition(0);
    }

    @Override
    public void onLogDeleted(Log log, int i) {
        mLogsAdapter.remove(i);
    }

    @Override
    public void onLogChanged(Log log, int i) {
        mLogsAdapter.change(i);
    }
}