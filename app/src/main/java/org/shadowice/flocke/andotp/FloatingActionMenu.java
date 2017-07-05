package org.shadowice.flocke.andotp;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

public class FloatingActionMenu {
    private boolean isFabMenuOpen = false;

    private ConstraintLayout mainLayout;

    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;

    private FloatingActionButton baseFloatingActionButton;
    private FloatingActionButton qrFAB;
    private FloatingActionButton manualFAB;

    private LinearLayout qrLayout;
    private LinearLayout manualLayout;

    private FABHandler fabHandler;

    public FloatingActionMenu(Context context, ConstraintLayout mainLayout) {
        this.mainLayout = mainLayout;

        fabOpenAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_close);

        baseFloatingActionButton = (FloatingActionButton) mainLayout.findViewById(R.id.baseFloatingActionButton);

        qrFAB = (FloatingActionButton) mainLayout.findViewById(R.id.qrFAB);
        manualFAB = (FloatingActionButton) mainLayout.findViewById(R.id.manualFAB);

        qrLayout = (LinearLayout) mainLayout.findViewById(R.id.qrLayout);
        manualLayout = (LinearLayout) mainLayout.findViewById(R.id.manualLayout);

        baseFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabMenuOpen)
                    collapse();
                else
                    expand();
            }
        });

        qrFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabHandler != null)
                    fabHandler.onQRFabClick();
                collapse();
            }
        });

        manualFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabHandler != null)
                    fabHandler.onManualFabClick();
                collapse();
            }
        });
    }

    public void setFABHandler(FABHandler fabHandler) {
        this.fabHandler = fabHandler;
    }

    public void show() {
        mainLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mainLayout.setVisibility(View.GONE);
    }

    public void expand() {
        ViewCompat.animate(baseFloatingActionButton)
                .rotation(45F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new LinearInterpolator())
                .start();

        qrLayout.setVisibility(View.VISIBLE);
        manualLayout.setVisibility(View.VISIBLE);

        qrLayout.startAnimation(fabOpenAnimation);
        manualLayout.startAnimation(fabOpenAnimation);

        qrFAB.setClickable(true);
        manualFAB.setClickable(true);

        isFabMenuOpen = true;
    }

    public void collapse() {
        ViewCompat.animate(baseFloatingActionButton)
                .rotation(0F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new LinearInterpolator())
                .start();

        qrLayout.startAnimation(fabCloseAnimation);
        manualLayout.startAnimation(fabCloseAnimation);

        qrLayout.setVisibility(View.GONE);
        manualLayout.setVisibility(View.GONE);

        qrFAB.setClickable(false);
        manualFAB.setClickable(false);

        isFabMenuOpen = false;

    }

    public interface FABHandler {
        void onQRFabClick();
        void onManualFabClick();
    }
}
