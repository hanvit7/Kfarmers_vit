package com.leadplatform.kfarmers.view.common;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class ImageSelectorActivity extends BaseFragmentActivity {

	int maxSize = 1;
	int nowSize = 0;
	int cell_size;
	String faceBookId ="";
	String faceBookDate = "";
	

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_base);
		
		initContentView(savedInstanceState);
	}
	
	private void initContentView(Bundle savedInstanceState) {
		
		Intent intent = getIntent();
		if (intent != null) {
			maxSize = intent.getIntExtra("maxsize",1);
			nowSize = intent.getIntExtra("nowsize",0);
			faceBookId = intent.getStringExtra("faceBookId");
			faceBookDate = intent.getStringExtra("faceBookDate");
		}

		
		Display display = getWindowManager().getDefaultDisplay();
		
		cell_size = display.getWidth() / 3;

		if(!faceBookId.isEmpty())
		{
			ImageSelectorFaceBookFragment fragment = ImageSelectorFaceBookFragment.newInstance(maxSize,nowSize,cell_size,faceBookId,faceBookDate);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, fragment, ImageSelectorFaceBookFragment.TAG);
			ft.commit();
		}
		else
		{
			ImageSelectorFragment fragment = ImageSelectorFragment.newInstance(maxSize,nowSize,cell_size);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, fragment, ImageSelectorFragment.TAG);
			ft.commit();
		}
	}
	
	public void onActionBarRightBtnClicked()
	{
		FragmentManager fm = getSupportFragmentManager();
		if(faceBookId.isEmpty())
		{
			ImageSelectorFragment fragment = (ImageSelectorFragment) fm.findFragmentByTag(ImageSelectorFragment.TAG);
			
			if (fragment != null) 
			{
				if(fragment.adapter.seletedImg.size()>0)
				{
					Intent intent = new Intent();
					intent.putStringArrayListExtra("imagePath", fragment.adapter.seletedImg);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		}
		else
		{
			ImageSelectorFaceBookFragment fragment = (ImageSelectorFaceBookFragment) fm.findFragmentByTag(ImageSelectorFaceBookFragment.TAG);
			
			if (fragment != null) 
			{
				if(ImageSelectorFaceBookFragment.seletedImg.size()>0)
				{
					ArrayList<String> seletedImg = new ArrayList<String>();
					for(String str : ImageSelectorFaceBookFragment.seletedImg)
					{
						seletedImg.add(ImageUtil.getFilePath(str));	
					}
					
					Intent intent = new Intent();
					intent.putStringArrayListExtra("imagePath", seletedImg);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		}
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText("이미지 선택");
		
		Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setText(R.string.actionbar_upload);
		rightBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onActionBarRightBtnClicked();
			}
		});
		
		Button leftBtn = (Button) findViewById(R.id.actionbar_left_button);
		leftBtn.setVisibility(View.VISIBLE);
		leftBtn.setText(R.string.actionbar_cancel);
		leftBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				finish();
			}
		});
	}
}
