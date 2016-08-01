package com.leadplatform.kfarmers.view.join;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.EditFarmerJson;
import com.leadplatform.kfarmers.model.parcel.EditGeneralData;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.regex.Pattern;

public class EditFarmerFragment extends BaseFragment {
	public static final String TAG = "EditFarmerFragment";

	private final int CONTEXT_MENU_GROUP_ID = 1;
	private ImageView profileImage;
	private TextView mobileAddressText;
	public EditText nameEdit, phoneEdit, emailEdit, idEdit, pwOldEdit, pwNewEdit, mBankMaster,mBankName,mBankNo,mLicenseeNo,mLicenseeSellNo;
	private CheckBox emailCheckBox, phoneCheckBox;
	private Button nextBtn;
	private String profilePath;
	private EditFarmerJson userInfo;
	private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).build();

	public static EditFarmerFragment newInstance() {
		final EditFarmerFragment f = new EditFarmerFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_EDIT_FARMER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_edit_farmer, container, false);

		profileImage = (ImageView) v.findViewById(R.id.Profile);
		nameEdit = (EditText) v.findViewById(R.id.Name);
		phoneEdit = (EditText) v.findViewById(R.id.Phone);
		emailEdit = (EditText) v.findViewById(R.id.Email);
		emailCheckBox = (CheckBox) v.findViewById(R.id.EmailFlag);
		phoneCheckBox = (CheckBox) v.findViewById(R.id.PhoneFlag);
		idEdit = (EditText) v.findViewById(R.id.ID);
		mobileAddressText = (TextView) v.findViewById(R.id.MobileAddress);
		pwOldEdit = (EditText) v.findViewById(R.id.PW);
		pwNewEdit = (EditText) v.findViewById(R.id.PWConfirm);
		nextBtn = (Button) v.findViewById(R.id.Next);

		mBankMaster = (EditText) v.findViewById(R.id.BankMaster);
		mBankName = (EditText) v.findViewById(R.id.BankName);
		mBankNo = (EditText) v.findViewById(R.id.BankNo);

		mLicenseeNo = (EditText) v.findViewById(R.id.licenseeNo);
		mLicenseeSellNo = (EditText) v.findViewById(R.id.licenseeSellNo);

		InputFilter filterKor = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
				if (!ps.matcher(source).matches()) {
					return "";
				}
				return null;
			}
		};

		mBankMaster.setFilters(new InputFilter[]{filterKor});
		mBankName.setFilters(new InputFilter[]{filterKor});

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(profileImage);

		profileImage.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				v.showContextMenu();
			}
		});

		profileImage.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});

		nextBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				try {
					if (getSherlockActivity() instanceof EditFarmerActivity) {
						((EditFarmerActivity) getSherlockActivity()).onNextBtnClicked(makeNextBtnData(), makeUserInfoData(),mBankMaster.getText().toString(),mBankName.getText().toString(),mBankNo.getText().toString(),mLicenseeNo.getText().toString(),mLicenseeSellNo.getText().toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		((EditFarmerActivity) getSherlockActivity()).initActionBarRightBtnNext();

		if (userInfo == null)
			getFarmerUserInfo();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
				runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE, profilePath);
				return;
			} else if (requestCode == Constants.REQUEST_TAKE_PICTURE) {
				if(null== data.getData())
				{
					return;
				}
				profilePath = ImageUtil.getConvertPathMediaStoreImageFile(getSherlockActivity(), data);
				runImageRotateActivity(Constants.REQUEST_TAKE_PICTURE, profilePath);
				return;
			} else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
				profilePath = data.getStringExtra("imagePath");
				displayProfileImageView(profilePath);
				return;
			}
		} else {
			profilePath = null;
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof EditFarmerActivity) {
			menu.add(CONTEXT_MENU_GROUP_ID, R.id.btn_camera_capture, 0, R.string.context_menu_camera_capture);
			menu.add(CONTEXT_MENU_GROUP_ID, R.id.btn_camera_gallery, 0, R.string.context_menu_camera_gallery);
			menu.setHeaderTitle(R.string.context_menu_camera_title);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == CONTEXT_MENU_GROUP_ID) {
			switch (item.getItemId()) {
			case R.id.btn_camera_capture:
				if (getSherlockActivity() instanceof EditFarmerActivity) {
					profilePath = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), EditFarmerFragment.this, Constants.REQUEST_TAKE_CAPTURE);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_ProfileImg", "촬영");
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof EditFarmerActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), EditFarmerFragment.this, Constants.REQUEST_TAKE_PICTURE);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_ProfileImg", "불러오기");
				}
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		unregisterForContextMenu(profileImage);
		super.onDestroy();
	}

	private void runImageRotateActivity(int takeType, String path) {
		if (getSherlockActivity() instanceof EditFarmerActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayProfileImageView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof EditFarmerActivity) {
			((EditFarmerActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							profileImage.setImageBitmap(loadedImage);
						}
					});
		}
	}

	private void getFarmerUserInfo() {
		CenterController.getFarmerUser(new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							userInfo = (EditFarmerJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), EditFarmerJson.class);
							if (userInfo != null) {
								displayInitEditView(userInfo);
							}
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
	}

	private void displayInitEditView(EditFarmerJson user) {
		nameEdit.setText(user.Name);
		phoneEdit.setText(user.Phone.replace("-", ""));
		emailEdit.setText(user.Email);
		emailCheckBox.setChecked((user.EmailFlag.equals("Y")));
		phoneCheckBox.setChecked((user.PhoneFlag.equals("Y")));

		emailCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_EmailCheck", isChecked == true ? "true" : "false");
			}
		});

		phoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_PhoneCheck", isChecked == true ? "true" : "false");
			}
		});

		idEdit.setText(user.ID);
		mobileAddressText.setText(user.ID);
		if (!PatternUtil.isEmpty(user.Profile))
			((EditFarmerActivity) getSherlockActivity()).imageLoader.displayImage(user.Profile, profileImage, options);

		if(user.BankAccount != null && !user.BankAccount.isEmpty()) {
			try
			{
				String str[] = user.BankAccount.split(":");
				mBankMaster.setText(str[0]);
				mBankName.setText(str[1]);
				mBankNo.setText(str[2]);
			}catch (Exception e)
			{}
		}

		if(user.BusinessLicensee != null && !user.BusinessLicensee.isEmpty()) {
			mLicenseeNo.setText(user.BusinessLicensee);
		}
		if(user.OnlineMarketing != null && !user.OnlineMarketing.isEmpty()) {
			mLicenseeSellNo.setText(user.OnlineMarketing);
		}
	}

	public EditGeneralData makeNextBtnData() {
		EditGeneralData data = new EditGeneralData();
		data.setProfile(profilePath);
		data.setPhone(phoneEdit.getText().toString());
		data.setEmail(emailEdit.getText().toString());
		data.setEmailFlag(emailCheckBox.isChecked());
		data.setPhoneFlag(phoneCheckBox.isChecked());
		data.setUserOldPw(pwOldEdit.getText().toString());
		data.setUserNewPW(pwNewEdit.getText().toString());
		return data;
	}

	public String makeUserInfoData() {
		try {
			return JsonUtil.objectToJson(userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
