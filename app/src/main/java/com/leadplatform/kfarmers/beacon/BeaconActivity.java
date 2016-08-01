package com.leadplatform.kfarmers.beacon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import java.util.HashMap;

public class BeaconActivity extends BaseFragmentActivity {
    private static final String TAG = "WebActivity";

	private WebView mWebView;

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_webview);

		Intent intent = null;
        if ((getIntent() == null) && (getIntent().getExtras() == null)) {
        	finish();
            return;
        }
        
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); 
        mWebView.setWebViewClient(new WebViewClientClass());
        
        HashMap<String, String> pushData = (HashMap<String, String>) getIntent().getSerializableExtra(BeaconHelper.BEACON_BUNDLE);
        
        if(pushData.get(BeaconHelper.BEACON_TYPE).equals("youtube"))
        {
        	intent = YouTubeStandalonePlayer.createVideoIntent((Activity) mContext, "AIzaSyD4qMitHiH5z6iz1wRN6L_Ciw02xdwhuKg", pushData.get(BeaconHelper.BEACON_YOUTUBE_URL), 0, true, false);
            startActivity(intent);	
            finish();
        }
        else if(pushData.get(BeaconHelper.BEACON_TYPE).equals("web"))
        {
        	mWebView.loadUrl(pushData.get(BeaconHelper.BEACON_WEB_URL));
        }
        else if(pushData.get(BeaconHelper.BEACON_TYPE).equals("youtube|web"))
        {
        	mWebView.loadUrl(pushData.get(BeaconHelper.BEACON_WEB_URL));
        	intent = YouTubeStandalonePlayer.createVideoIntent((Activity) mContext, "AIzaSyD4qMitHiH5z6iz1wRN6L_Ciw02xdwhuKg", pushData.get(BeaconHelper.BEACON_YOUTUBE_URL), 0, true, false);
            startActivity(intent);	
        }
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) { 
            mWebView.goBack(); 
            return true; 
        } 
        return super.onKeyDown(keyCode, event);
    }
     
    private class WebViewClientClass extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            view.loadUrl(url); 
            return true; 
        } 
    }


	@Override
	public void initActionBar() {
		//getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		//getSupportActionBar().setCustomView(R.layout.view_actionbar);
		//TextView title = (TextView) findViewById(R.id.title);
		//title.setText(R.string.address_title);

		//initActionBarBack();
	}
	
	

}
