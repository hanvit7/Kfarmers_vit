package com.leadplatform.kfarmers.view.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.leadplatform.kfarmers.KFarmersApplication;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.beacon.BeaconHelper;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.AppState;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.gcm.GcmController;
import com.leadplatform.kfarmers.util.gcm.GcmIntentService;
import com.leadplatform.kfarmers.view.base.BaseMenuFragmentActivity;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.menu.MenuFarmerFragment;
import com.leadplatform.kfarmers.view.menu.MenuFragment;
import com.leadplatform.kfarmers.view.menu.MenuUserFragment;
import com.leadplatform.kfarmers.view.menu.MenuVillageFragment;
import com.leadplatform.kfarmers.view.actionbar.SearchActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseMenuFragmentActivity {
    public static final String TAG = "MainActivity";

    public static final String EXTRA_CURRENT_TAB = "com.leadplatform.kfarmers.current_tab";

    public enum MainTab {NONE, HOME, DIARY, MARKET, SUPPORTERS, RECIPE}

    private FragmentTabHost fragmentTabHost;
    private ImageButton writeDiaryBtn;
    private boolean backPressFlag = false;

    HashMap<String, String> pushData;

    private MainTab mMoveTab = MainTab.NONE;
    private String mProfile;

    private RelativeLayout mChatLayout;
    private TextView mChatCount;

    private IntentFilter filter;

    BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    setChatData();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppPreferences.setPushAllData(getApplicationContext(), new ArrayList<String>());
        pushData = (HashMap<String, String>) getIntent().getSerializableExtra(GcmIntentService.PUSH_BUNDLE);
        pushCheck();

        mMoveTab = (MainTab) getIntent().getSerializableExtra(EXTRA_CURRENT_TAB);

        filter = new IntentFilter();
        filter.addAction("com.leadplatform.kfarmers.view.main.chatcount");
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState) {
        Bundle argumentFarm = new Bundle();
        argumentFarm.putSerializable("Type", DiaryTabFragment.DiaryTab.FARM_NEWS);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //메인 탭 설정
        View tabViewHome = layoutInflater.inflate(R.layout.view_tab_base, null);
        ((TextView) tabViewHome.findViewById(R.id.tab_name)).setText(getString(R.string.main_tab_home));

        View tabViewDiary = layoutInflater.inflate(R.layout.view_tab_base, null);
        ((TextView) tabViewDiary.findViewById(R.id.tab_name)).setText(getString(R.string.main_tab_diary));

        View tabViewProduct = layoutInflater.inflate(R.layout.view_tab_base, null);
        ((TextView) tabViewProduct.findViewById(R.id.tab_name)).setText(getString(R.string.main_tab_market));

        View tabViewEvent = layoutInflater.inflate(R.layout.view_tab_base, null);
        ((TextView) tabViewEvent.findViewById(R.id.tab_name)).setText(getString(R.string.main_tab_supporters));

        View tabViewRecipe = layoutInflater.inflate(R.layout.view_tab_base, null);
        ((TextView) tabViewRecipe.findViewById(R.id.tab_name)).setText(getString(R.string.main_tab_recipe));

        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        fragmentTabHost.getTabWidget().setStripEnabled(false);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec(MainTab.HOME.toString()).setIndicator(tabViewHome), HomeTabFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec(MainTab.DIARY.toString()).setIndicator(tabViewDiary), DiaryTabFragment.class, argumentFarm);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec(MainTab.MARKET.toString()).setIndicator(tabViewProduct), MarketTabFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec(MainTab.SUPPORTERS.toString()).setIndicator(tabViewEvent), SupportersTabFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec(MainTab.RECIPE.toString()).setIndicator(tabViewRecipe), RecipeTabFragment.class, null);

        fragmentTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (mMoveTab != null && mMoveTab != MainTab.NONE) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_Tab", mMoveTab.toString());
                }
                mMoveTab = MainTab.NONE;
            }
        });

        setCurrentTab(mMoveTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        AppPreferences.setPushAllData(getApplicationContext(), new ArrayList<String>());
        pushData = (HashMap<String, String>) intent.getSerializableExtra(GcmIntentService.PUSH_BUNDLE);
        pushCheck();

        mMoveTab = (MainTab) intent.getSerializableExtra(EXTRA_CURRENT_TAB);

        setCurrentTab(mMoveTab);
    }

    public void pushCheck() {
        if (pushData != null && !pushData.isEmpty()) {
            if (null == pushData.get(GcmIntentService.ACTION_TYPE) || pushData.get(GcmIntentService.ACTION_TYPE).equals("push")) {
                GcmIntentService.separatePushNotice(mContext, pushData);
            } else {
                BeaconHelper.separateBeacon(mContext, pushData);
            }
            pushData = null;
        }
    }

    public void setCurrentTab(MainTab tab) {
        if (mMoveTab != null && mMoveTab != MainTab.NONE) {
            fragmentTabHost.setCurrentTabByTag(tab.toString());
        }
    }

    public void setChatData() {
        if (mChatCount == null) {
            return;
        }

        int count = DbController.queryInquiryNoReadCount(mContext);

        if (count <= 0) {
            mChatCount.setVisibility(View.GONE);
        } else {
            mChatCount.setVisibility(View.VISIBLE);
            if (count > 99) {
                mChatCount.setText("..");
            } else {
                mChatCount.setText(String.valueOf(count));
            }
        }
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_main);//액션바

        mChatLayout = (RelativeLayout) findViewById(R.id.chatLayout);
        mChatCount = (TextView) findViewById(R.id.chatCount);
        ImageButton chatBtn = (ImageButton) findViewById(R.id.chatBtn);

        chatBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (AppPreferences.getLogin(mContext)) {
                    Intent intent = new Intent(mContext, InquiryActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        setChatData();

        ImageButton leftBtn = (ImageButton) findViewById(R.id.actionbar_left_button);
        leftBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_ActionBar-Menu", null);
                String type = getUserType();
                switch (type) {
                    case "F":
                        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_MYPAGE_FARMER);
                        break;
                    case "V":
                        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_MYPAGE_VILLAGE);
                        break;
                    case "U":
                        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_MYPAGE_USER);
                        break;
                    default:
                        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_MYPAGE_NONMEMBER);
                        break;
                }
                onActionBarLeftBtnClicked();
            }
        });

        ImageButton secondBtn = (ImageButton) findViewById(R.id.secondBtn);
        secondBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_ActionBar-Search", null);
                onActionBarSecondBtnClicked();
            }
        });

        writeDiaryBtn = (ImageButton) findViewById(R.id.WriteBtn);
        writeDiaryBtn.setVisibility(View.GONE);
        writeDiaryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_ActionBar-Write", null);
                runWriteDiaryActivity(getUserType());
            }
        });
    }

    @Override
    public void initNavigationMenuSetting() {
        setSlidingActionBarEnabled(true);
        SlidingMenu sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setMode(SlidingMenu.LEFT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        sm.setBehindOffsetRes(R.dimen.navigation_menu_offset);
        sm.setShadowWidthRes(R.dimen.image_view_margin);
        sm.setShadowDrawable(R.drawable.shadow_left);
        sm.setBehindScrollScale(0.0f);
        sm.setFadeDegree(0.5f);
    }

    @Override
    public void initLeftNavigationMenu(Bundle savedInstanceState) {
        if (findViewById(R.id.left_navigation_menu_frame) == null) {
            setBehindContentView(R.layout.fragment_left_navigation_menu);
        }
    }

    @Override
    public void initRightNavigationMenu(Bundle savedInstanceState) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppPreferences.getLogin(this)) {
            try {
                mProfile = DbController.queryProfileContent(this);
                mChatLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mProfile = "";
            mChatLayout.setVisibility(View.GONE);
        }
        displaySetMenuLeftFragment();
        pushCheck();

        registerReceiver(chatReceiver, filter);

        GcmController gcmController = new GcmController();
        gcmController.checkPlayService(this);
        setChatData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(chatReceiver);
    }

    @Override
    protected void onDestroy() {
        fragmentTabHost = null;
        KFarmersApplication.appState = AppState.NONE;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!backPressFlag) {
            Toast.makeText(MainActivity.this, "뒤로 버튼을 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            backPressFlag = true;
            backPressHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_App-Exit", null);
            KFarmersApplication.appState = AppState.NONE;
            super.onBackPressed();
        }
    }

    private final Handler backPressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            backPressFlag = false;
        }
    };

    private String getUserType() {
        try {
            if (AppPreferences.getLogin(this)) {
                JsonNode root = JsonUtil.parseTree(mProfile);
                String type = root.findValue("Type").textValue();

                switch (type) {
                    case "F":
                        return "F";
                    case "V":
                        return "V";
                    case "U":
                        return "U";
                }
                return "G";
            } else {
                return "G";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "G";
        }
    }

    private void displaySetMenuLeftFragment() {
        String type = getUserType();
        //유저 종류에 따른 분류
        switch (type) {
            case "F":
                getSupportFragmentManager().beginTransaction().replace(R.id.left_navigation_menu_frame, MenuFarmerFragment.newInstance(mProfile), MenuFarmerFragment.TAG).commitAllowingStateLoss();
                break;
            case "V":
                getSupportFragmentManager().beginTransaction().replace(R.id.left_navigation_menu_frame, MenuVillageFragment.newInstance(mProfile), MenuVillageFragment.TAG).commitAllowingStateLoss();
                break;
            case "U":
                getSupportFragmentManager().beginTransaction().replace(R.id.left_navigation_menu_frame, MenuUserFragment.newInstance(mProfile), MenuUserFragment.TAG).commitAllowingStateLoss();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.left_navigation_menu_frame, MenuFragment.newInstance(), MenuFragment.TAG).commitAllowingStateLoss();
                break;
        }
    }

    private void onActionBarLeftBtnClicked() {
        showMenu();
    }

    private void onActionBarSecondBtnClicked() {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }
}
