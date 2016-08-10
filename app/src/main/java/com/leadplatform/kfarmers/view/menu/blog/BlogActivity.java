package com.leadplatform.kfarmers.view.menu.blog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class BlogActivity extends BaseFragmentActivity {
	public static final String TAG = "BlogActivity";

	private String userIndex, profileUserIndex;

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_base);

		initContentView(savedInstanceState);
	}

	private void initContentView(Bundle savedInstanceState) {
		initIntentData();
		initUserInfo();
		BlogListFragment fragment = BlogListFragment.newInstance(userIndex);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, fragment, BlogListFragment.TAG);
		ft.commit();
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);

		displayBlogListActionBar();

		initActionBarHomeBtn();
	}

	private void initUserInfo() {
		try {
			String profile = DbController.queryProfileContent(this);
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				profileUserIndex = profileData.Index;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayBlogListActionBar() {
		TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText(R.string.GetListBlogTitle);

		if (userIndex.equals(profileUserIndex)) {
			Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
			rightBtn.setVisibility(View.VISIBLE);
			rightBtn.setText(R.string.actionbar_write);
			rightBtn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					onActionBarRightBtnClicked();
				}
			});
		}
	}

	public void displayBlogWriteActionBar() {
		// TextView title = (TextView) findViewById(R.id.title);
		// title.setText(R.string.WriteBlogTitle);

		Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setText(R.string.actionbar_upload);
		rightBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onActionBarUploadBtnClicked();
			}
		});
	}

	private void initIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			userIndex = intent.getStringExtra("userIndex");
		}
	}

	private void onActionBarRightBtnClicked() {
		BlogWriteFragment fragment = BlogWriteFragment.newInstance();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, fragment, BlogWriteFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
	}

	private void onActionBarUploadBtnClicked() {
		BlogWriteFragment fragment = (BlogWriteFragment) getSupportFragmentManager().findFragmentByTag(BlogWriteFragment.TAG);
		if (fragment != null) {
			fragment.onUploadBtnClicked();
		}
		// BlogWriteFragment fragment = BlogWriteFragment.newInstance();
		// FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// ft.add(R.id.fragment_container, fragment, BlogWriteFragment.TAG);
		// ft.addToBackStack(null);
		// ft.commit();

	}
}
