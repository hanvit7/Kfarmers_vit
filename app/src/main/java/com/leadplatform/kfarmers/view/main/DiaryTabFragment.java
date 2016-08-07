package com.leadplatform.kfarmers.view.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import com.leadplatform.kfarmers.model.json.FarmNewsJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.StoryListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
import com.leadplatform.kfarmers.model.parcel.FarmNewsFooterFilter;
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
import com.leadplatform.kfarmers.view.common.DialogFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.diary.StoryViewActivity;
import com.leadplatform.kfarmers.view.evaluation.ReviewDetailActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;
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

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.leadplatform.kfarmers.view.main.DiaryTabFragment.DiaryTab.FARM_NEWS;

public class DiaryTabFragment extends BaseRefreshMoreListFragment
        implements OnCloseCategoryDialogListener, ShareDialogFragment.OnCloseShareDialogListener {
    public static final String TAG = "DiaryTabFragment";

    public enum DiaryTab {FARM_NEWS, REVIEWS, DAILY_LIFE}

    private DiaryTab mDiaryTab = FARM_NEWS;

    private View mDiaryTabHeader, mTempView;
    private TextView mHeaderFarmNewsTextView, mHeaderReviewsTextView, mHeaderDailyLifeTextView;

    private LinearLayout mDiaryTabFooter;
    private RelativeLayout mFooterEcoCertification;
    private TextView mFooterCategoryTextView, mFooterEcoCertificationTextView, mFooterLocationDistanceTextView;
    private String mFooterCategoryTitle, mFooterEcoCertificationTitle, mFooterLocationDistanceTitle;
    private String[] mFooterCategoryArray, mFooterEcoCertificationArray, mFooterLocationDistanceArray;
    private int mFooterCategoryIndex = 0, mFooterEcoCertificationIndex = 0, mFooterLocationDistanceIndex = 0;

    private enum FarmNewsFooter {
        CATEGORY(0), ECO_CERTIFICATION(1), LOCATION_DISTANCE(2);
        public int value;

        FarmNewsFooter(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    private View mWriteDiaryLayout;
    private View mWriteDiaryView;
    private ImageView mWriteDiaryProfileImageView;
    private TextView mWriteDiaryTextView;
    private String mWriteDiaryText = "";
//    private String mWriteText2 = "";

    private DiaryTabAdapter mDiaryTabAdapter;

    private ImageButton mBackToTopButton;
    private boolean bMoreFlag = false;//?
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptionsProfile, mOptionProfile2, mOptionsProduct;
    private int mReviewsPage = 1;
    private int mTopBarHeight;
    private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;
    private String mLimit = "20";//??

    public static DiaryTabFragment newInstance(int diaryType) {
        Log.d(TAG, "newInstance");
        final DiaryTabFragment f = new DiaryTabFragment();

        final Bundle args = new Bundle();
        args.putInt("Type", diaryType);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
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
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_diary_tab, container, false);

        if (getArguments() != null) {
            DiaryTab temp = (DiaryTab) getArguments().getSerializable("Type");
            if (temp != null) {
                mDiaryTab = temp;
            }
            getArguments().remove("Type");
        } else {
            mDiaryTab = DiaryTab.FARM_NEWS;
        }
        bMoreFlag = false;

        //Diary Writing View Init
        mWriteDiaryLayout = inflater.inflate(R.layout.view_writing_diary, null);
        mWriteDiaryProfileImageView = (ImageView) mWriteDiaryLayout.findViewById(R.id.diary_writing_profile_image_view);
        mWriteDiaryTextView = (TextView) mWriteDiaryLayout.findViewById(R.id.diary_writing_text_view);
        mWriteDiaryView = mWriteDiaryLayout.findViewById(R.id.diary_writing_layout);
        mWriteDiaryView.setVisibility(View.GONE);
        mWriteDiaryView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Log.d(TAG, "mWriteDiaryView viewOnClick");
                ProfileJson profileJson = getUserProfile();
                if (profileJson == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                ((BaseFragmentActivity) getActivity()).runWriteDiaryActivity(profileJson.Type);
//                switch (mDiaryTab) {
//                    case FARM_NEWS:
//                        ((BaseFragmentActivity) getActivity()).runWriteDiaryActivity(profileJson.Type);
//                        break;
//                    case DAILY_LIFE:
//                        ((BaseFragmentActivity) getActivity()).runWriteDiaryActivity(profileJson.Type);
//                        break;
//                }
            }
        });
        mBackToTopButton = (ImageButton) view.findViewById(R.id.back_to_top_button);
        mBackToTopButton.setVisibility(View.GONE);
        mBackToTopButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getListView().smoothScrollBy(0, 0);
                getListView().setSelectionFromTop(0, 0);
            }
        });

        //Footer init
        mDiaryTabFooter = (LinearLayout) view.findViewById(R.id.diary_tab_footer);

        mFooterCategoryTextView = (TextView) view.findViewById(R.id.diary_tab_footer_category_text_view);
