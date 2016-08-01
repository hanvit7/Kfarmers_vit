package com.leadplatform.kfarmers.view.common;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.ImageUtil;

public class SlideImageAdapter extends PagerAdapter {
	private Context context;
	private ArrayList<String> images;
	
	public SlideImageAdapter(Context context, ArrayList<String> images ) {
		this.context = context;
		this.images = images;
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
		final View v = inflater.inflate(R.layout.view_image_pager_slide, container, false);

		ImageView image = (ImageView) v.findViewById(R.id.imageView);
		
		
		Bitmap bitmap = BitmapFactory.decodeFile(images.get(position));

		if(bitmap != null)
		{
			image.setImageBitmap(bitmap);
		}
		
		/*ImageView image = (ImageView) v.findViewById(R.id.normalImageView);
		imageLoader.displayImage(images.get(position), image, options);*/

		container.addView(v, container.getChildCount() > position ? position : container.getChildCount());

		return v;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((FrameLayout) object);
		CommonUtil.UiUtil.unbindDrawables((FrameLayout) object);
		object = null;
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	
}
