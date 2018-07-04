package com.example.sokol.monitor.Help;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.sokol.monitor.PopUpMessage;
import com.example.sokol.monitor.R;

public class MiniHelp extends FrameLayout implements View.OnTouchListener {
    private String mMessage;

    public MiniHelp(@NonNull Context context) {
        super(context);
        setup(context);
    }

    public MiniHelp(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MiniHelp,
                0, 0);
        try {
            String text = a.getString(R.styleable.MiniHelp_message);
            setMessage(text);
        } finally {
            a.recycle();
        }
        setup(context);
    }

    public void setMessage(String message){
        mMessage = message;
    }

    private void setup(Context context) {
        inflate(context, R.layout.help_mini, this);

        FrameLayout frameLayout = findViewById(R.id.help_mini_frame);
        frameLayout.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        PopUpMessage.pop((Activity) getContext(), mMessage);
        return false;
    }

}
