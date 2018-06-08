package com.example.sokol.monitor.DateTimePicker;


import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sokol.monitor.R;

import java.util.Calendar;

public class DateRangePicker extends LinearLayout
        implements DatePickerDialog.OnDateSetListener {

    Calendar startValues;
    Calendar endValues;
    Calendar currentlyModified;

    TextView mStartDate;
    TextView mEndDate;

    public DateRangePicker(Context context) {
        super(context);
        setup(context);
    }

    public DateRangePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.daterange_picker, this);
        mStartDate = findViewById(R.id.start_date);
        mEndDate = findViewById(R.id.end_date);

        setListeners(context, this);

        startValues = Calendar.getInstance();
        endValues = Calendar.getInstance();
        // zeruję unused fields, so it's always the beginning of a given day 00:00 time
        zeroTheHoursAndMinutes(startValues);
        zeroTheHoursAndMinutes(endValues);

        updateDisplayedDates();
    }

    private void setListeners(final Context context, final DateRangePicker listener ) {
        mStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentlyModified = startValues;
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateChangedListener(listener);
                newFragment.setInitialDate(currentlyModified);
                newFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "datePicker");
            }
        });
        mEndDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentlyModified = endValues;
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateChangedListener(listener);
                newFragment.setInitialDate(currentlyModified);
                newFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "datePicker2");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        currentlyModified.set(Calendar.YEAR, i);
        currentlyModified.set(Calendar.MONTH, i1);
        currentlyModified.set(Calendar.DAY_OF_MONTH, i2);
        updateDisplayedDates();
    }

    private void updateDisplayedDates(){
        mStartDate.setText(HumanReadabilityHelper.getDateStampString(startValues));
        mEndDate.setText(HumanReadabilityHelper.getDateStampString(endValues));
    }

    private void zeroTheHoursAndMinutes(Calendar c){
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
    }

    public long getStartValue() {
        return startValues.getTimeInMillis();
    }

    public long getEndValue() {
        return endValues.getTimeInMillis();
    }
}
