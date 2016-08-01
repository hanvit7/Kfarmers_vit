package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.NoHttpResponseException;

public class CommonResponseListener extends AsyncHttpResponseHandler {
    private final static String TAG = "CommonResponseListener";
    private Context context;

    public CommonResponseListener(Context context) {
        this.context = context;
    }

    public void onSuccess(int Code, String content) {
    }

    @Override
    public void onStart() {
        try {
            UiController.showProgressDialog(context);
        } catch (Exception e) {
        }
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] content) {
        if (BuildConfig.DEBUG) {
            longLogMessage(" onSuccess() statusCode = " + statusCode + " content = " + new String(content));
        }

        try {
            onSuccess(statusCode, new String(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinish() {
        try {
            UiController.hideProgressDialog(context);
        } catch (Exception e) {
        }
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
