package com.leadplatform.kfarmers.view.menu.release;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.ReleaseVillageJson;
import com.leadplatform.kfarmers.model.parcel.ReleaseListData;
import com.leadplatform.kfarmers.model.parcel.ReleaseVillageData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.DatePickerDialogFragment;
import com.leadplatform.kfarmers.view.common.DatePickerDialogFragment.OnCloseDatePickerDialogListener;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Calendar;

public class VillageReleaseEditFragment extends BaseFragment implements OnCloseDatePickerDialogListener {
	public static final String TAG = "VillageReleaseEditFragment";

	private final int CONTEXT_MENU_GROUP_ID = 1;
	private final int DATE_PICKER_DIALOG_RELEASE_START = 0;
	private final int DATE_PICKER_DIALOG_RELEASE_END = 1;
	private final int imageViewID[] = { R.id.Image1, R.id.Image2, R.id.Image3, R.id.Image4, R.id.Image5 };

	private int imageIndex = 0;
	private String imagePath[] = new String[5];
	private ReleaseListData listData;
	private TextView title, releaseDateStart, releaseDateEnd;
	private EditText releasePrice, releaseNote;
	private CheckBox alwaysCheck, finishCheck;
	private ImageView imageView[] = new ImageView[5];
	private ReleaseVillageJson releaseVillageData;

