package com.leadplatform.kfarmers.view.farm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.json.FarmJson;
import com.leadplatform.kfarmers.model.json.FavoriteJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.parcel.DiaryListData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.model.tag.LikeTag;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.join.EditFarmerActivity;
import com.leadplatform.kfarmers.view.join.EditVillageActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.menu.favorite.FavoriteActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

public class FarmFragment extends BaseRefreshMoreListFragment implements OnCloseCategoryDialogListener, OnCloseShareDialogListener {
	public static final String TAG = "FarmFragment";

	private ImageLoader imageLoader;
	private boolean bMoreFlag = false;
	public FarmJson farmData;
	private String userType, userIndex,userCategory;
	private View headerView;
	private ViewPager imageViewPager;
	private ImageView Profile, Introduction, Location, Phone, More, Blog; //SettingBtn;
	private TextView Farmer, CategoryName, ReleaseDate;  //mainTitleText
	private RelativeLayout CategoryLayout , ProductLayout;
	private LinearLayout PagingLayout;
	private int categoryIndex = 0;
	private ArrayList<String> categoryList;
	//private ArrayList<String> categoryTitleList;
	private ArrayList<CategoryJson> categoryObjectList;
	private ArrayList<DiaryListJson> farmObjectList;
	private FarmListAdapter farmListAdapter;
	
	private ProfileJson profileData;

	//private QuickReturnListViewOnScrollListener mQuicScrollListener;
	private LinearLayout bottomLayout;
	private LinearLayout leftLayout;
	private LinearLayout leftImgLayout;
	private LinearLayout mFavoriteLayout;
	private TextView mFavoriteCount;
	private TextView mFavoriteTitle;

	private ArrayList<FavoriteJson> mFavoriteList;

	private int favoriteCount = 0;
	private boolean isEdit = false;

	public static FarmFragment newInstance(String userType, String userIndex, String userCategory) {
		final FarmFragment f = new FarmFragment();

		final Bundle args = new Bundle();
		args.putString("userType", userType);
		args.putString("userIndex", userIndex);
		args.putString("userCategory", userCategory);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			userType = getArguments().getString("userType");
			userIndex = getArguments().getString("userIndex");
			userCategory = getArguments().getString("userCategory");
		}

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_FARM);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_farm, container, false);
		headerView = inflater.inflate(R.layout.fragment_farm_header, null, false);

		imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;

		imageViewPager = (ViewPager) headerView.findViewById(R.id.image_viewpager);
		Profile = (ImageView) headerView.findViewById(R.id.Profile);
		Introduction = (ImageView) headerView.findViewById(R.id.Introduction);
		Location = (ImageView) headerView.findViewById(R.id.Location);
		Phone = (ImageView) headerView.findViewById(R.id.Phone);
