package com.cast.tv.screen.mirroring.iptv.utils.chromecast;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.mediarouter.app.MediaRouteChooserDialog;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.util.ArrayList;
import java.util.List;

public class ChromecastConnection {

    private final Activity mActivity;

    private SessionListener mNewConnectionListener;

    private final CastStateUpdateListener mCastStateUpdateListener;

    private String mAppId;

    public ChromecastConnection(Activity activity, CastStateUpdateListener connectionCastStateUpdateListener) {
        this.mActivity = activity;
        this.mCastStateUpdateListener = connectionCastStateUpdateListener;

        // This is the first call to getContext which will start up the
        // CastContext and prep it for searching for a session to rejoin
        // Also adds the receiver update callback
        getContext().addCastStateListener(mCastStateUpdateListener);
        mCastStateUpdateListener.onCastStateChanged(getContext().getCastState());
    }

    public void initialize(String applicationId) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                // If the app Id changed
                if (applicationId == null || !applicationId.equals(mAppId)) {
                    if (isValidAppId(applicationId)) {
                        setAppId(applicationId);
                    } else {
                        return;
                    }
                }

                // Check if there is any available receivers for 5 seconds
                startRouteScan(5000L, new ScanCallback() {
                    @Override
                    void onRouteUpdate(List<MediaRouter.RouteInfo> routes) {
                        if (mCastStateUpdateListener != null) {
                            mCastStateUpdateListener.onCastStateChanged(getContext().getCastState());
                        }

                        if (getContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
                            stopRouteScan(this, null);
                            if (mCastStateUpdateListener != null) {
                                mCastStateUpdateListener.onReceiverAvailableUpdate(true);
                            }
                            CastSession session = getSessionManager().getCurrentCastSession();
                            if (session != null) {
                                // TODO Let the client know
                            }
                        }
                    }
                }, null);
            }
        });
    }

    public MediaRouter getMediaRouter() {
        return MediaRouter.getInstance(mActivity);
    }

    public CastContext getContext() {
        return CastContext.getSharedInstance(mActivity);
    }

    public SessionManager getSessionManager() {
        if (getContext() == null) return null;
        return getContext().getSessionManager();
    }

    public CastSession getSession() {
        if (getSessionManager() == null) return null;
        return getSessionManager().getCurrentCastSession();
    }

    public void setAppId(String applicationId) {
        this.mAppId = applicationId;
        getContext().setReceiverApplicationId(mAppId);
    }

    private boolean isValidAppId(String applicationId) {
        try {
            ScanCallback scanCallback = new ScanCallback() {
                @Override
                void onRouteUpdate(List<MediaRouter.RouteInfo> routes) {
                }
            };
            getMediaRouter().addCallback(new MediaRouteSelector.Builder()
                            .addControlCategory(CastMediaControlIntent.categoryForCast(applicationId))
                            .build(),
                    scanCallback,
                    MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
            getMediaRouter().removeCallback(scanCallback);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void stopMediaIfPlaying() {
        try {
            if (isChromeCastConnect() && getSession().getRemoteMediaClient() != null && getSession().getRemoteMediaClient().isPlaying()) {
                getSession().getRemoteMediaClient().stop();
            }
        } catch (Exception ignored) {}
    }

    public boolean isPlayingSomething() {
        return isChromeCastConnect() && getSession().getRemoteMediaClient() != null &&
                (getSession().getRemoteMediaClient().isPlaying() || getSession().getRemoteMediaClient().isPaused() || getSession().getRemoteMediaClient().isBuffering());
    }

    /**
     * This will create a new session or seamlessly selectRoute an existing one if we created it.
     *
     * @param routeId  the id of the route to selectRoute
     * @param callback calls callback.onJoin when we have joined a session,
     *                 or callback.onError if an error occurred
     */
    public void selectRoute(final String routeId, SelectRouteCallback callback) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (getSession() != null && getSession().isConnected()) {
                    return;
                }

                // We need this hack so that we can access these values in callbacks without having
                // to store it as a global variable, just always access first element
                final boolean[] foundRoute = {false};
                final int[] retries = {0};

                // We need to start an active scan because getMediaRouter().getRoutes() may be out
                // of date.  Also, maintaining a list of known routes doesn't work.  It is possible
                // to have a route in your "known" routes list, but is not in
                // getMediaRouter().getRoutes() which will result in "Ignoring attempt to select
                // removed route: ", even if that route *should* be available.  This state could
                // happen because routes are periodically "removed" and "added", and if the last
                // time chromecastSession router was scanning ended when the route was temporarily removed the
                // getRoutes() fn will have no record of the route.  We need the active scan to
                // avoid this situation as well.  PS. Just running the scan non-stop is a poor idea
                // since it will drain battery power quickly.
                ScanCallback scan = new ScanCallback() {
                    @Override
                    void onRouteUpdate(List<MediaRouter.RouteInfo> routes) {
                        // Look for the matching route
                        for (MediaRouter.RouteInfo route : routes) {
                            if (!foundRoute[0] && route.getId().equals(routeId)) {
                                foundRoute[0] = true;
                                try {
                                    getMediaRouter().selectRoute(route);
                                } catch (NullPointerException e) {
                                    foundRoute[0] = false;
                                }
                            }
                        }
                    }
                };

                Runnable retry = () -> {
                    // Reset foundRoute
                    foundRoute[0] = false;
                    // Feed current routes into scan so that it can retry.
                    // If route is there, it will try to join,
                    // if not, it should wait for the scan to find the route
                    scan.onRouteUpdate(getMediaRouter().getRoutes());
                };

                listenForConnection(new ConnectionCallback() {
                    @Override
                    public void onJoinedSuccess() {
                        stopRouteScan(scan, null);
                        callback.onSuccess();
                    }

                    @Override
                    public boolean onSessionStartFailed(int errorCode) {
                        if (errorCode == 7 || errorCode == 15) {
                            // It network or timeout error retry
                            retry.run();
                            return false;
                        } else {
                            stopRouteScan(scan, null);
                            callback.onError("onSessionStartFailed");
                            return true;
                        }
                    }

                    @Override
                    public boolean onSessionEndedBeforeStart(int errorCode) {
                        if (retries[0] < 10) {
                            retries[0]++;
                            retry.run();
                            return false;
                        } else {
                            stopRouteScan(scan, null);
                            callback.onError("onSessionEndedBeforeStart");
                            return true;
                        }
                    }
                });

                startRouteScan(15000L, scan, () -> {
                    stopRouteScan(scan, null);
                    callback.onError("timeout");
                });
            }
        });
    }

    public void requestStartSession(RequestSessionCallback callback) {
        mActivity.runOnUiThread(() -> {
            CastSession session = getSession();
            if (session == null) {
                try {
                    listenForConnection(callback);

                    MediaRouteChooserDialog builder = new MediaRouteChooserDialog(mActivity, androidx.appcompat.R.style.Theme_AppCompat_DayNight);
                    builder.setRouteSelector(new MediaRouteSelector.Builder()
                            .addControlCategory(CastMediaControlIntent.categoryForCast(mAppId))
                            .build());
                    builder.setCanceledOnTouchOutside(true);
                    builder.setOnCancelListener(dialog -> {
                        getSessionManager().removeSessionManagerListener(mNewConnectionListener, CastSession.class);
                        callback.onCancel();
                    });
                    builder.show();
                    callback.onDialogShow();
                } catch (Exception e) {
                    callback.onDialogCanNotShow();
                }

            } else {
                callback.onDialogCanNotShow();
            }
        });
    }

    public void requestEndSession(RequestEndSessionCallback callback) {
        mActivity.runOnUiThread(() -> {
            CastSession session = getSession();
            if (session != null) {
                // We are are already connected, so show the "connection options" Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                if (session.getCastDevice() != null) {
                    builder.setTitle(session.getCastDevice().getFriendlyName());
                }
                builder.setOnDismissListener(dialog -> callback.onCancel());
                builder.setPositiveButton("Stop Casting", (dialog, which) -> {
                    endSession();
                    callback.onSuccess();
                });
                builder.show();
            }
        });
    }

    public boolean isChromeCastConnect() {
        CastSession session = getSession();
        return session != null && session.isConnected();
    }

    private void listenForConnection(ConnectionCallback callback) {
        SessionManager sessionManager = getSessionManager();
        if (sessionManager == null) return;

        sessionManager.removeSessionManagerListener(mNewConnectionListener, CastSession.class);
        mNewConnectionListener = new SessionListener() {
            @Override
            public void onSessionStarted(CastSession castSession, String sessionId) {
                sessionManager.removeSessionManagerListener(this, CastSession.class);
                callback.onJoinedSuccess();
            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int errCode) {
                if (callback.onSessionStartFailed(errCode)) {
                    sessionManager.removeSessionManagerListener(this, CastSession.class);
                }
            }

            @Override
            public void onSessionEnded(CastSession castSession, int errCode) {
                if (callback.onSessionEndedBeforeStart(errCode)) {
                    sessionManager.removeSessionManagerListener(this, CastSession.class);
                }
            }
        };
        sessionManager.addSessionManagerListener(mNewConnectionListener, CastSession.class);
    }

    public void startRouteScan(Long timeout, ScanCallback callback, Runnable onTimeout) {
        mActivity.runOnUiThread(() -> {
            callback.setMediaRouter(getMediaRouter());

            if (timeout != null && timeout == 0) {
                callback.onFilteredRouteUpdate();
                return;
            }

            getMediaRouter().addCallback(new MediaRouteSelector.Builder()
                            .addControlCategory(CastMediaControlIntent.categoryForCast(mAppId))
                            .build(),
                    callback,
                    MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

            callback.onFilteredRouteUpdate();

            if (timeout != null) {
                new Handler().postDelayed(() -> {
                    getMediaRouter().removeCallback(callback);
                    // Notify
                    if (onTimeout != null) {
                        onTimeout.run();
                    }
                }, timeout);
            }
        });
    }

    public void stopRouteScan(ScanCallback callback, Runnable completionCallback) {
        if (callback == null) {
            completionCallback.run();
            return;
        }
        mActivity.runOnUiThread(() -> {
            callback.stop();
            getMediaRouter().removeCallback(callback);
            if (completionCallback != null) {
                completionCallback.run();
            }
        });
    }

    public void endSession() {
        try {
            mActivity.runOnUiThread(() -> getSessionManager().getCurrentCastSession().getRemoteMediaClient().stop());
            mActivity.runOnUiThread(() -> getSessionManager().endCurrentSession(true));

        } catch (Exception ignored) {}
    }

    private static class SessionListener implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting(CastSession castSession) {
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {
        }
    }

    public interface SelectRouteCallback {
        void onSuccess();
        void onError(String message);
    }

    public abstract static class RequestSessionCallback implements ConnectionCallback {
        public abstract void onError(int errorCode);

        public abstract void onCancel();

        public abstract void onDialogShow();

        public abstract void onDialogCanNotShow();

        @Override
        public final boolean onSessionEndedBeforeStart(int errorCode) {
            onSessionStartFailed(errorCode);
            return true;
        }

        @Override
        public final boolean onSessionStartFailed(int errorCode) {
            onError(errorCode);
            return true;
        }
    }

    public abstract static class RequestEndSessionCallback {
        public abstract void onSuccess();

        public abstract void onCancel();
    }

    public interface ConnectionCallback {
        void onJoinedSuccess();
        boolean onSessionStartFailed(int errorCode);
        boolean onSessionEndedBeforeStart(int errorCode);
    }

    public abstract static class ScanCallback extends MediaRouter.Callback {
        abstract void onRouteUpdate(List<MediaRouter.RouteInfo> routes);

        private boolean stopped = false;

        private MediaRouter mediaRouter;

        void setMediaRouter(MediaRouter router) {
            this.mediaRouter = router;
        }

        void stop() {
            stopped = true;
        }

        private void onFilteredRouteUpdate() {
            if (stopped || mediaRouter == null) {
                return;
            }
            List<MediaRouter.RouteInfo> outRoutes = new ArrayList<>();
            // Filter the routes
            for (MediaRouter.RouteInfo route : mediaRouter.getRoutes()) {
                Bundle extras = route.getExtras();
                if (extras != null) {
                    CastDevice.getFromBundle(extras);
                    if (extras.getString("com.google.android.gms.cast.EXTRA_SESSION_ID") != null) {
                        continue;
                    }
                }
                if (!route.isDefault()
                        && !route.getDescription().equals("Google Cast Multizone Member")
                        && route.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE
                ) {
                    outRoutes.add(route);
                }
            }
            onRouteUpdate(outRoutes);
        }

        @Override
        public final void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }

        @Override
        public final void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }

        @Override
        public final void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }
    }

    public abstract static class CastStateUpdateListener implements CastStateListener {
        private ImageView mCastIcon;
        private Drawable mCastedIcon;
        private Drawable mNotCastedIcon;
        private Drawable mNotAvailableIcon;
        public abstract void onReceiverAvailableUpdate(boolean available);

        public void setCastDrawable(Drawable castedIcon, Drawable notCastedIcon, Drawable notAvailableIcon) {
            mCastedIcon = castedIcon;
            mNotCastedIcon = notCastedIcon;
            mNotAvailableIcon = notAvailableIcon;
        }

        public void setCastIcon(ImageView castIcon) {
            mCastIcon = castIcon;
        }

        @Override
        public void onCastStateChanged(int state) {
            onReceiverAvailableUpdate(state != CastState.NO_DEVICES_AVAILABLE);

            if (mCastIcon != null) {
                if (state == CastState.CONNECTED) {
                    mCastIcon.setImageDrawable(mCastedIcon);
                } else if (state == CastState.NOT_CONNECTED || state == CastState.CONNECTING) {
                    mCastIcon.setImageDrawable(mNotCastedIcon);
                } else {
                    mCastIcon.setImageDrawable(mNotAvailableIcon);
                }
            }
        }
    }
}