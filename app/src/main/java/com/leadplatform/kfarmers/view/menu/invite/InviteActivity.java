package com.leadplatform.kfarmers.view.menu.invite;

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

public class InviteActivity extends BaseFragmentActivity {

    private FragmentTabHost fragmentTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_INVITE, null);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {

        setContentView(R.layout.activity_invite);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View tabView1 = layoutInflater.inflate(R.layout.view_tab_base, null);
        View tabView2 = layoutInflater.inflate(R.layout.view_tab_base, null);
        View tabView3 = layoutInflater.inflate(R.layout.view_tab_base, null);

        ((TextView)tabView1.findViewById(R.id.tab_name)).setText("카카오톡");
        ((TextView)tabView2.findViewById(R.id.tab_name)).setText("네이버 밴드");
        ((TextView)tabView3.findViewById(R.id.tab_name)).setText("연락처");


        Bundle argumentFarm = new Bundle();

        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        argumentFarm = new Bundle();
        argumentFarm.putInt("Type", InviteSnsFragment.TYPE_KAKAO);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("INVITE_TYPE_KAKAO").setIndicator(tabView1), InviteSnsFragment.class, argumentFarm);

        argumentFarm = new Bundle();
        argumentFarm.putInt("Type", InviteSnsFragment.TYPE_BAND);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("INVITE_TYPE_BAND").setIndicator(tabView2), InviteSnsFragment.class, argumentFarm);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("INVITE_TYPE_PHONE").setIndicator(tabView3), InvitePhoneFragment.class, null);

        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        fragmentTabHost.getTabWidget().setStripEnabled(false);

        fragmentTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                String name = "";
                if(tabId.equals("INVITE_TYPE_KAKAO"))
                {
                    name = "카카오톡";
                }else if(tabId.equals("INVITE_TYPE_BAND"))
                {
                    name = "밴드";
                }
                else
                {
                    name = "연락처";
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_INVITE, "Click_Tab", name);
            }
        });
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleText = (TextView) findViewById(R.id.title);
        actionBarTitleText.setText(R.string.setting_invite);
        initActionBarHomeBtn();
    }
}
