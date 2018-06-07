package com.example.sokol.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.example.sokol.monitor.Graphs.DistributionTimeSlupkowySimpleGraph2;
import com.example.sokol.monitor.Graphs.PieChart;
import com.example.sokol.monitor.Graphs.SlupkowySimpleGraph;
import com.example.sokol.monitor.Graphs.TextualData;
import com.example.sokol.monitor.LogsDialog.LogsDialogFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnNeedUserInterfaceUpdate, LogsDataProvider {

    // time ranges for data retrieved from the database:
    public static final int RANGE_UNKNOWN = -1;
    public static final int RANGE_ALL_TIME = 0;
    public static final int RANGE_3_MONTHS = 1;
    public static final int RANGE_MONTH = 2;
    public static final int RANGE_WEEK = 3;
    public static final int RANGE_DAY = 4;
    public static final int RANGE_TODAY_ONLY = 5;

    static final String EXTRA_COMMAND_TO_EXECUTE_UPON_START = "com.example.sokolrandomstringppp.Monitor.MainActivity.command_to_execute_upon_start";
    public static final int COMMAND_DO_NOTHING = 0;
    public static final int COMMAND_DISPLAY_CATS_DIALOG = 1;

    private LogsSelector mSelector;

    private long mPieChartsLastTouchedID = -1;
    private long lastUpdateDay0Hour = 0;
    private long lastSavedEntryNumber = 0;

    // fixes
    int SpinnerUses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set default preference values once. Must be called at every entry-point of the application
        // does nothing if preferences have been set already.
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

       ThemeChanger.changeTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        int command = intent.getIntExtra(MainActivity.EXTRA_COMMAND_TO_EXECUTE_UPON_START, COMMAND_DO_NOTHING);
        switch (command){
            case COMMAND_DISPLAY_CATS_DIALOG:
                showCatsDialog();
                break;
        }


        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new SettingsFragment())
                .commit();

