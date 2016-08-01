package com.leadplatform.kfarmers.view.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class ImageViewActivity extends Activity {

	private LinearLayout PagingLayout;
	private ArrayList<String> imageArrayList;
	private static final String ISLOCKED_ARG = "isLocked";
	private ViewPager mViewPager;
	private int pos = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imageview);

		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		PagingLayout = (LinearLayout) findViewById(R.id.Paging);

		if(getIntent().hasExtra("image")) {
			imageArrayList = new ArrayList<>();
			imageArrayList.add(getIntent().getStringExtra("image"));
		} else if(getIntent().hasExtra("imageArrary")) {
			imageArrayList = getIntent().getStringArrayListExtra("imageArrary");
		}

		if(getIntent().hasExtra("pos")) {
			pos = getIntent().getIntExtra("pos",0);
		}

		if (!ImageLoader.getInstance().isInited()) {
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
			ImageLoader.getInstance().init(config);
		}

		mViewPager.setAdapter(new ImagePagerAdapter());

		if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}
		mViewPager.setCurrentItem(pos);
		displayViewPagerIndicator(pos);

		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int position) {
				displayViewPagerIndicator(mViewPager.getCurrentItem());
			}
		});
	}

	private void displayViewPagerIndicator(int position) {

		PagingLayout.removeAllViews();

		for (int i = 0; i < imageArrayList.size(); i++) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView dot = new ImageView(this);

			if (i != 0) {
				lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(this, 3), 0, 0, 0);
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

	class ImagePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageArrayList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			ImageLoader.getInstance().displayImage(imageArrayList.get(position), photoView);
			container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
			CommonUtil.UiUtil.unbindDrawables((View) object);
			object = null;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