//        RelativeLayout subMenuBox1 = (RelativeLayout) v.findViewById(R.id.diary_tab_footer_category);
//        subMenuBox1.setOnClickListener(new ViewOnClickListener() {
//            @Override
//            public void viewOnClick(View v) {
//                setFarmNewsFooterDialog(FarmNewsFooter.CATEGORY);
//            }
//        });
        view.findViewById(R.id.diary_tab_footer_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFarmNewsFooterDialog(FarmNewsFooter.CATEGORY);
            }
        });

        mFooterEcoCertificationTextView = (TextView) view.findViewById(R.id.diary_tab_footer_eco_certification_text_view);
        mFooterEcoCertification = (RelativeLayout) view.findViewById(R.id.diary_tab_footer_eco_certification);
//        mFooterEcoCertification.setOnClickListener(new ViewOnClickListener() {
//            @Override
//            public void viewOnClick(View v) {
//                setFarmNewsFooterDialog(FarmNewsFooter.ECO_CERTIFICATION);
//            }
//        });
        mFooterEcoCertification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFarmNewsFooterDialog(FarmNewsFooter.ECO_CERTIFICATION);
            }
        });

        mFooterLocationDistanceTextView = (TextView) view.findViewById(R.id.diary_tab_footer_location_distance_text_view);
//        RelativeLayout subMenuBox3 = (RelativeLayout) view.findViewById(R.id.diary_tab_footer_location_distance);
//        subMenuBox3.setOnClickListener(new ViewOnClickListener() {
//            @Override
//            public void viewOnClick(View v) {
//                setFarmNewsFooterDialog(FarmNewsFooter.LOCATION_DISTANCE);
//            }
//        });
        view.findViewById(R.id.diary_tab_footer_location_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFarmNewsFooterDialog(FarmNewsFooter.LOCATION_DISTANCE);
            }
        });

        //Header Init
        mDiaryTabHeader = view.findViewById(R.id.diary_tab_header);

        mHeaderFarmNewsTextView = (TextView) mDiaryTabHeader.findViewById(R.id.diary_tab_header_farm_news_text_view);
        RelativeLayout headerFarmNews = (RelativeLayout) mDiaryTabHeader.findViewById(R.id.diary_tab_header_farm_news);
        headerFarmNews.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Log.d(TAG, "viewOnClick");
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "농장소식");
                mDiaryTab = DiaryTab.FARM_NEWS;
                mWriteDiaryText = "";
//                mWriteText2 = "";
                bMoreFlag = false;
                mDiaryTabAdapter.clear();
                mReviewsPage = 1;
                onHeaderClicked(true);
                getFarmNewsData(setFarmNewsFooterFilter(true, 0, ""));
                mDiaryTabFooter.setVisibility(View.VISIBLE);
            }
        });

        mHeaderReviewsTextView = (TextView) mDiaryTabHeader.findViewById(R.id.diary_tab_header_reviews_text_view);
        RelativeLayout headerReviews = (RelativeLayout) mDiaryTabHeader.findViewById(R.id.diary_tab_header_reviews);
        headerReviews.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "구매후기");
                mDiaryTab = DiaryTab.REVIEWS;
                mWriteDiaryText = "";
