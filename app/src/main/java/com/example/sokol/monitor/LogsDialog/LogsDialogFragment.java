package com.example.sokol.monitor.LogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.sokol.monitor.LogsData;
import com.example.sokol.monitor.LogsSelector;
import com.example.sokol.monitor.LogsDataProvider;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

public class LogsDialogFragment extends DialogFragment {

    OnNeedUserInterfaceUpdate mCallback;
    LogsDataProvider mDataProvider;
    private View mView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the required interfaces. If not, it throws an exception
        try {
            mCallback = (OnNeedUserInterfaceUpdate) context;
            mDataProvider = (LogsDataProvider) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate and LogsDataProvider");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mView = inflater.inflate(R.layout.logs_dialog, null);

        LogsData data = mDataProvider.getLogs();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }

}
