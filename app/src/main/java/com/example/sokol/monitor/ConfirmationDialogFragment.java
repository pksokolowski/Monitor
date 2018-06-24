package com.example.sokol.monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmationDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Confirmation required")
                .setMessage(mMessage)
                .setNegativeButton("cancel", null)
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fireOnConfirmationEvent();
                    }
                });

        return builder.create();
    }

    public static void ask(Activity context, String message, OnConfirmationListener listener){
        ConfirmationDialogFragment confirmation = new ConfirmationDialogFragment();
        confirmation.setMessage(message);
        confirmation.setOnConfirmationListener(listener);
        confirmation.show(context.getFragmentManager(), "disable notification confirmation");
    }

    private String mMessage;

    public void setMessage(String message){
        mMessage = message;
    }

    private void fireOnConfirmationEvent() {
        if (mListener == null) return;
        mListener.onConfirmation();
    }

    public void setOnConfirmationListener(OnConfirmationListener listener) {
        mListener = listener;
    }

    private OnConfirmationListener mListener;

    interface OnConfirmationListener {
        void onConfirmation();
    }
}
