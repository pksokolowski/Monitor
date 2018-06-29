package com.example.sokol.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.example.sokol.monitor.DateTimePicker.DateRangePicker;
import com.example.sokol.monitor.EasyCatsDialog.EasyUICatsFragment;
import com.example.sokol.monitor.EasyLogsDialog.EasyUILogsFragment;
import com.example.sokol.monitor.Graphs.DistributionTimeBarSimpleGraph;
import com.example.sokol.monitor.Graphs.PieChart;
import com.example.sokol.monitor.Graphs.BarSimpleGraphView;
import com.example.sokol.monitor.Graphs.TextualData;
import com.example.sokol.monitor.Help.HelpProvider;
import com.example.sokol.monitor.LogsDialog.Log;
import com.example.sokol.monitor.LogsDialog.LogsDialogFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnNeedUserInterfaceUpdate, LogsProvider {

    // time ranges for data retrieved from the database:
    public static final int RANGE_UNKNOWN = -1;
    public static final int RANGE_CUSTOM = 0;
    public static final int RANGE_ALL_TIME = 1;
    public static final int RANGE_3_MONTHS = 2;
    public static final int RANGE_MONTH = 3;
    public static final int RANGE_WEEK = 4;
    public static final int RANGE_DAY = 5;
    public static final int RANGE_TODAY_ONLY = 6;


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

        // first create a notification channel, for android O and later
        // no worries, it only makes changes the first time a given channel's creation
        // is requested.
        NotificationProvider.createNotificationChannels(this);

        // important: recognize first startup time; also saves now as the first run time if it is.
        long firstRunTime = FirstRunDateTimeManager.getFirstStartupTime(this);

       ThemeChanger.changeTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        int command = handleIntent(intent);

        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new SettingsFragment())
                .commit();

        // set insets on the bottom
        final ConstraintLayout mainConstraintLayout = findViewById(R.id.main_constraint_layout);
        mainConstraintLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                mainConstraintLayout.setPadding(0,0,0,
                        windowInsets.getSystemWindowInsetBottom());
                return windowInsets.consumeSystemWindowInsets();
            }
        });

        // establish whether to use sound with notification
        // also show help for new user
        boolean notificationWithSound = false;
        if(TimeHelper.isToday(firstRunTime)){
            notificationWithSound = true;
            // if the startup is not from the notification tap; hence (command == COMMAND_DO_NOTHING)
            // and the user never started any work tracking in the app before...
            if(command == COMMAND_DO_NOTHING && WorkInProgressManager.getCounter(this) == 0){
                HelpProvider.requestHelp(this, HelpProvider.TOPIC_GETTING_STARTED);
            }
        }

        NotificationProvider.showNotificationIfEnabled(this, notificationWithSound);

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

        final PieChart pie = findViewById(R.id.pie);
        pie.setOnSliceSelectedListener(new PieChart.OnSliceSelected() {
            @Override
            public void onSliceSelected(PieChart.Datum datumOrNull) {
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
        final DateRangePicker rangePicker = findViewById(R.id.range_picker);

        // set default range for data shown
        if (TimeHelper.isToday(firstRunTime)) {
            spinner.setSelection(RANGE_TODAY_ONLY);
        } else {
            spinner.setSelection(RANGE_3_MONTHS);
        }
        setRangePickerToMatchSpinner(rangePicker);

        refreshAllGraphs();

        rangePicker.setOnRangeChangedListener(new DateRangePicker.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(long start, long end) {
                refreshAllGraphs();
                spinner.setSelection(RANGE_CUSTOM);
                mPieChartsLastTouchedID = -1;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // if spinner is set tu "custom", do nothing
                if(i==RANGE_CUSTOM) return;
                if (SpinnerUses++ >0) {
                    // set value in the range picker
                    setRangePickerToMatchSpinner(rangePicker);
                    refreshAllGraphs();
                }
                // bugfix: so after changing data time range it will still recognize change
                mPieChartsLastTouchedID = -1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        scrollToXY(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private int handleIntent(Intent intent) {
        int command = intent.getIntExtra(MainActivity.EXTRA_COMMAND_TO_EXECUTE_UPON_START, COMMAND_DO_NOTHING);
        switch (command){
            case COMMAND_DISPLAY_CATS_DIALOG:
                showCatsDialog();
                break;
        }
        return command;
    }

    private void setRangePickerToMatchSpinner(DateRangePicker rangePicker) {
        rangePicker.setRange(getSpinnersLowerTimeBound(), getSpinnersUpperTimeBound());
    }

    private void showCatsDialog() {
//        SingleInstanceDialog catsDialog = new CatsDialogFragment();
//        catsDialog.showIfNotVisibleAlready(getFragmentManager());

        Fragment prev = getSupportFragmentManager().findFragmentByTag("CATS");
        if(prev != null) return;
        EasyUICatsFragment cats = new EasyUICatsFragment();
        cats.show(getSupportFragmentManager(), "CATS");
    }

    private void showLogsDialog() {
        EasyUILogsFragment logsDialog = new EasyUILogsFragment();
        logsDialog.show(getSupportFragmentManager(), "LOGS");
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
        if(pieData.size() ==0) pie.setNoDataMessage(getString(R.string.main_message_no_data_to_show));
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
                isTodayInRangeSelected() && WorkInProgressManager.getCounter(this) > lastSavedEntryNumber);
    }

    private boolean isTodayInRangeSelected(){
        long upperBound = getUpperTimeBoundForData();
        long zeroHourToday =  TimeHelper.get0HourNdaysAgo(0);
        return zeroHourToday < upperBound;
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

    private long getSpinnersLowerTimeBound(){
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

    private long getSpinnersUpperTimeBound(){
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

    private long getLowerTimeBoundForData() {
        DateRangePicker rangePicker = findViewById(R.id.range_picker);
        return rangePicker.getStartValue();
    }

    private long getUpperTimeBoundForData() {
        DateRangePicker rangePicker = findViewById(R.id.range_picker);
        return rangePicker.getEndValue();
    }

    private int getRangeSelection() {
        Spinner spinner = findViewById(R.id.spinner);
        int item_selected = spinner.getSelectedItemPosition();
        if (item_selected == -1) return RANGE_UNKNOWN;

        return item_selected;
    }

    private void updateSelectedInfo(LogsData data) {
        PeriodicDistro perio = new PeriodicDistro(data, getUpperTimeBoundForData()>=TimeHelper.now());

        BarSimpleGraphView graphSlup = findViewById(R.id.slupkowySimpleGraph);
        graphSlup.setData(getString(R.string.graph_title_recent_days), perio.mDaily);

        DistributionTimeBarSimpleGraph distrGraph = findViewById(R.id.distributionSimpleGraph);
        distrGraph.setData(getString(R.string.graph_title_24h_distribution), perio.mHourly, 24, 6);

        DistributionTimeBarSimpleGraph distrGraphWeekly = findViewById(R.id.distributionSimpleGraphWeekly);
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
    public List<Log> getLogs() {
        return Log.getLogsList(this, getLowerTimeBoundForData(), getUpperTimeBoundForData());
    }
}
