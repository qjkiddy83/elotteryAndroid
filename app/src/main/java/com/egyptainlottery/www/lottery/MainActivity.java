package com.egyptainlottery.www.lottery;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.Window;
import android.webkit.*;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    public static int REQUESTCODE1 = 1;

    private void downloadByBrowser(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("download", intent != null ? intent.toUri(0) : null);
            if (intent != null) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    Log.e("download", String.valueOf(downloadId));
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                    String type = downloadManager.getMimeTypeForDownloadedFile(downloadId);
                    Log.e("download", type);
                    if (TextUtils.isEmpty(type)) {
                        type = "*/*";
                    }
                    Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
                    Log.e("download", uri.toString());
                    if (uri != null) {
                        Intent handlerIntent = new Intent(Intent.ACTION_VIEW);
                        handlerIntent.setDataAndType(uri, type);
                        context.startActivity(handlerIntent);
                    }
                }
            }
        }
    }

    private void downloadBySystem(String url, String userAgent, String contentDisposition, String mimeType){
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
//        request.setAllowedOverMetered(false);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(false);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 设置下载文件保存的路径和文件名
        String fileName  = URLUtil.guessFileName(url, contentDisposition, mimeType);
        Log.e("download", fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        Log.e("download", String.valueOf(downloadId));
    }

    public void reload(String uri){
        WebView webView = (WebView) findViewById(R.id.wv);
        webView.loadUrl(uri);
    }

    private float getScale() {
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        int width = display.widthPixels;
        Float val = Float.valueOf(width) / Float.valueOf(1080);
        System.out.print(String.valueOf(val));
        return val.floatValue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == MainActivity.REQUESTCODE1){
            String uri = data.getStringExtra("uri");
            WebView webView = (WebView) findViewById(R.id.wv);
            webView.loadUrl("javascript:loginCallback('"+uri+"')");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        final MWebView webview = findViewById(R.id.wv);
        //声明WebSettings子类
        WebSettings webSettings = webview.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("UTF-8");

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webview.setInitialScale(Float.valueOf(getScale()*100).intValue());

        //缩放操作
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setDisplayZoomControls(false);

        //滚动条
        webview.setVerticalScrollBarEnabled(false);

        //允许通过chrome调试webview页面
//        webview.setWebContentsDebuggingEnabled(true);

        //允许本地缓存
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);

        //设置ua
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            //获取APP版本versionName
            String versionName = packageInfo.versionName;
            //获取APP版本versionCode
            int versionCode = packageInfo.versionCode;
            String ua = webSettings.getUserAgentString();
            webSettings.setUserAgentString(ua + ";lotteryVersion:"+versionCode+";lotteryVersionName:"+versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        webview.setWebChromeClient(new WebChromeClient(){
            //页面alert 代理
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.equals("http://manage.yubaxi.com/redirect")){
                    Intent i = new Intent(MainActivity.this,LoginActivity.class);
                    startActivityForResult(i, REQUESTCODE1 );
                }else {
                    view.loadUrl(url);
                }
                return true;
            }

        });

        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //使用默认浏览器下载
                downloadByBrowser(url);
                //使用系统下载器 测试发现安卓4.4自动启动安装失败
                /*
                downloadBySystem(url,userAgent,contentDisposition,mimetype);
                DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                registerReceiver(receiver, intentFilter);
                */
            }
        });
//        webview.loadUrl("http://lottery.yubaxi.com/");
//        webview.loadUrl("http://www.jsers.cn/demo/test.html");
        webview.loadUrl("http://192.168.11.194:8080");
    }

}
