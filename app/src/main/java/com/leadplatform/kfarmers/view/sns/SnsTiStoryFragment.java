package com.leadplatform.kfarmers.view.sns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class SnsTiStoryFragment extends BaseFragment {
	public static final String TAG = "SnsTiStoryFragment";//SnsActivity에서 참조

	private EditText apiUrlEdit, blogIdEdit, loginIdEdit, loginPwEdit;
	private Button nextBtn;

	public static SnsTiStoryFragment newInstance() {
		final SnsTiStoryFragment f = new SnsTiStoryFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_sns_tistory, container, false);

		apiUrlEdit = (EditText) v.findViewById(R.id.apiUrlEdit);
		blogIdEdit = (EditText) v.findViewById(R.id.blogIdEdit);
		loginIdEdit = (EditText) v.findViewById(R.id.loginIdEdit);
		loginPwEdit = (EditText) v.findViewById(R.id.loginPwEdit);
		nextBtn = (Button) v.findViewById(R.id.Next);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		nextBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				String apiUrl = apiUrlEdit.getText().toString();
				String blogId = blogIdEdit.getText().toString();
				String id = loginIdEdit.getText().toString();
				String pw = loginPwEdit.getText().toString();
				onSnsTistoryBtnClicked(apiUrl, blogId, id, pw);
			}
		});
	}

	public void onSnsTistoryBtnClicked(final String apiUrl, final String blogId, final String loginId, final String loginPw) {
		if (CommonUtil.PatternUtil.isEmpty(apiUrl)) {
			UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_api_url);
			return;
		}

		if (CommonUtil.PatternUtil.isEmpty(blogId)) {
			UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_blog_id);
			return;
		}

		if (CommonUtil.PatternUtil.isEmpty(loginId)) {
			UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_id);
			return;
		}

		if (CommonUtil.PatternUtil.isEmpty(loginPw)) {
			UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_pw);
			return;
		}

		UiController.hideSoftKeyboard(getSherlockActivity());
		CenterController.snsTistory(apiUrl, blogId, loginId, loginPw, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						DbController.updateTistorySession(getSherlockActivity(), blogId, loginId, loginPw);
						((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
						break;

					case 1001:
					case 1002:
					case 1003:
						((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
						break;

					default:
						((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
						break;
					}
				} catch (Exception e) {
					((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
				}
			}
		});
	}

	//
	// private final String CALLBACK_URL = "http://kfarmers.kr";
	// private final String AUTHORIZE_URL = "https://www.tistory.com/oauth/authorize";
	// private final String CONSUMER_KEY = "017db9d03b7e91ea41f15066338d011f";
	//
	// private WebView webView;
	//
	// public static SnsTiStoryFragment newInstance()
	// {
	// final SnsTiStoryFragment f = new SnsTiStoryFragment();
	// return f;
	// }
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState)
	// {
	// super.onCreate(savedInstanceState);
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	// {
	// final View v = inflater.inflate(R.layout.fragment_sns, container, false);
	//
	// webView = (WebView) v.findViewById(R.id.webView);
	//
	// return v;
	// }
	//
	// @Override
	// public void onActivityCreated(Bundle savedInstanceState)
	// {
	// super.onActivityCreated(savedInstanceState);
	//
	// displayInitWebView();
	// oAuthRequestAuthorize();
	// }
	//
	// private void displayInitWebView()
	// {
	// webView.setVisibility(View.INVISIBLE);
	// webView.clearCache(true);
	// webView.setWebChromeClient(new WebChromeClient());
	// webView.setVerticalScrollBarEnabled(false);
	// webView.setHorizontalScrollBarEnabled(false);
	// webView.getSettings().setJavaScriptEnabled(true);
	// webView.setWebViewClient(new WebViewClient()
	// {
	// public boolean shouldOverrideUrlLoading(WebView view, String url)
	// {
	// Uri uri = Uri.parse(url);
	// if (uri.getHost().equals("kfarmers.kr"))
	// {
	// uri = Uri.parse(url.replace("#", "?"));
	// final String access_token = uri.getQueryParameter("access_token");
	// if (access_token != null)
	// {
	// webView.stopLoading();
	// UiController.showProgressDialog(getSherlockActivity());
	//
	// CenterController.snsTistory(access_token, new CenterResponseListener(getSherlockActivity())
	// {
	// @Override
	// public void onSuccess(int Code, String content)
	// {
	// try
	// {
	// switch (Code)
	// {
	// case 0000:
	// DbController.updateTistorySession(getSherlockActivity(), access_token);
	// ((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
	// break;
	//
	// case 1001:
	// case 1002:
	// ((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
	// break;
	//
	// default:
	// ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
	// break;
	// }
	// }
	// catch (Exception e)
	// {
	// ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
	// }
	// }
	// });
	// }
	// }
	// return super.shouldOverrideUrlLoading(view, url);
	// }
	// });
	// }
	//
	// private void oAuthRequestAuthorize()
	// {
	// StringBuilder sb = new StringBuilder();
	// sb.append(AUTHORIZE_URL);
	// sb.append("?");
	// sb.append("client_id=");
	// sb.append(CONSUMER_KEY);
	// sb.append("&");
	// sb.append("redirect_uri=");
	// sb.append(CALLBACK_URL);
	// sb.append("&");
	// sb.append("response_type=token");
	//
	// webView.loadUrl(sb.toString());
	// webView.setVisibility(View.VISIBLE);
	// }
}
