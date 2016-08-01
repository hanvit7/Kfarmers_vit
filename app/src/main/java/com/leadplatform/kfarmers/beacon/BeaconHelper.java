package com.leadplatform.kfarmers.beacon;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.leadplatform.kfarmers.view.main.MainActivity;

public class BeaconHelper {

	public static final String ACTION_TYPE = "actionType";
	public static final String BEACON_BUNDLE = "beacon_bundle";
	public static final String BEACON_TYPE = "beacon_type";
	public static final String BEACON_WEB_URL = "beacon_webUrl";
	public static final String BEACON_YOUTUBE_URL = "beacon_youtubeUrl";
	public static final String BEACON_DIARY_IDX = "beacon_diaryIdx";
	public static final String BEACON_FARMER_IDX = "beacon_farmerIdx";
	
	public static void separateBeacon(Context context,
			HashMap<String, String> hashMap) {

		if (hashMap == null) {
			return;
		}

		Intent intent = null;
		
		if (hashMap.get(BeaconHelper.BEACON_TYPE).equals("youtube"))
		{
			intent = new Intent(context, BeaconActivity.class);
			intent.putExtra(BEACON_BUNDLE, hashMap);
		}
		else if (hashMap.get(BeaconHelper.BEACON_TYPE).equals("web"))
		{
			intent = new Intent(context, BeaconActivity.class);
			intent.putExtra(BEACON_BUNDLE, hashMap);
		}
		else if (hashMap.get(BeaconHelper.BEACON_TYPE).equals("youtube|web"))
		{
			intent = new Intent(context, BeaconActivity.class);
			intent.putExtra(BEACON_BUNDLE, hashMap);
		}
		else {
			intent = new Intent(context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}
		context.startActivity(intent);
	}

	public static boolean isScreenOn(Context context) {
		return ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
				.isScreenOn();
	}
}
