package com.example.sokol.monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class PopUpMessage extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(mMessage)
                .setPositiveButton(R.string.pop_up_got_it, null);

        return builder.create();
    }

    public static void pop(Activity context, String message){
        PopUpMessage popUp = new PopUpMessage();
        popUp.setMessage(message);
        popUp.show(context.getFragmentManager(), "popUp message");
    }

    private String mMessage;

    public void setMessage(String message){
        mMessage = message;
    }
}
