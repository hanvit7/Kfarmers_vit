package com.leadplatform.kfarmers.view.menu.point;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class PointActivity extends BaseFragmentActivity {
    public static final String TAG = "PointActivity";

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);
        showListView();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        ((TextView) findViewById(R.id.actionbar_title_text_view)).setText("포인트");
        initActionBarHomeBtn();
    }

    public void showListView()
    {
        PointListFragment fragment = PointListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, PointListFragment.TAG);
        ft.commit();
    }
}
