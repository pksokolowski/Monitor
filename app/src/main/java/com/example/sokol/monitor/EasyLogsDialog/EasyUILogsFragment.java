package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;

import com.example.sokol.monitor.R;
import com.example.sokol.monitor.SingleInstanceDialog;

public class EasyUILogsFragment extends DialogFragment {
    private View mView;


    @NonNull
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