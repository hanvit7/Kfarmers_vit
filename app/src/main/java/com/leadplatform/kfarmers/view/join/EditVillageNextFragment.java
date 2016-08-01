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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.EditVillageJson;
import com.leadplatform.kfarmers.model.parcel.EditVillageData;
import com.leadplatform.kfarmers.util.CommonUtil;
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

public class EditVillageNextFragment extends BaseFragment {
	public static final String TAG = "EditVillageNextFragment";

	private final int CONTEXT_MENU_GROUP_ID = 2;
	final private int imageBtnID[] = { R.id.Image1, R.id.Image2, R.id.Image3, R.id.Image4, R.id.Image5 };

	private int imageIndex = 0;
	private String imagePath[] = new String[5];

	private EditText farmEdit, addressEdit, categoryEdit, newCategoryEdit, introductionEdit;
	private ImageView imageBtn[] = new ImageView[5];
	private Button completBtn;
	private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).build();
	private EditVillageJson userInfo;

	public static EditVillageNextFragment newInstance(String userInfo) {
		final EditVillageNextFragment f = new EditVillageNextFragment();

		final Bundle args = new Bundle();
		args.putString("userInfo", userInfo);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			if (getArguments() != null) {
				userInfo = (EditVillageJson) JsonUtil.jsonToObject(getArguments().getString("userInfo"), EditVillageJson.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_edit_village_next, container, false);

		farmEdit = (EditText) v.findViewById(R.id.Farm);
		addressEdit = (EditText) v.findViewById(R.id.Address);
		for (int i = 0; i < imageBtn.length; i++) {
			imageBtn[i] = (ImageView) v.findViewById(imageBtnID[i]);
		}
		categoryEdit = (EditText) v.findViewById(R.id.Category);
		//newCategoryEdit = (EditText) v.findViewById(R.id.newCategory);
		introductionEdit = (EditText) v.findViewById(R.id.Introduction);
		completBtn = (Button) v.findViewById(R.id.Completion);

        v.findViewById(R.id.PhoneLayout).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                CommonUtil.AndroidUtil.actionDial(getActivity(), getResources().getString(R.string.setting_service_center_phone));
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_CategoryPhone", null);
            }
        });

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		for (int i = 0; i < imageBtn.length; i++) {
			registerForContextMenu(imageBtn[i]);
			imageBtn[i].setOnClickListener(imageViewOnClickListener);
			imageBtn[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return true;
				}
			});
		}

		completBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (getSherlockActivity() instanceof EditVillageActivity) {
					((EditVillageActivity) getSherlockActivity()).onCompletBtnClicked(makeCompleteBtnData());
				}
			}
		});

		displayInitEditView(userInfo);
		((EditVillageActivity) getSherlockActivity()).initActionBarRightBtnComplete();
	}

	@Override
	public void onDestroyView() {
		((EditVillageActivity) getSherlockActivity()).initActionBarRightBtnNext();
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
					imagePath[imageIndex] = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), EditVillageNextFragment.this, Constants.REQUEST_TAKE_CAPTURE);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_FarmImg", "촬영");
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof EditVillageActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), EditVillageNextFragment.this, Constants.REQUEST_TAKE_PICTURE);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_FarmImg", "불러오기");
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
		if (getSherlockActivity() instanceof EditVillageActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayImagesView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof EditVillageActivity) {
			((EditVillageActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
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

	private void displayInitEditView(EditVillageJson user) {
		farmEdit.setText(user.Farm);
		addressEdit.setText(user.Address);
		categoryEdit.setText(user.CategoryList);
		introductionEdit.setText(user.Introduction);

		if (!PatternUtil.isEmpty(user.Image1))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Image1, imageBtn[0], options);
		if (!PatternUtil.isEmpty(user.Image2))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Image2, imageBtn[1], options);
		if (!PatternUtil.isEmpty(user.Image3))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Image3, imageBtn[2], options);
		if (!PatternUtil.isEmpty(user.Image4))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Image4, imageBtn[3], options);
		if (!PatternUtil.isEmpty(user.Image5))
			((EditVillageActivity) getSherlockActivity()).imageLoader.displayImage(user.Image5, imageBtn[4], options);
	}

	public EditVillageData makeCompleteBtnData() {
		EditVillageData data = new EditVillageData();

		data.setFarm(farmEdit.getText().toString());
		data.setAddress(addressEdit.getText().toString());
		data.setImage1(imagePath[0]);
		data.setImage2(imagePath[1]);
		data.setImage3(imagePath[2]);
		data.setImage4(imagePath[3]);
		data.setImage5(imagePath[4]);
		//data.setCategory(newCategoryEdit.getText().toString());
        data.setCategory("");
		data.setIntroduction(introductionEdit.getText().toString());

		return data;
	}
}
