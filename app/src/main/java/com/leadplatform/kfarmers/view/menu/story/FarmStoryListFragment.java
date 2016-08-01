package com.leadplatform.kfarmers.view.menu.story;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.leadplatform.kfarmers.model.json.DiaryListJson;
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
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FarmStoryListFragment extends BaseRefreshMoreListFragment implements OnCloseShareDialogListener {
	public static final String TAG = "FarmStoryListFragment";

	private boolean bMoreFlag = false;
	private ArrayList<DiaryListJson> farmStoryItemList;
	private FarmStoryListAdapter farmStoryListAdapter;

    private LinearLayout mSubMenuLayout;
	private RelativeLayout subMenuBox1, subMenuBox2;
	private TextView subMenuText1, subMenuText2;
	private String subMenuTitle1, subMenuTitle2;
	
	public static final int SUBMENU_TYPE_1 = 0;
	public static final int SUBMENU_TYPE_2 = 1;
	
	public int selectType = SUBMENU_TYPE_1;
	
	double latitude,longitude;

	public static FarmStoryListFragment newInstance() {
		final FarmStoryListFragment f = new FarmStoryListFragment();

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_IMPRESSIVE, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_story_list, container, false);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        mSubMenuLayout = (LinearLayout) v.findViewById(R.id.SubMenuLayout);
		subMenuBox1 = (RelativeLayout) v.findViewById(R.id.SubMenuBox1);
		subMenuBox2 = (RelativeLayout) v.findViewById(R.id.SubMenuBox2);
		subMenuText1 = (TextView) v.findViewById(R.id.SubMenu1);
		subMenuText2 = (TextView) v.findViewById(R.id.SubMenu2);
		
		subMenuTitle1 = "인상깊은 이야기";
		subMenuTitle2 = "농어촌 이야기";
		
		subMenuText1.setText(subMenuTitle1);
		subMenuText2.setText(subMenuTitle2);

        mSubMenuLayout.setVisibility(View.GONE);
		
		subMenuBox1.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onSubMenuBtnClicked(SUBMENU_TYPE_1);
                selectBtn();
			}
		});
		subMenuBox2.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onSubMenuBtnClicked(SUBMENU_TYPE_2);
                selectBtn();
			}
		});
		
		latitude = AppPreferences.getLatitude(getSherlockActivity());
		longitude = AppPreferences.getLongitude(getSherlockActivity());

        selectBtn();
		
		return v;
	}

    private void selectBtn()
    {
        switch (selectType)
        {
            case SUBMENU_TYPE_1:
            {
                subMenuText1.setTypeface(null, Typeface.BOLD);
                subMenuText2.setTypeface(null, Typeface.NORMAL);
                subMenuText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonMediumText));
                subMenuText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                break;
            }
            case SUBMENU_TYPE_2:
            {
                subMenuText1.setTypeface(null, Typeface.NORMAL);
                subMenuText2.setTypeface(null, Typeface.BOLD);
                subMenuText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                subMenuText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonMediumText));
                break;
            }
        }
    }
	
	private void onSubMenuBtnClicked(int subMenuType) {

		selectType = subMenuType;
		adapterInit();
		getStoryList(makeStoryListData(true, 0,""));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (farmStoryListAdapter == null) {
			adapterInit();
		}
	}
	
	public void adapterInit()
	{
		if(farmStoryItemList != null)
		{
			farmStoryListAdapter.clear();
		}
		
		farmStoryItemList = new ArrayList<DiaryListJson>();
		
		switch (selectType)
		{
			case SUBMENU_TYPE_1:
				farmStoryListAdapter = new FarmStoryListAdapter(getSherlockActivity(), R.layout.item_recommend_impressive, farmStoryItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);	
				break;
			case SUBMENU_TYPE_2:
				farmStoryListAdapter = new FarmStoryListAdapter(getSherlockActivity(), R.layout.item_diary, farmStoryItemList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
				break;
		}
		
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(farmStoryListAdapter);
        swingBottomInAnimationAdapter.setAbsListView(getListView());
        
        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
		
		setListAdapter(swingBottomInAnimationAdapter);
		getStoryList(makeStoryListData(true, 0,""));		
	}

	private void getStoryList(DiaryListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			farmStoryListAdapter.clear();
			farmStoryListAdapter.notifyDataSetChanged();
		}

        UiController.showProgressDialogFragment(getView());

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
								farmStoryListAdapter.add(diary);
							}

							if (diaryCount == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							farmStoryListAdapter.notifyDataSetChanged();
						}
						break;
					}
                    UiController.hideProgressDialogFragment(getView());
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    UiController.hideProgressDialogFragment(getView());
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				onRefreshComplete();
				onLoadMoreComplete();
                UiController.hideProgressDialogFragment(getView());
				super.onFailure(statusCode, headers, content, error);
			}
		});
	}

	private class FarmStoryListAdapter extends ArrayAdapter<DiaryListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public FarmStoryListAdapter(Context context, int itemLayoutResourceId, ArrayList<DiaryListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2)).showImageOnLoading(R.drawable.common_dummy).build();
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
				holder.FarmImageView = (ImageButton) convertView.findViewById(R.id.farmImageView);
				
				if(selectType == SUBMENU_TYPE_1)
				{
					holder.Auth2 = (ImageView) convertView.findViewById(R.id.Auth2);
				}
				else
				{
					holder.Auth1 = (ImageView) convertView.findViewById(R.id.Auth);	
				}
				//holder.FarmHomeBtn = (ImageButton) convertView.findViewById(R.id.farmHomeBtn);
				//holder.ImgCount = (TextView) convertView.findViewById(R.id.ImgCount);
				
				convertView.setTag(holder);
			} else {
				holder = (DiaryListHolder) convertView.getTag();
			}

			final DiaryListJson item = getItem(position);

			if (item != null) {
				holder.rootLayout.setTag(new String(item.Diary));
				holder.rootLayout.setTag(R.id.Tag1, item.Farm);
				
				holder.rootLayout.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						String diary = (String) v.getTag();
						Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
						intent.putExtra("diary", diary);
						intent.putExtra("farm", (String) v.getTag(R.id.Tag1));
						intent.putExtra("type", DiaryDetailActivity.DETAIL_FARM_STORY);

						KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Item", (String) v.getTag(R.id.Tag1));

						startActivity(intent);

					}
				});

				if (!PatternUtil.isEmpty(item.ProfileImage)) {
					imageLoader.displayImage(item.ProfileImage, holder.Profile, options);
				}

				if (!PatternUtil.isEmpty(item.FarmerName)) {
					holder.Farmer.setText(item.FarmerName);
					holder.Farmer.setVisibility(View.VISIBLE);
				} else {
					holder.Farmer.setVisibility(View.INVISIBLE);
				}


				if (!PatternUtil.isEmpty(item.RegistrationDate)) {
					holder.Date.setText(item.RegistrationDate);
					holder.Date.setVisibility(View.VISIBLE);
				} else {
					holder.Date.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Description)) {
					holder.Description.setText(item.Description);
					holder.Description.setVisibility(View.VISIBLE);
				} else {
					holder.Description.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(item.Like) && !item.Like.equals("0")) {
					holder.LikeText.setText(item.Like);
					holder.LikeText.setVisibility(View.VISIBLE);
				} else {
					holder.LikeText.setVisibility(View.GONE);
				}

				if (!PatternUtil.isEmpty(item.Reply) && !item.Reply.equals("0")) {
					holder.ReplyText.setText(item.Reply);
					holder.ReplyText.setVisibility(View.VISIBLE);
				} else {
					holder.ReplyText.setVisibility(View.GONE);
				}

				if (item.ProductFlag2 != null && item.ProductFlag2.equals("T")) {
					//holder.FarmHomeBtn.setVisibility(View.GONE);
					holder.FarmImageView.setVisibility(View.VISIBLE);
					holder.FarmImageView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);
							intent.putExtra("id", item.ID);
							intent.putExtra("name", item.Farm);
							intent.putExtra("type",ShopActivity.type.Farm);

							KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Farmer-Shop", item.Farm);

							startActivity(intent);
						}
					});
				} else {
					holder.FarmImageView.setVisibility(View.GONE);
				}

				holder.Like.setTag(new LikeTag(item.Diary, position));
				holder.Like.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Item-Like", null);
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
									farmStoryListAdapter.notifyDataSetChanged();
								}
							}
						});
					}
				});

				holder.Reply.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Item-Reply", null);
						if (item.Type.equals("F")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_FARMER, item.Farm, item.Diary);
						} else if (item.Type.equals("V")) {
							((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_VILLAGE, item.Farm, item.Diary);
						}
					}
				});

				holder.Share.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						try {
							KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(item), TAG);
						} catch (Exception e) {
							e.printStackTrace();
						}
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

				/*if (images.size() != 0) {
					holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(getSherlockActivity(), item.Diary, DiaryDetailActivity.DETAIL_FARM_STORY, images, imageLoader));
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
					
					holder.imageView.setTag(item.Diary);
					holder.imageView.setTag(R.id.Tag1, item.Farm);
					
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
				
				if(item.Blind.equals("Y"))
				{
					holder.Blind.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.Blind.setVisibility(View.GONE);
				}
				
/*				if (item.Type.equals("F")) {
					holder.FarmHomeBtn.setVisibility(View.VISIBLE);
					holder.FarmHomeBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
							intent.putExtra("userType", item.Type);
							intent.putExtra("userIndex", item.FarmerIndex);
							startActivity(intent);
						}
					});
				}*/

				/*if(item.ImageCount.equals("1") || item.ImageCount.equals("0"))
				{
					holder.ImgCount.setVisibility(View.GONE);
				}
				else
				{					
					holder.ImgCount.setText(item.ImageCount);
				}*/
				
				if(selectType == SUBMENU_TYPE_1)
				{
					if (!PatternUtil.isEmpty(item.CategoryName)) {
						holder.Category.setText(item.CategoryName);
						holder.Category.setVisibility(View.VISIBLE);
					} else {
						holder.Category.setVisibility(View.INVISIBLE);
					}
					

					holder.Auth2.setImageResource(R.drawable.icon_impressive);
					//holder.Auth2.setVisibility(View.VISIBLE);

					
					if (!PatternUtil.isEmpty(item.Latitude) && !PatternUtil.isEmpty(item.Longitude) && latitude != 0 && longitude != 0) {
						double lat = Double.valueOf(item.Latitude);
						double lon = Double.valueOf(item.Longitude);
						Location foodLocation = new Location("Food");
						Location userLocation = new Location("User");

						foodLocation.setLatitude(lat);
						foodLocation.setLongitude(lon);
						userLocation.setLatitude(latitude);
						userLocation.setLongitude(longitude);

						float meters = userLocation.distanceTo(foodLocation);
						String distance = "푸드마일 ";
						holder.FoodMile.setText(distance + (int) meters / 1000 + "km");
						holder.FoodMile.setVisibility(View.VISIBLE);

						if (!PatternUtil.isEmpty(item.AddressKeyword1) && !PatternUtil.isEmpty(item.AddressKeyword2)) {
							holder.Address.setText(item.AddressKeyword1 + " > " + item.AddressKeyword2);
							holder.Address.setVisibility(View.VISIBLE);
						} else {
							holder.Address.setVisibility(View.GONE);
						}
					} else {
						holder.Address.setVisibility(View.GONE);
						holder.FoodMile.setVisibility(View.GONE);
					}
				}
				else
				{
					if (!PatternUtil.isEmpty(item.Farm)) {
						holder.Category.setText(item.Farm);
						holder.Category.setVisibility(View.VISIBLE);
					} else {
						holder.Category.setVisibility(View.INVISIBLE);
					}
					
					holder.Auth1.setVisibility(View.GONE);
					holder.Address.setVisibility(View.GONE);
					holder.FoodMile.setVisibility(View.GONE);	
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
				getStoryList(makeStoryListData(false, Integer.valueOf(farmStoryListAdapter.getItem(farmStoryListAdapter.getCount() - 1).Diary),farmStoryListAdapter.getItem(farmStoryListAdapter.getCount() - 1).RegistrationDate2));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getStoryList(makeStoryListData(true, 0,""));
		}
	};

	private DiaryListData makeStoryListData(boolean initFlag, int oldIndex, String oldDate) {
		
		DiaryListData data = new DiaryListData();
		
		switch (selectType) 
		{
			case SUBMENU_TYPE_1:
				data.setImpressive(true);
				data.setInitFlag(initFlag);
				data.setOldIndex(oldIndex);
				data.setOldDate(oldDate);
				break;
			case SUBMENU_TYPE_2:
				data.setInitFlag(initFlag);
				data.setOldIndex(oldIndex);
				data.setOldDate(oldDate);
				data.setCategory1("8");
				break;
		}
		return data;
	}

	@Override
	public void onDialogListSelection(int position, String object) {
		try {
			DiaryListJson data = (DiaryListJson) JsonUtil.jsonToObject(object, DiaryListJson.class);
			if (position == 0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Item-Share", "카카오톡");
				KaKaoController.sendKakaotalk(this, data);
			} else if (position == 1) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_IMPRESSIVE, "Click_Item-Share", "카카오스토리");
				KaKaoController.sendKakaostory(this, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
