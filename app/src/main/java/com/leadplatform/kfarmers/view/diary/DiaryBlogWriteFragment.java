package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.KakaoStoryMyStoriesParamBuilder;
import com.kakao.KakaoStoryService;
import com.kakao.KakaoStoryService.StoryType;
import com.kakao.MyStoryInfo;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.MyFaceBookJson;
import com.leadplatform.kfarmers.model.json.MyKakaoStoryJson;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.CommonUtil.TimeUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.MyKakaoStoryHttpResponseHandler;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.BaseSlideImageAdapter;
import com.leadplatform.kfarmers.view.base.OnLodingCompleteListener;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.sns.SnsActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiaryBlogWriteFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "DiaryBlogWriteFragment";

	final int TYPE_NAVER = 0;
	final int TYPE_DAUM = 1;
	final int TYPE_TISTORY = 2;

	int type = TYPE_NAVER;

	WebView webView;
	EditText editText;

	String content = "";
	String tag = "";
	String date = "";
	
	public int snsIndex = 0;
	private ArrayList<String> snsList;
	public TextView snsText;
	
	LinearLayout snsInfoLayout;
	
	public int selectItem = -1;
	private String kakaoLastId = "";
	private KakaoListAdapter kakaoListAdapter;



	private String faceBookPaging = "";
	private String faceBookLastId = "";
	private FaceBookAdapter faceBookAdapter;
	
	private boolean bMoreFlag = false;
	
	
	
	OnLodingCompleteListener mOnLodingCompleteListener;

	public static DiaryBlogWriteFragment newInstance() {
		final DiaryBlogWriteFragment f = new DiaryBlogWriteFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
    @Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mOnLodingCompleteListener = (OnLodingCompleteListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnLodingcompleteListener");
		}
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_write_blog_diary,
				container, false);
				
		snsList = new ArrayList<String>();
		Collections.addAll(snsList, getResources().getStringArray(R.array.GetListSns));
		
		v.findViewById(R.id.CategoryLayout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSnsBtnClicked();
            }
        });
		
		snsInfoLayout = (LinearLayout) v.findViewById(R.id.sns_layout);
		snsText = (TextView) v.findViewById(R.id.CategoryText);
		snsText.setText(snsList.get(0));
		
		webView = (WebView) v.findViewById(R.id.webView);
		webView.setWebViewClient(new WebClient()); // 응룡프로그램에서 직접 url 처리
		WebSettings set = webView.getSettings();
		set.setJavaScriptEnabled(true);
		set.setBuiltInZoomControls(false);

		webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");

		editText = (EditText) v.findViewById(R.id.UrlEdit);

		v.findViewById(R.id.urlButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						
						String url = editText.getText().toString().trim();						
						if(url.contains("://"))
						{
							webView.loadUrl(url);
						}
						else
						{
						    webView.loadUrl("http://"+url);
						}
						//webView.loadUrl("http://blog.naver.com/ssunde1/220110787056");
						// webView.loadUrl("http://blog.naver.com/hoon2e125/220061010888");
					}
				});
		
		onSnsBtnClicked();

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}


	private void onSnsBtnClicked() {
		CategoryDialogFragment fragment = CategoryDialogFragment.newInstance(0, snsIndex, snsList.toArray(new String[snsList.size()]), "");
		fragment.setOnCloseCategoryDialogListener(new OnCloseCategoryDialogListener() {
			@Override
			public void onDialogListSelection(int subMenuType, int position) {
				
				if (snsIndex != position) {
					snsIndex = position;
					snsText.setText(snsList.get(position));
					
					content = "";
					tag = "";
					date = "";
					
					selectItem = -1;
					kakaoLastId = "";
					faceBookLastId = "";
					faceBookPaging = "";
					
					Intent intent = new Intent(getSherlockActivity(), SnsActivity.class);
					
					switch (position) {
					case 0:
						snsInfoLayout.setVisibility(View.VISIBLE);
						webView.loadUrl("");
						
						if(null != kakaoListAdapter)
						{
							kakaoListAdapter.clear();
							kakaoListAdapter.notifyDataSetChanged();
						}
						if(null != faceBookAdapter)
						{
							faceBookAdapter.clear();
							faceBookAdapter.notifyDataSetChanged();
						}
						break;
					case 1:
						if(PatternUtil.isEmpty(DbController.queryBlogNaver(getSherlockActivity())))
						{
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Sns-Category", "네이버");
							intent.putExtra("snsType", Constants.REQUEST_SNS_NAVER);
							getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_NAVER);
						}
						else
						{
							snsInfoLayout.setVisibility(View.GONE);
							blogInit(Constants.REQUEST_SNS_BLOG_NAVER);
						}
						
						break;
					case 2:
						if(PatternUtil.isEmpty(DbController.queryBlogDaum(getSherlockActivity())))
						{
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Sns-Category", "다음");
							intent.putExtra("snsType", Constants.REQUEST_SNS_BLOG_DAUM);
							getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_BLOG_DAUM);
						}
						else
						{
							snsInfoLayout.setVisibility(View.GONE);
							blogInit(Constants.REQUEST_SNS_BLOG_DAUM);
						}
						
						break;
					case 3:
						if(PatternUtil.isEmpty(DbController.queryBlogTstory(getSherlockActivity())))
						{
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Sns-Category", "티스토리");
							intent.putExtra("snsType", Constants.REQUEST_SNS_BLOG_TSTORY);
							getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_BLOG_TSTORY);
						}
						else
						{
							snsInfoLayout.setVisibility(View.GONE);
							blogInit(Constants.REQUEST_SNS_BLOG_TSTORY);
						}
						
						break;
					case 4:

						KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Sns-Category", "카카오 스토리");
						kakaoStroyInit(Constants.REQUEST_SNS_KAKAO);

						break;
					case 5:

						KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Sns-Category", "페이스북");
						faceBookInit();
					
						break;
					/*case 6:
						kakaoStroyInit(Constants.REQUEST_SNS_KAKAO_CH);	
						break;*/
					}
				}
			}
		});

		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		//FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		fragment.show(ft, CategoryDialogFragment.TAG);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case Constants.REQUEST_SNS_NAVER:
				
				if(resultCode == Activity.RESULT_OK)
				{
                    DbController.updateNaverFlag(getSherlockActivity(), true);
                    snsInfoLayout.setVisibility(View.GONE);
					blogInit(Constants.REQUEST_SNS_BLOG_NAVER);
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
				
				break;
			case Constants.REQUEST_SNS_BLOG_DAUM:
				if(resultCode == Activity.RESULT_OK)
				{
					snsInfoLayout.setVisibility(View.GONE);
					blogInit(Constants.REQUEST_SNS_BLOG_DAUM);
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
				
				break;
			case Constants.REQUEST_SNS_BLOG_TSTORY:
				if(resultCode == Activity.RESULT_OK)
				{
					snsInfoLayout.setVisibility(View.GONE);
					blogInit(Constants.REQUEST_SNS_BLOG_TSTORY);
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
				break;
			case Constants.REQUEST_SNS_FACEBOOK:
				if(resultCode == Activity.RESULT_OK)
				{
					DbController.updateFaceBookFlag(getSherlockActivity(), true);
					
					faceBookInit();
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
				break;
			case Constants.REQUEST_SNS_KAKAO:
				if(resultCode == Activity.RESULT_OK)
				{
					DbController.updateKakaoFlag(getSherlockActivity(), true);
					
					kakaoStroyInit(Constants.REQUEST_SNS_KAKAO);
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
				
				break;
				
			case Constants.REQUEST_SNS_KAKAO_CH:
				if(resultCode == Activity.RESULT_OK)
				{
					kakaoStroyInit(Constants.REQUEST_SNS_KAKAO_CH);
				}
				else
				{
					snsText.setText(snsList.get(0));
					snsIndex = 0;
				}
	
			default:
				break;
		}
		
		
		
	}
	
	//////////////////// more
	
	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				
				if(snsText.getText().equals("카카오 스토리"))
				{
					getKakaoStory();	
				}
				else if(snsText.getText().equals("페이스북"))
				{
					getFacebook();
				}
				else
				{
					onLoadMoreComplete();	
				}
				
			} else {
				onLoadMoreComplete();
			}
		}
	};

	////////////////////faceBook
	
	private void faceBookInit()
	{
		bMoreFlag = false;
		faceBookLastId = "";
		faceBookPaging = "";

		if (AccessToken.getCurrentAccessToken() == null) {
			Intent intent = new Intent(getSherlockActivity(), SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_FACEBOOK);
			getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_FACEBOOK);
			return;
		}
		

		((DiaryWriteActivity)getActivity()).kakaoinitButton();
		snsInfoLayout.setVisibility(View.GONE);
		webView.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		
		faceBookAdapter = new FaceBookAdapter(getSherlockActivity(), R.layout.item_facebook, new ArrayList<MyFaceBookJson>(),
				((BaseFragmentActivity) getSherlockActivity()).imageLoader);
		
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(faceBookAdapter);
	    swingBottomInAnimationAdapter.setAbsListView(getListView());
	    
	    assert swingBottomInAnimationAdapter.getViewAnimator() != null;
	    swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
	    
		setListAdapter(swingBottomInAnimationAdapter);	
		getFacebook();
	}
	
	private void getFacebook()
	{
		UiController.showProgressDialog(getSherlockActivity());

		GraphRequest graphRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), "me/feed", new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse graphResponse) {
				try {
					ArrayList<MyFaceBookJson> arrayList = new ArrayList<MyFaceBookJson>();

					JsonNode data = JsonUtil.parseTree(graphResponse.getJSONObject().toString());

					JsonNode subData = data.get("data");
					JsonNode pagingData = data.get("paging");

					String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'+0000'";
					String dateFormat2 = "yyyy-MM-dd HH:mm:ss";

					SimpleDateFormat format = new SimpleDateFormat(dateFormat);
					SimpleDateFormat format2 = new SimpleDateFormat(dateFormat2);

					for (JsonNode node : subData) {
						MyFaceBookJson bookJson = (MyFaceBookJson) JsonUtil.jsonToObject(node.toString(), MyFaceBookJson.class);

						if (bookJson.message != null || bookJson.description != null) {
							bookJson.created_time = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(bookJson.created_time).getTime()));
							arrayList.add(bookJson);
						}

						/*if(bookJson.description == null && bookJson.name == null && bookJson.message != null)
						{
							bookJson.created_time = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(bookJson.created_time).getTime()));
							arrayList.add(bookJson);
						}*/
					}

					faceBookAdapter.addAll(arrayList);

					faceBookAdapter.notifyDataSetChanged();

					if (pagingData.has("next") && pagingData.get("next").textValue() != null) {
						/*String str = pagingData.get("next").textValue();
						faceBookLastId = str.split("&until=")[1];*/
						Uri uri = Uri.parse(pagingData.get("next").textValue());
						faceBookLastId = uri.getQueryParameter("until");
						faceBookPaging = uri.getQueryParameter("__paging_token");
						bMoreFlag = true;

					} else {
						faceBookLastId = "";
						bMoreFlag = false;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				onLoadMoreComplete();
				UiController.hideProgressDialog(getSherlockActivity());
			}
		});

		Bundle bundle = new Bundle();
		bundle.putString("limit","30");
		bundle.putString("until",faceBookLastId);
		bundle.putString("__paging_token",faceBookPaging);

		graphRequest.setParameters(bundle);
		graphRequest.executeAsync();

			/*Request.Callback callback = new Request.Callback() {

				@Override
				public void onCompleted(Response response) {
					try {

						ArrayList<MyFaceBookJson> arrayList = new ArrayList<MyFaceBookJson>();

						JsonNode data = JsonUtil.parseTree(response.getGraphObject().getInnerJSONObject().toString());

						JsonNode subData = data.get("data");
						JsonNode pagingData = data.get("paging");

						String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'+0000'";
						String dateFormat2 = "yyyy-MM-dd HH:mm:ss";

						SimpleDateFormat format = new SimpleDateFormat(dateFormat);
						SimpleDateFormat format2 = new SimpleDateFormat(dateFormat2);

						for (JsonNode node : subData) {
							MyFaceBookJson bookJson = (MyFaceBookJson) JsonUtil.jsonToObject(node.toString(), MyFaceBookJson.class);

							if (bookJson.message != null || bookJson.description != null) {
								bookJson.created_time = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(bookJson.created_time).getTime()));
								arrayList.add(bookJson);
							}
						
						*//*if(bookJson.description == null && bookJson.name == null && bookJson.message != null)
						{
							bookJson.created_time = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(bookJson.created_time).getTime()));
							arrayList.add(bookJson);
						}*//*
						}

						faceBookAdapter.addAll(arrayList);

						faceBookAdapter.notifyDataSetChanged();

						if (pagingData.has("next") && pagingData.get("next").textValue() != null) {
							String str = pagingData.get("next").textValue();
							faceBookLastId = str.split("&until=")[1];
							bMoreFlag = true;
						} else {
							faceBookLastId = "";
							bMoreFlag = false;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					onLoadMoreComplete();
					UiController.hideProgressDialog(getSherlockActivity());
				}

			};

			Bundle bundle = new Bundle();
			bundle.putString("limit","30");
			bundle.putString("until",faceBookLastId);

			Session fSession = Session.getActiveSession();
			Request request = new Request(fSession, "me/feed", bundle, HttpMethod.GET, callback);
			RequestAsyncTask asyncTask = new RequestAsyncTask(request);
			asyncTask.execute();*/
		}

		private class FaceBookAdapter extends ArrayAdapter<MyFaceBookJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions optionsProduct;
		private DisplayImageOptions optionsProfile;

		public FaceBookAdapter(Context context, int itemLayoutResourceId, ArrayList<MyFaceBookJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;

			/*this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new CustomRoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 68 ) / 2))
					.showImageOnLoading(R.drawable.common_dummy).build();*/
		
			this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(140))
					.showImageOnLoading(R.drawable.common_dummy).build();
			
			
			this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
					.build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}
			
			final MyFaceBookJson item = getItem(position);

			if (item != null) 
			{
				//uk.co.deanwild.flowtextview.FlowTextView flowTextView = ViewHolder.get(convertView, R.id.ftv);
				TextView des = ViewHolder.get(convertView, R.id.Description);
				TextView date = ViewHolder.get(convertView, R.id.Date);
				ImageView imgView = ViewHolder.get(convertView, R.id.img);
				RelativeLayout check = ViewHolder.get(convertView, R.id.ClickLayout);
				
				date.setText(item.created_time);
				
				if(item.message != null)
				{
					des.setText(item.message);
				}
				else if(item.description != null)
				{
					des.setText(item.description);
				}
				
				/*flowTextView.setTextSize(getResources().getDimension(R.dimen.CommonMediumText));
				flowTextView.setColor(getResources().getColor(R.color.CommonText));*/
							
				OnClickListener clickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectItem = (Integer) v.getTag(R.id.ClickLayout);
						//kakaoListAdapter.notifyDataSetChanged();
						((DiaryWriteActivity)getActivity()).onSaveBtnClicked();
					}
				};
				

				convertView.setTag(R.id.ClickLayout, position);
				convertView.setOnClickListener(clickListener);
					
				imgView.setVisibility(View.GONE);
				/*if(item.type.equals("photo"))
				{
					imgView.setVisibility(View.VISIBLE);
					imageLoader.displayImage(item.picture, imgView);
				}
				else
				{
					imgView.setVisibility(View.GONE);
				}*/
				
