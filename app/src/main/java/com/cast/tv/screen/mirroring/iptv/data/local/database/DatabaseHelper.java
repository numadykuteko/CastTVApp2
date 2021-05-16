package com.cast.tv.screen.mirroring.iptv.data.local.database;

import android.content.Context;

import androidx.room.Room;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;

import java.util.List;

import io.reactivex.Observable;

public class DatabaseHelper implements DatabaseHelperInterface {
    private static DatabaseHelper mInstance;
    private final ApplicationDatabase mApplicationDatabase;

    private DatabaseHelper(Context context) {
        mApplicationDatabase = Room.databaseBuilder(context, ApplicationDatabase.class, DataConstants.DATABASE_NAME).fallbackToDestructiveMigration()
                .build();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            return new DatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public Observable<Boolean> saveHistory(HistoryData historyData) {
        return Observable.fromCallable(() -> {
            historyData.setTimeAdded(System.currentTimeMillis() / 1000L);
            mApplicationDatabase.historyDataDao().insert(historyData);
            return true;
        });
    }

    @Override
    public Observable<List<HistoryData>> getListHistoryByType(String type) {
        return mApplicationDatabase.historyDataDao().loadAllByType(type).toObservable();
    }

    @Override
    public Observable<List<HistoryData>> getListHistory() {
        return mApplicationDatabase.historyDataDao().loadAll().toObservable();
    }

    @Override
    public Observable<Boolean> clearHistory(HistoryData historyData) {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.historyDataDao().delete(historyData);
            return true;
        });
    }

    @Override
    public Observable<Boolean> clearAllHistory() {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.historyDataDao().deleteAll();
            return true;
        });
    }


    @Override
    public Observable<Boolean> saveBookmark(BookmarkData bookmarkData) {
        return Observable.fromCallable(() -> {
            bookmarkData.setTimeAdded(System.currentTimeMillis() / 1000L);
            mApplicationDatabase.bookmarkDataDao().insert(bookmarkData);
            return true;
        });
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmark() {
        return mApplicationDatabase.bookmarkDataDao().loadAll().toObservable();
    }

    @Override
    public BookmarkData getBookmarkByPath(String path) {
        return mApplicationDatabase.bookmarkDataDao().findByPath(path);
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmarkByType(String type) {
        return mApplicationDatabase.bookmarkDataDao().loadAllByType(type).toObservable();
    }

    @Override
    public Observable<Boolean> clearBookmarkByPath(String path) {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.bookmarkDataDao().deleteByPath(path);
            return true;
        });
    }

    @Override
    public Observable<Boolean> clearAllBookmark() {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.bookmarkDataDao().deleteAll();
            return true;
        });
    }
}
