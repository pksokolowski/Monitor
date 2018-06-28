package com.example.sokol.monitor.EasyLogsDialog;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.R;

import java.util.List;

public class SpinnerCatsHelper {
    public static ArrayAdapter<String> setupSpinner(List<CatData> cats, View view, Spinner spinner){
        ArrayAdapter<String> catsAdapter;
        String[] spinnerOptions = new String[cats.size() + 1];
        spinnerOptions[0] = view.getContext().getString(R.string.logs_dialog_spinner_default_text);
        for (int i = 0; i < cats.size(); i++) {
            spinnerOptions[i + 1] = cats.get(i).getTitle();
        }
        catsAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        catsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(catsAdapter);

        return catsAdapter;
    }

    public static CatData getCatForCurrentSpinnerSelection(Spinner spinner, List<CatData> cats) {
        int selectedCatIndex = spinner.getSelectedItemPosition() -1;
        if(selectedCatIndex == -1) return null;
        return cats.get(selectedCatIndex);
    }

    public static int getSpinnerIndexForCatTitle(ArrayAdapter<String> adapter,  String title) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (title.equals(adapter.getItem(i))) {
                return i;
            }
        }
        return -1;
    }
}
