package com.example.sokol.monitor.EasyLogsDialog;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.R;

import java.util.List;

public class SpinnerCatsHelper {
    public static ArrayAdapter<String> setupSpinner(List<CatData> cats, View view, int spinner){
        ArrayAdapter<String> catsAdapter;
        String[] spinnerOptions = new String[cats.size() + 1];
        spinnerOptions[0] = view.getContext().getString(R.string.logs_dialog_spinner_default_text);
        for (int i = 0; i < cats.size(); i++) {
            spinnerOptions[i + 1] = cats.get(i).getTitle();
        }
        catsAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        catsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner catsSpinner = view.findViewById(spinner);
        catsSpinner.setAdapter(catsAdapter);

        return catsAdapter;
    }
}
