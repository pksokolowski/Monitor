package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.sokol.monitor.R;
import com.example.sokol.monitor.TimeHelper;

import java.util.List;

public class TextualData extends ConstraintLayout {
    public TextualData(Context context) {
        super(context);

    }

    public TextualData(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.textual_data, this);
    }

    public void setData(long[] daily_data) {
        // TODO: 31.03.2018 work on the data and display it the way you see fit
        long total_time = 0;
        long peak = 0;
        for(int i =0; i< daily_data.length;i++){
            long daily_datum = daily_data[i];
            total_time+= daily_datum;
            if(peak < daily_datum) peak = daily_datum;
        }
        long average = (long)((double)total_time/ (double)daily_data.length);
        long sum_of_diff_squares = 0;
        for(int i = 0; i< daily_data.length;i++){
            long diff = daily_data[i] - average;
            sum_of_diff_squares += Math.pow(diff,2);
        }
        long stdDeviation = (long)Math.sqrt(sum_of_diff_squares/daily_data.length);

        TextView tv_total = findViewById(R.id.total_time_value);
        TextView tv_peak = findViewById(R.id.on_peak_day_value);
        TextView tv_stdDeviation = findViewById(R.id.std_deviation_value);
        TextView tv_average = findViewById(R.id.av_value);


        tv_total.setText(TimeHelper.getDurationIntelligently(total_time));
        tv_peak.setText(daily_data.length > 1 ? TimeHelper.getDuration(peak) : " - ");
        tv_stdDeviation.setText(daily_data.length > 1 ? TimeHelper.getDuration(stdDeviation) : " - ");
        tv_average.setText(daily_data.length > 1 ? TimeHelper.getDuration(average) : " - ");

//        StringBuilder sb = new StringBuilder();
//        for(int i =0; i<daily_data.length;i++){
//            sb.append(TimeHelper.getHoursCount(daily_data[i]));
//            sb.append(" ; ");
//        }
//        tv_peak.setText(sb.toString());
    }
}
