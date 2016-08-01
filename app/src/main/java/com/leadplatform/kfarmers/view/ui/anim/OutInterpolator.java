package com.leadplatform.kfarmers.view.ui.anim;

import android.view.animation.Interpolator;

public class OutInterpolator implements Interpolator {
    private static final float DEFAULT_OVERSHOOT = 1.3F;
    private float mOverShoot = DEFAULT_OVERSHOOT;

    public OutInterpolator() {
    }

    public OutInterpolator(float paramFloat) {
        this.mOverShoot = paramFloat;
    }

    public float getInterpolation(float paramFloat) {
        float f = paramFloat - 1.0F;
        return 1.0F + f * f * (f * (1.0F + this.mOverShoot) + this.mOverShoot);
    }
}