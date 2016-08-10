package com.leadplatform.kfarmers.view.diary;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class StoryListActivity extends BaseFragmentActivity {
    public static final String TAG = "StoryListActivity";

    private String mKeyword;
    private String mImpressive;

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);

        if(mKeyword != null && !mKeyword.isEmpty()) {
            ((TextView) findViewById(R.id.actionbar_title_text_view)).setText(mKeyword);
        } else {
            ((TextView) findViewById(R.id.actionbar_title_text_view)).setText("밥상수다");
        }
        initActionBarHomeBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);
        if (getIntent() != null) {
            mKeyword = getIntent().getStringExtra("keyword");
            mImpressive = getIntent().getStringExtra("impressive");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        StoryListFragment fragment = StoryListFragment.newInstance(mKeyword,mImpressive);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, StoryListFragment.TAG);
        ft.commit();
    }
}

