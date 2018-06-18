package com.example.sokol.monitor.Graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Random;

public class PieChart extends View {

    // paints
    Paint mStroke;
    Paint mFill;
    Paint mText;

    List<Datum> mData;
    float[] mPercentages;
    float[] mAngles;

    Random rnd;
    RectF mRectF;
    int[] mColors;

    private int mLastIndexTouched = -1;

    private static final float STARTING_ANGLE = 0;

    public PieChart(Context context) {
        super(context);
        preparePaints();
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preparePaints();
    }

    private void preparePaints() {
        int text_size_in_sp_units = 15; //5dp

        int text_size_in_pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                text_size_in_sp_units, getResources().getDisplayMetrics());

        mStroke = new Paint();
        mStroke.setStyle(Paint.Style.STROKE);
        mStroke.setColor(Color.GRAY);
        mStroke.setStrokeWidth(Math.max(1, text_size_in_pixels / 9));
        mStroke.setAntiAlias(true);

        mFill = new Paint();
        mFill.setStyle(Paint.Style.FILL);
        mFill.setAntiAlias(true);

        mText = new Paint();
        mText.setColor(Color.WHITE);
        mText.setTextSize(text_size_in_pixels);

        rnd = new Random();
        mRectF = new RectF(0, 0, getRight(), getBottom());
    }

    public void setData(List<Datum> data) {
        mData = data;
        if (data == null || data.size() == 0) {
            mAngles = null;
            mPercentages = null;
            invalidate();
            return;
        }
        int n = data.size();

        // remove 0 time entries:
        for(int i =n-1; i>=0; i--){
            if(mData.get(i).totalTime == 0) mData.remove(i); else break;
        }

        // jeżeli usunąłem wszystkie elementy, bo puste timesy
        if(mData.size()==0) {
            invalidate();
            return;
        }

        mPercentages = new float[n];
        mAngles = new float[n];
        // perform basic math:
        long sum = -1;
        for (int i = 0; i < n; i++) {
            sum += data.get(i).totalTime;
        }

        for (int i = 0; i < n; i++) {
            mPercentages[i] = (float) data.get(i).totalTime / sum;
            mAngles[i] = ((float) data.get(i).totalTime / sum) * 360f;
        }

        mColors = prepareColors(mData.size());
        mLastIndexTouched = -1;

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // Monitor's code - I just want a large square.
        int smallerOfTheTwo = Math.min(widthSize, heightSize);

        setMeasuredDimension(smallerOfTheTwo, smallerOfTheTwo);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.size() == 0) return;

        float radius = getHeight() / 2f;
        float segStartPoint = STARTING_ANGLE;

        for (int i = 0; i < mAngles.length; i++) {
            Datum datum = mData.get(i);
            mFill.setColor(mColors[i]);
            if (i== mLastIndexTouched) {
                mFill.setColor(pressColor(mColors[i]));
            }
            //mText.setColor(mColors[mAngles.length-i-1]);
            canvas.drawArc(mRectF, segStartPoint, mAngles[i], true, mFill);
            float textAngle = segStartPoint + (mAngles[i] / 2);
            canvas.drawText(datum.title,
                    XProjectedAtAngle(textAngle, radius * 0.7f, radius),
                    YProjectedAtAngle(textAngle, radius * 0.7f, radius),
                    mText);

            segStartPoint += mAngles[i];
        }
    }

    private int[] prepareColors(int len) {
        int[] colors = new int[len];
        int colorIncrement = 10;
        int ColorBound = Math.max(50, 246 - (mData.size() * colorIncrement));
        int R = rnd.nextInt(ColorBound);
        int G = rnd.nextInt(ColorBound);
        int B = rnd.nextInt(ColorBound);

        for (int i = 0; i < len; i++) {
            int color = Color.argb(255, R, G, B);
            colors[i] = color;
            R = Math.min(R + colorIncrement, 255);
            G = Math.min(G + colorIncrement, 255);
            B = Math.min(B + colorIncrement, 255);
        }

        return colors;
    }

    private int pressColor(int color) {
        int result = Color.argb(255,
                colorSwapper(Color.red(color)),
                colorSwapper(Color.green(color)),
                colorSwapper(Color.blue(color)));
        return result;
    }

    private int colorSwapper(int colorComponent) {
        return 255 - colorComponent;
    }

    private static float XProjectedAtAngle(float angle, float radius, float Xorigin) {
        return Xorigin + radius * (float) Math.cos(Math.toRadians(angle));
    }

    private static float YProjectedAtAngle(float angle, float radius, float Yorigin) {
        return Yorigin + radius * (float) Math.sin(Math.toRadians(angle));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        // you may need the x/y location
        int x = (int) event.getX();
        int y = (int) event.getY();

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_UP:
                int touchedIndex = getIndexForAGivenXY(x, y);
                if (touchedIndex == -1) break;
                if (touchedIndex == mLastIndexTouched)
                {
                    mLastIndexTouched = -1;
                    mListener.OnSliceSelected(null);
                }
                else {
                    mLastIndexTouched = touchedIndex;
                    mListener.OnSliceSelected(mData.get(touchedIndex));
                }
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }

        // tell the View to redraw the Canvas
        //invalidate();

        // tell the View that we handled the event
        return true;
    }

    private int getIndexForAGivenXY(int touch_x, int touch_y) {
        if(mAngles == null) return -1;
        float radius = getHeight() / 2f;
        // odrzuć klicki poza kołem (distance > radius)
        float distanceFromCenter = (float) Math.sqrt(Math.pow(touch_x - radius, 2) + Math.pow(touch_y - radius, 2));
        if (distanceFromCenter > radius) return -1;

        // atan2 - theta from polar coortinates (r, theta)
        float angle = (float) Math.toDegrees(Math.atan2(touch_y - radius, touch_x - radius));
        // make it work for negative angles too, because theta będzie możliwie mała i przyjmie wartość ujemną
        // jeżeli po ujemnej stronie będzie bliżej.
        angle = (angle + 360) % 360;

        float startAngle = STARTING_ANGLE;
        for (int i = 0; i < mAngles.length; i++) {
            if (angle > startAngle && angle < startAngle + mAngles[i]) {
                return i;
            }
            startAngle += mAngles[i];
        }

        return -1;
    }

    public List<Datum> getData() {
        return mData;
    }

    public interface OnSliceSelected {
        void OnSliceSelected(@Nullable Datum datumOrNull);
    }

    OnSliceSelected mListener;

    public void setOnSliceSelectedListener(OnSliceSelected listener) {
        mListener = listener;
    }

    public static class Datum {
        public String title;
        public long totalTime;
        public long ID;

        public Datum(String title, long totalTime, long ID) {
            this.title = title;
            this.totalTime = totalTime;
            this.ID = ID;
        }
    }
}
