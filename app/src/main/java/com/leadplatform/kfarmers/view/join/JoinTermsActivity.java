package com.leadplatform.kfarmers.view.join;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class JoinTermsActivity extends BaseFragmentActivity
{
    public static final String TAG = "JoinTermsActivity";

    private FragmentTabHost fragmentTabHost;

    private String page = "";

    /***************************************************************/
    // Override
    /***************************************************************/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(getIntent().hasExtra("page"))
        {
            page = getIntent().getStringExtra("page");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("page"))
        {
            page = intent.getStringExtra("page");
        }
    }

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_join_terms);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_Terms, null);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        Bundle argument1 = new Bundle();
        argument1.putString("page", "1");
        Bundle argument2 = new Bundle();
        argument2.putString("page", "2");
        Bundle argument3 = new Bundle();
        argument3.putString("page", "3");
        Bundle argument4 = new Bundle();
        argument4.putString("page", "4");
        
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View tabView1 = layoutInflater.inflate(R.layout.view_tab_term_left, null);
        View tabView2 = layoutInflater.inflate(R.layout.view_tab_term_middle, null);
        View tabView3 = layoutInflater.inflate(R.layout.view_tab_term_middle, null);
        ((TextView)tabView3.findViewById(R.id.TitleText)).setText("위치기반서비스");
        View tabView4 = layoutInflater.inflate(R.layout.view_tab_term_right, null);

        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("1").setIndicator(tabView1), JoinTermsFragment.class, argument1);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("2").setIndicator(tabView2), JoinTermsFragment.class, argument2);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("3").setIndicator(tabView3), JoinTermsFragment.class, argument3);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("4").setIndicator(tabView4), JoinTermsFragment.class, argument4);
        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        fragmentTabHost.getTabWidget().setStripEnabled(false);

        if(!page.isEmpty())
        {
            fragmentTabHost.setCurrentTabByTag(page);
        }

        fragmentTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                String name = "";
                if(tabId.equals("1"))
                {
                    name = "이용약관";
                }
                else if(tabId.equals("2"))
                {
                    name = "개인정보취급방침";
                }
                else if(tabId.equals("3"))
                {
                    name = "위치기반서비스";
                }
                else if(tabId.equals("4"))
                {
                    name = "구매약관";
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_Terms, "Click_Tab", name);
            }
        });
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.JoinTitleTerms);
        initActionBarHomeBtn();
    }
}
