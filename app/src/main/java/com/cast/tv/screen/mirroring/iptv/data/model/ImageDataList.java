package com.cast.tv.screen.mirroring.iptv.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImageDataList implements Serializable {
    private List<ImageData> mImageDataList;

    public ImageDataList() {
        this.mImageDataList = new ArrayList<>();
    }

    public ImageDataList(List<ImageData> mImageDataList) {
        this.mImageDataList = mImageDataList;
    }

    public List<ImageData> getImageDataList() {
        return mImageDataList;
    }

    public void setImageDataList(List<ImageData> mImageDataList) {
        this.mImageDataList = mImageDataList;
    }
}
