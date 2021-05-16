package com.cast.tv.screen.mirroring.iptv.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

public class SavedData {
    @ColumnInfo(name = "displayName")
    protected String displayName;

    @ColumnInfo(name = "dateAdded")
    protected long dateAdded;

    @ColumnInfo(name = "size")
    protected int size;

    @ColumnInfo(name = "thumbnail")
    protected String thumbnail;

    @ColumnInfo(name = "duration")
    protected int duration;

    @ColumnInfo(name = "fileType")
    protected String fileType;

    @ColumnInfo(name = "filePath")
    @PrimaryKey
    @NonNull
    protected String filePath;

    @ColumnInfo(name = "timeAdded", defaultValue = "0")
    protected long timeAdded;

    public SavedData() {

    }

    public SavedData(String displayName, @NotNull String filePath, Uri fileUri, int dateAdded, int size, String fileType, String thumbnail, int duration) {
        this.displayName = displayName;
        this.dateAdded = dateAdded;
        this.fileType = fileType;
        this.size = size;
        this.filePath = filePath;
        this.thumbnail = thumbnail;
        this.duration = duration;
    }

    public SavedData(FileData copy) {
        this.displayName = copy.getDisplayName();
        this.dateAdded = copy.getDateAdded();
        this.fileType = copy.getFileType();
        this.size = copy.getSize();
        this.filePath = copy.getFilePath();
        this.thumbnail = copy.getThumbnail();
        this.duration = copy.getDuration();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;

        if (filePath.contains("/")) {
            String temp = filePath;
            while (temp.charAt(temp.length() - 1) == '/') {
                temp = temp.substring(0, filePath.length() - 1);
            }

            if (temp.contains("/")) {
                displayName = filePath.substring(filePath.lastIndexOf("/") + 1);
            } else {
                displayName = "No name";
            }
        } else {
            displayName = "No name";
        }
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }
}
