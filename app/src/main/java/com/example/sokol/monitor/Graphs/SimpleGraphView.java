package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.sokol.monitor.R;


/**
 * Draws graphs given a SurfaceView to draw on.
 * Created by Sokol on 08.04.2017.
 */


public class SimpleGraphView extends View
{
    Paint paint = null;
    Paint mTextPaint = null;
    String mTitle;
    Path path;
    long[] mDataLong;
    float[] mData;
    int mMax_Entries;

    // color choice
    int mMainColor;

    public SimpleGraphView(Context context) {
        super(context);
        preparePaints(context);
    }

    public SimpleGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints(context);
    }

    private void preparePaints(Context context){

        // set mMainColor, so it can be used both here and in the children
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.chartsMainColor});
        mMainColor = ta.getInt(0, Color.LTGRAY);

        int text_size_in_sp_units = 15; //5dp

        int text_size_in_pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                text_size_in_sp_units, getResources().getDisplayMetrics());

        mTextPaint = new Paint();
        mTextPaint.setColor(mMainColor);
        mTextPaint.setTextSize(text_size_in_pixels);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1, text_size_in_pixels/9));
        paint.setColor(mMainColor);

        path = new Path();
        
        ta.recycle();
    }

    public void setData(String title, long[] data){
        mTitle = title;
        mMax_Entries = data.length;
        mDataLong = data;
        mData = new float[mDataLong.length];
        convertLongValsToFloats(getHeight());

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        SpecialMeasurements(widthSize, heightSize);

        //MUST CALL THIS
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        convertLongValsToFloats(h);
    }

    private void convertLongValsToFloats(int height){
        if(mDataLong ==null) return;

        // data preparation
        long max_value = -1;
        for (long v : mDataLong)
        {
            if (v > max_value) max_value = v;
        }

        // safety precaution for the case where all values are smaller than one.
        if(max_value < 1) max_value = 1;

        int max_y_for_plotting = SpecialCalculateY(height);

        float skala_Y = (max_y_for_plotting-(mTextPaint.getTextSize()*(float)1.2)) / (float) max_value;

        // scale the data
        for (int i = 0; i < mData.length; i++)
        {
            mData[i] = (float) Math.max(0, mDataLong[i]) * skala_Y;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(mDataLong ==null)
        {
            // no data image:
            canvas.drawColor(Color.LTGRAY);
            return;}

        int x = getWidth();
        int y_bottom = getHeight();
        int y = SpecialCalculateY(y_bottom);

        // put title somewhere:
        float margin = mTextPaint.getTextSize();
        canvas.drawText(mTitle, margin, margin, mTextPaint);

        path.reset();

        // embellishments
        path.moveTo(0, y);
        path.lineTo(x, y);
        SpecialOzdobyWykresu(x, y, y_bottom, canvas);

        // when there is not enough data to display a meaningful chart, skip drawing.
        if (mData.length <= 1)
        {
            canvas.drawPath(path, paint);
            return;
        }

        // From now on, only drawing the data's representation
        // ---------------------------------------------------

        int lenToUseInXScaleCalc = mData.length-1;
        if(mMax_Entries != 0) lenToUseInXScaleCalc = mMax_Entries-1;
        float x_per_entry = (float) x / (float) (lenToUseInXScaleCalc);

        SpecialPlot(y, x, x_per_entry, canvas, 0);

        canvas.drawPath(path, paint);
    }

    /**
     * Calculates the height of the drawable area for the plotting
     * Below that y, returned by this method, are ozdoby lub nothing
     * @param y_bottom
     * @return maxY for drawable area for the plot
     */
    protected int SpecialCalculateY(int y_bottom)
    {
        return y_bottom;
    }

    protected void SpecialPlot(int y, int x, float x_per_entry, Canvas canvas, float minX)
    {
        // lastVal helps determine if drawing can be skipped. Useful when zero value
        // is reappearing for a lot of points, as zero value translates to a zero height
        // bar, so it doesn't have to be drawn at all.
        float lastVal = 1;

        path.moveTo(minX, y - mData[0]);
        for (int i = 1, len = mData.length; i < len; i++)
        {
            float calculated_x = minX + x_per_entry * i;
            float calculated_y = y - mData[i];
            if (lastVal == 0 && mData[i] == 0)
            {
                path.moveTo(calculated_x, calculated_y);
                continue;
            }
            path.lineTo(calculated_x, calculated_y);
            lastVal = mData[i];
        }
    }

    protected void SpecialMeasurements(float width, float height){}

    protected void SpecialOzdobyWykresu(float x, float y, float y_bottom, Canvas canvas){}
}
