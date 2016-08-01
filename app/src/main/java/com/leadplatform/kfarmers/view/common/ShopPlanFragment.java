package com.leadplatform.kfarmers.view.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ExhibitionListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ShopPlanFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "ShopPlanFragment";

	private final int mLimit = 20;
	private int mPage = 1;
	private boolean mMoreFlag = false;

	private ImageLoader imageLoader;
	private DisplayImageOptions optionProduct, optionProfile;

	private String id;
	private ShopListAdapter shopListAdapter;

	private ArrayList<ExhibitionListJson> mPlanArrayList;
	private ExhibitionAdapter mExhibitionAdapter;
	private View mHeaderView;
	private AutoScrollViewPager mHeaderViewPager;

	public static ShopPlanFragment newInstance(String id) {
		final ShopPlanFragment f = new ShopPlanFragment();
		final Bundle args = new Bundle();
		args.putString("id", id);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
            id = getArguments().getString("id");
		}

		this.imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
		this.optionProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();
		this.optionProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565)
				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
				.showImageOnLoading(R.drawable.icon_empty_profile).build();

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PLAN_SHOP, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);
		mHeaderView = inflater.inflate(R.layout.item_product_header,null);
		mHeaderViewPager = (AutoScrollViewPager) mHeaderView.findViewById(R.id.ViewPagerHeader);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout,refreshListener);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

		if (shopListAdapter == null) {
			getListView().addHeaderView(mHeaderView);
			/*FrameLayout.LayoutParams plControl = (FrameLayout.LayoutParams) mHeaderView.getLayoutParams();
			plControl.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.CommonSmallPadding);
			mHeaderView.setLayoutParams(plControl);*/

			shopListAdapter = new ShopListAdapter(getSherlockActivity(), R.layout.item_product_list, new ArrayList<ProductJson>());
			setListAdapter(shopListAdapter);
			getListView().setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ProductJson data = (ProductJson) shopListAdapter.getItem(position);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_PLAN_SHOP, "Click_Item", data.name);

					Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
					intent.putExtra("productIndex", data.idx);
					startActivity(intent);

				}
			});
			getListShop(true);
		}
	}

	private void getListShop(boolean init) {

		if (init) {
			mMoreFlag = false;
			mPage = 1;
			shopListAdapter.clear();
			shopListAdapter.notifyDataSetChanged();
		}

        SnipeApiController.getProductPlan(String.valueOf(mPage),String.valueOf(mLimit),id, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 200:
							JsonNode root = JsonUtil.parseTree(content);
							List<ProductJson> arrayList = new ArrayList<ProductJson>();

							if (root.path("product").size() > 0) {
								arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("product"), ProductJson.class);
								shopListAdapter.addAll(arrayList);
							}

							if(root.path("item").size() > 0) {
								ExhibitionListJson exhibitionListJson = (ExhibitionListJson) JsonUtil.jsonToObject(root.path("item").toString(),ExhibitionListJson.class);
								mPlanArrayList = new ArrayList<ExhibitionListJson>();
								mPlanArrayList.add(exhibitionListJson);
							}

							shopListAdapter.notifyDataSetChanged();

							if(arrayList.size() == mLimit) {
								mMoreFlag = true;
							} else {
								mMoreFlag = false;
							}
							mPage++;

							if(mExhibitionAdapter == null && mPlanArrayList != null && mPlanArrayList.size() > 0) {
								mExhibitionAdapter = new ExhibitionAdapter(getActivity(),mPlanArrayList);
								mHeaderViewPager.setAdapter(mExhibitionAdapter);
								mHeaderView.setVisibility(View.VISIBLE);

								((ShopActivity)getActivity()).title.setText(mPlanArrayList.get(0).title);
							}

							break;
						default:
							UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				onRefreshComplete();
				onLoadMoreComplete();
				super.onFailure(statusCode, headers, content, error);
			}
		});
	}

	private class ShopListAdapter extends ArrayAdapter<ProductJson> {
		private int itemLayoutResourceId;


		public ShopListAdapter(Context context, int itemLayoutResourceId, ArrayList<ProductJson> items) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}
			LinearLayout root = ViewHolder.get(convertView, R.id.root);
			ImageView img = ViewHolder.get(convertView, R.id.image);
			ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);
			TextView des = ViewHolder.get(convertView, R.id.des);
			TextView price = ViewHolder.get(convertView, R.id.price);
			TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
			TextView summary = ViewHolder.get(convertView, R.id.summary);
			TextView dDay = ViewHolder.get(convertView, R.id.textDday);

			final ProductJson item = (ProductJson) getItem(position);
			if (item != null)
			{
				if(item.profile_image != null && !item.profile_image.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.profile_image, img_profile, optionProfile);
				}
				else {
					img_profile.setImageResource(0);
				}
				imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, optionProduct);

				if(item.summary != null && !item.summary.isEmpty())
				{
					summary.setVisibility(View.VISIBLE);
					summary.setText(item.summary);
				}
				else
				{
					summary.setVisibility(View.GONE);
				}

				//des.setText(item.name);
				des.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

				int itemPrice = (int)Double.parseDouble(item.price);
				int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

				if(itemPrice > itemBuyPrice)
				{
					price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
					dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
					price.setVisibility(View.VISIBLE);

					price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
				else
				{
					price.setText("");
					dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
					price.setVisibility(View.GONE);
					price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

				}

				root.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_PLAN_SHOP, "Click_Item", item.name);
						Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
						intent.putExtra("productIndex", item.idx);
						startActivity(intent);
					}
				});

				if(item.duration!= null && item.duration.startsWith("D") && !item.duration.equals("D-day")) {
					dDay.setText(item.duration);
					dDay.setVisibility(View.VISIBLE);
				} else {
					dDay.setVisibility(View.GONE);
				}
			}
			return convertView;
		}
	}

	 private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		 @Override
	 	public void onLoadMore() {
	 		if (mMoreFlag == true) {
				mMoreFlag = false;
	 			getListShop(false);
	 		} else {
	 			onLoadMoreComplete();
	 		}
	 	}
	 };

	 private OnRefreshListener refreshListener = new OnRefreshListener() {
	 	@Override
	 	public void onRefreshStarted(View view) {
		 	getListShop(true);
	 	}
	 };

	public class ExhibitionAdapter extends PagerAdapter {

		private Context context;
		private ArrayList<ExhibitionListJson> items;

		public ExhibitionAdapter(Context context, ArrayList<ExhibitionListJson> items) {
			this.context = context;
			this.items = items;
		}

		public void addAll(ArrayList<ExhibitionListJson> items) {
			this.items = items;
		}

		public void add(ExhibitionListJson item) {
			this.items.add(item);
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
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v = inflater.inflate(R.layout.view_product_plan, container, false);

			ImageView image = (ImageView) v.findViewById(R.id.imageView);

			ExhibitionListJson item = items.get(position);

			imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.picture, image, optionProduct);

			container.addView(v, container.getChildCount() > position ? position: container.getChildCount());
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
