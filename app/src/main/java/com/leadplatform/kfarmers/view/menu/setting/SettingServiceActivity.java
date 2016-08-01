package com.leadplatform.kfarmers.view.menu.setting;

import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class SettingServiceActivity extends BaseFragmentActivity
{
    private static final String TAG = "SettingServiceActivity";
    

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SERVICEINFO, null);
    }
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_setting_service);
	}
    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.setting_service);
        initActionBarHomeBtn();
    }
}
