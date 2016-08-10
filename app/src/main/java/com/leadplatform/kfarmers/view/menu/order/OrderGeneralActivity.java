package com.leadplatform.kfarmers.view.menu.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class OrderGeneralActivity extends BaseFragmentActivity {
    public static final String TAG = "OrderGeneralActivity";

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
        OrderGeneralListFragment fragment = OrderGeneralListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, OrderGeneralListFragment.TAG);
        ft.commit();
    }

    public void showDetailView(String orderNo)
    {
        OrderGeneralDetailFragment fragment = OrderGeneralDetailFragment.newInstance(orderNo);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, OrderGeneralDetailFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        ((TextView) findViewById(R.id.actionbar_title_text_view)).setText("주문내역");

        initActionBarHomeBtn();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }
}
