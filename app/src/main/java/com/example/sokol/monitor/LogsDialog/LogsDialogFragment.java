package com.example.sokol.monitor.LogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.example.sokol.monitor.LogsData;
import com.example.sokol.monitor.LogsProvider;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.Collections;
import java.util.List;

public class LogsDialogFragment extends DialogFragment {

    OnNeedUserInterfaceUpdate mCallback;
    LogsProvider mDataProvider;
    private View mView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the required interfaces. If not, it throws an exception
        try {
            mCallback = (OnNeedUserInterfaceUpdate) context;
            mDataProvider = (LogsProvider) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate and LogsProvider");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.logs_dialog, null);

        List<Log> data = mDataProvider.getLogs();
        Collections.reverse(data);
        final LogsAdapter adapter = new LogsAdapter(data);

        final RecyclerView recyclerView = mView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("close", null);
        builder.setView(mView);
        builder.setNeutralButton("save changes", null);

        return builder.create();
    }

}