/*				if(selectItem == position)
				{
					check.setVisibility(View.VISIBLE);
				}
				else
				{
					check.setVisibility(View.GONE);
				}*/
				
			}
			
			return convertView;
		}
	}
	
	
	//////////////////// kakao
	
	private void kakaoStroyViewInit()
	{
		((DiaryWriteActivity)getActivity()).kakaoinitButton();
		snsInfoLayout.setVisibility(View.GONE);
		webView.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		
		kakaoListAdapter = new KakaoListAdapter(getSherlockActivity(), R.layout.item_kakao_story, new ArrayList<MyKakaoStoryJson>(),
				((BaseFragmentActivity) getSherlockActivity()).imageLoader);
		
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(kakaoListAdapter);
        swingBottomInAnimationAdapter.setAbsListView(getListView());
        
        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
        
		setListAdapter(swingBottomInAnimationAdapter);	
		getKakaoStory();
	}
	
	private void kakaoStroyInit(int type)
	{
		boolean isOpen = false;
		bMoreFlag = false;
		kakaoLastId = "";
		
		if(type == Constants.REQUEST_SNS_KAKAO)
		{
			if (com.kakao.Session.initializeSession(getSherlockActivity(), new SessionCallback() {
				
				@Override
				public void onSessionOpened() {
					kakaoStroyViewInit();
				}
				
				@Override
				public void onSessionClosed(KakaoException exception) {
					UiController.hideProgressDialog(getSherlockActivity());
                    DbController.updateKakaoFlag(getActivity(), false);
				}
			}))
			{
				UiController.showProgressDialog(getSherlockActivity());
			} else if (com.kakao.Session.getCurrentSession().isOpened()) {
				kakaoStroyViewInit();
			} else {
				Intent intent = new Intent(getSherlockActivity(), SnsActivity.class);
				intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO);
				getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_KAKAO);	
			}
		}
		else
		{
			if(PatternUtil.isEmpty(DbController.querySnsKakaoCh(getSherlockActivity())))
			{
				Intent intent = new Intent(getSherlockActivity(), SnsActivity.class);
				intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO_CH);
				getSherlockActivity().startActivityFromFragment(DiaryBlogWriteFragment.this, intent, Constants.REQUEST_SNS_KAKAO_CH);
			}
			else
			{
				((DiaryWriteActivity)getActivity()).kakaoinitButton();
				snsInfoLayout.setVisibility(View.GONE);
				webView.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				
				getListView().setDivider(null);
				getListView().setDividerHeight(0);
				setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
				
				kakaoListAdapter = new KakaoListAdapter(getSherlockActivity(), R.layout.item_kakao_story, new ArrayList<MyKakaoStoryJson>(),
						((BaseFragmentActivity) getSherlockActivity()).imageLoader);
				
				SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(kakaoListAdapter);
		        swingBottomInAnimationAdapter.setAbsListView(getListView());
		        
		        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
		        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
		        
				setListAdapter(swingBottomInAnimationAdapter);	
				getKakaoStroyCh();
			}
		}
	}
	
	private void getKakaoStroyCh()
	{
		String id = DbController.querySnsKakaoCh(getSherlockActivity()).trim();
		
		CenterController.getKakaoStoryCh("https://story.kakao.com/api/profiles/@"+id+"/activities?since="+kakaoLastId, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				
				try {
					JsonNode root = JsonUtil.parseTree(new String(arg2));

					ArrayList<MyKakaoStoryJson> arrayList = new ArrayList<MyKakaoStoryJson>();
					
					for(int i = 0 ; i < root.size() ; i++)
					{
						MyKakaoStoryJson json = new MyKakaoStoryJson();
						
						json.id = root.get(i).get("id").asText();
						json.content = root.get(i).get("content").asText();
						json.created_at = root.get(i).get("created_at").asText();
						json.media_type = root.get(i).get("media_type").asText();
						
						
						json.media = new String[root.get(i).get("media").size()];
						
						for(int j = 0 ; j < root.get(i).get("media").size() ; j++)
						{
							json.media[j] = root.get(i).get("media").get(j).get("thumbnail_url").asText();
						}
						
						arrayList.add(json);
					}
					
					kakaoListAdapter.addAll(arrayList);
					
					kakaoListAdapter.notifyDataSetChanged();
					
					if(arrayList.size()>0)
					{
						kakaoLastId = arrayList.get(arrayList.size()-1).id;
					}
					else
					{
						if(kakaoLastId.equals(""))
						{
							UiController.showDialog(getSherlockActivity(), R.string.dialog_kakao_no_data);
						}
					}
					
					if (arrayList.size() == 18)
						bMoreFlag = true;
					else
						bMoreFlag = false;
					
					onLoadMoreComplete();
					UiController.hideProgressDialog(getSherlockActivity());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
			}
		});
	}
	
	private void getKakaoStory()
	{
		UiController.showProgressDialog(getSherlockActivity());
		Bundle bundle = new KakaoStoryMyStoriesParamBuilder(kakaoLastId).build();
		KakaoStoryService.requestGetMyStories(new MyKakaoStoryHttpResponseHandler<MyStoryInfo[]>(getSherlockActivity()) {

			@Override
			protected void onHttpSuccess(MyStoryInfo[] resultObj) {
				
				String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
				String dateFormat2 = "yyyy-MM-dd HH:mm:ss";
				 
				SimpleDateFormat format = new SimpleDateFormat(dateFormat);
				SimpleDateFormat format2 = new SimpleDateFormat(dateFormat2);
				
				ArrayList<MyKakaoStoryJson> arrayList = new ArrayList<MyKakaoStoryJson>();
				
				for(int i = 0 ; i < resultObj.length ; i++)
				{
					if(!resultObj[i].getMediaType().equals(StoryType.NOT_SUPPORTED))
					{
						if(resultObj[i].getMedias() != null)
						{
							MyKakaoStoryJson json = new MyKakaoStoryJson();
							
							json.media = new String[resultObj[i].getMedias().length];
							
							for(int j = 0 ; j < resultObj[i].getMedias().length ; j++)
							{
								json.media[j] = resultObj[i].getMedias()[j].getOriginal();
							}

							json.id = resultObj[i].getId();
							json.content = resultObj[i].getContent();
							json.created_at = resultObj[i].getCreatedAt();
							json.media_type = resultObj[i].getMediaType().name();
							
							try {
								json.created_at = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(json.created_at).getTime()));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							arrayList.add(json);
						}
					}
				}
				
				kakaoListAdapter.addAll(arrayList);
				
				kakaoListAdapter.notifyDataSetChanged();
				
				if(resultObj.length > 0)
				{
					kakaoLastId = resultObj[resultObj.length-1].getId();
				}
				else
				{
					if(kakaoLastId.equals(""))
					{
						UiController.showDialog(getSherlockActivity(), R.string.dialog_kakao_no_data);
					}
				}
				
				if (resultObj.length == 18)
					bMoreFlag = true;
				else
					bMoreFlag = false;
				
				onLoadMoreComplete();
				UiController.hideProgressDialog(getSherlockActivity());
			}
		}, bundle);
			
	}
	
	private class KakaoListAdapter extends ArrayAdapter<MyKakaoStoryJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions optionsProduct;
		private DisplayImageOptions optionsProfile;

		public KakaoListAdapter(Context context, int itemLayoutResourceId, ArrayList<MyKakaoStoryJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;

			/*this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new CustomRoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 68 ) / 2))
					.showImageOnLoading(R.drawable.common_dummy).build();*/
		
			this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(140))
					.showImageOnLoading(R.drawable.common_dummy).build();
			
			
			this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
					.build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}
			
			final MyKakaoStoryJson item = getItem(position);

			if (item != null) 
			{
				TextView des = ViewHolder.get(convertView, R.id.Description);	
				TextView date = ViewHolder.get(convertView, R.id.Date);
				ViewPager pager = ViewHolder.get(convertView, R.id.image_viewpager);
				RelativeLayout imgLayout = ViewHolder.get(convertView, R.id.Imglayout);
				RelativeLayout check = ViewHolder.get(convertView, R.id.ClickLayout);
				
				des.setText(item.content);
				date.setText(item.created_at);

				OnClickListener clickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectItem = (Integer) v.getTag(R.id.ClickLayout);
						//kakaoListAdapter.notifyDataSetChanged();
						((DiaryWriteActivity)getActivity()).onSaveBtnClicked();
					}
				};
				
				convertView.setTag(R.id.ClickLayout, position);
				convertView.setOnClickListener(clickListener);
				
				if(item.media_type.equals("PHOTO") || item.media_type.equals("image"))
				{
					ArrayList<String> images = new ArrayList<String>();	
					
					for(String str : item.media)
					{
						images.add(str);
					}
					
					if(images.size()>0)
					{
						pager.setAdapter(new BaseSlideImageAdapter(
								getSherlockActivity(), DiaryBlogWriteFragment.this,
								BaseSlideImageAdapter.NO_CLICK, null,
								BaseSlideImageAdapter.NO_CLICK, images,
								imageLoader,clickListener,position));
						pager.setPageMargin((int) getResources()
								.getDimension(R.dimen.image_pager_margin));

						imgLayout.setVisibility(View.VISIBLE);
					}
					else {
						imgLayout.setVisibility(View.GONE);
					}
				}
				else
				{
					imgLayout.setVisibility(View.GONE);
				}
				
