package com.cast.tv.screen.mirroring.iptv.utils.ads;

import android.content.Context;

public class RateShowCountManager {
    private static final String LOG = "AdsShowCountManager";

    private static RateShowCountManager mInstance;
    private int mCountForBackHome;
    private final int NUMBER_TIME_TO_SHOW_ONCE = 4;

    private RateShowCountManager() {
        mCountForBackHome = 3;
    }

    public static RateShowCountManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RateShowCountManager();
        }

        return mInstance;
    }


    public boolean checkShowRateForBackHome() {
        return mCountForBackHome == NUMBER_TIME_TO_SHOW_ONCE;
    }

    public void increaseCountForBackHome() {
        if (mCountForBackHome == NUMBER_TIME_TO_SHOW_ONCE) {
            mCountForBackHome = 0;
        } else {
            mCountForBackHome++;
        }
    }

}
