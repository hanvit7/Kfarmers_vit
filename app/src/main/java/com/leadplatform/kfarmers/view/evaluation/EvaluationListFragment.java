package com.leadplatform.kfarmers.view.evaluation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.snipe.EvaluationListJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class EvaluationListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "EvaluationListFragment";

    private final String mLimit = "20";
    private  int mPage = 1;
    private boolean mMoreFlag = false;

    private ListAdapter mListAdapter;

    private LayoutInflater mInflater;

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (mMoreFlag == true) {
                mMoreFlag = false;
                getData(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getData(true);
        }
    };

    public static EvaluationListFragment newInstance() {
        final EvaluationListFragment f = new EvaluationListFragment();
        final Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_evaluation_list, container, false);
        mInflater = inflater;
        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);



        (v.findViewById(R.id.btnView)).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                final ProfileJson profileJson = getUserProfile();

                if (profileJson != null) {
                    SnipeApiController.getChatManager(new SnipeResponseListener(getActivity()) {
                        @Override
                        public void onSuccess(int Code, String content, String error) {
                            try {
                                JsonNode root = JsonUtil.parseTree(content);
                                if (profileJson.ID.equals(root.get("manager").textValue())) {
                                    return;
                                }
                                SnipeApiController.checkChatRoom(profileJson.ID, root.get("manager").textValue(), new SnipeResponseListener(getActivity()) {
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        if (mListAdapter  == null) {
            mListAdapter = new ListAdapter(getActivity(), R.layout.item_evaluation_place);

            View view = mInflater.inflate(R.layout.item_evaluation_header,null);
            getListView().addHeaderView(view);
            getListView().setAdapter(mListAdapter);
            getData(true);
        }
    }

    private ProfileJson getUserProfile() {

        if(!AppPreferences.getLogin(getActivity())) {
            return null;
        }
        try {
            String profile = DbController.queryProfileContent(getSherlockActivity());
            JsonNode root = JsonUtil.parseTree(profile);
            return (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getData(boolean isClear) {

        if (isClear) {
            mPage = 1;
            mMoreFlag = false;
            mListAdapter.clear();
        }

        SnipeApiController.getEvaluationLIst(String.valueOf(mPage), mLimit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                ArrayList<EvaluationListJson> evaluationList = (ArrayList<EvaluationListJson>) JsonUtil.jsonToArrayObject(root.path("item"), EvaluationListJson.class);
                                mListAdapter.addAll(evaluationList);

                                if (evaluationList.size() == Integer.parseInt(mLimit)) {
                                    mPage++;
                                    mMoreFlag = true;
                                } else {
                                    mMoreFlag = false;
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

    public class ListAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;

        public ListAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(itemLayoutResourceId, null);
            }

            EvaluationListJson item = (EvaluationListJson) getItem(position);

            if(item != null) {

                TextView name = ViewHolder.get(convertView, R.id.Name);
                TextView place = ViewHolder.get(convertView, R.id.Place);
                TextView placeText = ViewHolder.get(convertView, R.id.PlaceText);
                LinearLayout layout = ViewHolder.get(convertView, R.id.ListLayout);

                final android.support.v4.view.ViewPager pager = ViewHolder.get(convertView, R.id.Viewpager);
                final LinearLayout pagingLayout = ViewHolder.get(convertView, R.id.PagingLayout);

                name.setText(item.place);
                place.setText(item.location);
                placeText.setText(item.explain);

                if(layout.getChildCount() == 0) {

                    int pos = 1;

                    for (EvaluationListJson.Item item1 : item.item) {

                        int hol = pos % 2;

                        View subItem = mInflater.inflate(R.layout.item_evaluation_place_item, null);

                        if(hol == 0) {
                            subItem.setBackgroundColor(Color.parseColor("#ffffff"));
                        } else {
                            subItem.setBackgroundColor(Color.parseColor("#f4f4f4"));
                        }
                        pos++;

                        subItem.setTag(R.integer.tag_st, item1.status);
                        subItem.setTag(R.integer.tag_id, item1.place_idx);
                        subItem.setTag(R.integer.tag_pos, item1.idx);

                        TextView state = ViewHolder.get(subItem, R.id.State);
                        TextView des = ViewHolder.get(subItem, R.id.Des);

                        state.setText(item1.status_text);
                        des.setText(item1.subject);

                        if(item1.status.equals("T"))
                        {
                            state.setBackgroundColor(Color.parseColor("#ff8686"));
                        } else if(item1.status.equals("S")) {
                            state.setBackgroundColor(Color.parseColor("#3B5998"));
                        } else if(item1.status.equals("F")){
                            state.setBackgroundColor(Color.parseColor("#8C8C8C"));
                        }

                        subItem.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {

                                final String st = (String) v.getTag(R.integer.tag_st);
                                final String place = (String) v.getTag(R.integer.tag_id);
                                final String item = (String) v.getTag(R.integer.tag_pos);

                                if(st.equals("S"))
                                {
                                    UiController.showDialog(getSherlockActivity(), "소비자 집단 검증 진행 전입니다.");
                                    return;
                                } else if(st.equals("F"))
                                {
                                    ((EvaluationActivity)getActivity()).fragmentResult(place,item);
                                    return;
                                }

                                if (!AppPreferences.getLogin(getActivity())) {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    startActivity(intent);
                                    return;
                                }

                                final View codView = mInflater.inflate(R.layout.item_evaluation_code, null);
                                UiDialog.showDialog(getActivity(), "인증코드", codView, R.string.dialog_ok, new CustomDialogListener() {
                                    @Override
                                    public void onDialog(int type) {

                                        final EditText editText = (EditText) codView.findViewById(R.id.Code);

                                        if (editText.getText().toString().trim().length() <= 0) {
                                            UiController.showDialog(getActivity(),"인증코드를 입력해주세요.");
                                            return;
                                        }

                                        SnipeApiController.getEvaluationValid(place, item, editText.getText().toString().trim(), new SnipeResponseListener(getActivity()) {
                                            @Override
                                            public void onSuccess(int Code, String content, String error) {
                                                try {
                                                    switch (Code) {
                                                        case 200:
                                                            ((EvaluationActivity) getActivity()).fragmentDetail(place,item);
                                                            break;
                                                        case 401:
                                                            //예정,종료
                                                            UiController.showDialog(getSherlockActivity(), R.string.dialog_evaluation_valid_error);
                                                            break;
                                                        case 402:
                                                            //비밀번호
                                                            UiController.showDialog(getSherlockActivity(), R.string.dialog_evaluation_valid_no);
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
                                });
                            }
                        });
                        layout.addView(subItem);
                    }
                }

                if(pager.getAdapter() == null)
                {
                    ArrayList<String> image = new ArrayList<>();

                    for(EvaluationListJson.File file : item.file)
                    {
                        image.add(Constants.KFARMERS_SNIPE_IMG + file.path);
                    }
                    final ViewPagerAdapter viewePagerAdapter = new ViewPagerAdapter(getActivity(),image, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
                    pager.setAdapter(viewePagerAdapter);

                    pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                        @Override
                        public void onPageSelected(int position) {}

                        @Override
                        public void onPageScrollStateChanged(int state) {
                            displayViewPagerIndicator(pagingLayout, viewePagerAdapter,pager.getCurrentItem());
                        }
                    });
                    displayViewPagerIndicator(pagingLayout,viewePagerAdapter,pager.getCurrentItem());
                }


            }
            return convertView;
        }

        private void displayViewPagerIndicator(LinearLayout pagingLayout,ViewPagerAdapter viewePagerAdapter,int position) {

            pagingLayout.removeAllViews();

            for (int i = 0; i < viewePagerAdapter.getCount(); i++) {
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

                pagingLayout.addView(dot);
            }
        }

        public class ViewPagerAdapter extends PagerAdapter {

            private Context context;
            private ArrayList<String> items;

            private ImageLoader imageLoader;
            private DisplayImageOptions mOptionsProduct;


            public ViewPagerAdapter(Context context,ArrayList<String> items, ImageLoader imageLoader) {
                this.context = context;
                this.imageLoader = imageLoader;
                this.items = items;
                mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).displayer(new FadeInBitmapDisplayer(1000, true, true, true))
                        .build();
            }

            public void addAll(ArrayList<String> items)
            {
                this.items = items;
            }

            public void add(String item)
            {
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
                final View v = inflater.inflate(R.layout.view_home_impressive_pager,container, false);

                ImageView image = (ImageView) v.findViewById(R.id.imageView);
                String item = items.get(position);
                imageLoader.displayImage(item, image,mOptionsProduct);

                image.setTag(position);
                image.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int i = (int) v.getTag();
                        Intent intent = new Intent(context, ImageViewActivity.class);
                        intent.putExtra("pos", i);
                        intent.putStringArrayListExtra("imageArrary", items);
                        context.startActivity(intent);
                    }
                });
                container.addView(v, container.getChildCount() > position ? position : container.getChildCount());
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

}