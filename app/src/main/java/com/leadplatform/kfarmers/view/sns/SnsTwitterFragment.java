package com.leadplatform.kfarmers.view.sns;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.util.oauth.AccessToken;
import com.leadplatform.kfarmers.util.oauth.OAuthRequestTokenListener;
import com.leadplatform.kfarmers.util.oauth.OAuthRequestTokenTask;
import com.leadplatform.kfarmers.util.oauth.OAuthRetrieveAccessTokenListener;
import com.leadplatform.kfarmers.util.oauth.RetrieveAccessTokenTask;
import com.leadplatform.kfarmers.view.base.BaseFragment;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

public class SnsTwitterFragment extends BaseFragment
{
    public static final String TAG = "SnsTwitterFragment";//SnsActivity에서 참조

    private final String CALLBACK_URL = Constants.KFARMERS_FULL_DOMAIN;
    private final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

    private final String CONSUMER_KEY = "kBDVrAGUkS8CN4LBF99jJQ"; 
    private final String CONSUMER_SECRET = "GKS6Lshmb1yu66Tq2bkewS7U6TjxmczPUHJj9LqwLQ";

    private OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    private OAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);

    private WebView webView;

    public static SnsTwitterFragment newInstance()
    {
        final SnsTwitterFragment f = new SnsTwitterFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_sns, container, false);

        webView = (WebView) v.findViewById(R.id.webView);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        UiController.showProgressDialog(getSherlockActivity());
        displayInitWebView();
        new OAuthRequestTokenTask(CALLBACK_URL, consumer, provider, new OAuthRequestTokenListener()
        {
            @Override
            public void onSuccess(String authUrl)
            {
                UiController.hideProgressDialog(getSherlockActivity());
                if (authUrl != null)
                {
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(authUrl);
                }
                else
                {
                    ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                }
            }
        }).execute();
    }

    private void displayInitWebView()
    {
        webView.setVisibility(View.INVISIBLE);
        webView.clearCache(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()
        {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Uri uri = Uri.parse(url);
                if (uri.getHost().equals(Constants.KFARMERS_DOMAIN))
                {
                    webView.stopLoading();
                    UiController.showProgressDialog(getSherlockActivity());

                    new RetrieveAccessTokenTask(consumer, provider, new OAuthRetrieveAccessTokenListener()
                    {
                        @Override
                        public void onSuccess(final AccessToken accessToken)
                        {
                            CenterController.snsTwitter(accessToken.oAuthToken, accessToken.oAuthSecret, new CenterResponseListener(
                                    getSherlockActivity())
                            {
                                @Override
                                public void onSuccess(int Code, String content)
                                {
                                    try
                                    {
                                        switch (Code)
                                        {
                                            case 0000:
                                                DbController.updateTwitterSession(getSherlockActivity(), accessToken.oAuthToken,
                                                        accessToken.oAuthSecret);
                                                ((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
                                                break;

                                            case 1001:
                                            case 1002:
                                                ((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
                                                break;

                                            default:
                                                ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                                                break;
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                                    }
                                }
                            });
                        }
                    }).execute(uri);
                    return false;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }
}
