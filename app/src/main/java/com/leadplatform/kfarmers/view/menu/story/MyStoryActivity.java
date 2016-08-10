package com.leadplatform.kfarmers.view.menu.story;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class MyStoryActivity extends BaseFragmentActivity
{
    public static final String TAG = "MyStoryActivity";
    public static final int TYPE_MAIN = 0;
    public static final int TYPE_MY = 1;
    
    private int type = TYPE_MAIN;
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
            type = intent.getIntExtra("type", TYPE_MAIN);
            if(intent.hasExtra("userIndex"))
                userIndex = intent.getStringExtra("userIndex");
        }

        MyStoryListFragment fragment = MyStoryListFragment.newInstance(userIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, MyStoryListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        if(type == TYPE_MAIN)
            title.setText(R.string.title_my_story);
        else if(type == TYPE_MY)
            title.setText(R.string.title_my_story);
        initActionBarHomeBtn();
    }

}
