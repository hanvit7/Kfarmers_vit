package com.leadplatform.kfarmers.view.menu.favorite;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.FavoriteListHolder;
import com.leadplatform.kfarmers.model.json.FavoriteListJson;
import com.leadplatform.kfarmers.model.parcel.FavoriteListData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FavoriteListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "FavoriteListFragment";

	private boolean bMoreFlag = false;
	private int offsetCount = 0;
	private ArrayList<FavoriteListJson> favoriteObjectList;
	private FavoriteListAdapter favoriteListAdapter;

	private String type,index;

	public static FavoriteListFragment newInstance() {
		final FavoriteListFragment f = new FavoriteListFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_FAVORITE, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (favoriteListAdapter == null) {
			favoriteObjectList = new ArrayList<FavoriteListJson>();
			favoriteListAdapter = new FavoriteListAdapter(getSherlockActivity(), R.layout.item_favorite, favoriteObjectList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			getListView().setAdapter(favoriteListAdapter);
			getListView().setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					FavoriteListJson item = (FavoriteListJson) getListView().getAdapter().getItem(position);
					Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
					intent.putExtra("userType", item.Type);
					intent.putExtra("userIndex", item.FarmerIndex);
					startActivity(intent);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_FAVORITE, "Click_Item", item.Farm);
				}
			});
			getFavoriteList(makeListFavoriteData(true, 0));
		}
	}

	private FavoriteListData makeListFavoriteData(boolean initFlag, int oldIndex) {
		FavoriteListData data = new FavoriteListData();
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}

	private void getFavoriteList(final FavoriteListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			offsetCount = 0;
			favoriteListAdapter.clear();
			favoriteListAdapter.notifyDataSetChanged();
		}

		CenterController.getListFavorite(data, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						if (root.findPath("List").isArray()) {
							int count = 0;
							Iterator<JsonNode> it = root.findPath("List").iterator();
							while (it.hasNext()) {
								FavoriteListJson favorite = (FavoriteListJson) JsonUtil.jsonToObject(it.next().toString(), FavoriteListJson.class);
								favoriteListAdapter.add(favorite);
								count++;
							}

							if (count == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;
							offsetCount += count;

							favoriteListAdapter.notifyDataSetChanged();
						}
						break;
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

	private class FavoriteListAdapter extends ArrayAdapter<FavoriteListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public FavoriteListAdapter(Context context, int itemLayoutResourceId, ArrayList<FavoriteListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 40) / 2)).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FavoriteListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new FavoriteListHolder();
				holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				holder.Delete = (ImageView) convertView.findViewById(R.id.Delete);
				holder.FarmerIcon = (ImageView) convertView.findViewById(R.id.FarmerIcon);
				holder.Farm = (TextView) convertView.findViewById(R.id.Farm);
				holder.Farmer = (TextView) convertView.findViewById(R.id.Farmer);
				holder.CategoryList = (TextView) convertView.findViewById(R.id.CategoryList);

				convertView.setTag(holder);
			} else {
				holder = (FavoriteListHolder) convertView.getTag();
			}

			FavoriteListJson item = getItem(position);

			if (item != null) {
				if (!PatternUtil.isEmpty(item.ProfileImage)) {
					imageLoader.displayImage(item.ProfileImage, holder.Profile, options);
				}

				if (!PatternUtil.isEmpty(item.Type)) {
					if (item.Type.equals("F"))
						holder.FarmerIcon.setImageResource(R.drawable.icon_favorite_farmer);
					else if (item.Type.equals("V"))
						holder.FarmerIcon.setImageResource(R.drawable.icon_favorite_village);
				} else {
					holder.FarmerIcon.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Farm)) {
					holder.Farm.setText(item.Farm);
				} else {
					holder.Farm.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.FarmerName)) {
					holder.Farmer.setText(item.FarmerName);
				} else {
					holder.Farmer.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.CategoryList)) {
					holder.CategoryList.setText(item.CategoryList);
				} else {
					holder.CategoryList.setVisibility(View.INVISIBLE);
				}

				holder.Delete.setTag(position);
				holder.Delete.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						int position = (Integer) v.getTag();
						FavoriteListJson item = getItem(position);
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_FAVORITE, "Click_Item-Delete", item.Farm);
						centerSetFavorite(item);
					}
				});
			}

			return convertView;
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getFavoriteList(makeListFavoriteData(false, offsetCount));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getFavoriteList(makeListFavoriteData(true, 0));
		}
	};

	public void centerSetFavorite(final FavoriteListJson item) {
		CenterController.setFavorite(item.Type, item.FarmerIndex, "D", new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						UiController.toastCancelFavorite(getSherlockActivity());
						favoriteListAdapter.remove(item);
						favoriteListAdapter.notifyDataSetChanged();
						break;

					case 1005:
						UiController.showDialog(getSherlockActivity(), R.string.dialog_already_favorite);
						break;
					case 1006:
						UiController.showDialog(getSherlockActivity(), R.string.dialog_my_favorite);
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

}
