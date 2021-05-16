package com.cast.tv.screen.mirroring.iptv.data;

import android.content.Context;

import com.cast.tv.screen.mirroring.iptv.data.local.database.DatabaseHelper;
import com.cast.tv.screen.mirroring.iptv.data.local.preferences.PreferencesHelper;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;
import com.cast.tv.screen.mirroring.iptv.data.remote.ApiHelper;

import java.util.List;

import io.reactivex.Observable;

public class DataManager implements DataManagerInterface {
    private static DataManager mInstance;

    private final ApiHelper mApiHelper;
    private final DatabaseHelper mDatabaseHelper;
    private final PreferencesHelper mPreferencesHelper;

    private DataManager(Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
        mPreferencesHelper = PreferencesHelper.getInstance(context);
        mApiHelper = ApiHelper.getInstance();
    }

    public static DataManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataManager(context);
        }
        return mInstance;
    }

    @Override
    public boolean checkRatingUsDone() {
        return mPreferencesHelper.getRatingUs() != 0;
    }

    @Override
    public void setRatingUsDone() {
        mPreferencesHelper.setRatingUs(1);
    }

    @Override
    public void setShowGuideSelectMultiDone() {
        mPreferencesHelper.setShowGuideSelectMulti(1);
    }

    @Override
    public boolean getShowGuideSelectMulti() {
        return mPreferencesHelper.getShowGuideSelectMulti() == 0;
    }

    @Override
    public int getTheme() {
        return mPreferencesHelper.getTheme();
    }

    @Override
    public void setTheme(int theme) {
        mPreferencesHelper.setTheme(theme);
    }

    @Override
    public String getLastIPAddress() {
        return mPreferencesHelper.getLastIPAddress();
    }

    @Override
    public void saveLastIPAddress(String ipAddress) {
        mPreferencesHelper.saveLastIPAddress(ipAddress);
    }

    @Override
    public Observable<Boolean> saveHistory(HistoryData historyData) {
        return mDatabaseHelper.saveHistory(historyData);
    }

    @Override
    public Observable<List<HistoryData>> getListHistory() {
        return mDatabaseHelper.getListHistory();
    }

    @Override
    public Observable<Boolean> clearHistory(HistoryData historyData) {
        return mDatabaseHelper.clearHistory(historyData);
    }

    @Override
    public Observable<Boolean> clearAllHistory() {
        return mDatabaseHelper.clearAllHistory();
    }

    @Override
    public Observable<List<HistoryData>> getListHistoryByType(String type) {
        return mDatabaseHelper.getListHistoryByType(type);
    }

    @Override
    public Observable<Boolean> saveBookmark(BookmarkData bookmarkData) {
        return mDatabaseHelper.saveBookmark(bookmarkData);
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmark() {
        return mDatabaseHelper.getListBookmark();
    }

    @Override
    public Observable<Boolean> isPathBookmarked(String path) {
        return Observable.fromCallable(() -> {
            BookmarkData bookmarkData = mDatabaseHelper.getBookmarkByPath(path);
            return bookmarkData != null && bookmarkData.getFilePath() != null && bookmarkData.getFilePath().equals(path);
        });
    }

    @Override
    public Observable<Boolean> clearBookmarkByPath(String path) {
        return mDatabaseHelper.clearBookmarkByPath(path);
    }

    @Override
    public Observable<Boolean> clearAllBookmark() {
        return mDatabaseHelper.clearAllBookmark();
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmarkByType(String type) {
        return mDatabaseHelper.getListBookmarkByType(type);
    }

}
