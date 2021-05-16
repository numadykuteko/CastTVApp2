package com.cast.tv.screen.mirroring.iptv.data.model;

import androidx.room.Entity;

@Entity(
        tableName = "bookmark_data"
)
public class BookmarkData extends SavedData {
    public BookmarkData() {

    }

    public BookmarkData(FileData copy) {
        this.displayName = copy.getDisplayName();
        this.dateAdded = copy.getDateAdded();
        this.fileType = copy.getFileType();
        this.size = copy.getSize();
        this.filePath = copy.getFilePath();
        this.thumbnail = copy.getThumbnail();
        this.duration = copy.getDuration();
    }
}
