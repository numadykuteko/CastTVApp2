package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;
import android.text.format.DateFormat;

import com.cast.tv.screen.mirroring.iptv.R;

import java.util.Date;

public class DateTimeUtils {
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final String DATE_TIME_NAMING_FORMAT = "ddMMyyyy_HHmmss";
    private static final String DATE_NAMING_FORMAT = "ddMMyyyy";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final long SECOND = 1000;
    private static final long MINUTE = 60*SECOND;
    private static final long HOUR = 60*MINUTE;
    private static final long DAY = 24*HOUR;
    private static final long MONTH = 30*DAY;
    private static final long YEAR = 365*DAY;

    public static Date getCurrentDateTime() {
        return new Date(System.currentTimeMillis());
    }

    public static long getCurrentDateTimeUnix() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String fromDateToDateTimeString(Date date) {
        return DateFormat.format(DATE_TIME_FORMAT, date).toString();
    }

    public static String fromDateToDateString(Date date) {
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    public static String fromDateToDescriptionDate(Context context, Date date) {
        return fromTimeUnixToDescriptionDate(context, date.getTime());
    }

    public static String currentTimeToNaming() {
        return DateFormat.format(DATE_TIME_NAMING_FORMAT, new Date()).toString();
    }

    public static String currentDateToNaming() {
        return DateFormat.format(DATE_NAMING_FORMAT, new Date()).toString();
    }

    public static String fromTimeUnixToDateTimeString(long timeUnix) {
        if (timeUnix == 0)  return "";
        Date date = new Date(timeUnix * 1000L);
        return DateFormat.format(DATE_TIME_FORMAT, date).toString();
    }

    public static String fromTimeUnixToDateString(long timeUnix) {
        if (timeUnix == 0)  return "";
        Date date = new Date(timeUnix * 1000L);
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    public static Date fromMillisToDate(long timeUnix) {
        if (timeUnix == 0)  return new Date();
        return new Date(timeUnix * 1000L);
    }
    
    public static String fromTimeUnixToDescriptionDate(Context context, long timeUnix) {
        long currentTime = System.currentTimeMillis();
        long distanceTime = currentTime - timeUnix * 1000L;
        if (distanceTime <= MINUTE) {
            return context.getString(R.string.description_time_just_now);
        }
        if (distanceTime <= HOUR) {
            int number = (int) (distanceTime / MINUTE);
            return number + " " + context.getString(R.string.description_time_minute) + (number > 1 ? "s " : " ") + context.getString(R.string.description_time_ago);
        }
        if (distanceTime <= DAY) {
            int number = (int) (distanceTime / HOUR);
            return number + " " + context.getString(R.string.description_time_hour) + (number > 1 ? "s " : " ") + context.getString(R.string.description_time_ago);
        }
        if (distanceTime <= MONTH) {
            int number = (int) (distanceTime / DAY);
            return number + " " + context.getString(R.string.description_time_day) + (number > 1 ? "s " : " ") + context.getString(R.string.description_time_ago);
        }
        if (distanceTime <= YEAR) {
            int number = (int) (distanceTime / MONTH);
            return number + " " + context.getString(R.string.description_time_month) + (number > 1 ? "s " : " ") + context.getString(R.string.description_time_ago);
        }
        int number = (int) (distanceTime / YEAR);
        return number + " " + context.getString(R.string.description_time_year) + (number > 1 ? "s " : " ") + context.getString(R.string.description_time_ago);
    }

    public static String fromDurationToString(int duration) {
        int time = duration / 1000;
        int hour = time / 3600;
        int rest_time = time % 3600;

        int minute = rest_time / 60;
        int second = rest_time % 60;

        StringBuilder stringBuilder = new StringBuilder();

        if (hour > 0) stringBuilder.append(hour).append(" : ");

        if (minute >= 10) stringBuilder.append(minute);
        else stringBuilder.append(0).append(minute);

        stringBuilder.append(" : ");

        if (second >= 10) stringBuilder.append(second);
        else stringBuilder.append(0).append(second);

        return stringBuilder.toString();
    }
}
