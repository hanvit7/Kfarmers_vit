package com.leadplatform.kfarmers.util.facebook;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

/**
 * Created by PC on 2015-07-16.
 */
public class ShareFacebook {

    private static CallbackManager callbackManager;

    public static void share(Context context,String title, String des, String imgUrl)
    {
        callbackManager = CallbackManager.Factory.create();
        ShareDialog localShareDialog = new ShareDialog((Activity)context);
        localShareDialog.registerCallback(callbackManager, new FacebookCallback() {
            @Override
            public void onSuccess(Object o) {

            }
            @Override
            public void onCancel() {

            }
            @Override
            public void onError(FacebookException e) {

            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            String titleChg = "";
            if(title != null && !title.trim().isEmpty()) {
                titleChg = title+"님 이야기";
            }

            localShareDialog.show(((ShareLinkContent.Builder)new ShareLinkContent.Builder().setContentTitle(titleChg).setContentDescription(des).setImageUrl(Uri.parse(imgUrl))).setContentUrl(Uri.parse("http://naver.com")).build());
        }
    }
}
