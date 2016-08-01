package com.leadplatform.kfarmers.view.reply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class ReplyActivity extends BaseFragmentActivity
{
    public static final int REPLY_TYPE_FARMER = 0;
    public static final int REPLY_TYPE_VILLAGE = 1;
    public static final int REPLY_TYPE_CONSUMER = 2;
    public static final int REPLY_TYPE_INTERVIEW = 3;
    public static final int REPLY_TYPE_NORMAL = 4;
    public static final int REPLY_TYPE_ADMIN = 5;
    public static final int REPLY_TYPE_REVIEW= 6;
    public static final int REPLY_TYPE_CHATTER = 7;
    //public static final int REPLY_TYPE_NOTIC_FARMERS = 6;
    //public static final int REPLY_TYPE_NOTIC_VILLAGE = 7;

    public static final String TAG = "ReplyActivity";

    private int replyType;
    private String diaryTitle, diaryIndex;

    // private TextView actionBarTitleText;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initIntentData();
        initContentView(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_REPLY);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        ReplyListFragment fragment = ReplyListFragment.newInstance(replyType, diaryTitle, diaryIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, ReplyListFragment.TAG);
        ft.commit();
    }

    private void initIntentData()
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            replyType = intent.getIntExtra("replyType", REPLY_TYPE_FARMER);
            diaryTitle = intent.getStringExtra("diaryTitle");
            diaryIndex = intent.getStringExtra("diaryIndex");
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (replyType != REPLY_TYPE_ADMIN)
            overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_bottom);
    }

    @Override
    public void initActionBar()
    {
        // getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        // getSupportActionBar().setCustomView(R.layout.view_actionbar);
        // actionBarTitleText = (TextView) findViewById(R.id.title);
    }

    public void displayActionBarTitleText(String title)
    {
        // if (actionBarTitleText != null)
        // actionBarTitleText.setText(title);
    }
}
