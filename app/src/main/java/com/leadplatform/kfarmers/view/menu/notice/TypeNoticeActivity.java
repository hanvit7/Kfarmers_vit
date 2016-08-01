package com.leadplatform.kfarmers.view.menu.notice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class TypeNoticeActivity extends BaseFragmentActivity
{
    public static final String TAG = "TypeNoticeActivity";

    public String noticeType,noticeIndex;

    /***************************************************************/
    // Override

    /***************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        initIntentData();
        listFragment();

        if(noticeIndex != null && !noticeIndex.isEmpty()) {
            detailFragment();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.MenuFarmerBoard);
        initActionBarHomeBtn();
    }

    private void initIntentData()
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            noticeType = intent.getStringExtra("noticeType");
            noticeIndex = intent.getStringExtra("noticeIndex");
        }
    }

    public void listFragment()
    {
        TypeNoticeListFragment fragment = TypeNoticeListFragment.newInstance(noticeType, noticeIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setBreadCrumbTitle(TypeNoticeListFragment.TAG);
        ft.add(R.id.fragment_container, fragment, TypeNoticeListFragment.TAG);
        ft.commit();
    }

    public void detailFragment()
    {
        TypeNoticeDetailFragment fragment = TypeNoticeDetailFragment.newInstance(noticeIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setBreadCrumbTitle(TypeNoticeDetailFragment.TAG);
        ft.add(R.id.fragment_container, fragment, TypeNoticeDetailFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void repleActivity(String title)
    {
        /*if (noticeType.equals("F")) {
            runReplyActivity(ReplyActivity.REPLY_TYPE_NOTIC_FARMERS, title, noticeIndex);
        } else if (noticeType.equals("V")) {
            runReplyActivity(ReplyActivity.REPLY_TYPE_NOTIC_VILLAGE, title, noticeIndex);
        }*/
    }
}
