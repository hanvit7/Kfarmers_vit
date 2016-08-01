package com.leadplatform.kfarmers.view.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.EventJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.Supporters.SupportersDetailActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class SupportersTabFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "SupportersTabFragment";

    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOption;

    private final int mLimit = 20;
    private int mPage = 1;
    private boolean mMoreFlag = false;

    private LinearLayout mEmptyView;
    private TextView mEmptyText;

    private EventListAdapter mEventListAdapter;

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (mMoreFlag) {
                mMoreFlag = false;
                getDataList(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getDataList(true);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_EVENT_LIST);

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                .build();

        mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
        mEmptyText = (TextView) v.findViewById(R.id.EmptyText);

        mEmptyText.setText("현재 진행중인 서포터즈가 없습니다.");

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //getListView().setDivider(null);
        //getListView().setDividerHeight(0);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                EventJson eventJson = (EventJson) getListAdapter().getItem(position);

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_LIST, "Click_Item", eventJson.sub_title);

                Intent intent = new Intent(getActivity(), SupportersDetailActivity.class);
                intent.putExtra(SupportersDetailActivity.EVENT_IDX, eventJson.idx);
                startActivity(intent);
            }
        });

        if (mEventListAdapter == null) {
            mEventListAdapter = new EventListAdapter(getActivity(), R.layout.item_event_list);
            setListAdapter(mEventListAdapter);
        }
        getDataList(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getDataList(Boolean isClear) {
        if (isClear) {
            mMoreFlag = false;
            mPage = 1;
            mEventListAdapter.clear();
        }

        UiController.showProgressDialogFragment(getView());
        SnipeApiController.getEventList(String.valueOf(mPage), String.valueOf(mLimit), "F", new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 200) {

                        JsonNode root = JsonUtil.parseTree(content);
                        List<EventJson> arrayList = new ArrayList<>();

                        if (root.path("item").size() > 0) {
                            arrayList = (List<EventJson>) JsonUtil.jsonToArrayObject(root.path("item"), EventJson.class);
                            mEventListAdapter.addAll(arrayList);
                        }

                        mMoreFlag = arrayList.size() == mLimit;
                        mPage++;

                        if (mEventListAdapter.getCount() > 0) {
                            mEmptyView.setVisibility(View.GONE);
                        } else {
                            mEmptyView.setVisibility(View.VISIBLE);
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
                super.onFailure(statusCode, headers, content, error);
                onRefreshComplete();
                onLoadMoreComplete();
            }
        });
    }

    public class EventListAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;

        public EventListAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);
            }

            EventJson item = (EventJson) getItem(position);

            ImageView img = ViewHolder.get(convertView, R.id.Image);
            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView title = ViewHolder.get(convertView, R.id.Title);
            TextView date = ViewHolder.get(convertView, R.id.Date);
            TextView dday = ViewHolder.get(convertView, R.id.Dday);
            TextView point = ViewHolder.get(convertView, R.id.Point);


            name.setText(item.title);
            title.setText(item.sub_title);
            date.setText(item.start_date + " ~ " + item.end_date);

            if (item.status.equals("진행")) {
                dday.setText(item.duration);
            } else {
                dday.setText(item.status);
            }

            if (item.point != null && Integer.parseInt(item.point) > 0) {
                point.setText(item.point + "P 적립");
                point.setVisibility(View.VISIBLE);
            } else {
                point.setVisibility(View.GONE);
            }

            mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.file.title.path, img, mImageOption);
            return convertView;
        }
    }
}

