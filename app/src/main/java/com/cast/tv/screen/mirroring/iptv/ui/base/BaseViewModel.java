package com.cast.tv.screen.mirroring.iptv.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageData;
import com.cast.tv.screen.mirroring.iptv.data.model.SavedData;
import com.cast.tv.screen.mirroring.iptv.utils.scheduler.SchedulerProvider;
import com.cast.tv.screen.mirroring.iptv.utils.scheduler.SchedulerProviderInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseViewModel<N> extends AndroidViewModel {

    private DataManager mDataManager;
    private CompositeDisposable mCompositeDisposable;
    private WeakReference<N> mNavigator;
    private ObservableBoolean mIsLoading = new ObservableBoolean();
    private SchedulerProviderInterface mSchedulerProviderInterface;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        mCompositeDisposable = new CompositeDisposable();
        mSchedulerProviderInterface = new SchedulerProvider();

        mDataManager = DataManager.getInstance(application);
    }

    @Override
    protected void onCleared() {
        mCompositeDisposable.dispose();
        super.onCleared();
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public SchedulerProviderInterface getSchedulerProvider() {
        return mSchedulerProviderInterface;
    }

    public void setDataManager(DataManager appDataManager) {
        this.mDataManager = appDataManager;
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    public N getNavigator() {
        return mNavigator.get();
    }

    public void setNavigator(N navigator) {
        this.mNavigator = new WeakReference<>(navigator);
    }

    public ObservableBoolean getIsLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading.set(isLoading);
    }

    public void saveHistory(FileData fileData) {
        if (fileData == null) return;

        getCompositeDisposable().add(getDataManager()
                .saveHistory(new HistoryData(fileData))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {
                })
        );
    }

    public List<ImageData> getImageListFromSavedData(List<? extends SavedData> dataList) {
        if (dataList == null) return new ArrayList<>();
        List<ImageData> result = new ArrayList<>();

        for (SavedData savedData : dataList) {
            if (savedData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                result.add(new ImageData(savedData.getDisplayName(), savedData.getFilePath()));
            }
        }

        return result;
    }

    public List<ImageData> getImageListFromFileData(List<FileData> dataList) {
        if (dataList == null) return new ArrayList<>();

        List<ImageData> result = new ArrayList<>();

        for (FileData fileData : dataList) {
            if (fileData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                result.add(new ImageData(fileData.getDisplayName(), fileData.getFilePath()));
            }
        }

        return result;
    }
}
