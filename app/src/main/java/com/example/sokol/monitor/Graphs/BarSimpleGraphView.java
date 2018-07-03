package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.sokol.monitor.TimeHelper;

public class BarSimpleGraphView extends SimpleGraphView {
    Paint ThickPaint = null;
    Paint rulerPaint = null;

    public BarSimpleGraphView(Context context) {
        super(context);
        preparePaints();
    }

    public BarSimpleGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints();
    }

    private void preparePaints() {
        ThickPaint = new Paint();
        ThickPaint.setStyle(Paint.Style.STROKE);
        ThickPaint.setColor(mMainColor);

        rulerPaint = new Paint();
        rulerPaint.setColor(Color.RED);
        rulerPaint.setStyle(Paint.Style.STROKE);
        rulerPaint.setStrokeWidth(Math.max(1, paint.getStrokeWidth()/2f));
    }

    @Override
    protected void SpecialMeasurements(float width, float height) {
        // setting bar thickness:
        float barThickness = (width / (float) mMax_Entries) * 0.9f;
        ThickPaint.setStrokeWidth(Math.max(1, barThickness));
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
        float modified_x_per_entry = modified_x / (float) (lenToUseInXScaleCalc);

        float maxVal = -1;
        int max_index = -1;
        // drawing
        for (int i = 0, len = mData.length; i < len; i++) {
            if(mData[i] > maxVal) {maxVal = mData[i]; max_index = i;}
            if(mData[i] == 0) continue;

            float calculated_x = my_minX + modified_x_per_entry * i;
            float calculated_y = y - mData[i];

            canvas.drawLine(calculated_x, y, calculated_x, calculated_y, ThickPaint);
        }

        // ruler
        if(touchYRuler > y - maxVal){
            // calculate value at this level and display in HH:MM format
            float percentageOfMax = ( y - touchYRuler) / maxVal;
            long correspondingTime = (long)(percentageOfMax * (float)mDataLong[max_index]);

            canvas.drawLine(0, touchYRuler, x, touchYRuler, rulerPaint);
            canvas.drawText(TimeHelper.getDuration(correspondingTime), maxX * 0.75f, mTextPaint.getTextSize(), mTextPaint);
        }
    }

    int touchYRuler = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        // you may need the x/y location
        int x = (int) event.getX();
        int y = (int) event.getY();

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_UP:
                touchYRuler = y;
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }

        return true;
    }
}