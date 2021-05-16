package com.cast.tv.screen.mirroring.iptv.ui.bookmark;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkViewModel extends BaseViewModel<BookmarkNavigator> {
    private int mTypeSearch;
    private List<String> mTypeList = Arrays.asList(DataConstants.FILE_TYPE_VIDEO, DataConstants.FILE_TYPE_PHOTO, DataConstants.FILE_TYPE_AUDIO);

    private String mType;
    private ArrayList<BookmarkData> mListFile = new ArrayList<>();
    private MutableLiveData<List<BookmarkData>> mListFileLiveData = new MutableLiveData<>();

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
    }

    public void setTypeSearch(int typeSearch) {
        mTypeSearch = typeSearch;
        mType = mTypeList.get(mTypeSearch);
    }

    public MutableLiveData<List<BookmarkData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public void startSeeding() {
        getCompositeDisposable().add(getDataManager()
                .getListBookmarkByType(mType)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.size() > 0) {
                        mListFileLiveData.postValue(response);
                    } else {
                        mListFileLiveData.postValue(new ArrayList<>());
                    }
                }, throwable -> {
                    mListFileLiveData.postValue(new ArrayList<>());
                })
        );
    }

    public void deleteData(BookmarkData bookmarkData) {
        getCompositeDisposable().add(getDataManager()
                .clearBookmarkByPath(bookmarkData.getFilePath())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void deleteAllData() {
        getCompositeDisposable().add(getDataManager()
                .clearAllBookmark()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }
}
