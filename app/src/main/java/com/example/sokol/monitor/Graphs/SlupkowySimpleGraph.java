package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class SlupkowySimpleGraph extends SimpleGraphView {
    Paint ThickPaint = null;

    public SlupkowySimpleGraph(Context context) {
        super(context);
        preparePaints();
    }

    public SlupkowySimpleGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints();
    }

    private void preparePaints() {
        ThickPaint = new Paint();
        ThickPaint.setStyle(Paint.Style.STROKE);
        ThickPaint.setColor(mMainColor);
    }

    @Override
    protected void SpecialMeasurements(float width, float height) {
        // ustawiam Thickness słupków :
        float grubość_słupka = (width / (float) mMax_Entries) * 0.9f;
        ThickPaint.setStrokeWidth(Math.max(1, grubość_słupka));
    }

    @Override
    protected void SpecialPlot(int y, int x, float x_per_entry, Canvas canvas, float minX) {
        // ponieważ plot linesy zaczynają się za wcześnie, zaczynam plot później, o pół linesa;
        float HalfTheStrokeWidth = ThickPaint.getStrokeWidth() / 2f;
        float my_minX = HalfTheStrokeWidth;
        float maxX = x - HalfTheStrokeWidth;
        float modified_x = maxX - my_minX;
        int lenToUseInXScaleCalc = mData.length - 1;
        if (mMax_Entries != 0) lenToUseInXScaleCalc = mMax_Entries - 1;
        float modified_x_per_entry = (float) modified_x / (float) (lenToUseInXScaleCalc);

        // maluję:
        for (int i = 0, len = mData.length; i < len; i++) {

            float calculated_x = my_minX + modified_x_per_entry * i;
            float calculated_y = y - mData[i];

            canvas.drawLine(calculated_x, y, calculated_x, calculated_y, ThickPaint);
        }
    }

}