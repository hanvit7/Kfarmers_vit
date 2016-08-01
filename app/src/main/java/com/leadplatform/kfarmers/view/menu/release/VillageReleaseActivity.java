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

public class VillageReleaseActivity extends BaseFragmentActivity
{
    public static final String TAG = "VillageReleaseActivity";

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
        VillageReleaseListFragment fragment = VillageReleaseListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, VillageReleaseListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.GetListReleaseVillageTitle);
        displayReleaseListVillageActionBar();
        initActionBarHomeBtn();
    }

    public void displayReleaseListVillageActionBar()
    {
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.INVISIBLE);
    }

    public void displayReleaseEditVillageActionBar()
    {
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_save);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                FragmentManager fm = getSupportFragmentManager();
                VillageReleaseEditFragment fragment = (VillageReleaseEditFragment) fm.findFragmentByTag(VillageReleaseEditFragment.TAG);
                if (fragment != null)
                {
                    fragment.onReleaseEditBtnClicked();
                }
            }
        });
    }

    public void runReleaseEditFragment(ReleaseListData listData)
    {
        VillageReleaseEditFragment fragment = VillageReleaseEditFragment.newInstance(listData);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, VillageReleaseEditFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }
    
    public void finishReleaseEditFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        VillageReleaseEditFragment fragment = (VillageReleaseEditFragment) fragmentManager.findFragmentByTag(VillageReleaseEditFragment.TAG);
        if (fragment != null)
        {
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }    
}
