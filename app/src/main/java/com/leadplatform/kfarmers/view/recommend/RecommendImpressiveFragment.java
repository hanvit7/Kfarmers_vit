package com.leadplatform.kfarmers.view.recommend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.DiaryListHolder;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.parcel.DiaryListData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.model.tag.LikeTag;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import org.apache.http.Header;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;
import java.util.Iterator;

public class RecommendImpressiveFragment extends BaseRefreshMoreListFragment implements OnCloseShareDialogListener {
	public static final String TAG = "RecommendImpressiveFragment";

	private boolean bMoreFlag = false;
	private ArrayList<DiaryListJson> mainItemList;
	private MainAllListAdapter mainListAdapter;

	public static RecommendImpressiveFragment newInstance() {
		final RecommendImpressiveFragment f = new RecommendImpressiveFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_recommend_impressive_list, container, false);

		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (mainListAdapter == null) {
			double latitude = AppPreferences.getLatitude(getSherlockActivity());
			double longitude = AppPreferences.getLongitude(getSherlockActivity());
			// UserDb user = DbController.queryCurrentUser(getSherlockActivity());
			mainItemList = new ArrayList<DiaryListJson>();
			mainListAdapter = new MainAllListAdapter(getSherlockActivity(), R.layout.item_recommend_impressive, mainItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader, latitude,
					longitude);
			setListAdapter(mainListAdapter);
			getListDiary(makeListDiaryData(true, 0,""));
		}
	}

	private void getListDiary(DiaryListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			mainListAdapter.clear();
			mainListAdapter.notifyDataSetChanged();
		}

		CenterController.getListDiary(data, new CenterResponseListener(getSherlockActivity()) {
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
								DiaryListJson diary = (DiaryListJson) JsonUtil.jsonToObject(it.next().toString(), DiaryListJson.class);
								mainListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

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

	private class MainAllListAdapter extends ArrayAdapter<DiaryListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;
		private double userLatitude, userLongitude;

		public MainAllListAdapter(Context context, int itemLayoutResourceId, ArrayList<DiaryListJson> items, ImageLoader imageLoader, double userLatitude, double userLongitude) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2)).showImageOnLoading(R.drawable.common_dummy).build();
			this.userLatitude = userLatitude;
			this.userLongitude = userLongitude;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DiaryListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new DiaryListHolder();
				holder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.Top);
				holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				holder.Farmer = (TextView) convertView.findViewById(R.id.Farmer);
				holder.Category = (TextView) convertView.findViewById(R.id.Category);
				holder.Auth1 = (ImageView) convertView.findViewById(R.id.Auth1);
				holder.Auth2 = (ImageView) convertView.findViewById(R.id.Auth2);
				holder.Date = (TextView) convertView.findViewById(R.id.Date);
				holder.Description = (TextView) convertView.findViewById(R.id.Description);
				holder.imageView = (ImageView) convertView.findViewById(R.id.imageview);
				holder.Address = (TextView) convertView.findViewById(R.id.Address);
				holder.FoodMile = (TextView) convertView.findViewById(R.id.FoodMile);
				holder.LikeText = (TextView) convertView.findViewById(R.id.LikeText);
				holder.ReplyText = (TextView) convertView.findViewById(R.id.ReplyText);
				holder.Like = (RelativeLayout) convertView.findViewById(R.id.Like);
				holder.Reply = (RelativeLayout) convertView.findViewById(R.id.Reply);
				holder.Share = (RelativeLayout) convertView.findViewById(R.id.Share);
				holder.Blind = (TextView) convertView.findViewById(R.id.Blind);
				//holder.ImgCount = (TextView) convertView.findViewById(R.id.ImgCount);

				convertView.setTag(holder);
			} else {
				holder = (DiaryListHolder) convertView.getTag();
			}

			final DiaryListJson diary = getItem(position);

			if (diary != null) {
				holder.rootLayout.setTag(new String(diary.Diary));
				holder.rootLayout.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						String diary = (String) v.getTag();
						Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
						intent.putExtra("diary", diary);
						intent.putExtra("type", DiaryDetailActivity.DETAIL_IMPRESSIVE);
						startActivity(intent);
					}
				});

				if (!PatternUtil.isEmpty(diary.ProfileImage)) {
					imageLoader.displayImage(diary.ProfileImage, holder.Profile, options);
					holder.Profile.setVisibility(View.VISIBLE);
				} else {
					holder.Profile.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(diary.FarmerName)) {
					holder.Farmer.setText(diary.FarmerName);
					holder.Farmer.setVisibility(View.VISIBLE);
				} else {
					holder.Farmer.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(diary.CategoryName)) {
					holder.Category.setText(diary.CategoryName);
					holder.Category.setVisibility(View.VISIBLE);
				} else {
					holder.Category.setVisibility(View.INVISIBLE);
				}

				if (diary.Auths != null) {
					holder.Auth1.setImageResource(R.drawable.icon_eco);
					holder.Auth1.setVisibility(View.VISIBLE);
					holder.Auth2.setImageResource(R.drawable.icon_impressive);
					holder.Auth2.setVisibility(View.VISIBLE);
				} else {
					holder.Auth1.setImageResource(R.drawable.icon_impressive);
					holder.Auth1.setVisibility(View.VISIBLE);
					holder.Auth2.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(diary.RegistrationDate)) {
					holder.Date.setText(diary.RegistrationDate);
					holder.Date.setVisibility(View.VISIBLE);
				} else {
					holder.Date.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(diary.Description)) {
					holder.Description.setText(diary.Description);
					holder.Description.setVisibility(View.VISIBLE);
				} else {
					holder.Description.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(diary.Like) && !diary.Like.equals("0")) {
					holder.LikeText.setText(diary.Like);
					holder.LikeText.setVisibility(View.VISIBLE);
				} else {
					holder.LikeText.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(diary.Reply) && !diary.Reply.equals("0")) {
					holder.ReplyText.setText(diary.Reply);
					holder.ReplyText.setVisibility(View.VISIBLE);
				} else {
					holder.ReplyText.setVisibility(View.GONE);
				}

				holder.Like.setTag(new LikeTag(diary.Diary, position));
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
									mainListAdapter.notifyDataSetChanged();
								}
							}
						});
					}
				});

				holder.Reply.setTag(diary);
				holder.Reply.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						DiaryListJson data = (DiaryListJson) v.getTag();
						if (data.Type.equals("F")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_FARMER, data.Farm, data.Diary);
						} else if (data.Type.equals("V")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_VILLAGE, data.Farm, data.Diary);
						}
					}
				});

				holder.Share.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						try {
							KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(diary), TAG);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				if (!PatternUtil.isEmpty(diary.Latitude) && !PatternUtil.isEmpty(diary.Longitude) && userLatitude != 0 && userLongitude != 0) {
					double lat = Double.valueOf(diary.Latitude);
					double lon = Double.valueOf(diary.Longitude);
					Location foodLocation = new Location("Food");
					Location userLocation = new Location("User");

					foodLocation.setLatitude(lat);
					foodLocation.setLongitude(lon);
					userLocation.setLatitude(userLatitude);
					userLocation.setLongitude(userLongitude);

					float meters = userLocation.distanceTo(foodLocation);
					String distance = "푸드마일 ";
					holder.FoodMile.setText(distance + (int) meters / 1000 + "km");
					holder.FoodMile.setVisibility(View.VISIBLE);

					if (!PatternUtil.isEmpty(diary.AddressKeyword1) && !PatternUtil.isEmpty(diary.AddressKeyword2)) {
						holder.Address.setText(diary.AddressKeyword1 + " > " + diary.AddressKeyword2);
						holder.Address.setVisibility(View.VISIBLE);
					} else {
						holder.Address.setVisibility(View.GONE);
					}
				} else {
					holder.Address.setVisibility(View.GONE);
					holder.FoodMile.setVisibility(View.GONE);
				}

				ArrayList<String> images = new ArrayList<String>();
				if (!PatternUtil.isEmpty(diary.ProductImage1))
					images.add(diary.ProductImage1);
				if (!PatternUtil.isEmpty(diary.ProductImage2))
					images.add(diary.ProductImage2);
				if (!PatternUtil.isEmpty(diary.ProductImage3))
					images.add(diary.ProductImage3);
				if (!PatternUtil.isEmpty(diary.ProductImage4))
					images.add(diary.ProductImage4);
				if (!PatternUtil.isEmpty(diary.ProductImage5))
					images.add(diary.ProductImage5);

				/*if (images.size() != 0) {
					holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(getSherlockActivity(), diary.Diary, DiaryDetailActivity.DETAIL_IMPRESSIVE, images, imageLoader));
					holder.imageViewPager.setPageMargin((int) getResources().getDimension(R.dimen.image_pager_margin));
					holder.imageViewPager.setVisibility(View.VISIBLE);
				} else {
					holder.imageViewPager.setVisibility(View.GONE);
				}*/
				
				if (images.size() != 0) {
					DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
							.build();
					imageLoader.displayImage(images.get(0), holder.imageView,options);
					holder.imageView.setVisibility(View.VISIBLE);
					
					holder.imageView.setTag(diary.Diary);
					holder.imageView.setTag(R.id.Tag1, diary.Farm);
					
					holder.imageView.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							
							Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
							intent.putExtra("diary", (String) v.getTag());
							intent.putExtra("farm", (String) v.getTag(R.id.Tag1));
							intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
							startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
						}
					});
				} else {
					holder.imageView.setVisibility(View.GONE);
				}
				
				if(diary.Blind.equals("Y"))
				{
					holder.Blind.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.Blind.setVisibility(View.GONE);
				}
				
				/*if(Integer.parseInt(diary.ImageCount) > 1)
				{
					holder.ImgCount.setText("+");
				}
				else
				{
					holder.ImgCount.setVisibility(View.GONE);
				}*/
			}

			return convertView;
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getListDiary(makeListDiaryData(false, Integer.valueOf(mainListAdapter.getItem(mainListAdapter.getCount() - 1).Diary),mainListAdapter.getItem(mainListAdapter.getCount() - 1).RegistrationDate2));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getListDiary(makeListDiaryData(true, 0,""));
		}
	};

	private DiaryListData makeListDiaryData(boolean initFlag, int oldIndex, String oldDate) {
		DiaryListData data = new DiaryListData();
		data.setImpressive(true);
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);
		data.setOldDate(oldDate);

		return data;
	}

	@Override
	public void onDialogListSelection(int position, String object) {
		try {
			DiaryListJson data = (DiaryListJson) JsonUtil.jsonToObject(object, DiaryListJson.class);
			if (position == 0) {
				KaKaoController.sendKakaotalk(this, data);
			} else if (position == 1) {
				KaKaoController.sendKakaostory(this, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
