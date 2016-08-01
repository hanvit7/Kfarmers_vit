package com.leadplatform.kfarmers.view.farm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmLocationActivity extends BaseFragmentActivity
{
    public static final String TAG = "FarmLocationActivity";

    private String farmLatitude, farmLongitude, farmAddress;

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
            farmLatitude = intent.getStringExtra("farmLatitude");
            farmLongitude = intent.getStringExtra("farmLongitude");
            farmAddress = intent.getStringExtra("farmAddress");
        }

        FarmLocationFragment fragment = FarmLocationFragment.newInstance(farmLatitude, farmLongitude, farmAddress);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmLocationFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleText = (TextView) findViewById(R.id.title);
        actionBarTitleText.setText(R.string.GetViewLocationTitle);
        initActionBarHomeBtn();
    }

    /***************************************************************/
    // Display
    /***************************************************************/

    /***************************************************************/
    // Method
    /***************************************************************/
}
