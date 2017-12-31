package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.shadowice.flocke.andotp.R;

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {
    float padding = 0.0f;
    float fabY = 0.0f;

    private final static float CLICK_DRAG_TOLERANCE = 10;
    private float downRawX;
    private float downX;

    public DraggableFloatingActionButton(Context context) {
        super(context);
        init(context);
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOnTouchListener(this);
        padding = context.getResources().getDimension(R.dimen.fab_base_horizontal_offset);
        fabY = this.getY();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downRawX = motionEvent.getRawX();
                downX = view.getX() - downRawX;
                return true;

            case MotionEvent.ACTION_MOVE:
                float currX = motionEvent.getRawX() + downX;
                view.animate().x(currX).setDuration(0).setStartDelay(0).start();
                return true;

            case MotionEvent.ACTION_UP:
                float upDX = motionEvent.getRawX() - downRawX;

                if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE) {
                    return performClick();
                } else {
                    return true;
                }
            default:
                return super.onTouchEvent(motionEvent);
        }
    }
}
