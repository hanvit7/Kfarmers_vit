package com.leadplatform.kfarmers.view.menu.story;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class ConsumerStoryActivity extends BaseFragmentActivity
{
    public static final String TAG = "ConsumerStoryActivity";
    public static final int TYPE_CONSUMER_MAIN = 0;
    public static final int TYPE_CONSUMER_MY = 1;

    private int type = TYPE_CONSUMER_MAIN;
    private String userIndex = null;
    private String userType = null;

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
            type = intent.getIntExtra("type", TYPE_CONSUMER_MAIN);
            if (intent.hasExtra("userIndex"))
                userIndex = intent.getStringExtra("userIndex");
        }

        initUserInfo();
        if (userType != null && userType.equals("U"))
        {
            ImageButton writeDiaryBtn = (ImageButton) findViewById(R.id.writeDiaryBtn);
            writeDiaryBtn.setVisibility(View.VISIBLE);
            writeDiaryBtn.setOnClickListener(new ViewOnClickListener()
            {
                @Override
                public void viewOnClick(View v)
                {
                    runWriteDiaryActivity("U");
                }
            });
        }

        ConsumerStoryListFragment fragment = ConsumerStoryListFragment.newInstance(userIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, ConsumerStoryListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        if (type == TYPE_CONSUMER_MAIN)
            title.setText(R.string.title_consumer_story);
        else if (type == TYPE_CONSUMER_MY)
            title.setText(R.string.title_my_story);
        initActionBarHomeBtn();
    }

    private void initUserInfo()
    {
        try
        {
            String profile = DbController.queryProfileContent(this);
            if (profile != null)
            {
                JsonNode root = JsonUtil.parseTree(profile);
                ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
                userType = profileData.Type;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
