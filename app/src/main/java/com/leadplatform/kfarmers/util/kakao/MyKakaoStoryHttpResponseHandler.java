package com.leadplatform.kfarmers.util.kakao;

import android.content.Context;

import com.kakao.APIErrorResult;
import com.kakao.KakaoStoryHttpResponseHandler;


public abstract class MyKakaoStoryHttpResponseHandler<T> extends KakaoStoryHttpResponseHandler<T>
{
    private Context context = null;

    public MyKakaoStoryHttpResponseHandler(Context context)
    {
        this.context = context;
    }

    @Override
    protected void onHttpSessionClosedFailure(final APIErrorResult errorResult)
    {
        //UiController.hideProgressDialog(context);
        onHttpSuccess(null);
//        UiController.showDialog(context, R.string.dialog_error_unknown_kakao);
    }

    @Override
    protected void onNotKakaoStoryUser()
    {
        //UiController.hideProgressDialog(context);
        onHttpSuccess(null);
//        UiController.showDialog(context, R.string.dialog_error_unknown_kakao);
    }

    @Override
    protected void onFailure(final APIErrorResult errorResult)
    {
        //UiController.hideProgressDialog(context);
        onHttpSuccess(null);
//        UiController.showDialog(context, R.string.dialog_error_unknown_kakao);
    }
}
