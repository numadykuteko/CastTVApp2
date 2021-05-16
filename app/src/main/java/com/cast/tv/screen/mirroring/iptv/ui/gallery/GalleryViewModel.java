package com.cast.tv.screen.mirroring.iptv.ui.gallery;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseViewModel;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewModel extends BaseViewModel<GalleryNavigator> {
    private FileData mCurrentPath;
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();

    public GalleryViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCurrentPath(FileData fileData) {
        this.mCurrentPath = fileData;
    }

    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public void getFileList() {
        AsyncTask.execute(() -> {
            ArrayList<FileData> allData = FileUtils.getFileListOfDirectory(mCurrentPath);
            mListFileLiveData.postValue(allData);
        });
    }
}
