package com.example.sokol.monitor;

import java.util.ArrayList;
import java.util.List;

public class ErrorMessageConcatenator {
    private List<String> mErrors = new ArrayList<>();

    public void add(String errorMessage){
        mErrors.add(errorMessage);
    }

    public int size(){
        return mErrors.size();
    }

    public String getMultilineErrorsString(){
        StringBuilder sb = new StringBuilder(mErrors.size());
        boolean first_entry = true;
        for (String s : mErrors) {
            // two newLine chars if it's not the first entry
            if (!first_entry) {
                sb.append("\n\n");
            } else {
                first_entry = false;
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
