package com.leadplatform.kfarmers.view.recipe;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class RecipeListActivity extends BaseFragmentActivity {
    public static final String TAG = "RecipeListActivity";

    private String mCode;

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        if(getIntent().getStringExtra("code") != null) {
            mCode = getIntent().getStringExtra("code");
        } else {
            finish();
        }

        RecipeListFragment fragment = RecipeListFragment.newInstance(mCode);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, RecipeListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleText = (TextView) findViewById(R.id.title);
        actionBarTitleText.setText("레시피");

        ImageButton homeBtn = (ImageButton) findViewById(R.id.homeBtn);
        homeBtn.setVisibility(View.VISIBLE);
        homeBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                finish();
            }
        });
    }
}