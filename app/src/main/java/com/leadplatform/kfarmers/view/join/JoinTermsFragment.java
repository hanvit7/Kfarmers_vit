package com.leadplatform.kfarmers.view.join;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class JoinTermsFragment extends BaseFragment
{
    public static final String TAG = "JoinTermsFragment";

    private final String TERMS_URL = Constants.KFARMERS_FULL_M_DOMAIN+"/API/Agreement.php?Page=";

    private String page;
    private WebView webView;

    public static JoinTermsFragment newInstance(String page)
    {
        final JoinTermsFragment f = new JoinTermsFragment();

        final Bundle args = new Bundle();
        args.putString("page", page);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            page = getArguments().getString("page");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_join_terms, container, false);

        webView = (WebView) v.findViewById(R.id.wv_terms);
        webView.setWebViewClient(mClient);
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        webView.loadUrl(TERMS_URL + page);
    }

    private WebViewClient mClient = new WebViewClient()
    {
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
        {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon)
        {
            super.onPageStarted(view, url, favicon);

            UiController.showProgressDialog(getSherlockActivity());
        }

        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);

            UiController.hideProgressDialog(getSherlockActivity());
        }
    };
}
