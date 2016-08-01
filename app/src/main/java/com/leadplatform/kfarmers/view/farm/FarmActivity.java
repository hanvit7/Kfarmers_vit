package com.leadplatform.kfarmers.view.farm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmActivity extends BaseFragmentActivity
{
    public static final String TAG = "FarmActivity";

    private String userType, userIndex, userCategory="";
    private TextView actionBarTitleText, actionBarFavoriteText;
    private LinearLayout actionBarFavoriteBtn;

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
            userType = intent.getStringExtra("userType");
            userIndex = intent.getStringExtra("userIndex");
            
            if(!PatternUtil.isEmpty(intent.getStringExtra("userCategory")))
            	userCategory = intent.getStringExtra("userCategory");
        }

        FarmFragment fragment = FarmFragment.newInstance(userType, userIndex,userCategory);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_farm);
        actionBarTitleText = (TextView) findViewById(R.id.title);
        actionBarFavoriteBtn = (LinearLayout) findViewById(R.id.rightFarmBtn);

        actionBarFavoriteBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                try {
                    FragmentManager fm = getSupportFragmentManager();
                    FarmFragment fragment = (FarmFragment) fm.findFragmentByTag(FarmFragment.TAG);
                    if (fragment != null) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_FarmInfo",fragment.farmData.Farm);
                        Intent intent = new Intent(FarmActivity.this, FarmIntroductionActivity.class);
                        intent.putExtra("farmType", userType);
                        intent.putExtra("farmData", JsonUtil.objectToJson(fragment.farmData));
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /*actionBarFavoriteBtn.setVisibility(View.VISIBLE);
        actionBarFavoriteBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_FARM, "Click_ActionBar-Favorite", actionBarFavoriteText.getText().toString());
                if (AppPreferences.getLogin(FarmActivity.this))
                {
                    centerSetFavorite(userType, userIndex);
                }
                else
                {
                    Intent intent = new Intent(FarmActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
        actionBarFavoriteText = (TextView) findViewById(R.id.favoriteText);*/
        initActionBarHomeBtn();
    }
    
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2)
    {
        super.onActivityResult(arg0, arg1, arg2);
    }
    
  
    /***************************************************************/
    // Display
    /***************************************************************/
    public void displayActionBarTitleText(String title)
    {
        if (actionBarTitleText != null)
            actionBarTitleText.setText(title);
    }

    public void displayActionBarFavoriteText(String count)
    {
        if (actionBarFavoriteText != null)
            actionBarFavoriteText.setText(count);
    }

    /***************************************************************/
    // Method
    /***************************************************************/
    public void centerSetFavorite(String userType, String userIndex)
    {
        CenterController.setFavorite(userType, userIndex, "I", new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.toastAddFavorite(FarmActivity.this);
                            //int count = Integer.valueOf(actionBarFavoriteText.getText().toString()) + 1;
                            //displayActionBarFavoriteText(String.valueOf(count));
                            break;

                        case 1005:
                            UiController.showDialog(FarmActivity.this, R.string.dialog_already_favorite);
                            break;
                        case 1006:
                            UiController.showDialog(FarmActivity.this, R.string.dialog_my_favorite);
                            break;
                        default:
                            UiController.showDialog(FarmActivity.this, R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(FarmActivity.this, R.string.dialog_unknown_error);
                }
            }
        });
    }
}
