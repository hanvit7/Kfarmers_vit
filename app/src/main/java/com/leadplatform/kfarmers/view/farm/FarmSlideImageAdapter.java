package com.leadplatform.kfarmers.view.farm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

public class FarmSlideImageAdapter extends PagerAdapter {
	private Context context;
	private ArrayList<String> images;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public FarmSlideImageAdapter(Context context, ArrayList<String> images, ImageLoader imageLoader) {
		this.context = context;
		this.images = images;
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(200)).build();
	}
	public FarmSlideImageAdapter(Context context, ArrayList<String> images, ImageLoader imageLoader, DisplayImageOptions options) {
		this.context = context;
		this.images = images;
		this.imageLoader = imageLoader;
		this.options = options;
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
		final View v = inflater.inflate(R.layout.view_image_pager_farm, container, false);

		final ImageView image = (ImageView) v.findViewById(R.id.normalImageView);
		imageLoader.displayImage(images.get(position), image, options);

		image.setTag(position);

		image.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				int i = (int) v.getTag();
				Intent intent = new Intent(context, ImageViewActivity.class);
				intent.putExtra("pos", i);
				intent.putStringArrayListExtra("imageArrary", images);
				context.startActivity(intent);
			}
		});

		container.addView(v, container.getChildCount() > position ? position : container.getChildCount());

		return v;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((FrameLayout) object);
	}
}
