package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class StoryListFragment extends BaseRefreshMoreListFragment implements OnCloseShareDialogListener {
	public static final String TAG = "StoryListFragment";//StoryListActivity에서 참조

	private String mLimit = "20";

	private boolean bMoreFlag = false;
	private MainAllListAdapter mainListAdapter;
	private String  mKeyword,mImpressive;

	private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;

	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptionsProfile,mOptionsProduct;

	public static StoryListFragment newInstance(String keyword, String Impressive) {
		final StoryListFragment f = new StoryListFragment();
		final Bundle args = new Bundle();
		args.putString("keyword", keyword);
		args.putString("impressive", Impressive);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);

        if (getArguments() != null) {
			mKeyword = getArguments().getString("keyword");
			mImpressive = getArguments().getString("impressive");
		}

		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);

        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

		mainListAdapter = new MainAllListAdapter(getSherlockActivity(), R.layout.item_diary, new ArrayList());

		mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mainListAdapter);
		mSwingBottomInAnimationAdapter.setAbsListView(getListView());

        assert mSwingBottomInAnimationAdapter.getViewAnimator() != null;
		mSwingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);

        setListAdapter(mSwingBottomInAnimationAdapter);

		getTodayTagDetailList(true,"");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_DETAIL_DIARY) {
				if (data.getBooleanExtra("delete", false)) {
					String diary = data.getStringExtra("diary");
					for (int index = 0; index < mainListAdapter.getCount(); index++) {
						StoryListJson item = (StoryListJson) mainListAdapter.getItem(index);
						if (item.DiaryIndex.equals(diary)) {
							mainListAdapter.remove(item);
							mainListAdapter.notifyDataSetChanged();
							break;
						}
					}
				}
			}
		}
	}

    private void getTodayTagDetailList(boolean isInitFlag, String oldIndex) {

		if (isInitFlag) {
			bMoreFlag = false;
			mainListAdapter.clear();
		}

		CenterController.getUserStoryList(oldIndex,mLimit, mKeyword,"",mImpressive, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {
								Iterator<JsonNode> it = root.findPath("List").iterator();
								int diaryCount = 0;
								while (it.hasNext()) {
									diaryCount++;
									StoryListJson story = (StoryListJson) JsonUtil.jsonToObject(it.next().toString(), StoryListJson.class);
									mainListAdapter.add(story);
								}

								if (diaryCount == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;
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
	}

	private class MainAllListAdapter extends ArrayAdapter {
		private int itemLayoutResourceId;

		public MainAllListAdapter(Context context, int itemLayoutResourceId, ArrayList items) {
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
					rootLayout.setTag(story.DiaryIndex);
					rootLayout.setTag(R.integer.tag_st,story.Nickname);
					rootLayout.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							String diary = (String) v.getTag();
							String name = (String) v.getTag(R.integer.tag_st);
							Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
							intent.putExtra("type","story");
							intent.putExtra("diary",diary);
							intent.putExtra("name",name);
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

						String tag ="";
						String tagArr[] = story.Keyword.split(",");
						for(int i=0; i< tagArr.length; i++) {
							tag += "#"+tagArr[i]+" ";
						}
						Category.setText(tag);
						Category.setVisibility(View.VISIBLE);

						final ArrayList<String> arrayList = new ArrayList<String>();
						Collections.addAll(arrayList, tagArr);
						Category.setTag(arrayList);
						if(arrayList.size()==1) {
							Category.setOnClickListener(new ViewOnClickListener() {
								@Override
								public void viewOnClick(View v) {
									final ArrayList<String> arrayList1 = (ArrayList<String>) v.getTag();
									Intent intent = new Intent(getSherlockActivity(), StoryListActivity.class);
									intent.putExtra("keyword", arrayList1.get(0));
									intent.putExtra("impressive", "");
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								}});
						} else if(arrayList.size()>1){
							Category.setOnClickListener(new ViewOnClickListener() {
								@Override
								public void viewOnClick(View v) {
									try {
										AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
										builder.setTitle(R.string.dialog_category_title);
										builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												arg0.dismiss();
											}
										});
										final ArrayList<String> arrayList1 = (ArrayList<String>) v.getTag();
										ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.select_dialog_item, arrayList);
										builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String str = arrayList1.get(which);
												Intent intent = new Intent(getSherlockActivity(), StoryListActivity.class);
												intent.putExtra("keyword", str);
												intent.putExtra("impressive", "");
												intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												startActivity(intent);
												dialog.dismiss();
											}
										});
										builder.create().show();
									} catch (Exception e) {
									}
								}
							});
						}

					} else {
						Category.setVisibility(View.INVISIBLE);
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
						imageView.setTag(story.DiaryIndex);
						imageView.setTag(R.integer.tag_st,story.Nickname);
						imageView.setOnClickListener(new ViewOnClickListener() {
							@Override
							public void viewOnClick(View v) {
								String diary = (String) v.getTag();
								String name = (String) v.getTag(R.integer.tag_st);
								Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
								intent.putExtra("type","story");
								intent.putExtra("diary",diary);
								intent.putExtra("name",name);
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
										mainListAdapter.notifyDataSetChanged();
									}
								}
							});
						}
					});

					Reply.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Reply", null);
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_CHATTER, story.Nickname, story.DiaryIndex);
						}
					});

					Share.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							try {
								KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(story), TAG);
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
				getTodayTagDetailList(false,((StoryListJson)mainListAdapter.getItem(mainListAdapter.getCount()-1)).DiaryIndex);
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getTodayTagDetailList(true,"");
		}
	};


	@Override
	public void onDialogListSelection(int position, String object) {
		Log.e(TAG, "========= onDialogListSelection = position = " + position);
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
