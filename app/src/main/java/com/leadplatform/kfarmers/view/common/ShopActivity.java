package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class ShopActivity extends BaseFragmentActivity {
	public static final String TAG = "ShopActivity";

	// private WebView webView;

	public enum type {
		Farm,
		Plan
	}

	private type nowType = type.Farm;
	private String id, name;
	TextView title;

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_base);

		initIntentData();

		if(type.Farm == nowType) {
			farmShop();
		} else if(type.Plan == nowType){
			planShop();
		}
	}

	private void initIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			nowType = (type) intent.getSerializableExtra("type");
			id = intent.getStringExtra("id");
            name = intent.getStringExtra("name");
		}
	}

	private void planShop() {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, ShopPlanFragment.newInstance(id), ShopPlanFragment.TAG);
		ft.commit();

	}

	private void farmShop() {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, ShopFarmFragment.newInstance(id, ""), ShopFarmFragment.TAG);
		ft.commit();

		// Uri uri = Uri.parse("http://m.shop.naver.com/freshmentor/products/217508682");
		// Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		// startActivity(intent);

		// if (webView == null) {
		// webView = new WebView(this);
		// FrameLayout rootView = (FrameLayout) findViewById(R.id.fragment_container);
		// rootView.addView(webView);
		// // webView.getSettings().setPluginsEnabled(true);
		// // webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		//
		// webView.clearCache(true);
		// // webView.getSettings().setUseWideViewPort(true);
		// webView.getSettings().setJavaScriptEnabled(true);
		// webView.getSettings().setDomStorageEnabled(true);
		// // webView.addJavascriptInterface(new JavascriptInterface(this), "Android");
		// // webView.setWebChromeClient(new WebChromeClient());
		// webView.setWebViewClient(new ShopWebViewClient());
		// // webView.loadUrl("http://m.shop.naver.com/freshmentor/products/217508682");
		// webView.loadUrl("http://kfarmers744.godo.co.kr/m2/goods/view.php?category=001&goodsno=1");
		// }
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText(name);
		initActionBarHomeBtn();
	}

	// @Override
	// public void onBackPressed() {
	// if (webView.canGoBack()) {
	// webView.goBack();
	// } else {
	// super.onBackPressed();
	// }
	// }

	// private class ShopWebViewClient extends WebViewClient {
	// @Override
	// public boolean shouldOverrideUrlLoading(WebView view, String url) {
	// view.loadUrl(url);
	// return true;
	// }
	// }
}
