package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

public class DistributionTimeBarSimpleGraph extends SimpleGraphView {
    Paint ThickPaint = null;

    Paint tick_paint = null;
    Paint thick_tick_paint = null;

    public DistributionTimeBarSimpleGraph(Context context) {
        super(context);
        preparePaints();
    }

    public DistributionTimeBarSimpleGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints();
    }


    private void preparePaints() {
        int text_size_in_sp_units = 15;

        int text_size_in_pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                text_size_in_sp_units, getResources().getDisplayMetrics());

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
        float spacePerBar = 1f;
        if(mData.length < 50) spacePerBar = 0.95f;
        float barThickness = (width / (float) mMax_Entries) * spacePerBar;
        ThickPaint.setStrokeWidth(Math.max(1, barThickness));
    }

    public void setData(String title, long[] data, int ticksAmount, int thickTickPeriod) {
        decorationTicks = ticksAmount;
        decorationThicknessAccentPeriod = thickTickPeriod;
        super.setData(title, data);
    }

    private float decorationTicks = 24;
    private float decorationThicknessAccentPeriod = 6;

    @Override
    protected void SpecialOzdobyWykresu(float x, float y, float y_bottom, Canvas canvas) {
        float minX = 0;
        float maxX = x;
        float realX = maxX - minX;

        float hourOffset = realX / decorationTicks;
        float normal_tick_bottom = y_bottom - ((y_bottom - y) / (float) 2);
        for (int i = 0; i <= decorationTicks; i++) {
            float tick_x = minX + i * hourOffset;
            if (i % decorationThicknessAccentPeriod == 0)
                canvas.drawLine(tick_x, y, tick_x, y_bottom, thick_tick_paint);
            else
                canvas.drawLine(tick_x, y, tick_x, normal_tick_bottom, tick_paint);
        }
    }

    @Override
    protected void SpecialPlot(int y, int x, float x_per_entry, Canvas canvas, float minX) {
        float elems = x / (float) mData.length;
        float halfTheElem = elems/2f;

        float my_minX = halfTheElem;
        float maxX = x - halfTheElem;
        float modified_x = maxX - my_minX;
        int lenToUseInXScaleCalc = mData.length - 1;
        if (mMax_Entries != 0) lenToUseInXScaleCalc = mMax_Entries - 1;
        float modified_x_per_entry = modified_x / (float) (lenToUseInXScaleCalc);

        // drawing:
        for (int i = 0, len = mData.length; i < len; i++) {

            float calculated_x = my_minX + modified_x_per_entry * i;
            float calculated_y = y - mData[i];

            canvas.drawLine(calculated_x, y, calculated_x, calculated_y, ThickPaint);
        }
    }
}