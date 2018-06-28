package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.LogsDialog.Log;
import com.example.sokol.monitor.R;

import java.util.List;

public class EasyLogsEditor extends DialogFragment {
    private View mView;
    private Log mLog;
    private List<CatData> mCats;
    private ArrayAdapter<String> mCatsAdapter;

    public void setCats(List<CatData> cats){
        mCats = cats;
    }

    public void setOnInteractionEndedListener(OnInteractionEnded listener){
        mListener = listener;
    }
    private OnInteractionEnded mListener;
    public interface OnInteractionEnded{
        void onLogCreated(Log log);
        void onLogDeleted(Log log, int i);
        void onLogChanged(Log log, int i);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_logs_editor, null);

        mCatsAdapter = SpinnerCatsHelper.setupSpinner(mCats, mView, R.id.spinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setNeutralButton(R.string.easy_ui_button_delete, null)
                .setPositiveButton(R.string.easy_ui_button_save, null)
                .setNegativeButton(R.string.easy_ui_button_cancel, null);

        return builder.create();
    }
}
