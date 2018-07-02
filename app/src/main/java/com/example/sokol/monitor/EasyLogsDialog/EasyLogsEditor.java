package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.DateTimePicker.DateTimePicker;
import com.example.sokol.monitor.ErrorMessageConcatenator;
import com.example.sokol.monitor.Log;
import com.example.sokol.monitor.PopUpMessage;
import com.example.sokol.monitor.R;
import com.example.sokol.monitor.TimeHelper;

import java.util.List;

public class EasyLogsEditor extends DialogFragment implements Dialog.OnClickListener, View.OnClickListener {
    private View mView;
    private Log mLog;
    // index of the log, for example in recyclerViews adapter.
    // this is kept to make sure the owner class get's back the right index along with the log.
    private List<CatData> mCats;
    private long mLogID = -1;
    private ArrayAdapter<String> mCatsAdapter;

    private Spinner mSpinner;
    private DateTimePicker mStartTime;
    private DateTimePicker mEndTime;

    public void setCats(List<CatData> cats){
        mCats = cats;
    }

    public void setLog(Log log){
        mLog = log;
        if(mLog == null) return;
        mLogID = log.getID();
    }

    public void setOnInteractionEndedListener(OnInteractionEnded listener){
        mListener = listener;
    }
    private OnInteractionEnded mListener;

    public interface OnInteractionEnded{
        void onLogCreated(Log log);
        void onLogDeleted(long logID);
        void onLogChanged(long logID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_logs_editor, null);

        setupUI();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setNeutralButton(R.string.easy_ui_button_delete, this)
                .setPositiveButton(R.string.easy_ui_button_save, null)
                .setNegativeButton(R.string.easy_ui_button_cancel, null);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    }

    private void setupUI(){
        mSpinner = mView.findViewById(R.id.spinner);
        mCatsAdapter = SpinnerCatsHelper.setupSpinner(mCats, mView, mSpinner);
        mStartTime = mView.findViewById(R.id.start_time_picker);
        mEndTime = mView.findViewById(R.id.end_time_picker);

        if(mLog == null) {
            long timeNow = TimeHelper.now();
            mStartTime.setValue(timeNow);
            mEndTime.setValue(timeNow);
        }else{
            // set according to the mLog
            int catToSelect = SpinnerCatsHelper.getSpinnerIndexForCatTitle(mCatsAdapter, mLog.getCatTitle());
            if (catToSelect != -1) mSpinner.setSelection(catToSelect);

            mStartTime.setValue(mLog.getStartTime());
            mEndTime.setValue(mLog.getEndTime());
        }
    }

    private boolean isInputCorrect(boolean showErrorMessages){
       ErrorMessageConcatenator errors = new ErrorMessageConcatenator();

        if (mSpinner.getSelectedItemPosition() == 0) {
            errors.add(getString(R.string.logs_dialog_error_no_cat_selected));
        }

        if (mStartTime.getValue() >= mEndTime.getValue()) {
            errors.add(getString(R.string.logs_dialog_error_start_time_not_before_end_time));
        }

        if (errors.size() == 0) return true;

        if (showErrorMessages) {
            PopUpMessage.pop(getActivity(), errors.getMultilineErrorsString());
        }
        return false;
    }

    private void applyInputToALog(Log log){
        log.setCat(SpinnerCatsHelper.getCatForCurrentSpinnerSelection(mSpinner, mCats));
        log.setStartTime(mStartTime.getValue());
        log.setEndTime(mEndTime.getValue());
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        // deleting log
        if(mLog == null) return;
        DbHelper db = DbHelper.getInstance(getActivity());
        db.deleteLog(mLogID);
        if(mListener == null) return;
        mListener.onLogDeleted(mLogID);
    }

    @Override
    public void onClick(View view) {
        // save log
        if(!isInputCorrect(true)) return;

        DbHelper db = DbHelper.getInstance(getActivity());
        CatData cat = SpinnerCatsHelper.getCatForCurrentSpinnerSelection(mSpinner, mCats);
        long start = mStartTime.getValue();
        long end = mEndTime.getValue();

        if(mLog == null) {
            // add new
            long logID = db.pushLog(cat.getID(), start, end);
            mLog = new Log(logID, cat.getInitial(), cat.getTitle(), start, end);
            dismiss();
            if (mListener == null) return;
            mListener.onLogCreated(mLog);
        }else {
            // change existing
            applyInputToALog(mLog);
            db.changeLog(mLogID, cat.getID(), start, end);
            dismiss();
            if (mListener == null) return;
            mListener.onLogChanged(mLogID);
        }
    }
}
