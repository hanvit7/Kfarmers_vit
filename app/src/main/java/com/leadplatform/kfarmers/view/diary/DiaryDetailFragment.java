package com.leadplatform.kfarmers.view.diary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.DiaryDetailJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.leadplatform.kfarmers.view.base.InfinitePagerAdapter;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class DiaryDetailFragment extends BaseFragment
{
	public static final int DETAIL_DIARY = 0;
	public static final int DETAIL_IMPRESSIVE = 1;
	public static final int DETAIL_FARMER = 2;
	public static final int DETAIL_VILLAGE = 3;
	public static final int DETAIL_FARM_STORY = 4;
	private final int authImages[] = { 0, 1, R.drawable.icon_mark_2, R.drawable.icon_mark_3, R.drawable.icon_mark_4, R.drawable.icon_mark_5,
			R.drawable.icon_mark_6, R.drawable.icon_mark_7, R.drawable.icon_mark_8, R.drawable.icon_mark_9, R.drawable.icon_mark_10,
			R.drawable.icon_mark_11 };
	public DiaryDetailJson detailData;
	public SlideImageAdapter productAdapter;
	public ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options,profileOptions;
	public ViewPager productViewPager;
	private int diaryNo;
	private String intentDiary;
	private RelativeLayout    header1Layout, header2Layout;
	private TextView farmerText, categoryText, dateText, addressText, foodMileText,
			releaseTitleText, releaseDateText, releaseNoteText, temperatureText, humidityText, skyNoteText,productCountText;
	private View releaseLayout, lineLayout, weatherLayout;
	private TextView authText[] = new TextView[3];
	private ImageView profileImage, authImage[] = new ImageView[3];
	private LinearLayout mainLayout, bodyLayout, bottomLayout;
	private int detailType = DETAIL_DIARY;
	private String userIndex;
	private ScrollView scrollView;
	private LinearLayout PagingLayout;
	private LinearLayout product_layout;
	private LinearLayout share_layout,like_layout;
	private TextView likeText;
	
	public static DiaryDetailFragment newInstance(String diary , int type , int no)
	{
		final DiaryDetailFragment f = new DiaryDetailFragment();

		final Bundle args = new Bundle();
		args.putString("diary", diary);
		args.putInt("type", type);
		args.putInt("diaryno", no);
		f.setArguments(args);

		return f;
	}
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) 
		{
			intentDiary = getArguments().getString("diary");
			detailType = getArguments().getInt("type", DETAIL_DIARY);
			diaryNo = getArguments().getInt("diaryno", 0);
		}

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_STROY_DETAIL);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		final View v = inflater.inflate(R.layout.fragment_detail_diary, container, false);

		header1Layout = (RelativeLayout) v.findViewById(R.id.Header1);
		header2Layout = (RelativeLayout) v.findViewById(R.id.Header2);
		profileImage = (ImageView) v.findViewById(R.id.Profile);
		farmerText = (TextView) v.findViewById(R.id.Farmer);
		categoryText = (TextView) v.findViewById(R.id.Category);
		authImage[0] = (ImageView) v.findViewById(R.id.Auth1);
		authImage[1] = (ImageView) v.findViewById(R.id.Auth2);
		authImage[2] = (ImageView) v.findViewById(R.id.Auth3);
		authText[0] = (TextView) v.findViewById(R.id.Auth1Text);
		authText[1] = (TextView) v.findViewById(R.id.Auth2Text);
		authText[2] = (TextView) v.findViewById(R.id.Auth3Text);
		dateText = (TextView) v.findViewById(R.id.Date);
		addressText = (TextView) v.findViewById(R.id.Address);
		foodMileText = (TextView) v.findViewById(R.id.FoodMile);
		bodyLayout = (LinearLayout) v.findViewById(R.id.Body);
		mainLayout = (LinearLayout) v.findViewById(R.id.Main);
		releaseTitleText = (TextView) v.findViewById(R.id.ReleaseTitle);
		releaseDateText = (TextView) v.findViewById(R.id.ReleaseDate);
		releaseNoteText = (TextView) v.findViewById(R.id.ReleaseNote);
		temperatureText = (TextView) v.findViewById(R.id.Temperature);
		humidityText = (TextView) v.findViewById(R.id.Humidity);
		skyNoteText = (TextView) v.findViewById(R.id.SkyNote);
		bottomLayout = (LinearLayout) v.findViewById(R.id.bottomLayout);
		
		releaseLayout = v.findViewById(R.id.ReleaseLayout);
		lineLayout = v.findViewById(R.id.LineLayout);
		weatherLayout = v.findViewById(R.id.WeatherLayout);
		mainLayout.setVisibility(View.INVISIBLE);
		
		scrollView  = (ScrollView)v.findViewById(R.id.scrollview);
		scrollView.setVerticalScrollBarEnabled(false);
		
		productCountText = (TextView)v.findViewById(R.id.product_count);
		PagingLayout = (LinearLayout) v.findViewById(R.id.Paging);
		
		
		productViewPager = (ViewPager) v.findViewById(R.id.image_viewpager);
		product_layout = (LinearLayout)v.findViewById(R.id.product_layout);
		
		share_layout = (LinearLayout)v.findViewById(R.id.shareView);
		like_layout = (LinearLayout)v.findViewById(R.id.like_layout);
		likeText = (TextView)v.findViewById(R.id.likeText);
		//productViewPager.setPageMargin((int) getResources().getDimension(R.dimen.image_pager_margin));
		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		initUserInfo();
		CenterController.getDetailDiary(intentDiary, new CenterResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						detailData = (DiaryDetailJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), DiaryDetailJson.class);
						if (detailData != null) {
							displayDetailView(detailData);
							
							((DiaryDetailActivity)getActivity()).dataHashMap.put(diaryNo, detailData);
							
							if(PatternUtil.isEmpty(((DiaryDetailActivity)getActivity()).farmName))
							{
								((DiaryDetailActivity)getActivity()).farmName = detailData.Farm;
								((DiaryDetailActivity)getActivity()).initView();
							}
							
							try {
								((DiaryDetailActivity)getActivity()).setBottomData();	
							} catch (Exception e) {
							}
							
							if(((DiaryDetailActivity)getActivity()).isProductCheck == false)
							{
								getListShop(detailData.ID,detailData.CategoryIndex);
							}
							else
							{
								setProductLayout();
							}
						}
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
		

	}

	
	private void initUserInfo() {
		try {
			String profile = DbController.queryProfileContent(getSherlockActivity());
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				userIndex = profileData.Index;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void displayViewPagerIndicator(int position) {
		PagingLayout.removeAllViews();

		for (int i = 0; i < productAdapter.getCount(); i++) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView dot = new ImageView(getSherlockActivity());

			if (i != 0) {
				lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(
						getSherlockActivity(), 3), 0, 0, 0);
				dot.setLayoutParams(lp);
			}

			dot.setImageResource(R.drawable.button_farm_paging_on);

			if(i == position){
				dot.setColorFilter(Color.GRAY);
			}else {
				dot.setColorFilter(Color.LTGRAY);
			}

			PagingLayout.addView(dot);
		}
	}
	
	private void displayDetailView(DiaryDetailJson data) {
		final LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(200)).build();
		profileOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.bitmapConfig(Config.RGB_565).displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2)).showImageOnLoading(R.drawable.icon_empty_profile).build();
		// UserDb user = DbController.queryCurrentUser(DiaryDetailActivity.this);
		double latitude = AppPreferences.getLatitude(getSherlockActivity());
		double longitude = AppPreferences.getLongitude(getSherlockActivity());
		
		
		((DiaryDetailActivity)getActivity()).actionBarTitleText.setText(data.Farm);

		if (!PatternUtil.isEmpty(data.ProfileImage)) {
			imageLoader.displayImage(data.ProfileImage, profileImage, profileOptions);
		} else {
			profileImage.setVisibility(View.INVISIBLE);
		}

		if (!PatternUtil.isEmpty(data.FarmerName)) {
			farmerText.setText(data.FarmerName);
		} else {
			farmerText.setVisibility(View.INVISIBLE);
		}

		if (detailType == DETAIL_DIARY || detailType == DETAIL_FARM_STORY) {
			if (!PatternUtil.isEmpty(data.Farm)) {
				//actionBarTitleText.setText(data.Farm);
			} else {
				//actionBarTitleText.setVisibility(View.INVISIBLE);
			}
		} else if (detailType == DETAIL_IMPRESSIVE) {
			//actionBarTitleText.setText(R.string.title_impressive);
		} else if (detailType == DETAIL_FARMER) {
			//actionBarTitleText.setText(R.string.title_farmer);
		} else if (detailType == DETAIL_VILLAGE) {
			//actionBarTitleText.setText(R.string.title_village);
		}

		if (!PatternUtil.isEmpty(data.CategoryName)) {
			categoryText.setText(data.CategoryName);
		} else {
			categoryText.setVisibility(View.INVISIBLE);
		}

		for (ImageView image : authImage) {
			image.setVisibility(View.GONE);
		}

		for (TextView text : authText) {
			text.setVisibility(View.GONE);
		}

		if (data.Auths != null) {
			JSONArray authArray, serialArray;
			try {
				authArray = new JSONArray(data.Auths);
				serialArray = new JSONArray(data.Serials);
				for (int index = 0; index < authArray.length(); index++) {
					authImage[index].setImageResource(authImages[Integer.valueOf(authArray.getString(index))]);
					authImage[index].setVisibility(View.VISIBLE);
					authText[index].setText(serialArray.getString(index));
					authText[index].setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (!PatternUtil.isEmpty(data.RegistrationDate)) {
			dateText.setText(data.RegistrationDate);
		} else {
			dateText.setVisibility(View.INVISIBLE);
		}

		if (data.Rows != null) {
			ArrayList<String> imgArrayList = new ArrayList<>();
			int i = 0;
			for (RowJson item : data.Rows) {
				if (item.Type.equals("Text")) {
					TextView view = (TextView) inflater.inflate(R.layout.item_detail_text, null);
					view.setText(item.Value);
					bodyLayout.addView(view);
				} else if (item.Type.equals("Image")) {
					DynamicRatioImageView view = (DynamicRatioImageView) inflater.inflate(R.layout.item_detail_image, null);
					imageLoader.displayImage(item.Value, view, options);
					imgArrayList.add(item.Value);

					view.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							return false;
						}
					});
					view.setTag(R.integer.tag_pos,i);
					view.setTag(imgArrayList);
					view.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							int i = (int) v.getTag(R.integer.tag_pos);
							ArrayList<String> img = (ArrayList<String>) v.getTag();
							Intent intent = new Intent(getActivity(), ImageViewActivity.class);
							intent.putExtra("pos", i);
							intent.putStringArrayListExtra("imageArrary",img);
							getActivity().startActivity(intent);
						}
					});
					bodyLayout.addView(view);
					i++;
				}
			}
		}


		/*if (detailType != DETAIL_FARM_STORY) {
			bottomLayout.setVisibility(View.VISIBLE);

            releaseLayout.setVisibility(View.GONE);
            lineLayout.setVisibility(View.GONE);

			*//*if (detailData.FarmerType.equals("F")) {
				if (detailData.GeneralCategoryIndex.equals("7")) {
					releaseTitleText.setText("체험");
				} else {
					releaseTitleText.setText("출하");
				}
			} else {
				releaseTitleText.setText("체험");
			}

			if (!PatternUtil.isEmpty(data.ReleaseDate)) {
				releaseDateText.setText(data.ReleaseDate);
			} else {
				releaseDateText.setVisibility(View.INVISIBLE);
			}

			if (!PatternUtil.isEmpty(data.ReleaseNote)) {
				releaseNoteText.setText(data.ReleaseNote);
			} else {
				releaseNoteText.setText("정보가 없습니다.");
			}*//*

			if (!PatternUtil.isEmpty(data.Temperature)) {
				temperatureText.setText("온도 " + data.Temperature + "℃");
			} else {
				temperatureText.setVisibility(View.GONE);
			}

			if (!PatternUtil.isEmpty(data.Humidity)) {
				humidityText.setText("습도 " + data.Humidity + "%");
			} else {
				humidityText.setVisibility(View.GONE);
			}

			if (!PatternUtil.isEmpty(data.Sky)) {
				skyNoteText.setText(data.Sky);
			} else {
				skyNoteText.setText("정보가 없습니다.");
			}

			boolean bRelease = false, bWeather = false;

			if (PatternUtil.isEmpty(data.ReleaseDate) && PatternUtil.isEmpty(data.ReleaseNote)) {
				bRelease = true;
				releaseLayout.setVisibility(View.GONE);
				lineLayout.setVisibility(View.GONE);
			}

			if (PatternUtil.isEmpty(data.Temperature) && PatternUtil.isEmpty(data.Humidity) && PatternUtil.isEmpty(data.Sky)) {
				bWeather = true;
				weatherLayout.setVisibility(View.GONE);
				lineLayout.setVisibility(View.GONE);
			}

			if (bRelease && bWeather)
				bottomLayout.setVisibility(View.GONE);
		} else {
			bottomLayout.setVisibility(View.GONE);
		}*/

		if (!PatternUtil.isEmpty(data.Latitude) && !PatternUtil.isEmpty(data.Longitude) && latitude != 0 && longitude != 0) {
			double lat = Double.valueOf(data.Latitude);
			double lon = Double.valueOf(data.Longitude);
			Location foodLocation = new Location("Food");
			Location userLocation = new Location("User");

			foodLocation.setLatitude(lat);
			foodLocation.setLongitude(lon);
			userLocation.setLatitude(latitude);
			userLocation.setLongitude(longitude);

			float meters = userLocation.distanceTo(foodLocation);
			String distance = "푸드마일 ";
			foodMileText.setText(distance + (int) meters / 1000 + "km");

			if (!PatternUtil.isEmpty(data.AddressKeyword1) && !PatternUtil.isEmpty(data.AddressKeyword2)) {
				addressText.setText(data.AddressKeyword1 + " > " + data.AddressKeyword2);
			} else {
				addressText.setVisibility(View.GONE);
			}
		} else {
			addressText.setVisibility(View.GONE);
			foodMileText.setVisibility(View.GONE);
		}


		mainLayout.setVisibility(View.VISIBLE);

		if (userIndex != null && userIndex.equals(data.FarmerIndex)) {
			registerForContextMenu(header1Layout);
			registerForContextMenu(header2Layout);
			registerForContextMenu(bodyLayout);
			registerForContextMenu(bottomLayout);
		}
		
		share_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try 
				{
					KaKaoController.onShareBtnClicked(getActivity(), JsonUtil.objectToJson(detailData), "");	
				} catch (Exception e) {}
			}
		});
		
		like_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Like", detailData.Diary);

				((DiaryDetailActivity)getActivity()).centerLikeDiary(detailData.Diary, new OnLikeDiaryListener() {
					@Override
					public void onResult(int code, boolean plus) {
						if (code == 0) {
							int count = Integer.parseInt(detailData.Like);
							if (plus) {
								detailData.Like = String.valueOf(count + 1);
							} else {
								if (count != 0)
									detailData.Like = String.valueOf(count - 1);
							}

							if (!PatternUtil.isEmpty(detailData.Like) && !detailData.Like.equals("0")) {
								//likeText.setText( getString(R.string.GetListDiaryLike) + " (" + detailData.Like + ")");
								likeText.setText(detailData.Like);
								likeText.setVisibility(View.VISIBLE);
							} else {
								likeText.setVisibility(View.GONE);
								likeText.setText("0");
								
							//	likeText.setText( R.string.GetListDiaryLike "");
							}
						}
					}
				});
			}
		});
		
		if (!PatternUtil.isEmpty(detailData.Like) && !detailData.Like.equals("0")) {
			likeText.setText(detailData.Like);
			likeText.setVisibility(View.VISIBLE);
			//likeText.setVisibility(View.GONE);
		} else {
			//likeText.setVisibility(View.VISIBLE);
			likeText.setVisibility(View.GONE);
			likeText.setText("0");
		//	likeText.setText( R.string.GetListDiaryLike "");
		}
	}
	
	public void setProductLayout()
	{
		if(((DiaryDetailActivity)getActivity()).shopArrayList != null && ((DiaryDetailActivity)getActivity()).shopArrayList.size()>0)
		{
			product_layout.setVisibility(View.VISIBLE);
			
			if(((DiaryDetailActivity)getActivity()).shopArrayList.size()==1)
			{
				productAdapter = new SlideImageAdapter(getSherlockActivity(), ((DiaryDetailActivity)getActivity()).shopArrayList);
				productViewPager.setAdapter(productAdapter);
				PagingLayout.setVisibility(View.GONE);
			}
			else
			{
				productAdapter = new SlideImageAdapter(getSherlockActivity(), ((DiaryDetailActivity)getActivity()).shopArrayList);
				InfinitePagerAdapter adapter = new InfinitePagerAdapter(productAdapter);
				productViewPager.setAdapter(adapter);
			}
			displayViewPagerIndicator(0);
			
			productCountText.setText("관련상품  "+ ((DiaryDetailActivity)getActivity()).shopArrayList.size()+"개");

			productViewPager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int position) {
					displayViewPagerIndicator(productViewPager.getCurrentItem());
				}
			});	
		}
		else
		{
			product_layout.setVisibility(View.GONE);
		}
	}

	
	private void getListShop(String farmerId, String categoryIndex) {

        SnipeApiController.getProductList("9999", "", "", farmerId, categoryIndex,false,null,null, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            ArrayList<ProductJson> arrayList = new ArrayList<ProductJson>();

                            if (root.path("item").size() > 0) {
                                arrayList = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
                                ((DiaryDetailActivity)getActivity()).shopArrayList = arrayList;
                                setProductLayout();
                            }
                            else
                            {
                                product_layout.setVisibility(View.GONE);
                            }
                            break;

                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					product_layout.setVisibility(View.GONE);
                }
                ((DiaryDetailActivity)getActivity()).isProductCheck = true;
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
				product_layout.setVisibility(View.GONE);
            }
        });
	}
	
	public class SlideImageAdapter extends PagerAdapter {

		private Context context;
		private ArrayList<ProductJson> items;

		public SlideImageAdapter(Context context,
				ArrayList<ProductJson> items) {
			this.context = context;
			this.items = items;
		}
		
		public void addAll(ArrayList<ProductJson> items)
		{
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v = inflater.inflate(R.layout.item_story_product_list, container, false);

			LinearLayout root = (LinearLayout) v.findViewById(R.id.root);
			ImageView img = (ImageView) v.findViewById(R.id.image);
			ImageView img_profile = (ImageView) v.findViewById(R.id.image_profile);
			TextView des = (TextView) v.findViewById(R.id.des);
			TextView price = (TextView) v.findViewById(R.id.price);
			TextView dcPrice = (TextView) v.findViewById(R.id.dcPrice);
			TextView summary = (TextView) v.findViewById(R.id.summary);
			TextView dDay = (TextView) v.findViewById(R.id.textDday);

			img_profile.setVisibility(View.GONE);

			/*if(items.get(position).profile_image != null && !items.get(position).profile_image.isEmpty()) {
				imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + items.get(position).profile_image, img_profile, options);
			}
			else {
				img_profile.setImageResource(0);
			}*/
			imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + items.get(position).image1, img, options);

			if(items.get(position).summary != null && !items.get(position).summary.isEmpty())
			{
				summary.setVisibility(View.VISIBLE);
				summary.setText(items.get(position).summary);
			}
			else
			{
				summary.setVisibility(View.GONE);
			}

			//des.setText(item.name);
			des.setText(items.get(position).name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

			int itemPrice = (int)Double.parseDouble(items.get(position).price);
			int itemBuyPrice = (int) Double.parseDouble(items.get(position).buyprice);

			if(itemPrice > itemBuyPrice)
			{
				price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
				dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(items.get(position).buyprice)) + getResources().getString(R.string.korea_won));
				price.setVisibility(View.VISIBLE);

				price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}
			else
			{
				price.setText("");
				dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(items.get(position).buyprice)) + getResources().getString(R.string.korea_won));
				price.setVisibility(View.GONE);
				price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

			}

			v.setTag(items.get(position).idx);
			v.setTag(R.integer.tag_id,items.get(position).name);

			root.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Product", (String) v.getTag(R.integer.tag_id));
					Intent intent = new Intent(getActivity(),ProductActivity.class);
					intent.putExtra("productIndex", v.getTag().toString());
					startActivity(intent);
				}
			});

			if(items.get(position).duration.startsWith("D") && !items.get(position).duration.equals("D-day")) {
				dDay.setText(items.get(position).duration);
				dDay.setVisibility(View.VISIBLE);
			} else {
				dDay.setVisibility(View.GONE);
			}

			container.addView(v, container
					.getChildCount() > position ? position
					: container.getChildCount());

			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((LinearLayout) object);
			CommonUtil.UiUtil.unbindDrawables((LinearLayout) object);
			object = null;
		}
	}
}
