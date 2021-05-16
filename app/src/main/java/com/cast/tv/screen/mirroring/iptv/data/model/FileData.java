package com.cast.tv.screen.mirroring.iptv.data.model;

import android.net.Uri;

public class FileData {
    private String displayName;
    private Uri fileUri;
    private long dateAdded = -1;
    private int size;
    private String thumbnail;
    private int duration;
    private String fileType;
    private String filePath;
    private FileData parentFile;
    private boolean isBookmarked = false;

    public FileData() {

    }

    public FileData(String displayName, String filePath, Uri fileUri, int dateAdded, int size, String fileType, String thumbnail, int duration) {
        this.displayName = displayName;
        this.fileUri = fileUri;
        this.dateAdded = dateAdded;
        this.fileType = fileType;
        this.size = size;
        this.filePath = filePath;
        this.thumbnail = thumbnail;
        this.duration = duration;
    }

    public FileData(FileData copy) {
        this.displayName = copy.displayName;
        this.fileUri = copy.fileUri;
        this.dateAdded = copy.dateAdded;
        this.fileType = copy.fileType;
        this.size = copy.size;
        this.filePath = copy.filePath;
        this.thumbnail = copy.thumbnail;
        this.duration = copy.duration;
        this.parentFile = copy.parentFile;
        this.isBookmarked = copy.isBookmarked;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
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

    public FileData getParentFile() {
        return parentFile;
    }

    public void setParentFile(FileData parentFile) {
        this.parentFile = parentFile;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public void revertBookmark() {
        isBookmarked = !isBookmarked;
    }
}
