package com.ch33m5.webforcenow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.app.NotificationCompat;

public class KeepAliveService extends Service {

    private static final String TAG = "WebforceNow";
    private static final String CHANNEL_ID = "webforce_media_playback";
    private static final int NOTIFICATION_ID = 1;

    private static KeepAliveService sInstance;
    private PowerManager.WakeLock wakeLock;
    private WindowManager windowManager;
    private FrameLayout overlayContainer;
    private WindowManager.LayoutParams overlayParams;

    public static KeepAliveService getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "KeepAliveService onCreate");
        sInstance = this;
        createNotificationChannel();
        acquireWakeLock();
        startForeground(NOTIFICATION_ID, buildNotification());
        addOverlay();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "KeepAliveService onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "KeepAliveService onDestroy");
        removeOverlay();
        releaseWakeLock();
        sInstance = null;
        super.onDestroy();
    }

    private void addOverlay() {
        if (overlayContainer != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            Log.w(TAG, "No overlay permission, skip overlay");
            return;
        }

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            int type;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            int size = dpToPx(1);

            overlayParams = new WindowManager.LayoutParams(
                    size,
                    size,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
            overlayParams.gravity = Gravity.TOP | Gravity.START;

            overlayContainer = new FrameLayout(this);
            windowManager.addView(overlayContainer, overlayParams);
            Log.d(TAG, "Overlay added (1dp, non-touchable)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to add overlay", e);
            overlayContainer = null;
        }
    }

    private void removeOverlay() {
        if (overlayContainer != null && windowManager != null) {
            try {
                windowManager.removeView(overlayContainer);
                Log.d(TAG, "Overlay removed");
            } catch (Exception e) {
                Log.e(TAG, "Failed to remove overlay", e);
            }
            overlayContainer = null;
        }
    }

    public void moveToOverlay(View webView) {
        if (overlayContainer == null || webView == null) return;

        try {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }

            FrameLayout.LayoutParams childLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            webView.setLayoutParams(childLp);
            overlayContainer.addView(webView);
            Log.d(TAG, "WebView moved to overlay (invisible, alive)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to move WebView to overlay", e);
        }
    }

    public View removeFromOverlay() {
        if (overlayContainer == null || overlayContainer.getChildCount() == 0) return null;

        try {
            View webView = overlayContainer.getChildAt(0);
            overlayContainer.removeView(webView);
            Log.d(TAG, "WebView removed from overlay");
            return webView;
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove WebView from overlay", e);
            return null;
        }
    }

    public boolean hasWebView() {
        return overlayContainer != null && overlayContainer.getChildCount() > 0;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebforceNow:keepalive");
            wakeLock.acquire();
            Log.d(TAG, "WakeLock acquired");
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
            Log.d(TAG, "WakeLock released");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.media_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.media_channel_description));
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    private Notification buildNotification() {
        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.media_notification_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }
}
