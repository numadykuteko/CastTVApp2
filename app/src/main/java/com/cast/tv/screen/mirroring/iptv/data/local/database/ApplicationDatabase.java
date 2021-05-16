package com.cast.tv.screen.mirroring.iptv.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cast.tv.screen.mirroring.iptv.data.local.database.dao.BookmarkDataDao;
import com.cast.tv.screen.mirroring.iptv.data.local.database.dao.HistoryDataDao;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;

@Database(entities = {HistoryData.class, BookmarkData.class}, version = 1, exportSchema = false)
public abstract class ApplicationDatabase extends RoomDatabase {
    public abstract HistoryDataDao historyDataDao();
    public abstract BookmarkDataDao bookmarkDataDao();
}
