package com.leadplatform.kfarmers.view.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnViewType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ExhibitionListJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.evaluation.ReviewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ProductListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "ProductListFragment";

    private String mNowType = ProductActivity.TYPE;

	private final int limit = 40;
	private String oldIndex = "";
	
	private boolean bMoreFlag = false;
	private ArrayList<ProductJson> mainItemList;
	private MainAllListAdapter mainListAdapter;

    private RelativeLayout categoryLayout;
    private TextView categoryText;

    /*private View mTopBarLayout;
    private int mTopBarHeight;*/
    private QuickReturnListViewOnScrollListener mQuicScrollListener;
    private View mTempLayout;

    private ExhibitionAdapter mExhibitionAdapter;
    private View mHeaderView;
    private AutoScrollViewPager mHeaderViewPager;

    private ImageLoader imageLoader;
    private DisplayImageOptions optionsProduct;

    //private TextView mTextTitle;


    //private LinearLayout subMenuLayout;
    //private RelativeLayout subMenuBox1, subMenuBox2;
    //private TextView subMenuText1, subMenuText2;

    private ArrayList<String> categoryList;
    private int categoryIndex = 0;

    private LinearLayout PagingLayout;

    private ArrayList<ExhibitionListJson> mPlanArrayList;

    private ImageButton mTopDiaryBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCT_LIST);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_product_list, container, false);

        if(categoryList == null) {
            categoryList = new ArrayList<>();
            Collections.addAll(categoryList, getResources().getStringArray(R.array.GetListDiarySubMenu2_1));
        }

        imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                .build();

        mHeaderView = inflater.inflate(R.layout.item_product_header, null);
        mHeaderViewPager = (AutoScrollViewPager) mHeaderView.findViewById(R.id.ViewPagerHeader);

        PagingLayout = (LinearLayout) mHeaderView.findViewById(R.id.PagingLayout);

        mTopDiaryBtn = (ImageButton) v.findViewById(R.id.topBtn);
        mTopDiaryBtn.setVisibility(View.GONE);

        mTopDiaryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getListView().smoothScrollBy(0, 0);
                getListView().setSelectionFromTop(0, 0);

            }
        });

        //mTextTitle = (TextView)  mHeaderView.findViewById(R.id.TextTitle);

		/*subMenuLayout = (LinearLayout) v.findViewById(R.id.SubMenuLayout);
        subMenuBox1 = (RelativeLayout) v.findViewById(R.id.SubMenuBox1);
        subMenuBox2 = (RelativeLayout) v.findViewById(R.id.SubMenuBox2);

        subMenuText1 = (TextView) v.findViewById(R.id.SubMenu1);
        subMenuText2 = (TextView) v.findViewById(R.id.SubMenu2);

        subMenuText1.setText("추천");
        subMenuText2.setText("일반");*/

       /* mTopBarLayout = v.findViewById(R.id.TabMenuLayout);

        mTopBarLayout.findViewById(R.id.TabBar2_3).setVisibility(View.GONE);
        mTopBarLayout.findViewById(R.id.TabMenuBox3).setVisibility(View.GONE);

        ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText1)).setText("소비자검증");
        ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText2)).setText("MD검증");

        mTopBarLayout.findViewById(R.id.TabMenuText1).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mNowType = ProductActivity.TYPE1;
                selectBtn();
                getListProduct(true, categoryIndex);
            }
        });

        mTopBarLayout.findViewById(R.id.TabMenuText2).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mNowType = ProductActivity.TYPE2;
                selectBtn();
                getListProduct(true,categoryIndex);
            }
        });*/

        categoryLayout = (RelativeLayout) v.findViewById(R.id.categoryLayout);
        categoryText = (TextView) v.findViewById(R.id.categoryText);

        //mTopBarHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonMediumByLargeRow2);
        //mTempLayout = new View(getActivity());
        //mTempLayout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,mTopBarHeight));

        //categoryText.setText(categoryList.get(categoryIndex));

        categoryLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                /*onCategoryBtnClicked(categoryIndex, new CategoryDialogFragment.OnCloseCategoryDialogListener() {
                    @Override
                    public void onDialogListSelection(int subMenuType, int position) {
                        if (categoryIndex != position) {
                            categoryIndex = position;
                            categoryText.setText(categoryList.get(position));
                            getListProduct(true, categoryIndex);
                            KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_LIST, "Click_Category", categoryList.get(position));
                        }
                    }
                });*/

                Intent intent = new Intent(getActivity(), ReviewActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_in_from_now);
            }
        });
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
        //selectBtn();

        mHeaderViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                /*try {
                    String text = mExhibitionAdapter.items.get(mHeaderViewPager.getCurrentItem()).title;
                    while(text.startsWith("\n"))
                    {
                        text = text.replaceFirst("\n","");
                    }
                    mTextTitle.setText(text);
                } catch (Exception e) {}*/
            }

            @Override
            public void onPageScrollStateChanged(int position) {
                displayViewPagerIndicator(mHeaderViewPager.getCurrentItem());
            }
        });
        return v;
	}

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        oldIndex = "";
        bMoreFlag = false;

        mPlanArrayList = null;
        mExhibitionAdapter = null;

        mainItemList = new ArrayList<>();
        mainListAdapter = new MainAllListAdapter();


        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        if(getListView().getAdapter() == null)
        {
            getListView().addHeaderView(mHeaderView);
        }
        else
        {
            if(getListView().getHeaderViewsCount()==0)
            {
                getListView().setAdapter(null);
                getListView().addHeaderView(mHeaderView);
            }
        }

        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        int bottomHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.CommonLargeRow);
        mQuicScrollListener = new QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                /*.header(mTopBarLayout)
                .minHeaderTranslation(-mTopBarHeight)*/
                .footer(categoryLayout)
                .minFooterTranslation(bottomHeight)
                .isSnappable(true)
                .build();
        mQuicScrollListener.registerExtraOnScrollListener(onScrollListener);

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem>1) {
                    mTopDiaryBtn.setVisibility(View.VISIBLE);
                } else {
                    mTopDiaryBtn.setVisibility(View.GONE);
                }
            }
        };
        mQuicScrollListener.registerExtraOnScrollListener(onScrollListener);

        getListView().setOnScrollListener(mQuicScrollListener);

        setListAdapter(mainListAdapter);

        getListProductExhibition();

		/*if(getListView().getAdapter() == null)
        {
            getListView().addHeaderView(mTempLayout);
        }
        else
        {
            if(getListView().getHeaderViewsCount()==0)
            {
                getListView().setAdapter(null);
                getListView().addHeaderView(mTempLayout);
            }
        }*/
	}

    /*private void selectBtn()
    {
        CenterController.cancel(getActivity(), true);

        switch (mNowType)
        {
            case ProductActivity.TYPE1:
            {
                ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText1)).setTypeface(null, Typeface.BOLD);
                ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText2)).setTypeface(null, Typeface.NORMAL);
                break;
            }
            case ProductActivity.TYPE2:
            {
                ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText1)).setTypeface(null, Typeface.NORMAL);
                ((TextView)mTopBarLayout.findViewById(R.id.TabMenuText2)).setTypeface(null, Typeface.BOLD);
                break;
            }
        }
    }*/

    /*@Override
    public void onPause() {
        super.onPause();
        mHeaderViewPager.stopAutoScroll();
    }

    @Override
    public void onResume() {

        if(mExhibitionAdapter != null && mExhibitionAdapter.getCount()>1)
        {
            mHeaderViewPager.startAutoScroll();
        }
        super.onResume();
    }*/

    private void displayViewPagerIndicator(int position) {
        PagingLayout.removeAllViews();

        if(mExhibitionAdapter.getCount()>1) {
            for (int i = 0; i < mExhibitionAdapter.getCount(); i++) {
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
        }
    }

    private void getListProductExhibition() {
        SnipeApiController.getProductExhibition(new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                mPlanArrayList = (ArrayList<ExhibitionListJson>) JsonUtil.jsonToArrayObject(root.path("item"), ExhibitionListJson.class);

                                /*if(root.path("item").size() > 1) {
                                    mHeaderViewPager.setInterval(3000);
                                    mHeaderViewPager.startAutoScroll();
                                }*/
                                mHeaderView.setVisibility(View.VISIBLE);
                            } else {
                                getListView().removeHeaderView(mHeaderView);
                                mHeaderView.setVisibility(View.GONE);
                            }
                            getListProduct(true, categoryIndex);
                            break;
                        default:
                            getListView().removeHeaderView(mHeaderView);
                            mHeaderView.setVisibility(View.GONE);
                            getListProduct(true, categoryIndex);
                            break;
                    }
                } catch (Exception e) {
                    getListView().removeHeaderView(mHeaderView);
                    mHeaderView.setVisibility(View.GONE);
                    getListProduct(true, categoryIndex);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                getListView().removeHeaderView(mHeaderView);
                mHeaderView.setVisibility(View.GONE);
                getListProduct(true, categoryIndex);
            }
        });
    }

	private void getListProduct(Boolean isClear,int categoryNo) {

		if (isClear) {
			oldIndex = "";
			bMoreFlag = false;
            mainItemList = new ArrayList<>();
			mainListAdapter.notifyDataSetChanged();
		}

        //UiController.showProgressDialogFragment(getView());
        SnipeApiController.getProductList(String.valueOf(limit),oldIndex,String.valueOf(categoryNo),"","",false,null,mNowType,new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            List<ProductJson> arrayList = new ArrayList<ProductJson>();

                            if(root.path("item").size() > 0)
                            {
                                arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
                                mainItemList.addAll(arrayList);

                                oldIndex = mainItemList.get(mainItemList.size() - 1).idx;
                            }

                            if (arrayList.size() == limit)
                                bMoreFlag = true;
                            else
                                bMoreFlag = false;


                            if(mExhibitionAdapter == null && mPlanArrayList != null && mPlanArrayList.size() > 0) {
                                mExhibitionAdapter = new ExhibitionAdapter(getActivity(),mPlanArrayList);
                                mHeaderViewPager.setAdapter(mExhibitionAdapter);
                                displayViewPagerIndicator(0);
                                mHeaderView.setVisibility(View.VISIBLE);
                            }

                            mainListAdapter.notifyDataSetChanged();

                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                   // UiController.hideProgressDialogFragment(getView());
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                   // UiController.hideProgressDialogFragment(getView());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
               // UiController.hideProgressDialogFragment(getView());
                super.onFailure(statusCode, headers, content, error);
            }
        });
	}

    private class MainAllListAdapter extends BaseAdapter {
		private DisplayImageOptions optionsProfile;

        public MainAllListAdapter()
        {
            this.optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                    .showImageOnLoading(R.drawable.icon_empty_profile).build();
        }

        @Override
        public int getCount() {
            return mainItemList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mainItemList.get(position);
        }

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item_product_list, null);
			}

            LinearLayout root = ViewHolder.get(convertView,R.id.root);
			ImageView img = ViewHolder.get(convertView, R.id.image);
            ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);
            TextView des = ViewHolder.get(convertView, R.id.des);
            TextView price = ViewHolder.get(convertView, R.id.price);
            TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
            TextView summary = ViewHolder.get(convertView, R.id.summary);
            TextView dDay = ViewHolder.get(convertView, R.id.textDday);

            final ProductJson item = (ProductJson) getItem(position);
            if (item != null)
            {
                if(item.profile_image != null && !item.profile_image.isEmpty()) {
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.profile_image, img_profile, optionsProfile);
                }
                else {
                    img_profile.setImageResource(0);
                }
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, optionsProduct);

                if(item.summary != null && !item.summary.isEmpty())
                {
                    summary.setVisibility(View.VISIBLE);
                    summary.setText(item.summary);
                }
                else
                {
                    summary.setVisibility(View.GONE);
                }

                //des.setText(item.name);
                des.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                int itemPrice = (int)Double.parseDouble(item.price);
                int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

                if(itemPrice > itemBuyPrice)
                {
                    price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
                    dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                    price.setVisibility(View.VISIBLE);

                    price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    price.setText("");
                    dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                    price.setVisibility(View.GONE);
                    price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                }

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_LIST, "Click_Item", item.name);
                        Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
                        intent.putExtra("productIndex", item.idx);
                        startActivity(intent);
                    }
                });

                if(item.duration != null && item.duration.startsWith("D") && !item.duration.equals("D-day")) {
                    dDay.setText(item.duration);
                    dDay.setVisibility(View.VISIBLE);
                } else {
                    dDay.setVisibility(View.GONE);
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
				getListProduct(false,categoryIndex);
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getListProduct(true,categoryIndex);
		}
	};

    public void onCategoryBtnClicked(int categoryIndex , CategoryDialogFragment.OnCloseCategoryDialogListener onCloseCategoryDialogListener) {
        CategoryDialogFragment fragment = CategoryDialogFragment.newInstance(0, categoryIndex, categoryList.toArray(new String[categoryList.size()]), "");
        fragment.setOnCloseCategoryDialogListener(onCloseCategoryDialogListener);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(fragment,CategoryDialogFragment.TAG);
        ft.commitAllowingStateLoss();
        //fragment.show(ft, CategoryDialogFragment.TAG);
    }

    public class ExhibitionAdapter extends PagerAdapter {

        private Context context;
        private ArrayList<ExhibitionListJson> items;

        public ExhibitionAdapter(Context context, ArrayList<ExhibitionListJson> items) {
            this.context = context;
            this.items = items;
        }

        public void addAll(ArrayList<ExhibitionListJson> items) {
            this.items = items;
        }

        public void add(ExhibitionListJson item) {
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
            final View v = inflater.inflate(R.layout.view_product_plan, container, false);

            ImageView image = (ImageView) v.findViewById(R.id.imageView);

            ExhibitionListJson item = items.get(position);

            imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.picture, image, optionsProduct);

            image.setTag(R.integer.tag_id,item.idx);
            image.setTag(R.integer.tag_st,item.title);

            image.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {

                    String id  = (String) v.getTag(R.integer.tag_id);
                    String name  = (String) v.getTag(R.integer.tag_st);
                    Intent intent = new Intent(getSherlockActivity(), ShopActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("type",ShopActivity.type.Plan);
                    startActivity(intent);
                }
            });

            container.addView(v, container.getChildCount() > position ? position: container.getChildCount());
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
