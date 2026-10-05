package com.yallapips.keyboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.*;

public class MainActivity extends Activity {

    private static final String PREFS = "yp_prefs";
    private static final String KEY_URL = "server_url";
    private static final String DEFAULT_URL = "http://178.212.61.215:7777";

    private WebView webView;
    private LinearLayout setupLayout;
    private EditText urlInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView    = findViewById(R.id.webview);
        setupLayout = findViewById(R.id.setup_layout);
        urlInput   = findViewById(R.id.url_input);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_URL, null);

        if (savedUrl != null) {
            loadApp(savedUrl);
        } else {
            showSetup();
        }

        findViewById(R.id.btn_connect).setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (url.isEmpty()) { url = DEFAULT_URL; }
            if (!url.startsWith("http")) { url = "http://" + url; }
            prefs.edit().putString(KEY_URL, url).apply();
            loadApp(url);
        });

        // Long-press the connect button to reset (from web view)
        webView.setOnLongClickListener(v -> false);
    }

    private void showSetup() {
        setupLayout.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        urlInput.setText(DEFAULT_URL);
    }

    private void loadApp(String url) {
        setupLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void resetServer() {
                runOnUiThread(() -> {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_URL).apply();
                    showSetup();
                });
            }
        }, "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String url2) {
                view.loadData(
                    "<html><body style='background:#111;color:#fff;font-family:sans-serif;text-align:center;padding-top:40%'>" +
                    "<h2>⚠️ Cannot reach server</h2>" +
                    "<p>" + url + "</p>" +
                    "<p>Make sure your VPS is running<br>and you are connected to the internet.</p>" +
                    "<br><button onclick='Android.resetServer()' style='padding:12px 24px;font-size:16px'>Change Server URL</button>" +
                    "</body></html>",
                    "text/html", "utf-8"
                );
            }
        });

        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
