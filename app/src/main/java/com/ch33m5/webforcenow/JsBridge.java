package com.ch33m5.webforcenow;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsBridge {

    private static final String TAG = "WebforceNow";
    private WebView webView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setWebView(WebView webView) {
        this.webView = webView;
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
