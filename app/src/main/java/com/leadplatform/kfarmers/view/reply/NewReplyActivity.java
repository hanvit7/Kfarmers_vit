package com.leadplatform.kfarmers.view.reply;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class NewReplyActivity extends BaseFragmentActivity {
    public static final String TAG = "NewReplyActivity";

    String title = "레시피";
    String idx = "";

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        if (getIntent() != null) {
            idx = getIntent().getStringExtra("idx");
            title = getIntent().getStringExtra("title");
        }

        NewReplyFragment fragment = NewReplyFragment.newInstance(idx);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, NewReplyFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleText = (TextView) findViewById(R.id.title);
        actionBarTitleText.setText(title);
    }
}