//		More = (ImageView) headerView.findViewById(R.id.More);
//		Blog = (ImageView) headerView.findViewById(R.id.Blog);
		Farmer = (TextView) headerView.findViewById(R.id.Farmer);
		CategoryName = (TextView) headerView.findViewById(R.id.CategoryName);
		ReleaseDate = (TextView) headerView.findViewById(R.id.ReleaseDate);
		CategoryLayout = (RelativeLayout) headerView.findViewById(R.id.Category);
		ProductLayout = (RelativeLayout) headerView.findViewById(R.id.Product);
		PagingLayout = (LinearLayout) headerView.findViewById(R.id.Paging);

		bottomLayout = (LinearLayout) headerView.findViewById(R.id.bottomLayout);
		leftLayout = (LinearLayout) headerView.findViewById(R.id.leftLayout);
		leftImgLayout = (LinearLayout) headerView.findViewById(R.id.leftImgLayout);

		mFavoriteLayout = (LinearLayout) headerView.findViewById(R.id.favoriteLayout);
		mFavoriteCount = (TextView) headerView.findViewById(R.id.favoriteText);
		mFavoriteTitle = (TextView) headerView.findViewById(R.id.favoriteTitleText);

		//SettingBtn = (ImageView) headerView.findViewById(R.id.setting_btn);
		//mainTitleText = (TextView) headerView.findViewById(R.id.mainTitleText);

		// setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().addHeaderView(headerView);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

		if (farmListAdapter == null) {
			double latitude = AppPreferences.getLatitude(getSherlockActivity());
			double longitude = AppPreferences.getLongitude(getSherlockActivity());
			farmObjectList = new ArrayList<DiaryListJson>();
			farmListAdapter = new FarmListAdapter(getSherlockActivity(), R.layout.item_farm_diary, farmObjectList, latitude, longitude);

			/*int bottomHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonLargeRow);
			mQuicScrollListener = new QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
					.footer(bottomLayout)
					.minFooterTranslation(bottomHeight)
					.isSnappable(true)
					.build();
			mQuicScrollListener.registerExtraOnScrollListener(onScrollListener);
			getListView().setOnScrollListener(mQuicScrollListener);*/
			setListAdapter(farmListAdapter);
		}

		CategoryLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onCategoryBtnClicked();
			}
		});
		ProductLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {

				if (farmData.ProductFlag2.equals("F")) {
					UiController.showDialog(getActivity(), R.string.dialog_no_product);
					return;
				}

				KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Farmer-Shop", farmData.Farm);
				Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);
				intent.putExtra("id", farmData.FarmerId);
				intent.putExtra("name", farmData.Farm);
				intent.putExtra("type", ShopActivity.type.Farm);
				startActivity(intent);
			}
		});

		mFavoriteLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				addFavorite();
			}
		});

		leftLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {

				if (favoriteCount > 0) {
					Intent intent = new Intent(getActivity(), FavoriteActivity.class);
					intent.putExtra("type", FavoriteActivity.Type.farm);
					intent.putExtra("index", farmData.FarmerIndex);
					intent.putExtra("id", farmData.FarmerId);
					intent.putExtra("userType", farmData.FarmerType);
					startActivity(intent);
				} else {
					addFavorite();
					/*UiDialog.showDialog(getActivity(),"관심농부", "관심 가져 주실래요?", R.string.dialog_ok, R.string.dialog_close, new CustomDialogListener() {
						@Override
						public void onDialog(int type) {
							if (UiDialog.DIALOG_POSITIVE_LISTENER == type) {
								addFavorite();
							}
						}
					});*/
				}
			}
		});

		/*mFavoriteCount.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				Intent intent = new Intent(getActivity(), FavoriteActivity.class);
				intent.putExtra("type", FavoriteActivity.Type.farm);
				intent.putExtra("index", farmData.FarmerIndex);
				intent.putExtra("id", farmData.FarmerId);
				intent.putExtra("userType", farmData.FarmerType);
				startActivity(intent);
			}
		});*/
	}

	public void addFavorite() {
		if (AppPreferences.getLogin(getActivity())) {
			CenterController.setFavorite(userType, userIndex, "I", new CenterResponseListener(getActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					try {
						switch (Code) {
							case 0000:
								String type = JsonUtil.parseTree(content).path("Type").asText();
								if(type.equals("I")) {
									UiController.toastAddFavorite(getActivity());
									favoriteCount = favoriteCount + 1;
									mFavoriteCount.setText(String.valueOf(favoriteCount) + "명");
								} else {
									UiController.toastCancelFavorite(getActivity());
									favoriteCount = favoriteCount - 1;
									mFavoriteCount.setText(String.valueOf(favoriteCount) + "명");
								}
								getFarmFavorite();
								break;
							case 1005:
								UiController.showDialog(getActivity(), R.string.dialog_already_favorite);
								break;
							case 1006:
								UiController.showDialog(getActivity(), R.string.dialog_my_favorite);
								break;
							default:
								UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
					}
				}
			});
		} else {
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (farmData == null || isEdit)
		{
			getFarmData(userType, userIndex);
			isEdit = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_DETAIL_DIARY) {
				if (data.getBooleanExtra("delete", false)) {
					String diary = data.getStringExtra("diary");
					for (int index = 0; index < farmListAdapter.getCount(); index++) {
						DiaryListJson item = farmListAdapter.getItem(index);
						if (item.Diary.equals(diary)) {
							farmListAdapter.remove(item);
							farmListAdapter.notifyDataSetChanged();
						}
					}
				}
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void getFarmData(String userType, String userIndex) {
		CenterController.getFarm(userType, userIndex, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							farmData = (FarmJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), FarmJson.class);
							getFarmFavorite();
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

	private void getFarmFavorite() {
		CenterController.getListFavoriteFarm(farmData.FarmerType, farmData.FarmerIndex, "", new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							mFavoriteList = (ArrayList<FavoriteJson>) JsonUtil.jsonToArrayObject(root.findPath("List"),FavoriteJson.class);

							if (farmData != null) {
								displayFarmHeaderView(farmData);
								getListDiary(makeListDiaryData(true, 0, ""));
								if(mFavoriteList != null && mFavoriteList.size()>0)
									favoriteCount = root.findPath("Count").asInt();
									makeFavoriteView();
							}
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

	private void makeFavoriteView() {

		((ImageView)headerView.findViewById(R.id.arrowImage)).setColorFilter(Color.WHITE);
		leftImgLayout.removeAllViews();
		if(favoriteCount > 0) {
			mFavoriteCount.setText(String.valueOf(favoriteCount) + "명");
			mFavoriteCount.setVisibility(View.VISIBLE);
			mFavoriteTitle.setText("관심있는");
			((ImageView)headerView.findViewById(R.id.arrowImage)).setVisibility(View.VISIBLE);
		} else {
			mFavoriteCount.setVisibility(View.GONE);
			mFavoriteTitle.setText("관심가져 주실래요?");
			((ImageView)headerView.findViewById(R.id.arrowImage)).setVisibility(View.VISIBLE);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				int width = leftImgLayout.getWidth();
				int pt = CommonUtil.DIPManager.dip2px(30,getActivity());
				int margin = CommonUtil.DIPManager.dip2px(5,getActivity());
				int count = width/(pt+margin);
				if(count == 0) count = count+1;

				final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pt,pt);
				layoutParams.setMargins(0,0,margin,0);

				final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
						.bitmapConfig(Config.RGB_565)
						.displayer(new RoundedBitmapDisplayer(pt / 2)) //pt
						.showImageOnLoading(R.drawable.icon_empty_profile).build();

				if(mFavoriteList.size() == 0) {
					ImageView imageView = new ImageView(getActivity());
					imageView.setBackgroundResource(R.drawable.common_dummy_circle);
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
					imageView.setPadding(3, 3, 3, 3);
					String loadedImage= "drawable://" + R.drawable.icon_empty_profile;
					imageLoader.displayImage(loadedImage, imageView, options);
					leftImgLayout.addView(imageView, layoutParams);
				} else {
					for(int i=0 ; i < count ; i++) {
						if(mFavoriteList.size() <= i) break;
						ImageView imageView = new ImageView(getActivity());
						imageView.setBackgroundResource(R.drawable.common_dummy_circle);
						imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						imageView.setPadding(3,3,3,3);

						String image = "";
						if(mFavoriteList.get(i).ProfileImage == null || mFavoriteList.get(i).ProfileImage.trim().isEmpty()) {
							image = "drawable://" + R.drawable.icon_empty_profile;
						} else {
							image = mFavoriteList.get(i).ProfileImage;
						}
						imageLoader.displayImage(image, imageView, options, new ImageLoadingListener() {
							@Override
							public void onLoadingStarted(String imageUri, View view) {}
							@Override
							public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
								String loadedImage= "drawable://" + R.drawable.icon_empty_profile;
								imageLoader.displayImage(loadedImage, (ImageView) view, options);
							}
							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {}
							@Override
							public void onLoadingCancelled(String imageUri, View view) {}
						});
						leftImgLayout.addView(imageView, layoutParams);
					}
				}
			}
		},100);


		/*for(int i=0; i< mFavoriteList.size(); i++) {
			if(mFavoriteList.get(i).ProfileImage == null || mFavoriteList.get(i).ProfileImage.trim().isEmpty()) {
			} else {
				imageLoader.displayImage(mFavoriteList.get(i).ProfileImage, mfavoriteImage, options, new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {}
					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						String loadedImage= "drawable://" + R.drawable.bt_profile;
						imageLoader.displayImage(loadedImage, (ImageView) view, options);
					}
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {}
					@Override
					public void onLoadingCancelled(String imageUri, View view) {}
				});
				break;
			}
		}*/
	}

	private void getListDiary(DiaryListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			farmListAdapter.clear();
			farmListAdapter.notifyDataSetChanged();
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
								farmListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							farmListAdapter.notifyDataSetChanged();
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

	private class FarmListAdapter extends ArrayAdapter<DiaryListJson> {
		private int itemLayoutResourceId;
		// private DisplayImageOptions options;
		DisplayImageOptions options;
		private double userLatitude, userLongitude;

		public FarmListAdapter(Context context, int itemLayoutResourceId, ArrayList<DiaryListJson> items,
				double userLatitude, double userLongitude) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			// this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			// .bitmapConfig(Config.RGB_565).displayer(new CustomRoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(),
			// 50)/2)).showImageOnLoading(R.drawable.common_dummy).build();
			this.userLatitude = userLatitude;
			this.userLongitude = userLongitude;
			options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DiaryListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new DiaryListHolder();
				holder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.Top);
				// holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				// holder.Farmer = (TextView) convertView.findViewById(R.id.Farmer);
				holder.Category = (TextView) convertView.findViewById(R.id.Category);
				holder.Auth1 = (ImageView) convertView.findViewById(R.id.Auth);
				holder.Verification = (ImageView) convertView.findViewById(R.id.Verification);
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
				holder.FarmImageView = (ImageButton) convertView.findViewById(R.id.farmImageView);
				holder.Blind = (TextView) convertView.findViewById(R.id.Blind);
				//holder.ImgCount = (TextView) convertView.findViewById(R.id.ImgCount);

				convertView.setTag(holder);
			} else {
				holder = (DiaryListHolder) convertView.getTag();
			}

			final DiaryListJson diary = getItem(position);

			if (diary != null) {
				holder.rootLayout.setTag(new String(diary.Diary));
				holder.rootLayout.setTag(R.id.Tag1, diary.Farm);
				
				holder.rootLayout.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {

						KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Item", farmData.Farm);
						Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
						intent.putExtra("diary", (String) v.getTag());
						intent.putExtra("farm", (String) v.getTag(R.id.Tag1));
						intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
						startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
					}
				});

				// if (!PatternUtil.isEmpty(diary.ProfileImage))
				// {
				// imageLoader.displayImage(diary.ProfileImage, holder.Profile, options);
				// }
				// else
				// {
				// holder.Profile.setVisibility(View.INVISIBLE);
				// }

				// if (!PatternUtil.isEmpty(diary.FarmerName))
				// {
				// holder.Farmer.setText(diary.FarmerName);
				// }
				// else
				// {
				// holder.Farmer.setVisibility(View.INVISIBLE);
				// }

				if (!PatternUtil.isEmpty(diary.CategoryName)) {
					holder.Category.setText(diary.CategoryName);
					holder.Category.setVisibility(View.VISIBLE);
				} else {
					holder.Category.setVisibility(View.INVISIBLE);
				}

				if (diary.Auths != null) {
					holder.Auth1.setVisibility(View.VISIBLE);
				} else {
					holder.Auth1.setVisibility(View.GONE);
				}
				if(!diary.Verification.equals("N"))
				{
					holder.Verification.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.Verification.setVisibility(View.GONE);
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
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Item-Like", farmData.Farm);
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
									farmListAdapter.notifyDataSetChanged();
								}
							}
						});
					}
				});

				holder.Reply.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Item-Reply", farmData.Farm);
						if (diary.Type.equals("F")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_FARMER, diary.Farm, diary.Diary);
						} else if (diary.Type.equals("V")) {
							((BaseFragmentActivity) getSherlockActivity())
									.runReplyActivity(ReplyActivity.REPLY_TYPE_VILLAGE, diary.Farm, diary.Diary);
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
					String distance = (userType.equals("V")) ? "거리 " : "푸드마일 ";
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
					holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(getSherlockActivity(), FarmFragment.this,
							Constants.REQUEST_DETAIL_DIARY, diary.Diary, DiaryDetailActivity.DETAIL_DIARY, images, imageLoader));
					holder.imageViewPager.setPageMargin((int) getResources().getDimension(R.dimen.image_pager_margin));
					holder.imageViewPager.setVisibility(View.VISIBLE);
				} else {
					holder.imageViewPager.setVisibility(View.GONE);
				}*/
				
				if (images.size() != 0) {
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

/*				if (diary.ProductFlag != null && diary.ProductFlag.equals("Y") && userType.equals("F")) {
					holder.FarmImageView.setVisibility(View.VISIBLE);
					holder.FarmImageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);
							intent.putExtra("FarmerIndex", diary.FarmerIndex);
							intent.putExtra("Farm", diary.Farm);
							startActivity(intent);
						}
					});
				} else {
					holder.FarmImageView.setVisibility(View.GONE);
				}*/
				
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
	
    /*@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.menu_farm_item, menu);
		menu.setHeaderTitle(R.string.context_menu_title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.btn_edit:
			runSettingActivity();
			return true;
		}
		return super.onContextItemSelected(item);
	}*/

	private void displayFarmHeaderView(final FarmJson data) {
		ImageLoader imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;

		((FarmActivity) getSherlockActivity()).displayActionBarTitleText(data.Farm);
		//((FarmActivity) getSherlockActivity()).displayActionBarFavoriteText(data.FavoriteCount);

		/*if (userType.equals("F")) {
			mainTitleText.setText("농부이야기");
		} else {
			mainTitleText.setText("마을이야기");
		}*/

		ArrayList<String> images = new ArrayList<String>();
		if (!PatternUtil.isEmpty(data.FarmImage1))
			images.add(data.FarmImage1);
		if (!PatternUtil.isEmpty(data.FarmImage2))
			images.add(data.FarmImage2);
		if (!PatternUtil.isEmpty(data.FarmImage3))
			images.add(data.FarmImage3);
		if (!PatternUtil.isEmpty(data.FarmImage4))
			images.add(data.FarmImage4);
		if (!PatternUtil.isEmpty(data.FarmImage5))
			images.add(data.FarmImage5);

		if (images.size() != 0) {
			imageViewPager.setAdapter(new FarmSlideImageAdapter(getSherlockActivity(), images, imageLoader));
			displayViewPagerIndicator(0);
			imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int arg0) {

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int position) {
					displayViewPagerIndicator(imageViewPager.getCurrentItem());
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Swipe_Image", String.valueOf(imageViewPager.getCurrentItem()));
				}
			});
		} else {
			imageViewPager.setVisibility(View.INVISIBLE);
		}

		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565)
				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 55) / 2))
				.showImageOnLoading(R.drawable.common_dummy).build();

		if (!PatternUtil.isEmpty(data.ProfileImage)) {
			imageLoader.displayImage(data.ProfileImage, Profile, options);
		}

		if (!PatternUtil.isEmpty(data.FarmerName)) {
			Farmer.setText(data.FarmerName);
		} else {
			Farmer.setVisibility(View.INVISIBLE);
		}

		CategoryName = (TextView) headerView.findViewById(R.id.CategoryName);
		ReleaseDate = (TextView) headerView.findViewById(R.id.ReleaseDate);
		displayCategoryView(data.Categories);

		Introduction.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				try {

					KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_FarmInfo", farmData.Farm);

					Intent intent = new Intent(getSherlockActivity(), FarmIntroductionActivity.class);
					intent.putExtra("farmType", userType);
					intent.putExtra("farmData", JsonUtil.objectToJson(farmData));
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Location.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (!TextUtils.isEmpty(farmData.Latitude) && !TextUtils.isEmpty(farmData.Longitude) && !TextUtils.isEmpty(farmData.Address)) {
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_FarmMap", farmData.Farm);
					Intent intent = new Intent(getSherlockActivity(), FarmLocationActivity.class);
					intent.putExtra("farmLatitude", farmData.Latitude);
					intent.putExtra("farmLongitude", farmData.Longitude);
					intent.putExtra("farmAddress", farmData.Address);
					startActivity(intent);
				}
			}
		});

		Phone.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				/*CommonUtil.AndroidUtil.actionDial(getSherlockActivity(), farmData.Phone);*/
				CommonUtil.AndroidUtil.actionDial(getSherlockActivity(), getResources().getString(R.string.kfarmers_phone));
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_FarmPhone", null);
			}
		});
		
		/*if (AppPreferences.getLogin(getSherlockActivity()))
		{
			try 
			{
				String profile = DbController.queryProfileContent(getSherlockActivity());
				if (profile != null) {
					JsonNode root = JsonUtil.parseTree(profile);
					profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
					
					if(profileData.Index.equals(farmData.FarmerIndex))
					{
						SettingBtn.setVisibility(View.VISIBLE);
						SettingBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Setting", null);
								runSettingActivity();
							}
						});
					}
					else
					{
						SettingBtn.setVisibility(View.GONE);
					}
				}
			}
			catch (Exception e) {
				SettingBtn.setVisibility(View.GONE);
			}
		}
		else
		{
			SettingBtn.setVisibility(View.GONE);
		}*/
		
		
