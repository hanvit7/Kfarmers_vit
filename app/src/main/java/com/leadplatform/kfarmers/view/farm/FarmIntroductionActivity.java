package com.leadplatform.kfarmers.view.farm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmIntroductionActivity extends BaseFragmentActivity
{
    public static final String TAG = "IntroductionActivity";

    private String farmType, farmData;
    private TextView actionBarTitleText;

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
        Intent intent = getIntent();
        if (intent != null)
        {
            farmType = intent.getStringExtra("farmType");
            farmData = intent.getStringExtra("farmData");
        }

        FarmIntroductionFragment fragment = FarmIntroductionFragment.newInstance(farmType, farmData);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmIntroductionFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        actionBarTitleText = (TextView) findViewById(R.id.title);
        initActionBarHomeBtn();
    }

    /***************************************************************/
    // Display
    /***************************************************************/
    public void displayActionBarTitleText(String title)
    {
        if (actionBarTitleText != null)
            actionBarTitleText.setText(title);
    }

    /***************************************************************/
    // Method
    /***************************************************************/
}