//        final FrameLayout frame = (FrameLayout) findViewById(R.id.settings_frame);
//        frame.post(new Runnable() {
//            @Override
//            public void run() {
//                View frag_view = getFragmentManager().findFragmentById(R.id.settings_frame).getView();
//                if(frag_view== null) return;
//                int height = frag_view.getHeight();
//                frame.setMinimumHeight(height);
//            }
//        });


        NotificationProvider.showNotificationIfEnabled(this, false);

        Button saveLoadButton = findViewById(R.id.cats_button);
        saveLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCatsDialog();
            }
        });
        findViewById(R.id.logs_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogsDialog();
            }
        });


        // this will be used to fetch logs from db
        // a layer of abstraction, just to keep things tidy here.
        mSelector = new LogsSelector(this);

        refreshAllGraphs();
        // data for all cats

        // create the pieChart here and populate it with data.
        final PieChart pie = findViewById(R.id.pie);
        pie.setOnSliceSelectedListener(new PieChart.OnSliceSelected() {
            @Override
            public void OnSliceSelected(PieChart.Datum datumOrNull) {
                if (datumOrNull == null) {
                    updateSelectedInfo(mSelector.getLogsForAllNonDeletedCats(MainActivity.this, getLowerTimeBoundForData(), getUpperTimeBoundForData()));
                    mPieChartsLastTouchedID = -1;
                } else if (mPieChartsLastTouchedID != datumOrNull.ID) {
                    // zmiana
                    updateSelectedInfo(mSelector.fetchDataFromDb(getLowerTimeBoundForData(), getUpperTimeBoundForData(), MainActivity.this, datumOrNull.ID));
                    mPieChartsLastTouchedID = datumOrNull.ID;
                }
            }
        });

        final Spinner spinner = findViewById(R.id.spinner);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (SpinnerUses++ >0) refreshAllGraphs();
                // bugfix: so after changing data time range it will still recognize change
                mPieChartsLastTouchedID = -1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        scrollToXY(0, 0);
    }

    private void showCatsDialog() {
        CatsDialogFragment eventLoadSave = new CatsDialogFragment();
        eventLoadSave.show(getFragmentManager(), "event loadsave");
    }

    private void showLogsDialog() {
        LogsDialogFragment logsViewer = new LogsDialogFragment();
        logsViewer.show(getFragmentManager(), "logs viewer");
    }

    public static void startMe(Context context, int command){
        Intent intent = new Intent(context, MainActivity.class);
        if(command != COMMAND_DO_NOTHING)
            intent.putExtra(EXTRA_COMMAND_TO_EXECUTE_UPON_START, command);
        context.startActivity(intent);
    }

    private void refreshAllGraphs() {
        LogsData all_cats_data = mSelector.getLogsForAllNonDeletedCats(this, getLowerTimeBoundForData(), getUpperTimeBoundForData());
        PieChart pie = findViewById(R.id.pie);
        List<PieChart.Datum> pieData = mSelector.convertLogsToCatSums(all_cats_data);
        pie.setData(pieData);
        updateSelectedInfo(all_cats_data);

        lastUpdateDay0Hour = TimeHelper.now();
        lastSavedEntryNumber = WorkInProgressManager.getCounter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // jeżeli ostatni refresh był nie-dzisiaj : refresh.
        // jeżeli jest ustawione RANGE_TODAY_ONLY, i w międzyczasie zapisano jakiś work, to  też refresh.
        if (shouldUpdateDisplayedData()) {
            refreshAllGraphs();
        }

        registerReceiver(mMainActivityReceiver, new IntentFilter(ACTION_SAVED_NEW_DATA));
    }

    private boolean shouldUpdateDisplayedData() {
        return (TimeHelper.get0HourTimeOfAGivenDay(lastUpdateDay0Hour) != TimeHelper.get0HourNdaysAgo(0) ||
                getRangeSelection() == RANGE_TODAY_ONLY && WorkInProgressManager.getCounter(this) > lastSavedEntryNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMainActivityReceiver);
    }

    public static final String ACTION_SAVED_NEW_DATA = "com.example.sokolrandomstringppp.monitor.SAVED_NEW_DATA";

    BroadcastReceiver mMainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SAVED_NEW_DATA:
                    if (shouldUpdateDisplayedData()) {
                        refreshAllGraphs();
                    }
                    break;
            }
        }
    };

    private void scrollToXY(final int x, final int y) {
        // make sure the scrollView is scrolled all the way up
        final ScrollView scrollview = findViewById(R.id.main_scroll_view);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                // scrollview.fullScroll(ScrollView.FOCUS_UP);
                scrollview.scrollTo(x, y);
            }
        });
    }

    private long getLowerTimeBoundForData() {
        Spinner spinner = findViewById(R.id.spinner);
        int item_selected = spinner.getSelectedItemPosition();
        if (item_selected == -1) return 0;

        switch (item_selected) {
            case RANGE_ALL_TIME:
                return 0;
            case RANGE_3_MONTHS:
                return TimeHelper.get0HourNdaysAgo(90);
            case RANGE_MONTH:
                return TimeHelper.get0HourNdaysAgo(30);
            case RANGE_WEEK:
                return TimeHelper.get0HourNdaysAgo(7);
            case RANGE_DAY:
                return TimeHelper.get0HourNdaysAgo(1);
            case RANGE_TODAY_ONLY:
                return TimeHelper.get0HourNdaysAgo(0);
        }

        return 0;
    }

    private long getUpperTimeBoundForData() {
        Spinner spinner = findViewById(R.id.spinner);
        int item_selected = spinner.getSelectedItemPosition();
        if (item_selected == -1) return 0;

        // two possible options:
        long today = TimeHelper.get0HourNdaysAgo(0);
        long tomorrow = TimeHelper.get0HourNdaysAgo(-1);

        switch (item_selected) {
            case RANGE_ALL_TIME:
                return today;
            case RANGE_3_MONTHS:
                return today;
            case RANGE_MONTH:
                return today;
            case RANGE_WEEK:
                return today;
            case RANGE_DAY:
                return today;
            case RANGE_TODAY_ONLY:
                return tomorrow;
        }

        return 0;
    }

    private int getRangeSelection() {
        Spinner spinner = findViewById(R.id.spinner);
        int item_selected = spinner.getSelectedItemPosition();
        if (item_selected == -1) return RANGE_UNKNOWN;

        return item_selected;
    }

    private void updateSelectedInfo(LogsData data) {
        // calculate distributions in time:
//
//            long today0Hour = TimeHelper.get0HourNdaysAgo(0);
//            long sum = 0;
//            for (int i = 0; i < data.getStartTimes().length; i++) {
//                long start = data.getStartTimes()[i];
//                long end = data.getEndTimes()[i];
//
//                if (start > today0Hour - TimeHelper.DAY_LEN_IN_MILLIS)
//                    sum += end - start;
//            }
//            String result = TimeHelper.getDuration(sum);

        PeriodicDistro perio = new PeriodicDistro(data);

        //result+= " |perio: "+TimeHelper.getDuration(perio.mDaily[perio.mDaily.length-1]);
//        Toast.makeText(this, perio.oddling, Toast.LENGTH_SHORT).show();

        SlupkowySimpleGraph graphSlup = findViewById(R.id.slupkowySimpleGraph);
        graphSlup.setData(getString(R.string.graph_title_recent_days), perio.mDaily);

        DistributionTimeSlupkowySimpleGraph2 distrGraph = findViewById(R.id.distributionSimpleGraph);
        distrGraph.setData(getString(R.string.graph_title_24h_distribution), perio.mHourly, 24, 6);

        DistributionTimeSlupkowySimpleGraph2 distrGraphWeekly = findViewById(R.id.distributionSimpleGraphWeekly);
        distrGraphWeekly.setData(getString(R.string.graph_title_weekly_distribution), perio.mWeekly, 7, 1);

        TextualData textuals = findViewById(R.id.textuals);
        textuals.setData(perio.mDaily);
    }

    @Override
    public void onNeedUserInterfaceUpdate() {
        mSelector = new LogsSelector(MainActivity.this);
        refreshAllGraphs();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public LogsData getLogs() {
        return mSelector.getLogsForAllNonDeletedCats(this, getLowerTimeBoundForData(), getUpperTimeBoundForData());
    }
}
