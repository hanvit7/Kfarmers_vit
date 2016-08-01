package com.leadplatform.kfarmers.view.menu.notice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.NoticeChildJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;

public class TypeNoticeDetailFragment extends BaseFragment {
	public static final String TAG = "TypeNoticeDetailFragment";

	private String userType, userIndex, noticeIndex;
	private NoticeChildJson noticeDetail;
	private LinearLayout mLayout;
	private TextView mTitle,mDate;
	private RelativeLayout mRepleBtn;
	private UserDb mUser;

	public static TypeNoticeDetailFragment newInstance(String noticeIndex) {
		final TypeNoticeDetailFragment f = new TypeNoticeDetailFragment();

		final Bundle args = new Bundle();
		args.putString("noticeIndex", noticeIndex);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			noticeIndex = getArguments().getString("noticeIndex");
		}
		mUser = DbController.queryCurrentUser(getActivity());

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCER_COMMUNICATION_DETAIL, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_notice_detail, container, false);

		mLayout = (LinearLayout) v.findViewById(R.id.Layout);
		mTitle = (TextView) v.findViewById(R.id.Title);
		mDate = (TextView) v.findViewById(R.id.Date);
		mRepleBtn = (RelativeLayout) v.findViewById(R.id.RepleBtn);


		mRepleBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				SnipeApiController.getChatManager(new SnipeResponseListener(getActivity()) {
					@Override
					public void onSuccess(int Code, String content, String error) {
						try {
							JsonNode root = JsonUtil.parseTree(content);
							if (mUser.getUserID().equals(root.get("manager").textValue())) {
								return;
							}
							SnipeApiController.checkChatRoom(mUser.getUserID(), root.get("manager").textValue(), new SnipeResponseListener(getActivity()) {
								@Override
								public void onSuccess(int Code, String content, String error) {
									try {
										switch (Code) {
											case 200:
												Intent intent = new Intent(getActivity(), InquiryActivity.class);
												intent.putExtra("index", content);
												startActivity(intent);
												break;
											default:
												UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
										}
									} catch (Exception e) {
										UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getNoticeData();
	}

	private void makeView()
	{
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200)).build();

		mTitle.setText(noticeDetail.Title);
		mDate.setText(noticeDetail.Date);



		LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (RowJson item : noticeDetail.Rows) {
			if (item.Type.equals("Text")) {
				TextView view = (TextView) inflater.inflate(R.layout.item_detail_text, null);
				view.setText(item.Value);
				mLayout.addView(view);
			} else if (item.Type.equals("Image")) {
				DynamicRatioImageView view = (DynamicRatioImageView) inflater.inflate(R.layout.item_detail_image, null);
				//ImageLoader.getInstance().displayImage("http://farmplan.co.kr/CustomerImage/20150413170609_552b78f1eba23_0.png", view, options);
				ImageLoader.getInstance().displayImage(item.Value, view, options);
				mLayout.addView(view);
			}
		}
	}

	private void getNoticeData()
	{
		CenterController.getDetailNotice(noticeIndex, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							noticeDetail = (NoticeChildJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), NoticeChildJson.class);
							makeView();
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
			}
		});
	}
}