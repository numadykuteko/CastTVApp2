package com.cast.tv.screen.mirroring.iptv.data.model;

import androidx.room.Entity;

@Entity(
        tableName = "history_data"
)
public class HistoryData extends SavedData {
    public HistoryData() {

    }

    public HistoryData(FileData copy) {
        this.displayName = copy.getDisplayName();
        this.dateAdded = copy.getDateAdded();
        this.fileType = copy.getFileType();
        this.size = copy.getSize();
        this.filePath = copy.getFilePath();
        this.thumbnail = copy.getThumbnail();
        this.duration = copy.getDuration();
    }
}
