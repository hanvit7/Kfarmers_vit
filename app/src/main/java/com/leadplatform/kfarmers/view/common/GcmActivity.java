package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class GcmActivity extends BaseFragmentActivity {

	private final int FINISH_TIME_OUT_MSG = 0;
	private final long FINISH_TIME_OUT_SEC = 10 * 1000;
	private WakeLock wakeLock;

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.activity_gcm);

		Intent intent = getIntent();
		if (intent != null) {
			String msg = intent.getStringExtra("message");
			UiController.showDialog(this, "K파머스", msg, R.string.dialog_see, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					finishTimeOutHandler.removeMessages(FINISH_TIME_OUT_MSG);
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {

					} else {
						finish();
					}
				}
			});
			finishTimeOutHandler.sendEmptyMessageDelayed(FINISH_TIME_OUT_MSG, FINISH_TIME_OUT_SEC);
		}
	}

	@Override
	public void initActionBar() {

	}

	private Handler finishTimeOutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FINISH_TIME_OUT_MSG) {
				finish();
			}
		}
	};

}
