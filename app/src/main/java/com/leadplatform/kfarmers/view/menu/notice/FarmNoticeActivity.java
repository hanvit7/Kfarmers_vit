package com.leadplatform.kfarmers.view.menu.notice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmNoticeActivity extends BaseFragmentActivity
{
    public static final String TAG = "FarmNoticeActivity";

    private String userType, userIndex, noticeIndex;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        initIntentData();
        FarmNoticeListFragment fragment = FarmNoticeListFragment.newInstance(userType, userIndex, noticeIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmNoticeActivity.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.GetListNoticeTitle);
        initActionBarHomeBtn();
    }

    private void initIntentData()
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            userType = intent.getStringExtra("userType");
            userIndex = intent.getStringExtra("userIndex");
            noticeIndex = intent.getStringExtra("noticeIndex");
        }
    }
}
