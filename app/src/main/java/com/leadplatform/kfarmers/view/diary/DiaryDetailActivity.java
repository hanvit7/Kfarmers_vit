package com.leadplatform.kfarmers.view.diary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.DiaryDetailJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.OnDeleteDiaryListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;

public class DiaryDetailActivity extends BaseFragmentActivity implements OnCloseShareDialogListener {
    public static final int DETAIL_DIARY = 0;
    public static final int DETAIL_IMPRESSIVE = 1;
    public static final int DETAIL_FARMER = 2;
    public static final int DETAIL_VILLAGE = 3;
    public static final int DETAIL_FARM_STORY = 4;

    //private ImageButton actionBarFarmHomeBtn;

    public String farmName = "";
    private String diary;

    //private RelativeLayout likeLayout, replyLayout, shareLayout;
    private RelativeLayout replyLayout, farmLayout; //productLayout,farmLayout;
    public TextView actionBarTitleText, likeText, replyText;

    private int detailType = DETAIL_DIARY;

    public ViewPager viewPager;

    private ArrayList<String> diaryList;
    private DetailViewPagerAdapter adapter;

    public HashMap<Integer, DiaryDetailJson> dataHashMap;
    public ArrayList<ProductJson> shopArrayList;
    public boolean isProductCheck = false;

    private ImageView leftArrow, rightArrow;

    /***************************************************************/
    // Override

    /***************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_detail_diary);

		/*likeLayout = (RelativeLayout) findViewById(R.id.Like);
        likeText = (TextView) findViewById(R.id.LikeText);
		shareLayout = (RelativeLayout) findViewById(R.id.Share);*/

        replyLayout = (RelativeLayout) findViewById(R.id.ReplyLayout);
        replyText = (TextView) findViewById(R.id.RepleText);

        //productLayout = (RelativeLayout) findViewById(R.id.ProductHomeLayout);
        farmLayout = (RelativeLayout) findViewById(R.id.ProductHomeLayout);


        viewPager = (ViewPager) findViewById(R.id.viewpager);

        dataHashMap = new HashMap<Integer, DiaryDetailJson>();

        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);

        Intent intent = getIntent();
        if (intent != null) {
            diary = intent.getStringExtra("diary");
            farmName = intent.getStringExtra("farm");
            detailType = intent.getIntExtra("type", DETAIL_DIARY);
        }

        replyLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Reply", null);
                if (getDetailData().FarmerType.equals("F")) {
                    runReplyActivity(
                            ReplyActivity.REPLY_TYPE_FARMER,
                            getDetailData().Farm,
                            getDetailData().Diary);
                } else if (getDetailData().FarmerType.equals("V")) {
                    runReplyActivity(
                            ReplyActivity.REPLY_TYPE_VILLAGE,
                            getDetailData().Farm,
                            getDetailData().Diary);
                }
            }
        });

        farmLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Farm", getDetailData().Farm);

                Intent intent = new Intent(mContext, FarmActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("userType", getDetailData().FarmerType);
                intent.putExtra("userIndex", getDetailData().FarmerIndex);
                startActivity(intent);
            }
        });
		
		/*likeLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				centerLikeDiary(getDetailData().Diary, new OnLikeDiaryListener() {
					@Override
					public void onResult(int code, boolean plus) {
						if (code == 0) {
							int count = Integer.parseInt(getDetailData().Like);
							if (plus) {
								getDetailData().Like = String.valueOf(count + 1);
							} else {
								if (count != 0)
									getDetailData().Like = String.valueOf(count - 1);
							}

							if (!PatternUtil.isEmpty(getDetailData().Like) && !getDetailData().Like.equals("0")) {
								likeText.setText( getString(R.string.GetListDiaryLike) + " (" + getDetailData().Like + ")");
							} else {
								likeText.setText( R.string.GetListDiaryLike "");
							}
						}
					}
				});
			}
		});*/
		

		/*productLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {

				if(shopArrayList != null && shopArrayList.size()>0)
				{
					Intent intent = new Intent(mContext, ShopActivity.class);
					intent.putExtra("farmerId", getDetailData().ID);
					intent.putExtra("farm", farmName);
					startActivity(intent);
				}
				else
				{
					UiDialog.showDialog(mContext, R.string.dialog_no_product);
				}
			}
		});*/
		

