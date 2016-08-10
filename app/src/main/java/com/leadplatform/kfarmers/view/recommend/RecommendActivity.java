package com.leadplatform.kfarmers.view.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class RecommendActivity extends BaseFragmentActivity
{
    public static final String TAG = "RecommendActivity";

    public static final int FRAGMENT_TYPE_IMPRESSIVE = 0;
    public static final int FRAGMENT_TYPE_FARMER = 1;
    public static final int FRAGMENT_TYPE_VILLAGE = 2;

    private int fragmentType;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initIntentData();
        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        String fragmentTag = null;
        Fragment fragment = null;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        switch (fragmentType)
        {
            case FRAGMENT_TYPE_IMPRESSIVE:
                fragmentTag = RecommendImpressiveFragment.TAG;
                fragment = RecommendImpressiveFragment.newInstance();
                break;
            case FRAGMENT_TYPE_FARMER:
                fragmentTag = RecommendFarmerFragment.TAG;
                fragment = RecommendFarmerFragment.newInstance();
                break;
            case FRAGMENT_TYPE_VILLAGE:
                fragmentTag = RecommendVillageFragment.TAG;
                fragment = RecommendVillageFragment.newInstance();
                break;
        }

        if (fragment != null)
        {
            ft.add(R.id.fragment_container, fragment, fragmentTag);
            ft.commit();
        }
    }

    private void initIntentData()
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            fragmentType = intent.getIntExtra("fragmentType", FRAGMENT_TYPE_IMPRESSIVE);
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        switch (fragmentType)
        {
            case FRAGMENT_TYPE_IMPRESSIVE:
                title.setText(R.string.title_impressive);
                break;
            case FRAGMENT_TYPE_FARMER:
                title.setText(R.string.title_farmer);
                break;
            case FRAGMENT_TYPE_VILLAGE:
                title.setText(R.string.title_village);
                break;
        }
        initActionBarHomeBtn();
    }

    /***************************************************************/
    // Display
    /***************************************************************/

    /***************************************************************/
    // Method
    /***************************************************************/
}
