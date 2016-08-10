package com.leadplatform.kfarmers.view.menu.release;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.parcel.ReleaseListData;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmerReleaseActivity extends BaseFragmentActivity
{
    public static final String TAG = "FarmerReleaseActivity";

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
        FarmerReleaseListFragment fragment = FarmerReleaseListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmerReleaseListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.GetListReleaseFarmerTitle);
        displayReleaseListFarmerActionBar();
        initActionBarHomeBtn();
    }

    public void displayReleaseListFarmerActionBar()
    {
        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setVisibility(View.INVISIBLE);
    }

    public void displayReleaseEditFarmerActionBar()
    {
        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_save);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                FragmentManager fm = getSupportFragmentManager();
                FarmerReleaseEditFragment fragment = (FarmerReleaseEditFragment) fm.findFragmentByTag(FarmerReleaseEditFragment.TAG);
                if (fragment != null)
                {
                    fragment.onReleaseEditBtnClicked();
                }
            }
        });
    }

    public void runReleaseEditFragment(ReleaseListData listData)
    {
        FarmerReleaseEditFragment fragment = FarmerReleaseEditFragment.newInstance(listData);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmerReleaseEditFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void finishReleaseEditFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FarmerReleaseEditFragment fragment = (FarmerReleaseEditFragment) fragmentManager.findFragmentByTag(FarmerReleaseEditFragment.TAG);
        if (fragment != null)
        {
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }
}
