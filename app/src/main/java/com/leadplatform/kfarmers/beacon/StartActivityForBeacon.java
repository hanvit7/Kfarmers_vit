package com.leadplatform.kfarmers.beacon;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import com.leadplatform.kfarmers.KFarmersApplication;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.AppState;
import com.leadplatform.kfarmers.model.json.BeaconJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.gcm.GcmIntentService;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.SplashActivity;

public class StartActivityForBeacon extends BaseFragmentActivity {
	
	HashMap<String, String> pushMap;
	AlertDialog.Builder alert;
	DialogInterface mPopupDlg = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        CommonUtil.setVibratorAndRing(mContext);
	}
	@Override
	public void onCreateView(Bundle savedInstanceState) {
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        CommonUtil.setVibratorAndRing(mContext);
        beaconPross(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		beaconPross(getIntent());
	}
	
	private void beaconPross(Intent intent) 
	{
	    pushMap = new HashMap<String, String>();
        BeaconJson beacon = (BeaconJson)getIntent().getSerializableExtra(BeaconHelper.BEACON_BUNDLE);
        
        pushMap.put(BeaconHelper.ACTION_TYPE, "beaon");
        
		if(beacon.minor.toString().equals("1")) // 케이파머스
		{
			pushMap.put(BeaconHelper.BEACON_TYPE, "youtube");
			pushMap.put(BeaconHelper.BEACON_YOUTUBE_URL, "TfMzeRTPNY0");
		}
		else if(beacon.minor.toString().equals("2")) // 고창
		{
			pushMap.put(BeaconHelper.BEACON_TYPE, "youtube|web");
			pushMap.put(BeaconHelper.BEACON_YOUTUBE_URL, "-fGdEgvoscY");
			pushMap.put(BeaconHelper.BEACON_WEB_URL, "http://m.berryfarm.kr");
		}
        
		alert = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Light_Dialog));

        alert.setTitle("K파머스");
        alert.setMessage("비콘");
		
		android.content.DialogInterface.OnClickListener mDefaultNegativeListener = new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
		
		android.content.DialogInterface.OnClickListener mDefaultPositiveListener = new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Intent intent = null;
				// 앱이 미구동 상태.
		        if (KFarmersApplication.appState != AppState.IS_RUNNING) {
		        	intent = new Intent(mContext, SplashActivity.class);
		        	intent.putExtra(GcmIntentService.PUSH_BUNDLE, pushMap);
		            startActivity(intent);
		        }
		        // 앱 구동중.
		        else 
		        {
		        	BeaconHelper.separateBeacon(mContext, pushMap);
		        }
		        
		        finish();
			}
		};
		
		alert.setPositiveButton(R.string.dialog_see, mDefaultPositiveListener);
		alert.setNegativeButton(R.string.dialog_close, mDefaultNegativeListener);
		alert.setCancelable(true);
		
		if ( mPopupDlg != null)
		{
			mPopupDlg.dismiss();
			mPopupDlg = null;
		}
		
		mPopupDlg = alert.show();
			
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if ( mPopupDlg != null)
		{
			mPopupDlg.dismiss();
			mPopupDlg = null;
		}
		
		Log.d("beacon","des");
	}

	@Override
	public void initActionBar() {}
}
