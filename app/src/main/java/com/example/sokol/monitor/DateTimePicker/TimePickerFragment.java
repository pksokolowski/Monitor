package com.example.sokol.monitor.DateTimePicker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {
    TimePickerDialog.OnTimeSetListener listener;
    Calendar initialTime;

    public void setTimeChangedListener(TimePickerDialog.OnTimeSetListener listener) {
        this.listener = listener;
    }

    public void setInitialTime(Calendar c){
        initialTime = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        if(initialTime==null) initialTime = Calendar.getInstance();
        int hour = initialTime.get(Calendar.HOUR_OF_DAY);
        int minute = initialTime.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

}
