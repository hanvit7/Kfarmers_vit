package com.leadplatform.kfarmers.view.join;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.parcel.JoinFarmerData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class JoinFarmerFragment extends BaseFragment {
	public static final String TAG = "JoinFarmerFragment";

	private final int CONTEXT_MENU_GROUP_ID = 1;
	private ImageView profileImage;
	private EditText nameEdit;
	private EditText phoneEdit;
	private EditText emailEdit;
	private CheckBox emailCheckBox;
	private CheckBox phoneCheckBox;
	private EditText idEdit;
	private EditText pwEdit;
	private EditText confirmPwEdit;
	private Button duplicateBtn;
	private Button nextBtn;
	private TextView mobileAddressText;
	private String profilePath;

	public static JoinFarmerFragment newInstance() {
		final JoinFarmerFragment f = new JoinFarmerFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UiController.showDialog(getSherlockActivity(), getString(R.string.dialog_join_info));

		/*FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		JoinDialogFragment joinDialogFragment = new JoinDialogFragment();
		joinDialogFragment.show(ft,JoinFarmerFragment.TAG);*/

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_join_farmer, container, false);

		profileImage = (ImageView) v.findViewById(R.id.Profile);
		nameEdit = (EditText) v.findViewById(R.id.Name);
		phoneEdit = (EditText) v.findViewById(R.id.Phone);
		emailEdit = (EditText) v.findViewById(R.id.Email);
		emailCheckBox = (CheckBox) v.findViewById(R.id.EmailFlag);
		phoneCheckBox = (CheckBox) v.findViewById(R.id.PhoneFlag);
		idEdit = (EditText) v.findViewById(R.id.ID);
		pwEdit = (EditText) v.findViewById(R.id.PW);
		confirmPwEdit = (EditText) v.findViewById(R.id.PWConfirm);
		duplicateBtn = (Button) v.findViewById(R.id.Duplicate);
		mobileAddressText = (TextView) v.findViewById(R.id.MobileAddress);
		nextBtn = (Button) v.findViewById(R.id.Next);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(profileImage);

		try {
			String phoneNumber = CommonUtil.AndroidUtil.getPhoneNumber(getSherlockActivity());
			phoneEdit.setText(phoneNumber);	
		} catch (Exception e) {}
		
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

		duplicateBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (getSherlockActivity() instanceof JoinFarmerActivity) {
					((JoinFarmerActivity) getSherlockActivity()).onDuplicateBtnClicked(idEdit.getText().toString());
				}
			}
		});

		nextBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (getSherlockActivity() instanceof JoinFarmerActivity) {
					((JoinFarmerActivity) getSherlockActivity()).onNextBtnClicked(makeNextBtnData());
				}
			}
		});

		((JoinFarmerActivity) getSherlockActivity()).initActionBarRightBtnNext();
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
		if (getSherlockActivity() instanceof JoinFarmerActivity) {
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
				if (getSherlockActivity() instanceof JoinFarmerActivity) {
					profilePath = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), JoinFarmerFragment.this, Constants.REQUEST_TAKE_CAPTURE);
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof JoinFarmerActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), JoinFarmerFragment.this, Constants.REQUEST_TAKE_PICTURE);
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
		if (getSherlockActivity() instanceof JoinFarmerActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayProfileImageView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof JoinFarmerActivity) {
			((JoinFarmerActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							profileImage.setImageBitmap(loadedImage);
						}
					});
		}
	}

	public void displaySuccessCheckID() {
		UiController.showDialog(getSherlockActivity(), R.string.dialog_success_check_id);
		idEdit.setEnabled(false);
		mobileAddressText.setText(idEdit.getText().toString());
	}

	public JoinFarmerData makeNextBtnData() {
		JoinFarmerData data = new JoinFarmerData();
		data.setProfile(profilePath);
		data.setName(nameEdit.getText().toString());
		data.setPhone(phoneEdit.getText().toString());
		data.setEmail(emailEdit.getText().toString());
		data.setEmailFlag(emailCheckBox.isChecked());
		data.setPhoneFlag(phoneCheckBox.isChecked());
		data.setUserID(idEdit.getText().toString());
		data.setUserPW(pwEdit.getText().toString());
		data.setConfirmPw(confirmPwEdit.getText().toString());
		data.setDuplicateFlag(idEdit.isEnabled() ? false : true);

		return data;
	}
}
