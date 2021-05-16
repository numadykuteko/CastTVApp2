package com.cast.tv.screen.mirroring.iptv.utils;

import android.app.Activity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class SnackBarUtils {
    public static Snackbar getSnackbar(Activity context, String message) {
        return Snackbar.make(Objects.requireNonNull(context).findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
    }

    public static Snackbar getIndefiniteSnackbar(Activity context, String message) {
        return Snackbar.make(Objects.requireNonNull(context).findViewById(android.R.id.content),
                message, Snackbar.LENGTH_INDEFINITE);
    }
}
