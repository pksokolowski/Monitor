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
    private Paint mStroke;
    private Paint mFill;
    private Paint mText;
    private Paint mNoDataText;

    private List<Datum> mData;
    private float[] mPercentages;
    private float[] mAngles;

    private Random rnd;
    private RectF mRectF;
    private int[] mColors;

    private int mLastIndexTouched = -1;

    private String mNoDataMessage;

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
        mText.setTextAlign(Paint.Align.CENTER);

        mNoDataText = new Paint();
        mNoDataText.setColor(Color.BLACK);
        mNoDataText.setTextSize(text_size_in_pixels*1.5f);

        rnd = new Random();
        mRectF = new RectF(0, 0, getRight(), getBottom());
    }

    public void setNoDataMessage(String message){
        mNoDataMessage = message;
        // set background color randomly
        int ColorUpperBound = 200;
        int ColorLowerBound = 50;
        // text color shift compared to background
        int shift = 30;
        // random values for RGB channels withing bounds set above
        int R = ColorLowerBound + rnd.nextInt(ColorUpperBound - ColorLowerBound);
        int G = ColorLowerBound + rnd.nextInt(ColorUpperBound - ColorLowerBound);
        int B = ColorLowerBound + rnd.nextInt(ColorUpperBound - ColorLowerBound);
        mFill.setColor(Color.argb(255, R, G, B));
        mNoDataText.setColor(Color.argb(255, R+shift, G+shift, B+shift));
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
        if (mData == null || mData.size() == 0) { drawNoDataMessage(canvas); return; }

        float radius = getHeight() / 2f;
        float segStartAngle = STARTING_ANGLE;

        for (int i = 0; i < mAngles.length; i++) {
            Datum datum = mData.get(i);
            mFill.setColor(mColors[i]);
            if (i== mLastIndexTouched) {
                mFill.setColor(pressColor(mColors, i));
            }
            //mText.setColor(mColors[mAngles.length-i-1]);
            canvas.drawArc(mRectF, segStartAngle, mAngles[i], true, mFill);

            // drawing text on slice
            drawTextIfThereIsSpaceEnough(canvas, segStartAngle, segStartAngle + mAngles[i], radius, datum.title);

            segStartAngle += mAngles[i];
        }
    }

    private void drawNoDataMessage(Canvas canvas){
        if (mNoDataMessage == null) return;

        canvas.drawArc(mRectF, STARTING_ANGLE, 360, true, mFill);

        float angle = 360;
        float radius = getHeight() / 2f;
        float textAngle = STARTING_ANGLE + (angle / 2);
        canvas.drawText(mNoDataMessage,
                XProjectedAtAngle(textAngle, radius * 0.7f, radius),
                YProjectedAtAngle(textAngle, radius * 0.7f, radius),
                mNoDataText);
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

    private int pressColor(int[] colors, int i) {
        int color = colors[colors.length-1-i];
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

    private static float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private void drawTextIfThereIsSpaceEnough(Canvas canvas, float startAngle, float endAngle, float radius, String textToDraw) {
        float textRadius = radius * 0.7f;
        float halfFontHeight = ((mText.descent() + mText.ascent()) / 2);

        if (endAngle - startAngle < 90) {
            float distance = getDistance(
                    XProjectedAtAngle(startAngle, textRadius, radius), YProjectedAtAngle(startAngle, textRadius, radius) - halfFontHeight,
                    XProjectedAtAngle(endAngle, textRadius, radius), YProjectedAtAngle(endAngle, textRadius, radius) - halfFontHeight);
            if (distance <= mText.getTextSize()) return;
        }

        float textAngle = startAngle + ((endAngle-startAngle) / 2);
        canvas.drawText(textToDraw,
                XProjectedAtAngle(textAngle, textRadius, radius),
                YProjectedAtAngle(textAngle, textRadius, radius) - halfFontHeight,
                mText);
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
                    mListener.onSliceSelected(null);
                }
                else {
                    mLastIndexTouched = touchedIndex;
                    mListener.onSliceSelected(mData.get(touchedIndex));
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
        void onSliceSelected(@Nullable Datum datumOrNull);
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