/*		shareLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				try {
					KaKaoController.onExportFooterClicked(DiaryDetailActivity.this, JsonUtil.objectToJson(getDetailData()), "");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});*/

        reqDiaryList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
		
		/*ObjectAnimator  objectAnimator = ObjectAnimator.ofFloat(new PagerHintMovement(-20), "progress", -1f, 1f);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.setDuration(1000);
        //objectAnimator.setRepeatCount(3);
       // objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                viewPager.beginFakeDrag();
            }
 
            @Override
            public void onAnimationEnd(Animator animation) {

                viewPager.endFakeDrag();
                viewPager.setCurrentItem(0);
            }
 
            @Override
            public void onAnimationCancel(Animator animation) {
            	viewPager.setCurrentItem(0);
            }
 
            @Override
            public void onAnimationRepeat(Animator animation) {
 
                if(animation.getInterpolator() instanceof AccelerateInterpolator){
                    animation.setInterpolator(new DecelerateInterpolator());
                }else{
                    animation.setInterpolator(new AccelerateInterpolator());
                }
            }
        });
        objectAnimator.setStartDelay(2000);
        objectAnimator.start();*/
    }
	
	
	/*class PagerHintMovement{
		 
	    float goal;
	    float progress;
	 
	    PagerHintMovement(float goal) {
	        this.goal = goal;
	    }
	 
	    public float getProgress() {
	        return progress;
	    }
	 
	    public void setProgress(float progress) {
	        this.progress = progress;
	 
	        if(viewPager.isFakeDragging()){
	            viewPager.fakeDragBy( goal * progress);
	        }
	 
	    }
	}*/


    public DiaryDetailJson getDetailData() {
        DiaryDetailJson detailJson = dataHashMap.get(viewPager.getCurrentItem());
        return detailJson;
    }

    public void setBottomData() {
		/*if (!PatternUtil.isEmpty(getDetailData().Like) && !getDetailData().Like.equals("0")) {
			likeText.setText( getString(R.string.GetListDiaryLike) + " (" + getDetailData().Like + ")");
		} else {
			likeText.setText( R.string.GetListDiaryLike "");
		}*/

        if (getDetailData() != null && getDetailData().Reply != null) {
            if (!PatternUtil.isEmpty(getDetailData().Reply) && !getDetailData().Reply.equals("0")) {
                replyText.setText(/* getString(R.string.GetListDiaryReply) + */" (" + getDetailData().Reply + ")");
            } else {
                replyText.setText(/* R.string.GetListDiaryReply */"");
            }
        } else {
            replyText.setText("");
        }
    }

    public void initView() {
        if (detailType == DETAIL_DIARY || detailType == DETAIL_FARM_STORY) {
            if (!PatternUtil.isEmpty(farmName)) {
                actionBarTitleText.setText(farmName);
                actionBarTitleText.setVisibility(View.VISIBLE);
            } else {
                actionBarTitleText.setVisibility(View.INVISIBLE);
            }
        } else if (detailType == DETAIL_IMPRESSIVE) {
            actionBarTitleText.setText(R.string.title_impressive);
        } else if (detailType == DETAIL_FARMER) {
            actionBarTitleText.setText(R.string.title_farmer);
        } else if (detailType == DETAIL_VILLAGE) {
            actionBarTitleText.setText(R.string.title_village);
        }
    }

    private void reqDiaryList() {
        CenterController.getDiaryList(diary, new CenterResponseListener(mContext) {
            @Override
            public void onSuccess(int Code, String content) {
                super.onSuccess(Code, content);

                try {

                    switch (Code) {
                        case 0000:
                            diaryList = new ArrayList<String>();
                            JsonNode jsonNode = JsonUtil.parseTree(content);

                            JsonNode node = jsonNode.get("Data");

                            for (int i = 0; i < node.get("List").size(); i++) {
                                diaryList.add(node.get("List").get(i).textValue());
                            }

                            adapter = new DetailViewPagerAdapter(getSupportFragmentManager());
                            viewPager.setAdapter(adapter);

                            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                                @Override
                                public void onPageSelected(int arg0) {
                                }

                                @Override
                                public void onPageScrolled(int arg0, float arg1, int arg2) {
                                }

                                @Override
                                public void onPageScrollStateChanged(int arg0) {

                                    if (ViewPager.SCROLL_STATE_IDLE == arg0) {
                                        setBottomData();
                                    }
                                }
                            });
                            viewPager.setCurrentItem(diaryList.indexOf(diary));
                            showArrowBtn(diary);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });

		/*TokenApiController.getDiaryList(diary,mContext, new TokenResponseListener(mContext) {
			@Override
			public void onSuccess(int Code, String content) 
			{

			}
			
			@Override
			public void onFailure(int statusCode,
					Header[] headers, byte[] content,
					Throwable error) {
				super.onFailure(statusCode, headers, content, error);
			}
		});*/
    }
	
	
	/*private void initUserInfo() {
		try {
			String profile = DbController.queryProfileContent(this);
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

    public void showArrowBtn(String diary) {
        if (diary == null) {
            return;
        }

        Animation mAniArrow = AnimationUtils.loadAnimation(DiaryDetailActivity.this, R.anim.arrow_onoff);

        mAniArrow.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                leftArrow.setVisibility(View.GONE);
                rightArrow.setVisibility(View.GONE);
            }
        });

        leftArrow.setAnimation(mAniArrow);
        rightArrow.setAnimation(mAniArrow);

		/*if (diaryList.indexOf(diary) > 0) {

		}
		if (diaryList.indexOf(diary) < diaryList.size() - 1) {

		}*/
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_detail);
        actionBarTitleText = (TextView) findViewById(R.id.actionbar_title_text_view);
		/*actionBarFarmHomeBtn = (ImageButton) findViewById(R.id.FarmHomeBtn);

		actionBarFarmHomeBtn.setOnClickListener(new ViewOnClickListener() {

            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(mContext, FarmActivity.class);
                intent.putExtra("userType", getDetailData().FarmerType);
                intent.putExtra("userIndex", getDetailData().FarmerIndex);
                startActivity(intent);
                    }
		});*/

        initActionBarHomeBtn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Menu", "복사");

                String str = "";
                if (getDetailData() != null) {
                    try {
                        if (getDetailData().Rows != null) {
                            for (RowJson content : getDetailData().Rows) {
                                if (content.Type.equals("Text")) {
                                    str += content.Value;
                                }
                            }
                        }
                        ClipboardManager clip = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        clip.setText(str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;

            case R.id.btn_edit:
                if (getDetailData() != null) {
                    try {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Menu", "수정");
                        Intent intent = DiaryWriteActivity.newIntent(
                                getApplicationContext(),
                                DiaryWriteActivity.DiaryWriteState.IMPORT_FROM_SNS,
                                JsonUtil.objectToJson(getDetailData()));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;

            case R.id.btn_delete:
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Menu", "삭제");

                centerDeleteDiary(getDetailData().Diary, new OnDeleteDiaryListener() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            Intent intent = new Intent();
                            intent.putExtra("diary", diary);
                            intent.putExtra("delete", true);
                            setResult(RESULT_OK, intent);
                            finish();
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
            DiaryDetailJson data = (DiaryDetailJson) JsonUtil.jsonToObject(object, DiaryDetailJson.class);
            if (position == 0) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Share", "카카오톡");
                KaKaoController.sendKakaotalk(DiaryDetailActivity.this, data);
            } else if (position == 1) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_DETAIL, "Click_Share", "카카오스토리");
                KaKaoController.sendKakaostory(DiaryDetailActivity.this, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DetailViewPagerAdapter extends FragmentStatePagerAdapter {

        public DetailViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return DiaryDetailFragment.newInstance(diaryList.get(arg0), detailType, arg0);
        }

        @Override
        public int getCount() {
            return diaryList.size();
        }
    }
}
