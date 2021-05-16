package com.cast.tv.screen.mirroring.iptv.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;

public class PreferencesHelper implements PreferencesHelperInterface {
    private static PreferencesHelper mInstance;
    private final SharedPreferences mPrefs;

    private PreferencesHelper(Context context) {
        mPrefs = context.getSharedPreferences(DataConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferencesHelper getInstance(Context context) {
        if (mInstance == null) {
            return new PreferencesHelper(context);
        }
        return mInstance;
    }

    @Override
    public void setRatingUs(int rated) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_RATING_US, rated).apply();
    }

    @Override
    public int getRatingUs() {
        return mPrefs.getInt(DataConstants.PREF_NAME_RATING_US, 0);
    }

    @Override
    public void setShowGuideSelectMulti(int shown) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_SHOW_GUIDE, shown).apply();
    }

    @Override
    public int getShowGuideSelectMulti() {
        return mPrefs.getInt(DataConstants.PREF_NAME_SHOW_GUIDE, 0);
    }

    @Override
    public int getTheme() {
        return 0;
    }

    @Override
    public void setTheme(int theme) {

    }

    @Override
    public String getLastIPAddress() {
        return mPrefs.getString(DataConstants.PREF_NAME_IP_ADDRESS, null);
    }

    @Override
    public void saveLastIPAddress(String ipAddress) {
        mPrefs.edit().putString(DataConstants.PREF_NAME_IP_ADDRESS, ipAddress).apply();
    }
}
