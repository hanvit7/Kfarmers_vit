package com.leadplatform.kfarmers.view.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
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
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.StoryDetailJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.leadplatform.kfarmers.view.base.InfinitePagerAdapter;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Collections;

public class StoryViewActivity extends BaseFragmentActivity implements ShareDialogFragment.OnCloseShareDialogListener {

	private RelativeLayout replyLayout, header1Layout;
	private TextView actionBarTitleText, likeText, replyText, shareText, nameText, categoryText, dateText, pipeText;
	private ImageView profileImage;
	private LinearLayout  mainLayout, bodyLayout,likeLayout, shareLayout;
	private StoryDetailJson detailStoryData;
	private String diaryIndex, userId,name;

	public SlideImageAdapter productAdapter;
	private ViewPager productViewPager;
	private LinearLayout pagingLayout;
	private LinearLayout product_layout;
	private TextView productCountText;

	private String type;
	private String mModifyData;

	private ArrayList<ProductJson> mProductItem;

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_story_view);

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_STORY_USER_DETAIL);

		initIntentData();
		initContentView();
		initUserInfo();
		productInit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getStroyData(diaryIndex);
	}

	private void initContentView() {
		header1Layout = (RelativeLayout) findViewById(R.id.Header1);
		likeLayout = (LinearLayout) findViewById(R.id.LikeLayout);
		likeText = (TextView) findViewById(R.id.LikeText);
		replyLayout = (RelativeLayout) findViewById(R.id.ReplyLayout);
		replyText = (TextView) findViewById(R.id.ReplyText);
		shareLayout = (LinearLayout) findViewById(R.id.shareView);
		shareText = (TextView) findViewById(R.id.ShareText);
		profileImage = (ImageView) findViewById(R.id.Profile);
		nameText = (TextView) findViewById(R.id.Name);
		categoryText = (TextView) findViewById(R.id.Category);
		dateText = (TextView) findViewById(R.id.Date);
		bodyLayout = (LinearLayout) findViewById(R.id.Body);
		mainLayout = (LinearLayout) findViewById(R.id.Main);
		mainLayout.setVisibility(View.INVISIBLE);
		pipeText = (TextView) findViewById(R.id.Pipe);

		productViewPager = (ViewPager) findViewById(R.id.image_viewpager);
		product_layout = (LinearLayout) findViewById(R.id.product_layout);
		productCountText = (TextView) findViewById(R.id.product_count);
		pagingLayout = (LinearLayout) findViewById(R.id.Paging);
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		actionBarTitleText = (TextView) findViewById(R.id.title);

		if(type.equals("daily")) {
			actionBarTitleText.setText("일상");
		} else {
			actionBarTitleText.setText("밥상수다");
		}
		initActionBarHomeBtn();
	}

	@Override
	protected void onDestroy() {
		unregisterForContextMenu(header1Layout);
		unregisterForContextMenu(bodyLayout);
		super.onDestroy();
	}

	private void initIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			diaryIndex = intent.getStringExtra("diary");
			name = intent.getStringExtra("name");
			type = intent.getStringExtra("type");
			mProductItem = (ArrayList<ProductJson>) intent.getSerializableExtra("product");
		}
		else {
			name = "";
		}
	}

	private void initUserInfo() {
		try {
			String profile = DbController.queryProfileContent(this);
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				userId = profileData.ID;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void displayDetailView(StoryDetailJson data) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200)).build();
		DisplayImageOptions profileOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(this, 50) / 2)).bitmapConfig(Config.RGB_565).build();

		if (!PatternUtil.isEmpty(data.ProfileImage)) {
			imageLoader.displayImage(data.ProfileImage, profileImage, profileOptions);
		}

		if (!PatternUtil.isEmpty(data.Nickname)) {
			nameText.setText(data.Nickname);
		} else {
			nameText.setVisibility(View.INVISIBLE);
		}

		if(type.equals("story")) {
			if (!PatternUtil.isEmpty(data.BlogTag)) {
				String tag ="";
				String tagArr[] = data.BlogTag.split(",");
				for(int i=0; i< tagArr.length; i++) {
					tag += "#"+tagArr[i]+" ";
				}
				categoryText.setText(tag);
				categoryText.setVisibility(View.VISIBLE);
				pipeText.setVisibility(View.VISIBLE);
				final ArrayList<String> arrayList = new ArrayList<String>();
				Collections.addAll(arrayList, tagArr);
				categoryText.setTag(arrayList);
				if(arrayList.size()==1) {
					categoryText.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							final ArrayList<String> arrayList1 = (ArrayList<String>) v.getTag();
							Intent intent = new Intent(mContext, StoryListActivity.class);
							intent.putExtra("keyword", arrayList1.get(0));
							intent.putExtra("impressive", "");
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}});
				} else if(arrayList.size()>1){
					categoryText.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							try {
								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
								builder.setTitle(R.string.dialog_category_title);
								builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();
									}
								});
								final ArrayList<String> arrayList1 = (ArrayList<String>) v.getTag();
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item, arrayList);
								builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String str = arrayList1.get(which);
										Intent intent = new Intent(mContext, StoryListActivity.class);
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
				categoryText.setVisibility(View.INVISIBLE);
				pipeText.setVisibility(View.INVISIBLE);
			}
		} else if(type.equals("daily")) {
			categoryText.setText("일상");
		}

		if (!PatternUtil.isEmpty(data.Date)) {
			dateText.setText(data.Date);
		} else {
			dateText.setVisibility(View.INVISIBLE);
		}

		bodyLayout.removeAllViews();
		if (data.Rows != null) {
			for (RowJson item : data.Rows) {
				if (item.Type.equals("Text")) {
					TextView view = (TextView) inflater.inflate(R.layout.item_detail_text, null);
					view.setText(item.Value);
					bodyLayout.addView(view);
				} else if (item.Type.equals("Image")) {
					DynamicRatioImageView view = (DynamicRatioImageView) inflater.inflate(R.layout.item_detail_image, null);
					imageLoader.displayImage(item.Value, view, options);
					bodyLayout.addView(view);
				}
			}
		}

		if (!PatternUtil.isEmpty(data.Like) && !data.Like.equals("0")) {
			likeText.setText(data.Like);
			likeText.setVisibility(View.VISIBLE);
		} else {
			likeText.setVisibility(View.GONE);
			likeText.setText("0");
		}

		likeLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				centerLikeUserDiary(diaryIndex, new OnLikeDiaryListener() {
					@Override
					public void onResult(int code, boolean plus) {
						if (code == 0) {
							int count = Integer.parseInt(detailStoryData.Like);
							if (plus) {
								detailStoryData.Like = String.valueOf(count + 1);
							} else {
								if (count != 0)
									detailStoryData.Like = String.valueOf(count - 1);
							}

							if (!PatternUtil.isEmpty(detailStoryData.Like) && !detailStoryData.Like.equals("0")) {
								likeText.setText(detailStoryData.Like);
								likeText.setVisibility(View.VISIBLE);
							} else {
								likeText.setVisibility(View.GONE);
								likeText.setText("0");
							}
						}
					}
				});
			}
		});

		shareLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try
				{
					KaKaoController.onShareBtnClicked(StoryViewActivity.this, JsonUtil.objectToJson(detailStoryData), "");
				} catch (Exception e) {}
			}
		});


		if (!PatternUtil.isEmpty(data.Reply) && !data.Reply.equals("0")) {
			replyText.setText(data.Reply);
		} else {
			replyText.setText("");
		}

		replyLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				runReplyActivity(ReplyActivity.REPLY_TYPE_NORMAL, detailStoryData.Nickname,diaryIndex);
			}
		});

		mainLayout.setVisibility(View.VISIBLE);

		if (userId != null && userId.equals(data.UserID)) {
			registerForContextMenu(header1Layout);
			registerForContextMenu(bodyLayout);
		}
	}

	private void getStroyData(String diary) {
		CenterController.getUserStoryDetail(diary, new CenterResponseListener(this) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content);
						detailStoryData = (StoryDetailJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), StoryDetailJson.class);
						if (detailStoryData != null) {
							displayDetailView(detailStoryData);
						}
						break;

					default:
						UiController.showDialog(mContext, R.string.dialog_unknown_error);
						break;
					}
				} catch (Exception e) {
					UiController.showDialog(mContext, R.string.dialog_unknown_error);
				}
			}
		});
	}

	private void getListProduct() {
		SnipeApiController.getProductListRecomend(String.valueOf(30), String.valueOf(5), new SnipeResponseListener(this) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					switch (Code) {
						case 200:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.path("item").size() > 0) {
								mProductItem = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
								setProductLayout();
							}
							break;
						default:
							setProductLayout();
					}
				} catch (Exception e) {
					setProductLayout();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				setProductLayout();
			}
		});
	}

	public void productInit() {
		if(type.equals("story")) {
			if(mProductItem != null && mProductItem.size()>0)
			{
				setProductLayout();
			}
			else
			{
				getListProduct();
			}
		}
	}

	public void setProductLayout()
	{
		if(mProductItem != null && mProductItem.size()>0) // 상품이 있으면
		{
			product_layout.setVisibility(View.VISIBLE);

			productAdapter = new SlideImageAdapter(this, mProductItem, imageLoader);
			InfinitePagerAdapter adapter = new InfinitePagerAdapter(productAdapter);
			productViewPager.setAdapter(adapter);

			displayViewPagerIndicator(0);

			productCountText.setText("추천상품");

			productViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

	private void displayViewPagerIndicator(int position) {
		pagingLayout.removeAllViews();

		for (int i = 0; i < productAdapter.getCount(); i++) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView dot = new ImageView(this);

			if (i != 0) {
				lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(this, 3), 0, 0, 0);
				dot.setLayoutParams(lp);
			}
			dot.setImageResource(R.drawable.button_farm_paging_on);

			if(i == position){
				dot.setColorFilter(Color.GRAY);
			}else {
				dot.setColorFilter(Color.LTGRAY);
			}
			pagingLayout.addView(dot);
		}
	}

	public class SlideImageAdapter extends PagerAdapter {

		private Context context;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;//optionsProfile;
		private ArrayList<ProductJson> items;

		public SlideImageAdapter(Context context,
								 ArrayList<ProductJson> items, ImageLoader imageLoader) {
			this.context = context;
			this.imageLoader = imageLoader;
			this.items = items;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.bitmapConfig(Config.RGB_565)
					.showImageOnLoading(R.drawable.common_dummy).build();

			/*this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(StoryViewActivity.this, 50) / 2))
					.showImageOnLoading(R.drawable.icon_empty_profile).build();*/
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
				imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + items.get(position).profile_image, img_profile, optionsProfile);
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
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_STORY_USER_DETAIL, "Click_Product", (String) v.getTag(R.integer.tag_id));

					Intent intent = new Intent(StoryViewActivity.this,ProductActivity.class);
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


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_diary_item, menu);
		menu.setHeaderTitle(R.string.context_menu_title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.btn_copy:
				String str = "";
				if (detailStoryData != null) {
					try {
						if (detailStoryData.Rows != null) {
							for (RowJson content : detailStoryData.Rows) {
								if (content.Type.equals("Text")) {
									str += content.Value;
								}
							}
						}
						ClipboardManager clip = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
						clip.setText(str);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return true;

			case R.id.btn_edit:
				if (detailStoryData != null) {
					try {
						Intent intent = new Intent(this, StoryWriteActivity.class);
						intent.putExtra("detail", JsonUtil.objectToJson(detailStoryData));
						intent.putExtra("type",type);
						intent.putExtra("index",diaryIndex);
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return true;

			case R.id.btn_delete:
				CenterController.deleteUserStory(diaryIndex, new CenterResponseListener(this) {
					@Override
					public void onSuccess(int Code, String content) {
						try {
							switch (Code) {
								case 0000:
									Intent intent = new Intent();
									intent.putExtra("diary", diaryIndex);
									intent.putExtra("delete", true);
									setResult(RESULT_OK, intent);
									finish();
									break;
								default:
									UiController.showDialog(StoryViewActivity.this, R.string.dialog_unknown_error);
									break;
							}
						} catch (Exception e) {
							UiController.showDialog(StoryViewActivity.this, R.string.dialog_unknown_error);
						}
					}
				});
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDialogListSelection(int position, String object) {
		try {
			StoryDetailJson data = (StoryDetailJson) JsonUtil.jsonToObject(object, StoryDetailJson.class);
			data.Diary = diaryIndex;
			if (position == 0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_STORY_USER_DETAIL, "Click_Share", "카카오톡");
				KaKaoController.sendKakaotalk(StoryViewActivity.this,data);
			} else {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_STORY_USER_DETAIL, "Click_Share", "카카오스토리");
				KaKaoController.sendKakaostory(StoryViewActivity.this, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
