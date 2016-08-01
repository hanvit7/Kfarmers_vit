package com.leadplatform.kfarmers.view.evaluation;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class ReviewActivity extends BaseFragmentActivity {
    public static final String TAG = "ReviewActivity";

    public String userId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        if(getIntent() != null && getIntent().hasExtra("id"))
            userId = getIntent().getStringExtra("id");
        fragmentList();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView mActionBarTitle = (TextView) findViewById(R.id.title);
        if(userId.equals("")) {
            mActionBarTitle.setText(getString(R.string.review_realtime_title));
        } else {
            mActionBarTitle.setText(getString(R.string.review_title));
        }
        initActionBarHomeBtn();
    }

    public void fragmentList() {
        ReviewListFragment fragment = ReviewListFragment.newInstance(userId);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, ReviewListFragment.TAG);
        ft.commit();
    }

    public void fragmentDetail(String place, String item) {
        EvaluationDetailFragment fragment = EvaluationDetailFragment.newInstance(place,item);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, EvaluationDetailFragment.TAG);
        ft.addToBackStack(EvaluationDetailFragment.TAG);
        ft.commit();
    }
}
