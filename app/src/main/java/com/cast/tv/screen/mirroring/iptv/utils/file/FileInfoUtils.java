package com.cast.tv.screen.mirroring.iptv.utils.file;

import android.annotation.SuppressLint;

import java.io.File;
import java.util.Date;

public class FileInfoUtils {

    // GET PDF DETAILS
    public static String getFormattedDate(File file) {
        Date lastModDate = new Date(file.lastModified());
        String[] formatDate = lastModDate.toString().split(" ");
        String time = formatDate[3];
        String[] formatTime = time.split(":");
        String date = formatTime[0] + ":" + formatTime[1];

        return formatDate[0] + ", " + formatDate[1] + " " + formatDate[2] + " at " + date;
    }

    @SuppressLint("DefaultLocale")
    public static String getFormattedSize(File file) {
        return String.format("%.2f MB", (double) file.length() / (1024 * 1024));
    }
}
