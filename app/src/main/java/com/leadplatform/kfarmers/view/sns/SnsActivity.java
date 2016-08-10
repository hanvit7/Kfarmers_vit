package com.leadplatform.kfarmers.view.sns;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import net.daum.mf.oauth.MobileOAuthLibrary;

public class SnsActivity extends BaseFragmentActivity
{
    private final static String TAG = "SNSActivity";

    static final String DAUM_CLIENT_ID = "405346064575943911";

    private int snsType;

    /***************************************************************/
    // Override
    /***************************************************************/

    /*@Override
    protected void onNewIntent(Intent intent) {
        Log.e("", "onNewIntent");
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            Log.e("", "url : " + uri);
            // authorize() 호출 후에 url scheme을 통해 callback이 들어온다.
            MobileOAuthLibrary.getInstance().handleUrlScheme(uri);
        }
    }*/


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals("daum-405346064575943911")) {
            snsType = Constants.REQUEST_SNS_DAUM;
            MobileOAuthLibrary.getInstance().handleUrlScheme(uri);
        }
        runSnsFragment(snsType);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);
        initContentView(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null && uri.getScheme().equals("daum-405346064575943911")) {
            snsType = Constants.REQUEST_SNS_DAUM;
            MobileOAuthLibrary.getInstance().handleUrlScheme(uri);
        }
        runSnsFragment(snsType);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            snsType = intent.getIntExtra("snsType", -1);
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        displayInitTitleText(snsType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
/*        if (Session.getActiveSession() != null)
        {
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }*/
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 사용이 끝나면 반드시 호출해주어야 한다.
        MobileOAuthLibrary.getInstance().uninitialize();
    }

    private void displayInitTitleText(int snsType)
    {
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        switch (snsType)
        {
            case Constants.REQUEST_SNS_NAVER:
                title.setText(R.string.SNSNaverBlogTitle);
                break;
            case Constants.REQUEST_SNS_DAUM:
                title.setText(R.string.SNSDaumBlogTitle);
                break;
            case Constants.REQUEST_SNS_TISTORY:
                title.setText(R.string.SNSTiStoryTitle);
                break;
            case Constants.REQUEST_SNS_FACEBOOK:
                title.setText(R.string.SNSFacebookTitle);
                break;
            case Constants.REQUEST_SNS_TWITTER:
                title.setText(R.string.SNSTwitterTitle);
                break;
            case Constants.REQUEST_SNS_KAKAO:
                title.setText(R.string.SNSKakaoTitle);
                break;
            case Constants.REQUEST_SNS_BLOG_NAVER:
                title.setText(R.string.SNSNaverBlogTitle);
                break;
            case Constants.REQUEST_SNS_BLOG_DAUM:
                title.setText(R.string.SNSDaumBlogTitle);
                break;
            case Constants.REQUEST_SNS_BLOG_TSTORY:
                title.setText(R.string.SNSTiStoryTitle);
                break;
            case Constants.REQUEST_SNS_KAKAO_CH:
                title.setText(R.string.SNSKakaoChTitle);
                break;
        }
    }

    private void runSnsFragment(int snsType)
    {
        if(snsType == Constants.REQUEST_SNS_DAUM) {
            UiController.showDialog(mContext, R.string.dialog_sns_modify, new CustomDialogListener() {
                @Override
                public void onDialog(int type) {
                    finish();
                }
            });
            return;
        }

        SherlockFragment fragment = null;
        String tag = null;

        switch (snsType)
        {
            case Constants.REQUEST_SNS_NAVER:
                fragment = SnsNaverFragment.newInstance();
                tag = SnsNaverFragment.TAG;
                break;

            case Constants.REQUEST_SNS_DAUM:
                fragment = SnsDaumFragment.newInstance();
                tag = SnsDaumFragment.TAG;
                break;

            case Constants.REQUEST_SNS_TISTORY:
                fragment = SnsTiStoryFragment.newInstance();
                tag = SnsTiStoryFragment.TAG;
                break;

            case Constants.REQUEST_SNS_FACEBOOK:
                fragment = SnsFacebookFragment.newInstance();
                tag = SnsFacebookFragment.TAG;
            	break;

            case Constants.REQUEST_SNS_TWITTER:
                fragment = SnsTwitterFragment.newInstance();
                tag = SnsTwitterFragment.TAG;
                break;
            case Constants.REQUEST_SNS_KAKAO:
                fragment = SnsKakaoLoginFragment.newInstance();
                tag = SnsKakaoLoginFragment.TAG;
                break;  
                
            case Constants.REQUEST_SNS_BLOG_NAVER:
                fragment = SnsBlogFragment.newInstance(Constants.REQUEST_SNS_BLOG_NAVER);
                tag = SnsBlogFragment.TAG;
                break;

            case Constants.REQUEST_SNS_BLOG_DAUM:
                fragment = SnsBlogFragment.newInstance(Constants.REQUEST_SNS_BLOG_DAUM);
                tag = SnsBlogFragment.TAG;
                break;
            case Constants.REQUEST_SNS_BLOG_TSTORY:
                fragment = SnsBlogFragment.newInstance(Constants.REQUEST_SNS_BLOG_TSTORY);
                tag = SnsBlogFragment.TAG;
                break;  
                
            case Constants.REQUEST_SNS_KAKAO_CH:
                fragment = SnsBlogFragment.newInstance(Constants.REQUEST_SNS_KAKAO_CH);
                tag = SnsBlogFragment.TAG;
                break;
        }

        if (fragment != null)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, tag);
            ft.commit();
        }
    }

    public void displaySuccessConnectSNS()
    {
        UiController.showDialog(this, R.string.dialog_connect_sns, new CustomDialogListener()
        {
            @Override
            public void onDialog(int type)
            {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    public void displayErrorConnectSNS()
    {
        UiController.showDialog(this, R.string.dialog_error_sns, new CustomDialogListener()
        {
            @Override
            public void onDialog(int type)
            {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void displayUnknownAccountSNS()
    {
        UiController.showDialog(this, R.string.dialog_unknown_account, new CustomDialogListener()
        {
            @Override
            public void onDialog(int type)
            {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
