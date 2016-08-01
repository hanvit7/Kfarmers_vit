package com.leadplatform.kfarmers.view.recipe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class RecipeListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "RecipeListFragment";

	private ImageLoader mImageLoader;
	private DisplayImageOptions mImageOption;

	private final int mLimit = 40;
	private  int mPage = 1;
	private boolean mMoreFlag = false;

	private String mCode;

	private ArrayList<RecipeJson> mRecipeList;

	private EventListAdapter mEventListAdapter;

	private LinearLayout mEmptyView;

	private BaseRefreshMoreListFragment.OnLoadMoreListener loadMoreListener = new BaseRefreshMoreListFragment.OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (mMoreFlag == true) {
				mMoreFlag = false;
				getDataList(false);
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getDataList(true);
		}
	};

	public static RecipeListFragment newInstance(String code) {
		final RecipeListFragment f = new RecipeListFragment();
		final Bundle args = new Bundle();
		args.putString("code",code);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mCode = getArguments().getString("code");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_RECIPE_LIST);

		mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
		mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
				.build();

		mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);

		//((ImageView)mEmptyView.findViewById(R.id.EmptyImage)).setVisibility(View.VISIBLE);

		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

		if(mEventListAdapter == null) {
			mRecipeList = new ArrayList<>();
			mEventListAdapter = new EventListAdapter(getActivity(), R.layout.item_recipe_list);
			setListAdapter(mEventListAdapter);
		}
		getDataList(true);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void getDataList(Boolean isClear) {
		if (isClear) {
			mRecipeList = new ArrayList<>();
			mMoreFlag = false;
			mPage = 1;
			mEventListAdapter.clear();
		}

		UiController.showProgressDialogFragment(getView());
		SnipeApiController.getRecipeList(String.valueOf(mPage), String.valueOf(mLimit),mCode, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					if (Code == 200) {

						JsonNode root = JsonUtil.parseTree(content);
						List<RecipeJson> arrayList = new ArrayList<>();

						if (root.path("item").size() > 0) {
							arrayList = (List<RecipeJson>) JsonUtil.jsonToArrayObject(root.path("item"), RecipeJson.class);
							mRecipeList.addAll(arrayList);
							mEventListAdapter.addAll(mRecipeList);
						}

						if(mEventListAdapter.getCount() > 0) {
							mEmptyView.setVisibility(View.GONE);
						} else {
							mEmptyView.setVisibility(View.VISIBLE);
						}

						if (arrayList.size() == mLimit)
							mMoreFlag = true;
						else
							mMoreFlag = false;
						mPage++;

					} else {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				onRefreshComplete();
				onLoadMoreComplete();
			}
		});
	}

	public class EventListAdapter extends ArrayAdapter
	{
		private  int itemLayoutResourceId;

		public EventListAdapter(Context context, int resource) {
			super(context, resource);
			itemLayoutResourceId = resource;
		}

		@Override
		public int getCount() {
			return (mRecipeList.size() / 2) + (mRecipeList.size() % 2 > 0 ? 1:0) ;
		}

		@Override
		public Object getItem(int position) {
			return mRecipeList.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}

			View item1 = ViewHolder.get(convertView, R.id.item1);
			View item2 = ViewHolder.get(convertView, R.id.item2);

			RecipeJson item = (RecipeJson) getItem(position*2);
			if( item != null ) {
				item1.setTag(item.idx);
				item1.setTag(R.integer.tag_st,item.cooking);
				ImageView imageView = ViewHolder.get(convertView, R.id.image1);
				TextView title = ViewHolder.get(convertView, R.id.title1);
				TextView name = ViewHolder.get(convertView, R.id.name1);

				if(item.land_picture != null && !item.land_picture.trim().isEmpty()) {
					mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.land_picture, imageView, mImageOption);
				} else {
					imageView.setImageResource(R.drawable.common_dummy);
				}

				title.setText(item.title);
				name.setText(item.cooking);

				item1.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {

						Intent intent = new Intent(getActivity(),RecipeViewActivity.class);
						intent.putExtra("recipe",(String)v.getTag());
						intent.putExtra("cooking",(String) v.getTag(R.integer.tag_st));
						startActivity(intent);
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_RECIPE_LIST, "Click_Item", (String) v.getTag(R.integer.tag_st));
					}
				});
			}

			if(mRecipeList.size() > (position*2)+1) {
				item = (RecipeJson) getItem((position*2)+1);
				if( item != null ) {
					item2.setTag(item.idx);
					item2.setTag(R.integer.tag_st,item.cooking);
					ImageView imageView = ViewHolder.get(convertView, R.id.image2);
					TextView title = ViewHolder.get(convertView, R.id.title2);
					TextView name = ViewHolder.get(convertView, R.id.name2);

					imageView.setImageResource(R.drawable.common_dummy);
					if(item.land_picture != null && !item.land_picture.trim().isEmpty()) {
						mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.land_picture, imageView, mImageOption);
					} else {
						imageView.setImageResource(R.drawable.common_dummy);
					}

					title.setText(item.title);
					name.setText(item.cooking);

					item2.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							Intent intent = new Intent(getActivity(),RecipeViewActivity.class);
							intent.putExtra("recipe",(String)v.getTag());
							intent.putExtra("cooking",(String) v.getTag(R.integer.tag_st));
							startActivity(intent);
						}
					});
					item2.setVisibility(View.VISIBLE);
				}
			} else {
				item2.setVisibility(View.INVISIBLE);
				convertView.setOnClickListener(null);
			}
			return convertView;
		}
	}
}