//		More.setOnClickListener(new ViewOnClickListener() {
//			@Override
//			public void viewOnClick(View v) {
//				try {
//					FarmMoreDialogFragment fragment = FarmMoreDialogFragment.newInstance(userType, JsonUtil.objectToJson(farmData));
//					FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
//					ft.addToBackStack(null);
//					fragment.show(ft, FarmMoreDialogFragment.TAG);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//		Blog.setOnClickListener(new ViewOnClickListener() {
//			@Override
//			public void viewOnClick(View v) {
//				Intent intent = new Intent(getSherlockActivity(), BlogActivity.class);
//				intent.putExtra("userIndex", farmData.FarmerIndex);
//				getSherlockActivity().startActivity(intent);
//			}
//		});
	}
	
	public void runSettingActivity()
	{
		Intent intent = null;
		
		if (profileData.Type.equals("F")) 
		{
			intent = new Intent(getActivity(), EditFarmerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		}
		else if (profileData.Type.equals("V")) 
		{
			intent = new Intent(getActivity(), EditVillageActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		}
		
		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			getActivity().startActivityFromFragment(FarmFragment.this, intent, Constants.REQUEST_EDIT_DIARY);
			isEdit = true;
		}
	}
	
	private void displayViewPagerIndicator(int position) {
		PagingLayout.removeAllViews();

		for (int i = 0; i < imageViewPager.getAdapter().getCount(); i++) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView dot = new ImageView(getSherlockActivity());

			if (i != 0) {
				lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 3), 0, 0, 0);
				dot.setLayoutParams(lp);
			}

			if (i == position) {
				dot.setImageResource(R.drawable.button_farm_paging_on);
			} else {
				dot.setImageResource(R.drawable.button_farm_paging_off);
			}

			PagingLayout.addView(dot);
		}
	}

	private void displayCategoryView(ArrayList<CategoryJson> Categories) {
		if (Categories != null) {
			categoryIndex = 0;
			String firstSubName = "전체";
			/*if (userType.equals("F")) {
				firstSubName = "전체 ";
			} else {
				firstSubName = "마을이야기 ";
			}*/
			String firstPrimaryIndex = "", firstSubIndex = "";
			CategoryJson firstCategory = new CategoryJson();

			int totalCount = 0;
			for (int count = 0; count < Categories.size(); count++) {
				totalCount += Integer.valueOf(Categories.get(count).Count);
				firstPrimaryIndex += Categories.get(count).PrimaryIndex;
				firstSubIndex += Categories.get(count).SubIndex;
				if (count < (Categories.size() - 1)) {
					firstPrimaryIndex += ",";
					firstSubIndex += ",";
				}
				
				if(!PatternUtil.isEmpty(userCategory)&&Categories.get(count).SubIndex.equals(userCategory))
				{
					categoryIndex = count+1;
				}
			}
			firstCategory.SubName = firstSubName;
			firstCategory.SubIndex = firstSubIndex;
			firstCategory.PrimaryIndex = firstPrimaryIndex;

			if (categoryList == null)
			{
				categoryList = new ArrayList<String>();
				//categoryTitleList = new ArrayList<String>();
			}
			
			categoryList.clear();
			//categoryTitleList.clear();
			
			categoryList.add(firstSubName + "(" + totalCount + ")");
			//categoryTitleList.add(firstSubName + "(" + totalCount + ")");
			
			//int i = 0;
			for (CategoryJson item : Categories)
			{
				categoryList.add(item.SubName + "(" + item.Count + ")");
				
				/*if(i < Categories.size()-1)
				{
					categoryTitleList.add(item.SubName + " 이야기 (" + item.Count + ")");
				}
				else
				{
					categoryTitleList.add(item.SubName + " (" + item.Count + ")");
				}*/
				
				//i++;
			}

			if (categoryObjectList == null)
				categoryObjectList = new ArrayList<CategoryJson>();
			categoryObjectList.add(firstCategory);
			categoryObjectList.addAll(Categories);
			
			displayCategorySelectView(categoryIndex);
		}
	}

	private void displayCategorySelectView(int categoryIndex) {
		if (categoryObjectList != null) {
			String category = categoryList.get(categoryIndex);
			CategoryName.setText(category);
			/*if (categoryIndex == 0) {
				ReleaseDate.setText("");
			} else {
				ReleaseDate.setText(categoryObjectList.get(categoryIndex).ReleaseDate);
			}*/
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getListDiary(makeListDiaryData(false, Integer.valueOf(farmListAdapter.getItem(farmListAdapter.getCount() - 1).Diary),farmListAdapter.getItem(farmListAdapter.getCount() - 1).RegistrationDate2));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	// private OnRefreshListener refreshListener = new OnRefreshListener()
	// {
	// @Override
	// public void onRefreshStarted(View view)
	// {
	// getListDiary(makeListDiaryData(true, 0));
	// }
	// };

	private void onCategoryBtnClicked() {
		
		CategoryDialogFragment fragment = CategoryDialogFragment.newInstance(0, categoryIndex, categoryList.toArray(new String[categoryList.size()]),
				TAG);
		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		fragment.show(ft, CategoryDialogFragment.TAG);
	}

	@Override
	public void onDialogListSelection(int subMenuType, int position) {
		boolean updateFlag = false;

		if (categoryIndex != position) {
			updateFlag = true;
			categoryIndex = position;
		}

		if (updateFlag) {
			displayCategorySelectView(position);
			KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Category", CategoryName.getText().toString());
			getListDiary(makeListDiaryData(true, 0,""));
		}
	}

	private DiaryListData makeListDiaryData(boolean initFlag, int oldIndex, String oldDate) {
		DiaryListData data = new DiaryListData();
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);
		data.setOldDate(oldDate);
		data.setFarmer(userIndex);
		data.setCategory1(categoryObjectList.get(categoryIndex).PrimaryIndex);
		data.setCategory2(categoryObjectList.get(categoryIndex).SubIndex);

		return data;
	}

	@Override
	public void onDialogListSelection(int position, String object) {
		try {
			DiaryListJson data = (DiaryListJson) JsonUtil.jsonToObject(object, DiaryListJson.class);
			if (position == 0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Item-Share", "카카오톡");
				KaKaoController.sendKakaotalk(this, data);
			} else if (position == 1) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_Item-Share", "카카오스토리");
				KaKaoController.sendKakaostory(this, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
