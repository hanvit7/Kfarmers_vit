package com.leadplatform.kfarmers.view.menu.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class OrderActivity extends BaseFragmentActivity {
    public static final String TAG = "OrderActivity";

    private String mOrderNo = "";

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        if(getIntent().getData() != null)
        {
            Uri uri = getIntent().getData();
            mOrderNo = uri.getQueryParameter("order");
        }
        else if(getIntent().hasExtra("orderNo") && null != getIntent().getStringExtra("orderNo"))
        {
            mOrderNo = getIntent().getStringExtra("orderNo");
        }

        if(null!= mOrderNo && !mOrderNo.trim().toString().isEmpty())
        {
            showListView();
            showDetailView(mOrderNo);
        }
        else
        {
            showListView();
        }
    }

    public void showListView()
    {
        OrderListFragment fragment = OrderListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, OrderListFragment.TAG);
        ft.commit();
    }

    public void showDetailView(String orderNo)
    {
        OrderDetailFragment fragment = OrderDetailFragment.newInstance(orderNo);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, OrderDetailFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        ((TextView) findViewById(R.id.title)).setText("주문내역");
        initActionBarHomeBtn();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }
}
