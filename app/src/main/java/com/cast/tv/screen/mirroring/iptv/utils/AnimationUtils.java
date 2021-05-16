package com.cast.tv.screen.mirroring.iptv.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class AnimationUtils {
    public static void scaleView(View v, float xScale, float yScale, int timeInMilis) {
        Animation anim = new ScaleAnimation(
                1f, xScale, // Start and end values for the X axis scaling
                1f, yScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(timeInMilis);
        v.startAnimation(anim);
    }

    private static Animation.AnimationListener defaultAnimationListener() {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setFillAfter(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.cancel();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        };
    }

    public static Animation getAnimation(Context context, int id) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, id);
        animation.setAnimationListener(defaultAnimationListener());

        return animation;
    }

    public static Animation getAnimation(Context context, int id, Animation.AnimationListener animationListener) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, id);
        animation.setAnimationListener(animationListener);

        return animation;
    }

    public static void changeColorTextAnimation (TextView textView, int color_origin, int color_target) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator.ofObject(
                        textView, // Object to animating
                        "textColor", // Property to animate
                        new ArgbEvaluator(), // Interpolation function
                        color_origin, // Start color
                        color_target // End color
                ).setDuration(1500) // Duration in milliseconds
                        .start();
            }
        }, 500);
    }

    public static void setAnimationDouble(View v, int animation_start, int animation_end) {
        v.startAnimation(getAnimation(v.getContext(), animation_start, new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation a) {
                if (animation_end != -1) {
                    v.startAnimation(getAnimation(v.getContext(), animation_end));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        }));
    }
}
