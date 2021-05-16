package com.cast.tv.screen.mirroring.iptv.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryViewModel extends BaseViewModel<HistoryNavigator> {
    private int mTypeSearch;
    private List<String> mTypeList = Arrays.asList(DataConstants.FILE_TYPE_VIDEO, DataConstants.FILE_TYPE_PHOTO, DataConstants.FILE_TYPE_AUDIO);

    private String mType;
    private ArrayList<HistoryData> mListFile = new ArrayList<>();
    private MutableLiveData<List<HistoryData>> mListFileLiveData = new MutableLiveData<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
    }

    public void setTypeSearch(int typeSearch) {
        mTypeSearch = typeSearch;
        mType = mTypeList.get(mTypeSearch);
    }

    public MutableLiveData<List<HistoryData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public void startSeeding() {
        getCompositeDisposable().add(getDataManager()
                .getListHistoryByType(mType)
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

    public void deleteData(HistoryData HistoryData) {
        getCompositeDisposable().add(getDataManager()
                .clearHistory(HistoryData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void deleteAllData() {
        getCompositeDisposable().add(getDataManager()
                .clearAllHistory()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }
}

