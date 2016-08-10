package com.leadplatform.kfarmers.view.menu.story;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.DiaryDetailJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.StoryDetailJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.leadplatform.kfarmers.view.base.OnDeleteDiaryListener;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.diary.DiaryWriteActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class StoryDetailActivity extends BaseFragmentActivity {
	public static final int DETAIL_CONSUMER = 5;
	public static final int DETAIL_INTERVIEW = 6;
	public static final int DETAIL_NORMAL = 7;

	private RelativeLayout likeLayout, replyLayout, shareLayout, header1Layout;
	private TextView actionBarTitleText, likeText, replyText, shareText, farmerText, categoryText, dateText, releaseDateText, releaseNoteText, temperatureText, humidityText, skyNoteText;
	private ImageView profileImage;
	private LinearLayout footerLayout, mainLayout, bodyLayout;
	private int detailType = DETAIL_CONSUMER;
	private StoryDetailJson detailStoryData;
	private String diaryIndex, userIndex;

	private final int authImages[] = { 0, 1, R.drawable.icon_mark_2, R.drawable.icon_mark_3, R.drawable.icon_mark_4, R.drawable.icon_mark_5, R.drawable.icon_mark_6, R.drawable.icon_mark_7,
			R.drawable.icon_mark_8, R.drawable.icon_mark_9, R.drawable.icon_mark_10, R.drawable.icon_mark_11 };

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_detail_story);

		initIntentData();
		initContentView(savedInstanceState);
		initUserInfo();
	}

	private void initContentView(Bundle savedInstanceState) {
		header1Layout = (RelativeLayout) findViewById(R.id.Header1);
		likeLayout = (RelativeLayout) findViewById(R.id.Like);
		likeText = (TextView) findViewById(R.id.LikeText);
		replyLayout = (RelativeLayout) findViewById(R.id.Reply);
		replyText = (TextView) findViewById(R.id.ReplyText);
		shareLayout = (RelativeLayout) findViewById(R.id.Share);
		shareText = (TextView) findViewById(R.id.ShareText);
		profileImage = (ImageView) findViewById(R.id.Profile);
		farmerText = (TextView) findViewById(R.id.Farmer);
		dateText = (TextView) findViewById(R.id.Date);
		bodyLayout = (LinearLayout) findViewById(R.id.Body);
		footerLayout = (LinearLayout) findViewById(R.id.Footer);
		mainLayout = (LinearLayout) findViewById(R.id.Main);

		footerLayout.setVisibility(View.INVISIBLE);
		mainLayout.setVisibility(View.INVISIBLE);

		if (detailType == DETAIL_CONSUMER || detailType == DETAIL_NORMAL) {
			getConsumerStory(diaryIndex);
		} else if (detailType == DETAIL_INTERVIEW) {
			getInterViewStory(diaryIndex);
		}
		// else if (detailType == DETAIL_NORMAL)
		// {
		// getNormalStory(diaryIndex);
		// }
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		actionBarTitleText = (TextView) findViewById(R.id.actionbar_title_text_view);
		if (detailType == DETAIL_CONSUMER || detailType == DETAIL_NORMAL) {
			actionBarTitleText.setText(R.string.MenuLeftTextConsumer);
		} else if (detailType == DETAIL_INTERVIEW) {
			actionBarTitleText.setText(R.string.MenuLeftTextDirect);
		}
		// else if (detailType == DETAIL_NORMAL)
		// {
		// actionBarTitleText.setText(R.string.MenuLeftTextRice);
		// }
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
			detailType = intent.getIntExtra("type", DETAIL_CONSUMER);
		}
	}

	private void initUserInfo() {
		try {
			String profile = DbController.queryProfileContent(this);
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				userIndex = profileData.Index;
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
			farmerText.setText(data.Nickname);
		} else {
			farmerText.setVisibility(View.INVISIBLE);
		}

		if (!PatternUtil.isEmpty(data.Date)) {
			dateText.setText(data.Date);
		} else {
			dateText.setVisibility(View.INVISIBLE);
		}

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
			likeText.setText(/* getString(R.string.GetListDiaryLike) + */" (" + data.Like + ")");
		} else {
			likeText.setText(/* R.string.GetListDiaryLike */"");
		}

		if (!PatternUtil.isEmpty(data.Reply) && !data.Reply.equals("0")) {
			replyText.setText(/* getString(R.string.GetListDiaryReply) + */" (" + data.Reply + ")");
		} else {
			replyText.setText(/* R.string.GetListDiaryReply */"");
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
								likeText.setText(/* getString(R.string.GetListDiaryLike) + */" (" + detailStoryData.Like + ")");
							} else {
								likeText.setText(/* R.string.GetListDiaryLike */"");
							}
						}
					}
				});
			}
		});

		replyLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if (detailType == DETAIL_CONSUMER) {
					runReplyActivity(
							ReplyActivity.REPLY_TYPE_CONSUMER,
							detailStoryData.Nickname,
							diaryIndex);
				} else if (detailType == DETAIL_INTERVIEW) {
					runReplyActivity(
							ReplyActivity.REPLY_TYPE_INTERVIEW,
							detailStoryData.Nickname,
							diaryIndex);
				} else if (detailType == DETAIL_NORMAL) {
					runReplyActivity(
							ReplyActivity.REPLY_TYPE_NORMAL,
							detailStoryData.Nickname,
							diaryIndex);
				}
			}
		});

		shareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UiController.showDialog(StoryDetailActivity.this, R.string.dialog_ready);
			}
		});

		footerLayout.setVisibility(View.VISIBLE);
		mainLayout.setVisibility(View.VISIBLE);

		if (userIndex != null && userIndex.equals(data.UserIndex)) {
			registerForContextMenu(header1Layout);
			registerForContextMenu(bodyLayout);
		}
	}

	private void getConsumerStory(String diary) {
		CenterController.getConsumerStory(diary, new CenterResponseListener(this) {
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

	private void getInterViewStory(String diary) {
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

	private void getNormalStory(String diary) {
		CenterController.getNormalStory(diary, new CenterResponseListener(this) {
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

	private DiaryDetailJson makeEditStoryData(StoryDetailJson storyData) {
		DiaryDetailJson diaryData = new DiaryDetailJson();

		if (detailType == DETAIL_CONSUMER || detailType == DETAIL_NORMAL) {
			diaryData.Diary = diaryIndex;
			diaryData.Rows = storyData.Rows;
		} else if (detailType == DETAIL_INTERVIEW) {
			diaryData.Diary = diaryIndex;
			diaryData.Rows = storyData.Rows;
			diaryData.BlogTitle = storyData.BlogTitle;
			diaryData.BlogAlign = storyData.BlogAlign;
			diaryData.BlogTag = storyData.BlogTag;
		}

		return diaryData;
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
		case R.id.btn_edit:
			if (detailStoryData != null) {
				try {
					Intent intent = DiaryWriteActivity.newIntent(
							getApplicationContext(),
							DiaryWriteActivity.DiaryWriteState.IMPORT_FROM_SNS,
							JsonUtil.objectToJson(detailStoryData));
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;

		case R.id.btn_delete:
			if (detailType == DETAIL_CONSUMER || detailType == DETAIL_NORMAL) {
				centerDeleteConsumer(diaryIndex, new OnDeleteDiaryListener() {
					@Override
					public void onResult(boolean success) {
						if (success)
							finish();
					}
				});
			} else if (detailType == DETAIL_INTERVIEW) {
				centerDeleteInterview(diaryIndex, new OnDeleteDiaryListener() {
					@Override
					public void onResult(boolean success) {
						if (success)
							finish();
					}
				});
			}
			// else if (detailType == DETAIL_NORMAL)
			// {
			// centerDeleteNormal(diaryIndex, new OnDeleteDiaryListener()
			// {
			// @Override
			// public void onResult(boolean success)
			// {
			// if (success)
			// finish();
			// }
			// });
			// }
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
