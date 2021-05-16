package com.cast.tv.screen.mirroring.iptv.utils.chromecast;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.Nullable;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.webserver.SimpleWebServer;

public class MediaWebService extends IntentService {
    private String mDeviceIpAddress;
    private PendingIntent mPendingIntent;

    public MediaWebService() {
        super("MediaWebService");
    }

    public MediaWebService(String name) {
        super(name);
    }

    public void setDeviceIpAddress(String ipAddress) {
        mDeviceIpAddress = ipAddress;
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        SimpleWebServer.stopServer();
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            mPendingIntent = intent.getParcelableExtra(DataConstants.PENDING_INTENT_SERVICE);
        } else {
            sendErrorMessage();
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            onDestroy();
            return;
        }
        if (intent.getExtras() == null || intent.getExtras().getString(DataConstants.IP_LINK_KEY) == null) {
            onDestroy();
            return;
        }

        setDeviceIpAddress(intent.getExtras().getString(DataConstants.IP_LINK_KEY));

        try {
            /**
             * Running a server on Internal storage.
             */

            String[] options = {"-h",
                    mDeviceIpAddress,
                    "-p 8080",
                    "-d",
                    Environment.getExternalStorageDirectory().getAbsolutePath()};

            SimpleWebServer.runServer(options, new SimpleWebServer.ConnectionStatusListener() {
                @Override
                public void onConnected() {
                    DataManager.getInstance(getApplicationContext()).saveLastIPAddress(mDeviceIpAddress);
                    sendSuccessMessage();
                }

                @Override
                public void onAlreadyConnected() {
                    DataManager.getInstance(getApplicationContext()).saveLastIPAddress(mDeviceIpAddress);
                    sendSuccessMessage();
                }

                @Override
                public void onError() {
                    sendErrorMessage();
                    stopSelf();
                }
            });

        } catch (Exception e) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        SimpleWebServer.stopServer();
        sendDestroyMessage();
        super.onDestroy();
    }

    private void sendSuccessMessage() {
        Intent result = new Intent();
        try {
            mPendingIntent.send(MediaWebService.this, DataConstants.CONNECT_SUCCESS_MESSAGE, result);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage() {
        Intent result = new Intent();
        try {
            mPendingIntent.send(MediaWebService.this, DataConstants.CONNECT_ERROR_MESSAGE, result);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void sendDestroyMessage() {
        Intent result = new Intent();
        try {
            mPendingIntent.send(MediaWebService.this, DataConstants.CONNECT_DESTROY_MESSAGE, result);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
