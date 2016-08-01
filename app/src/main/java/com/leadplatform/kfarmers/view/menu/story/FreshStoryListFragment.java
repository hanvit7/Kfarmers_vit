package com.leadplatform.kfarmers.view.menu.story;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.Header;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.StoryListHolder;
import com.leadplatform.kfarmers.model.json.StoryListJson;
import com.leadplatform.kfarmers.model.parcel.StoryListData;
import com.leadplatform.kfarmers.model.tag.LikeTag;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.BaseSlideImageAdapter;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FreshStoryListFragment extends BaseRefreshMoreListFragment implements OnCloseShareDialogListener {
	public static final String TAG = "FreshStoryListFragment";

	private boolean bMoreFlag = false;
	private String userIndex;
	private ArrayList<StoryListJson> freshStoryItemList;
	private FarmStoryListAdapter freshStoryListAdapter;

	public static FreshStoryListFragment newInstance(String userIndex) {
		final FreshStoryListFragment f = new FreshStoryListFragment();

		final Bundle args = new Bundle();
		args.putString("userIndex", userIndex);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			userIndex = getArguments().getString("userIndex");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_story_list, container, false);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (freshStoryListAdapter == null) {
			freshStoryItemList = new ArrayList<StoryListJson>();
			freshStoryListAdapter = new FarmStoryListAdapter(getSherlockActivity(), R.layout.item_story, freshStoryItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			setListAdapter(freshStoryListAdapter);
			getFreshStoryList(makeFarmStoryListData(true, 0));
		}
	}

	private void getFreshStoryList(StoryListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			freshStoryListAdapter.clear();
			freshStoryListAdapter.notifyDataSetChanged();
		}

		CenterController.getListFresh(data, new CenterResponseListener(getSherlockActivity()) {
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
								StoryListJson diary = (StoryListJson) JsonUtil.jsonToObject(it.next().toString(), StoryListJson.class);
								freshStoryListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							freshStoryListAdapter.notifyDataSetChanged();
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

	private class FarmStoryListAdapter extends ArrayAdapter<StoryListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public FarmStoryListAdapter(Context context, int itemLayoutResourceId, ArrayList<StoryListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2)).showImageOnLoading(R.drawable.common_dummy).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			StoryListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new StoryListHolder();
				holder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.Top);
				holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				holder.Farmer = (TextView) convertView.findViewById(R.id.Farmer);
				holder.Date = (TextView) convertView.findViewById(R.id.Date);
				holder.Description = (TextView) convertView.findViewById(R.id.Description);
				holder.imageViewPager = (ViewPager) convertView.findViewById(R.id.image_viewpager);
				holder.LikeText = (TextView) convertView.findViewById(R.id.LikeText);
				holder.ReplyText = (TextView) convertView.findViewById(R.id.ReplyText);
				holder.Like = (RelativeLayout) convertView.findViewById(R.id.Like);
				holder.Reply = (RelativeLayout) convertView.findViewById(R.id.Reply);
				holder.Share = (RelativeLayout) convertView.findViewById(R.id.Share);

				convertView.setTag(holder);
			} else {
				holder = (StoryListHolder) convertView.getTag();
			}

			final StoryListJson item = getItem(position);

			if (item != null) {
				holder.rootLayout.setTag(new String(item.DiaryIndex));
				holder.rootLayout.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						String diary = (String) v.getTag();
						Intent intent = new Intent(getSherlockActivity(), StoryDetailActivity.class);
						intent.putExtra("diary", diary);
						intent.putExtra("type", StoryDetailActivity.DETAIL_NORMAL);
						startActivity(intent);
					}
				});

				if (!PatternUtil.isEmpty(item.ProfileImage)) {
					imageLoader.displayImage(item.ProfileImage, holder.Profile, options);
				} else {
					holder.Profile.setImageResource(R.drawable.icon_empty_profile);
				}

				if (!PatternUtil.isEmpty(item.Nickname)) {
					holder.Farmer.setText(item.Nickname);
				} else {
					holder.Farmer.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Date)) {
					holder.Date.setText(item.Date);
				} else {
					holder.Date.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Description)) {
					holder.Description.setText(item.Description);
				} else {
					holder.Description.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Like) && !item.Like.equals("0")) {
					holder.LikeText.setVisibility(View.VISIBLE);
					holder.LikeText.setText(item.Like);
				} else {
					holder.LikeText.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(item.Reply) && !item.Reply.equals("0")) {
					holder.ReplyText.setVisibility(View.VISIBLE);
					holder.ReplyText.setText(item.Reply);
				} else {
					holder.ReplyText.setVisibility(View.GONE);
				}

				holder.Like.setTag(new LikeTag(item.DiaryIndex, position));
				holder.Like.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						final LikeTag tag = (LikeTag) v.getTag();
						((BaseFragmentActivity) getSherlockActivity()).centerLikeDiary(tag.index, new OnLikeDiaryListener() {
							@Override
							public void onResult(int code, boolean plus) {
								if (code == 0) {
									int count = Integer.parseInt(getItem(tag.position).Like);
									if (plus) {
										getItem(tag.position).Like = String.valueOf(count + 1);
									} else {
										if (count != 0)
											getItem(tag.position).Like = String.valueOf(count - 1);
									}
									freshStoryListAdapter.notifyDataSetChanged();
								}
							}
						});
					}
				});

				holder.Reply.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						if (item.Auth.equals("C")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_CONSUMER, item.Nickname, item.DiaryIndex);
						} else if (item.Auth.equals("D")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_INTERVIEW, item.Nickname, item.DiaryIndex);
						} else if (item.Auth.equals("N")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_NORMAL, item.Nickname, item.DiaryIndex);
						}
					}
				});

				holder.Share.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						// try
						// {
						// KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(item), TAG);
						// }
						// catch (Exception e)
						// {
						// e.printStackTrace();
						// }
					}
				});

				ArrayList<String> images = new ArrayList<String>();
				if (!PatternUtil.isEmpty(item.ProductImage1))
					images.add(item.ProductImage1);
				if (!PatternUtil.isEmpty(item.ProductImage2))
					images.add(item.ProductImage2);
				if (!PatternUtil.isEmpty(item.ProductImage3))
					images.add(item.ProductImage3);
				if (!PatternUtil.isEmpty(item.ProductImage4))
					images.add(item.ProductImage4);
				if (!PatternUtil.isEmpty(item.ProductImage5))
					images.add(item.ProductImage5);

				if (images.size() != 0) {
					holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(getSherlockActivity(), item.DiaryIndex, StoryDetailActivity.DETAIL_NORMAL, images, imageLoader));
					holder.imageViewPager.setPageMargin((int) getResources().getDimension(R.dimen.image_pager_margin));
					holder.imageViewPager.setVisibility(View.VISIBLE);
				} else {
					holder.imageViewPager.setVisibility(View.GONE);
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
				getFreshStoryList(makeFarmStoryListData(false, Integer.valueOf(freshStoryListAdapter.getItem(freshStoryListAdapter.getCount() - 1).DiaryIndex)));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getFreshStoryList(makeFarmStoryListData(true, 0));
		}
	};

	private StoryListData makeFarmStoryListData(boolean initFlag, int oldIndex) {
		StoryListData data = new StoryListData();
		data.setUserIndex(userIndex);
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}

	@Override
	public void onDialogListSelection(int position, String object) {
		// try
		// {
		// StoryListJson data = (StoryListJson) JsonUtil.jsonToObject(object, StoryListJson.class);
		// if (position == 0)
		// {
		// KaKaoController.sendKakaotalk(this, data);
		// }
		// else if (position == 1)
		// {
		// KaKaoController.sendKakaostory(this, data);
		// }
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
	}
}
