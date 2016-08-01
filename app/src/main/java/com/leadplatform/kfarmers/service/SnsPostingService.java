package com.leadplatform.kfarmers.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.MyStoryInfo;
import com.leadplatform.kfarmers.controller.CommonResponseListener;
import com.leadplatform.kfarmers.model.item.SnsPostItem;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.URLShortener;
import com.leadplatform.kfarmers.util.kakao.KakaoStory;
import com.leadplatform.kfarmers.util.kakao.MyKakaoStoryHttpResponseHandler;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;
import com.leadplatform.kfarmers.view.login.OpenIdLoginListener;
import com.nhn.android.naverlogin.data.OAuthLoginState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SnsPostingService extends Service {

    private static final String TAG = "SnsPostingService";

    public static final String DATA = "data";

    private static final String PERMISSION = "publish_actions";

    private SnsPostItem mSnsPostItem;
    private ArrayList<File> mImageArrayList;

    private String mDiaryTitle;

    public SnsPostingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.hasExtra(DATA))
        {
            mSnsPostItem = (SnsPostItem) intent.getSerializableExtra(DATA);
            if(mSnsPostItem != null) {
                snsPosting();
            }
        }
        return START_NOT_STICKY;
    }


    public boolean checkPermissions(Set<String> permissions, List<String> checkPermissions) {
        boolean isCheck = false;
        for(int i = 0; i< checkPermissions.size();i++)
        {
            if(permissions.contains(checkPermissions.get(i))) {
                isCheck = true;
            }
            else
            {
                isCheck = false;
                break;
            }
        }
        return isCheck;
    }
    private void faceBookPosting() {

        if (AccessToken.getCurrentAccessToken() == null  || !AccessToken.getCurrentAccessToken().getPermissions().contains(PERMISSION)) {
            LoginManager.getInstance()
                    .setDefaultAudience(DefaultAudience.EVERYONE)
                    .logInWithPublishPermissions((Activity) getApplicationContext(), Arrays.asList(PERMISSION));
            return;
        }

        Bundle bundle = new Bundle();
        for(File file : mImageArrayList) {
            try {
                RandomAccessFile f = new RandomAccessFile(file, "r");
                byte[] b = new byte[(int)f.length()];
                f.read(b);
                bundle.putByteArray("picture", b);
                bundle.putString("privacy", "{\"value\":\"SELF\"}");
                //bundle.putString("message", mSnsPostItem.getFaceBookData().get(SnsPostItem.TEXT));
                break;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GraphRequest graphRequest = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(), "me/photos", null, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                String id = "";
                try {
                    JsonNode data = JsonUtil.parseTree(graphResponse.getJSONObject().toString());
                    id = data.get("id").asText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GraphRequest graphRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), id , new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        try {
                            JsonNode data = JsonUtil.parseTree(graphResponse.getJSONObject().toString());
                            String image = data.get("images").get(0).get("source").asText();

                            URLShort urlShort = new URLShort();
                            urlShort.execute(image);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putString("fields", "images");

                graphRequest.setParameters(bundle);
                graphRequest.executeAsync();
            }
        });
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    private class URLShort extends AsyncTask<String, String, JSONObject> {
        String ShortUrl;

        @Override
        protected JSONObject doInBackground(String... args) {
            URLShortener jParser = new URLShortener();
            JSONObject json = jParser.getJSONFromUrl(args[0]);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null){
                try {
                    ShortUrl = json.getString("id");

                    Bundle bundle = new Bundle();
                    bundle.putString("picture", ShortUrl);
                    bundle.putString("message", mSnsPostItem.getFaceBookData().get(SnsPostItem.TEXT));
                    bundle.putString("link", "http://m.kfarmers.kr/"+mSnsPostItem.getFaceBookData().get(SnsPostItem.TAG));

                    GraphRequest graphRequest = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(), "me/feed", null, new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {

                        }
                    });
                    graphRequest.setParameters(bundle);
                    graphRequest.executeAsync();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void snsPosting()
    {
        mImageArrayList = new ArrayList<File>();

        if(mSnsPostItem.getImages() != null) {
            for (String path : mSnsPostItem.getImages()) {
                mImageArrayList.add(new File(path));
            }
        }

        if(mSnsPostItem.isFaceBook()) {
            faceBookPosting();
        }

        if(mSnsPostItem.isKakao()) {
            KakaoStory.requestStoryPost(getApplicationContext(), mSnsPostItem.getKakaoData().get(SnsPostItem.TEXT), mImageArrayList, new MyKakaoStoryHttpResponseHandler<MyStoryInfo>(getApplicationContext()) {
                @Override
                protected void onHttpSuccess(MyStoryInfo resultObj) {
                }
            });
        }

        if(mSnsPostItem.isNaver()) {

            if(mSnsPostItem.getNaverData().get(SnsPostItem.TITLE) == null || mSnsPostItem.getNaverData().get(SnsPostItem.TITLE).isEmpty())
            {
                String[] titleS = mSnsPostItem.getNaverData().get(SnsPostItem.TAG).split(",");

                if(titleS == null && titleS.length < 1)
                {
                    mDiaryTitle = "K파머스 앱에서 등록한 글입니다.";
                }
                else
                {
                    mDiaryTitle = "[ " +titleS[0] + " 재배일기 ]";
                }
            }
            else
            {
                mDiaryTitle = mSnsPostItem.getNaverData().get(SnsPostItem.TITLE);
            }

            if(OAuthLoginState.OK == NaverLoginHelper.naverState(getApplicationContext()))
            {
                NaverLoginHelper.naverBlogWrite(getApplicationContext(),mDiaryTitle, mSnsPostItem.getNaverData().get(SnsPostItem.TEXT), mImageArrayList,new CommonResponseListener(getApplicationContext()));
            }
            else if(OAuthLoginState.NEED_REFRESH_TOKEN == NaverLoginHelper.naverState(getApplicationContext()))
            {
                NaverLoginHelper.naverTokenRefresh(getApplicationContext(), new OpenIdLoginListener() {
                    @Override
                    public void onResult(boolean isSuccess, String content) {
                        if(OAuthLoginState.OK == NaverLoginHelper.naverState(getApplicationContext()))
                        {
                            NaverLoginHelper.naverBlogWrite(getApplicationContext(),mDiaryTitle, mSnsPostItem.getNaverData().get(SnsPostItem.TEXT), mImageArrayList, new CommonResponseListener(getApplicationContext()));
                        }
                        else
                        {
                            NaverLoginHelper.naverLogout(getApplicationContext());
                            NaverLoginHelper.clearNaverDb(getApplicationContext());
                        }
                    }
                });
            }
            else
            {
                NaverLoginHelper.naverLogout(getApplicationContext());
                NaverLoginHelper.clearNaverDb(getApplicationContext());
            }
        }
    }
}
