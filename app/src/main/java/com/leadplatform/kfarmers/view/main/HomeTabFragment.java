package com.leadplatform.kfarmers.view.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnViewType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnScrollViewOnScrollChangedListener;
import com.etiennelawlor.quickreturn.library.views.NotifyingScrollView;
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
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.json.NoticeListJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RecommendListJson;
import com.leadplatform.kfarmers.model.json.snipe.EventJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJson;
import com.leadplatform.kfarmers.model.parcel.NoticeListData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.LicenseeInfoActivity;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.evaluation.EvaluationActivity;
import com.leadplatform.kfarmers.view.Supporters.SupportersDetailActivity;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.join.JoinTermsActivity;
import com.leadplatform.kfarmers.view.menu.invite.InviteActivity;
import com.leadplatform.kfarmers.view.menu.notice.FarmNoticeActivity;
import com.leadplatform.kfarmers.view.menu.story.FarmStoryActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.leadplatform.kfarmers.view.market.register.ProductRegisterActivity;
import com.leadplatform.kfarmers.view.recipe.RecipeViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;

public class HomeTabFragment extends BaseFragment {
    public static final String TAG = "HomeTabFragment";

    ImageLoader mImageLoader;
    DisplayImageOptions mOptionsProduct;
    DisplayImageOptions mOptionsProfile;
    LayoutInflater mInflate;

    RelativeLayout mImpressiveLayout;
    LinearLayout mProductLayout;
    LinearLayout mNoticeAndInvateLayout;
    LinearLayout mInvateLayout;
    LinearLayout mNoticLayout;
    LinearLayout mEventLayout;
    LinearLayout mRecipeLayout;
    LinearLayout mEvaluationLayout;

    NotifyingScrollView mScrollview;

    ViewPager mImpressiveViewpager;
    LinearLayout mImpressivePagingLayout;
    ImpressiveAdapter mImpressiveAdapter;
    TextView mImpressiveText;
    RelativeLayout mImpressiveTextView, mImpressiveMoreView;

    LinearLayout mProductListLayout;
    ArrayList<ProductJson> mProductList;

    LinearLayout mEvaluationListLayout;

    LinearLayout mFarmerListLayout;
    ArrayList<RecommendListJson> mFarmerList;

    LinearLayout mEventListLayout;
    ArrayList<EventJson> mEventList;

    TextView mNoticTitle, mNoticText;
    ArrayList<NoticeListJson> mNoticeList;

    TextView mTerms1, mTerms2, mTerms3, mTerms4;

    RelativeLayout mBottomLayout;
    int bottomHeight = 0;

    private ArrayList<RecipeJson> mRecipeDataList;

    private QuickReturnScrollViewOnScrollChangedListener mQuicScrollListener;

