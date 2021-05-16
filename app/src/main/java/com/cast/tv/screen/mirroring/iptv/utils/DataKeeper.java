package com.cast.tv.screen.mirroring.iptv.utils;

import com.cast.tv.screen.mirroring.iptv.data.model.ImageDataList;

public class DataKeeper {
    private static ImageDataList mImageDataList;

    public static void setImageDataList(ImageDataList imageDataList) {
        mImageDataList = imageDataList;
    }

    public static void releaseList() {
        mImageDataList = null;
    }

    public static ImageDataList getImageDataList() {
        return mImageDataList;
    }
}
