package com.example.sokol.monitor.EasyLogsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sokol.monitor.model.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.Help.HelpProvider;
import com.example.sokol.monitor.model.Log;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.List;

public class EasyUILogsFragment extends DialogFragment implements EasyLogsAdapter.OnItemSelectedListener, View.OnClickListener, EasyLogsEditor.OnInteractionEnded {
    private View mView;

    private List<CatData> mCats;
    private EasyLogsAdapter mLogsAdapter;
    private RecyclerView mRecycler;
    private FloatingActionButton mFab;
    private TextView mTitle;

    OnNeedUserInterfaceUpdate mUserInterfaceUpdater;

    // this helps recognize whether an onResume is the fragment being created or coming
    // back to foreground.
    boolean isFirstOnResume = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the required interfaces. If not, it throws an exception
        try {
            mUserInterfaceUpdater = (OnNeedUserInterfaceUpdate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_dialog, null);

        setupUIElements();

        DbHelper db = DbHelper.getInstance(getActivity());
        // obtain a list of categories
        mCats = db.getCategories(CatData.CATEGORY_STATUS_INACTIVE);

        List<Log> data = db.getLogsLaterThan(-1);
        mLogsAdapter = new EasyLogsAdapter(data);

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mLogsAdapter);

        mLogsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }

    private void setupUIElements(){
        mRecycler = mView.findViewById(R.id.easy_ui_recycler);
        mFab = mView.findViewById(R.id.easy_ui_fab);
        mTitle = mView.findViewById(R.id.easy_ui_title);

        mTitle.setText(R.string.easy_ui_logs_title);
        mFab.setOnClickListener(this);

        ImageView helpImage = mView.findViewById(R.id.easy_ui_help);
        helpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpProvider.requestHelp((AppCompatActivity)getActivity(), HelpProvider.TOPIC_LOGS);
            }
        });
    }

    @Override
    public void onItemSelected(int i, Log log) {
        displayEditor(log, i);
    }

    /**
     * fab (floating action button) click
     */
    @Override
    public void onClick(View view) {
        displayEditor(null, 0);
    }

    private void displayEditor(Log log, int i){
        lastLogIndexUserInteractedWith = i;
        EasyLogsEditor editor = new EasyLogsEditor();
        editor.setLog(log);
        editor.setCats(mCats);
        editor.setOnInteractionEndedListener(this);
        editor.show(getActivity().getSupportFragmentManager(), "LOGS editor");
    }

    public void includeLogCreatedElsewhere(long ID){
        // get the log
        Log log = Log.fetchLogWithId(getActivity(), ID);

        if(log == null) return;
        // add the log
        mLogsAdapter.addALog(log);
        mRecycler.scrollToPosition(0);
        lastLogIndexUserInteractedWith += 1;
    }

    private void includeLogsOnResume() {
        // fetch any logs later than the last known
        int knownLogsCount = mLogsAdapter.getItemCount();
        long lastKnownLogID =  -1;
        if(knownLogsCount> 0){
            lastKnownLogID = mLogsAdapter.getLogAt(0).getID();
        }
        DbHelper db = DbHelper.getInstance(getActivity());
        List<Log> newLogs = db.getLogsLaterThan(lastKnownLogID);

        if(newLogs.size() == 0) return;

        mLogsAdapter.addLogs(newLogs);
        mRecycler.scrollToPosition(0);

        lastLogIndexUserInteractedWith += newLogs.size();
    }

    private int lastLogIndexUserInteractedWith = -1;
    private int getLogIndex(long logID){
        if(logID == mLogsAdapter.getLogAt(lastLogIndexUserInteractedWith).getID()){
            return lastLogIndexUserInteractedWith;
        }
        for(int i = 0; i< mLogsAdapter.getItemCount(); i++){
            long ID = mLogsAdapter.getLogAt(i).getID();
            if(logID == ID){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();

        // on resume, unless it's just after being
        if (!isFirstOnResume) {
            includeLogsOnResume();
        }

        isFirstOnResume = false;
    }

    // callbacks from EasyLogsEditor
    @Override
    public void onLogCreated(Log log) {
        mLogsAdapter.addALog(log);
        mRecycler.scrollToPosition(0);
        mUserInterfaceUpdater.onNeedUserInterfaceUpdate();
    }

    @Override
    public void onLogDeleted(long logID) {
        mLogsAdapter.remove(getLogIndex(logID));
        mUserInterfaceUpdater.onNeedUserInterfaceUpdate();
    }

    @Override
    public void onLogChanged(long logID) {
        mLogsAdapter.change(getLogIndex(logID));
        mUserInterfaceUpdater.onNeedUserInterfaceUpdate();
    }
}