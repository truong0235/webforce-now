package com.ch33m5.webforcenow;

import android.os.SystemClock;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import android.view.MotionEvent;
import android.view.KeyEvent;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class JsBridge {

    private static final String TAG = "WebforceNow";
    private WebView webView;
    private Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setWebView(WebView webView) {
        this.webView = webView;
        this.context = webView.getContext();
    }

    public void clearWebView() {
        this.webView = null;
    }

    @JavascriptInterface
    public void handleMessage(String method, String data) {
        Log.d(TAG, "[JsBridge] JS -> Java: " + method + " data=" + data);
        // Handle messages from JS here
        // Example: route by method name
        switch (method) {
            case "log":
                Log.d(TAG, "[JS] " + data);
                break;
            default:
                Log.w(TAG, "[JsBridge] Unknown method: " + method);
                break;
        }
    }


    @JavascriptInterface
    public void playSound() {
        if (context == null) return;
        try {
            AssetFileDescriptor afd = context.getAssets().openFd("audio/nokia-ringtone.mp3");
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        } catch (Exception e) {
            Log.e(TAG, "playSound failed", e);
        }
    }



    @JavascriptInterface
    public void log(String message) {
        Log.d(TAG, "[JS] " + message);
    }

    public void callJs(String js) {
        callJs(js, null);
    }

    public void callJs(String js, ValueCallback<String> callback) {
        WebView wv = webView;
        if (wv == null) {
            Log.w(TAG, "[JsBridge] WebView is null, cannot call JS");
            return;
        }
        mainHandler.post(() -> wv.evaluateJavascript(js, callback));
    }

    @JavascriptInterface
    public void click(float x, float y) {
        if (this.webView == null) {
            Log.w(TAG, "[JsBridge] WebView is null, cannot click");
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            WebView wv = this.webView;
            if (wv == null) return;
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
            MotionEvent up = MotionEvent.obtain(now, now + 100, MotionEvent.ACTION_UP, x, y, 0);
            wv.dispatchTouchEvent(down);
            wv.dispatchTouchEvent(up);
            down.recycle();
            up.recycle();
            Log.d(TAG, "[JsBridge] Clicked at (" + x + ", " + y + ")");
        });
    }

    @JavascriptInterface
    public void click() {
        click(10, 10);
    }

    @JavascriptInterface
    public void keyPress() {
        if (this.webView == null) {
            Log.w(TAG, "[JsBridge] WebView not found, cant send key event");
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            WebView wv = this.webView;
            wv.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_16));
            wv.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_16));
            Log.d(TAG, "[JsBridge] key sent");
        });
    }

    public String loadAsset(String path, android.content.Context context) {
        try (InputStream is = context.getAssets().open(path)) {
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int len;
            while ((len = is.read(tmp)) != -1) {
                buf.write(tmp, 0, len);
            }
            return buf.toString("UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load asset: " + path, e);
            return "";
        }
    }

    public void injectAssets(android.content.Context context, String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            sb.append(loadAsset(path, context));
            sb.append(";");
        }
        String js = sb.toString();
        if (!js.isEmpty()) {
            callJs(js);
        }
    }
}
