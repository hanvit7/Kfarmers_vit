package com.leadplatform.kfarmers.view.menu.favorite;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FavoriteActivity extends BaseFragmentActivity
{
    public static final String TAG = "FavoriteActivity";

    public static enum Type {
        me,
        farm
    }

    private String mIndex,mId,mUserType;
    private Type mType;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        mIndex = getIntent().getStringExtra("index");
        mId = getIntent().getStringExtra("id");
        mUserType = getIntent().getStringExtra("userType");
        mType = (Type) getIntent().getSerializableExtra("type");

        if(Type.me == mType) {
            meFragment();
        } else {
            farmragment();
        }
    }

    private void meFragment() {
        FavoriteListFragment fragment = FavoriteListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FavoriteListFragment.TAG);
        ft.commit();
    }

    private void farmragment() {
        FavoriteFarmListFragment fragment = FavoriteFarmListFragment.newInstance(mUserType,mIndex,mId);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FavoriteFarmListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.GetListFavoriteTitle);
        initActionBarHomeBtn();
    }
}
