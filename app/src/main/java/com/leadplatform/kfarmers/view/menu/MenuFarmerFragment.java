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
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
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
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.join.EditFarmerActivity;
import com.leadplatform.kfarmers.view.join.JoinTermsActivity;
import com.leadplatform.kfarmers.view.menu.favorite.FavoriteActivity;
import com.leadplatform.kfarmers.view.menu.notice.TypeNoticeActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderGeneralActivity;
import com.leadplatform.kfarmers.view.menu.point.PointActivity;
import com.leadplatform.kfarmers.view.menu.release.FarmerReleaseActivity;
import com.leadplatform.kfarmers.view.menu.release.VillageReleaseActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingServiceActivity;
import com.leadplatform.kfarmers.view.product.register.ProductRegisterActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
//농부 메뉴
public class MenuFarmerFragment extends BaseFragment {
	public static final String TAG = "MenuFarmerFragment";

	private final int menuBtnResourceId[] = {R.id.ItemStatus, R.id.userLayout, R.id.homeText,R.id.editText, R.id.buyText, R.id.cartText, R.id.favoriteText, R.id.releaseText, R.id.expInfoText, R.id.replyText,R.id.Setting,R.id.ProductText,R.id.InquiryText,R.id.PointText, R.id.OrderGeneralText, R.id.reviewText};
	private ArrayList<View> menuBtnArrayList;
	private ProfileJson profileJson;
	private ImageView profileImage;
	private TextView farmText, nameText, farmStateText;
	private DisplayImageOptions options;

	public static MenuFarmerFragment newInstance(String profile) {
		final MenuFarmerFragment f = new MenuFarmerFragment();

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_menu_right_farmer, container, false);

