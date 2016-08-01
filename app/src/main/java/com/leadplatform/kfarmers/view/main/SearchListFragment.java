package com.leadplatform.kfarmers.view.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.SearchListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.parcel.SearchListData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

public class SearchListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "SearchListFragment";

	private int productCount = 0;

	private boolean bMoreFlag = false;
	private int offsetCount = 0;
	private String searchData;
	private ArrayList<Object> searchObjectList;
	private SearchListAdapter searchListAdapter;

	public static SearchListFragment newInstance(String searchData) {
		final SearchListFragment f = new SearchListFragment();

		final Bundle args = new Bundle();
		args.putString("searchData", searchData);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			searchData = getArguments().getString("searchData");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_search_list, container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if(getListAdapter().getItem(position).getClass() == SearchListJson.class) {
					SearchListJson item = (SearchListJson) getListAdapter().getItem(position);

					KfarmersAnalytics.onClick(KfarmersAnalytics.S_SEARCH_MAIN, "Click_Item", item.Farm);

					Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
					intent.putExtra("userType", item.Type);
					intent.putExtra("userIndex", item.FarmerIndex);
					startActivity(intent);
				} else {
					ProductJson item = (ProductJson) getListAdapter().getItem(position);

					KfarmersAnalytics.onClick(KfarmersAnalytics.S_SEARCH_MAIN, "Click_Item", item.name);

					Intent intent = new Intent(getActivity(), ProductActivity.class);
					intent.putExtra("productIndex", item.idx);
					startActivity(intent);
				}
			}
		});

		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (searchListAdapter == null) {
			searchObjectList = new ArrayList<>();
			searchListAdapter = new SearchListAdapter(getSherlockActivity(), R.layout.item_search_product_farm, searchObjectList,
					((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			setListAdapter(searchListAdapter);

			bMoreFlag = false;
			offsetCount = 0;
			searchListAdapter.clear();
			searchListProduct();
		}
	}

	private void searchListProduct() {

		SnipeApiController.getProductListKeyword(searchData, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					switch (Code) {
						case 200:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.path("item").size() > 0) {
								ArrayList<ProductJson> data = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
								productCount = data.size();
								searchListAdapter.addAll(data);
							}
							searchListDiary(makeSearchListData(true, 0, searchData));
							break;
						default:
							searchListDiary(makeSearchListData(true, 0, searchData));
							break;
					}
				} catch (Exception e) {
					searchListDiary(makeSearchListData(true, 0, searchData));
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				searchListDiary(makeSearchListData(true, 0, searchData));
			}
		});
	}

	private void searchListDiary(SearchListData data) {
		if (data == null)
			return;

		CenterController.getSearchListDiary(data, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onLoadMoreComplete();
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						if (root.findPath("List").isArray()) {
							int count = 0;
							Iterator<JsonNode> it = root.findPath("List").iterator();
							while (it.hasNext()) {
								count++;
								SearchListJson search = (SearchListJson) JsonUtil.jsonToObject(it.next().toString(), SearchListJson.class);
								searchListAdapter.add(search);
							}

							if (count == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;
							offsetCount += count;

							searchListAdapter.notifyDataSetChanged();
						}
						break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				onLoadMoreComplete();
				super.onFailure(statusCode, headers, content, error);
			}
		});
	}

	private SearchListData makeSearchListData(boolean initFlag, int oldIndex, String search) {
		SearchListData data = new SearchListData();

		data.setInitFlag(initFlag);
		data.setOffset(oldIndex);
		data.setSearch(search);

		return data;
	}

	private class SearchListAdapter extends ArrayAdapter<Object> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions profileOptions;
		private DisplayImageOptions options;

		public SearchListAdapter(Context context, int itemLayoutResourceId, ArrayList<Object> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.profileOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
					.showImageOnLoading(R.drawable.icon_empty_profile).build();
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(200)).showImageOnLoading(R.drawable.common_dummy).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}
			if(getItem(position).getClass() == SearchListJson.class) {
				View productView = ViewHolder.get(convertView, R.id.productView);
				View farmView = ViewHolder.get(convertView, R.id.farmView);

				productView.setVisibility(View.GONE);
				farmView.setVisibility(View.VISIBLE);

				TextView farmTextView = ViewHolder.get(convertView, R.id.farmTextView);

				if(position == productCount) {
					farmTextView.setVisibility(View.VISIBLE);
				} else {
					farmTextView.setVisibility(View.GONE);
				}

				final SearchListJson item = (SearchListJson) getItem(position);
				if (item != null) {
					ImageView profile = ViewHolder.get(convertView, R.id.Profile);
					ImageButton productImageView = ViewHolder.get(convertView, R.id.productImageView);
					TextView Farm = ViewHolder.get(convertView, R.id.Farm);
					TextView Address = ViewHolder.get(convertView, R.id.Address);

					if (!PatternUtil.isEmpty(item.ProfileImage)) {
						imageLoader.displayImage(item.ProfileImage, profile, profileOptions);
					}

					// if (!PatternUtil.isEmpty(item.ProductImage)) {
					// imageLoader.displayImage(item.ProductImage, holder.ProductImage, options);
					// } else {
					// holder.ProductImage.setVisibility(View.INVISIBLE);
					// }

					if (item.ProductFlag2.equals("T")) {
						productImageView.setVisibility(View.VISIBLE);
						productImageView.setFocusable(false);
						productImageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);

								KfarmersAnalytics.onClick(KfarmersAnalytics.S_SEARCH_MAIN, "Click_Item-FarmerShop", item.Farm);

								intent.putExtra("id", item.FarmerId);
								intent.putExtra("name", item.Farm);
								intent.putExtra("type",ShopActivity.type.Farm);
								startActivity(intent);
							}
						});
					} else {
						productImageView.setVisibility(View.GONE);
						productImageView.setFocusable(false);
					}

					if (!PatternUtil.isEmpty(item.Farm)) {
						Farm.setText(item.Farm);
					} else {
						Farm.setVisibility(View.INVISIBLE);
					}

				/*if (!PatternUtil.isEmpty(item.FarmerName)) {
					holder.Category.setText(item.FarmerName);
				} else {
					holder.Category.setVisibility(View.INVISIBLE);
				}*/

					if (!PatternUtil.isEmpty(item.AddressKeyword1) && !PatternUtil.isEmpty(item.AddressKeyword2)) {
						Address.setText(item.AddressKeyword1 + " > " + item.AddressKeyword2);
					} else {
						Address.setVisibility(View.GONE);
					}
				}
			} else {
				View productView = ViewHolder.get(convertView, R.id.productView);
				View farmView = ViewHolder.get(convertView, R.id.farmView);

				productView.setVisibility(View.VISIBLE);
				farmView.setVisibility(View.GONE);

				TextView productTextView = ViewHolder.get(convertView, R.id.productTextView);

				if(position>0) {
					productTextView.setVisibility(View.GONE);
				} else if(position==0){
					productTextView.setVisibility(View.VISIBLE);
				}
				ProductJson item = (ProductJson) getItem(position);
				if(item != null) {

					LinearLayout root = ViewHolder.get(convertView, R.id.root);

					ImageView img = ViewHolder.get(convertView, R.id.image);
					ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);

					TextView des = ViewHolder.get(convertView, R.id.des);
					TextView price = ViewHolder.get(convertView, R.id.price);
					TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
					TextView summary = ViewHolder.get(convertView, R.id.summary);
					TextView dDay = ViewHolder.get(convertView, R.id.textDday);
					TextView productType = ViewHolder.get(convertView, R.id.productType);

					img_profile.setVisibility(View.GONE);

					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, options);

					if(item.summary != null && !item.summary.isEmpty())
					{
						summary.setVisibility(View.VISIBLE);
						summary.setText(item.summary);
					}
					else
					{
						summary.setVisibility(View.GONE);
					}

					productType.setVisibility(View.VISIBLE);
					if(item.verification.equals(ProductActivity.TYPE3)) {
						productType.setText("일반");
						productType.setBackgroundResource(R.color.minicart_cart_enabled);
						productType.setVisibility(View.VISIBLE);
					} else {
						productType.setText("검증");
						productType.setBackgroundResource(R.color.pink);
						productType.setVisibility(View.GONE);
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
						price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

					}

					if(item.duration.startsWith("D") && !item.duration.equals("D-day")) {
						dDay.setText(item.duration);
						dDay.setVisibility(View.VISIBLE);
					} else {
						dDay.setVisibility(View.GONE);
					}
				}
			}
			return convertView;
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				searchListDiary(makeSearchListData(false, offsetCount, searchData));
			} else {
				onLoadMoreComplete();
			}
		}
	};
}
