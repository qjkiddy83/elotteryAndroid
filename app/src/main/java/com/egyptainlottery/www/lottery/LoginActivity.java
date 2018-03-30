package com.egyptainlottery.www.lottery;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private Dialog mDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    DialogUtils.closeDialog(mDialog);
                    break;
            }
        }
    };

    /**
     * 关闭dialog
     *
     * @param mDialogUtils
     */
    public static void closeDialog(Dialog mDialogUtils) {
        if (mDialogUtils != null && mDialogUtils.isShowing()) {
            mDialogUtils.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        Log.e("test",intent.getStringExtra("uName"));

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
        webview.loadUrl("http://manage.yubaxi.com/redirect");

        mDialog = DialogUtils.createLoadingDialog(LoginActivity.this);
        mHandler.sendEmptyMessageDelayed(1, 2000);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                if (mDialog != null) {
                    mDialog.show();
                }
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url){
                if(mDialog.isShowing()) {
                    mDialog.hide();
                }
                if(url.indexOf("lottery.yubaxi.com")>-1){
                    closeDialog(mDialog);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("uri", url);
                    setResult(MainActivity.REQUESTCODE1, resultIntent);
                    LoginActivity.this.finish();
                }
            }
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse){
                System.out.print(errorResponse);
                System.out.print(request);
            }
        });
//        webView.loadUrl("http://www.jsers.cn");
    }

}
