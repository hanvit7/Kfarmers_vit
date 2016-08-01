package com.leadplatform.kfarmers.view.market;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.DiaryListHolder;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJson;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.model.tag.LikeTag;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.OnKeyBackPressedListener;
import com.leadplatform.kfarmers.util.kakao.KaKaoController;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.OnLikeDiaryListener;
import com.leadplatform.kfarmers.view.cart.CartActivity;
import com.leadplatform.kfarmers.view.common.CWebview;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment.OnCloseShareDialogListener;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.farm.FarmSlideImageAdapter;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.payment.PaymentActivity;
import com.leadplatform.kfarmers.view.recipe.RecipeListActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProductFragment extends BaseRefreshMoreListFragment implements
        OnCloseShareDialogListener, OnKeyBackPressedListener {
    public static final String TAG = "ProductFragment";

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private ImageLoader imageLoader;
    private DisplayImageOptions options, optionsProfile;

    private YouTubeThumbnailLoader mYouTubeThumbnailLoader;

    private ProductJson mItem;
    private List<ProductJson> mOptionList;

    private String productIndex;
    private View headerView;
    private ViewPager imageViewPager;

    private LinearLayout PagingLayout;
    private ImageView mFarmLayout;

    private ArrayList<DiaryListJson> farmObjectList;
    private FarmListAdapter farmListAdapter;

    private final int limit = 20;

    private Button btnHeaderInfo, btnHeaderDes, btnHeaderReview;

    ImageView imgHeaderAuth,imgHeaderRecipy,imgHeaderFarm;

    //private View webViewLine;

    private LinearLayout mTypeLayout;

    private TextView mTypeDes;

    private WebView desWebView, infoWebView;

    private boolean bMoreFlag = false;

    private Button mBuyBtn, mInquiryBtn;

    private RelativeLayout mListTitletLayout, mReviewTitleLayout;

    private RatingBar mRatingBar;

    private TextView mRatingText;

    //private ProductMiniCartView mProductMiniCartView;

    private FrameLayout mYoutubeViewLayout;
    private YouTubeThumbnailView mYouTubeThumbnailView;

    private RelativeLayout mNoReviewLayout;

    private ReviewAdapter mReviewAdapter;

    private int mReviewPage = 1;
    private boolean bReviewMoreFlag = false;

    private ArrayList<RecipeJson> mRecipeJson;

    private String mNotice;

    private LinearLayout mNoticeLayout;
    private TextView mNoticeText;

    //private boolean isFullPlayer = false;
    //private YouTubePlayerSupportFragment mYouTubePlayerFragment;
    //private YouTubePlayer mYoutubePlayer;

    public static ProductFragment newInstance(String productIndex) {
        final ProductFragment f = new ProductFragment();

        final Bundle args = new Bundle();
        args.putString("productIndex", productIndex);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productIndex = getArguments().getString("productIndex");
        }

        imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
                .build();

        optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_product, container,false);
        headerView = inflater.inflate(R.layout.fragment_product_header, null,false);

        mNoReviewLayout = (RelativeLayout) headerView.findViewById(R.id.NoReviewLayout);

        mListTitletLayout = (RelativeLayout) headerView.findViewById(R.id.TitleLayout);
        mReviewTitleLayout = (RelativeLayout) headerView.findViewById(R.id.ReviewTitleLayout);

        mRatingBar = (RatingBar) headerView.findViewById(R.id.Ratingbar);
        mRatingText = (TextView) headerView.findViewById(R.id.RatingText);

        mNoticeLayout = (LinearLayout) headerView.findViewById(R.id.noticeView);
        mNoticeText = (TextView) headerView.findViewById(R.id.noticeText);

        imageViewPager = (ViewPager) headerView
                .findViewById(R.id.image_viewpager);
        PagingLayout = (LinearLayout) headerView.findViewById(R.id.Paging);

        btnHeaderDes = (Button) headerView.findViewById(R.id.Btn_Des);
        btnHeaderInfo = (Button) headerView.findViewById(R.id.Btn_Info);
        btnHeaderReview = (Button) headerView.findViewById(R.id.Btn_Review);

        imgHeaderAuth = (ImageView) headerView.findViewById(R.id.img_auth);
        imgHeaderRecipy = (ImageView) headerView.findViewById(R.id.img_recipe);
        imgHeaderFarm = (ImageView) headerView.findViewById(R.id.img_farm);

        mYoutubeViewLayout = (FrameLayout) headerView.findViewById(R.id.YoutubeViewLayout);

        mYoutubeViewLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Youtube", null);
                ((ProductActivity) getActivity()).youtubeFragment(mItem.video_url);
            }
        });

        btnHeaderDes.setSelected(true);

        btnHeaderDes.setOnClickListener(clickListener);
        btnHeaderInfo.setOnClickListener(clickListener);
        btnHeaderReview.setOnClickListener(clickListener);

        /*mFarmLayout = (ImageView) headerView.findViewById(R.id.Introduction);

        mFarmLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getActivity(), FarmActivity.class);
                intent.putExtra("userType", "F");
                intent.putExtra("userIndex", mItem.member_idx);
                startActivity(intent);
            }
        });*/

        //webViewLine = (View) headerView.findViewById(R.id.ContentLine);

        mTypeLayout = (LinearLayout) headerView.findViewById(R.id.TypeLayout);
        mTypeDes = (TextView) headerView.findViewById(R.id.TypeDes);

        desWebView = (WebView) headerView.findViewById(R.id.DescriptionWebView);
        //desWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        desWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        desWebView.getSettings().setLoadWithOverviewMode(true);
        desWebView.getSettings().setUseWideViewPort(true);

        desWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mItem.video_url && !mItem.video_url.isEmpty()) {
                            mYoutubeViewLayout.setVisibility(View.VISIBLE);
                        }
                        mTypeDes.setVisibility(View.VISIBLE);
                    }
                }, 200);
            }
        });

        infoWebView = (WebView) headerView.findViewById(R.id.InfoWebView);
        infoWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        infoWebView.getSettings().setLoadWithOverviewMode(true);
        infoWebView.getSettings().setUseWideViewPort(true);


        /*v.findViewById(R.id.CartBtn).setOnClickListener(clickListener);*/
        mBuyBtn = (Button) v.findViewById(R.id.BuyBtn);
        mInquiryBtn = (Button) v.findViewById(R.id.InquiryBtn);
        mBuyBtn.setOnClickListener(clickListener);
        mInquiryBtn.setOnClickListener(clickListener);

        //((View)v.findViewById(R.id.More)).setOnClickListener(clickListener);
        headerView.findViewById(R.id.DiaryMoreView)
                .setOnClickListener(clickListener);

        mYouTubeThumbnailView = (YouTubeThumbnailView) headerView.findViewById(R.id.thumbnail);

        //setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, null);

        //initMiniCartView(v);

        //mProductMiniCartView.setData(arrayList);
        //mProductMiniCartView.SetProductInitView();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mYouTubeThumbnailLoader != null) {
            mYouTubeThumbnailLoader.release();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mYouTubeThumbnailLoader != null) {
            mYouTubeThumbnailLoader.release();
        }
    }

    public void initYoutubeThumbnail() {
        try {
            mYouTubeThumbnailView.setTag(mItem.video_url);
            mYouTubeThumbnailView.initialize(Constants.YOUTUBE_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {

                    mYouTubeThumbnailLoader = loader;
                    mYouTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                        @Override
                        public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                        }

                        @Override
                        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        }
                    });
                    String videoId = (String) view.getTag();
                    mYouTubeThumbnailLoader.setVideo(videoId);
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                }
            });

        } catch (Exception e) {
        }
    }

    /*public void initYoutubePlayer()
    {
        // 유투브 플레이어 삽입
        FragmentManager fragmentManager = getFragmentManager();
        mYouTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        mYouTubePlayerFragment.initialize(Constants.YOUTUBE_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                //youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
                youTubePlayer.setShowFullscreenButton(false);
                mYoutubePlayer = youTubePlayer;
                mYoutubePlayer.cueVideo(mItem.video_url);
                *//*mYoutubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                    @Override
                    public void onFullscreen(boolean b) {
                        isFullPlayer = b;
                    }
                });*//*
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult errorReason) {
                if (errorReason.isUserRecoverableError()) {

                    errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
                } else {
                    String errorMessage = String.format(
                            "There was an error initializing the YouTubePlayer (%1$s)",
                            errorReason.toString());
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.YoutubeView, mYouTubePlayerFragment);
        fragmentTransaction.commit();
    }*/

   /* public void initMiniCartView(View v)
    {
        mProductMiniCartView = null;
        mProductMiniCartView = new ProductMiniCartView((WrappingSlidingDrawer)v.findViewById(R.id.ProductDrawer),getActivity());
        mProductMiniCartView.hideHandleView();
        mProductMiniCartView.setBtnListener(new ProductMiniCartView.MiniCartListener(){

            @Override
            public void onCartBtn(ArrayList<ProductJson> productJsons) {
                addCartData(false, productJsons);
            }

            @Override
            public void onBuyBtn(ArrayList<ProductJson> productJsons) {
                addCartData(true, productJsons);
            }
        });
    }*/

    public void addCartData(final Boolean buyDirect, ArrayList<ProductJson> productJsons) {
        try {
            UserDb user = DbController.queryCurrentUser(getActivity());
            if (user == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            } else {

                String id = user.getUserID();
                String buy_Direct = "";
                if (!buyDirect) {
                    buy_Direct = "F";
                } else {
                    buy_Direct = "T";
                }

                // item
                JSONArray itemJsonArray = new JSONArray();

                JSONObject itemObject = new JSONObject();
                itemObject.put("idx", mItem.idx);

                // option
                JSONArray optionJsonArray = new JSONArray();
                for (ProductJson subItemJson : productJsons) {
                    JSONObject optionObject = new JSONObject();
                    optionObject.put("idx", subItemJson.idx);
                    optionObject.put("cnt", subItemJson.count);
                    optionObject.put("price", subItemJson.buyprice);
                    optionObject.put("point",subItemJson.provide_point);
                    optionJsonArray.add(optionObject);
                }
                itemObject.put("option", optionJsonArray);
                itemJsonArray.add(itemObject);

                SnipeApiController.insertCartItem(id, buy_Direct, itemJsonArray, new SnipeResponseListener(getActivity()) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        if (buyDirect) {
                            ArrayList<String> idx = new ArrayList<String>();
                            idx.add(mItem.idx);

                            Intent intent = new Intent(getActivity(), PaymentActivity.class);
                            intent.putExtra("idx", idx);
                            intent.putExtra("direct", "T");

                            startActivityForResult(intent, PaymentActivity.PAYMENT_CHECK);
                        } else {
                            UiController.showDialog(getActivity(), getString(R.string.MenuLCart), getString(R.string.dialog_cart_add), R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
                                @Override
                                public void onDialog(int type) {
                                    if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Dialog-Cart", "true");
                                        startActivity(new Intent(getActivity(), CartActivity.class));
                                    } else {
                                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Dialog-Cart", "false");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ProductActivity) activity).setOnKeyBackPressedListener(this);
    }

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (getListAdapter() == mReviewAdapter) {
                if (bReviewMoreFlag == true) {
                    bReviewMoreFlag = false;
                    getReviewData();
                } else {
                    onLoadMoreComplete();
                }
            } else if (getListAdapter() == farmListAdapter) {
                if (bMoreFlag == true) {
                    bMoreFlag = false;
                    getListDiary(farmListAdapter.getItem(farmListAdapter.getCount() - 1).Diary, farmListAdapter.getItem(farmListAdapter.getCount() - 1).RegistrationDate2);
                } else {
                    onLoadMoreComplete();
                }
            }
            else {
                onLoadMoreComplete();
            }
        }
    };

/*	private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
		public void onRefreshStarted(View view) {
			getFarmerShopProduct();
		}
	};*/

    OnClickListener clickListener = new OnClickListener() {

        Intent intent;
        UserDb user;

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.Btn_Des:
                    if (btnHeaderDes.isSelected())
                        break;

                    btnHeaderDes.setSelected(true);
                    btnHeaderInfo.setSelected(false);
                    btnHeaderReview.setSelected(false);
                    infoWebView.setVisibility(View.GONE);
                    desWebView.setVisibility(View.VISIBLE);

                    if (null != mItem.video_url && !mItem.video_url.isEmpty()) {
                        //RelativeLayout.LayoutParams deaParam = (RelativeLayout.LayoutParams) mYoutubeViewLayout.getLayoutParams();
                        //deaParam.addRule(RelativeLayout.BELOW, R.id.DescriptionWebView);
                        //mYoutubeViewLayout.setLayoutParams(deaParam);
                        mYoutubeViewLayout.setVisibility(View.VISIBLE);
                    }

                    mTypeLayout.setVisibility(View.VISIBLE);
                    mListTitletLayout.setVisibility(View.VISIBLE);
                    mReviewTitleLayout.setVisibility(View.GONE);
                    mNoReviewLayout.setVisibility(View.GONE);
                    headerView.setBackgroundColor(Color.parseColor("#eeeeee"));
                    getView().setBackgroundColor(Color.parseColor("#eeeeee"));

                    RelativeLayout.LayoutParams desParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    desParam.addRule(RelativeLayout.BELOW, R.id.YoutubeViewLayout);
                    //webViewLine.setLayoutParams(desParam);
                    mTypeLayout.setLayoutParams(desParam);

                    setListAdapter(farmListAdapter);

                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Explanation", null);

                    break;

                case R.id.Btn_Info:
                    if (btnHeaderInfo.isSelected())
                        break;

                    btnHeaderDes.setSelected(false);
                    btnHeaderInfo.setSelected(true);
                    btnHeaderReview.setSelected(false);
                    infoWebView.setVisibility(View.VISIBLE);
                    desWebView.setVisibility(View.GONE);

                    mTypeLayout.setVisibility(View.GONE);

                /*RelativeLayout.LayoutParams InfoParam = (RelativeLayout.LayoutParams) mYoutubeViewLayout.getLayoutParams();
                InfoParam.addRule(RelativeLayout.BELOW, R.id.InfoWebView);
                mYoutubeViewLayout.setLayoutParams(InfoParam);*/

                    mYoutubeViewLayout.setVisibility(View.GONE);
                    mListTitletLayout.setVisibility(View.VISIBLE);
                    mReviewTitleLayout.setVisibility(View.GONE);
                    mNoReviewLayout.setVisibility(View.GONE);
                    headerView.setBackgroundColor(Color.parseColor("#eeeeee"));
                    getView().setBackgroundColor(Color.parseColor("#eeeeee"));

                    RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    infoParams.addRule(RelativeLayout.BELOW, R.id.InfoWebView);
                    //webViewLine.setLayoutParams(infoParams);
                    mTypeLayout.setLayoutParams(infoParams);

                    setListAdapter(farmListAdapter);

                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Info", null);

                    break;

                case R.id.Btn_Review:

                    btnHeaderDes.setSelected(false);
                    btnHeaderInfo.setSelected(false);
                    btnHeaderReview.setSelected(true);

                    infoWebView.setVisibility(View.GONE);
                    desWebView.setVisibility(View.GONE);
                    mTypeLayout.setVisibility(View.GONE);
                    mYoutubeViewLayout.setVisibility(View.GONE);
                    mListTitletLayout.setVisibility(View.GONE);

                    if (mReviewAdapter.getCount() > 0) {
                        mReviewTitleLayout.setVisibility(View.VISIBLE);
                        mNoReviewLayout.setVisibility(View.GONE);
                        setListAdapter(mReviewAdapter);
                        headerView.setBackgroundColor(Color.parseColor("#eeeeee"));
                        getView().setBackgroundColor(Color.parseColor("#eeeeee"));
                    } else {
                        setListAdapter(null);
                        mReviewTitleLayout.setVisibility(View.GONE);
                        mNoReviewLayout.setVisibility(View.VISIBLE);
                        headerView.setBackgroundColor(Color.parseColor("#ffffff"));
                        getView().setBackgroundColor(Color.parseColor("#ffffff"));
                    }


                    break;
                case R.id.DiaryMoreView:

                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item-More", mItem.farm_name);

                    intent = new Intent(getSherlockActivity(), FarmActivity.class);
                    intent.putExtra("userType", "F");
                    intent.putExtra("userIndex", mItem.member_idx);
                    intent.putExtra("userCategory", mItem.category);
                    startActivity(intent);
                    break;

                case R.id.BuyBtn:
                    if (mItem.verification.equals(ProductActivity.TYPE3)) {
                        /*Intent intent = new Intent(getActivity(), ProductInquiryActivity.class);
                        intent.putExtra(ProductInquiryActivity.DATA, mItem);
                        intent.putExtra(ProductInquiryActivity.OPTION, (Serializable) mOptionList);
                        startActivity(intent);*/

                        Intent intent = new Intent(getActivity(), CWebview.class);

                        /*if (BuildConfig.DEBUG) {
                            intent.putExtra("url","http://refarm.net/"+mItem.id+"/"+mItem.code+"?view_type=mini");
                        } else {
                            intent.putExtra("url","http://farmorder.kr/"+mItem.id+"/"+mItem.code+"?view_type=mini");
                        }*/
                        intent.putExtra("url","http://farmorder.kr/"+mItem.id+"/"+mItem.code+"?view_type=mini");
                        startActivity(intent);
                        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://farmorder.kr/"+mItem.id+"/"+mItem.code+"?view_type=mini"));
                        //startActivity(intent);
                    } else {
                        user = DbController.queryCurrentUser(getActivity());
                        if (user == null) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            return;
                        }
                        if (user.getUserID().equals(mItem.id)) {
                            Toast.makeText(getActivity(), "본인 상품 입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Option", mItem.name);

                        intent = new Intent(getActivity(), ProductOptionActivity.class);
                        intent.putExtra(ProductOptionActivity.INTENT_OPTION_LIST, (ArrayList<ProductJson>) mOptionList);
                        intent.putExtra(ProductOptionActivity.INTENT_OPTION_NAME, mItem.name);
                        startActivityForResult(intent, ProductOptionActivity.REQUEST_OPTION);
                    }


                /*ProfileJson profileJson = null;
                try {
                    String profile = DbController.queryProfileContent(getActivity());
                    JsonNode root = JsonUtil.parseTree(profile);
                    profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (profileJson == null)
                {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                else
                {
                    if(profileJson.PayAgreement.equals("F"))
                    {
                        Intent intent = new Intent(getActivity(), ShopAgreementActivity.class);
                        startActivity(intent);
                    }
                    else {
                        mProductMiniCartView.showVew();
                    }
                }*/
                    break;
                case R.id.InquiryBtn:
                    user = DbController.queryCurrentUser(getActivity());
                    if (user == null) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        return;
                    } else {
                        if (user.getUserID().equals(mItem.id)) {
                            Toast.makeText(getActivity(), "본인 상품 입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SnipeApiController.checkChatRoom(user.getUserID(), mItem.id, new SnipeResponseListener(getActivity()) {
                            @Override
                            public void onSuccess(int Code, String content, String error) {
                                try {
                                    switch (Code) {
                                        case 200:
                                            Intent intent = new Intent(getActivity(), InquiryActivity.class);
                                            intent.putExtra("index", content);
                                            startActivity(intent);
                                            break;
                                        default:
                                            UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                                    }
                                } catch (Exception e) {
                                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                                }
                            }
                        });
                    }
                    break;
            /*case R.id.CartBtn:
                UserDb user = DbController.queryCurrentUser(getActivity());
                if (user == null)
                {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    startActivity(new Intent(getActivity(), CartActivity.class));
                }
                break;*/
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().addHeaderView(headerView);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        if (farmListAdapter == null) {
            double latitude = AppPreferences.getLatitude(getSherlockActivity());
            double longitude = AppPreferences
                    .getLongitude(getSherlockActivity());
            farmObjectList = new ArrayList<DiaryListJson>();
            farmListAdapter = new FarmListAdapter(getSherlockActivity(),
                    R.layout.item_farm_diary, farmObjectList,
                    ((BaseFragmentActivity) getSherlockActivity()).imageLoader,
                    latitude, longitude);

			 /*SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(farmListAdapter);
			 swingBottomInAnimationAdapter.setAbsListView(getListView());

			 assert swingBottomInAnimationAdapter.getViewAnimator() != null;
			 swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);*/

            setListAdapter(farmListAdapter);
        }
        if (mReviewAdapter == null) {
            mReviewAdapter = new ReviewAdapter(getActivity(), R.layout.item_review);
        }
        getFarmerShopProduct();
    }

    private void getFarmerShopProduct() {

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(productIndex);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("idx", jsonArray);

        SnipeApiController.getProductItem(jsonObject, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);

                            mItem = (ProductJson) JsonUtil.jsonToObject(root.path("item").toString(), ProductJson.class);
                            mOptionList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("option"), ProductJson.class);
                            mRecipeJson = (ArrayList<RecipeJson>) JsonUtil.jsonToArrayObject(root.path("recipe"), RecipeJson.class);

                            mNotice = root.path("notice").textValue();

                            displayFarmHeaderView();

                            if (null != mItem.video_url && !mItem.video_url.isEmpty()) {
                                initYoutubeThumbnail();
                                //initYoutubePlayer();
                            }
                            //mProductMiniCartView.setData((ArrayList<ProductJson>) mOptionList);
                            getListDiary("", "");
                            getReviewData();

                            mRatingBar.setRating((Float.parseFloat(mItem.rating) / 10f) / 2f);
                            mRatingText.setText(Html.fromHtml("<b>" + mItem.rating + "%</b>" + " (" + mItem.rating_count + "명)"));
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
		/*CenterController.getViewFarmerShopProduct(productIndex,
				new CenterResponseListener(getSherlockActivity()) {
					@Override
					public void onSuccess(int Code, String content) {
						try {
							switch (Code) {
							case 0000:
								JsonNode root = JsonUtil.parseTree(content);
								productDetailJson = (ProductDetailJson) JsonUtil
										.jsonToObject(root.findPath("Row")
												.toString(),
												ProductDetailJson.class);
								if (productDetailJson != null) {
									displayFarmHeaderView();
									getListDiary("","");
								}
								break;

							default:
								UiController.showDialog(getSherlockActivity(),
										R.string.dialog_unknown_error);
								break;
							}
						} catch (Exception e) {
							UiController.showDialog(getSherlockActivity(),
									R.string.dialog_unknown_error);
						}
					}
				});*/
    }

    private void getListDiary(String oldIndex, String oldDate) {
        if (mItem == null)
            return;

        String category = "";
        for(ProductJson.Categorys categorys : mItem.categorys) {
            if(!category.isEmpty()) {
                category +=",";
            }
            category += categorys.idx;
        }

        CenterController.getListDiaryByCategory2(
                category, mItem.id, limit, oldIndex, oldDate,
                new CenterResponseListener(getSherlockActivity()) {
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

                                        Iterator<JsonNode> it = root.findPath(
                                                "List").iterator();
                                        while (it.hasNext()) {
                                            diaryCount++;
                                            DiaryListJson diary = (DiaryListJson) JsonUtil
                                                    .jsonToObject(it.next()
                                                                    .toString(),
                                                            DiaryListJson.class);
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
                            UiController.showDialog(getSherlockActivity(),
                                    R.string.dialog_unknown_error);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] content, Throwable error) {
                        onRefreshComplete();
                        onLoadMoreComplete();
                        super.onFailure(statusCode, headers, content, error);

                    }
                });
    }

    private class FarmListAdapter extends ArrayAdapter<DiaryListJson> {
        private int itemLayoutResourceId;
        // private DisplayImageOptions options;
        private double userLatitude, userLongitude;

        public FarmListAdapter(Context context, int itemLayoutResourceId,
                               ArrayList<DiaryListJson> items, ImageLoader imageLoader,
                               double userLatitude, double userLongitude) {
            super(context, itemLayoutResourceId, items);
            this.itemLayoutResourceId = itemLayoutResourceId;
            // this.options = new
            // DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            // .bitmapConfig(Config.RGB_565).displayer(new
            // CustomRoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(),
            // 50)/2)).showImageOnLoading(R.drawable.common_dummy).build();
            this.userLatitude = userLatitude;
            this.userLongitude = userLongitude;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DiaryListHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSherlockActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);

                holder = new DiaryListHolder();
                holder.rootLayout = (RelativeLayout) convertView
                        .findViewById(R.id.Top);
                // holder.Profile = (ImageView)
                // convertView.findViewById(R.id.Profile);
                // holder.Farmer = (TextView)
                // convertView.findViewById(R.id.Farmer);
                holder.Category = (TextView) convertView
                        .findViewById(R.id.Category);
                holder.Auth1 = (ImageView) convertView.findViewById(R.id.Auth);
                holder.Verification = (ImageView) convertView.findViewById(R.id.Verification);
                holder.Date = (TextView) convertView.findViewById(R.id.Date);
                holder.Description = (TextView) convertView
                        .findViewById(R.id.Description);
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageview);
                holder.Address = (TextView) convertView
                        .findViewById(R.id.Address);
                holder.FoodMile = (TextView) convertView
                        .findViewById(R.id.FoodMile);
                holder.LikeText = (TextView) convertView
                        .findViewById(R.id.LikeText);
                holder.ReplyText = (TextView) convertView
                        .findViewById(R.id.ReplyText);
                holder.Like = (RelativeLayout) convertView
                        .findViewById(R.id.Like);
                holder.Reply = (RelativeLayout) convertView
                        .findViewById(R.id.Reply);
                holder.Share = (RelativeLayout) convertView
                        .findViewById(R.id.Share);
                holder.FarmImageView = (ImageButton) convertView
                        .findViewById(R.id.farmImageView);
                holder.Blind = (TextView) convertView.findViewById(R.id.Blind);
                //holder.ImgCount = (TextView) convertView.findViewById(R.id.ImgCount);

                convertView.setTag(holder);
            } else {
                holder = (DiaryListHolder) convertView.getTag();
            }

            final DiaryListJson diary = getItem(position);

            if (diary != null) {
                holder.rootLayout.setTag(new String(diary.Diary));
                holder.rootLayout.setTag(R.integer.tag_id, new String(diary.Farm));
                holder.rootLayout.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        String diary = (String) v.getTag();

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item", (String) v.getTag(R.integer.tag_id));

                        Intent intent = new Intent(getSherlockActivity(),
                                DiaryDetailActivity.class);
                        intent.putExtra("diary", diary);
                        intent.putExtra("type",
                                DiaryDetailActivity.DETAIL_DIARY);
                        startActivityForResult(intent,
                                Constants.REQUEST_DETAIL_DIARY);
                    }
                });

                // if (!PatternUtil.isEmpty(diary.ProfileImage))
                // {
                // imageLoader.displayImage(diary.ProfileImage, holder.Profile,
                // options);
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

                if (!diary.Verification.equals("N")) {
                    holder.Verification.setVisibility(View.VISIBLE);
                } else {
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

                if (!PatternUtil.isEmpty(diary.Reply)
                        && !diary.Reply.equals("0")) {
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
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item-Like", tag.index);
                        ((BaseFragmentActivity) getSherlockActivity())
                                .centerLikeDiary(tag.index,
                                        new OnLikeDiaryListener() {
                                            @Override
                                            public void onResult(int code,
                                                                 boolean plus) {
                                                if (code == 0) {
                                                    int count = Integer
                                                            .parseInt(getItem(tag.position).Like);
                                                    if (plus) {
                                                        getItem(tag.position).Like = String
                                                                .valueOf(count + 1);
                                                    } else {
                                                        if (count != 0)
                                                            getItem(tag.position).Like = String
                                                                    .valueOf(count - 1);
                                                    }
                                                    farmListAdapter
                                                            .notifyDataSetChanged();
                                                }
                                            }
                                        });
                    }
                });

                holder.Reply.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item-Reply", diary.Diary);
                        if (diary.Type.equals("F")) {
                            ((BaseFragmentActivity) getSherlockActivity())
                                    .runReplyActivity(
                                            ReplyActivity.REPLY_TYPE_FARMER,
                                            diary.Farm, diary.Diary);
                        } else if (diary.Type.equals("V")) {
                            ((BaseFragmentActivity) getSherlockActivity())
                                    .runReplyActivity(
                                            ReplyActivity.REPLY_TYPE_VILLAGE,
                                            diary.Farm, diary.Diary);
                        }
                    }
                });

                holder.Share.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        try {
                            KaKaoController.onShareBtnClicked(
                                    getSherlockActivity(),
                                    JsonUtil.objectToJson(diary), TAG);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                if (!PatternUtil.isEmpty(diary.Latitude)
                        && !PatternUtil.isEmpty(diary.Longitude)
                        && userLatitude != 0 && userLongitude != 0) {
                    double lat = Double.valueOf(diary.Latitude);
                    double lon = Double.valueOf(diary.Longitude);
                    Location foodLocation = new Location("Food");
                    Location userLocation = new Location("User");

                    foodLocation.setLatitude(lat);
                    foodLocation.setLongitude(lon);
                    userLocation.setLatitude(userLatitude);
                    userLocation.setLongitude(userLongitude);

                    float meters = userLocation.distanceTo(foodLocation);
                    String distance = (diary.Type.equals("V")) ? "거리 "
                            : "푸드마일 ";
                    holder.FoodMile.setText(distance + (int) meters / 1000
                            + "km");
                    holder.FoodMile.setVisibility(View.VISIBLE);

                    if (!PatternUtil.isEmpty(diary.AddressKeyword1)
                            && !PatternUtil.isEmpty(diary.AddressKeyword2)) {
                        holder.Address.setText(diary.AddressKeyword1 + " > "
                                + diary.AddressKeyword2);
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

/*				if (images.size() != 0) {
					holder.imageViewPager.setAdapter(new BaseSlideImageAdapter(
							getSherlockActivity(), ProductFragment.this,
							Constants.REQUEST_DETAIL_DIARY, diary.Diary,
							DiaryDetailActivity.DETAIL_DIARY, images,
							imageLoader));
					holder.imageViewPager.setPageMargin((int) getResources()
							.getDimension(R.dimen.image_pager_margin));
					holder.imageViewPager.setVisibility(View.VISIBLE);
				} else {
					holder.imageViewPager.setVisibility(View.GONE);
				}*/

                if (images.size() != 0) {

                    imageLoader.displayImage(images.get(0), holder.imageView, options);
                    holder.imageView.setVisibility(View.VISIBLE);

                    holder.imageView.setTag(diary.Diary);
                    holder.imageView.setTag(R.id.Tag1, diary.Farm);

                    holder.imageView.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(View v) {

                            KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item", (String) v.getTag());

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

                holder.FarmImageView.setVisibility(View.GONE);

                if (diary.Blind.equals("Y")) {
                    holder.Blind.setVisibility(View.VISIBLE);
                } else {
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

    private void displayFarmHeaderView() {
        ImageLoader imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;

        ((ProductActivity) getSherlockActivity()).displayActionBarTitleText(mItem.farm_name);

        ArrayList<String> images = new ArrayList<String>();
        if (!PatternUtil.isEmpty(mItem.image1))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image1);
        if (!PatternUtil.isEmpty(mItem.image2))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image2);
        if (!PatternUtil.isEmpty(mItem.image3))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image3);
        if (!PatternUtil.isEmpty(mItem.image4))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image4);
        if (!PatternUtil.isEmpty(mItem.image5))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image5);
        if (!PatternUtil.isEmpty(mItem.image6))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image6);
        if (!PatternUtil.isEmpty(mItem.image7))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image7);
        if (!PatternUtil.isEmpty(mItem.image8))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image8);
        if (!PatternUtil.isEmpty(mItem.image9))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image9);
        if (!PatternUtil.isEmpty(mItem.image10))
            images.add(Constants.KFARMERS_SNIPE_IMG + mItem.image10);

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
                .build();

        if (images.size() != 0) {
            imageViewPager.setAdapter(new FarmSlideImageAdapter(
                    getSherlockActivity(), images, imageLoader, options));
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
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Swipe_Image", String.valueOf(imageViewPager.getCurrentItem()));
                }
            });
        } else {
            imageViewPager.setVisibility(View.INVISIBLE);
        }

        if (mItem.summary != null && !mItem.summary.isEmpty()) {
            ((TextView) headerView.findViewById(R.id.SummaryText)).setText(mItem.summary);
        } else {
            ((TextView) headerView.findViewById(R.id.SummaryText)).setVisibility(View.GONE);
        }

        try {
            ((TextView) headerView.findViewById(R.id.Title)).setText(mItem.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈;
        } catch (Exception e) {
            ((TextView) headerView.findViewById(R.id.Title)).setText(mItem.name);
        }

        TextView price = (TextView) headerView.findViewById(R.id.Price);
        TextView dcPrice = (TextView) headerView.findViewById(R.id.DcPrice);
        TextView poindAdd = (TextView) headerView.findViewById(R.id.TextPointAdd);
        if (mOptionList.size() > 0) {
            int itemPrice = (int) Double.parseDouble(mOptionList.get(0).price);
            int itemBuyPrice = (int) Double.parseDouble(mOptionList.get(0).buyprice);

            if (itemPrice > itemBuyPrice) {
                price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice) + getResources().getString(R.string.korea_won));
                dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemBuyPrice) + getResources().getString(R.string.korea_won));
                price.setVisibility(View.VISIBLE);

                price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                price.setText("");
                dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemBuyPrice) + getResources().getString(R.string.korea_won));
                price.setVisibility(View.GONE);
                price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            int point = Integer.parseInt(mOptionList.get(0).provide_point);
            if(point > 0) {
                poindAdd.setText(Html.fromHtml(getString(R.string.Product_point,CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(point))));
            }
        }

        if(mNotice != null && !mNotice.trim().isEmpty()) {
            mNoticeText.setText(Html.fromHtml(mNotice));
            mNoticeLayout.setVisibility(View.VISIBLE);
        } else {
            mNoticeLayout.setVisibility(View.GONE);
        }

        desWebView.loadUrl(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/product/content/" + mItem.idx);
        infoWebView.loadUrl(Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/product/notification/" + mItem.notification_type + "/" + mItem.idx);

        infoWebView.setVisibility(View.GONE);
        desWebView.setVisibility(View.VISIBLE);

        if (mItem.verification.equals(ProductActivity.TYPE3)) {
            btnHeaderInfo.setVisibility(View.GONE);
            mTypeDes.setText(Html.fromHtml(getString(R.string.product_type_normal)));
        } else {
            btnHeaderInfo.setVisibility(View.VISIBLE);
            mTypeDes.setText(Html.fromHtml(getString(R.string.product_type_verify)));
        }

        if (mItem.verification.equals(ProductActivity.TYPE1)) {
            imgHeaderAuth.setImageResource(R.drawable.user_auth);
            imgHeaderAuth.setVisibility(View.VISIBLE);
        } else if (mItem.verification.equals(ProductActivity.TYPE2)) {
            imgHeaderAuth.setImageResource(R.drawable.md_auth);
            imgHeaderAuth.setVisibility(View.VISIBLE);
        } else {
            imgHeaderAuth.setVisibility(View.GONE);
        }

        if(mRecipeJson.size()>0) {
            imgHeaderRecipy.setVisibility(View.VISIBLE);
        } else {
            imgHeaderRecipy.setVisibility(View.GONE);
        }

        imgHeaderRecipy.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getActivity(), RecipeListActivity.class);
                intent.putExtra("code",mItem.code);
                startActivity(intent);
            }
        });

        imgHeaderFarm.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
                intent.putExtra("userType", "F");
                intent.putExtra("userIndex", mItem.member_idx);
                startActivity(intent);
            }
        });

		/*if (!PatternUtil.isEmpty(productDetailJson.ProductInfo))
			textInfo.setText(productDetailJson.ProductInfo.trim());*/

        //infoWebView.setLayoutParams(new LayoutParams(desWebView.getWidth(), 0));

        /*desWebView.setPadding(getResources().getDimensionPixelSize(R.dimen.CommonLargePadding), 0, getResources().getDimensionPixelSize(R.dimen.CommonLargePadding), 0);
        infoWebView.setPadding(getResources().getDimensionPixelSize(R.dimen.CommonLargePadding), 0, getResources().getDimensionPixelSize(R.dimen.CommonLargePadding), 0);*/

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCT_DETAIL, mItem.name);
    }

    private void displayViewPagerIndicator(int position) {
        PagingLayout.removeAllViews();

        for (int i = 0; i < imageViewPager.getAdapter().getCount(); i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getSherlockActivity());

            if (i != 0) {
                lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(
                        getSherlockActivity(), 3), 0, 0, 0);
                dot.setLayoutParams(lp);
            }

            if (i == position) {
                dot.setImageResource(R.drawable.button_farm_paging_on);
            } else {
                dot.setImageResource(R.drawable.button_farm_paging_off);
            }

            PagingLayout.addView(dot);
        }

        //mFarmLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDialogListSelection(int position, String object) {
        Log.e(TAG, "========= onDialogListSelection = position = " + position);
        try {
            DiaryListJson data = (DiaryListJson) JsonUtil.jsonToObject(object,
                    DiaryListJson.class);
            if (position == 0) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item-Share", "카카오톡");
                KaKaoController.sendKakaotalk(this, data);
            } else if (position == 1) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_DETAIL, "Click_Item-Share", "카카오스토리");
                KaKaoController.sendKakaostory(this, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBack() {

        /*if(mProductMiniCartView.isShow())
        {
            mProductMiniCartView.hideView();
        }*/
        /*else if(isFullPlayer)
        {
            mYoutubePlayer.setFullscreen(false);
            mYoutubePlayer.pause();
        }*/
        //else
        {
            ProductActivity productActivity = (ProductActivity) getActivity();
            productActivity.setOnKeyBackPressedListener(null);
            productActivity.onBackPressed();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PaymentActivity.PAYMENT_CHECK) {
            if (resultCode == PaymentActivity.PAYMENT_CHECK_ITEM) {
                getFarmerShopProduct();
            } else if (resultCode == PaymentActivity.PAYMENT_CHECK_OPTION) {
                getFarmerShopProduct();
            }
        }

        if (requestCode == ProductOptionActivity.REQUEST_OPTION) {
            try {
                ArrayList<ProductJson> arrayList = (ArrayList<ProductJson>) data.getSerializableExtra(ProductOptionActivity.INTENT_OPTION_LIST);
                if (resultCode == ProductOptionActivity.RESULT_CART) {
                    addCartData(false, arrayList);
                } else if (resultCode == ProductOptionActivity.RESULT_BUY) {
                    addCartData(true, arrayList);
                }
            } catch (Exception e) {
            }
            {
            }
        }

        /*else if (requestCode == RECOVERY_DIALOG_REQUEST) {
            initYoutubePlayer();
        }*/
    }

    private void getReviewData() {
        SnipeApiController.getReviewList(mItem.code,"","", String.valueOf(mReviewPage), String.valueOf(limit), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    onRefreshComplete();
                    onLoadMoreComplete();

                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                ArrayList<ReviewListJson> data = (ArrayList<ReviewListJson>) JsonUtil.jsonToArrayObject(root.path("item"), ReviewListJson.class);
                                mReviewAdapter.addAll(data);
                                mReviewPage++;

                                if (data.size() == limit)
                                    bReviewMoreFlag = true;
                                else
                                    bReviewMoreFlag = false;
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

    public class ReviewAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d";
        SimpleDateFormat format, format2;

        LayoutInflater inflater;

        LinearLayout.LayoutParams layoutParams;

        public ReviewAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);
            }

            ReviewListJson item = (ReviewListJson) getItem(position);

            if (item != null) {
                ImageView profile = ViewHolder.get(convertView, R.id.Profile);
                TextView name = ViewHolder.get(convertView, R.id.Name);
                TextView ratingText = ViewHolder.get(convertView, R.id.RatingText);
                TextView description = ViewHolder.get(convertView, R.id.Description);
                TextView date = ViewHolder.get(convertView, R.id.Date);
                TextView typeText = ViewHolder.get(convertView, R.id.TypeText);

                View replyView = ViewHolder.get(convertView, R.id.review_layout);
                TextView replyCount = ViewHolder.get(convertView, R.id.reply_count);

                RatingBar ratingBar = ViewHolder.get(convertView, R.id.Ratingbar);

                HorizontalScrollView horizontalScrollview = ViewHolder.get(convertView, R.id.horizontalScrollview);
                LinearLayout imageLayout = ViewHolder.get(convertView, R.id.ImageLayout);

                if(item.member_profile_image != null && !item.member_profile_image.isEmpty()) {
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.member_profile_image, profile, optionsProfile);
                } else {
                    profile.setImageResource(R.drawable.icon_empty_profile);
                }

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);

                imageLayout.removeAllViews();

                int i = 0;
                for (ReviewListJson.File file : item.file) {
                    View view = inflater.inflate(R.layout.item_review_image, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.Image);
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + file.path, imageView, options);
                    imageLayout.addView(view, layoutParams);

                    imageView.setTag(R.integer.tag_pos, position);
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
                }

                if(i>0) {
                    horizontalScrollview.setVisibility(View.VISIBLE);
                } else {
                    horizontalScrollview.setVisibility(View.GONE);
                }

                if (item.member_name == null || item.member_name.isEmpty()) {
                    name.setText("소비자 회원");
                } else {
                    name.setText(item.member_name);
                }

                ratingText.setText(item.rating + "점");
                description.setText(item.comment);
                description.setTag(position);

                description.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int i = (int) v.getTag();
                        if (((TextView) v).getLineCount() >= 10) {
                            ReviewListJson reviewListJson = (ReviewListJson) getItem(i);
                            UiDialog.showDialog(getActivity(), "리뷰", reviewListJson.comment);
                        }
                    }
                });

                try {
                    date.setText(format2.format((format.parse(item.datetime).getTime())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (item.division.equals("E")) {
                    typeText.setText(item.division_text);
                    typeText.setVisibility(View.VISIBLE);
                } else {
                    typeText.setText("");
                    typeText.setVisibility(View.GONE);
                }

                float rating = Float.parseFloat(item.rating) / 2f;
                ratingBar.setRating(rating);

                if(Integer.parseInt(item.comments_count) > 0) {
                    replyCount.setText("("+item.comments_count+") 댓글달기");
                } else {
                    replyCount.setText("댓글달기");
                }

                replyView.setTag(item.idx);
                replyView.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        String idx = (String) v.getTag();
                        ((ProductActivity)getActivity()).runReplyActivity(ReplyActivity.REPLY_TYPE_REVIEW, "구매후기 댓글", idx);
                    }
                });
            }
            return convertView;
        }
    }
}
