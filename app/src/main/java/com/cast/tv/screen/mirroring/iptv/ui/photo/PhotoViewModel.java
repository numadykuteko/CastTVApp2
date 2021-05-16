package com.cast.tv.screen.mirroring.iptv.ui.photo;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseViewModel;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewModel extends BaseViewModel<PhotoNavigator> {
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();
    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public PhotoViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFileList(int order) {
        AsyncTask.execute(() -> {
            ArrayList<FileData> allData = FileUtils.getAllExternalFileList(getApplication(), DataConstants.FILE_TYPE_PHOTO, order);
            if (allData != null && allData.size() > 0) {
                getCompositeDisposable().add(getDataManager()
                        .getListBookmarkByType(DataConstants.FILE_TYPE_PHOTO)
                        .subscribeOn(getSchedulerProvider().io())
                        .observeOn(getSchedulerProvider().ui())
                        .subscribe(response -> {
                            if (response != null && response.size() > 0) {
                                for (BookmarkData bookmarkData: response) {
                                    if (bookmarkData.getFilePath() != null) {
                                        for(int i = 0; i < allData.size(); i++) {
                                            if (allData.get(i).getFilePath().equals(bookmarkData.getFilePath())) {
                                                allData.get(i).setBookmarked(true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            mListFileLiveData.postValue(allData);
                        }, throwable -> {
                            mListFileLiveData.postValue(allData);
                        })
                );
            } else {
                mListFileLiveData.postValue(allData);
            }
        });
    }

    public void revertBookmark(FileData fileData) {
        if (fileData.isBookmarked()) {
            getCompositeDisposable().add(getDataManager()
                    .clearBookmarkByPath(fileData.getFilePath())
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(response -> {

                    }, throwable -> {
                    })
            );
        } else {
            BookmarkData bookmarkData = new BookmarkData(fileData);
            getCompositeDisposable().add(getDataManager()
                    .saveBookmark(bookmarkData)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(response -> {

                    }, throwable -> {
                    })
            );
        }
    }
}
