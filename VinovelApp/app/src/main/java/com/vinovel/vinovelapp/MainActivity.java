package com.vinovel.vinovelapp;

import com.vinovel.vinovelapp.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends Activity {

    private static String LOG_TAG = "vnv";
    private static String DEFAULT_PAGE_URL = "http://www.vinovel.com/";
    private static String ID_LAST_URL = "VNV_LAST_URL";

    private WebView webview;
    private LinearLayout llSplash;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        llSplash = (LinearLayout)findViewById(R.id.splash_logo);
        webview = (WebView) findViewById(R.id.fullscreen_content);

        runnable = new Runnable() {
            @Override
            public void run() {
                llSplash.setVisibility(View.GONE);
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, 8000);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        webview.setWebViewClient(new WebViewClientClass());
        webview.addJavascriptInterface(new WebAppInterface(this), "vnv_js_api");

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String lastURL = pref.getString(ID_LAST_URL, DEFAULT_PAGE_URL);
        Log.i(LOG_TAG, "start url = " + lastURL);
        webview.loadUrl(lastURL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentURL = webview.getUrl();
        Log.i(LOG_TAG, "onPause!! " + currentURL);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        pref.edit().putString(ID_LAST_URL, currentURL).commit();

        if(currentURL.contains("episode")){
            if (webview.canGoBack()) {
                webview.goBack();
            }else{
                webview.loadUrl(DEFAULT_PAGE_URL);
            }
        }
        webview.onPause();

        // TODO : method 1, change to blank page to mute every thing
        //webview.loadUrl("about:blank");

        // TODO : method 2, change system volume to mute.
        /*
        AudioManager audioManager;
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        */

        // TODO : method 3, call javascript
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    public void onBackPressed() {

        //Log.i(LOG_TAG, "url = " + webview.getUrl());

        if(0 == DEFAULT_PAGE_URL.compareTo(webview.getUrl())) {
            super.onBackPressed();
            return;
        }

        if (webview.canGoBack()) {
            webview.goBack();
            return;
        }

        webview.loadUrl(DEFAULT_PAGE_URL);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO : limit webview's content to show only vinovel.com
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            llSplash.setVisibility(View.GONE);
        }
    }

    private class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void setPortraitLayout() {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        @JavascriptInterface
        public void setLandscapeLayout() {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}