	public static VillageReleaseEditFragment newInstance(ReleaseListData listData) {
		final VillageReleaseEditFragment f = new VillageReleaseEditFragment();

		final Bundle args = new Bundle();
		args.putParcelable(ReleaseListData.KEY, listData);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			listData = getArguments().getParcelable(ReleaseListData.KEY);
		}

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_EXPERIENCE_MANAGE_EDIT, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_village_release_edit, container, false);

		title = (TextView) v.findViewById(R.id.actionbar_title_text_view);
		releaseDateStart = (TextView) v.findViewById(R.id.releaseDateStart);
		releaseDateEnd = (TextView) v.findViewById(R.id.releaseDateEnd);
		alwaysCheck = (CheckBox) v.findViewById(R.id.alwaysCheck);
		finishCheck = (CheckBox) v.findViewById(R.id.finishCheck);
		releasePrice = (EditText) v.findViewById(R.id.releasePrice);
		releaseNote = (EditText) v.findViewById(R.id.releaseNote);
		for (int i = 0; i < imageViewID.length; i++) {
			imageView[i] = (ImageView) v.findViewById(imageViewID[i]);
		}

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		title.setText(listData.getSubName());

		releaseDateStart.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onReleaseDateStartClicked();
			}
		});

		releaseDateEnd.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onReleaseDateEndClicked();
			}
		});

		alwaysCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					releaseDateStart.setEnabled(false);
					releaseDateEnd.setEnabled(false);
				} else {
					if (!finishCheck.isChecked()) {
						releaseDateStart.setEnabled(true);
						releaseDateEnd.setEnabled(true);
					}
				}
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EXPERIENCE_MANAGE_EDIT, "Click_Always", isChecked==true ? "true":"false");
			}
		});
		finishCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					releaseDateStart.setEnabled(false);
					releaseDateEnd.setEnabled(false);
					releasePrice.setEnabled(false);
					releaseNote.setEnabled(false);
					for (ImageView view : imageView) {
						view.setEnabled(false);
					}
				} else {
					if (!alwaysCheck.isChecked()) {
						releaseDateStart.setEnabled(true);
						releaseDateEnd.setEnabled(true);
					}
					releasePrice.setEnabled(true);
					releaseNote.setEnabled(true);
					for (ImageView view : imageView) {
						view.setEnabled(true);
					}
				}
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_EXPERIENCE_MANAGE_EDIT, "Click_Finish", isChecked==true ? "true":"false");
			}
		});

		for (int i = 0; i < imageView.length; i++) {
			registerForContextMenu(imageView[i]);
			imageView[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return true;
				}
			});
		}
		imageView[0].setOnClickListener(imageViewOnClickListener);

		if (releaseVillageData == null) {
			getReleaseItem();
		}

		((VillageReleaseActivity) getSherlockActivity()).displayReleaseEditVillageActionBar();
	}

	@Override
	public void onDetach() {
		((VillageReleaseActivity) getSherlockActivity()).displayReleaseListVillageActionBar();
		super.onDetach();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof VillageReleaseActivity) {
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
				if (getSherlockActivity() instanceof VillageReleaseActivity) {
					imagePath[imageIndex] = ImageUtil.takePictureFromCameraFragment(getSherlockActivity(), VillageReleaseEditFragment.this, Constants.REQUEST_TAKE_CAPTURE);
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof VillageReleaseActivity) {
					ImageUtil.takePictureFromGalleryFragment(getSherlockActivity(), VillageReleaseEditFragment.this, Constants.REQUEST_TAKE_PICTURE);
				}
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		for (int i = 0; i < imageView.length; i++) {
			unregisterForContextMenu(imageView[i]);
		}
		super.onDestroy();
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
					imageView[imageIndex + 1].setOnClickListener(imageViewOnClickListener);
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

	public void onReleaseEditBtnClicked() {

		KfarmersAnalytics.onClick(KfarmersAnalytics.S_EXPERIENCE_MANAGE_EDIT, "Click_ActionBar-Edit", null);

		ReleaseVillageData data = new ReleaseVillageData();

		data.setCategory(listData.getSubIndex());
		data.setReleaseDateStart(releaseDateStart.getText().toString());
		data.setReleaseDateEnd(releaseDateEnd.getText().toString());
		data.setAlways(alwaysCheck.isChecked());
		data.setFinish(finishCheck.isChecked());
		data.setPrice(releasePrice.getText().toString());
		data.setReleaseNote(releaseNote.getText().toString());
		data.setImagePath(imagePath);

		CenterController.editReleaseVillage(data, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						((VillageReleaseActivity) getSherlockActivity()).finishReleaseEditFragment();
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
	}

	private void runImageRotateActivity(int takeType, String path) {
		if (getSherlockActivity() instanceof VillageReleaseActivity) {
			Intent intent = new Intent(getSherlockActivity(), ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	private void displayImagesView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof VillageReleaseActivity) {
			((VillageReleaseActivity) getSherlockActivity()).imageLoader.loadImage("file://" + path, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT),
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							imageView[imageIndex].setScaleType(ScaleType.CENTER_CROP);
							imageView[imageIndex].setImageBitmap(loadedImage);
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

	public void getReleaseItem() {
		CenterController.getReleaseVillage(listData.getSubIndex(), new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						releaseVillageData = (ReleaseVillageJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ReleaseVillageJson.class);

						if (!PatternUtil.isEmpty(releaseVillageData.ReleaseDateStart)) {
							releaseDateStart.setText(releaseVillageData.ReleaseDateStart);
						}

						if (!PatternUtil.isEmpty(releaseVillageData.ReleaseDateEnd)) {
							releaseDateEnd.setText(releaseVillageData.ReleaseDateEnd);
						}

						if (!PatternUtil.isEmpty(releaseVillageData.ReleaseNote)) {
							releaseNote.setText(releaseVillageData.ReleaseNote);
						}

						if (!PatternUtil.isEmpty(releaseVillageData.Alway)) {
							alwaysCheck.setChecked((releaseVillageData.Alway.equals("Y")) ? true : false);
						}

						if (!PatternUtil.isEmpty(releaseVillageData.Finish)) {
							finishCheck.setChecked((releaseVillageData.Finish.equals("Y")) ? true : false);
						}

						if (!PatternUtil.isEmpty(releaseVillageData.Price)) {
							releasePrice.setText(releaseVillageData.Price);
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

	private void onReleaseDateStartClicked() {
		Calendar c;

		if (TextUtils.isEmpty(releaseDateStart.getText().toString())) {
			c = Calendar.getInstance();
		} else {
			c = CommonUtil.TimeUtil.simpleDateFormat(releaseDateStart.getText().toString());
		}

		Fragment fragment = DatePickerDialogFragment.newInstance(DATE_PICKER_DIALOG_RELEASE_START, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				VillageReleaseEditFragment.TAG);
		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		((SherlockDialogFragment) fragment).show(ft, DatePickerDialogFragment.TAG);
	}

	private void onReleaseDateEndClicked() {
		Calendar c;

		if (TextUtils.isEmpty(releaseDateEnd.getText().toString())) {
			c = Calendar.getInstance();
		} else {
			c = CommonUtil.TimeUtil.simpleDateFormat(releaseDateEnd.getText().toString());
		}

		Fragment fragment = DatePickerDialogFragment.newInstance(DATE_PICKER_DIALOG_RELEASE_END, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				VillageReleaseEditFragment.TAG);
		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		((SherlockDialogFragment) fragment).show(ft, DatePickerDialogFragment.TAG);
	}

	@Override
	public void onDateSet(int index, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);

		if (index == DATE_PICKER_DIALOG_RELEASE_START) {
			releaseDateStart.setText(CommonUtil.TimeUtil.simpleDateFormat(c.getTime()));
		} else if (index == DATE_PICKER_DIALOG_RELEASE_END) {
			releaseDateEnd.setText(CommonUtil.TimeUtil.simpleDateFormat(c.getTime()));
		}
	}
}
