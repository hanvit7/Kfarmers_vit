package com.leadplatform.kfarmers.view.base;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ProgressBar;

public class BaseRefreshMoreGridFragment extends BaseFragment
{
    private boolean isLoadingMore = false;
    private int currentScrollState;
    private OnLoadMoreListener onLoadMoreListener;
    private ProgressBar progressBarLoadMore;
    private PullToRefreshLayout refreshLayout;
    private GridView gridview;

    public void setRefreshListView(Context context, View rootView, int layoutID, OnRefreshListener refreshListener)
    {
        refreshLayout = (PullToRefreshLayout) rootView.findViewById(layoutID);
        if (refreshLayout != null)
        {
            ActionBarPullToRefresh.from(getSherlockActivity()).allChildrenArePullable().listener(refreshListener).setup(refreshLayout);
        }
    }

    public void setMoreListView(Context context, GridView gridview, OnLoadMoreListener onLoadMoreListener)
    {
        this.gridview = gridview;
        setOnScrollListener();
        setOnLoadMoreListener(onLoadMoreListener);
    }

    private void setOnScrollListener()
    {
        gridview.setOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                currentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (onLoadMoreListener != null)
                {
                    if (visibleItemCount == totalItemCount)
                    {
                        if (progressBarLoadMore != null)
                            progressBarLoadMore.setVisibility(View.GONE);
                        return;
                    }

                    boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

                    if (!isLoadingMore && loadMore && currentScrollState != SCROLL_STATE_IDLE)
                    {
                        isLoadingMore = true;
                        if (progressBarLoadMore != null)
                            progressBarLoadMore.setVisibility(View.VISIBLE);
                        onLoadMore();
                    }
                }
            }
        });
    }

    private void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener)
    {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void onLoadMore()
    {
        if (onLoadMoreListener != null)
        {
            onLoadMoreListener.onLoadMore();
        }
    }

    public void onLoadMoreComplete()
    {
        isLoadingMore = false;
        if (progressBarLoadMore != null)
            progressBarLoadMore.setVisibility(View.GONE);
    }

    public void onRefreshComplete()
    {
        if (refreshLayout != null)
        {
            refreshLayout.setRefreshComplete();
        }
    }

    public interface OnLoadMoreListener
    {
        public void onLoadMore();
    }
}
