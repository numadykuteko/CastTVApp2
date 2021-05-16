package com.cast.tv.screen.mirroring.iptv.constants;

import java.util.Arrays;
import java.util.List;

public class DataConstants {

    public static final String DATABASE_NAME = "cast.everything_application";
    public static final String PREF_NAME = "cast.everything_application";

    public static final String EMAIL_DEV = "elaineeyui@gmail.com";

    public static final int CONNECT_TIMEOUT_NETWORK = 10;

    public static final int MIN_TIME_USING_TO_SHOW_RATE = 16 * 1000;

    public static final String PREF_NAME_RATING_US = "PREF_NAME_RATING_US";
    public static final String PREF_NAME_SHOW_GUIDE = "PREF_NAME_SHOW_GUIDE";
    public static final String PREF_NAME_IP_ADDRESS = "PREF_NAME_IP_ADDRESS";

    public static final String IMAGE_DESCRIPTION = "Image provided by " + DATABASE_NAME;

    public static final int MAX_SIZE_IMAGE_LOADER_WIDTH = 200;
    public static final int MAX_SIZE_IMAGE_LOADER_HEIGHT = 300;

    public static final int HORIZONTAL_IMAGE_LOADER_WIDTH = 100;
    public static final int HORIZONTAL_IMAGE_LOADER_HEIGHT = 100;

    public static final String IP_LINK_KEY = "IP_LINK_KEY";

    public static final String FILE_TYPE_PHOTO = "photo";
    public static final String PHOTO_EXTENSION_1 = ".png";
    public static final String PHOTO_MIME_1 = "png";
    public static final String PHOTO_EXTENSION_2 = ".jpg";
    public static final String PHOTO_MIME_2 = "jpg";
    public static final String PHOTO_EXTENSION_3 = ".jpeg";
    public static final String PHOTO_MIME_3 = "jpeg";
    public static final String PHOTO_EXTENSION_4 = ".gif";
    public static final String PHOTO_MIME_4 = "gif";
    public static final String PHOTO_EXTENSION_5 = ".bmp";
    public static final String PHOTO_MIME_5 = "bmp";
    public static final String PHOTO_EXTENSION_6 = ".apng";
    public static final String PHOTO_MIME_6 = "apng";
    public static final String PHOTO_EXTENSION_7 = ".webp";
    public static final String PHOTO_MIME_7 = "webp";

    public static final String FILE_TYPE_VIDEO = "video";
    public static final String VIDEO_EXTENSION_1 = ".mp4";
    public static final String VIDEO_MIME_1 = "mp4";
    public static final String VIDEO_EXTENSION_2 = ".webm";
    public static final String VIDEO_MIME_2 = "webm";
    public static final String VIDEO_EXTENSION_3 = ".wav";
    public static final String VIDEO_MIME_3 = "wav";
    public static final String VIDEO_EXTENSION_4 = ".mp2t";
    public static final String VIDEO_MIME_4 = "mp2t";
    public static final String VIDEO_EXTENSION_5 = ".ogg";
    public static final String VIDEO_MIME_5 = "ogg";

    public static final String FILE_TYPE_AUDIO = "audio";
    public static final String AUDIO_EXTENSION_1 = ".mp3";
    public static final String AUDIO_MIME_1 = "mp3";
    public static final String AUDIO_EXTENSION_2 = ".m4a";
    public static final String AUDIO_MIME_2 = "m4a";
    public static final String AUDIO_EXTENSION_3 = ".mpa";
    public static final String AUDIO_MIME_3 = "mpa";
    public static final String AUDIO_EXTENSION_4 = ".flac";
    public static final String AUDIO_MIME_4 = "flac";

    public static final String STREAM_MIME_1 = "m3u8";

    public static final String FILE_TYPE_DIRECTORY = "__directory";

    public static final String ROOT_DIRECTORY = "/Cast Tv App/";

    public static final List<String> SCREEN_LIST = Arrays.asList("Samsung", "Insignia", "LG", "Sharp", "TCL", "Toshiba", "Westinghouse", "Sony", "Panasonic", "Vizio", "Hisense", "Other");

    public static final String PENDING_INTENT_SERVICE = "PENDING_INTENT_SERVICE";
    public static final int CONNECT_SUCCESS_MESSAGE = 200;
    public static final int CONNECT_ERROR_MESSAGE = 400;
    public static final int CONNECT_DESTROY_MESSAGE = 401;

    public static final List<String> IPTV_CATEGORY_NAME_LIST = Arrays.asList("Auto",
            "Business", "Classic", "Comedy", "Documentary", "Education", "Entertainment",
            "Family", "Fashion", "Food", "General", "Health","History","Hobby","Kids",
            "Legislative","Lifestyle","Local","Movies","Music","News","Quiz","Religious",
            "Sci-Fi","Shop","Sport","Travel","Weather","Other");

    public static final String TRANSCODE_VIDEO_FOLDER_NAME = "cast_123272126216261_transcode_folder";
    public static final String TRANSCODE_VIDEO_PREFIX_NAME = "cast_123272126216261_transcode_video_";
}
