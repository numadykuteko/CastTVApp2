package com.cast.tv.screen.mirroring.iptv.data.remote;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ApiHelper implements ApiHelperInterface {
    private static ApiHelper mInstance;
    private ApiHelper() {
    }
    public static ApiHelper getInstance() {
        if (mInstance == null) {
            return new ApiHelper();
        }
        return mInstance;
    }
    private OkHttpClient getOkHttpRequest() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(DataConstants.CONNECT_TIMEOUT_NETWORK, TimeUnit.SECONDS)
                .readTimeout(DataConstants.CONNECT_TIMEOUT_NETWORK, TimeUnit.SECONDS)
                .writeTimeout(DataConstants.CONNECT_TIMEOUT_NETWORK, TimeUnit.SECONDS)
                .build();
    }
}
