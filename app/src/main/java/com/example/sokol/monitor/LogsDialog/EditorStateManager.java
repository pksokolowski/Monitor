package com.example.sokol.monitor.LogsDialog;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import com.example.sokol.monitor.R;

/**
 * manages the state of the editor_layout visibility, as well as manages the boolean indicating whether or not
 * the editor part if expanded or not. The main reason why this is separated into another class is to keep
 * the boolean safe from an incidental change. Instead of said boolean, the LogsDialogFragment will just hold
 * an instance of this class.
 *
 * It provides an animation too.
 */
class EditorStateManager implements Transition.TransitionListener {
    boolean isExpanded() {
        return isExpanded;
    }

    private boolean isExpanded = false;

    private Context context;
    private ImageButton mEditorExpanderButton;
    private ConstraintLayout mEditorLayout;
    private RecyclerView mRecycler;
    private ConstraintLayout mMyLayout;

    private boolean canRecyclerScroll = true;

    EditorStateManager(Context context, ImageButton mEditorExpanderButton, ConstraintLayout mEditorLayout, RecyclerView mRecycler, ConstraintLayout mMyLayout) {
        this.context = context;
        this.mEditorExpanderButton = mEditorExpanderButton;
        this.mEditorLayout = mEditorLayout;
        this.mRecycler = mRecycler;
        this.mMyLayout = mMyLayout;

        // block scrolling when canRecyclerScroll is set to false
        // this was introduced as a bug fix, because scrolling in recyclerView and transition
        // animation, together can cause a crash.
        mRecycler.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if(canRecyclerScroll) return false;
                return rv.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
            }
        });
    }

    void toggleEditorMode(){
        // bugfix, because it would crash with transition and recycler scrolling combined.
        mRecycler.stopScroll();
        canRecyclerScroll = false;

        int height = mEditorLayout.getHeight();

        int endMargin = 0;
        int recyclerHei = mRecycler.getHeight();
        if (isExpanded) {
            endMargin = 0;
            recyclerHei += height;
            mEditorExpanderButton.setImageDrawable(context.getDrawable(R.drawable.ic_expand_more_accent_24dp));
        } else {
            endMargin = height;
            recyclerHei -= height;
            mEditorExpanderButton.setImageDrawable(context.getDrawable(R.drawable.ic_expand_less_accent_24dp));
        }
        isExpanded = !isExpanded;

        ConstraintSet oldSet= new ConstraintSet();
        oldSet.clone(context, R.layout.logs_dialog);

        // actual changes animated:
        oldSet.setMargin(R.id.expand_editor_image_button, ConstraintSet.TOP, endMargin);
        oldSet.constrainHeight(R.id.recycler, recyclerHei);

        AutoTransition transition = new AutoTransition();
        transition.setDuration(500);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());

        transition.addListener(this);

        TransitionManager.beginDelayedTransition(mMyLayout, transition);
        oldSet.applyTo(mMyLayout);
    }

    @Override
    public void onTransitionStart(Transition transition) {
        mEditorExpanderButton.setEnabled(false);
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        mEditorExpanderButton.setEnabled(true);
        canRecyclerScroll=true;
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }
}
