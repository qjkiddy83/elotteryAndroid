package com.egyptainlottery.www.lottery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.*;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static int REQUESTCODE1 = 1;

    public void reload(String uri){
        WebView webView = (WebView) findViewById(R.id.wv);
        webView.loadUrl(uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == MainActivity.REQUESTCODE1){
//            Log.e("test",data.getStringExtra("uName"));
            String uri = data.getStringExtra("uri");
//            Toast.makeText(MainActivity.this,uri,Toast.LENGTH_SHORT);
//            try {
//                Thread.currentThread();
//                Thread.sleep(1000);
            WebView webView = (WebView) findViewById(R.id.wv);
            webView.loadUrl("javascript:loginCallback('"+uri+"')");
//            MainActivity.this.reload(uri);
//            }catch (Exception e){}
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        final WebView webview = (WebView) findViewById(R.id.wv);
        //声明WebSettings子类
        WebSettings webSettings = webview.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
//        //缩放操作
//        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
//        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
//        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webview.setVerticalScrollBarEnabled(false);

        //允许通过chrome调试webview页面
//        webview.setWebContentsDebuggingEnabled(true);

        //允许本地缓存
        webSettings.setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        webview.setWebChromeClient(new WebChromeClient(){
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
//                Toast.makeText(Main2Activity.this,url,Toast.LENGTH_SHORT).show();
                if(url.equals("http://manage.yubaxi.com/redirect")){
                    Intent i = new Intent(MainActivity.this,LoginActivity.class);
                    i.putExtra("uName", "legend");
                    startActivityForResult(i, REQUESTCODE1 );
                }else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url){
                CookieManager cookieManager = CookieManager.getInstance();
//                Log.e("test",cookieManager.getCookie(url));
            }
        });
        webview.loadUrl("http://lottery.yubaxi.com/");
    }

}
