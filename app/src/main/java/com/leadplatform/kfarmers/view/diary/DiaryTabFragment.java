package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnViewType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
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
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.StoryListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
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
import com.leadplatform.kfarmers.view.evaluation.ReviewDetailActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class DiaryTabFragment extends BaseRefreshMoreListFragment implements OnCloseCategoryDialogListener, OnCloseShareDialogListener {
    public static final String TAG = "DiaryTabFragment";

    public static final int DIARY_TYPE_FARM = 1;
    public static final int DIARY_TYPE_REVIEW = 2;
    public static final int DIARY_TYPE_STORY = 3;


    public static final int SUBMENU_TYPE_1 = 0;
    public static final int SUBMENU_TYPE_2 = 1;
    public static final int SUBMENU_TYPE_3 = 2;

    private String mLimit = "20";

    private int mSelectType = DIARY_TYPE_FARM;

    private View mDiaryTabView, mTempView;

    private RelativeLayout mTabMenuBox1, mTabMenuBox2, mTabMenuBox3;
    private TextView mTabMenuText1, mTabMenuText2, mTabMenuText3;

    private int subMenuIndex1 = 0, subMenuIndex2 = 0, subMenuIndex3 = 0;
    private LinearLayout subMenuLayout;
    private RelativeLayout subMenuBox1, subMenuBox2, subMenuBox3;
    private TextView subMenuText1, subMenuText2, subMenuText3;
    private String subMenuTitle1, subMenuTitle2, subMenuTitle3;
    private ArrayList<String> subMenuList1, subMenuList2, subMenuList3;

    private boolean bMoreFlag = false;
    private ArrayList<ProductJson> mProductList;
    private MainAllListAdapter mainListAdapter;

    private int mTopBarHeight;

    private int adPos = 0;

    private ArrayList<StoryListJson> mStoryListArray;

    private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;

    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptionsProfile, mOptionProfile2, mOptionsProduct;

    private View mStoryHeaderView;
    private View mImpressiveView;
    private ImpressiveAdapter mImpressiveAdapter;
    private ViewPager mImpressiveViewpager;
    private RelativeLayout mImpressiveTextView, mImpressiveMoreView;
    private LinearLayout mImpressivePagingLayout;
    private TextView mImpressiveText;

    private View mWriteView;
    private ImageView mWriteProfileImageView;
    private TextView mWriteTextView;

    private String mWriteText1 = "";
    private String mWriteText2 = "";

    private ImageButton mTopDiaryBtn;

    private int reviePage = 1;

    public static DiaryTabFragment newInstance(int diaryType) {
        final DiaryTabFragment f = new DiaryTabFragment();

        final Bundle args = new Bundle();
        args.putInt("Type", diaryType);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_STROY_LIST);

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();
        mOptionProfile2 = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 30) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();
        mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_diary_list, container, false);

        if (getArguments() != null) {
            mSelectType = getArguments().getInt("Type", DIARY_TYPE_FARM);
            getArguments().remove("Type");
        } else {
            mSelectType = DIARY_TYPE_FARM;
        }

        adPos = 0;
        mStoryListArray = null;
        bMoreFlag = false;

        headerInit(inflater);

        mTopDiaryBtn = (ImageButton) v.findViewById(R.id.topBtn);
        mTopDiaryBtn.setVisibility(View.GONE);

        subMenuLayout = (LinearLayout) v.findViewById(R.id.SubMenuLayout);
        subMenuBox1 = (RelativeLayout) v.findViewById(R.id.SubMenuBox1);
        subMenuBox2 = (RelativeLayout) v.findViewById(R.id.SubMenuBox2);
        subMenuBox3 = (RelativeLayout) v.findViewById(R.id.SubMenuBox3);
        subMenuText1 = (TextView) v.findViewById(R.id.SubMenu1);
        subMenuText2 = (TextView) v.findViewById(R.id.SubMenu2);
        subMenuText3 = (TextView) v.findViewById(R.id.SubMenu3);

        subMenuList1 = new ArrayList<String>();
        subMenuList2 = new ArrayList<String>();
        subMenuList3 = new ArrayList<String>();

        mDiaryTabView = v.findViewById(R.id.TabMenuLayout);

        mTopBarHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonMediumByLargeRow2);
        mTempView = new View(getActivity());
        mTempView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mTopBarHeight));

        mTabMenuBox1 = (RelativeLayout) mDiaryTabView.findViewById(R.id.TabMenuBox1);
        mTabMenuBox2 = (RelativeLayout) mDiaryTabView.findViewById(R.id.TabMenuBox2);
        mTabMenuBox3 = (RelativeLayout) mDiaryTabView.findViewById(R.id.TabMenuBox3);

        mTabMenuText1 = (TextView) mDiaryTabView.findViewById(R.id.TabMenuText1);
        mTabMenuText2 = (TextView) mDiaryTabView.findViewById(R.id.TabMenuText2);
        mTabMenuText3 = (TextView) mDiaryTabView.findViewById(R.id.TabMenuText3);

        mTabMenuText1.setText("농장소식");
        mTabMenuText2.setText("구매후기");
        mTabMenuText3.setText("일상");

        mTabMenuBox1.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "농장소식");
                mSelectType = DIARY_TYPE_FARM;
                mWriteText1 = "";
                mWriteText2 = "";
                mStoryListArray = null;
                bMoreFlag = false;
                adPos = 0;
                mainListAdapter.clear();
                reviePage = 1;

                selectBtn(true);
                //getListProduct();
                getListDiary(makeListDiaryData(true, 0, ""));
                subMenuLayout.setVisibility(View.VISIBLE);

            }
        });

        mTabMenuBox2.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "구매후기");
                mSelectType = DIARY_TYPE_REVIEW;
                mWriteText1 = "";
                mWriteText2 = "";
                mStoryListArray = null;
                bMoreFlag = false;
                adPos = 0;
                mainListAdapter.clear();
                reviePage = 1;

                selectBtn(true);
                getReviewData(true);
                //getListDiary(makeListDiaryData(true, 0,""));
                subMenuLayout.setVisibility(View.GONE);
            }
        });

        mTabMenuBox3.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "일상");
                mSelectType = DIARY_TYPE_STORY;
                mWriteText1 = "";
                mWriteText2 = "";
                mStoryListArray = null;
                bMoreFlag = false;
                adPos = 0;
                mainListAdapter.clear();
                reviePage = 1;

                selectBtn(true);
                getListDaily(true, "");
                //getListDiary(makeListDiaryData(true, 0,""));
                subMenuLayout.setVisibility(View.GONE);
            }
        });

        mTopDiaryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getListView().smoothScrollBy(0, 0);
                getListView().setSelectionFromTop(0, 0);

            }
        });
        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bMoreFlag = false;
        adPos = 0;

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        if (getListAdapter() == null) {
            getListView().addHeaderView(mTempView);
            getListView().addHeaderView(mStoryHeaderView);
        } else {
            if (getListView().getHeaderViewsCount() == 0) {
                setListAdapter(null);
                getListView().addHeaderView(mTempView);
                getListView().addHeaderView(mStoryHeaderView);
            }
        }

        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
        /*if (mainListAdapter == null) {
            mainListAdapter = new MainAllListAdapter(getSherlockActivity(), R.layout.item_diary, new ArrayList(),
					((BaseFragmentActivity) getSherlockActivity()).imageLoader, AppPreferences.getLatitude(getSherlockActivity()),
					AppPreferences.getLongitude(getSherlockActivity()));
		}*/

        mainListAdapter = new MainAllListAdapter(getSherlockActivity(), R.layout.item_diary, new ArrayList(), AppPreferences.getLatitude(getSherlockActivity()),
                AppPreferences.getLongitude(getSherlockActivity()));

        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mainListAdapter);
        mSwingBottomInAnimationAdapter.setAbsListView(getListView());

        assert mSwingBottomInAnimationAdapter.getViewAnimator() != null;
        mSwingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);

        setListAdapter(mSwingBottomInAnimationAdapter);

        QuickReturnListViewOnScrollListener scrollListener;
        scrollListener = new QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.TWITTER)
                .header(mDiaryTabView)
                .minHeaderTranslation(-mTopBarHeight)
                .footer(subMenuLayout)
                .minFooterTranslation(mTopBarHeight)
                .isSnappable(true)
                .build();
        scrollListener.registerExtraOnScrollListener(onScrollListener);

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem > 1) {
                    mTopDiaryBtn.setVisibility(View.VISIBLE);
                } else {
                    mTopDiaryBtn.setVisibility(View.GONE);
                }
            }
        };
        scrollListener.registerExtraOnScrollListener(onScrollListener);
        getListView().setOnScrollListener(scrollListener);

        setButtonText(SUBMENU_TYPE_1, subMenuIndex1);
        setButtonText(SUBMENU_TYPE_2, subMenuIndex2);
        setButtonText(SUBMENU_TYPE_3, subMenuIndex3);

        subMenuBox1.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSubMenuBtnClicked(SUBMENU_TYPE_1);
            }
        });
        subMenuBox2.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSubMenuBtnClicked(SUBMENU_TYPE_2);
            }
        });
        subMenuBox3.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSubMenuBtnClicked(SUBMENU_TYPE_3);
            }
        });

        getListDiary(makeListDiaryData(true, 0, ""));
        //getListProduct();

    }

    private ProfileJson getUserProfile() {

        if (!AppPreferences.getLogin(getActivity())) {
            return null;
        }
        try {
            String profile = DbController.queryProfileContent(getSherlockActivity());
            JsonNode root = JsonUtil.parseTree(profile);
            return (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        selectBtn(false);
        writeViewSetting();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_DETAIL_DIARY) {
                if (data.getBooleanExtra("delete", false)) {
                    String diary = data.getStringExtra("diary");
                    for (int index = 0; index < mainListAdapter.getCount(); index++) {
                        if (mainListAdapter.getItem(index) instanceof DiaryListJson) {
                            DiaryListJson item = (DiaryListJson) mainListAdapter.getItem(index);
                            if (item.Diary.equals(diary)) {
                                mainListAdapter.remove(item);
                                mainListAdapter.notifyDataSetChanged();
                                break;
                            }
                        } else if (mainListAdapter.getItem(index) instanceof StoryListJson) {
                            StoryListJson item = (StoryListJson) mainListAdapter.getItem(index);
                            if (item.DiaryIndex.equals(diary)) {
                                mainListAdapter.remove(item);
                                mainListAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void headerInit(LayoutInflater inflater) {

        mStoryHeaderView = inflater.inflate(R.layout.item_story_header, null);

        mWriteView = mStoryHeaderView.findViewById(R.id.wirteView);
        mWriteProfileImageView = (ImageView) mStoryHeaderView.findViewById(R.id.profileImageView);
        mWriteTextView = (TextView) mStoryHeaderView.findViewById(R.id.textView);
        mWriteView.setVisibility(View.GONE);

        mImpressiveView = mStoryHeaderView.findViewById(R.id.ImpressiveView);
        mImpressiveTextView = (RelativeLayout) mStoryHeaderView.findViewById(R.id.ImpressiveTextView);
        mImpressiveMoreView = (RelativeLayout) mStoryHeaderView.findViewById(R.id.ImpressiveMoreView);

        mImpressiveViewpager = (ViewPager) mStoryHeaderView.findViewById(R.id.ImpressiveViewpager);
        mImpressivePagingLayout = (LinearLayout) mStoryHeaderView.findViewById(R.id.ImpressivePagingLayout);
        mImpressiveText = (TextView) mStoryHeaderView.findViewById(R.id.ImpressiveText);

        mImpressiveAdapter = new ImpressiveAdapter(getActivity(), new ArrayList<StoryListJson>(), ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
        mImpressiveViewpager.setAdapter(mImpressiveAdapter);

        mImpressiveViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (mImpressiveAdapter.getCount() > 0) {
                    if (5 == mImpressiveAdapter.getCount() - 1) {
                        mImpressiveMoreView.setVisibility(View.VISIBLE);
                        mImpressiveTextView.setVisibility(View.GONE);
                    } else {
                        String text = mImpressiveAdapter.items.get(mImpressiveViewpager.getCurrentItem()).Description;
                        while (text.startsWith("\n")) {
                            text = text.replaceFirst("\n", "");
                        }
                        mImpressiveText.setText(text);

                        mImpressiveMoreView.setVisibility(View.GONE);
                        mImpressiveTextView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Swipe_Impressive", String.valueOf(mImpressiveViewpager.getCurrentItem()));
                displayViewPagerIndicator(mImpressiveViewpager.getCurrentItem());
            }
        });

        mWriteView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                ProfileJson profileJson = getUserProfile();
                if (profileJson == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                switch (mSelectType) {
                    case DIARY_TYPE_FARM:
                        ((BaseFragmentActivity) getActivity()).runWriteDiaryActivity(profileJson.Type);
                        break;
                    case DIARY_TYPE_STORY:
                        ((BaseFragmentActivity) getActivity()).runWriteDiaryActivity(profileJson.Type);
                        break;
                }
            }
        });
    }

    private void displayViewPagerIndicator(int position) {
        mImpressivePagingLayout.removeAllViews();

        for (int i = 0; i < mImpressiveAdapter.getCount(); i++) {
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

            mImpressivePagingLayout.addView(dot);
        }
    }

    private void writeViewSetting() {

        if (mWriteText1.isEmpty()) {
            mWriteView.setVisibility(View.GONE);
            return;
        }

        ProfileJson profileJson = getUserProfile();
        switch (mSelectType) {
            case DIARY_TYPE_FARM:
                if (profileJson != null && (profileJson.Type.equals("F") || profileJson.Type.equals("V"))) {
                    if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
                        mImageLoader.displayImage(profileJson.ProfileImage, mWriteProfileImageView, mOptionsProfile);
                    }
                    if (profileJson.PermissionFlag.equals("Y") && profileJson.TemporaryPermissionFlag.equals("Y")) {
                        mWriteTextView.setHint(mWriteText1);
                        mWriteView.setVisibility(View.VISIBLE);
                    } else if (profileJson.PermissionFlag.equals("Y") && profileJson.TemporaryPermissionFlag.equals("N")) {
                        mWriteTextView.setHint(mWriteText2);
                        mWriteView.setVisibility(View.VISIBLE);
                    } else {
                        mWriteView.setVisibility(View.GONE);
                        mWriteProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                    }
                } else {
                    mWriteView.setVisibility(View.GONE);
                    mWriteProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                }
                break;
            case DIARY_TYPE_STORY:
                if (profileJson != null && (profileJson.Type.equals("F") || profileJson.Type.equals("V"))) {
                    mWriteView.setVisibility(View.VISIBLE);
                    mWriteTextView.setHint(mWriteText1);
                    if (profileJson != null && !PatternUtil.isEmpty(profileJson.ProfileImage)) {
                        mImageLoader.displayImage(profileJson.ProfileImage, mWriteProfileImageView, mOptionsProfile);
                    } else {
                        mWriteProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                    }
                } else {
                    mWriteView.setVisibility(View.GONE);
                    mWriteProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                }
                break;
        }
    }

    private void selectBtn(boolean isUser) {
        if (isUser)
            CenterController.cancel(getActivity(), true);

        subMenuBox2.setVisibility(View.VISIBLE);
        subMenuList1.clear();
        subMenuList2.clear();
        subMenuList3.clear();
        subMenuIndex1 = 0;
        subMenuIndex2 = 0;
        subMenuIndex3 = 0;

        mWriteView.setVisibility(View.GONE);

        switch (mSelectType) {
            case DIARY_TYPE_FARM: {
                setButtonText(SUBMENU_TYPE_1, subMenuIndex1);
                setButtonText(SUBMENU_TYPE_2, subMenuIndex2);
                setButtonText(SUBMENU_TYPE_3, subMenuIndex3);

                mTabMenuText1.setTypeface(null, Typeface.BOLD);
                mTabMenuText2.setTypeface(null, Typeface.NORMAL);
                mTabMenuText3.setTypeface(null, Typeface.NORMAL);
                //mTabMenuText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonMediumText_1));
                //mTabMenuText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                //mTabMenuText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                mImpressiveView.setVisibility(View.GONE);
                break;
            }
            case DIARY_TYPE_REVIEW: {
                /*setButtonText(SUBMENU_TYPE_1,subMenuIndex1);
                setButtonText(SUBMENU_TYPE_2,subMenuIndex2);
                setButtonText(SUBMENU_TYPE_3,subMenuIndex3);*/

                mTabMenuText1.setTypeface(null, Typeface.NORMAL);
                mTabMenuText2.setTypeface(null, Typeface.BOLD);
                mTabMenuText3.setTypeface(null, Typeface.NORMAL);
                //mTabMenuText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                //mTabMenuText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonMediumText));
                //mTabMenuText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                subMenuLayout.setVisibility(View.GONE);
                break;
            }
            case DIARY_TYPE_STORY: {
                mTabMenuText1.setTypeface(null, Typeface.NORMAL);
                mTabMenuText2.setTypeface(null, Typeface.NORMAL);
                mTabMenuText3.setTypeface(null, Typeface.BOLD);
                //mTabMenuText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                //mTabMenuText2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonSmallByMediumText));
                //mTabMenuText3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.CommonMediumText));
                subMenuLayout.setVisibility(View.GONE);
                mImpressiveView.setVisibility(View.GONE);
                break;
            }
        }
    }


    private void getTodayTagDetailList(boolean isInitFlag, String oldIndex) {

        if (isInitFlag) {
            bMoreFlag = false;
            mainListAdapter.clear();
        }
    }


    private void getListProduct() {
        SnipeApiController.getProductListRecomend(String.valueOf(30), String.valueOf(30), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                mProductList = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
                            }
                            getListDiary(makeListDiaryData(true, 0, ""));
                            break;
                        default:
                            getListDiary(makeListDiaryData(true, 0, ""));
                            break;
                    }
                } catch (Exception e) {
                    getListDiary(makeListDiaryData(true, 0, ""));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                getListDiary(makeListDiaryData(true, 0, ""));
            }
        });
    }

    private void getListDiary(DiaryListData data) {
        if (data == null)
            return;

        if (data.isInitFlag()) {
            bMoreFlag = false;
            mainListAdapter.clear();
        }

        // UiController.showProgressDialogFragment(getView());
        CenterController.getListDiary(getActivity(), data, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);

                            if (root.has("Text") && !root.get("Text").toString().trim().isEmpty() && !root.get("Text").asText().equals("null")) {
                                mWriteText1 = root.get("Text").asText();
                            } else {
                                mWriteText1 = "영농일기를 올려주세요";
                            }

                            if (root.has("Text2") && !root.get("Text2").asText().trim().isEmpty() && !root.get("Text2").asText().equals("null")) {
                                mWriteText2 = root.get("Text2").asText();
                            } else {
                                mWriteText2 = "영농일기를 올려주세요";
                            }
                            writeViewSetting();

                            if (root.findPath("List").isArray()) {
                                int diaryCount = 0;
                                int adCount = 0;
                                for (JsonNode jsonNode : root.findPath("List")) {
                                    diaryCount++;
                                    adCount++;
                                    DiaryListJson diary = (DiaryListJson) JsonUtil.jsonToObject(jsonNode.toString(), DiaryListJson.class);
                                    mainListAdapter.add(diary);

									/*if (mainListAdapter.getCount() == 3) {
                                        mainListAdapter.add(mProductList.get(0));
									*//*adCount = 0;
                                    adPos++;
									if(mProductList.size() <= adPos) {
										adPos = 0;
									}*//*
									}*/
                                }

                                bMoreFlag = diaryCount == 20;
                            }
                            break;
                    }
                    //UiController.hideProgressDialogFragment(getView());
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    //UiController.hideProgressDialogFragment(getView());
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

    private void getReviewData(boolean isClear) {

        if (isClear) {
            bMoreFlag = false;
            reviePage = 1;
            mainListAdapter.clear();
        }
        SnipeApiController.getReviewList("", "", "", String.valueOf(reviePage), mLimit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                ArrayList<ReviewListJson> evaluationList = (ArrayList<ReviewListJson>) JsonUtil.jsonToArrayObject(root.path("item"), ReviewListJson.class);
                                mainListAdapter.addAll(evaluationList);

                                if (evaluationList.size() == Integer.parseInt(mLimit)) {
                                    reviePage++;
                                    bMoreFlag = true;
                                } else {
                                    bMoreFlag = false;
                                }
                                break;
                            }
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                onRefreshComplete();
                onLoadMoreComplete();
            }
        });
    }

    private void getListDaily(boolean isInitflag, String oldDate) {

        if (isInitflag) {
            bMoreFlag = false;
            mainListAdapter.clear();
        }

        // UiController.showProgressDialogFragment(getView());
        CenterController.getDailyList(oldDate, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);

                            if (root.has("Text") && !root.get("Text").asText().trim().isEmpty() && !root.get("Text").asText().equals("null")) {
                                mWriteText1 = root.get("Text").asText();
                                writeViewSetting();
                            } else {
                                mWriteText1 = "일상 이야기를 올려주세요";
                                writeViewSetting();
                            }

                            if (root.findPath("List").isArray()) {
                                int diaryCount = 0;
                                for (JsonNode jsonNode : root.findPath("List")) {
                                    diaryCount++;
                                    DiaryListJson diary = (DiaryListJson) JsonUtil.jsonToObject(jsonNode.toString(), DiaryListJson.class);

                                    if (diary.Image != null) diary.ProductImage1 = diary.Image;
                                    if (diary.Name != null) {
                                        diary.FarmerName = diary.Name;
                                        diary.Farm = diary.FarmerName;
                                    }

                                    if (diary.MemberIndex != null)
                                        diary.FarmerIndex = diary.MemberIndex;
                                    if (diary.CategoryName == null || diary.CategoryName.isEmpty())
                                        diary.CategoryName = "일상";

                                    mainListAdapter.add(diary);
                                }

                                bMoreFlag = diaryCount == 20;
                            }
                            break;
                    }
                    //UiController.hideProgressDialogFragment(getView());
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    //UiController.hideProgressDialogFragment(getView());
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
        private double userLatitude, userLongitude;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d";
        SimpleDateFormat format, format2;

        public MainAllListAdapter(Context context, int itemLayoutResourceId, ArrayList items, double userLatitude, double userLongitude) {
            super(context, itemLayoutResourceId, items);
            this.itemLayoutResourceId = itemLayoutResourceId;

            // this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            // .bitmapConfig(Config.RGB_565).displayer(new RoundedBitmapDisplayer(100)).showImageOnLoading(R.drawable.common_dummy).build();

            this.userLatitude = userLatitude;
            this.userLongitude = userLongitude;

            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //DiaryListHolder holder;

            if (getItem(position) != null) {
                if (getItem(position) instanceof ReviewListJson) {
                    if (convertView == null || !convertView.getTag(R.integer.tag_id).equals("review")) {
                        LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.item_review_list, null);
                        convertView.setTag(R.integer.tag_id, "review");
                    }
                } else if (getItem(position) instanceof DiaryListJson || getItem(position) instanceof ProductJson) {
                    if (convertView == null || !convertView.getTag(R.integer.tag_id).equals("diary")) {
                        LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(itemLayoutResourceId, null);
                        convertView.setTag(R.integer.tag_id, "diary");
                    }
                }

                if (getItem(position) instanceof ReviewListJson) {

                    ReviewListJson item = (ReviewListJson) getItem(position);

                    View view = ViewHolder.get(convertView, R.id.RootView);
                    ImageView profile = ViewHolder.get(convertView, R.id.Profile);
                    TextView name = ViewHolder.get(convertView, R.id.Name);
                    TextView ratingText = ViewHolder.get(convertView, R.id.RatingText);
                    TextView description = ViewHolder.get(convertView, R.id.Description);
                    TextView date = ViewHolder.get(convertView, R.id.Date);
                    //TextView typeText = ViewHolder.get(convertView, R.id.TypeText);
                    View replyLatyout = ViewHolder.get(convertView, R.id.reply_layout);
                    View replyOnLatyout = ViewHolder.get(convertView, R.id.reply_on);
                    View replyOffLatyout = ViewHolder.get(convertView, R.id.reply_off);

                    ImageView reply_profile = ViewHolder.get(convertView, R.id.reply_profile);
                    TextView reply_name = ViewHolder.get(convertView, R.id.reply_name);
                    TextView reply_text = ViewHolder.get(convertView, R.id.reply_text);
                    TextView reply_count = ViewHolder.get(convertView, R.id.reply_count);

                    RatingBar ratingBar = ViewHolder.get(convertView, R.id.Ratingbar);

                    //LinearLayout imageLayout = ViewHolder.get(convertView, R.id.ImageLayout);

                    ImageView imageView = ViewHolder.get(convertView, R.id.imageview);

                    view.setTag(position);
                    view.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(View v) {
                            int pos = (int) v.getTag();
                            Intent intent = new Intent(getActivity(), ReviewDetailActivity.class);
                            intent.putExtra("data", (Serializable) getItem(pos));
                            startActivity(intent);
                        }
                    });

                    if (item.file != null && item.file.size() > 0) {
                        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.file.get(0).path, imageView, mOptionsProduct);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        imageView.setVisibility(View.GONE);
                    }

                    if (item.member_profile_image != null && !item.member_profile_image.isEmpty()) {
                        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.member_profile_image, profile, mOptionsProfile);
                    } else {
                        profile.setImageResource(R.drawable.icon_empty_profile);
                    }

                /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);

                imageLayout.removeAllViews();

                int i = 0;
                for (ReviewListJson.File file : item.file) {
                    View view = mInflater.inflate(R.layout.item_review_big_image, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.Image);
                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + file.path, imageView, mImageOption);
                    imageLayout.addView(view, layoutParams);

                    imageView.setTag(R.integer.tag_pos,position);
                    imageView.setTag(R.integer.tag_id,i);

                    imageView.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(View v) {
                            int pos = (int) v.getTag(R.integer.tag_pos);
                            int i = (int) v.getTag(R.integer.tag_id);

                            ArrayList<String> imgArrayList = new ArrayList<String>();
                            ReviewListJson item = (ReviewListJson) getItem(pos);

                            for (ReviewListJson.File file : item.file) {
                                imgArrayList.add(Constants.KFARMERS_SNIPE_IMG + file.path);
                            }
                            Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                            intent.putExtra("pos", i);
                            intent.putStringArrayListExtra("imageArrary", imgArrayList);
                            startActivity(intent);
                        }
                    });
                    i++;
                }*/

                    if (item.member_name == null || item.member_name.isEmpty()) {
                        name.setText("소비자 회원");
                    } else {
                        name.setText(item.member_name);
                    }

                    ratingText.setText(item.rating + "점");
                    description.setText(item.comment);

                /*description.setTag(position);
                description.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int i = (int) v.getTag();
                        if (((TextView) v).getLineCount() >= 2) {
                            ReviewListJson reviewListJson = (ReviewListJson) getItem(i);
                            UiDialog.showDialog(getActivity(), "리뷰", reviewListJson.comment);
                        }
                    }
                });*/

                    try {
                        date.setText(format2.format((format.parse(item.datetime).getTime())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                /*if (item.division.equals("E")) {
                    typeText.setText(item.division_text);
                    typeText.setVisibility(View.VISIBLE);
                } else {
                    typeText.setText("");
                    typeText.setVisibility(View.GONE);
                }*/

                    float rating = Float.parseFloat(item.rating) / 2f;
                    ratingBar.setRating(rating);

                    if (item.prodcut != null) {

                        LinearLayout root = ViewHolder.get(convertView, R.id.root);

                        ImageView img = ViewHolder.get(convertView, R.id.image);
                        ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);

                        TextView des = ViewHolder.get(convertView, R.id.des);
                        TextView price = ViewHolder.get(convertView, R.id.price);
                        TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
                        TextView summary = ViewHolder.get(convertView, R.id.summary);
                        TextView dDay = ViewHolder.get(convertView, R.id.textDday);
                        img_profile.setVisibility(View.GONE);

                        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.prodcut.image1, img, mOptionsProduct);

                        if (item.prodcut.summary != null && !item.prodcut.summary.isEmpty()) {
                            summary.setVisibility(View.VISIBLE);
                            summary.setText(item.prodcut.summary);
                        } else {
                            summary.setVisibility(View.GONE);
                        }

                        //des.setText(item.name);
                        des.setText(item.prodcut.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                        int itemPrice = (int) Double.parseDouble(item.prodcut.price);
                        int itemBuyPrice = (int) Double.parseDouble(item.prodcut.buyprice);

                        if (itemPrice > itemBuyPrice) {
                            price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice) + getResources().getString(R.string.korea_won));
                            dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                            price.setVisibility(View.VISIBLE);

                            price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            price.setText("");
                            dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                            price.setVisibility(View.GONE);
                            price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

                        }

                        root.setTag(position);
                        root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                int pos = (int) v.getTag();
                                ReviewListJson item = (ReviewListJson) getItem(pos);

                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_REVIEW_LIST, "Click_Product", item.prodcut.name);

                                Intent intent = new Intent(getActivity(), ProductActivity.class);
                                intent.putExtra("productIndex", item.prodcut.idx);
                                startActivity(intent);
                            }
                        });

                        if (item.prodcut.duration.startsWith("D") && !item.prodcut.duration.equals("D-day")) {
                            dDay.setText(item.prodcut.duration);
                            dDay.setVisibility(View.VISIBLE);
                        } else {
                            dDay.setVisibility(View.GONE);
                        }
                    }

                    if (item.comments != null) {
                        replyOnLatyout.setVisibility(View.VISIBLE);
                        replyOffLatyout.setVisibility(View.VISIBLE);

                        if (item.comments.profile_image != null && !item.comments.profile_image.isEmpty()) {
                            mImageLoader.displayImage(item.comments.profile_image, reply_profile, mOptionProfile2);
                        } else {
                            reply_profile.setImageResource(R.drawable.icon_empty_profile);
                        }

                        reply_name.setText(item.comments.target_name);

                        String des[] = item.comments.description.split("==]Name]==");
                        if (des.length > 1) {
                            reply_text.setText(des[1]);
                        } else {
                            reply_text.setText(item.comments.description);
                        }
                        reply_count.setText("(" + item.comments_count + ")");
                    } else {
                        replyOnLatyout.setVisibility(View.GONE);
                        replyOffLatyout.setVisibility(View.VISIBLE);
                        reply_count.setText("");
                    }

                    replyLatyout.setTag(item.idx);
                    replyLatyout.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(View v) {
                            String idx = (String) v.getTag();
                            ((MainActivity) getActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_REVIEW, "구매후기 댓글", idx);
                        }
                    });

                } else if (getItem(position) instanceof ProductJson) {

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
                    LinearLayout adContentLayout = ViewHolder.get(convertView, R.id.AdViewContent);
                    TextView Pipe = ViewHolder.get(convertView, R.id.Pipe);

                    adContentLayout.removeAllViews();

                    adLayout.setVisibility(View.VISIBLE);
                    DiaryView.setVisibility(View.GONE);

                    LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    View line = new View(getActivity());
                    line.setBackgroundColor(Color.parseColor("#eaeaea"));

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                    layoutParams.setMargins(getResources().getDimensionPixelOffset(R.dimen.CommonSsmallMargin), 0, getResources().getDimensionPixelOffset(R.dimen.CommonSsmallMargin), 0);
                    line.setLayoutParams(layoutParams);
                    adContentLayout.addView(line);

                    for (int i = 0; i <= mProductList.size(); i++) {

                        ProductJson item = mProductList.get(i);

                        View view = inflater.inflate(R.layout.item_story_product, null);
                        ImageView adImage = ViewHolder.get(view, R.id.image);
                        TextView adDes = ViewHolder.get(view, R.id.des);
                        TextView adPrice = ViewHolder.get(view, R.id.price);
                        TextView adDcPirce = ViewHolder.get(view, R.id.dcPrice);
                        //TextView adStar = ViewHolder.get(view, R.id.star);
                        //RatingBar adRatingBar = ViewHolder.get(view,R.id.ratingbar);

                        adDes.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                        int itemPrice = (int) Double.parseDouble(item.price);
                        int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

                        if (itemPrice > itemBuyPrice) {
                            adPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice) + getResources().getString(R.string.korea_won));
                            adDcPirce.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                            adPrice.setVisibility(View.VISIBLE);

                            adPrice.setPaintFlags(adPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            adPrice.setText("");
                            adDcPirce.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                            adPrice.setVisibility(View.GONE);
                            adPrice.setPaintFlags(adPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }

					/*if(!item.rating_count.equals("0")) {
						adStar.setText(item.rating + "%");
						float rating = (Float.parseFloat(item.rating)/10)/2f;
						adRatingBar.setRating(rating);
					} else {
						adStar.setVisibility(View.GONE);
						adRatingBar.setVisibility(View.GONE);
					}*/

                        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, adImage, mOptionsProduct);
                        //mImageLoader.displayImage(productListJson.ProfileImage,profileImageView,mOptionsProfile);

                        view.setTag(item.idx);
                        view.setTag(R.integer.tag_id, item.name);

                        view.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {
                                Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
                                intent.putExtra("productIndex", (String) v.getTag());
                                startActivity(intent);
                            }
                        });

                        adContentLayout.addView(view);

                        if (i == 2) break;
                        line = new View(getActivity());
                        line.setBackgroundColor(Color.parseColor("#eaeaea"));
                        line.setLayoutParams(layoutParams);
                        adContentLayout.addView(line);
                    }
                } else {

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
                    LinearLayout adContentLayout = ViewHolder.get(convertView, R.id.AdViewContent);
                    TextView Pipe = ViewHolder.get(convertView, R.id.Pipe);

                    adContentLayout.removeAllViews();

                    adLayout.setVisibility(View.GONE);
                    DiaryView.setVisibility(View.VISIBLE);
                    final DiaryListJson diary = (DiaryListJson) getItem(position);

                    if (diary != null) {

                        rootLayout.setTag(diary.Diary);
                        rootLayout.setTag(R.id.Tag1, diary.Farm);
                        rootLayout.setTag(R.integer.tag_st, diary.BoardType);
                        rootLayout.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {

                                String diary = (String) v.getTag();
                                String type = (String) v.getTag(R.integer.tag_st);
                                String name = (String) v.getTag(R.id.Tag1);

                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item", name);

                                if (type == null || type.equals("D")) {
                                    Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
                                    intent.putExtra("diary", diary);
                                    intent.putExtra("farm", name);
                                    intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
                                    startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
                                } else {
                                    Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
                                    intent.putExtra("type", "daily");
                                    intent.putExtra("diary", diary);
                                    intent.putExtra("name", name);
                                    startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
                                }
                            }
                        });

                        if (!PatternUtil.isEmpty(diary.ProfileImage)) {
                            mImageLoader.displayImage(diary.ProfileImage, Profile, mOptionsProfile);
                        } else {
                            Profile.setImageResource(R.drawable.icon_empty_profile);
                        }

                        if (!PatternUtil.isEmpty(diary.FarmerName)) {
                            Farmer.setText(diary.FarmerName);
                            Farmer.setVisibility(View.VISIBLE);
                        } else {
                            Farmer.setVisibility(View.INVISIBLE);
                        }

                        if (!PatternUtil.isEmpty(diary.CategoryName)) {
                            Category.setText(diary.CategoryName);
                            Category.setVisibility(View.VISIBLE);
                            Pipe.setVisibility(View.VISIBLE);
                        } else {
                            Category.setVisibility(View.INVISIBLE);
                            Pipe.setVisibility(View.INVISIBLE);
                        }

                        Category.setOnClickListener(null);

                        if (diary.Verification != null && !diary.Verification.equals("N")) {
                            Verification.setVisibility(View.VISIBLE);
                        } else {
                            Verification.setVisibility(View.GONE);
                        }

                        if (diary.Auths != null) {
                            Auth1.setVisibility(View.VISIBLE);
                        } else {
                            Auth1.setVisibility(View.GONE);
                        }

                        if (!PatternUtil.isEmpty(diary.RegistrationDate)) {
                            Date.setText(diary.RegistrationDate);
                            Date.setVisibility(View.VISIBLE);
                        } else {
                            Date.setVisibility(View.INVISIBLE);
                        }

                        if (!PatternUtil.isEmpty(diary.Description)) {
                            Description.setText(diary.Description);
                            Description.setVisibility(View.VISIBLE);
                        } else {
                            Description.setVisibility(View.GONE);
                        }

                        if (!PatternUtil.isEmpty(diary.Like) && !diary.Like.equals("0")) {
                            LikeText.setText(diary.Like);
                            LikeText.setVisibility(View.VISIBLE);
                        } else {
                            LikeText.setVisibility(View.GONE);
                        }

                        if (!PatternUtil.isEmpty(diary.Reply) && !diary.Reply.equals("0")) {
                            ReplyText.setText(diary.Reply);
                            ReplyText.setVisibility(View.VISIBLE);
                        } else {
                            ReplyText.setVisibility(View.GONE);
                        }

                        if (diary.ProductFlag2 != null && diary.ProductFlag2.equals("T")) {
                            //holder.FarmHomeBtn.setVisibility(View.GONE);
                            FarmImageView.setVisibility(View.VISIBLE);
                            FarmImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-FarmerShop", diary.Farm);

                                    Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);
                                    intent.putExtra("id", diary.ID);
                                    intent.putExtra("name", diary.Farm);
                                    intent.putExtra("type", ShopActivity.type.Farm);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            FarmImageView.setVisibility(View.GONE);

