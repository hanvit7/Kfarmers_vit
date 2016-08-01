package com.leadplatform.kfarmers.view.inquiry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.InquiryDb;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.InquiryJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class InquiryListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "InquiryListFragment";

    private UserDb mUser;

    private final String mLimit = "999";
    private int mPage = 1;
    private boolean bMoreFlag = false;

    private LinearLayout mEmptyView;

    ItemAdapter mItemAdapter;

    private Button mInquiryBtn;

    public static InquiryListFragment newInstance() {
        final InquiryListFragment f = new InquiryListFragment();
        final Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUser = DbController.queryCurrentUser(getActivity());

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_INQUIRY_LIST, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_inquiry_list,container, false);

        mInquiryBtn = (Button) v.findViewById(R.id.InquiryBtn);
        mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
        ((TextView) v.findViewById(R.id.EmptyText)).setText("문의 리스트가 없습니다.");
        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        mInquiryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                SnipeApiController.getChatManager(new SnipeResponseListener(getActivity()) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        try {
                            JsonNode root = JsonUtil.parseTree(content);
                            if (mUser.getUserID().equals(root.get("manager").textValue())) {
                                return;
                            }
                            SnipeApiController.checkChatRoom(mUser.getUserID(), root.get("manager").textValue(), new SnipeResponseListener(getActivity()) {
                                @Override
                                public void onSuccess(int Code, String content, String error) {
                                    try {
                                        switch (Code) {
                                            case 200:
                                                ((InquiryActivity) getActivity()).fragmentChat(content);
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
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
        if (mItemAdapter == null) {
            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_inquiry_list);
            setListAdapter(mItemAdapter);
            getListOrder(false);

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((InquiryActivity)getActivity()).fragmentChat(mItemAdapter.getItem(position).idx);
                }
            });
        }
    }

    public void onForceUpdate()
    {
        if(mItemAdapter != null)
        {
            int start = getListView().getFirstVisiblePosition();
            int last = getListView().getLastVisiblePosition();

            for(;start < last ; start++)
            {
                InquiryJson inquiryJson = mItemAdapter.getItem(start);

                InquiryDb inquiryDb = DbController.queryInquiry(getActivity(), inquiryJson.idx);
                if(inquiryDb != null && inquiryDb.read.equals("F"))
                {
                    mItemAdapter.getItem(start).read = "F";
                }
                else
                {
                    mItemAdapter.getItem(start).read = "T";
                }
            }
            mItemAdapter.notifyDataSetChanged();
        }
    }

    public class ItemAdapter extends ArrayAdapter<InquiryJson> {
        ImageLoader imageLoader;
        DisplayImageOptions optionsProduct;
        LayoutInflater inflater;
        int resource;
        DisplayImageOptions mOptionsProfile;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d\na h:mm";

        SimpleDateFormat format,format2;

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                    .showImageOnLoading(R.drawable.common_dummy).build();
            imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
            mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                    .showImageOnLoading(R.drawable.icon_empty_profile).showImageOnFail(R.drawable.icon_empty_profile).build();

            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            InquiryJson item = getItem(position);

            ImageView profile = ViewHolder.get(convertView, R.id.Profile);
            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView des = ViewHolder.get(convertView, R.id.Des);
            TextView date = ViewHolder.get(convertView, R.id.Date);
            //ImageView newIcon = ViewHolder.get(convertView, R.id.New);


            if(item.profile_images != null && !item.profile_images.isEmpty()) {
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + (item.profile_images.split(":")[0]), profile, mOptionsProfile);
            } else {
                profile.setImageResource(R.drawable.icon_empty_profile);
            }

            if(item.members != null && !item.members.isEmpty())
                name.setText(item.members.split(":")[0]+"  ");
            des.setText(item.message);

            try {
                String str = format2.format((format.parse(item.updatetime).getTime()));
                date.setText(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            InquiryDb inquiryDb = DbController.queryInquiry(getActivity(), item.idx);

            if(inquiryDb != null && inquiryDb.read.equals("F"))
            {
                //newIcon.setVisibility(View.VISIBLE);
                Drawable draw = getResources().getDrawable(R.drawable.icon_menu_new);
                name.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
            }
            else
            {
                //newIcon.setVisibility(View.GONE);
                name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            return convertView;
        }
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getListOrder(true);
        }
    };

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (bMoreFlag == true) {
                bMoreFlag = false;
                getListOrder(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private void getListOrder(Boolean isClear)
    {
        if (isClear) {
            mPage = 1;
            bMoreFlag = false;
            mItemAdapter.clear();
            mItemAdapter.notifyDataSetChanged();
        }

        SnipeApiController.getChatList(mUser.getUserID(), mLimit, String.valueOf(mPage), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if(Code == 200)
                    {
                        if(JsonUtil.parseTree(content).get("item").isArray()) {
                            JsonNode root = JsonUtil.parseTree(content).get("item");
                            List<InquiryJson> arrayList = (List<InquiryJson>) JsonUtil.jsonToArrayObject(root, InquiryJson.class);
                            mItemAdapter.addAll(arrayList);

                            if (arrayList.size() == Integer.parseInt(mLimit))
                                bMoreFlag = true;
                            else
                                bMoreFlag = false;

                            mPage+=1;
                        }

                        if(mItemAdapter.getCount()>0)
                        {
                            mEmptyView.setVisibility(View.GONE);
                        }
                        else
                        {
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }
}

