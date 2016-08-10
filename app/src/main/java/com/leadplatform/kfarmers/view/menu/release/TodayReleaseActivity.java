package com.leadplatform.kfarmers.view.menu.release;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class TodayReleaseActivity extends BaseFragmentActivity
{
    public static final String TAG = "TodayReleaseActivity";

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
        TodayReleaseListFragment fragment = TodayReleaseListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, TodayReleaseListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.GetReleaseTodayTitle);
        initActionBarHomeBtn();
    }
}
