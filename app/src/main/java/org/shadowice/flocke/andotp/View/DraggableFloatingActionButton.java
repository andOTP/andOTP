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
                int parentWidth = ((View)view.getParent()).getWidth();
                float parentWidthThird = parentWidth / 3.0f;
                float newX = motionEvent.getRawX() + downX + (view.getWidth() / 2.0f);

                if(newX <= parentWidthThird) {
                    view.animate().x(padding).setDuration(100).setStartDelay(0).start();
                } else if(newX > parentWidthThird && newX < parentWidthThird * 2) {
                    float newPos = (parentWidth / 2.0f) - (view.getWidth() / 2.0f);
                    view.animate().x(newPos).setDuration(100).setStartDelay(0).start();
                } else {
                    float newPos = (parentWidth - padding) - view.getWidth();
                    view.animate().x(newPos).setDuration(100).setStartDelay(0).start();
                }

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
