package com.leadplatform.kfarmers.view.menu;

import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseMenuFragmentActivity;
import com.leadplatform.kfarmers.view.cart.CartActivity;
import com.leadplatform.kfarmers.view.common.LicenseeInfoActivity;
import com.leadplatform.kfarmers.view.evaluation.ReviewActivity;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.join.EditGeneralActivity;
import com.leadplatform.kfarmers.view.join.JoinTermsActivity;
import com.leadplatform.kfarmers.view.menu.favorite.FavoriteActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderActivity;
import com.leadplatform.kfarmers.view.menu.point.PointActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingServiceActivity;
import com.leadplatform.kfarmers.view.menu.story.MyStoryActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
//유저 메뉴
public class MenuUserFragment extends BaseFragment {
	public static final String TAG = "MenuUserFragment";

	private final int menuBtnResourceId[] = { R.id.userLayout,R.id.editText,R.id.buyText,R.id.cartText, R.id.favoriteText,R.id.replyText,R.id.Setting,R.id.InquiryText,R.id.PointText,R.id.MyStoryText, R.id.reviewText};
	private ArrayList<View> menuBtnArrayList;
	private ProfileJson profileJson;
	private ImageView profileImage;
	private TextView nameText;
	private DisplayImageOptions options;

	public static MenuUserFragment newInstance(String profile) {
		final MenuUserFragment f = new MenuUserFragment();

		final Bundle args = new Bundle();
		args.putString("profile", profile);
		f.setArguments(args);

		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if (getArguments() != null) {
				String profile = getArguments().getString("profile");
				JsonNode root = JsonUtil.parseTree(profile);
				profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

        if(!AppPreferences.getLogin(getActivity()))
        {
            return;
        }

		try
    	{
        	String profile = DbController.queryProfileContent(getSherlockActivity());
			JsonNode root = JsonUtil.parseTree(profile);
			profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
			
			if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
				((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(profileJson.ProfileImage, profileImage, options);
			}

			if (!PatternUtil.isEmpty(profileJson.Name)) {
				nameText.setText(profileJson.Name);
			} else {
				nameText.setVisibility(View.INVISIBLE);
			}
			
    	}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_menu_right_user, container, false);

		profileImage = (ImageView) v.findViewById(R.id.profileImage);
		nameText = (TextView) v.findViewById(R.id.nameText);

        menuBtnArrayList = new ArrayList<View>();
		for (int resourceId : menuBtnResourceId) {
			View menuBtn = v.findViewById(resourceId);
			menuBtn.setTag(resourceId);
			menuBtn.setOnClickListener(menuBtnOnClickListener);
			menuBtnArrayList.add(menuBtn);
		}

		options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 60) / 2)).build();

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(profileJson.ProfileImage, profileImage, options);
		}

		if (!PatternUtil.isEmpty(profileJson.Name)) {
			nameText.setText(profileJson.Name);
		} else {
			nameText.setVisibility(View.INVISIBLE);
		}

		displaySelectMenuBtn(0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_LEFT_MENU) {
			displaySelectMenuBtn(0);
		}
	}

	private ViewOnClickListener menuBtnOnClickListener = new ViewOnClickListener() {
		@Override
		public void viewOnClick(View v) {
			int resourceId = (Integer) v.getTag();
			displaySelectMenuBtn(resourceId);
			runMenuActivity(resourceId);
		}
	};

	private void displaySelectMenuBtn(int resourceId) {
		for (View menuBtn : menuBtnArrayList) {
			int menuId = (Integer) menuBtn.getTag();
			if (menuId == resourceId) {
				if (menuBtn instanceof TextView) {
					menuBtn.setSelected(true);
				}
			} else {
				if (menuBtn instanceof TextView) {
					menuBtn.setSelected(false);
				}
			}
		}
	}

	private void runMenuActivity(int resourceId) {
        Intent intent = null;
		switch (resourceId) {
            case R.id.userLayout:
                intent = new Intent(getActivity(), EditGeneralActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Edit", null);
                break;
            case R.id.favoriteText:
                intent = new Intent(getActivity(), FavoriteActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				intent.putExtra("type", FavoriteActivity.Type.me);
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Favorite", null);
                break;
            case R.id.replyText:
                intent = new Intent(getActivity(), ReplyActivity.class);
                intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_ADMIN);
                intent.putExtra("diaryTitle", getString(R.string.MenuRightUserTextReply));
                intent.putExtra("diaryIndex", "0");
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Reply", null);
                break;
            case R.id.editText:
                intent = new Intent(getActivity(), EditGeneralActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Edit", null);
                break;
            /*case R.id.logoutText:
                ((BaseFragmentActivity) getActivity()).logout();
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                break;*/
            case R.id.buyText:
                intent = new Intent(getActivity(), OrderActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Order", null);
                break;
            case R.id.cartText:
                intent = new Intent(getActivity(), CartActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Cart", null);
                break;
            case R.id.Setting:
                intent = new Intent(getActivity(), SettingActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Setting", null);
                break;
            case R.id.Terms:
                intent = new Intent(getActivity(), JoinTermsActivity.class);
                intent.putExtra("page","1");
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Terms", "이용약관");
                break;
            case R.id.ServiceInfo:
                intent = new Intent(getActivity(), SettingServiceActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_Service", null);
                break;
            case R.id.Company:
                intent = new Intent(getActivity(), LicenseeInfoActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_USER, "Click_CompanyInfo", null);
                break;
			case R.id.InquiryText:
				intent = new Intent(getActivity(), InquiryActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
			case R.id.PointText:
				intent = new Intent(getActivity(), PointActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
			case R.id.MyStoryText:
				intent = new Intent(getActivity(), MyStoryActivity.class);
				intent.putExtra("userIndex",profileJson.Index);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
			case R.id.reviewText:
				intent = new Intent(getActivity(), ReviewActivity.class);
				intent.putExtra("id",profileJson.ID);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
		}

		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_LEFT_MENU);
		}
	}
}
