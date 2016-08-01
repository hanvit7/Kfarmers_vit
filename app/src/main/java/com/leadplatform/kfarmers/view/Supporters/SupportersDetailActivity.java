package com.leadplatform.kfarmers.view.Supporters;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class SupportersDetailActivity extends BaseFragmentActivity {
    public static final String TAG = "SupportersDetailActivity";

    public static final String EVENT_IDX = "EVENT_IDX";

    private String mEventIdx;

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_detail);
        ((TextView) findViewById(R.id.title)).setText("서포터즈");
        initActionBarHomeBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);
        if (getIntent() != null) {
            mEventIdx = getIntent().getStringExtra(EVENT_IDX);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        SupportersDetailFragment fragment = SupportersDetailFragment.newInstance(mEventIdx);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, SupportersDetailFragment.TAG);
        ft.commit();
    }
}