    public static HomeTabFragment newInstance(int diaryType) {
        final HomeTabFragment f = new HomeTabFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_HOME);

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.common_dummy)
                .displayer(new FadeInBitmapDisplayer(1000, true, true, true))
                .build();
        mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_home, container, false);

        mInflate = inflater;

        mScrollview = (NotifyingScrollView) v.findViewById(R.id.scrollview);
        mBottomLayout = (RelativeLayout) v.findViewById(R.id.bottomLayout);

        mNoticeAndInvateLayout = (LinearLayout) v.findViewById(R.id.NoticeAndInvateLayout);

        bottomHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonLargeRow);
        mBottomLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getActivity(), ProductRegisterActivity.class);
                startActivity(intent);
            }
        });

        invateInit(v);
        impressiveInit(v);
        productListInit(v);
        eventListInit(v);
        evaluationInit(v);
        recipeListInit(v);
        noticListInit(v);
        temrsInit(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getImpressiveList();
        getListNotic();
        getListProduct();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppPreferences.getLogin(getActivity())) {
            try {
                String mProfile = DbController.queryProfileContent(getActivity());
                JsonNode root = JsonUtil.parseTree(mProfile);
                ProfileJson profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);

                if (profileJson.Type.equals("F") && profileJson.PermissionFlag.equals("Y") && profileJson.TemporaryPermissionFlag.equals("Y")) {
                    mBottomLayout.setVisibility(View.VISIBLE);

                    mQuicScrollListener = new QuickReturnScrollViewOnScrollChangedListener.Builder(QuickReturnViewType.FOOTER)
                            .footer(mBottomLayout)
                            .minFooterTranslation(bottomHeight)
                            .build();
                    mScrollview.setOnScrollChangedListener(mQuicScrollListener);
                    mScrollview.setOverScrollEnabled(false);
                } else {
                    mScrollview.setOnScrollChangedListener(null);
                    mBottomLayout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mScrollview.setOnScrollChangedListener(null);
            mBottomLayout.setVisibility(View.GONE);
        }
    }

    public void temrsInit(View v) {
        ViewOnClickListener viewOnClickListener = new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                switch (v.getId()) {
                    case R.id.Terms1:
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Terms", "이용약관");
                        Intent intent = new Intent(getActivity(), JoinTermsActivity.class);
                        intent.putExtra("page", "1");
                        getActivity().startActivity(intent);
                        break;
                    case R.id.Terms2:
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Terms", "개인정보");
                        Intent intent2 = new Intent(getActivity(), JoinTermsActivity.class);
                        intent2.putExtra("page", "2");
                        getActivity().startActivity(intent2);
                        break;
                    case R.id.Terms3:
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Terms", "위치기반서비스");
                        Intent intent3 = new Intent(getActivity(), JoinTermsActivity.class);
                        intent3.putExtra("page", "4");
                        getActivity().startActivity(intent3);
                        break;
                    case R.id.Terms4:
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Terms", "구매하기");
                        Intent intent4 = new Intent(getActivity(), LicenseeInfoActivity.class);
                        getActivity().startActivity(intent4);
                        break;
                }
            }
        };
        mTerms1 = (TextView) v.findViewById(R.id.Terms1);
        mTerms2 = (TextView) v.findViewById(R.id.Terms2);
        mTerms3 = (TextView) v.findViewById(R.id.Terms3);
        mTerms4 = (TextView) v.findViewById(R.id.Terms4);

        mTerms1.setOnClickListener(viewOnClickListener);
        mTerms2.setOnClickListener(viewOnClickListener);
        mTerms3.setOnClickListener(viewOnClickListener);
        mTerms4.setOnClickListener(viewOnClickListener);
    }

    public void noticListInit(View v) {
        mNoticLayout = (LinearLayout) v.findViewById(R.id.NoticLayout);

        mNoticLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getSherlockActivity(), FarmNoticeActivity.class);
                intent.putExtra("userType", "G");
                intent.putExtra("userIndex", "");

                if (mNoticeList != null && mNoticeList.size() > 0) {
                    intent.putExtra("noticeIndex", mNoticeList.get(0).Index + "");
                } else {
                    intent.putExtra("noticeIndex", "");
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Notice", null);
                startActivity(intent);
            }
        });

        mNoticTitle = (TextView) v.findViewById(R.id.NoticTitle);
        mNoticText = (TextView) v.findViewById(R.id.NoticText);
    }

    private void getListNotic() {
        NoticeListData data = new NoticeListData();

        CenterController.getListAppNotice(data, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode jsonNode = JsonUtil.parseTree(content);
                            mNoticeList = (ArrayList<NoticeListJson>) JsonUtil.jsonToArrayObject(jsonNode.findValue("List"), NoticeListJson.class);

                            if (mNoticeList != null && mNoticeList.size() > 0) {
                                mNoticTitle.setText(mNoticeList.get(0).Title);
                                mNoticText.setText(mNoticeList.get(0).Description);
                            }
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    public void invateInit(View v) {
        mInvateLayout = (LinearLayout) v.findViewById(R.id.InvateLayout);

        mInvateLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Invate", null);
                Intent intent = new Intent(getSherlockActivity(), InviteActivity.class);
                startActivity(intent);
            }
        });
    }

    public void recipeListInit(View v) {
        mRecipeLayout = (LinearLayout) v.findViewById(R.id.RecipeLayout);
    }

    public void recipeMakeView() {
        LinearLayout item1 = (LinearLayout) mRecipeLayout.findViewById(R.id.item1);
        LinearLayout item2 = (LinearLayout) mRecipeLayout.findViewById(R.id.item2);

        ImageView imageView1 = (ImageView) mRecipeLayout.findViewById(R.id.image1);
        ImageView imageView2 = (ImageView) mRecipeLayout.findViewById(R.id.image2);

        TextView title1 = (TextView) mRecipeLayout.findViewById(R.id.title1);
        TextView title2 = (TextView) mRecipeLayout.findViewById(R.id.title2);

        TextView name1 = (TextView) mRecipeLayout.findViewById(R.id.name1);
        TextView name2 = (TextView) mRecipeLayout.findViewById(R.id.name2);

        if (mRecipeDataList != null && mRecipeDataList.size() > 0) {
            mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mRecipeDataList.get(0).land_picture, imageView1, mOptionsProduct);
            title1.setText(mRecipeDataList.get(0).title);
            name1.setText(mRecipeDataList.get(0).cooking);

            item1.setTag(mRecipeDataList.get(0).idx);
            item1.setTag(R.integer.tag_st, mRecipeDataList.get(0).cooking);

            item1.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    Intent intent = new Intent(getActivity(), RecipeViewActivity.class);
                    intent.putExtra("recipe", (String) v.getTag());
                    intent.putExtra("cooking", (String) v.getTag(R.integer.tag_st));
                    startActivity(intent);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_RECIPE_LIST, "Click_Item", (String) v.getTag(R.integer.tag_st));
                }
            });

            if (mRecipeDataList.size() > 1) {
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mRecipeDataList.get(1).land_picture, imageView2, mOptionsProduct);
                title2.setText(mRecipeDataList.get(1).title);
                name2.setText(mRecipeDataList.get(1).cooking);

                item2.setTag(mRecipeDataList.get(1).idx);
                item2.setTag(R.integer.tag_st, mRecipeDataList.get(1).cooking);

                item2.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        Intent intent = new Intent(getActivity(), RecipeViewActivity.class);
                        intent.putExtra("recipe", (String) v.getTag());
                        intent.putExtra("cooking", (String) v.getTag(R.integer.tag_st));
                        startActivity(intent);
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_RECIPE_LIST, "Click_Item", (String) v.getTag(R.integer.tag_st));
                    }
                });
            }
        }

        mRecipeLayout.setVisibility(View.VISIBLE);
    }

    private void recipeGetData() {
        SnipeApiController.getRecipeList("1", "2", null, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    if (Code == 200) {

                        JsonNode root = JsonUtil.parseTree(content);
                        if (root.path("item").size() > 0) {
                            mRecipeDataList = (ArrayList<RecipeJson>) JsonUtil.jsonToArrayObject(root.path("item"), RecipeJson.class);
                        }
                        recipeMakeView();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEvaluationLayout.setVisibility(View.VISIBLE);
                            }
                        }, 100);
                    } else {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEvaluationLayout.setVisibility(View.VISIBLE);
                            }
                        }, 100);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEvaluationLayout.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEvaluationLayout.setVisibility(View.VISIBLE);
                    }
                }, 100);
            }
        });
    }

    public void eventListInit(View v) {
        mEventListLayout = (LinearLayout) v.findViewById(R.id.EventListLayout);
        mEventLayout = (LinearLayout) v.findViewById(R.id.EventLayout);
    }

    public void eventListMakeView() {
        for (EventJson eventJson : mEventList) {
            View view = mInflate.inflate(R.layout.item_home_event_list, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.Image);
            TextView titleSub = (TextView) view.findViewById(R.id.TitleSub);
            TextView dday = (TextView) view.findViewById(R.id.Dday);

            titleSub.setText(eventJson.sub_title);

            if (eventJson.status.equals("진행")) {
                dday.setText(eventJson.duration);
            } else {
                dday.setText(eventJson.status);
            }

            mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + eventJson.file.title.path, imageView, mOptionsProduct);
            view.setTag(eventJson.idx);

            view.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Event", null);
                    Intent intent = new Intent(getSherlockActivity(), SupportersDetailActivity.class);
                    intent.putExtra(SupportersDetailActivity.EVENT_IDX, (String) v.getTag());
                    startActivity(intent);
                }
            });

            mEventListLayout.addView(view);
            mEventLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getListEvent() {
        SnipeApiController.getEventList("1", "1", "T", new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                mEventList = (ArrayList<EventJson>) JsonUtil.jsonToArrayObject(root.path("item"), EventJson.class);
                                eventListMakeView();
                            }
                            //videoGetData();
                            recipeGetData();
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void farmerListMakeView() {
        ArrayList<RecommendListJson> mTemp = new ArrayList<>();
        for (int i = 0; i < mFarmerList.size(); i++) {
            mTemp.add(mFarmerList.get(i));
            if (i >= 7) {
                break;
            }
        }
        mFarmerList = mTemp;

        int count = (mFarmerList.size() / 4) + ((mFarmerList.size() % 4) > 0 ? 1 : 0);
        for (int i = 0; i < count; i++) {
            View view = mInflate.inflate(R.layout.item_home_farmer_list, null);

            ImageView imageView1 = (ImageView) view.findViewById(R.id.image1);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.image2);
            ImageView imageView3 = (ImageView) view.findViewById(R.id.image3);
            ImageView imageView4 = (ImageView) view.findViewById(R.id.image4);

            RecommendListJson recommendListJson1 = mFarmerList.get((i * 4) + 0);
            RecommendListJson recommendListJson2 = mFarmerList.get((i * 4) + 1);
            RecommendListJson recommendListJson3 = mFarmerList.get((i * 4) + 2);
            RecommendListJson recommendListJson4 = mFarmerList.get((i * 4) + 3);

            mImageLoader.displayImage(recommendListJson1.ProfileImage, imageView1, mOptionsProduct);
            mImageLoader.displayImage(recommendListJson2.ProfileImage, imageView2, mOptionsProduct);
            mImageLoader.displayImage(recommendListJson3.ProfileImage, imageView3, mOptionsProduct);
            mImageLoader.displayImage(recommendListJson4.ProfileImage, imageView4, mOptionsProduct);

            imageView1.setTag(recommendListJson1.FarmerIndex);
            imageView1.setTag(R.integer.tag_id, recommendListJson1.Farm);
            imageView2.setTag(recommendListJson2.FarmerIndex);
            imageView2.setTag(R.integer.tag_id, recommendListJson2.Farm);
            imageView3.setTag(recommendListJson3.FarmerIndex);
            imageView3.setTag(R.integer.tag_id, recommendListJson3.Farm);
            imageView4.setTag(recommendListJson4.FarmerIndex);
            imageView4.setTag(R.integer.tag_id, recommendListJson4.Farm);

            ViewOnClickListener viewOnClickListener = new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Farmer", (String) v.getTag(R.integer.tag_id));
                    Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
                    intent.putExtra("userType", "F");
                    intent.putExtra("userIndex", (String) v.getTag());
                    startActivity(intent);
                }
            };

            imageView1.setOnClickListener(viewOnClickListener);
            imageView2.setOnClickListener(viewOnClickListener);
            imageView3.setOnClickListener(viewOnClickListener);
            imageView4.setOnClickListener(viewOnClickListener);

            mFarmerListLayout.addView(view);
        }

        View line = new View(getActivity());
        line.setBackgroundColor(Color.parseColor("#eaeaea"));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        layoutParams.setMargins(0, 10, 0, 10);
        line.setLayoutParams(layoutParams);

        mFarmerListLayout.addView(line);
    }

    public void productListInit(View v) {
        mProductListLayout = (LinearLayout) v.findViewById(R.id.ProductListLayout);
        mProductLayout = (LinearLayout) v.findViewById(R.id.ProductLayout);
    }

    public void productListMakeView() {
        LinearLayout layoutProductItem1 = (LinearLayout) mProductListLayout.findViewById(R.id.LayoutProductItem1);
        LinearLayout layoutProductItem2 = (LinearLayout) mProductListLayout.findViewById(R.id.LayoutProductItem2);

        ImageView imgProductImage1 = (ImageView) mProductLayout.findViewById(R.id.ImgProductImage1);
        ImageView imgProductImage2 = (ImageView) mProductLayout.findViewById(R.id.ImgProductImage2);

        ImageView imgProductProfile1 = (ImageView) mProductLayout.findViewById(R.id.ImgProductProfile1);
        ImageView imgProductProfile2 = (ImageView) mProductLayout.findViewById(R.id.ImgProductProfile2);

        TextView txtProductName1 = (TextView) mProductLayout.findViewById(R.id.TxtProductName1);
        TextView txtProductName2 = (TextView) mProductLayout.findViewById(R.id.TxtProductName2);


        if (mProductList != null && mProductList.size() > 0) {
            txtProductName1.setText(mProductList.get(0).name.replace(" ", "\u00A0")); // 문자단위 줄바꿈
            mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mProductList.get(0).image1, imgProductImage1, mOptionsProduct);

            if (mProductList.get(1).profile_image != null && !mProductList.get(1).profile_image.isEmpty())
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + mProductList.get(0).profile_image, imgProductProfile1, mOptionsProfile);

            layoutProductItem1.setTag(mProductList.get(0).idx);
            layoutProductItem1.setTag(R.integer.tag_id, mProductList.get(0).name);
            layoutProductItem1.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Product", (String) v.getTag(R.integer.tag_id));
                    Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
                    intent.putExtra("productIndex", (String) v.getTag());
                    startActivity(intent);
                }
            });

            if (mProductList.size() > 1) {
                txtProductName2.setText(mProductList.get(1).name.replace(" ", "\u00A0")); // 문자단위 줄바꿈
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mProductList.get(1).image1, imgProductImage2, mOptionsProduct);

                if (mProductList.get(1).profile_image != null && !mProductList.get(1).profile_image.isEmpty())
                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + mProductList.get(1).profile_image, imgProductProfile2, mOptionsProfile);

                layoutProductItem2.setTag(mProductList.get(1).idx);
                layoutProductItem2.setTag(R.integer.tag_id, mProductList.get(1).name);
                layoutProductItem2.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Product", (String) v.getTag(R.integer.tag_id));
                        Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
                        intent.putExtra("productIndex", (String) v.getTag());
                        startActivity(intent);
                    }
                });
            }
            mProductLayout.setVisibility(View.VISIBLE);
        } else {
            mProductLayout.setVisibility(View.GONE);
        }
        mNoticeAndInvateLayout.setVisibility(View.VISIBLE);
    }

    private void getListProduct() {
        SnipeApiController.getProductListRecomend(String.valueOf(30), String.valueOf(2), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                mProductList = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
                                productListMakeView();
                            }
                            getListEvent();
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
            }
        });
    }

    public void evaluationInit(View v) {
        mEvaluationLayout = (LinearLayout) v.findViewById(R.id.EvaluationLayout);
        mEvaluationListLayout = (LinearLayout) v.findViewById(R.id.EvaluationListLayout);

        ((TextView) v.findViewById(R.id.EvaluationDes)).setText(Html.fromHtml(getString(R.string.evaluation_des)));

        mEvaluationLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getActivity(), EvaluationActivity.class);
                startActivity(intent);
            }
        });
    }

    public void impressiveInit(View v) {
        mImpressiveLayout = (RelativeLayout) v.findViewById(R.id.ImpressiveLayout);
        mImpressiveTextView = (RelativeLayout) v.findViewById(R.id.ImpressiveTextView);
        mImpressiveMoreView = (RelativeLayout) v.findViewById(R.id.ImpressiveMoreView);
        mImpressiveViewpager = (ViewPager) v.findViewById(R.id.ImpressiveViewpager);
        mImpressivePagingLayout = (LinearLayout) v.findViewById(R.id.ImpressivePagingLayout);
        mImpressiveText = (TextView) v.findViewById(R.id.ImpressiveText);

        mImpressiveAdapter = new ImpressiveAdapter(getActivity(), new ArrayList<DiaryListJson>(), ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
        mImpressiveViewpager.setAdapter(mImpressiveAdapter);

        mImpressiveViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mImpressiveAdapter.getCount() > 0) {
                    if (mImpressiveViewpager.getCurrentItem() == mImpressiveAdapter.getCount() - 1) {
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
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Swipe_Impressive", String.valueOf(mImpressiveViewpager.getCurrentItem()));
                displayViewPagerIndicator(mImpressiveViewpager.getCurrentItem());
            }
        });
    }

    private void getImpressiveList() {
        CenterController.getListDiaryHome(new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);
                            ArrayList<DiaryListJson> jsonArrayList = (ArrayList<DiaryListJson>) JsonUtil.jsonToArrayObject(root.findPath("List"), DiaryListJson.class);
                            ArrayList<DiaryListJson> mTempArrayList = new ArrayList<DiaryListJson>();
                            for (int i = 0; i < jsonArrayList.size(); i++) {
                                mTempArrayList.add(jsonArrayList.get(i));
                                if (i >= 5) {
                                    break;
                                }
                            }

                            mImpressiveAdapter.addAll(mTempArrayList);
                            mImpressiveAdapter.notifyDataSetChanged();
                            displayViewPagerIndicator(0);

                            mImpressiveLayout.setVisibility(View.VISIBLE);
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
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

    public class ImpressiveAdapter extends PagerAdapter {
        private Context context;
        private ImageLoader imageLoader;
        private ArrayList<DiaryListJson> items;

        public ImpressiveAdapter(Context context, ArrayList<DiaryListJson> items, ImageLoader imageLoader) {
            this.context = context;
            this.imageLoader = imageLoader;
            this.items = items;
        }

        public void addAll(ArrayList<DiaryListJson> items) {
            this.items = items;
        }

        public void add(DiaryListJson item) {
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
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = inflater.inflate(R.layout.view_home_impressive_pager, container, false);

            ImageView image = (ImageView) v.findViewById(R.id.imageView);

            DiaryListJson item = items.get(position);

            imageLoader.displayImage(item.ProductImage1, image, mOptionsProduct);

            image.setTag(item.Diary);
            image.setTag(R.id.Tag1, item.Farm);
            image.setTag(R.integer.tag_pos, position);

            image.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    if (getCount() - 1 == (Integer) v.getTag(R.integer.tag_pos)) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Impressive", "more");
                        Intent intent = new Intent(getSherlockActivity(), FarmStoryActivity.class);
                        startActivity(intent);
                    } else {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_HOME, "Click_Impressive", (String) v.getTag(R.id.Tag1));
                        Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
                        intent.putExtra("diary", (String) v.getTag());
                        intent.putExtra("farm", (String) v.getTag(R.id.Tag1));
                        intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
                        startActivity(intent);
                    }
                }
            });

            container.addView(v, container
                    .getChildCount() > position ? position
                    : container.getChildCount());

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
            CommonUtil.UiUtil.unbindDrawables((RelativeLayout) object);
            object = null;
        }
    }
}
