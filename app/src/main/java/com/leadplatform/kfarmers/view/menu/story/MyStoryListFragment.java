package com.leadplatform.kfarmers.view.menu.story;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.StoryListJson;
import com.leadplatform.kfarmers.model.parcel.StoryListData;
import com.leadplatform.kfarmers.model.tag.LikeTag;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.diary.StoryViewActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MyStoryListFragment extends BaseRefreshMoreListFragment implements OnCloseShareDialogListener {
	public static final String TAG = "MyStoryListFragment";

	private String mLimit = "20";
	private boolean bMoreFlag = false;
	private String userIndex;
	private ArrayList<StoryListJson> myStoryItemList;
	private FarmStoryListAdapter myStoryListAdapter;
	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptionsProfile,mOptionsProduct;

	public static MyStoryListFragment newInstance(String userIndex) {
		final MyStoryListFragment f = new MyStoryListFragment();

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
		mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
		mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565)
				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
				.showImageOnLoading(R.drawable.icon_empty_profile).build();
		mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
				.build();
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
		if (myStoryListAdapter == null) {
			myStoryItemList = new ArrayList<StoryListJson>();
			myStoryListAdapter = new FarmStoryListAdapter(getSherlockActivity(), R.layout.item_diary, myStoryItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			setListAdapter(myStoryListAdapter);
			getMyStoryList(makeFarmStoryListData(true, 0));
		}
	}

	private void getMyStoryList(StoryListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			myStoryListAdapter.clear();
			myStoryListAdapter.notifyDataSetChanged();
		}

		CenterController.getUserStoryList(String.valueOf(data.getOldIndex()),mLimit, "",userIndex,"", new CenterResponseListener(getSherlockActivity()) {
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
									myStoryListAdapter.add(diary);
								}

								if (diaryCount == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;

								myStoryListAdapter.notifyDataSetChanged();
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
				//UiController.hideProgressDialogFragment(getView());
				super.onFailure(statusCode, headers, content, error);
			}
		});


		/*CenterController.getListMy(data, new CenterResponseListener(getSherlockActivity()) {
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
								myStoryListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							myStoryListAdapter.notifyDataSetChanged();
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
		});*/
	}

	private class FarmStoryListAdapter extends ArrayAdapter<StoryListJson> {
		private int itemLayoutResourceId;


		public FarmStoryListAdapter(Context context, int itemLayoutResourceId, ArrayList<StoryListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}

			RelativeLayout DiaryView = ViewHolder.get(convertView, R.id.DiaryView);
			RelativeLayout rootLayout = ViewHolder.get(convertView, R.id.Top);
			ImageView Profile = ViewHolder.get(convertView, R.id.Profile);
			TextView Farmer = ViewHolder.get(convertView, R.id.Farmer);
			TextView Category = ViewHolder.get(convertView, R.id.Category);
			ImageView Auth1 = ViewHolder.get(convertView, R.id.Auth);
			ImageView Verification = ViewHolder.get(convertView, R.id.Verification);
			TextView Date = ViewHolder.get(convertView, R.id.Date);
			TextView Description = ViewHolder.get(convertView, R.id.Description);
			ImageView imageView = ViewHolder.get(convertView, R.id.imageview);
			TextView Address = ViewHolder.get(convertView, R.id.Address);
			TextView FoodMile = ViewHolder.get(convertView, R.id.FoodMile);
			TextView LikeText = ViewHolder.get(convertView, R.id.LikeText);
			TextView ReplyText = ViewHolder.get(convertView, R.id.ReplyText);
			RelativeLayout Like = ViewHolder.get(convertView, R.id.Like);
			RelativeLayout Reply = ViewHolder.get(convertView, R.id.Reply);
			RelativeLayout Share = ViewHolder.get(convertView, R.id.Share);
			ImageButton FarmImageView = ViewHolder.get(convertView, R.id.farmImageView);
			TextView Blind = ViewHolder.get(convertView, R.id.Blind);
			LinearLayout adLayout = ViewHolder.get(convertView, R.id.AdView);

			ImageView adImage = ViewHolder.get(convertView, R.id.image);
			TextView adDes = ViewHolder.get(convertView, R.id.des);
			TextView adPrice = ViewHolder.get(convertView, R.id.price);
			TextView adDcPirce = ViewHolder.get(convertView, R.id.dcPrice);
			TextView adStar = ViewHolder.get(convertView, R.id.star);
			RatingBar adRatingBar = ViewHolder.get(convertView,R.id.ratingbar);

			if(getItem(position) instanceof StoryListJson) {
				adLayout.setVisibility(View.GONE);
				DiaryView.setVisibility(View.VISIBLE);

				Auth1.setVisibility(View.GONE);
				Verification.setVisibility(View.GONE);
				Address.setVisibility(View.GONE);
				FoodMile.setVisibility(View.GONE);
				Blind.setVisibility(View.GONE);
				FarmImageView.setVisibility(View.GONE);

				final StoryListJson story = (StoryListJson) getItem(position);

				if (story != null) {
					rootLayout.setTag(position);
					rootLayout.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							int pos = (int) v.getTag();
							StoryListJson storyListJson = getItem(pos);

							Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
							intent.putExtra("diary",storyListJson.DiaryIndex);
							intent.putExtra("name",storyListJson.Nickname);
							if(storyListJson.Keyword != null && !storyListJson.Keyword.isEmpty()) {
								intent.putExtra("type","story");
							} else {
								intent.putExtra("type","daily");
							}
							startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
						}
					});

					if(!PatternUtil.isEmpty(story.Nickname)) {
						Farmer.setText(story.Nickname);
					} else {
						Farmer.setText("");
					}

					if (!PatternUtil.isEmpty(story.ProfileImage)) {
						mImageLoader.displayImage(story.ProfileImage, Profile, mOptionsProfile);
						Profile.setVisibility(View.VISIBLE);
					} else {
						Profile.setVisibility(View.INVISIBLE);
					}

					if (!PatternUtil.isEmpty(story.Keyword)) {
						Category.setText(story.Keyword);
						Category.setVisibility(View.VISIBLE);
					} else {
						Category.setText("일상");
						//Category.setVisibility(View.INVISIBLE);
					}

					if (!PatternUtil.isEmpty(story.Date)) {
						Date.setText(story.Date);
						Date.setVisibility(View.VISIBLE);
					} else {
						Date.setVisibility(View.INVISIBLE);
					}

					if (!PatternUtil.isEmpty(story.Description)) {
						Description.setText(story.Description);
						Description.setVisibility(View.VISIBLE);
					} else {
						Description.setVisibility(View.GONE);
					}

					if (!PatternUtil.isEmpty(story.Like) && !story.Like.equals("0")) {
						LikeText.setText(story.Like);
						LikeText.setVisibility(View.VISIBLE);
					} else {
						LikeText.setVisibility(View.GONE);
					}

					if (!PatternUtil.isEmpty(story.Reply) && !story.Reply.equals("0")) {
						ReplyText.setText(story.Reply);
						ReplyText.setVisibility(View.VISIBLE);
					} else {
						ReplyText.setVisibility(View.GONE);
					}


					if (!PatternUtil.isEmpty(story.Image)) {
						mImageLoader.displayImage(story.Image, imageView, mOptionsProduct);
						imageView.setVisibility(View.VISIBLE);
						imageView.setTag(position);
						imageView.setOnClickListener(new ViewOnClickListener() {
							@Override
							public void viewOnClick(View v) {
								int pos = (int) v.getTag();
								StoryListJson storyListJson = getItem(pos);

								Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
								intent.putExtra("diary",storyListJson.DiaryIndex);
								intent.putExtra("name",storyListJson.Nickname);
								if(storyListJson.Keyword != null && !storyListJson.Keyword.isEmpty()) {
									intent.putExtra("type","story");
								} else {
									intent.putExtra("type","daily");
								}
								startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
							}
						});

					} else {
						imageView.setVisibility(View.GONE);
					}

					Like.setTag(new LikeTag(story.DiaryIndex, position));
					Like.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							final LikeTag tag = (LikeTag) v.getTag();

							KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Like", null);

							((BaseFragmentActivity) getSherlockActivity()).centerLikeUserDiary(tag.index, new OnLikeDiaryListener() {
								@Override
								public void onResult(int code, boolean plus) {
									if (code == 0) {
										int count = Integer.parseInt((((StoryListJson) getItem(tag.position)).Like));
										if (plus) {
											((StoryListJson) getItem(tag.position)).Like = String.valueOf(count + 1);
										} else {
											if (count != 0)
												((StoryListJson) getItem(tag.position)).Like = String.valueOf(count - 1);
										}
										myStoryListAdapter.notifyDataSetChanged();
									}
								}
							});
						}
					});

					Reply.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Reply", null);
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(
									ReplyActivity.REPLY_TYPE_NORMAL,
									story.Nickname,
									story.DiaryIndex);
						}
					});

					Share.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							try {
								KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(story),TAG);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
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
				getMyStoryList(makeFarmStoryListData(false, Integer.valueOf(myStoryListAdapter.getItem(myStoryListAdapter.getCount() - 1).DiaryIndex)));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getMyStoryList(makeFarmStoryListData(true, 0));
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
		Log.e(TAG, "========= onDialogSelected = position = " + position);
		try {
			StoryListJson data = (StoryListJson) JsonUtil.jsonToObject(object, StoryListJson.class);
			if (position == 0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Share", "카카오톡");
				KaKaoController.sendKakaotalk(this, data);
			} else if (position == 1) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Share", "카카오스토리");
				KaKaoController.sendKakaostory(this, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
