package com.leadplatform.kfarmers.view.menu.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.OrderGeneralItemJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class OrderGeneralListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "OrderGeneralListFragment";

    private String lastIndex = "";
    private final String limit = "20";
    private boolean bMoreFlag = false;

    private LinearLayout mEmptyView;
    private Button mAdminBtn;

    ItemAdapter mItemAdapter;

    public static OrderGeneralListFragment newInstance() {
        final OrderGeneralListFragment f = new OrderGeneralListFragment();
        final Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_ORDER_LIST, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_order_general_list,container, false);

        mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
        ((TextView) v.findViewById(R.id.EmptyText)).setText("주문 데이터가  없습니다.");
        ((ImageView) v.findViewById(R.id.EmptyImage)).setImageResource(R.drawable.icon_order_empty);
        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        mAdminBtn = (Button) v.findViewById(R.id.adminBtn);

        mAdminBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kfarmers.net/order/manager"));
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        OrderGeneralItemJson orderGeneralItemJson = (OrderGeneralItemJson) getListAdapter().getItem(position);
        ((OrderGeneralActivity)getActivity()).showDetailView(orderGeneralItemJson.unique);
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
            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_order_general_list);
            setListAdapter(mItemAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mItemAdapter.getCount() == 0)
        {
            getListOrder(true);
        }
    }

    public class ItemAdapter extends ArrayAdapter<OrderGeneralItemJson> {
        ImageLoader imageLoader;
        DisplayImageOptions optionsProduct;
        LayoutInflater inflater;
        int resource;

        String dateFormat = "yyyy.MM.dd";
        String dateFormat2 = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format;
        SimpleDateFormat format2;

        Calendar calendarNow = Calendar.getInstance();

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                    .build();
            imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;

            format = new SimpleDateFormat(dateFormat);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            LinearLayout rootview = ViewHolder.get(convertView, R.id.RootView);
            TextView date = ViewHolder.get(convertView, R.id.date);
            TextView productName = ViewHolder.get(convertView, R.id.productName);
            TextView price = ViewHolder.get(convertView, R.id.price);
            TextView status = ViewHolder.get(convertView, R.id.status);
            TextView client = ViewHolder.get(convertView, R.id.client);

            ImageView image = ViewHolder.get(convertView, R.id.productImg);

            final OrderGeneralItemJson item = getItem(position);

            if(item != null) {
                date.setText(item.datetime);

                int priceData = Integer.parseInt(item.price) * Integer.parseInt(item.cnt);
                price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(priceData) + getResources().getString(R.string.korea_won));
                status.setText(item.status_text.msg);
                client.setText(item.deposit_name + " / " +item.send_hp) ;
                productName.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, image, optionsProduct);
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
        UserDb user = DbController.queryCurrentUser(getActivity());

        if (isClear) {
            lastIndex = "";
            bMoreFlag = false;
            mItemAdapter.clear();
            mItemAdapter.notifyDataSetChanged();
        }

        SnipeApiController.getOrderGeneralList(user.getUserID(), lastIndex, limit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 200) {
                        JsonNode root = JsonUtil.parseTree(content).get("item");
                        if(root.isArray()) {
                            List<OrderGeneralItemJson> arrayList = (List<OrderGeneralItemJson>) JsonUtil.jsonToArrayObject(root, OrderGeneralItemJson.class);
                            mItemAdapter.addAll(arrayList);

                            if (arrayList.size() == Integer.parseInt(limit))
                                bMoreFlag = true;
                            else
                                bMoreFlag = false;

                            lastIndex = mItemAdapter.getItem(mItemAdapter.getCount()-1).idx;

                            if (mItemAdapter.getCount() > 0) {
                                mEmptyView.setVisibility(View.GONE);
                            } else {
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }
}

