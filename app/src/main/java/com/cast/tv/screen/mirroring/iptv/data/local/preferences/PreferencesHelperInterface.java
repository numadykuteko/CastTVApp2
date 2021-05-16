package com.cast.tv.screen.mirroring.iptv.data.local.preferences;

public interface PreferencesHelperInterface {
    int getRatingUs();
    void setRatingUs(int rated);

    void setShowGuideSelectMulti(int shown);
    int getShowGuideSelectMulti();

    int getTheme();
    void setTheme(int theme);

    String getLastIPAddress();
    void saveLastIPAddress(String ipAddress);
}
