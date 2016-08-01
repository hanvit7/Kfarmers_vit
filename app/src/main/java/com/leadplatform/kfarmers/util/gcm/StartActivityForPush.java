package com.leadplatform.kfarmers.util.gcm;

import android.content.Intent;
import android.os.Bundle;

import com.leadplatform.kfarmers.KFarmersApplication;
import com.leadplatform.kfarmers.model.AppState;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.main.SplashActivity;

import java.util.HashMap;

public class StartActivityForPush extends BaseFragmentActivity {

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		Intent intent = null;
        if ((getIntent() == null) && (getIntent().getExtras() == null)) {
            return;
        }

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PUSH);
        
        HashMap<String, String> hashMap = (HashMap<String, String>) getIntent().getSerializableExtra(GcmIntentService.PUSH_BUNDLE);

        // 앱이 미구동 상태.
        if (KFarmersApplication.appState != AppState.IS_RUNNING) {
        	intent = new Intent(this, SplashActivity.class);
        	intent.putExtra(GcmIntentService.PUSH_BUNDLE,hashMap);
            startActivity(intent);
        }
        // 앱 구동중.
        else {
        	GcmIntentService.separatePushNotice(this, hashMap);
        }
        finish();
	}

	@Override
	public void initActionBar() {}
}
