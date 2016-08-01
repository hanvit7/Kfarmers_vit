package com.leadplatform.kfarmers.view.recommend;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.Header;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.RecommendListHolder;
import com.leadplatform.kfarmers.model.json.RecommendListJson;
import com.leadplatform.kfarmers.model.parcel.RecommendListData;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreGridFragment;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class RecommendVillageFragment extends BaseRefreshMoreGridFragment {
	public static final String TAG = "RecommendVillageFragment";

	private boolean bMoreFlag = false;
	private int offsetCount = 0;
	private ArrayList<RecommendListJson> mainItemList;
	private RecommendListAdapter mainListAdapter;

	private GridView gridView;

	public static RecommendVillageFragment newInstance() {
		final RecommendVillageFragment f = new RecommendVillageFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_recommend_village_list, container, false);

		gridView = (GridView) v.findViewById(R.id.gridview);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setMoreListView(getSherlockActivity(), gridView, loadMoreListener);
		if (mainListAdapter == null) {
			mainItemList = new ArrayList<RecommendListJson>();
			mainListAdapter = new RecommendListAdapter(getSherlockActivity(), R.layout.item_recommend_village, mainItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			gridView.setAdapter(mainListAdapter);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					RecommendListJson data = (RecommendListJson) gridView.getAdapter().getItem(position);
					if (data != null) {
						Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
						intent.putExtra("userType", "V");
						intent.putExtra("userIndex", data.FarmerIndex);
						startActivity(intent);
					}
				}
			});
			getListFarmer(makeListData(true, null));
		}
	}

	private void getListFarmer(RecommendListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			offsetCount = 0;
			mainListAdapter.clear();
			mainListAdapter.notifyDataSetChanged();
		}

		CenterController.recommendList(data, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						if (root.findPath("List").isArray()) {
							int diaryCount = 0;
							Iterator<JsonNode> it = root.findPath("List").iterator();
							while (it.hasNext()) {
								diaryCount++;
								RecommendListJson diary = (RecommendListJson) JsonUtil.jsonToObject(it.next().toString(), RecommendListJson.class);
								mainListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;
							offsetCount += diaryCount;

							mainListAdapter.notifyDataSetChanged();
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

	private class RecommendListAdapter extends ArrayAdapter<RecommendListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public RecommendListAdapter(Context context, int itemLayoutResourceId, ArrayList<RecommendListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(200))
					.showImageOnLoading(R.drawable.common_dummy).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RecommendListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new RecommendListHolder();
				holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				holder.Village = (TextView) convertView.findViewById(R.id.Village);
				holder.CategoryList = (TextView) convertView.findViewById(R.id.CategoryList);

				convertView.setTag(holder);
			} else {
				holder = (RecommendListHolder) convertView.getTag();
			}

			RecommendListJson item = getItem(position);

			if (item != null) {
				if (!PatternUtil.isEmpty(item.ProfileImage)) {
					imageLoader.displayImage(item.ProfileImage, holder.Profile, options);
				} else {
					holder.Profile.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Farm)) {
					holder.Village.setText(item.Farm);
				} else {
					holder.Village.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.CategoryList)) {
					holder.CategoryList.setText(item.CategoryList);
				} else {
					holder.CategoryList.setVisibility(View.INVISIBLE);
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
				getListFarmer(makeListData(false, String.valueOf(offsetCount)));
				// getListFarmer(makeListData(false, mainListAdapter.getItem(mainListAdapter.getCount() - 1).Index));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getListFarmer(makeListData(true, null));
		}
	};

	private RecommendListData makeListData(boolean initFlag, String oldIndex) {
		RecommendListData data = new RecommendListData();
		data.setType("V");
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}
}
