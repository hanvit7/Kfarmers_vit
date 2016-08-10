package com.leadplatform.kfarmers.view.payment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.json.snipe.CartItemJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.McryptUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderActivity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

public class PaymentWebActivity extends BaseFragmentActivity {
    public static final String TAG = "PaymentWebActivity";

    public WebView mWebView;

    private static final int DIALOG_PROGRESS_WEBVIEW = 0;
    private static final int DIALOG_PROGRESS_MESSAGE = 1;
    private static final int DIALOG_ISP = 2;
    private static final int DIALOG_CARDAPP = 3;
    private static String DIALOG_CARDNM = "";
    private AlertDialog alertIsp;

    private String orderNo = "";
    private String orderData = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PAYMENT_WEB);
        mActivityList.add(this);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {

        if(getIntent().hasExtra("orderNo") && !getIntent().getStringExtra("orderNo").trim().toString().isEmpty())
        {
            orderNo = getIntent().getStringExtra("orderNo");
            orderData = getIntent().getStringExtra("orderData");
        }

        if(orderNo.isEmpty() || orderData.isEmpty())
        {
            finish();
            return;
        }

        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebChromeClient(new ChromeClient());
        mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            mWebView.setWebContentsDebuggingEnabled(true);
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        //기본 페이지
        String token = "token="+SnipeApiController.getClientToken();
        String orignal = "&orignal="+ McryptUtil.enc(orderData);
        String postUrl = token+orignal;

        mWebView.postUrl(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/order/payment/view/" + orderNo, postUrl.getBytes());

    }

    public void paymentOk()
    {
        int totalPrice = 0;
        try {
            JsonNode root = JsonUtil.parseTree(orderData);
            ArrayList<CartItemJson> data = (ArrayList<CartItemJson>) JsonUtil.jsonToArrayObject(root, CartItemJson.class);

            for(CartItemJson cartItemJson : data)
            {
                for(CartItemJson itemJson : cartItemJson.option)
                {
                    totalPrice += Integer.valueOf(itemJson.buyprice) * Integer.valueOf(itemJson.cnt);
                }
            }

            KfarmersAnalytics.onEcommerce(orderNo,totalPrice);

            for(CartItemJson cartItemJson : data)
            {
                for(CartItemJson itemJson : cartItemJson.option)
                {
                    KfarmersAnalytics.onEcommerceItem(orderNo, itemJson);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(mContext,"결제 완료 되었습니다.",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PaymentWebActivity.this, OrderActivity.class);
        intent.putExtra("orderNo",orderNo);
        startActivity(intent);
        PaymentWebActivity.this.finish();
    }

    public void checkOrderItem()
    {

        SnipeApiController.getOrderItem(orderNo, new SnipeResponseListener(PaymentWebActivity.this) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                if(content.equals("T"))
                {
                    UiController.showProgressDialog(PaymentWebActivity.this);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            UiController.hideProgressDialog(PaymentWebActivity.this);
                            paymentOk();
                        }
                    }, 3000);
                }
                else
                {
                    mWebView.loadUrl(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/order/payment/fail");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.stopLoading();
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.clearFormData();
        mWebView = null;
    }

    /*private void clearApplicationCache(java.io.File dir){
        if(dir==null)
            dir = getCacheDir();
        else;
        if(dir==null)
            return;
        else;
        java.io.File[] children = dir.listFiles();
        try{
            for(int i=0;i<children.length;i++)
                if(children[i].isDirectory())
                    clearApplicationCache(children[i]);
                else children[i].delete();
        }
        catch(Exception e){}
    }*/


    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleText = (TextView) findViewById(R.id.actionbar_title_text_view);
        actionBarTitleText.setText("결제하기");
        //initActionBarHomeBtn();
    }


    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            PaymentWebActivity.this.setProgress(newProgress * 1000);
        }

        /*@Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(PaymentWebActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new AlertDialog.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }*/
    }

    @SuppressWarnings("unused")
    private AlertDialog getCardInstallAlertDialog(final String coCardNm) {

        final Hashtable<String, String> cardNm = new Hashtable<String, String>();
        cardNm.put("HYUNDAE", "현대 앱카드");
        cardNm.put("SAMSUNG", "삼성 앱카드");
        cardNm.put("LOTTE", "롯데 앱카드");
        cardNm.put("SHINHAN", "신한 앱카드");
        cardNm.put("KB", "국민 앱카드");
        cardNm.put("HANASK", "하나SK 통합안심클릭");
        cardNm.put("SHINHAN_SMART", "Smart 신한앱");

        final Hashtable<String, String> cardInstallUrl = new Hashtable<String, String>();
        cardInstallUrl.put("HYUNDAE", "market://details?id=com.hyundaicard.appcard");
        cardInstallUrl.put("SAMSUNG", "market://details?id=kr.co.samsungcard.mpocket");
        cardInstallUrl.put("LOTTE", "market://details?id=com.lotte.lottesmartpay");
        cardInstallUrl.put("SHINHAN", "market://details?id=com.shcard.smartpay");
        cardInstallUrl.put("KB", "market://details?id=com.kbcard.cxh.appcard");
        cardInstallUrl.put("HANASK", "market://details?id=com.ilk.visa3d");
        cardInstallUrl.put("SHINHAN_SMART", "market://details?id=com.shcard.smartpay");//여기 수정 필요!!2014.04.01

        AlertDialog alertCardApp = new AlertDialog.Builder(PaymentWebActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("알림")
                .setMessage(cardNm.get(coCardNm) + " 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String installUrl = cardInstallUrl.get(coCardNm);
                        Uri uri = Uri.parse(installUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        Log.d("<INIPAYMOBILE>", "Call : " + uri.toString());
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException anfe) {
                            Toast.makeText(PaymentWebActivity.this, cardNm.get(coCardNm) + "설치 url이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                        }
                        //finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(PaymentWebActivity.this, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create();

        return alertCardApp;

    }


    protected Dialog onCreateDialog(int id) {//ShowDialog


        switch (id) {

            case DIALOG_PROGRESS_WEBVIEW:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("로딩중입니다. \n잠시만 기다려주세요.");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;

            case DIALOG_PROGRESS_MESSAGE:
                break;


            case DIALOG_ISP:

                alertIsp = new AlertDialog.Builder(PaymentWebActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("알림")
                        .setMessage("모바일 ISP 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                        .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String ispUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp";
                                mWebView.loadUrl(ispUrl);
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(PaymentWebActivity.this, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        })
                        .create();

                return alertIsp;

            case DIALOG_CARDAPP:
                return getCardInstallAlertDialog(DIALOG_CARDNM);

        }//end switch

        return super.onCreateDialog(id);

    }


    private class CustomWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {

                Intent intent;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException ex) {
                    return false;
                }

                try {
                    Uri uri = Uri.parse(intent.getDataString());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    if(intent.getScheme().equals("smartxpay-transfer"))
                    {
                        finish();
                    }
                } catch (ActivityNotFoundException e) {

                    /*//ISP
                    if (url.startsWith("ispmobile://")) {
                        //showDialog(DIALOG_ISP);
                        return false;
                    }
                    //현대앱카드
                    else if (intent.getDataString().startsWith("hdcardappcardansimclick://")) {
                        DIALOG_CARDNM = "HYUNDAE";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //신한앱카드
                    else if (intent.getDataString().startsWith("shinhan-sr-ansimclick://")) {
                        DIALOG_CARDNM = "SHINHAN";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //삼성앱카드
                    else if (intent.getDataString().startsWith("mpocket.online.ansimclick://")) {
                        DIALOG_CARDNM = "SAMSUNG";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //롯데앱카드
                    else if (intent.getDataString().startsWith("lottesmartpay://")) {
                        DIALOG_CARDNM = "LOTTE";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //KB앱카드
                    else if (intent.getDataString().startsWith("kb-acp://")) {
                        DIALOG_CARDNM = "KB";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //하나SK카드 통합안심클릭앱
                    else if (intent.getDataString().startsWith("hanaansim://")) {
                        DIALOG_CARDNM = "HANASK";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }

                    //신한카드 SMART신한 앱
                    else if (intent.getDataString().startsWith("smshinhanansimclick://")) {
                        DIALOG_CARDNM = "SHINHAN_SMART";
                        //showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    //현대카드 백신앱
                    else if (intent.getDataString().startsWith("droidxantivirusweb")) {
                        *//*************************************************************************************//*
                        Log.d("<INIPAYMOBILE>", "ActivityNotFoundException, droidxantivirusweb 문자열로 인입될시 마켓으로 이동되는 예외 처리: ");
                        *//*************************************************************************************//*
                        Intent hydVIntent = new Intent(Intent.ACTION_VIEW);
                        hydVIntent.setData(Uri.parse("market://search?q=net.nshc.droidxantivirus"));
                        startActivity(hydVIntent);
                    }
                    */
                    //INTENT:// 인입될시 예외 처리
                    if (url.startsWith("intent://")) {
                        try {
                            Intent excepIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String packageNm = excepIntent.getPackage();

                            excepIntent = new Intent(Intent.ACTION_VIEW);
                            //excepIntent.setData(Uri.parse("market://search?q=" + packageNm));
                            excepIntent.setData(Uri.parse("market://details?id=" + packageNm));
                            startActivity(excepIntent);

                        } catch (URISyntaxException e1) {}
                    }
                }
            } else {
                if (url.equals(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/order/payment/ok")) {
                    paymentOk();
                    return false;
                }
                else if (url.equals(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/order/payment/check")) {
                    checkOrderItem();
                    return false;
                }
                else if (url.equals(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/order/payment/fail")) {
                    view.loadUrl(url);
                    return false;
                }else {
                    view.loadUrl(url);
                    return false;
                }
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                showDialog(DIALOG_PROGRESS_WEBVIEW);
            }catch (Exception e){}
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            try {
                dismissDialog(DIALOG_PROGRESS_WEBVIEW);
            }catch (Exception e){}
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            /*view.loadData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
                    "</head><body>" + "요청실패 : (" + errorCode + ")" + description + "</body></html>", "text/html", "utf-8");*/
        }
    }
}



