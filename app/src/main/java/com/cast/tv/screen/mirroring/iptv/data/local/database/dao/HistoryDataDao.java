package com.cast.tv.screen.mirroring.iptv.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface HistoryDataDao {

    @Delete
    void delete(HistoryData historyData);

    @Query("DELETE FROM history_data WHERE filePath = :path")
    void deleteByPath(String path);

    @Query("DELETE FROM history_data")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryData historyData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HistoryData> historyDataList);

    @Query("SELECT * FROM history_data ORDER BY timeAdded DESC")
    Single<List<HistoryData>> loadAll();

    @Query("SELECT * FROM history_data WHERE fileType = :type ORDER BY timeAdded DESC")
    Single<List<HistoryData>> loadAllByType(String type);
}
