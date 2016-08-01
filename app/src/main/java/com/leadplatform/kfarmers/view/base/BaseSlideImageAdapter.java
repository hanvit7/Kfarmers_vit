package com.leadplatform.kfarmers.view.base;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.menu.story.StoryDetailActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class BaseSlideImageAdapter extends PagerAdapter {
	
	public static final int NO_CLICK = 1000;
	
	private Context context;
	private Fragment fragment;
	private int requestCode;
	private String diary;
	private int type;
	private ArrayList<String> images;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private OnClickListener clickListener;
	private int tag;

	public BaseSlideImageAdapter(Context context, String diary, int type, ArrayList<String> images, ImageLoader imageLoader) {
		this.context = context;
		this.diary = diary;
		this.type = type;
		this.images = images;
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
				.build();
	}

	public BaseSlideImageAdapter(Context context, Fragment fragment, int requestCode, String diary, int type, ArrayList<String> images, ImageLoader imageLoader) {
		this.context = context;
		this.fragment = fragment;
		this.requestCode = requestCode;
		this.diary = diary;
		this.type = type;
		this.images = images;
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
				.build();
	}
	
	public BaseSlideImageAdapter(Context context, Fragment fragment, int requestCode, String diary, int type, ArrayList<String> images, ImageLoader imageLoader,OnClickListener clickListener,int tag) {
		this.context = context;
		this.fragment = fragment;
		this.requestCode = requestCode;
		this.diary = diary;
		this.type = type;
		this.images = images;
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
				.build();
		
		this.clickListener = clickListener;
		this.tag = tag;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = inflater.inflate(R.layout.view_image_pager, container, false);

		DynamicImageView image = (DynamicImageView) v.findViewById(R.id.imageView);
		imageLoader.displayImage(images.get(position), image, options);

		image.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				Intent intent = null;
				
				switch (type) {
				case DiaryDetailActivity.DETAIL_DIARY:
				case DiaryDetailActivity.DETAIL_FARMER:
				case DiaryDetailActivity.DETAIL_IMPRESSIVE:
				case DiaryDetailActivity.DETAIL_VILLAGE:
				case DiaryDetailActivity.DETAIL_FARM_STORY:
					intent = new Intent(context, DiaryDetailActivity.class);
					break;

				case StoryDetailActivity.DETAIL_CONSUMER:
				case StoryDetailActivity.DETAIL_INTERVIEW:
				case StoryDetailActivity.DETAIL_NORMAL:
					intent = new Intent(context, StoryDetailActivity.class);
					break;
				case BaseSlideImageAdapter.NO_CLICK:
					v.setTag(R.id.ClickLayout, tag);
					clickListener.onClick(v);
					default:
						break;
				}

				if (intent != null) {
					intent.putExtra("diary", diary);
					intent.putExtra("type", type);

					if (fragment == null)
						context.startActivity(intent);
					else
						fragment.startActivityForResult(intent, requestCode);
				}

			}
		});	
		
		container.addView(v, container.getChildCount() > position ? position : container.getChildCount());

		return v;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((FrameLayout) object);
		CommonUtil.UiUtil.unbindDrawables((FrameLayout) object);
		object = null;
	}
}
