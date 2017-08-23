/*
 * Copyright (C) 2017 Jakob Nixdorf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import org.shadowice.flocke.andotp.R;

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

        baseFloatingActionButton = mainLayout.findViewById(R.id.baseFloatingActionButton);

        qrFAB = mainLayout.findViewById(R.id.qrFAB);
        manualFAB = mainLayout.findViewById(R.id.manualFAB);

        qrLayout = mainLayout.findViewById(R.id.qrLayout);
        manualLayout = mainLayout.findViewById(R.id.manualLayout);

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
                .setDuration(150)
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
                .setDuration(150)
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
