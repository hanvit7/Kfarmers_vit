package com.leadplatform.kfarmers.view.menu.story;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class InterviewStoryActivity extends BaseFragmentActivity
{
    public static final String TAG = "InterviewStoryActivity";
    public static final int TYPE_INTERVIEW_MAIN = 0;
    public static final int TYPE_INTERVIEW_MY = 1;

    private int type = TYPE_INTERVIEW_MAIN;
    private String userIndex = null;

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
            type = intent.getIntExtra("type", TYPE_INTERVIEW_MAIN);
            if (intent.hasExtra("userIndex"))
                userIndex = intent.getStringExtra("userIndex");
        }

        InterviewStoryListFragment fragment = InterviewStoryListFragment.newInstance(userIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, InterviewStoryListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        if (type == TYPE_INTERVIEW_MAIN)
            title.setText(R.string.title_interview_story);
        else if (type == TYPE_INTERVIEW_MY)
            title.setText(R.string.title_my_story);
        initActionBarHomeBtn();
    }

}