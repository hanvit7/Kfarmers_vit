package com.leadplatform.kfarmers.view.address;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;
import com.leadplatform.kfarmers.view.payment.PaymentActivity;

public class AddressSearchWebFragment extends BaseDialogFragment {
	public static final String TAG = "AddressSearchWebFragment";

	private final String URL= Constants.KFARMERS_SNIPE_FULL_DOMAIN+"/daum/postcode";

	private WebView mWebview;

	private View mProgress;

	public static AddressSearchWebFragment newInstance() {
		final AddressSearchWebFragment f = new AddressSearchWebFragment();
		final Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
		setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_address_search_web, container, false);

		mProgress = v.findViewById(R.id.progress);
		mWebview = (WebView) v.findViewById(R.id.webView);
		mWebview.getSettings().setJavaScriptEnabled(true);
		mWebview.setWebViewClient(new AddressWebViewClient());
		mWebview.setWebChromeClient(new WebChromeClient());
		mWebview.loadUrl(URL);

		mProgress.setVisibility(View.VISIBLE);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mProgress.setVisibility(View.GONE);
			}
		}, 1000);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	class AddressWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mProgress.setVisibility(View.GONE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Uri paramUri = Uri.parse(url);
			if ("kfarmers".equals(paramUri.getScheme()))
			{
				//view.loadData("", "text/plain", "utf-8");
				/*((PaymentActivity)getActivity()).setSearchData(adapter.getItem(arg2));*/

				AddressJson addressJson = new AddressJson();

				addressJson.setZipCode(paramUri.getQueryParameter("zonecode"));

				if(paramUri.getQueryParameter("userSelectedType").equals("R")) {
					addressJson.setZipCodeCategory(addressJson.ROAD);

					if(!paramUri.getQueryParameter("autoRoadAddress").isEmpty()) {
						addressJson.setAddress(paramUri.getQueryParameter("autoRoadAddress"));
					} else {
						addressJson.setAddress(paramUri.getQueryParameter("roadAddress"));
					}
				} else {
					addressJson.setZipCodeCategory(addressJson.JIBUN);

					if(!paramUri.getQueryParameter("autoJibunAddress").isEmpty()) {
						addressJson.setAddress(paramUri.getQueryParameter("autoJibunAddress"));
					} else {
						addressJson.setAddress(paramUri.getQueryParameter("jibunAddress"));
					}
				}

				((PaymentActivity)getActivity()).setSearchWebData(addressJson);
				((PaymentActivity)getActivity()).hideSearchWebFragment();
			}
			//return true;
			return super.shouldOverrideUrlLoading(view, url);
		}
	}

	/*class SearchingAddressFragment$AddressWebViewClient extends WebViewClient
	{
		private SearchingAddressFragment$AddressWebViewClient(SearchingAddressFragment paramSearchingAddressFragment)
		{
			super(paramSearchingAddressFragment);
		}

		public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
		{
			Log.i(getClass().getSimpleName(), "override url: " + paramString);
			Uri localUri = Uri.parse(paramString);
			if ("beone".equals(localUri.getScheme()))
			{
				SearchingAddressFragment.access$100(this.this$0).loadData("", "text/plain", "utf-8");
				EventBus.getDefault().post(new SelectingEvent(DeliveryInfo.class, new DeliveryInfo(this.this$0.getActivity(), localUri)));
				return false;
			}
			return super.shouldOverrideUrlLoading(paramWebView, paramString);
		}
	}*/

}