/*					if (diary.Type.equals("F")) {
						holder.FarmHomeBtn.setVisibility(View.VISIBLE);
						holder.FarmHomeBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
								intent.putExtra("userType", diary.Type);
								intent.putExtra("userIndex", diary.FarmerIndex);
								startActivity(intent);
							}
						});
					}*/
                        }

                        Like.setTag(new LikeTag(diary.Diary, position, diary.BoardType));
                        Like.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {
                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Like", null);

                                final LikeTag tag = (LikeTag) v.getTag();

                                if (tag.boardType == null || tag.boardType.equals("D")) {
                                    ((BaseFragmentActivity) getSherlockActivity()).centerLikeDiary(tag.index, new OnLikeDiaryListener() {
                                        @Override
                                        public void onResult(int code, boolean plus) {
                                            if (code == 0) {
                                                int count = Integer.parseInt((((DiaryListJson) getItem(tag.position)).Like));
                                                if (plus) {
                                                    ((DiaryListJson) getItem(tag.position)).Like = String.valueOf(count + 1);
                                                } else {
                                                    if (count != 0)
                                                        ((DiaryListJson) getItem(tag.position)).Like = String.valueOf(count - 1);
                                                }
                                                mainListAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                } else {
                                    ((BaseFragmentActivity) getSherlockActivity()).centerLikeUserDiary(tag.index, new OnLikeDiaryListener() {
                                        @Override
                                        public void onResult(int code, boolean plus) {
                                            if (code == 0) {
                                                int count = Integer.parseInt((((DiaryListJson) getItem(tag.position)).Like));
                                                if (plus) {
                                                    ((DiaryListJson) getItem(tag.position)).Like = String.valueOf(count + 1);
                                                } else {
                                                    if (count != 0)
                                                        ((DiaryListJson) getItem(tag.position)).Like = String.valueOf(count - 1);
                                                }
                                                mainListAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        Reply.setTag(diary.BoardType);
                        Reply.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {
                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Reply", null);

                                String tag = (String) v.getTag();

                                if (tag == null || tag.equals("D")) {
                                    if (diary.Type.equals("F")) {
                                        ((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_FARMER, diary.Farm, diary.Diary);
                                    } else if (diary.Type.equals("V")) {
                                        ((BaseFragmentActivity) getSherlockActivity())
                                                .runReplyActivity(ReplyActivity.REPLY_TYPE_VILLAGE, diary.Farm, diary.Diary);
                                    }
                                } else {
                                    ((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_NORMAL, diary.Name, diary.Diary);
                                }
                            }
                        });

                        Share.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {
                                try {
                                    KaKaoController.onShareBtnClicked(getSherlockActivity(), JsonUtil.objectToJson(diary), MainActivity.MainTab.DIARY.toString());
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
                            String distance = "푸드마일";
                            FoodMile.setText(distance + (int) meters / 1000 + "km");
                            FoodMile.setVisibility(View.VISIBLE);

                            if (!PatternUtil.isEmpty(diary.AddressKeyword1) && !PatternUtil.isEmpty(diary.AddressKeyword2)) {
                                Address.setText(diary.AddressKeyword1 + " > " + diary.AddressKeyword2);
                                Address.setVisibility(View.VISIBLE);
                            } else {
                                Address.setVisibility(View.GONE);
                            }
                        } else {
                            Address.setVisibility(View.GONE);
                            FoodMile.setVisibility(View.GONE);
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

                        if (images.size() != 0) {

                            mImageLoader.displayImage(images.get(0), imageView, mOptionsProduct);

                            imageView.setVisibility(View.VISIBLE);
					/*holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(getSherlockActivity(), DiaryTabFragment.this,
							Constants.REQUEST_DETAIL_DIARY, diary.Diary, DiaryDetailActivity.DETAIL_DIARY, images, imageLoader));
					holder.imageViewPager.setPageMargin((int) getResources().getDimension(R.dimen.image_pager_margin));
					holder.imageViewPager.setVisibility(View.VISIBLE);*/

                            imageView.setTag(diary.Diary);
                            imageView.setTag(R.id.Tag1, diary.Farm);
                            imageView.setTag(R.integer.tag_st, diary.BoardType);

                            imageView.setOnClickListener(new ViewOnClickListener() {
                                @Override
                                public void viewOnClick(View v) {

                                    String diary = (String) v.getTag();
                                    String type = (String) v.getTag(R.integer.tag_st);
                                    String name = (String) v.getTag(R.id.Tag1);

                                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Story", (String) v.getTag(R.id.Tag1));

                                    if (type == null || type.equals("D")) {
                                        Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
                                        intent.putExtra("diary", diary);
                                        intent.putExtra("farm", name);
                                        intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
                                        startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
                                    } else {
                                        Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
                                        intent.putExtra("type", "daily");
                                        intent.putExtra("diary", diary);
                                        intent.putExtra("name", name);
                                        startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
                                    }
                                }
                            });

                        } else {
                            imageView.setVisibility(View.GONE);
                            //holder.imageViewPager.setVisibility(View.GONE);
                        }

                        if (diary.Blind != null && diary.Blind.equals("Y")) {
                            Blind.setVisibility(View.VISIBLE);
                        } else {
                            Blind.setVisibility(View.GONE);
                        }

				/*if(Integer.parseInt(diary.ImageCount) > 1)
				{
					holder.ImgCount.setText("+");
				}
				else
				{
					holder.ImgCount.setVisibility(View.GONE);
				}*/

				/*if(diary.ImageCount.equals("1") || diary.ImageCount.equals("0"))
				{
					holder.ImgCount.setVisibility(View.GONE);
				}
				else
				{
					holder.ImgCount.setText(diary.ImageCount);
				}*/
                    }
                }

			/*Animation animation = null;
			animation = AnimationUtils.loadAnimation(getSherlockActivity(), R.anim.push_up_in);
			convertView.startAnimation(animation);
			animation = null;*/
            }
			/*if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				*//*holder = new DiaryListHolder();
				holder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.Top);
				holder.Profile = (ImageView) convertView.findViewById(R.id.Profile);
				holder.Farmer = (TextView) convertView.findViewById(R.id.Farmer);
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
				//holder.FarmHomeBtn = (ImageButton) convertView.findViewById(R.id.farmHomeBtn);
				holder.Blind = (TextView) convertView.findViewById(R.id.Blind);
				//holder.ImgCount = (TextView) convertView.findViewById(R.id.ImgCount);

				holder.adLayout = (LinearLayout) convertView.findViewById(R.id.item_product_list);

				convertView.setTag(holder);*//*
			}*//* else {
				holder = (DiaryListHolder) convertView.getTag();
			}*//*
*/


            return convertView;
        }
    }

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (bMoreFlag == true) {
                bMoreFlag = false;

                if (mSelectType == DIARY_TYPE_FARM) {
                    getListDiary(makeListDiaryData(false, Integer.valueOf(((DiaryListJson) mainListAdapter.getItem(mainListAdapter.getCount() - 1)).Diary), ((DiaryListJson) mainListAdapter.getItem(mainListAdapter.getCount() - 1)).RegistrationDate2));
                } else if (mSelectType == DIARY_TYPE_REVIEW) {
                    getReviewData(false);
                } else if (mSelectType == DIARY_TYPE_STORY) {
                    getListDaily(false, ((DiaryListJson) mainListAdapter.getItem(mainListAdapter.getCount() - 1)).RegistrationDate2);
                }
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            //getListDiary(makeListDiaryData(true, 0,""));

            mStoryListArray = null;
            bMoreFlag = false;
            adPos = 0;
            mainListAdapter.clear();

            if (mSelectType == DIARY_TYPE_FARM) {
                //getListProduct();
                getListDiary(makeListDiaryData(true, 0, ""));
            } else if (mSelectType == DIARY_TYPE_REVIEW) {
                getReviewData(true);
            } else {
                getListDaily(true, "");
            }
        }
    };

    private void onSubMenuBtnClicked(int subMenuType) {
        CategoryDialogFragment fragment = null;
        switch (subMenuType) {
            case SUBMENU_TYPE_1:
                fragment = CategoryDialogFragment.newInstance(SUBMENU_TYPE_1, subMenuIndex1, subMenuTitle1, subMenuList1.toArray(new String[subMenuList1.size()]), MainActivity.MainTab.DIARY.toString());
                break;
            case SUBMENU_TYPE_2:
                fragment = CategoryDialogFragment.newInstance(SUBMENU_TYPE_2, subMenuIndex2, subMenuTitle2, subMenuList2.toArray(new String[subMenuList2.size()]), MainActivity.MainTab.DIARY.toString());
                break;
            case SUBMENU_TYPE_3:
                fragment = CategoryDialogFragment.newInstance(SUBMENU_TYPE_3, subMenuIndex3, subMenuTitle3, subMenuList3.toArray(new String[subMenuList3.size()]), MainActivity.MainTab.DIARY.toString());
                break;
        }
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(fragment, CategoryDialogFragment.TAG);
        ft.commitAllowingStateLoss();
        //fragment.show(ft, CategoryDialogFragment.TAG);

    }

    public void setButtonText(int subMenuType, int position) {
        if (subMenuList1.size() == 0) {
            switch (mSelectType) {
                case DIARY_TYPE_FARM:
                    Collections.addAll(subMenuList1, getResources().getStringArray(R.array.GetListDiarySubMenu2_1));
                    Collections.addAll(subMenuList2, getResources().getStringArray(R.array.GetListDiarySubMenuAuth));
                    Collections.addAll(subMenuList3, getResources().getStringArray(R.array.GetListDiarySubMenuLocalFood));
                    break;
				/*case DIARY_TYPE_CUSTOMER:
					Collections.addAll(subMenuList1, getResources().getStringArray(R.array.GetListDiarySubMenu4_1));
					Collections.addAll(subMenuList2, getResources().getStringArray(R.array.GetListDiarySubMenu4_2));
					Collections.addAll(subMenuList3, getResources().getStringArray(R.array.GetListDiarySubMenu4_3));
					break;*/
            }
        }

        switch (subMenuType) {
            case SUBMENU_TYPE_1:

                switch (mSelectType) {
                    case DIARY_TYPE_FARM:
                        if (position == 0) {
                            subMenuTitle1 = "카테고리";
                            subMenuText1.setText(subMenuTitle1);
                        } else {
                            subMenuText1.setText(subMenuList1.get(position));
                        }
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Type", subMenuList1.get(position));
                        break;
					/*case DIARY_TYPE_CUSTOMER:
						if(position==0)
						{
							subMenuTitle1 = "소비자";
							subMenuText1.setText(subMenuTitle1);
						}
						else
						{
							subMenuText1.setText(subMenuList1.get(position));
						}
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Experience-Type", subMenuList1.get(position));
						break;*/
                }
                break;
            case SUBMENU_TYPE_2:

                switch (mSelectType) {
                    case DIARY_TYPE_FARM:
                        if (position == 0) {
                            subMenuTitle2 = "친환경인증";
                            subMenuText2.setText(subMenuTitle2);
                        } else {
                            subMenuText2.setText(subMenuList2.get(position));
                        }
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Certification", subMenuList2.get(position));
                        break;
					/*case DIARY_TYPE_CUSTOMER:
						if(position==0)
						{
							subMenuTitle2 = "체험시기";		
							subMenuText2.setText(subMenuTitle2);
						}
						else
						{
							subMenuText2.setText(subMenuList2.get(position));
						}
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Experience-Time", subMenuList2.get(position));
						break;*/
                }
                break;
            case SUBMENU_TYPE_3:

                switch (mSelectType) {
                    case DIARY_TYPE_FARM:
                        if (position == 0) {
                            subMenuTitle3 = "지역/거리";
                            subMenuText3.setText(subMenuTitle3);
                        } else {
                            subMenuText3.setText(subMenuList3.get(position));
                        }
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Distance", subMenuList3.get(position));
                        break;
					/*case DIARY_TYPE_CUSTOMER:
						if(position==0)
						{
							subMenuTitle3 = "지역/거리";		
							subMenuText3.setText(subMenuTitle3);
						}
						else
						{
							subMenuText3.setText(subMenuList3.get(position));
						}
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Experience-Distance", subMenuList3.get(position));
						break;*/
                }
                break;
        }
    }

    @Override
    public void onDialogListSelection(int subMenuType, int position) {
        boolean updateFlag = false;

        switch (subMenuType) {
            case SUBMENU_TYPE_1:
                if (subMenuIndex1 != position) {
                    updateFlag = true;
                    subMenuIndex1 = position;
                    //subMenuText1.setText(subMenuList1.get(position));
                    setButtonText(subMenuType, position);

                    if (position == 7) {
                        subMenuBox2.setVisibility(View.GONE);
                    } else {
                        subMenuBox2.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case SUBMENU_TYPE_2:
                if (subMenuIndex2 != position) {
                    updateFlag = true;
                    subMenuIndex2 = position;
                    //subMenuText2.setText(subMenuList2.get(position));
                    setButtonText(subMenuType, position);
                }
                break;
            case SUBMENU_TYPE_3:
                if (subMenuIndex3 != position) {
                    updateFlag = true;
                    subMenuIndex3 = position;
                    //subMenuText3.setText(subMenuList3.get(position));
                    setButtonText(subMenuType, position);
                }
                break;
        }

        if (updateFlag) {
            //getListProduct();
            getListDiary(makeListDiaryData(true, 0, ""));
        }
    }

    @Override
    public void onDialogListSelection(int position, String object) {
        Log.e(TAG, "========= onDialogListSelection = position = " + position);
        try {

            DiaryListJson data = (DiaryListJson) JsonUtil.jsonToObject(object, DiaryListJson.class);
            if (position == 0) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Share", "카카오톡");
                KaKaoController.sendKakaotalk(this, data);
            } else if (position == 1) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Item-Share", "카카오스토리");
                if (data.Type != null && data.Type.equals("U")) {
                    StoryListJson story = (StoryListJson) JsonUtil.jsonToObject(object, StoryListJson.class);
                    story.Nickname = data.Name;
                    KaKaoController.sendKakaostory(this, story);
                } else {
                    KaKaoController.sendKakaostory(this, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DiaryListData makeListDiaryData(boolean initFlag, int oldIndex, String oldDate) {
        final String allCategory = "1,2,3,4,5,6,7,9";
        //final String farmCategory = "1,2,3,4,5,6"; // 농산물 , 가공 통합   가공 코드추가 "5"
        final String farmCategory = allCategory;
        final int[] farmCategoryArray = {0, 1, 2, 3, 4, 5, 6, 7};
        final int[] farmAuthArray = {0, 2, 3, 4, 5, 6 /* , 7, 8, 9, 10, 11 */};

        final String storyCategory = "8";

        String category;

        double latitude = AppPreferences.getLatitude(getSherlockActivity());
        double longitude = AppPreferences.getLongitude(getSherlockActivity());
        DiaryListData data = new DiaryListData();
        data.setInitFlag(initFlag);
        data.setOldIndex(oldIndex);
        data.setOldDate(oldDate);

        switch (mSelectType) {
            case DIARY_TYPE_FARM:
                if (farmCategoryArray[subMenuIndex1] == 0)
                    category = farmCategory;
                else
                    category = String.valueOf(farmCategoryArray[subMenuIndex1]);

                if (category.equals("7")) {
                    category = "7,9";
                }

                data.setCategory1(category);
                data.setAuth(farmAuthArray[subMenuIndex2]);
                if (subMenuIndex3 != 0 && subMenuList3.get(subMenuIndex3).contains("km")) {
                    if (latitude != 0 && longitude != 0) {
                        data.setDistance(Integer.valueOf(subMenuList3.get(subMenuIndex3).replace("km", "")));
                        data.setLatitude(latitude);
                        data.setLongitude(longitude);
                    }
                } else if (subMenuIndex3 != 0) {
                    data.setAddress(subMenuList3.get(subMenuIndex3));
                }
                break;

            case DIARY_TYPE_REVIEW:
                /*if (villageCategoryArray[subMenuIndex1] == 0)
                    category = villageCategory;
                else
                    category = String.valueOf(villageCategoryArray[subMenuIndex1]);

                data.setCategory1(category);
                // data.setAuth(villageAuthArray[subMenuIndex2]);
                data.setReleaseDate2Month(villageAuthArray[subMenuIndex2]);
                if (subMenuIndex3 != 0 && subMenuList3.get(subMenuIndex3).contains("km")) {
                    if (latitude != 0 && longitude != 0) {
                        data.setDistance(Integer.valueOf(subMenuList3.get(subMenuIndex3).replace("km", "")));
                        data.setLatitude(latitude);
                        data.setLongitude(longitude);
                    }
                } else if (subMenuIndex3 != 0) {
                    data.setAddress(subMenuList3.get(subMenuIndex3));
                }*/
                break;

            case DIARY_TYPE_STORY:
                category = storyCategory;
                data.setCategory1(category);
                break;
        }
        return data;
    }

    public class ImpressiveAdapter extends PagerAdapter {

        private Context context;
        private ArrayList<StoryListJson> items;

        public ImpressiveAdapter(Context context, ArrayList<StoryListJson> items, ImageLoader imageLoader) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void addAll(ArrayList<StoryListJson> items) {
            this.items = items;
        }

        public void add(StoryListJson item) {
            this.items.add(item);
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
            final View v = inflater.inflate(R.layout.view_home_impressive_pager, container, false);

            ImageView image = (ImageView) v.findViewById(R.id.imageView);

            StoryListJson item = items.get(position);

            mImageLoader.displayImage(item.Image, image, mOptionsProduct);

            image.setTag(item.DiaryIndex);
            image.setTag(R.integer.tag_st, item.Nickname);
            image.setTag(R.integer.tag_pos, position);

            image.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {

                    if (5 == (Integer) v.getTag(R.integer.tag_pos)) {
                        Intent intent = new Intent(getSherlockActivity(), StoryListActivity.class);
                        intent.putExtra("keyword", "");
                        intent.putExtra("impressive", "Y");
                        startActivity(intent);
                    } else {
                        String diary = (String) v.getTag();
                        String name = (String) v.getTag(R.integer.tag_st);
                        Intent intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
                        intent.putExtra("type", "story");
                        intent.putExtra("diary", diary);
                        intent.putExtra("name", name);
                        startActivityForResult(intent, Constants.REQUEST_DETAIL_DIARY);
                    }
                }
            });
            container.addView(v, container.getChildCount() > position ? position : container.getChildCount());
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
            CommonUtil.UiUtil.unbindDrawables((RelativeLayout) object);
        }
    }
}
