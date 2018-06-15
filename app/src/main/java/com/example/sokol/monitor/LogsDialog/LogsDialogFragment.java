package com.example.sokol.monitor.LogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.CatNameToIDHelper;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.DateTimePicker.DateTimePicker;
import com.example.sokol.monitor.LogsProvider;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogsDialogFragment extends DialogFragment
        implements LogsAdapter.OnItemSelectedListener, DialogInterface.OnClickListener {

    OnNeedUserInterfaceUpdate mCallback;
    LogsProvider mDataProvider;
    private View mView;

    private Spinner mCatSpinner;
    private DateTimePicker mStartPicker;
    private DateTimePicker mEndPicker;
    private Button mClearButton;
    private Button mChangeButton;
    private Button mAddDelButton;
    private ImageButton mEditorExpanderButton;
    private ConstraintLayout mEditorLayout;
    private RecyclerView mRecycler;
    private ConstraintLayout mMyLayout;

    private LogsAdapter mLogsAdapter;
    private ArrayAdapter<String> mCatsAdapter;
    private List<CatData> mCats;

    // currently selected record
    private Log activeLog = null;
    private int activeLogIndex = -1;

    // editor visibility
    private EditorStateManager editorStateManager;

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
        mMyLayout = (ConstraintLayout) mView;

        // obtain a list of categories
        mCats = DbHelper.getInstance(mView.getContext()).getCategories(CatData.CATEGORY_STATUS_INACTIVE);

        setUpUIElements();

        List<Log> data = mDataProvider.getLogs();
        Collections.reverse(data);
        mLogsAdapter = new LogsAdapter(data);

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mLogsAdapter);

        mLogsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("close", null);
        builder.setView(mView);
        builder.setNeutralButton("save changes", (DialogInterface.OnClickListener) this);

        return builder.create();
    }

    private void setUpUIElements() {
        mCatSpinner = mView.findViewById(R.id.cat_spinner);
        mStartPicker = mView.findViewById(R.id.start_picker);
        mEndPicker = mView.findViewById(R.id.end_picker);
        mClearButton = mView.findViewById(R.id.clear_button);
        mChangeButton = mView.findViewById(R.id.change_button);
        mAddDelButton = mView.findViewById(R.id.add_del_button);
        mEditorExpanderButton = mView.findViewById(R.id.expand_editor_image_button);
        mEditorLayout = mView.findViewById(R.id.editor_layout);
        mRecycler = mView.findViewById(R.id.recycler);

        editorStateManager = new EditorStateManager(getContext(), mEditorExpanderButton, mEditorLayout, mRecycler, mMyLayout);

        String[] spinnerOptions = new String[mCats.size() + 1];
        spinnerOptions[0] = "";
        for (int i = 0; i < mCats.size(); i++) {
            spinnerOptions[i + 1] = mCats.get(i).getTitle();
        }

        // set time on DatePimePickers to the same value:
        Calendar c = Calendar.getInstance();
        long timeNow = c.getTimeInMillis();
        mStartPicker.setValue(timeNow);
        mEndPicker.setValue(timeNow);

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
                if (activeLog == null) return;
                if (!isUserInputValid(true)) return;

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
                    if (!isUserInputValid(true)) return;
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

        mEditorExpanderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               editorStateManager.toggleEditorMode();
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
        else mAddDelButton.setText("delete");
    }

    /**
     * Saving changes:
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        // if nothing changed, just return:
        if (addedLogs.size() == 0 && changedLogs.size() == 0 && deletedLogs.size() == 0) return;

        DbHelper db = DbHelper.getInstance(getActivity());
        // get all cats, just in case there would be anything missing on the usual list
        List<CatData> allCats = db.getCategories(CatData.CATEGORY_STATUS_DELETED);
        CatNameToIDHelper titleToID = new CatNameToIDHelper(allCats);

        // deliver changes of each type in sequence:
        // additions:
        if (addedLogs.size() != 0) {
            for (Log log : addedLogs) {
                long catID = titleToID.getIDByTitle(log.getCatTitle());
                db.pushLog(catID, log.getStartTime(), log.getEndTime());
            }
        }
        // modifications:
        if (changedLogs.size() != 0) {
            for (Log log : changedLogs) {
                long catID = titleToID.getIDByTitle(log.getCatTitle());
                db.changeLog(log.getID(), catID, log.getStartTime(), log.getEndTime());
            }
        }
        // deletions:
        if (deletedLogs.size() != 0) {
            for (Log log : deletedLogs) {
                db.deleteLog(log.getID());
            }
        }

        mCallback.onNeedUserInterfaceUpdate();
    }

    private boolean isUserInputValid(boolean displayErrorMessages) {
        List<String> errors = new ArrayList<>();

        if (mCatSpinner.getSelectedItemPosition() == 0) {
            errors.add("Select a category.");
        }

        if (mStartPicker.getValue() >= mEndPicker.getValue()) {
            errors.add("Start time must be before end time.");
        }

        if (errors.size() == 0) return true;

        if (displayErrorMessages) {
            StringBuilder sb = new StringBuilder(errors.size());
            boolean first_entry = true;
            for (String s : errors) {
                // two newLine chars if it's not the first entry
                if (!first_entry) {
                    sb.append("\n\n");
                } else {
                    first_entry = false;
                }
                sb.append(s);
            }
            Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
