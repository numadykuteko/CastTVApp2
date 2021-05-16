package com.cast.tv.screen.mirroring.iptv.utils.ads;

import android.content.Context;

public class AdsShowCountManager {
    private static final String LOG = "AdsShowCountManager";

    private static AdsShowCountManager mInstance;
    private int mCountForClickItem;
    private final int NUMBER_TIME_TO_SHOW_ONCE = 1;

    private AdsShowCountManager() {
        mCountForClickItem = 0;
    }

    public static AdsShowCountManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AdsShowCountManager();
        }

        return mInstance;
    }


    public boolean checkShowAdsForClickItem() {
        return mCountForClickItem == NUMBER_TIME_TO_SHOW_ONCE;
    }

    public void increaseCountForClickItem() {
        if (mCountForClickItem == NUMBER_TIME_TO_SHOW_ONCE) {
            mCountForClickItem = 0;
        } else {
            mCountForClickItem ++;
        }
    }

}
