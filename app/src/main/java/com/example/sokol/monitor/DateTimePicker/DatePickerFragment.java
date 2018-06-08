package com.example.sokol.monitor.DateTimePicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {
    DatePickerDialog.OnDateSetListener listener;
    Calendar initialDate;

    public void setDateChangedListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    public void setInitialDate(Calendar c){
        initialDate = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        if(initialDate==null) initialDate = Calendar.getInstance();
        int year = initialDate.get(Calendar.YEAR);
        int month = initialDate.get(Calendar.MONTH);
        int day = initialDate.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), listener, year, month, day);
    }


}
