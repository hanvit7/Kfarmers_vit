package com.leadplatform.kfarmers.view.join;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
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
import com.leadplatform.kfarmers.model.json.EditVillageJson;
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

public class EditVillageFragment extends BaseFragment {
	public static final String TAG = "EditVillageFragment";

	private final int CONTEXT_MENU_GROUP_ID = 1;
	private ImageView profileImage;
	private TextView mobileAddressText;
	private EditText nameEdit, phoneEdit, emailEdit, idEdit, pwOldEdit, pwNewEdit;
	private CheckBox emailCheckBox, phoneCheckBox;
	private Button nextBtn;
	private String profilePath;
	private EditVillageJson userInfo;
	private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).build();

	public static EditVillageFragment newInstance() {
		final EditVillageFragment f = new EditVillageFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_EDIT_VILLAGE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_edit_village, container, false);

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
					if (getSherlockActivity() instanceof EditVillageActivity) {
						((EditVillageActivity) getSherlockActivity()).onNextBtnClicked(makeNextBtnData(), makeUserInfoData());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		((EditVillageActivity) getSherlockActivity()).initActionBarRightBtnNext();

		if (userInfo == null)
			getVillageUserInfo();
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
		if (getSherlockActivity() instanceof EditVillageActivity) {
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
				if (getSherlockActivity() instanceof EditVillageActivity) {
					profilePath = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), EditVillageFragment.this, Constants.REQUEST_TAKE_CAPTURE);

					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_ProfileImg", "촬영");
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof EditVillageActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), EditVillageFragment.this, Constants.REQUEST_TAKE_PICTURE);

					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_ProfileImg", "불러오기");
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
		if (getSherlockActivity() instanceof EditVillageActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayProfileImageView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof EditVillageActivity) {
			((EditVillageActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							profileImage.setImageBitmap(loadedImage);
						}
					});
		}
	}

	private void getVillageUserInfo() {
		CenterController.getVillageUser(new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						userInfo = (EditVillageJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), EditVillageJson.class);
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

	private void displayInitEditView(EditVillageJson user) {
		nameEdit.setText(user.Name);
		phoneEdit.setText(user.Phone.replace("-", ""));
		emailEdit.setText(user.Email);
		emailCheckBox.setChecked((user.EmailFlag.equals("Y")));
		phoneCheckBox.setChecked((user.PhoneFlag.equals("Y")));

		emailCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_EmailCheck", isChecked == true ? "true" : "false");
			}
		});

		phoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_PhoneCheck", isChecked == true ? "true" : "false");
			}
		});

		idEdit.setText(user.ID);
		mobileAddressText.setText(user.ID);
		if (!PatternUtil.isEmpty(user.Profile))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Profile, profileImage, options);
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
