package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.NoHttpResponseException;

//HTTP 응답 리스너 핸들러
public class CenterResponseListener extends AsyncHttpResponseHandler {
    private final static String TAG = "CenterResponseListener";

    private Context context;

    public CenterResponseListener(Context context) {
        this.context = context;//생성자
    }

    @Override
    public void onStart() {
        UiController.showProgressDialog(context);
    }

    public void onSuccess(int Code, String content) {
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] content) {
        if (BuildConfig.DEBUG) {
            longLogMessage(" onSuccess() statusCode = " + statusCode + " content = " + new String(content));
        }

        try {
            final JsonNode root = JsonUtil.parseTree(new String(content).toString());
            final String code = root.path("Code").textValue();
            if (code.equals("2000")) {
                // 세션만료 : 로그인하고 다시 서비스 요청한다.
                CenterController.expiredSession(context);
            } else {
                onSuccess(Integer.valueOf(code), new String(content));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
