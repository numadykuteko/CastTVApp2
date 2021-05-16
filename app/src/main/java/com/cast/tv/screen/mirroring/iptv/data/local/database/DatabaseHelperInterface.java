package com.cast.tv.screen.mirroring.iptv.data.local.database;

import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;

import java.util.List;

import io.reactivex.Observable;

public interface DatabaseHelperInterface {
    Observable<Boolean> saveHistory(HistoryData historyData);
    Observable<List<HistoryData>> getListHistory();
    Observable<Boolean> clearHistory(HistoryData historyData);
    Observable<Boolean> clearAllHistory();
    Observable<List<HistoryData>> getListHistoryByType(String type);

    Observable<Boolean> saveBookmark(BookmarkData bookmarkData);
    Observable<List<BookmarkData>> getListBookmark();
    BookmarkData getBookmarkByPath(String path);
    Observable<Boolean> clearBookmarkByPath(String path);
    Observable<Boolean> clearAllBookmark();
    Observable<List<BookmarkData>> getListBookmarkByType(String type);
}
