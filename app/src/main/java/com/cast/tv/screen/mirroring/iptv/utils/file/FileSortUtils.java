package com.cast.tv.screen.mirroring.iptv.utils.file;

import com.cast.tv.screen.mirroring.iptv.data.model.FileData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils.*;

public class FileSortUtils {

    // Sorting order constants

    private FileSortUtils() {}

    public static void performSortOperation(int option, List<FileData> listFile) {
        switch (option) {
            case SORT_BY_DATE:
                sortFilesByDateNewestToOldest(listFile);
                break;
            case SORT_BY_NAME:
                sortByNameAlphabetical(listFile);
                break;
            case SORT_BY_SIZE:
                sortFilesBySizeIncreasingOrder(listFile);
                break;
            case SORT_BY_SIZE_REVERT:
                sortFilesBySizeDecreasingOrder(listFile);
                break;
            case SORT_BY_TYPE:
                sortFilesByTypeOrder(listFile);
                break;
        }
    }

    // SORTING FUNCTIONS

    private static void sortByNameAlphabetical(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> file.getDisplayName().compareToIgnoreCase(file2.getDisplayName()));
    }

    private static void sortFilesByDateNewestToOldest(List<FileData> filesList) {
        Collections.sort(filesList, (file, file2) -> Long.compare(file2.getDateAdded(), file.getDateAdded()));
    }

    private static void sortFilesBySizeIncreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file1.getSize(), file2.getSize()));
    }

    private static void sortFilesBySizeDecreasingOrder(List<FileData> filesList) {
        Collections.sort(filesList, (file1, file2) -> Long.compare(file2.getSize(), file1.getSize()));
    }

    private static void sortFilesByTypeOrder(List<FileData> filesList) {
        Comparator<FileData> sortByType = (file1, file2) -> {
            int compareType = file1.getFileType().compareToIgnoreCase(file2.getFileType());

            if (compareType != 0) {
                return compareType;
            }

            return Long.compare(file2.getDateAdded(), file1.getDateAdded());
        };

        Collections.sort(filesList, sortByType);
    }
}
