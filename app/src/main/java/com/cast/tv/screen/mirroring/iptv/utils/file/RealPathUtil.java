package com.cast.tv.screen.mirroring.iptv.utils.file;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.utils.DateTimeUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class RealPathUtil {

    private static class SingletonHolder {
        static final RealPathUtil INSTANCE = new RealPathUtil();
    }

    public static RealPathUtil getInstance() {
        return RealPathUtil.SingletonHolder.INSTANCE;
    }

    /**
     * Returns actual path from uri
     *
     * @param context - current context
     * @param fileUri - uri of file
     * @return - actual path
     */
    public String getRealPath(Context context, Uri fileUri) {
        if (fileUri == null) return null;

        return getRealPathFromURI_API19(context, fileUri);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    private String getRealPathFromURI_API19(final Context context, final Uri uri) {
        String path = null;
        if (uri == null) return null;
        Log.d("duynm", uri.toString());

        // DocumentProvider

        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if (type != null && "primary".equals(type.toLowerCase())) {
                        if (split.length > 1) {
                            path = Environment.getExternalStorageDirectory().toString() + "/" + split[1];
                        } else {
                            path = Environment.getExternalStorageDirectory().toString() + "/";
                        }
                    } else {
                        File[] external = context.getExternalMediaDirs();
                        for (File f: external) {
                            String filePath = f.getAbsolutePath();

                            if (filePath.contains(type)) {
                                return filePath.substring(0, filePath.indexOf("Android")) + split[1];
                            }
                        }
                        return "storage" + "/" + docId.replace(":", "/");
                    }
                } else if (isMediaDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, true);
                } else if (isRawDownloadsDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, true);
                } else if (isDownloadsDocument(uri)) {
                    path = getDownloadsDocumentPath(context, uri, false);
                } else {
                    path = loadToCacheFile(context, uri);
                }
            } else {
                path = loadToCacheFile(context, uri);
            }
        } catch (Exception e) {
            return null;
        }

        return path;
    }

    private String loadToCacheFile(Context context, Uri uri) {
        try {
            if (uri == null) return null;

            if (!uri.toString().contains("/") || uri.toString().lastIndexOf("/") == uri.toString().length() - 1) return null;
            String nameFile = uri.toString().substring(uri.toString().lastIndexOf("/") + 1);

            if (!nameFile.contains(".")) return null;
            String prefix = nameFile.substring(nameFile.lastIndexOf("."));

            nameFile = nameFile.substring(0, nameFile.lastIndexOf("."));

            File imageFile = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile;

            newFile = File.createTempFile(nameFile, prefix, imageFile);

            if (createFileFromStream(context, uri, newFile)) {
                return newFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean createFileFromStream(Context context, Uri sourceUri, File destination) {
        try (InputStream ins = context.getContentResolver().openInputStream(sourceUri)) {
            OutputStream os = new FileOutputStream(destination);
            byte[] buffer = new byte[4096];
            int length;

            if (ins != null) {
                while ((length = ins.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();

                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String getGoogleDrivePath(final Context context, final Uri uri) {
        try {

            ToastUtils.showMessageShort(context, context.getString(R.string.loading_from_google_drive));

            @SuppressLint("Recycle")
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            String originalName = (returnCursor.getString(nameIndex));
            String size = (Long.toString(returnCursor.getLong(sizeIndex)));

            if (originalName == null) {
                originalName = context.getString(R.string.prefix_for_google_drive) + DateTimeUtils.currentTimeToNaming();
            }

            File file = new File(DirectoryUtils.getDefaultStorageLocation(), originalName);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();

            return file.getPath();

        } catch (Exception e) {
            Log.e("duynm Exception", e.getMessage());
            return null;
        }
    }

    /**
     * Get a file path from an Uri that points to the Downloads folder.
     *
     * @param context       The context
     * @param uri           The uri to query
     * @param hasSubFolders The flag that indicates if the file is in the root or in a subfolder
     * @return The absolute file path
     */
    private String getDownloadsDocumentPath(Context context, Uri uri, boolean hasSubFolders) {
        String fileName = getFilePath(context, uri);
        String subFolderName = hasSubFolders ? getSubFolders(uri) : "";

        String filePath = "";

        if (fileName != null) {
            if (subFolderName != null)
                filePath = Environment.getExternalStorageDirectory().toString() +
                        "/Download/" + subFolderName + fileName;
            else
                filePath = Environment.getExternalStorageDirectory().toString() +
                        "/Download/" + fileName;
        }

        if (filePath.length() > 0 && FileUtils.checkFileExist(filePath)) {
            return filePath;
        }

        final String id = DocumentsContract.getDocumentId(uri);

        String path = null;
        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:", "");
            }
            List<String> contentUriPrefixesToTry = Arrays.asList("content://downloads/public_downloads",
                    "content://downloads/my_downloads");

            for (String contentUriPrefix: contentUriPrefixesToTry) {
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix), Long.parseLong(id));
                    path = getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    /**
     * Get all the subfolders from an Uri.
     *
     * @param uri The uri
     * @return A string containing all the subfolders that point to the final file path
     */
    private String getSubFolders(Uri uri) {
        String replaceChars = String.valueOf(uri).replace("%2F", "/")
                .replace("%20", " ").replace("%3A", ":");
        // searches for "Download" to get the directory path
        // for example, if the file is inside a folder "test" in the Download folder, this method
        // returns "test/"
        String[] components = replaceChars.split("/");
        String sub5 = "", sub4 = "", sub3 = "", sub2 = "", sub1 = "";

        if (components.length >= 2) {
            sub5 = components[components.length - 2];
        }
        if (components.length >= 3) {
            sub4 = components[components.length - 3];
        }
        if (components.length >= 4) {
            sub3 = components[components.length - 4];
        }
        if (components.length >= 5) {
            sub2 = components[components.length - 5];
        }
        if (components.length >= 6) {
            sub1 = components[components.length - 6];
        }
        if (sub1.equals("Download")) {
            return sub2 + "/" + sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if (sub2.equals("Download")) {
            return sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if (sub3.equals("Download")) {
            return sub4 + "/" + sub5 + "/";
        } else if (sub4.equals("Download")) {
            return sub5 + "/";
        } else {
            return null;
        }
    }

    /**
     * Get the file path (without subfolders if any)
     *
     * @param context The context
     * @param uri     The uri to query
     * @return The file path
     */
    private String getFilePath(Context context, Uri uri) {
        final String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        final String column = "_data";
        final String[] projection = {
                column
        };
        String path = null;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                path = cursor.getString(index);
            }
        } catch (Exception e) {
            Log.e("Error", " " + e.getMessage());
        }
        return path;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * This function is used to check for a google drive file URI.
     *
     * @param uri - input uri
     * @return true, if is google drive uri, otherwise false
     */
    public boolean isGoogleDriveFile(Uri uri) {
        if ("com.google.android.apps.docs.storage".equals(uri.getAuthority()))
            return true;
        return "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    /**
     * This function is used to check for a google photo file URI.
     *
     * @param uri - input uri
     * @return true, if is google photo uri, otherwise false
     */
    public boolean isGooglePhotoFile(Uri uri) {
        if ("com.google.android.apps.photos".equals(uri.getAuthority()))
            return true;
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check
     * @return True if is a raw downloads document, otherwise false
     */
    private boolean isRawDownloadsDocument(Uri uri) {
        String uriToString = String.valueOf(uri);
        return uriToString.contains("com.android.providers.downloads.documents/document/raw");
    }

    private boolean isMediaDocument(Uri uri) {
        String uriToString = String.valueOf(uri);
        return uriToString.contains("com.android.providers.media.documents");
    }
}
