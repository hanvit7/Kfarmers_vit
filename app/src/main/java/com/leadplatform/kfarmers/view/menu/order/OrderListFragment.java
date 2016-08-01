package com.leadplatform.kfarmers.view.menu.order;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.OrderItemJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class OrderListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "OrderListFragment";

    private final String limit = "20";
    private int page = 1;
    private boolean bMoreFlag = false;

    private LinearLayout mEmptyView;

    ItemAdapter mItemAdapter;

    public static OrderListFragment newInstance() {
        final OrderListFragment f = new OrderListFragment();
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
        final View v = inflater.inflate(R.layout.fragment_base_pull_list,container, false);

        mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
        ((TextView) v.findViewById(R.id.EmptyText)).setText("구매하신 상품이 없습니다.");
        ((ImageView) v.findViewById(R.id.EmptyImage)).setImageResource(R.drawable.icon_order_empty);
        v.findViewById(R.id.EmptyButton).setVisibility(View.VISIBLE);
        ((Button) v.findViewById(R.id.EmptyButton)).setText("추천상품 보기");
        ((Button) v.findViewById(R.id.EmptyButton)).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                ((OrderActivity) getActivity()).runMainActivity(MainActivity.MainTab.MARKET);
                getActivity().finish();
            }
        });

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

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
            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_order_list);
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

    public class ItemAdapter extends ArrayAdapter<OrderItemJson> {
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

            LinearLayout itemView = ViewHolder.get(convertView, R.id.ItemView);
            //TextView detailText = ViewHolder.get(convertView, R.id.ItemBtn);
            TextView dateText = ViewHolder.get(convertView, R.id.ItemDate);
            //TextView review = ViewHolder.get(convertView, R.id.reviewText);

            final OrderItemJson orderItem = getItem(position);

            dateText.setText(orderItem.datetime.substring(0,10));

            /*try {
                Date date = format2.parse(orderItem.datetime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH ,2);
                calendar.set(Calendar.HOUR_OF_DAY,0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.SECOND,0);

                if(calendarNow.after(calendar)) {
                    review.setVisibility(View.VISIBLE);
                } else {
                    review.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }*/



            /*detailText.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    ((OrderActivity) getActivity()).showDetailView(orderItem.unique);
                }
            });*/

            convertView.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    ((OrderActivity) getActivity()).showDetailView(orderItem.unique);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_ORDER_LIST, "Click_Item", null);
                }
            });

            /*review.setTag(R.integer.tag_id,position);
            review.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    int pos1 = (int) v.getTag(R.integer.tag_id);
                    Intent intent = new Intent(getActivity(), EvaluationActivity.class);
                    intent.putExtra("type",EvaluationActivity.type.review);
                    intent.putExtra("data",getItem(pos1).option);
                    startActivity(intent);
                }
            });*/


            itemView.removeAllViews();
            int pos = 0;
            int size = orderItem.option.size()-1;
            for(OrderItemJson subItem : orderItem.option)
            {
                View item = inflater.inflate(R.layout.item_order_list_sub_item, null);

                ImageView imageView = (ImageView) item.findViewById(R.id.image);
                TextView title = (TextView) item.findViewById(R.id.des);
                View line = item.findViewById(R.id.line);

                /*int count = Integer.parseInt(subItem.count) - 1;
                if(count>0) {
                    title.setText(Html.fromHtml(subItem.title + "<b>   외 " + count + "</b>"));
                }
                else
                {
                    title.setText(subItem.title);
                }*/

                title.setText(subItem.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+subItem.image1, imageView, optionsProduct);

                if(pos< size)
                {
                    line.setVisibility(View.VISIBLE);
                }
                else
                {
                    line.setVisibility(View.INVISIBLE);
                }

                itemView.addView(item);

                pos++;
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
            page = 1;
            bMoreFlag = false;
            mItemAdapter.clear();
            mItemAdapter.notifyDataSetChanged();
        }

        SnipeApiController.getOrderList(user.getUserID(),String.valueOf(page),limit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if(Code == 200)
                    {
                        JsonNode root = JsonUtil.parseTree(content).get("item");
                        List<OrderItemJson> arrayList = (List<OrderItemJson>) JsonUtil.jsonToArrayObject(root, OrderItemJson.class);
                        mItemAdapter.addAll(arrayList);

                        if (arrayList.size() == Integer.parseInt(limit))
                            bMoreFlag = true;
                        else
                            bMoreFlag = false;
                        page++;

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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }
}

