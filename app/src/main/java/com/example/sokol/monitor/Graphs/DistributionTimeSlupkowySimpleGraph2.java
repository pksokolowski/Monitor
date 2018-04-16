package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

public class DistributionTimeSlupkowySimpleGraph2 extends SimpleGraphView {
    // paint na słupki
    Paint ThickPaint = null;

    Paint tick_paint = null;
    Paint thick_tick_paint = null;

    public DistributionTimeSlupkowySimpleGraph2(Context context) {
        super(context);
        preparePaints();
    }

    public DistributionTimeSlupkowySimpleGraph2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints();
    }


    private void preparePaints() {
        // wykonuję jeszcze raz, pomimo że było wykonane w super, ale ok.
        int text_size_in_sp_units = 15; //5dp

        int text_size_in_pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                text_size_in_sp_units, getResources().getDisplayMetrics());

        // a to już jest specific dla tego subView
        tick_paint = new Paint();
        tick_paint.setStyle(Paint.Style.STROKE);
        tick_paint.setStrokeWidth(Math.max(1, text_size_in_pixels / 13));
        tick_paint.setColor(mMainColor);

        thick_tick_paint = new Paint();
        thick_tick_paint.setStyle(Paint.Style.STROKE);
        thick_tick_paint.setStrokeWidth(Math.max(1, text_size_in_pixels / 8));
        thick_tick_paint.setColor(mMainColor);

        ThickPaint = new Paint();
        ThickPaint.setStyle(Paint.Style.STROKE);
        ThickPaint.setColor(mMainColor);
    }

    @Override
    protected int SpecialCalculateY(int y_bottom) {
        float margin = mTextPaint.getTextSize();
        return y_bottom - (int) (margin / 2);
    }

    @Override
    protected void SpecialMeasurements(float width, float height) {
        // ustawiam Thickness słupków :
        float grubosc = 1f;
        if(mData.length < 50) grubosc = 0.95f;
        float grubość_słupka = (width / (float) mMax_Entries) * grubosc;
        ThickPaint.setStrokeWidth(Math.max(1, grubość_słupka));
    }

    public void setData(String title, long[] data, int ticksAmount, int thickTickPeriod) {
        ozdobyTicks = ticksAmount;
        ozdobyThicknessPeriod = thickTickPeriod;
        super.setData(title, data);
    }

    private float ozdobyTicks = 24;
    private float ozdobyThicknessPeriod = 6;

    @Override
    protected void SpecialOzdobyWykresu(float x, float y, float y_bottom, Canvas canvas) {
        // ozdoby-znaczniki czasu, godzinowe:
        float minX = 0;//getMinX((int)x);
        float maxX = x;//getMaxX((int)x);
        float realX = maxX - minX;

        float hourOffset = realX / ozdobyTicks;
        float normal_tick_bottom = y_bottom - ((y_bottom - y) / (float) 2);
        for (int i = 0; i <= ozdobyTicks; i++) {
            float tick_x = minX + i * hourOffset;
            if (i % ozdobyThicknessPeriod == 0)
                canvas.drawLine(tick_x, y, tick_x, y_bottom, thick_tick_paint);
            else
                canvas.drawLine(tick_x, y, tick_x, normal_tick_bottom, tick_paint);
        }
    }

    @Override
    protected void SpecialPlot(int y, int x, float x_per_entry, Canvas canvas, float minX) {
        // ponieważ plot linesy zaczynają się za wcześnie, zaczynam plot później, o pół linesa;
//              float StrokeWidth = ThickPaint.getStrokeWidth();
//        //float HalfTheStrokeWidth = StrokeWidth / 2f;
//        float hourTickWidth = (float) x / ozdobyTicks;
//        float halfTheStrokekWidth = StrokeWidth / 2f;

        float elems = x / (float) mData.length;
        float halfTheElem = elems/2f;

        float my_minX = halfTheElem; //getMinX(x);
        float maxX = x - halfTheElem;// getMaxX(x);
        float modified_x = maxX - my_minX;
        int lenToUseInXScaleCalc = mData.length - 1;
        if (mMax_Entries != 0) lenToUseInXScaleCalc = mMax_Entries - 1;
        float modified_x_per_entry = modified_x / (float) (lenToUseInXScaleCalc);


        // maluję:
        for (int i = 0, len = mData.length; i < len; i++) {

            float calculated_x = my_minX + modified_x_per_entry * i;
            float calculated_y = y - mData[i];

            canvas.drawLine(calculated_x, y, calculated_x, calculated_y, ThickPaint);
        }
    }
}