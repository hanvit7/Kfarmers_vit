package com.leadplatform.kfarmers.view.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;

import com.leadplatform.kfarmers.R;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class BaseRefreshMoreListFragment extends BaseListFragment {
    private boolean isTopMore = false;
    private boolean isLoadingMore = false;
    private OnLoadMoreListener onLoadMoreListener;
    private ProgressBar progressBarLoadMore;
    private PullToRefreshLayout refreshLayout;
    private List<OnScrollListener> mExtraOnScrollListenerList = new ArrayList<OnScrollListener>();

    public void setRefreshListView(Context context, View rootView, int layoutID, OnRefreshListener refreshListener) {
        refreshLayout = (PullToRefreshLayout) rootView.findViewById(layoutID);
        if (refreshLayout != null) {
            ActionBarPullToRefresh.from(getSherlockActivity()).allChildrenArePullable().listener(refreshListener).setup(refreshLayout);
        }
    }

    public void setTopMoreListView(Context context, int layoutID, OnLoadMoreListener onLoadMoreListener) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerViewLayout = mInflater.inflate(layoutID, null);
        if (footerViewLayout != null) {
            isTopMore = true;
            progressBarLoadMore = (ProgressBar) footerViewLayout.findViewById(R.id.footer_progressBar);

            if (getListView().getHeaderViewsCount() == 0) {
                getListView().addHeaderView(footerViewLayout);
            }
            setOnScrollListener();
            setOnLoadMoreListener(onLoadMoreListener);
        }
    }

    public View footerView(Context context, int layoutID) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerViewLayout = mInflater.inflate(layoutID, null);
        return footerViewLayout;
    }

    public void setMoreListView(Context context, int layoutID, OnLoadMoreListener onLoadMoreListener) {
        View footerViewLayout = footerView(context, layoutID);
        if (footerViewLayout != null) {
            isTopMore = false;
            progressBarLoadMore = (ProgressBar) footerViewLayout.findViewById(R.id.footer_progressBar);

            if (getListView().getFooterViewsCount() == 0) {
                getListView().addFooterView(footerViewLayout);
            }
            setOnScrollListener();
            setOnLoadMoreListener(onLoadMoreListener);
        }
    }

    public OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            for (AbsListView.OnScrollListener listener : mExtraOnScrollListenerList) {
                listener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            for (AbsListView.OnScrollListener listener : mExtraOnScrollListenerList) {
                listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            if (onLoadMoreListener != null) {
                if (isTopMore) {
                    if (!isLoadingMore && firstVisibleItem == 0 && totalItemCount > 1) {
                        isLoadingMore = true;
                        if (progressBarLoadMore != null)
                            progressBarLoadMore.setVisibility(View.VISIBLE);
                        onLoadMore();
                    }
                } else {
                    boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

                    if (!isLoadingMore && loadMore) {
                        isLoadingMore = true;
                        if (progressBarLoadMore != null)
                            progressBarLoadMore.setVisibility(View.VISIBLE);
                        onLoadMore();
                    }
                }
            }
        }
    };

    private void setOnScrollListener() {
        getListView().setOnScrollListener(onScrollListener);
    }

    private void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void onLoadMore() {
        if (onLoadMoreListener != null) {
            onLoadMoreListener.onLoadMore();
        }
    }

    public void onLoadMoreComplete() {
        isLoadingMore = false;
        if (progressBarLoadMore != null)
            progressBarLoadMore.setVisibility(View.GONE);
    }

    public void onRefreshComplete() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshComplete();
        }
    }

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }

    public void registerExtraOnScrollListener(AbsListView.OnScrollListener listener) {
        mExtraOnScrollListenerList.add(listener);
    }
}
