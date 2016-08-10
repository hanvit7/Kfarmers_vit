package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.util.ArrayList;

public class ImageRotateActivity extends BaseFragmentActivity {
	private int takeType;
	ArrayList<String> imgPath;
	ArrayList<String> tempPath;
	private Button rotateBtn;
	private ImageView rotateImage;
	private ViewPager imageViewPager;
	
	private SlideImageAdapter adapter;
	private LinearLayout PagingLayout;

	private Button rightBtn;
	
	boolean isMulti = true;

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_image_rotate);

		initContentView(savedInstanceState);
	}

	private void initContentView(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			takeType = intent.getIntExtra("takeType", Constants.REQUEST_TAKE_PICTURE);
			
			imgPath = intent.getStringArrayListExtra("imagePath");
			
			if(imgPath == null)
			{
				imgPath = new ArrayList<String>();
				imgPath.add(intent.getStringExtra("imagePath"));
				
				isMulti = false;
			}
		}

		PagingLayout = (LinearLayout) findViewById(R.id.Paging);
		tempPath = new ArrayList<String>();
		rotateBtn = (Button) findViewById(R.id.rotate_btn);

		imageViewPager = (ViewPager) findViewById(R.id.image_viewpager);
		imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int position) {
				displayViewPagerIndicator(imageViewPager.getCurrentItem());
			}
		});
		
		imageLodingTask.execute();

		/*rotateImage = (ImageView) findViewById(R.id.rotate_image);

		if (imgPath != null && imgPath.size()>0) {
			//rotateBitmap = ImageUtil.makeBitmap(ImageUtil.getPictureByteData(imagePath), Constants.RESIZE_IMAGE_WIDTH * Constants.RESIZE_IMAGE_HEIGHT,imagePath);
			rotateBitmap = ImageUtil.makeBitmap(imgPath.get(0), Constants.RESIZE_IMAGE_WIDTH * Constants.RESIZE_IMAGE_HEIGHT);
			if(rotateBitmap != null)
			{
				rotateImage.setImageBitmap(rotateBitmap);
			}
			// rotateBitmap = ImageResizer.resize(new File(imagePath), Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT, ResizeMode.FIT_EXACT);

			// imageLoader.loadImage("file://" + imagePath, new ImageSize(Constants.RESIZE_IMAGE_WIDTH, Constants.RESIZE_IMAGE_HEIGHT), new SimpleImageLoadingListener() {
			// @Override
			// public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			// rotateBitmap = loadedImage;
			// rotateImage.setImageBitmap(rotateBitmap);
			// }
			// });
		}*/
	}
	
	private void displayViewPagerIndicator(int position) {
		PagingLayout.removeAllViews();

		for (int i = 0; i < imageViewPager.getAdapter().getCount(); i++) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView dot = new ImageView(mContext);

			if (i != 0) {
				lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(ImageRotateActivity.this, 3), 0, 0, 0);
				dot.setLayoutParams(lp);
			}

			if (i == position) {
				dot.setImageResource(R.drawable.button_farm_paging_on);
			} else {
				dot.setImageResource(R.drawable.button_farm_paging_off);
			}

			PagingLayout.addView(dot);
		}
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText(R.string.rotate_image_title);

		Button leftBtn = (Button) findViewById(R.id.actionbar_left_button);
		leftBtn.setVisibility(View.VISIBLE);
		leftBtn.setText(R.string.actionbar_cancel);
		leftBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onActionBarLeftBtnClicked();
			}
		});

		rightBtn = (Button) findViewById(R.id.actionbar_right_button);
		rightBtn.setText(R.string.actionbar_ok);
	}

	@Override
	protected void onDestroy() {
		/*if (rotateBitmap != null) {
			rotateBitmap.recycle();
			rotateBitmap = null;
		}*/
		super.onDestroy();
	}

	/***************************************************************/
	// Method
	/***************************************************************/
	public void onActionBarLeftBtnClicked() {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void onActionBarRightBtnClicked() {
		/*File file = ImageUtil.createExternalStoragePublicImageFile();
		//ImageUtil.saveBitmapFile(mContext, file, rotateBitmap);
		if (takeType == Constants.REQUEST_TAKE_CAPTURE) {
			// 원본이미지는 삭제하고 reSize 된 이미지만 남겨둔다.
			if(PatternUtil.isEmpty(imagePath))
			{
				return;
			}
			ImageUtil.deleteExternalStoragePublicImageFile(imagePath);
			
			// 킷캣에서 에러발생하기에 스캔방식 변경			
			//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(new File(imagePath))));
			MediaScannerConnection.scanFile(mContext, new String[]{ imagePath }, new String[] { "image/jpg" }, null);	
		}*/

		Intent intent = new Intent();
		//intent.putExtra("imagePath", tempPath);
		
		if(isMulti)
		{
			intent.putStringArrayListExtra("imagePath", tempPath);
		}
		else
		{
			intent.putExtra("imagePath", tempPath.get(0));
		}
		setResult(RESULT_OK, intent);
		finish();
	}

	public void onRotateBtnClicked() {
		String path = tempPath.get(imageViewPager.getCurrentItem());
		File file = new File(path);
		Bitmap bitmap = ImageUtil.rotate(BitmapFactory.decodeFile(path),90);
		ImageUtil.saveBitmapFile(mContext, file, bitmap);
		
		adapter.notifyDataSetChanged();
	}

	AsyncTask<Void, Void, Void> imageLodingTask = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(Void... params) {

			final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).build();

			UiController.showProgressDialog(ImageRotateActivity.this);

			for(String path : imgPath)
			{
				try {
					if (path.contains("http")) {
						if (imageLoader.getDiskCache().get(path) == null || !imageLoader.getDiskCache().get(path).isFile()) {
							imageLoader.loadImageSync(path, options);
						}
					}

					Bitmap rotateBitmap = ImageUtil.makeBitmap(ImageUtil.getFilePath(path), Constants.RESIZE_IMAGE_WIDTH * Constants.RESIZE_IMAGE_HEIGHT);
					File imgFile = ImageUtil.createTempImageFile(imageLoader.getDiskCache().getDirectory());
					ImageUtil.saveBitmapFile(mContext, imgFile, rotateBitmap);

					rotateBitmap.recycle();
					rotateBitmap = null;
					tempPath.add(imgFile.getAbsolutePath());
				}catch (Exception e){}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if(tempPath.size() == 0 )
			{
				finish();
			}
			adapter = new SlideImageAdapter(mContext, tempPath);
			imageViewPager.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			displayViewPagerIndicator(0);

			UiController.hideProgressDialog(ImageRotateActivity.this);

			rightBtn.setVisibility(View.VISIBLE);
			rightBtn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					onActionBarRightBtnClicked();
				}
			});

			rotateBtn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					onRotateBtnClicked();
				}
			});
		}
	};
}
