package com.example.sokol.monitor.DateTimePicker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.sokol.monitor.R;

import java.util.Calendar;

public class DateTimePicker extends LinearLayout
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    Calendar values;

    TextView mDate;
    TextView mTime;

    public DateTimePicker(Context context) {
        super(context);
        setup(context);
    }

    public DateTimePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.datetime_picker, this);
        mDate = findViewById(R.id.date);
        mTime = findViewById(R.id.time);

        setListeners(context, this);

        values = Calendar.getInstance();

        updateDisplayedDate();
        updateDisplayedTime();
    }

    private void setListeners(final Context context, final DateTimePicker listener ) {
        mDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateChangedListener(listener);
                newFragment.setInitialDate(getCurrentDateTimeAsCalendar());
                newFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "datePicker");
            }
        });
        mTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setTimeChangedListener(listener);
                newFragment.setInitialTime(getCurrentDateTimeAsCalendar());
                newFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "timePicker");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        values.set(Calendar.YEAR, i);
        values.set(Calendar.MONTH, i1);
        values.set(Calendar.DAY_OF_MONTH, i2);

        updateDisplayedDate();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        values.set(Calendar.HOUR_OF_DAY, i);
        values.set(Calendar.MINUTE, i1);

       updateDisplayedTime();
    }

    private void updateDisplayedDate(){
        mDate.setText(HumanReadabilityHelper.getDateStampString(values));
    }

    private void updateDisplayedTime(){
        mTime.setText(HumanReadabilityHelper.getTimeStampString(values));
    }

    private Calendar getCurrentDateTimeAsCalendar(){
        return values;
    }

    public long getValue() {
        Calendar c = getCurrentDateTimeAsCalendar();
        return c.getTimeInMillis();
    }
}
