package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.sokol.monitor.LogsDialog.Log;
import com.example.sokol.monitor.R;

public class EasyLogsEditor extends DialogFragment {
    private View mView;
    private Log mLog;

    public void setOnInteractionEndedListener(OnInteractionEnded listener){
        mListener = listener;
    }
    private OnInteractionEnded mListener;
    public interface OnInteractionEnded{
        void onLogCreated(Log log);
        void onLogDeleted(Log log, int i);
        void onLogChanged(Log log, int i);
        void onCancelled(Log log);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }
}
