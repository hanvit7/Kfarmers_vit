package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class LicenseeInfoActivity extends BaseFragmentActivity {
    public static final String TAG = "LicenseeInfoActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_COMPANY, null);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_licenseeinfo);

        findViewById(R.id.PhoneText).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                CommonUtil.AndroidUtil.actionDial(mContext, getResources().getString(R.string.setting_service_center_phone));
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_COMPANY, "Click_Tel", null);
            }
        });

        findViewById(R.id.PhoneIcon).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                CommonUtil.AndroidUtil.actionDial(mContext, getResources().getString(R.string.setting_service_center_phone));
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_COMPANY, "Click_Tel", null);
            }
        });

        findViewById(R.id.SiteBtn).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Uri uri = Uri.parse("http://ftc.go.kr/info/bizinfo/communicationView.jsp?apv_perm_no=2014393028730200148&area1=&area2=&currpage=1&searchKey=04&searchVal=1348721139");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_COMPANY, "Click_Site", null);
            }
        });

    }


    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText("사업자 정보");
        initActionBarHomeBtn();
    }
}
