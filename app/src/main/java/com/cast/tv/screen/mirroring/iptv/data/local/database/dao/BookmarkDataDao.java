package com.cast.tv.screen.mirroring.iptv.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface BookmarkDataDao {

    @Delete
    void delete(BookmarkData bookmarkData);

    @Query("DELETE FROM bookmark_data WHERE filePath = :path")
    void deleteByPath(String path);

    @Query("DELETE FROM bookmark_data")
    void deleteAll();

    @Query("SELECT * FROM bookmark_data WHERE filePath = :path LIMIT 1")
    BookmarkData findByPath(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookmarkData bookmarkData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookmarkData> bookmarkDataList);

    @Query("SELECT * FROM bookmark_data ORDER BY timeAdded DESC")
    Single<List<BookmarkData>> loadAll();

    @Query("SELECT * FROM bookmark_data WHERE fileType = :type ORDER BY timeAdded DESC")
    Single<List<BookmarkData>> loadAllByType(String type);
}
