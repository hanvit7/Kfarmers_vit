package com.leadplatform.kfarmers.view.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.ShopListJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.DynamicImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProductGalleyFragment extends BaseFragment {
	public static final String TAG = "ProductGalleyFragment";

	public ViewPager imageViewPager;
	private ImageLoader imageLoader;

	ArrayList<ShopListJson> items;

	public static ProductGalleyFragment newInstance(
			ArrayList<ShopListJson> items) {
		final ProductGalleyFragment f = new ProductGalleyFragment();

		final Bundle args = new Bundle();
		args.putSerializable("data", items);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			items = (ArrayList<ShopListJson>) getArguments().getSerializable(
					"data");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_product_galley,
				container, false);
		imageViewPager = (ViewPager) v.findViewById(R.id.image_viewpager);
		imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		imageViewPager.setAdapter(new SlideImageAdapter(getActivity(), items,
				imageLoader));
		imageViewPager.setPageMargin((int) getResources().getDimension(
				R.dimen.image_pager_margin));

	}

	public class SlideImageAdapter extends PagerAdapter {

		private Context context;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		private ArrayList<ShopListJson> items;

		private final DecimalFormat df = new DecimalFormat("#,###");

		public SlideImageAdapter(Context context,
				ArrayList<ShopListJson> items, ImageLoader imageLoader) {
			this.context = context;
			this.imageLoader = imageLoader;
			this.items = items;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.bitmapConfig(Config.RGB_565)
					.showImageOnLoading(R.drawable.common_dummy).build();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v = inflater.inflate(R.layout.view_image_product,
					container, false);

			DynamicImageView image = (DynamicImageView) v
					.findViewById(R.id.imageView);
			TextView title = (TextView) v.findViewById(R.id.title);
			TextView price = (TextView) v.findViewById(R.id.price);

			title.setText(items.get(position).ProductName);
			price.setText(df.format(Long.valueOf(items.get(position).Price))
					+ "원");

			imageLoader.displayImage(items.get(position).ProductImage1, image,
					options);

			v.setTag(items.get(position).IDX);

			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getSherlockActivity(),
							ProductActivity.class);
					intent.putExtra("productIndex", v.getTag().toString());
					startActivity(intent);
				}
			});

			container.addView(v, container
                    .getChildCount() > position ? position
                    : container.getChildCount());

			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((RelativeLayout) object);
			CommonUtil.UiUtil.unbindDrawables((RelativeLayout) object);
			object = null;
		}
	}
}
