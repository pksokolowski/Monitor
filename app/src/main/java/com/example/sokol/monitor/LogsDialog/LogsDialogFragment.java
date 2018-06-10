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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.DateTimePicker.DateTimePicker;
import com.example.sokol.monitor.LogsProvider;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogsDialogFragment extends DialogFragment
        implements LogsAdapter.OnItemSelectedListener {

    OnNeedUserInterfaceUpdate mCallback;
    LogsProvider mDataProvider;
    private View mView;

    private Spinner mCatSpinner;
    private DateTimePicker mStartPicker;
    private DateTimePicker mEndPicker;
    private Button mClearButton;
    private Button mChangeButton;
    private Button mAddDelButton;

    private LogsAdapter mLogsAdapter;
    private ArrayAdapter<String> mCatsAdapter;
    private List<CatData> mCats;

    // currently selected record
    private Log activeLog = null;
    private int activeLogIndex = -1;

    private Set<Log> deletedLogs = new HashSet<>();
    private Set<Log> changedLogs = new HashSet<>();
    private Set<Log> addedLogs = new HashSet<>();

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

        // obtain a list of categories
        mCats = DbHelper.getInstance(mView.getContext()).getCategories(CatData.CATEGORY_STATUS_INACTIVE);

        setUpUIElements();

        List<Log> data = mDataProvider.getLogs();
        Collections.reverse(data);
        mLogsAdapter = new LogsAdapter(data);

        final RecyclerView recyclerView = mView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mLogsAdapter);

        mLogsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("close", null);
        builder.setView(mView);
        builder.setNeutralButton("save changes", null);

        return builder.create();
    }

    private void setUpUIElements() {
        mCatSpinner = mView.findViewById(R.id.cat_spinner);
        mStartPicker = mView.findViewById(R.id.start_picker);
        mEndPicker = mView.findViewById(R.id.end_picker);
        mClearButton = mView.findViewById(R.id.clear_button);
        mChangeButton = mView.findViewById(R.id.change_button);
        mAddDelButton = mView.findViewById(R.id.add_del_button);

        String[] spinnerOptions = new String[mCats.size() + 1];
        spinnerOptions[0] = "";
        for (int i = 0; i < mCats.size(); i++) {
            spinnerOptions[i + 1] = mCats.get(i).getTitle();
        }

        // populate spinner with mCats and default "Select category" string
        mCatsAdapter = new ArrayAdapter<String>(mView.getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        mCatsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCatSpinner.setAdapter(mCatsAdapter);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearLogEditor();
            }
        });

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set values to the activeLog and save it as Changed.
                if (activeLog == null) return; // TODO: 10.06.2018 diplay error
                applyEditorValuesToLog(activeLog);
                mLogsAdapter.change(activeLogIndex);
                changedLogs.add(activeLog);
                clearLogEditor();
            }
        });

        mAddDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 10.06.2018 depending on activeLog being null or not, delete or create
                if (activeLog == null) {
                    // add new log
                    CatData cat = getCatForCurrentSpinnerSelection();
                    activeLog = new Log(-1, cat.getInitial(), cat.getTitle(), mStartPicker.getValue(), mEndPicker.getValue());
                    mLogsAdapter.addALog(activeLog);
                    addedLogs.add(activeLog);
                    clearLogEditor();
                } else {
                    mLogsAdapter.remove(activeLogIndex);
                    deletedLogs.add(activeLog);
                    clearLogEditor();
                }
            }
        });

    }

    private void applyEditorValuesToLog(Log log) {
        log.setStartTime(mStartPicker.getValue());
        log.setEndTime(mEndPicker.getValue());
        log.setCat(getCatForCurrentSpinnerSelection());
    }

    private void clearLogEditor() {
        mCatSpinner.setSelection(0);
        long timeNow = Calendar.getInstance().getTimeInMillis();
        mStartPicker.setValue(timeNow);
        mEndPicker.setValue(timeNow);
        activeLog = null;
        activeLogIndex = -1;
        setAddDelButtonText();
    }

    private CatData getCatForCurrentSpinnerSelection() {
        return mCats.get(mCatSpinner.getSelectedItemPosition() - 1);
    }

    private int getSpinnerIndexForCatTitle(String title) {
        for (int i = 0; i < mCatsAdapter.getCount(); i++) {
            if (title.equals(mCatsAdapter.getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onItemSelected(int i, Log log) {
        int catToSelect = getSpinnerIndexForCatTitle(log.getCatTitle());
        if (catToSelect != -1) mCatSpinner.setSelection(catToSelect);

        mStartPicker.setValue(log.getStartTime());
        mEndPicker.setValue(log.getEndTime());

        activeLog = log;
        activeLogIndex = i;
        setAddDelButtonText();
    }

    private void setAddDelButtonText() {
        if (activeLog == null) mAddDelButton.setText("add");
        else mAddDelButton.setText("del");
    }
}