/*				if(selectItem == position)
				{
					check.setVisibility(View.VISIBLE);
				}
				else
				{
					check.setVisibility(View.GONE);
				}*/
				
			}
			
			return convertView;
		}
	}
	
	//////////////////// blog
	
	private void blogInit(int type) 
	{
		((DiaryWriteActivity)getActivity()).bloginitButton();
		
		webView.setVisibility(View.VISIBLE);
		getListView().setVisibility(View.GONE);
		
		switch (type) {
		case Constants.REQUEST_SNS_BLOG_NAVER:
			webView.loadUrl("http://blog.naver.com/"+DbController.queryBlogNaver(getSherlockActivity()));
			break;
		case Constants.REQUEST_SNS_BLOG_DAUM:
			webView.loadUrl("http://blog.daum.net/"+DbController.queryBlogDaum(getSherlockActivity()));
			break;
		case Constants.REQUEST_SNS_BLOG_TSTORY:
			webView.loadUrl("http://"+DbController.queryBlogTstory(getSherlockActivity())+".tistory.com");
			break;
		default:
			break;
		}
	}
	
	class WebClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            UiController.showProgressDialog(getSherlockActivity());
        }

        @Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
            UiController.hideProgressDialog(getSherlockActivity());
			if (url.contains("naver.com") || url.contentEquals("blog.me")) {
				type = TYPE_NAVER;
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByClassName('post_ct')[0].innerHTML+'</head>');");
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			} else if (url.contains("daum.net")) {
				type = TYPE_DAUM;
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByClassName('small')[0].innerHTML+'</head>');");
			} else if (url.contains("tistory.com")) {
				type = TYPE_TISTORY;
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				// webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByClassName('area_content')[0].innerHTML+'</head>');");
			}
		}
	}
	
	public void getHtml() {
		webView.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");	
	}

	public void saveHtml(String content) {
		
		String dateFormat;
		SimpleDateFormat format;

		StringBuffer resultString = new StringBuffer();

		Pattern SCRIPTS = Pattern.compile(
				"<(no)?script[^>]*>.*?</(no)?script>", Pattern.DOTALL);
		Pattern STYLE = Pattern.compile("<style[^>]*>.*?</style>",
				Pattern.DOTALL);
		Pattern TAGS = Pattern.compile("<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>");
		Pattern ENTITY_REFS = Pattern.compile("&[^;]+;");
		//Pattern WHITESPACE = Pattern.compile("\\s\\s+");

		Pattern IMG = null;
		Matcher m = null;

		m = SCRIPTS.matcher(content);
		content = m.replaceAll("");
		m = STYLE.matcher(content);
		content = m.replaceAll("");

		Document doc = Jsoup.parse(content);

		switch (type) {
		case TYPE_NAVER:
			
			if(doc.getElementsByClass("map") != null)
				doc.getElementsByClass("map").remove();
			
			if(doc.getElementsByClass("_mapInfo") != null)
				doc.getElementsByClass("_mapInfo").remove();
			
			content = doc.select("div.post_ct").html();
			tag = doc.select("dl.past_tag dd").html();
			date = doc.select("em.num").html();
			
			dateFormat = "yyyy.MM.dd. HH:mm";
			format = new SimpleDateFormat(dateFormat);
			
			try {
				Date postDate = format.parse(date);
			} catch (ParseException e1) {
				date = "";
			}

            boolean isImg = false;

            IMG = Pattern.compile("<img[^>]*src=[\"']([^>\"']+)[\"'][^>]*>");
            m = IMG.matcher(content);

            while (m.find()) {
                String str = m.group(1);
                if(str != null && !str.equals("http://static.naver.net/blank.gif"))
                {
                    str = str.replace("&amp;","==]and]==");
                    str = "==]img]==" + str.toString()+ "==]img]==";
                    m.appendReplacement(resultString,str);
                    isImg = true;
                }
            }

            if(!isImg)
            {
                IMG = Pattern.compile("<span[^>]*thumburl=[\"']?([^>\"']+)[\"']?[^>]*>");
                m = IMG.matcher(content);

                while (m.find()) {
                    String str = m.group(1);
                    m.appendReplacement(resultString, "==]img]==" + str.toString()
                            + "w2==]img]==");
                }
            }

			break;
		case TYPE_DAUM:
			
			if(doc.getElementsByClass("relation_article") != null)
				doc.getElementsByClass("relation_article").remove();
			
			if(doc.getElementsByClass("articleNavi") != null)
				doc.getElementsByClass("articleNavi").remove();
			
			if(doc.getElementById("likeButton") != null)
				doc.getElementById("likeButton").remove();
			
			if(doc.getElementById("socialShareContainer") != null)
				doc.getElementById("socialShareContainer").remove();
			
			if(doc.getElementsByClass("map_attach") != null)
				doc.getElementsByClass("map_attach").remove();
			
			content = doc.select("div.small").html();
			
			date = doc.select("span.date").html();
			
			dateFormat = "yyyy.MM.dd HH:mm";
			format = new SimpleDateFormat(dateFormat);
			
			try {
				Date postDate = format.parse(date);
			} catch (ParseException e1) {
				date = "";
			}

			IMG = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");
			m = IMG.matcher(content);

			while (m.find()) {
				String str = m.group(1);
				m.appendReplacement(resultString, "==]img]==" + str.toString()
						+ "==]img]==");
			}

			break;
		case TYPE_TISTORY:
			
			if(doc.getElementsByClass("map_attach") != null)
				doc.getElementsByClass("map_attach").remove();
			
			IMG = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");
			content = doc.select("div.area_content").html();
			
			date = doc.select("span.datetime").html();
			
			if(date.length() == 5)
			{
				dateFormat = "yyyy.MM.dd";
				format = new SimpleDateFormat(dateFormat);

				date = format.format(Calendar.getInstance().getTime()) + " " + date;
			}
			else
			{
				date = date.replaceAll("/", ".");
			}
			
			
			if(content.trim().isEmpty())
			{
				IMG = Pattern.compile("<img[^>]*original=[\"']?([^>\"']+)[\"']?[^>]*>");
				content = doc.select("div.article").html();	
			}
			
			tag = doc.select("div.tagTrail span.line").html();
			
			if(!tag.trim().isEmpty())
			{
				m = TAGS.matcher(tag);
				tag = m.replaceAll("");
			}

 			m = IMG.matcher(content);

			while (m.find()) {
				String str = m.group(1);
				m.appendReplacement(resultString, "==]img]==" + str.toString()
						+ "==]img]==");
			}

            break;
		}

        m.appendTail(resultString);
        content = resultString.toString();

		m = TAGS.matcher(content);
		content = m.replaceAll("");

		m = ENTITY_REFS.matcher(content);
		content = m.replaceAll("");

        content = content.replaceAll("==]and]==","&");

		//m = WHITESPACE.matcher(content);
		//content = m.replaceAll(" ");
		
		content = content.trim();
		String str[] = content.split("\n");
		
		StringBuilder builder = new StringBuilder();

        for(int i =0 ; i< str.length; i++)
		{
			builder.append(str[i].trim());
			
			if(i<str.length-1)
			{
				builder.append("\n");
			}
		}
		this.content = builder.toString();
	}

	class MyJavaScriptInterface {
		@JavascriptInterface
		public void showHTML(String html) {
			saveHtml(html);
			
			mOnLodingCompleteListener.OnLodingComplete(TAG,!content.trim().equals("")?true:false);
			
			/*boolean isOneDay = true;
			
			String dateFormat = "yyyy.MM.dd HH:mm";
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			
			try {
				Date postDate = format.parse(date);
				Date nowDate = Calendar.getInstance().getTime();
				nowDate.setDate(nowDate.getDate()-1);
				
				if(postDate.before(nowDate))
				{
					isOneDay = false;
				}
			} catch (ParseException e1) {
				
				if(date.contains("방금전")||date.contains("분전"))
				{
					isOneDay = true;
				}
				else
				{
					isOneDay = false;
				}
			}
			
			if(isOneDay)
			{
				mOnLodingCompleteListener.OnLodingComplete(TAG,!content.trim().equals("")?true:false);
			}
			else
			{
				getSherlockActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						UiController.hideProgressDialog(getSherlockActivity());					
					}
				});
				UiController.showDialog(getSherlockActivity(), getString(R.string.toast_sns_get));
			}*/
		}
	}
}
