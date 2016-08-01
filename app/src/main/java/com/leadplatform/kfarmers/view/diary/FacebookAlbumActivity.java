package com.leadplatform.kfarmers.view.diary;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FacebookAlbumActivity extends BaseFragmentActivity {

	@Override
	public void onCreateView(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_base);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		FacebookAlbumFragment fragment = FacebookAlbumFragment.newInstance();
		ft.replace(R.id.fragment_container, fragment, FacebookAlbumFragment.TAG);
		ft.commit();
	}

	@Override
	public void initActionBar() 
	{}

}
