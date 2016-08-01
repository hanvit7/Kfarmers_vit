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
import android.widget.ImageView.ScaleType;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.parcel.JoinVillageData;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class JoinVillageNextFragment extends BaseFragment {
	public static final String TAG = "JoinVillageNextFragment";

	private final int CONTEXT_MENU_GROUP_ID = 2;
	private final int imageBtnID[] = { R.id.Image1, R.id.Image2, R.id.Image3, R.id.Image4, R.id.Image5 };

	private JoinVillageData joinData;
	private int imageIndex = 0;
	private String imagePath[] = new String[5];

	private EditText farmEdit;
	private EditText addressEdit;
	private ImageView imageBtn[] = new ImageView[5];
	private EditText categoryEdit;
	private EditText introductionEdit;
	private CheckBox agreeCheckBox;
	private Button completBtn, agreementBtn;

	public static JoinVillageNextFragment newInstance(JoinVillageData data) {
		final JoinVillageNextFragment f = new JoinVillageNextFragment();

		final Bundle args = new Bundle();
		args.putParcelable(JoinVillageData.KEY, data);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			joinData = getArguments().getParcelable(JoinVillageData.KEY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_join_village_next, container, false);

		farmEdit = (EditText) v.findViewById(R.id.Farm);
		addressEdit = (EditText) v.findViewById(R.id.Address);
		for (int i = 0; i < imageBtn.length; i++) {
			imageBtn[i] = (ImageView) v.findViewById(imageBtnID[i]);
		}
		categoryEdit = (EditText) v.findViewById(R.id.Category);
		introductionEdit = (EditText) v.findViewById(R.id.Introduction);
		agreeCheckBox = (CheckBox) v.findViewById(R.id.AgreementFlag);
		completBtn = (Button) v.findViewById(R.id.Completion);
		agreementBtn = (Button) v.findViewById(R.id.ForAgreement);
		return v;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		for (int i = 0; i < imageBtn.length; i++) {
			registerForContextMenu(imageBtn[i]);
			imageBtn[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return true;
				}
			});
		}
		imageBtn[0].setOnClickListener(imageViewOnClickListener);

		completBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (getSherlockActivity() instanceof JoinVillageActivity) {
					((JoinVillageActivity) getSherlockActivity()).onCompletBtnClicked(makeCompleteBtnData());
				}
			}
		});

		agreementBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				Intent intent = new Intent(getSherlockActivity(), JoinTermsActivity.class);
				startActivity(intent);
			}
		});

		((JoinVillageActivity) getSherlockActivity()).initActionBarRightBtnComplete();
	}

	@Override
	public void onDestroyView() {
		((JoinVillageActivity) getSherlockActivity()).initActionBarRightBtnNext();
		super.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
				runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE, imagePath[imageIndex]);
				return;
			} else if (requestCode == Constants.REQUEST_TAKE_PICTURE) {
				if(null== data.getData())
				{
					return;
				}
				imagePath[imageIndex] = ImageUtil.getConvertPathMediaStoreImageFile(getSherlockActivity(), data);
				runImageRotateActivity(Constants.REQUEST_TAKE_PICTURE, imagePath[imageIndex]);
				return;
			} else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
				if (imageIndex < 4) {
					imageBtn[imageIndex + 1].setOnClickListener(imageViewOnClickListener);
				}
				imagePath[imageIndex] = data.getStringExtra("imagePath");
				displayImagesView(imagePath[imageIndex]);
				return;
			}
		} else {
			imagePath[imageIndex] = null;
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof JoinVillageActivity) {
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
				if (getSherlockActivity() instanceof JoinVillageActivity) {
					imagePath[imageIndex] = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), JoinVillageNextFragment.this, Constants.REQUEST_TAKE_CAPTURE);
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof JoinVillageActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), JoinVillageNextFragment.this, Constants.REQUEST_TAKE_PICTURE);
				}
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		for (int i = 0; i < imageBtn.length; i++) {
			unregisterForContextMenu(imageBtn[i]);
		}
		super.onDestroy();
	}

	private void runImageRotateActivity(int takeType, String path) {
		if (getSherlockActivity() instanceof JoinVillageActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayImagesView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof JoinVillageActivity) {
			((JoinVillageActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							imageBtn[imageIndex].setScaleType(ScaleType.CENTER_CROP);
							imageBtn[imageIndex].setImageBitmap(loadedImage);
						}
					});
		}
	}

	private final ViewOnClickListener imageViewOnClickListener = new ViewOnClickListener() {
		@Override
		public void viewOnClick(View v) {
			switch (v.getId()) {
			case R.id.Image1:
				imageIndex = 0;
				break;
			case R.id.Image2:
				imageIndex = 1;
				break;
			case R.id.Image3:
				imageIndex = 2;
				break;
			case R.id.Image4:
				imageIndex = 3;
				break;
			case R.id.Image5:
				imageIndex = 4;
				break;
			}
			v.showContextMenu();
		}
	};

	public JoinVillageData makeCompleteBtnData() {
		joinData.setFarm(farmEdit.getText().toString());
		joinData.setAddress(addressEdit.getText().toString());
		joinData.setImagePath(imagePath);
		joinData.setCategory(categoryEdit.getText().toString());
		joinData.setIntroduction(introductionEdit.getText().toString());
		joinData.setAgreeFlag(agreeCheckBox.isChecked());
		return joinData;
	}
}