//                mWriteText2 = "";
                bMoreFlag = false;
                mDiaryTabAdapter.clear();
                mReviewsPage = 1;
                onHeaderClicked(true);
                getReviewsData(true);
                mDiaryTabFooter.setVisibility(View.GONE);
            }
        });

        mHeaderDailyLifeTextView = (TextView) mDiaryTabHeader.findViewById(R.id.diary_tab_header_daily_life_text_view);
        RelativeLayout headerDailyLife = (RelativeLayout) mDiaryTabHeader.findViewById(R.id.diary_tab_header_daily_life);
        headerDailyLife.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Type", "일상");
                mDiaryTab = DiaryTab.DAILY_LIFE;
                mWriteDiaryText = "";
//                mWriteText2 = "";
                bMoreFlag = false;
                mDiaryTabAdapter.clear();
                mReviewsPage = 1;
                onHeaderClicked(true);
                getDailyLifeData(true, "");
                mDiaryTabFooter.setVisibility(View.GONE);
            }
        });

        mTopBarHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonMediumByLargeRow2);
        mTempView = new View(getActivity());
        mTempView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mTopBarHeight));
        setRefreshListView(getSherlockActivity(), view, R.id.refresh_layout, refreshListener);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        bMoreFlag = false;

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        if (getListAdapter() == null) {
            getListView().addHeaderView(mTempView);//header 간격만큼 리스트뷰 아래로 내림...
            getListView().addHeaderView(mWriteDiaryLayout);
        } else {
            if (getListView().getHeaderViewsCount() == 0) {
                setListAdapter(null);
                getListView().addHeaderView(mTempView);
                getListView().addHeaderView(mWriteDiaryLayout);
            }
        }

        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        mDiaryTabAdapter = new DiaryTabAdapter(getSherlockActivity(),
                R.layout.item_diary,
                new ArrayList(),
                AppPreferences.getLatitude(getSherlockActivity()),
                AppPreferences.getLongitude(getSherlockActivity()));

        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mDiaryTabAdapter);
        mSwingBottomInAnimationAdapter.setAbsListView(getListView());

        assert mSwingBottomInAnimationAdapter.getViewAnimator() != null;
        mSwingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);

        setListAdapter(mSwingBottomInAnimationAdapter);

        QuickReturnListViewOnScrollListener scrollListener;
        scrollListener = new QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.TWITTER)
                .header(mDiaryTabHeader)
                .minHeaderTranslation(-mTopBarHeight)
                .footer(mDiaryTabFooter)
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
                    mBackToTopButton.setVisibility(View.VISIBLE);
                } else {
                    mBackToTopButton.setVisibility(View.GONE);
                }
            }
        };
//        scrollListener.registerExtraOnScrollListener(onScrollListener);
        getListView().setOnScrollListener(scrollListener);