		profileImage = (ImageView) v.findViewById(R.id.profileImage);
		farmText = (TextView) v.findViewById(R.id.farmText);
		nameText = (TextView) v.findViewById(R.id.nameText);
        farmStateText = (TextView) v.findViewById(R.id.stateText);

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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(profileJson.ProfileImage, profileImage, options);
		}

		if (!PatternUtil.isEmpty(profileJson.Farm)) {
			farmText.setText(profileJson.Farm);
		} else {
			farmText.setVisibility(View.INVISIBLE);
		}

		if (!PatternUtil.isEmpty(profileJson.Name)) {
			nameText.setText(profileJson.Name);
		} else {
			nameText.setVisibility(View.INVISIBLE);
		}

		if(profileJson.PermissionFlag.equals("N")) {
			farmStateText.setText(getString(R.string.stateDeFarmer));
		} else {
			if (profileJson.TemporaryPermissionFlag.equals("N")) {
				farmStateText.setText(getString(R.string.stateNoFarmer));
			} else {
				farmStateText.setText(getString(R.string.stateOkFarmer));
			}
		}

		displaySelectMenuBtn(0);
	}

	/*private void displayInitExperienceMenu() {
		if (menuBtnArrayList != null) {
			for (View view : menuBtnArrayList) {
				if (experienceFlag == false) {
					if (view.getId() == R.id.expInfoText *//* || view.getId() == R.id.expReserveText *//*) {
						if (view instanceof TextView) {
							((TextView) view).setTextColor(Color.parseColor("#FF6A6A6A"));
							view.setOnClickListener(null);
						}
					}
				}
			}
		}
	}*/

	/*private void initUserCategoryData(String userId) {
		CenterController.getCategory(userId, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						if (root.findPath("List").isArray()) {
							Iterator<JsonNode> it = root.findPath("List").iterator();
							while (it.hasNext()) {
								CategoryJson category = (CategoryJson) JsonUtil.jsonToObject(it.next().toString(), CategoryJson.class);
								if (category.PrimaryIndex.equals("7")) {
									experienceFlag = true;
								}
							}

							if (!experienceFlag)
								displayInitExperienceMenu();

							categoryInitFlag = true;
						}
						break;

					case 1002:
						UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_release);
						break;
					default:
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
						break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}
		});
	}*/

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
				// else if (menuBtn instanceof LinearLayout)
				// {
				// for (int index = 0; index < ((LinearLayout) menuBtn).getChildCount(); index++)
				// {
				// View view = ((LinearLayout) menuBtn).getChildAt(index);
				// view.setSelected(true);
				// }
				// }
			} else {
				if (menuBtn instanceof TextView) {
					menuBtn.setSelected(false);
				}
				// else if (menuBtn instanceof LinearLayout)
				// {
				// for (int index = 0; index < ((LinearLayout) menuBtn).getChildCount(); index++)
				// {
				// View view = ((LinearLayout) menuBtn).getChildAt(index);
				// view.setSelected(false);
				// }
				// }
			}
		}
	}

	private void runMenuActivity(int resourceId) {

        Intent intent = null;
		switch (resourceId) {
            case R.id.ItemStatus:

				/*FrameLayout root = new FrameLayout(getActivity());
				TextView textView = new TextView(getActivity());
				textView.setText("K파머스 회원등급은 <strong>예비승인</strong>과 <strong>정상승인</strong>으로 구분됩니다.\n" +
						"회원승인시 자동으로 <strong>예비승인</strong> 상태가 됩니다.\n" +
						" \n" +
						"<strong>예비승인</strong> 상태에서 등록된 정보는 생산자 본인과 K파머스 관리자에게만 공개되며 SNS 동시등록 및 공유가 불가합니다.\n" +
						"<strong>예비승인</strong>기간 등록된 정보를 바탕으로 <strong>정상승인</strong> 여부가 결정됩니다.\n" +
						" \n" +
						"<strong>정상승인</strong> 기준은 다음과 같습니다.\n" +
						"\n" +
						"\n" +
						"1. 꾸준한 정보등록 (주 2-3회)\n" +
						"2. 홍보/판매 정보가 아닌 순수 생산정보 등록\n" +
						"3. 프로필사진과 농장소개는 명확하게 등록\n" +
						"(생산자 얼굴,재배지전경,농장주소) \n" +
						" \n");

				textView.setVisibility(View.INVISIBLE);

				WebView webView = new WebView(getActivity());
				webView.loadUrl("file:///android_asset/member_grade.html");
				root.addView(textView);
				root.addView(webView);

				UiDialog.showDialog(getActivity(), "회원등급", root, R.string.dialog_ok, null);*/

				intent = new Intent(getActivity(), TypeNoticeActivity.class);
				intent.putExtra("noticeType","F");
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Communication", null);
                break;
            case R.id.userLayout:
                intent = new Intent(getActivity(), EditFarmerActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Edit", null);
                break;
            case R.id.homeText:
				if (profileJson.PermissionFlag.equals("N")) {
					UiController.showDialog(getActivity(), R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
							new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
										CommonUtil.AndroidUtil.actionDial(getActivity(), getResources().getString(R.string.setting_service_center_phone));
									}
								}
							});
				} else {
					intent = new Intent(getActivity(), FarmActivity.class);
					intent.putExtra("userType", profileJson.Type);
					intent.putExtra("userIndex", profileJson.Index);
					((BaseMenuFragmentActivity) getActivity()).toggle();
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Farm", profileJson.Farm);
				}
				break;
            case R.id.favoriteText:
                intent = new Intent(getActivity(), FavoriteActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				intent.putExtra("type",FavoriteActivity.Type.me);
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Favorite", null);
                break;
            case R.id.releaseText:
                intent = new Intent(getActivity(), FarmerReleaseActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Release", null);
                break;
            case R.id.expInfoText:
                intent = new Intent(getActivity(), VillageReleaseActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Experience", null);
                break;
            case R.id.replyText:
                intent = new Intent(getActivity(), ReplyActivity.class);
                intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_ADMIN);
                intent.putExtra("diaryTitle", getString(R.string.MenuRightUserTextReply));
                intent.putExtra("diaryIndex", "0");
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Reply", null);
                break;
            case R.id.editText:
                intent = new Intent(getActivity(), EditFarmerActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Edit", null);
                break;
            /*case R.id.logoutText:
                ((BaseFragmentActivity) getActivity()).logout();
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                break;*/
            case R.id.buyText:
                intent = new Intent(getActivity(), OrderActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Order", null);
                break;
            case R.id.cartText:
                intent = new Intent(getActivity(), CartActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Cart", null);
                break;
            case R.id.Setting:
                intent = new Intent(getActivity(), SettingActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Setting", null);
                break;
            case R.id.Terms:
                intent = new Intent(getActivity(), JoinTermsActivity.class);
                intent.putExtra("page", "1");
				((BaseMenuFragmentActivity) getActivity()).toggle();
                break;
            case R.id.ServiceInfo:
                intent = new Intent(getActivity(), SettingServiceActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
                break;
            case R.id.Company:
                intent = new Intent(getActivity(), LicenseeInfoActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
                break;
			case R.id.ProductText:
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_FARMER, "Click_Product", null);
				if (profileJson.PermissionFlag.equals("N")) {
					UiController.showDialog(getActivity(), R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
							new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
										CommonUtil.AndroidUtil.actionDial(getActivity(), getResources().getString(R.string.setting_service_center_phone));
									}
								}
							});
				}
				else if (profileJson.TemporaryPermissionFlag.equals("N")) {
					UiController.showDialog(getActivity(), R.string.dialog_ready_product, R.string.dialog_call, R.string.dialog_cancel,
							new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
										CommonUtil.AndroidUtil.actionDial(getActivity(), getResources().getString(R.string.setting_service_center_phone));
									}
								}
							});
				}else {
					intent = new Intent(getActivity(), ProductRegisterActivity.class);
					((BaseMenuFragmentActivity) getActivity()).toggle();
				}
				break;
			case R.id.InquiryText:
				intent = new Intent(getActivity(), InquiryActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
			case R.id.PointText:
				intent = new Intent(getActivity(), PointActivity.class);
				((BaseMenuFragmentActivity) getActivity()).toggle();
				break;
			case R.id.OrderGeneralText:
				intent = new Intent(getActivity(), OrderGeneralActivity.class);
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
			getActivity().startActivityFromFragment(this, intent, Constants.REQUEST_LEFT_MENU);
		}
	}
}
