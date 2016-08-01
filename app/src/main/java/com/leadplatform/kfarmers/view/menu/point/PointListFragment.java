package com.leadplatform.kfarmers.view.menu.point;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.PointJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class PointListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "PointListFragment";

    private final String limit = "30";
    private int page = 1;
    private boolean bMoreFlag = false;

    private LinearLayout mEmptyView;
    private TextView mEmptyText;

    private ItemAdapter mItemAdapter;

    private View mViewHeader;
    //private ArcProgress mArcProgress;
    private Timer mTimer;

    private static final int DEFAULT_COUNT = 30;
    private int mTimerMax = DEFAULT_COUNT;
    private int mTimerCount = 0;
    private int mUsePoint = 0;
    private int mProgPoint = 0;

    private TextView mTextUsePoint;

    public static PointListFragment newInstance() {
        final PointListFragment f = new PointListFragment();
        final Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_POINT_LIST, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_base_pull_list,container, false);

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        mViewHeader = inflater.inflate(R.layout.item_point_header,null);

        mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
        mEmptyText = (TextView) v.findViewById(R.id.EmptyText);
        mEmptyText.setText("포인트 내역이 없습니다.");

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.bottomMargin=0;
        mEmptyView.setLayoutParams(layoutParams);

        mTextUsePoint = (TextView) mViewHeader.findViewById(R.id.TextUsePoint);
        //mArcProgress = (ArcProgress) mViewHeader.findViewById(R.id.ArcPoint);
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
            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_point_cell);
            getListView().addHeaderView(mViewHeader);
            setListAdapter(mItemAdapter);
        }
        getPointData(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if(mTimer != null)
            mTimer.cancel();*/
        mPointHandler.removeMessages(0);
    }

    public class ItemAdapter extends ArrayAdapter<PointJson> {
        ImageLoader imageLoader;
        DisplayImageOptions optionsProduct;
        LayoutInflater inflater;
        int resource;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy.MM.dd HH:mm";
        String dateFormat3 = "yyyy.MM.dd";

        SimpleDateFormat format1;
        SimpleDateFormat format2;
        SimpleDateFormat format3;

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                    .build();
            imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;

            format1 = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
            format3 = new SimpleDateFormat(dateFormat3);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            TextView txtDate = ViewHolder.get(convertView, R.id.TxtDate);
            TextView txtPoint = ViewHolder.get(convertView, R.id.TxtPoint);
            //TextView txtAction = ViewHolder.get(convertView, R.id.TxtAction);
            TextView txtExpireDate = ViewHolder.get(convertView, R.id.TxtExpireDate);
            TextView txtTitle = ViewHolder.get(convertView, R.id.TxtTitle);

            PointJson pointIttem = getItem(position);

            if(pointIttem != null) {
                try {
                    txtDate.setText(format3.format(format1.parse(pointIttem.datetime)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //txtAction.setText(pointIttem.action_text);
                txtTitle.setText(pointIttem.title);

                if(pointIttem.action.equals("S") || pointIttem.action.equals("R")) {
                    try {
                        txtExpireDate.setText(format3.format(format1.parse(pointIttem.expire_date))+ " 만료");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    txtExpireDate.setVisibility(View.VISIBLE);
                    //txtPoint.setTextColor(getResources().getColor(R.color.CommonText));
                    txtPoint.setText("+ " + CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(pointIttem.point)) + " "+pointIttem.action_text);
                } else {
                    txtExpireDate.setVisibility(View.GONE);
                    //txtPoint.setTextColor(getResources().getColor(R.color.CommonPrice));
                    txtPoint.setText("- " + CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(pointIttem.use_point)) + " "+pointIttem.action_text);
                }
            }
            return convertView;
        }
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getPointData(true);
        }
    };

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (bMoreFlag == true) {
                bMoreFlag = false;
                getPointData(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    Handler mPointHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mTimerCount<=0) {
                mTextUsePoint.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(mUsePoint)+" Point");
            } else {
                mTextUsePoint.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(mProgPoint*(mTimerMax-mTimerCount))+" Point");
                mTimerCount--;
                mPointHandler.sendEmptyMessageDelayed(0,10);
            }
        }
    };

    private void getPointData(Boolean isClear)
    {
        UserDb user = DbController.queryCurrentUser(getActivity());

        if (isClear) {
            page = 1;
            bMoreFlag = false;
            mItemAdapter.clear();
            mItemAdapter.notifyDataSetChanged();
        }

        SnipeApiController.getPointList(user.getUserID(), String.valueOf(page), limit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 200) {
                        JsonNode root = JsonUtil.parseTree(content).get("item");

                        if(root.isArray()) {
                            List<PointJson> arrayList = (List<PointJson>) JsonUtil.jsonToArrayObject(root, PointJson.class);
                            mItemAdapter.addAll(arrayList);

                            if(page == 1 && arrayList != null && arrayList.size()>0) {
                                mUsePoint = Integer.parseInt(arrayList.get(0).member_point);

                                if(mUsePoint > DEFAULT_COUNT) {
                                    mTimerCount = DEFAULT_COUNT;
                                    mTimerMax = DEFAULT_COUNT;
                                } else {
                                    mTimerCount = mUsePoint;
                                    mTimerMax = mUsePoint;
                                }

                                if(mUsePoint != 0) {
                                    mProgPoint = mUsePoint / mTimerMax;
                                }
                                mPointHandler.removeMessages(0);
                                mPointHandler.sendEmptyMessageDelayed(0,300);

                                //mArcProgress.setMax(mUsePoint);
                                //mArcProgress.setProgress(0);

                            /*mTimer = new Timer();
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(mTimerCount<=0) {
                                                if(mTimer != null) {
                                                    mTimer.cancel();
                                                    mTimer.purge();
                                                }
                                                //mTextUsePoint.setText(String.valueOf(mUsePoint));
                                                mTextUsePoint.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(mUsePoint)+"P");
                                                //mArcProgress.setProgress(mUsePoint);
                                            } else {
                                                //mArcProgress.setProgress(mProgPoint*(100-mTimerCount));
                                                //mTextUsePoint.setText(String.valueOf(mProgPoint*(100-mTimerCount))+"P");
                                                mTextUsePoint.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(mProgPoint*(100-mTimerCount))+"P");
                                                mTimerCount--;
                                            }
                                        }
                                    });
                                }
                            }, 300, 10);*/
                            }
                            page++;

                            if (arrayList.size() == Integer.parseInt(limit))
                                bMoreFlag = true;
                            else
                                bMoreFlag = false;

                            mEmptyView.setVisibility(View.GONE);
                        }

                        if(mItemAdapter.getCount() == 0) {
                            mTextUsePoint.setText("0 Point");
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
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }
}