//        onFarmNewsFooterClicked(FarmNewsFooter.CATEGORY, mFooterCategoryIndex);
//        onFarmNewsFooterClicked(FarmNewsFooter.ECO_CERTIFICATION, mFooterEcoCertificationIndex);
//        onFarmNewsFooterClicked(FarmNewsFooter.LOCATION_DISTANCE, mFooterLocationDistanceIndex);

        getFarmNewsData(setFarmNewsFooterFilter(true, 0, ""));
    }

    private ProfileJson getUserProfile() {
        Log.d(TAG, "getUserProfile");
        if (!AppPreferences.getLogin(getActivity())) {
            return null;
        }
        try {
            String profile = DbController.queryProfileContent(getSherlockActivity());
            JsonNode root = JsonUtil.parseTree(profile);
            return (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(),
                    ProfileJson.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        onHeaderClicked(false);
        setWriteDiaryView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_DETAIL_DIARY) {
                if (data.getBooleanExtra("delete", false)) {
                    String diary = data.getStringExtra("diary");
                    for (int index = 0; index < mDiaryTabAdapter.getCount(); index++) {
                        if (mDiaryTabAdapter.getItem(index) instanceof FarmNewsJson) {
                            FarmNewsJson item = (FarmNewsJson) mDiaryTabAdapter.getItem(index);
                            if (item.Diary.equals(diary)) {
                                mDiaryTabAdapter.remove(item);
                                mDiaryTabAdapter.notifyDataSetChanged();
                                break;
                            }
                        } else if (mDiaryTabAdapter.getItem(index) instanceof StoryListJson) {
                            StoryListJson item = (StoryListJson) mDiaryTabAdapter.getItem(index);
                            if (item.DiaryIndex.equals(diary)) {
                                mDiaryTabAdapter.remove(item);
                                mDiaryTabAdapter.notifyDataSetChanged();
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

    private void setWriteDiaryView() {
        Log.d(TAG, "setWriteDiaryView");
        if (mWriteDiaryText.isEmpty()) {
            mWriteDiaryView.setVisibility(View.GONE);
            return;
        }

        ProfileJson profileJson = getUserProfile();
        switch (mDiaryTab) {
            case FARM_NEWS:
                if (profileJson != null
                        && (profileJson.Type.equals("F") || profileJson.Type.equals("V"))) {
                    if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
                        mImageLoader.displayImage(profileJson.ProfileImage, mWriteDiaryProfileImageView, mOptionsProfile);
                    }

                    if (profileJson.PermissionFlag.equals("Y")) {
                        if (profileJson.TemporaryPermissionFlag.equals("Y")) {
                            mWriteDiaryTextView.setHint(mWriteDiaryText);
                        } else if (profileJson.TemporaryPermissionFlag.equals("N")) {
                            mWriteDiaryTextView.setHint("영농일기를 올려주세요");
                        }
                        mWriteDiaryView.setVisibility(View.VISIBLE);
                    } else {
                        mWriteDiaryView.setVisibility(View.GONE);
                        mWriteDiaryProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                    }
                } else {
                    mWriteDiaryView.setVisibility(View.GONE);
                    mWriteDiaryProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                }
                break;

            case DAILY_LIFE:
                if (profileJson != null
                        && (profileJson.Type.equals("F") || profileJson.Type.equals("V"))) {
                    mWriteDiaryView.setVisibility(View.VISIBLE);
                    mWriteDiaryTextView.setHint(mWriteDiaryText);

                    if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
                        mImageLoader.displayImage(
                                profileJson.ProfileImage,
                                mWriteDiaryProfileImageView,
                                mOptionsProfile);
                    } else {
                        mWriteDiaryProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                    }
                } else {
                    mWriteDiaryView.setVisibility(View.GONE);
                    mWriteDiaryProfileImageView.setImageResource(R.drawable.icon_empty_profile);
                }
                break;
        }
    }

    private void onHeaderClicked(boolean isUser) {
        Log.d(TAG, "onHeaderClicked");
        if (isUser)
            CenterController.cancel(getActivity(), true);//하 던 작 업 중 단??

        mFooterEcoCertification.setVisibility(View.VISIBLE);
        mFooterCategoryArray = null;
        mFooterEcoCertificationArray = null;
        mFooterLocationDistanceArray = null;
        mFooterCategoryIndex = 0;
        mFooterEcoCertificationIndex = 0;
        mFooterLocationDistanceIndex = 0;

        mWriteDiaryView.setVisibility(View.GONE);

        switch (mDiaryTab) {
            case FARM_NEWS: {
                onFarmNewsFooterClicked(FarmNewsFooter.CATEGORY, mFooterCategoryIndex);
                onFarmNewsFooterClicked(FarmNewsFooter.ECO_CERTIFICATION, mFooterEcoCertificationIndex);
                onFarmNewsFooterClicked(FarmNewsFooter.LOCATION_DISTANCE, mFooterLocationDistanceIndex);

                mHeaderFarmNewsTextView.setTypeface(null, Typeface.BOLD);
                mHeaderReviewsTextView.setTypeface(null, Typeface.NORMAL);
                mHeaderDailyLifeTextView.setTypeface(null, Typeface.NORMAL);
                break;
            }
            case REVIEWS: {
                mHeaderFarmNewsTextView.setTypeface(null, Typeface.NORMAL);
                mHeaderReviewsTextView.setTypeface(null, Typeface.BOLD);
                mHeaderDailyLifeTextView.setTypeface(null, Typeface.NORMAL);
                mDiaryTabFooter.setVisibility(View.GONE);
                break;
            }
            case DAILY_LIFE: {
                mHeaderFarmNewsTextView.setTypeface(null, Typeface.NORMAL);
                mHeaderReviewsTextView.setTypeface(null, Typeface.NORMAL);
                mHeaderDailyLifeTextView.setTypeface(null, Typeface.BOLD);
                mDiaryTabFooter.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void getFarmNewsData(FarmNewsFooterFilter data) {
        Log.d(TAG, "getFarmNewsData");
        if (data == null)
            return;

        if (data.isInitFlag()) {
            bMoreFlag = false;
            mDiaryTabAdapter.clear();
        }

        CenterController.getFarmNewsList(getActivity(), data, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "getFarmNewsData onSuccess");
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 0) {
                        JsonNode root = JsonUtil.parseTree(content);

                        if (root.has("Text")
                                && !root.get("Text").toString().trim().isEmpty()
                                && !root.get("Text").asText().equals("null")) {
                            mWriteDiaryText = root.get("Text").asText();
                        } else {
                            mWriteDiaryText = "영농일기를 올려주세요";
                        }

//                        if (root.has("Text2")
//                                && !root.get("Text2").asText().trim().isEmpty()
//                                && !root.get("Text2").asText().equals("null")) {
//                            mWriteText2 = root.get("Text2").asText();
//                        } else {
//                            mWriteText2 = "영농일기를 올려주세요";
//                        }
                        setWriteDiaryView();

                        if (root.findPath("List").isArray()) {
                            int diaryCount = 0;
                            for (JsonNode jsonNode : root.findPath("List")) {
                                diaryCount++;
                                FarmNewsJson diary = (FarmNewsJson) JsonUtil.jsonToObject(jsonNode.toString(),
                                        FarmNewsJson.class);


                                mDiaryTabAdapter.add(diary);
                            }
                            bMoreFlag = diaryCount == 20;
                        }
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                Log.d(TAG, "onFailure");
                super.onFailure(statusCode, headers, content, error);
                onRefreshComplete();
                onLoadMoreComplete();
            }
        });
    }

    private void getReviewsData(boolean isClear) {
        Log.d(TAG, "getReviewsData");
        if (isClear) {
            bMoreFlag = false;
            mReviewsPage = 1;
            mDiaryTabAdapter.clear();
        }

        SnipeApiController.getReviewList("", "", "", String.valueOf(mReviewsPage), mLimit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                Log.d(TAG, "SnipeResponseListener");
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                ArrayList<ReviewListJson> evaluationList = (ArrayList<ReviewListJson>) JsonUtil.jsonToArrayObject(root.path("item"), ReviewListJson.class);
                                mDiaryTabAdapter.addAll(evaluationList);

                                if (evaluationList.size() == Integer.parseInt(mLimit)) {
                                    mReviewsPage++;
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
                Log.d(TAG, "onFailure");
                super.onFailure(statusCode, headers, content, error);
                onRefreshComplete();
                onLoadMoreComplete();
            }
        });
    }

    private void getDailyLifeData(boolean initFlag, String oldDate) {
        Log.d(TAG, "getDailyLifeData");
        if (initFlag) {
            bMoreFlag = false;
            mDiaryTabAdapter.clear();
        }

        CenterController.getDailyLifeList(oldDate, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "getDailyLifeData onSuccess");
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 0) {
                        JsonNode root = JsonUtil.parseTree(content);

                        if (root.has("Text")
                                && !root.get("Text").asText().trim().isEmpty()
                                && !root.get("Text").asText().equals("null")) {
                            mWriteDiaryText = root.get("Text").asText();

                        } else {
                            mWriteDiaryText = "일상 이야기를 올려주세요";
                        }
                        setWriteDiaryView();

                        if (root.findPath("List").isArray()) {
                            int diaryCount = 0;
                            for (JsonNode jsonNode : root.findPath("List")) {
                                diaryCount++;
                                FarmNewsJson diary = (FarmNewsJson) JsonUtil.jsonToObject(jsonNode.toString(),
                                        FarmNewsJson.class);

                                if (diary.Image != null) diary.ProductImage1 = diary.Image;
                                if (diary.Name != null) {
                                    diary.FarmerName = diary.Name;
                                    diary.Farm = diary.FarmerName;
                                }

                                if (diary.MemberIndex != null)
                                    diary.FarmerIndex = diary.MemberIndex;
                                if (diary.CategoryName == null || diary.CategoryName.isEmpty())
                                    diary.CategoryName = "일상";

                                mDiaryTabAdapter.add(diary);
                            }

                            bMoreFlag = diaryCount == 20;
                        }
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                Log.d(TAG, "onFailure");
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    private class DiaryTabAdapter extends ArrayAdapter {

        private int itemLayoutResourceId;
        private double userLatitude, userLongitude;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d";
        SimpleDateFormat format, format2;

        public DiaryTabAdapter(Context context, int itemLayoutResourceId, ArrayList items,
                               double userLatitude, double userLongitude) {
            super(context, itemLayoutResourceId, items);
            Log.d(TAG, "DiaryTabAdapter");
            this.itemLayoutResourceId = itemLayoutResourceId;

            this.userLatitude = userLatitude;
            this.userLongitude = userLongitude;

            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView");
            if (getItem(position) != null) {
                if (getItem(position) instanceof ReviewListJson) {
                    if (convertView == null || !convertView.getTag(R.integer.tag_id).equals("review")) {
                        LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.item_review_list, null);
                        convertView.setTag(R.integer.tag_id, "review");
                    }
                } else if (getItem(position) instanceof FarmNewsJson || getItem(position) instanceof ProductJson) {
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

                    if (item.member_name == null || item.member_name.isEmpty()) {
                        name.setText("소비자 회원");
                    } else {
                        name.setText(item.member_name);
                    }

                    ratingText.setText(item.rating + "점");
                    description.setText(item.comment);

                    try {
                        date.setText(format2.format((format.parse(item.datetime).getTime())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

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
                            ((MainActivity) getActivity()).runReplyActivity(
                                    ReplyActivity.REPLY_TYPE_REVIEW,
                                    "구매후기 댓글",
                                    idx);
                        }
                    });
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
                    final FarmNewsJson diary = (FarmNewsJson) getItem(position);

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
                                                int count = Integer.parseInt((((FarmNewsJson) getItem(tag.position)).Like));
                                                if (plus) {
                                                    ((FarmNewsJson) getItem(tag.position)).Like = String.valueOf(count + 1);
                                                } else {
                                                    if (count != 0)
                                                        ((FarmNewsJson) getItem(tag.position)).Like = String.valueOf(count - 1);
                                                }
                                                mDiaryTabAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                } else {
                                    ((BaseFragmentActivity) getSherlockActivity()).centerLikeUserDiary(tag.index, new OnLikeDiaryListener() {
                                        @Override
                                        public void onResult(int code, boolean plus) {
                                            if (code == 0) {
                                                int count = Integer.parseInt((((FarmNewsJson) getItem(tag.position)).Like));
                                                if (plus) {
                                                    ((FarmNewsJson) getItem(tag.position)).Like = String.valueOf(count + 1);
                                                } else {
                                                    if (count != 0)
                                                        ((FarmNewsJson) getItem(tag.position)).Like = String.valueOf(count - 1);
                                                }
                                                mDiaryTabAdapter.notifyDataSetChanged();
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
                                        ((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(
                                                ReplyActivity.REPLY_TYPE_FARMER,
                                                diary.Farm,
                                                diary.Diary);
                                    } else if (diary.Type.equals("V")) {
                                        ((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(
                                                ReplyActivity.REPLY_TYPE_VILLAGE,
                                                diary.Farm,
                                                diary.Diary);
                                    }
                                } else {
                                    ((BaseFragmentActivity) getSherlockActivity()).runReplyActivity(
                                            ReplyActivity.REPLY_TYPE_NORMAL,
                                            diary.Name,
                                            diary.Diary);
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
                    }
                }
            }
            return convertView;
        }
    }

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {//불 러 온 리 스 트 모 두 사 용
            Log.d(TAG, "onLoadMore");
            if (bMoreFlag) {
                bMoreFlag = false;
                switch (mDiaryTab) {
                    case FARM_NEWS:
                        getFarmNewsData(setFarmNewsFooterFilter(false,
                                Integer.valueOf(((FarmNewsJson) mDiaryTabAdapter
                                        .getItem(mDiaryTabAdapter.getCount() - 1)).Diary),
                                ((FarmNewsJson) mDiaryTabAdapter
                                        .getItem(mDiaryTabAdapter.getCount() - 1)).RegistrationDate2));
                        break;
                    case REVIEWS:
                        getReviewsData(false);
                        break;
                    case DAILY_LIFE:
                        getDailyLifeData(false, ((FarmNewsJson) mDiaryTabAdapter
                                .getItem(mDiaryTabAdapter.getCount() - 1)).RegistrationDate2);
                        break;
                }
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            Log.d(TAG, "onRefreshStarted");
            bMoreFlag = false;
            mDiaryTabAdapter.clear();
            switch (mDiaryTab) {
                case FARM_NEWS:
                    getFarmNewsData(setFarmNewsFooterFilter(true, 0, ""));
                    break;
                case REVIEWS:
                    getReviewsData(true);
                    break;
                case DAILY_LIFE:
                    getDailyLifeData(false, ((FarmNewsJson) mDiaryTabAdapter
                            .getItem(mDiaryTabAdapter.getCount() - 1)).RegistrationDate2);
                    break;
            }
        }
    };

    private void setFarmNewsFooterDialog(FarmNewsFooter farmNewsFooter) {
        Log.d(TAG, "setFarmNewsFooterDialog");
        DialogFragment fragment = null;
        switch (farmNewsFooter) {
            case CATEGORY:
                fragment = DialogFragment.newInstance(
                        farmNewsFooter.value,
                        mFooterCategoryIndex,
                        mFooterCategoryTitle,
                        mFooterCategoryArray,
                        MainActivity.MainTab.DIARY.toString());
                break;
            case ECO_CERTIFICATION:
                fragment = DialogFragment.newInstance(
                        farmNewsFooter.value,
                        mFooterEcoCertificationIndex,
                        mFooterEcoCertificationTitle,
                        mFooterEcoCertificationArray,
                        MainActivity.MainTab.DIARY.toString());
                break;
            case LOCATION_DISTANCE:
                fragment = DialogFragment.newInstance(
                        farmNewsFooter.value,
                        mFooterLocationDistanceIndex,
                        mFooterLocationDistanceTitle,
                        mFooterLocationDistanceArray,
                        MainActivity.MainTab.DIARY.toString());
                break;
        }
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(fragment, DialogFragment.TAG);
        ft.commitAllowingStateLoss();
    }

    private void onFarmNewsFooterClicked(FarmNewsFooter farmNewsFooter, int position) {
        Log.d(TAG, "onFarmNewsFooterClicked");
        if (mFooterCategoryArray == null) {
            switch (mDiaryTab) {
                case FARM_NEWS:
                    mFooterCategoryArray = getResources().getStringArray(R.array.diary_tab_footer_category_array);
                    mFooterEcoCertificationArray = getResources().getStringArray(R.array.diary_tab_footer_eco_certification_array);
                    mFooterLocationDistanceArray = getResources().getStringArray(R.array.diary_tab_footer_location_distance_array);
                    break;
            }
        }

        switch (farmNewsFooter) {
            case CATEGORY:
                switch (mDiaryTab) {
                    case FARM_NEWS:
                        if (position == 0) {
                            mFooterCategoryTitle = "카테고리";
                            mFooterCategoryTextView.setText(mFooterCategoryTitle);
                        } else {
                            mFooterCategoryTextView.setText(mFooterCategoryArray[position]);
                        }
                        //KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Type", mFooterCategoryList.get(position));
                        break;
                }
                break;
            case ECO_CERTIFICATION:
                switch (mDiaryTab) {
                    case FARM_NEWS:
                        if (position == 0) {
                            mFooterEcoCertificationTitle = "친환경인증";
                            mFooterEcoCertificationTextView.setText(mFooterEcoCertificationTitle);
                        } else {
                            mFooterEcoCertificationTextView.setText(mFooterEcoCertificationArray[position]);
                        }
                        //KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Certification", mFooterEcoCertificationList.get(position));
                        break;
                }
                break;
            case LOCATION_DISTANCE:
                switch (mDiaryTab) {
                    case FARM_NEWS:
                        if (position == 0) {
                            mFooterLocationDistanceTitle = "지역/거리";
                            mFooterLocationDistanceTextView.setText(mFooterLocationDistanceTitle);
                        } else {
                            mFooterLocationDistanceTextView.setText(mFooterLocationDistanceArray[position]);
                        }
                        //KfarmersAnalytics.onClick(KfarmersAnalytics.S_STROY_LIST, "Click_Product-Distance", mFooterLocationDistanceList.get(position));
                        break;
                }
                break;
        }
    }

    @Override
    public void onDialogSelected(int subMenuType, int position) {
        Log.d(TAG, "onDialogSelected");
        boolean updateFlag = false;

        FarmNewsFooter farmNewsFooter;
        switch (subMenuType) {
            case 0:
                farmNewsFooter = FarmNewsFooter.CATEGORY;
                break;
            case 1:
                farmNewsFooter = FarmNewsFooter.ECO_CERTIFICATION;
                break;
            case 2:
                farmNewsFooter = FarmNewsFooter.LOCATION_DISTANCE;
                break;
            default:
                return;
        }

        switch (farmNewsFooter) {
            case CATEGORY:
                if (mFooterCategoryIndex != position) {
                    updateFlag = true;
                    mFooterCategoryIndex = position;
                    onFarmNewsFooterClicked(farmNewsFooter, position);

                    if (position == 7) {
                        mFooterEcoCertification.setVisibility(View.GONE);
                    } else {
                        mFooterEcoCertification.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case ECO_CERTIFICATION:
                if (mFooterEcoCertificationIndex != position) {
                    updateFlag = true;
                    mFooterEcoCertificationIndex = position;
                    onFarmNewsFooterClicked(farmNewsFooter, position);
                }
                break;
            case LOCATION_DISTANCE:
                if (mFooterLocationDistanceIndex != position) {
                    updateFlag = true;
                    mFooterLocationDistanceIndex = position;
                    onFarmNewsFooterClicked(farmNewsFooter, position);
                }
                break;
        }

        if (updateFlag) {
            getFarmNewsData(setFarmNewsFooterFilter(true, 0, ""));
        }
    }

    private FarmNewsFooterFilter setFarmNewsFooterFilter(boolean initFlag, int oldIndex, String oldDate) {//list diary
        Log.d(TAG, "setFarmNewsFooterFilter");
        final String farmCategory = "1,2,3,4,5,6,7,9";
        final int[] farmCategoryArray = {0, 1, 2, 3, 4, 5, 6, 7};
        final int[] farmAuthArray = {0, 2, 3, 4, 5, 6};

        final String storyCategory = "8";

        String category;

        double latitude = AppPreferences.getLatitude(getSherlockActivity());
        double longitude = AppPreferences.getLongitude(getSherlockActivity());
        FarmNewsFooterFilter filter = new FarmNewsFooterFilter();
        filter.setInitFlag(initFlag);
        filter.setOldIndex(oldIndex);
        filter.setOldDate(oldDate);

        switch (mDiaryTab) {
            case FARM_NEWS:
                if (farmCategoryArray[mFooterCategoryIndex] == 0)
                    category = farmCategory;
                else
                    category = String.valueOf(farmCategoryArray[mFooterCategoryIndex]);

                if (category.equals("7")) {
                    category = "7,9";
                }

                filter.setCategory1(category);
                filter.setAuth(farmAuthArray[mFooterEcoCertificationIndex]);
                if (mFooterLocationDistanceIndex != 0) {
                    if (mFooterLocationDistanceArray[mFooterLocationDistanceIndex].contains("km")) {
                        if (latitude != 0 && longitude != 0) {
                            filter.setDistance(Integer.valueOf(mFooterLocationDistanceArray[mFooterLocationDistanceIndex].replace("km", "")));
                            filter.setLatitude(latitude);
                            filter.setLongitude(longitude);
                        }
                    } else {
                        filter.setAddress(mFooterLocationDistanceArray[mFooterLocationDistanceIndex]);
                    }
                }
                break;

            case REVIEWS:
                break;

            case DAILY_LIFE:
                category = storyCategory;
                filter.setCategory1(category);
                break;
        }
        return filter;
    }

    @Override
    public void onDialogListSelection(int position, String object) {
        Log.e(TAG, "========= onDialogListSelection = position = " + position);
        try {
            FarmNewsJson data = (FarmNewsJson) JsonUtil.jsonToObject(object, FarmNewsJson.class);
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
}
