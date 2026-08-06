package com.ch33m5.webforcenow;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WebforceNow";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private WebView myWebView;
    private boolean isInBackground;
    private JsBridge jsBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        startKeepAliveService();

        jsBridge = new JsBridge();

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        myWebView.addJavascriptInterface(jsBridge, "AndroidBridge");
        jsBridge.setWebView(myWebView);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished: " + url);
                jsBridge.injectAssets(MainActivity.this,
                    "js/disableFocus.js",
                    "js/init.js",
                    "js/detectIdle.js"
                );
            }
        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setOffscreenPreRaster(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        myWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        myWebView.loadUrl("https://play.geforcenow.com/mall/#/layout/games");
    }
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack())
            myWebView.goBack();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isInBackground) {
            isInBackground = false;
            moveWebViewBackToActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations()) {
            isInBackground = true;
            moveWebViewToOverlay();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MainActivity onDestroy");
        KeepAliveService svc = KeepAliveService.getInstance();
        if (svc != null) {
            View wv = svc.removeFromOverlay();
            if (wv != null && wv instanceof WebView) {
                ((WebView) wv).destroy();
            }
        }
        stopService(new Intent(this, KeepAliveService.class));
        if (myWebView != null) {
            myWebView.destroy();
            myWebView = null;
        }
        if (jsBridge != null) {
            jsBridge.clearWebView();
        }
        super.onDestroy();
    }

    private void moveWebViewToOverlay() {
        KeepAliveService svc = KeepAliveService.getInstance();
        if (svc == null || myWebView == null) return;

        try {
            ViewGroup parent = (ViewGroup) myWebView.getParent();
            if (parent != null) {
                parent.removeView(myWebView);
            }
            myWebView.onResume();
            svc.moveToOverlay(myWebView);
        } catch (Exception e) {
            Log.e(TAG, "Failed to move WebView to overlay", e);
        }
    }

    private void moveWebViewBackToActivity() {
        KeepAliveService svc = KeepAliveService.getInstance();
        if (svc == null || myWebView == null) return;

        try {
            View webView = svc.removeFromOverlay();
            if (webView != null) {
                ViewGroup container = findViewById(R.id.webview_container);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                container.addView(webView, lp);
                Log.d(TAG, "WebView moved back to activity");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to move WebView back to activity", e);
        }
    }

    private void startKeepAliveService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
                return;
            }
        }
        launchKeepAliveService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.w(TAG, "Notification permission denied");
            }
            launchKeepAliveService();
        }
    }

    private void launchKeepAliveService() {
        Log.d(TAG, "Starting KeepAliveService");
        Intent serviceIntent = new Intent(this, KeepAliveService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
