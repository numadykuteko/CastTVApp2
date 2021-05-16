package com.cast.tv.screen.mirroring.iptv.utils.file;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.video.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
    public static final String AUTHORITY_APP = "com.cast.tv.screen.mirroring.iptv.provider";

    public enum FileType {
        type_IMAGE,
    }

    private static final List<String> orderList = Arrays.asList(MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.SIZE);
    public static final int SORT_BY_DATE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_BY_SIZE_REVERT = 3;
    public static final int SORT_BY_TYPE = 4;

    public static ArrayList<FileData> getExternalFileList(Context context, String fileType, int order) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.SIZE};
        String selectionMimeType;
        String[] selectionArgs;
        if (fileType.equals(DataConstants.FILE_TYPE_PHOTO)) {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR" + MediaStore.Files.FileColumns.MIME_TYPE + "= ?";
            selectionArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.PHOTO_MIME_1),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.PHOTO_MIME_2),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.PHOTO_MIME_3),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.PHOTO_MIME_4)};

        } else if (fileType.equals(DataConstants.FILE_TYPE_VIDEO)) {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR" + MediaStore.Files.FileColumns.MIME_TYPE + "= ?";
            selectionArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.VIDEO_MIME_1),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.VIDEO_MIME_2),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.VIDEO_MIME_3)};
        } else {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
            selectionArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.AUDIO_MIME_1),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.AUDIO_MIME_2),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.AUDIO_MIME_3),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(DataConstants.AUDIO_MIME_4)};
        }

        String orderBy;
        if (order == 1) {
            orderBy = orderList.get(order) + " ASC";
        } else {
            orderBy = orderList.get(order) + " DESC";
        }

        Cursor cursor = cr.query(uri, projection, selectionMimeType, selectionArgs, orderBy);
        ArrayList<FileData> fileList = new ArrayList<>();
        if (cursor != null) {

            while (cursor.moveToNext()) {

                int columnIdIndex = cursor.getColumnIndex(projection[0]);
                int columnNameIndex = cursor.getColumnIndex(projection[1]);
                int columnDateIndex = cursor.getColumnIndex(projection[2]);
                int columnSizeIndex = cursor.getColumnIndex(projection[3]);

                long fileId = -1;
                try {
                    fileId = cursor.getLong(columnIdIndex);
                } catch (Exception e) {
                    continue;
                }

                Uri fileUri = Uri.parse(uri.toString() + "/" + fileId);

                String displayName = cursor.getString(columnNameIndex);
                if (displayName == null || displayName.length() == 0) {
                    displayName = "No name";
                }

                int dateAdded;
                try {
                    dateAdded = Integer.parseInt(cursor.getString(columnDateIndex));
                } catch (Exception e) {
                    dateAdded = -1;
                }

                int size = 0;
                try {
                    size = Integer.parseInt(cursor.getString(columnSizeIndex));
                } catch (Exception e) {
                    size = -1;
                }

                fileList.add(new FileData(displayName, null, fileUri, dateAdded, size, fileType, null, -1));
            }
            cursor.close();
        }
        return fileList;
    }

    public static ArrayList<FileData> getAllVideos(Context context) {
        ArrayList<FileData> mListVideo = new ArrayList<>();

        int column_index_data, column_index_date, column_index_duration, column_index_id, column_index_thum;

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Thumbnails.DATA
        };

        final String orderBy = MediaStore.Images.Media.DATE_ADDED;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_date = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            column_index_thum = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
            column_index_duration = -2330303;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    column_index_duration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                }
            } catch (Exception ignored) {
            }

            while (cursor.moveToNext()) {
                FileData fileData = new FileData();
                fileData.setFileType(DataConstants.FILE_TYPE_VIDEO);

                fileData.setFilePath(cursor.getString(column_index_data));
                if (fileData.getFilePath() == null) {
                    continue;
                }

                fileData.setThumbnail(cursor.getString(column_index_thum));
                try {
                    fileData.setDateAdded(Integer.parseInt(cursor.getString(column_index_date)));
                } catch (Exception e) {
                    fileData.setDateAdded(-1);
                }

                if (column_index_duration != -2330303 && cursor.getString(column_index_duration) != null && cursor.getString(column_index_duration).length() > 0) {
                    try {
                        fileData.setDuration(Integer.parseInt(cursor.getString(column_index_duration)));
                    } catch (Exception e) {
                        fileData.setDuration(0);
                    }
                } else {
                    fileData.setDuration(0);
                }

                mListVideo.add(fileData);
            }
            cursor.close();
        }

        if (mListVideo.size() > 0) {
            List<FileData> needToRemove = new ArrayList<>();

            for (FileData fileData : mListVideo) {
                if (fileData != null && fileData.getFilePath() != null) {
                    String path = fileData.getFilePath().toLowerCase();
                    if (path.endsWith(".webm") || path.endsWith(".mp4") || path.endsWith(".wav") || path.endsWith(".mp2t") || path.endsWith(".ogg")) {
                        continue;
                    }
                    needToRemove.add(fileData);
                } else {
                    needToRemove.add(fileData);
                }
            }

            mListVideo.removeAll(needToRemove);
        }

        return mListVideo;
    }

    /**
     * Uri selectedFileUri = data.getData();
     * String selectedImagePath = FileUtils.getRealPathV3(selectedFileUri, context);
     */
    public static String getRealPathV3(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String[] split = DocumentsContract.getDocumentId(uri).split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                String uriId = DocumentsContract.getDocumentId(uri);
                if (uriId.startsWith("raw:/")) {
                    return uriId.replace("raw:/", "");
                } else if (uriId.startsWith("msf:")) {
                    return null;
                } else {
                    try {
                        long idLong = Long.parseLong(uriId);
                        final Uri downloadContentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), idLong);

                        return getDataColumn(context, downloadContentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return getIndexAsString(cursor, column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static String getIndexAsString(Cursor c, int i) {
        if (i == -1)
            return null;
        if (c.isNull(i)) {
            return null;
        }
        switch (c.getType(i)) {
            case Cursor.FIELD_TYPE_STRING:
                return c.getString(i);
            case Cursor.FIELD_TYPE_FLOAT: {
                return Double.toString(c.getDouble(i));
            }
            case Cursor.FIELD_TYPE_INTEGER: {
                return Long.toString(c.getLong(i));
            }
            default:
            case Cursor.FIELD_TYPE_NULL:
            case Cursor.FIELD_TYPE_BLOB:
                throw new IllegalStateException("data null");
        }
    }

    public static ArrayList<FileData> getAllExternalFileList(Context context, String fileType, int order) {
        DirectoryUtils directoryUtils = new DirectoryUtils(context);
        ArrayList<String> fileList = new ArrayList<>();

        switch (fileType) {
            case DataConstants.FILE_TYPE_AUDIO:
                fileList = directoryUtils.getAllAudiosOnDevice();
                break;
            case DataConstants.FILE_TYPE_PHOTO:
                fileList = directoryUtils.getAllPhotosOnDevice();
                break;
            case DataConstants.FILE_TYPE_VIDEO:
                fileList = directoryUtils.getAllVideosOnDevice();
                break;
        }

        ArrayList<FileData> resultList = new ArrayList<>();
        for (String filePath : fileList) {
            File file = new File(filePath);
            if (!file.exists()) continue;

            Uri uri = Uri.fromFile(file);
            int size = Integer.parseInt(String.valueOf(file.length() / 1024));

            FileData fileData = new FileData(getFileName(filePath), filePath, uri, (int) (file.lastModified() / 1000), size, fileType, null, -1);
            resultList.add(fileData);
        }

        FileSortUtils.performSortOperation(order, resultList);

        return resultList;
    }

    public static ArrayList<FileData> getFileListOfDirectory(FileData dirFileData) {
        String dirPath = dirFileData.getFilePath();

        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] listFile = dir.listFiles();
        ArrayList<FileData> resultList = new ArrayList<>();

        if (listFile != null) {
            for (File file : listFile) {
                FileData fileData = new FileData();
                fileData.setFilePath(file.getAbsolutePath());
                fileData.setParentFile(dirFileData);
                fileData.setDateAdded(file.lastModified() / 1000);

                if (file.isDirectory()) {
                    fileData.setFileType(DataConstants.FILE_TYPE_DIRECTORY);
                } else {
                    if (FileUtils.isVideoFile(file.getAbsolutePath())) {
                        fileData.setFileType(DataConstants.FILE_TYPE_VIDEO);
                    } else if (FileUtils.isPhotoFile(file.getAbsolutePath())) {
                        fileData.setFileType(DataConstants.FILE_TYPE_PHOTO);
                    } else if (FileUtils.isAudioFile(file.getAbsolutePath())) {
                        fileData.setFileType(DataConstants.FILE_TYPE_AUDIO);
                    } else {
                        continue;
                    }

                    fileData.setSize(Integer.parseInt(String.valueOf(file.length() / 1024)));
                    fileData.setFileUri(Uri.fromFile(file));
                }

                resultList.add(fileData);
            }
        }

        FileSortUtils.performSortOperation(SORT_BY_TYPE, resultList);

        return resultList;
    }

    public static void shareFile(Context context, File file) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        context.startActivity(Intent.createChooser(sharingIntent, "Share file with"));
    }

    private static void shareFileWithType(Context context, ArrayList<Uri> uris, String type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_file_title));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(type);

        try {
            context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_chooser)));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not share file now.");
        }
    }

    public static void uploadFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Document")
                .setType("")
                .setStream(uri)
                .getIntent()
                .setPackage("com.google.android.apps.docs");

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static void uploadTxtFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Document")
                .setType("application/txt")
                .setStream(uri)
                .getIntent()
                .setPackage("com.google.android.apps.docs");

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static void uploadImageFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Image")
                .setType("image/png")
                .setStream(uri)
                .getIntent()
                .setPackage("com.google.android.apps.docs");

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    private static void openFileInternal(Context context, String path, String dataType) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);

            target.setDataAndType(uri, dataType);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(target, context.getString(R.string.open_file)));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, context.getString(R.string.open_file_error));
        }
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = "File name";
        String scheme = uri.getScheme();

        if (scheme == null)
            return null;

        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }

        return fileName;
    }

    public static String getFileName(String path) {
        if (path == null)
            return "File name";

        int index = path.lastIndexOf("/");
        return index < path.length() ? path.substring(index + 1) : "File name";
    }

    public static String getFileDirectoryPath(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    public static String getMinimalDirectoryPath(String directoryPath, String firstIndicator) {
        if (directoryPath.contains(firstIndicator)) {
            return directoryPath.substring(directoryPath.indexOf(firstIndicator));
        }

        return directoryPath;
    }

    public static String getUniqueOtherFileName(Context context, String fileName, String extension) {

        String outputFileName = fileName;
        File file = new File(outputFileName);

        if (!file.exists())
            return outputFileName;

        File parentFile = file.getParentFile();
        if (parentFile != null) {
            File[] listFiles = parentFile.listFiles();

            if (listFiles != null) {
                int append = checkRepeat(outputFileName, Arrays.asList(listFiles), extension);
                outputFileName = outputFileName.replace(extension, append + extension);
            }
        }

        return outputFileName;
    }

    private static int checkRepeat(String finalOutputFile, final List<File> mFile, String extension) {
        boolean flag = true;
        int append = 0;
        while (flag) {
            append++;
            String name = finalOutputFile.replace(extension,
                    "(" + append + ")" + extension);
            flag = mFile.contains(new File(name));
        }

        return append;
    }

    public static void deleteFileOnExist(String path) {
        if (path == null) return;

        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean checkFileExist(String path) {
        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception ignored) {
        }

        return false;
    }

    public static boolean checkFileExistAndType(String path, FileType fileType) {
        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }

            path = path.toLowerCase();

            switch (fileType) {

                case type_IMAGE:
                    return path.endsWith(".jpeg") || path.endsWith(".jpg") || path.endsWith(".png");


            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public static boolean isVideoFile(String filePath) {
        return (filePath.toLowerCase().endsWith(DataConstants.VIDEO_EXTENSION_1) ||
                filePath.toLowerCase().endsWith(DataConstants.VIDEO_EXTENSION_2) ||
                filePath.toLowerCase().endsWith(DataConstants.VIDEO_EXTENSION_3) ||
                filePath.toLowerCase().endsWith(DataConstants.VIDEO_EXTENSION_4) ||
                filePath.toLowerCase().endsWith(DataConstants.VIDEO_EXTENSION_5));
    }

    public static boolean isAudioFile(String filePath) {
        return (filePath.toLowerCase().endsWith(DataConstants.AUDIO_EXTENSION_1) ||
                filePath.toLowerCase().endsWith(DataConstants.AUDIO_EXTENSION_2) ||
                filePath.toLowerCase().endsWith(DataConstants.AUDIO_EXTENSION_3) ||
                filePath.toLowerCase().endsWith(DataConstants.AUDIO_EXTENSION_4));
    }

    public static boolean isPhotoFile(String filePath) {
        return (filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_1) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_2) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_3) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_4) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_5) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_6) ||
                filePath.toLowerCase().endsWith(DataConstants.PHOTO_EXTENSION_7));
    }

    public static boolean checkSupportedFile(String filePath) {
        if (isVideoFile(filePath)) {
            return true;
        } else if (isAudioFile(filePath)) {
            return true;
        } else return isPhotoFile(filePath);
    }

    public static int getDurationOfMedia(Context context, String videoPath) {
        if (checkFileExist(videoPath)) {
            Uri uri = Uri.fromFile(new File(videoPath));
            if (uri == null)
                return 0;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    Cursor cursor = null;

                    cursor = MediaStore.Video.query(context.getContentResolver(), uri, new String[]{MediaStore.Video.VideoColumns.DURATION});

                    if (cursor.moveToFirst()) {
                        String duration = cursor.getString(0);
                        return Integer.parseInt(duration);
                    }
                } catch (Exception ignored) {}
            }

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(context, Uri.fromFile(new File(videoPath)));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);

            retriever.release();

            return (int) (timeInMillisec / 1000) * 1000;
        }

        return 0;
    }

    public static String getContentType(String path) {

        if (path.toLowerCase().endsWith(".mp3")) {
            return "audio/mp3";
        } else if (path.toLowerCase().endsWith(".m4a")) {
            return "audio/mp4";
        } else if (path.toLowerCase().endsWith(".mpa")) {
            return "audio/mpeg";
        } else if (path.toLowerCase().endsWith(".webm")) {
            return "video/webm";
        } else if (path.toLowerCase().endsWith(".mp4")) {
            if (VideoUtils.isAACVideo(path)) {
                return "audio/mp4";
            }
            return "video/mp4";
        } else if (path.toLowerCase().endsWith(".wav")) {
            return "video/mp4";
        } else if (path.toLowerCase().endsWith(".mp2t")) {
            return "video/mp4";
        } else if (path.toLowerCase().endsWith(".ogg")) {
            return "video/mp4";
        } else if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".bmp") ) {
            return "image/jpeg";
        } else if (path.toLowerCase().endsWith(".png") || path.toLowerCase().endsWith(".apng")) {
            return "image/png";
        } else if (path.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (path.toLowerCase().endsWith(".webp")) {
            return "image/png";
        } else if (path.toLowerCase().endsWith(".m3u8")) {
            return "application/x-mpegurl";
        }

        return "video/mp4";
    }
}
