package com.example.sokol.monitor.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.sokol.monitor.R;

public class ConfirmationDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirmation_dialog_title)
                .setMessage(mMessage)
                .setNegativeButton(R.string.confirmation_dialog_negative_button, null)
                .setPositiveButton(R.string.confirmation_dialog_positive_button, new DialogInterface.OnClickListener() {
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
        confirmation.show(context.getFragmentManager(), "confirmation dialog");
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

    public interface OnConfirmationListener {
        void onConfirmation();
    }
}
