package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.McryptUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.NoHttpResponseException;

import java.net.URLDecoder;

public abstract class SnipeResponseListener extends AsyncHttpResponseHandler {
    private static final String TAG = "SnipeResponseListener";
    protected Context context;

    public SnipeResponseListener(Context context) {
        this.context = context;
    }

    public abstract void onSuccess(int Code, String content, String error);

    @Override
    public void onStart() {
        UiController.showProgressDialog(context);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] content) {
        if (BuildConfig.DEBUG) {
            longLogMessage(" onSuccess() statusCode = " + statusCode + " content = " + new String(content));
        }
        try {
            final JsonNode root = JsonUtil.parseTree(new String(content));
            final String code = root.path("code").asText();
            final String data = root.path("data").asText();
            final String error = root.path("error").asText();

            //601 토큰 만료  602 잘못된 토큰 603 토큰없을때
            if (code.equals("601") || code.equals("603")) {
                // 토큰 만료 : 로그인하고 다시 서비스 요청한다.
                SnipeApiController.expiredToken(context);
            } else if (code.equals("600")) { // 600 토큰 받기 성공
                if (null != data && !data.isEmpty()) {
                    onSuccess(Integer.valueOf(code), data, error);
                }
            } else {
                if (null != data && !data.isEmpty()) {
                    String decrypted = URLDecoder.decode(McryptUtil.decryptString(data));
                    onSuccess(Integer.valueOf(code), decrypted, error);
                } else {
                    onSuccess(Integer.valueOf(code), data, error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onSuccess(400, "", "");
        }
    }

    @Override
    public void onFinish() {
        UiController.hideProgressDialog(context);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, " onFailure() statusCode = " + statusCode + " / error = " + error.getMessage());
            Toast.makeText(context, " onFailure() statusCode = " + statusCode, Toast.LENGTH_LONG).show();
        }

        if (error instanceof NoHttpResponseException) {
            Log.e(TAG, " onFailure() NoHttpResponseException !!!! ");
        }
        UiController.showDialog(context, R.string.dialog_network_error);
    }

    public static void longLogMessage(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longLogMessage(str.substring(4000));
        } else
            Log.d(TAG, str);
    }
}
