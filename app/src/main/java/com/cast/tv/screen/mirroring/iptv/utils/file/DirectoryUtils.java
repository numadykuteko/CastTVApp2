package com.cast.tv.screen.mirroring.iptv.utils.file;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryUtils {
    private Context mContext;
    private ArrayList<String> mFilePaths;

    public DirectoryUtils(Context context) {
        mContext = context;
        mFilePaths = new ArrayList<>();
    }


    ArrayList<String> getAllPhotosOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.PHOTO_EXTENSION_1, DataConstants.PHOTO_EXTENSION_2, DataConstants.PHOTO_EXTENSION_3, DataConstants.PHOTO_EXTENSION_4));
        return mFilePaths;
    }

    ArrayList<String> getAllAudiosOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.AUDIO_EXTENSION_1, DataConstants.AUDIO_EXTENSION_2, DataConstants.AUDIO_EXTENSION_3, DataConstants.AUDIO_EXTENSION_4));
        return mFilePaths;
    }

    ArrayList<String> getAllVideosOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.VIDEO_EXTENSION_1, DataConstants.VIDEO_EXTENSION_2, DataConstants.VIDEO_EXTENSION_3));
        return mFilePaths;
    }

    private void walkDir(File dir, List<String> extensions) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDir(aListFile, extensions);
                } else {
                    for (String extension: extensions) {
                        if (aListFile.getName().endsWith(extension)) {
                            //Do what ever u want
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public static String getDefaultStorageLocation() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DataConstants.ROOT_DIRECTORY);
        if (!dir.exists()) {
            boolean isDirectoryCreated = dir.mkdir();
            if (!isDirectoryCreated) {
                Log.e("Error", "Directory could not be created");
            }
        }
        return dir.getAbsolutePath() + "/";
    }

    public static String getImageStorageLocation(String rootFileName, String afterFix) {
        String splitFolder = "";
        if (rootFileName.length() > 8) {
            splitFolder = rootFileName.substring(0, 7) + afterFix;
        } else {
            splitFolder = rootFileName + afterFix;
        }
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DataConstants.ROOT_DIRECTORY + "/" + splitFolder);
        if (!dir.exists()) {
            boolean isDirectoryCreated = dir.mkdir();
            if (!isDirectoryCreated) {
                Log.e("Error", "Directory could not be created");
            }
        }
        return dir.getAbsolutePath() + "/";
    }
}
