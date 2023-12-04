package com.rabbitlbj.webapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends AppCompatActivity {

    private static final String JSON_URL = "http://192.168.11.236:20000/files/index.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);

        // 获取 WindowManager
        WindowManager windowManager = getWindowManager();

        // 获取 DisplayMetrics 对象
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        // 获取屏幕宽度和高度
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 输出屏幕宽度和高度
        Log.d("Screen Size", "Width: " + screenWidth + " Height: " + screenHeight);

        // 执行异步任务来处理 HTTP 请求和 JSON 解析
        // 使用多线程处理 HTTP 请求和 JSON 解析
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 在新线程中执行网络请求
                String imageUrl = "";
                String jsonResult = performHttpRequest(JSON_URL);
                if (jsonResult != null) {
                    // 在 UI 线程中使用 Gson 解析 JSON 数据
                    Gson gson = new Gson();
                    YourDataModel data = gson.fromJson(jsonResult, YourDataModel.class);
                    // 获取图片地址
                    imageUrl = data.getUrl();
                    Log.d("Image URL", imageUrl);
                }

                // 在 UI 线程中处理 JSON 数据
                String finalImageUrl = imageUrl;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 使用 WebView 打开 URL
                        openUrlInWebView(finalImageUrl);
                    }
                });
            }
        }).start();
    }



    private void openUrlInWebView(String url) {
        WebView webView = findViewById(R.id.webview); // Replace with your WebView ID
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed
        // 设置 WebViewClient 以在 WebView 内加载页面，而不是使用默认浏览器
        webView.setWebViewClient(new WebViewClient());
        // 加载 URL
        webView.loadUrl(url);
    }


    private String performHttpRequest(String urlString) {
        try {
            // 在新线程中执行 HTTP 请求
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("HTTP Request", "Error: " + e.getMessage());
            return null;
        }
    }

    private class YourDataModel {
        private String url;

        public String getUrl() {
            return url;
        }
    }

}