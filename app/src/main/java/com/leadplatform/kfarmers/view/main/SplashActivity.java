package com.leadplatform.kfarmers.view.main;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.kakao.Session;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.KFarmersApplication;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.SnipeResponsePlanListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.AppState;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.gcm.GcmIntentService;
import com.leadplatform.kfarmers.util.kakao.KakaoLinker;
import com.leadplatform.kfarmers.util.location.ISensorListener;
import com.leadplatform.kfarmers.util.location.SensorFactory;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;

import org.apache.http.Header;

import java.util.HashMap;

public class SplashActivity extends BaseFragmentActivity implements ISensorListener {
	private static final String TAG = "SplashActivity";

	private final int FINISH_TIME_OUT_MSG = 0;
	private final long FINISH_TIME_OUT_SEC = 2 * 1000;
	private final int LOCATION_TIME_OUT_MSG = 0;
	private final long LOCATION_TIME_OUT_SEC = 3 * 1000;

	private SensorFactory sensorFactory;
	private boolean versionCheckState = false, userCheckState = false, locationCheckState = false;

	private HashMap<String, String> pushMap;

	private TextView mVersionText;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "VIT] onCreate : " + TAG);
		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SPLASH);
	}

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_splash);

		if (getIntent().getData() != null) {
			Uri uri = getIntent().getData();

			pushMap = new HashMap<String, String>();

			if (uri.getQueryParameter(GcmIntentService.PUSH_TYPE) != null) {
				pushMap.put(GcmIntentService.PUSH_TYPE, uri.getQueryParameter(GcmIntentService.PUSH_TYPE));
			} else if (uri.getQueryParameter(KakaoLinker.CUSTOM_TYPE) != null) {
				pushMap.put(GcmIntentService.PUSH_TYPE, uri.getQueryParameter(KakaoLinker.CUSTOM_TYPE));
			}

			if (uri.getQueryParameter(GcmIntentService.PUSH_INDEX) != null) {
				pushMap.put(GcmIntentService.PUSH_INDEX, uri.getQueryParameter(GcmIntentService.PUSH_INDEX));
			} else if (uri.getQueryParameter(KakaoLinker.CUSTOM_INDEX) != null) {
				pushMap.put(GcmIntentService.PUSH_INDEX, uri.getQueryParameter(KakaoLinker.CUSTOM_INDEX));
			}
		}
		// 푸쉬매니저로부터 받은 인텐트 정보.
		else if ((getIntent() != null) && (getIntent().getExtras() != null)) {
			pushMap = (HashMap<String, String>) getIntent().getSerializableExtra(GcmIntentService.PUSH_BUNDLE);
		}

		mVersionText = (TextView) findViewById(R.id.versionText);
		try {
			PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersionText.setText("v " + i.versionName);
		} catch (NameNotFoundException e) {
		}
	}

	@Override
	public void initActionBar() {
	}


	@Override
	public void onResume() {
		super.onResume();
		startCurrentLocation();
		checkVersion();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopCurrentLocation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopCurrentLocation();
	}

	private Handler locationTimeOutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOCATION_TIME_OUT_MSG) {
				Log.d(TAG, "VIT] 위치 타임아웃 메세지 받음");
				Log.e(TAG, "============== locationTimeOutHandler !!! == ");
				if (versionCheckState && userCheckState) {
					stopCurrentLocation();//현재 위치 탐색 중지
					runMainActivity(pushMap);//MainActivity 호출 및 pushMap 전달
					finish();
				} else {
					KFarmersApplication.appState = AppState.IS_RUNNING;//앱구동상태 : is_running
					locationTimeOutHandler.sendEmptyMessageDelayed(LOCATION_TIME_OUT_MSG, 1000);//위치 타임아웃 1초 딜레이
				}
			}
		}
	};

	private Handler finishTimeOutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FINISH_TIME_OUT_MSG) {
				Log.d(TAG, "VIT] 초기화완료 타임아웃 메세지 받음");
				Log.e(TAG, "============== finishTimeOutHandler !!! == ");
				if (versionCheckState && userCheckState && locationCheckState) {
					stopCurrentLocation();//현재 위치 탐색 중지
					runMainActivity(pushMap);//MainActivity 호출 및 pushMap 전달
					KFarmersApplication.appState = AppState.IS_RUNNING;//앱구동상태 : is_running
					finish();
				}
			}
		}
	};

	private void checkUserData() {
		Log.d(TAG, "VIT] 저장된 데이터에서 유저 상태 트루");
		AppPreferences.setLogin(this, false);//AppPreferences 로그인 값 false
		UserDb user = DbController.queryCurrentUser(this);
		if (user != null && user.getAutoLoginFlag() == 1) {
			requestUserProfile();
		} else {

			DbController.updateCurrentUser(this, "");
			DbController.clearDb(mContext);

			if (AccessToken.getCurrentAccessToken() != null) {
				LoginManager.getInstance().logOut();
			}

			NaverLoginHelper.naverLogout(mContext);

			Session.initializeSession(mContext, null);
			Session.getCurrentSession().close(null);

			SnipeApiController.setToken("");

			userCheckState = true;
			Log.d(TAG, "VIT] 저장된 데이터에서 유저 상태 트루");
			getToken();
		}
	}

	private void requestUserProfile() {
		Log.d(TAG, "VIT] 서버에 프로파일 요청");
		CenterController.getProfile(new CenterResponseListener(this) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							Log.d(TAG, "VIT] 서버에 요청한 프로파일의 리퀘스트 코드: 0000");
							userCheckState = true;//유저 상태확인 트루

							AppPreferences.setLogin(SplashActivity.this, true);//로그인 확인 플래그 트루
							DbController.updateProfileContent(SplashActivity.this, content);
							getToken();
							break;
						default:
							Log.d(TAG, "VIT] 서버에 요청한 프로파일의 리퀘스트 코드: 0000가 아님");
							UiController.showDialog(mContext, R.string.dialog_unknown_error, new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
								}
							});
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(mContext, R.string.dialog_unknown_error, new CustomDialogListener() {
						@Override
						public void onDialog(int type) {
						}
					});
				}
			}
		});
	}

	private void startCurrentLocation() {
		sensorFactory = SensorFactory.getInstance((Application) getApplicationContext());
		Log.d(TAG, "VIT] GPS 인스턴스 받아오기");
		Location lastLocation = sensorFactory.getLastKnownLocation();
		Log.d(TAG, "VIT] 저장되어있는 최종 위치값");
		if (lastLocation == null) {
			sensorFactory.addListener(this);
			Log.d(TAG, "VIT] GPS 리스너 추가");
			locationTimeOutHandler.sendEmptyMessageDelayed(LOCATION_TIME_OUT_MSG, LOCATION_TIME_OUT_SEC);
			Log.d(TAG, "VIT] 위치 타임아웃 메세지 전달 (startCurrentLocation)");
		} else {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "============== Location Success !!! == Latitude = " + lastLocation.getLatitude() + " == Longitude = " + lastLocation.getLongitude());
			locationCheckState = true;
			Log.d(TAG, "VIT] 현재 위치 확인 트루");
			AppPreferences.setLatitude(this, lastLocation.getLatitude());
			AppPreferences.setLongitude(this, lastLocation.getLongitude());
			Log.d(TAG,"VIT] 현재 위치 저장");
		}
	}

	private void stopCurrentLocation() {//현재 위치 탐색 중지
		locationTimeOutHandler.removeMessages(LOCATION_TIME_OUT_MSG);
		Log.d(TAG, "VIT] 위치 타임아웃 제거");
		sensorFactory.removeListener(this);
		Log.d(TAG, "VIT] GPS 리스너 삭제");
	}


	@Override
	public void updateLocation(Location location) {//현재 위치 업데이트
		if (location != null) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "============== updateLocation Success !!! == Latitude = " + location.getLatitude() + " == Longitude = " + location.getLongitude());
			locationCheckState = true;
			Log.d(TAG,"VIT] 현재 위치확인 트루");
			AppPreferences.setLatitude(this, location.getLatitude());
			AppPreferences.setLongitude(this, location.getLongitude());
			Log.d(TAG,"VIT] 현재 위치 저장");
		}
	}

	@Override
	public void updateOrientation(int azimuth, int pitch, int roll) {
	}

	private void checkVersion() {
		SnipeApiController.getAppVersion(new SnipeResponsePlanListener(this) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					switch (Code) {
						case 200:
							double appVersion, serverVersion;

							serverVersion = Double.valueOf(content);
							appVersion = Double.valueOf(CommonUtil.AndroidUtil.getAppVersion(SplashActivity.this));

							if (appVersion < serverVersion) {

								LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								View convertView = inflater.inflate(R.layout.item_update, null);

								UiDialog.showDialog(mContext, "업데이트 안내", convertView, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
									@Override
									public void onDialog(int type) {
										if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
											Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
											marketLaunch.setData(Uri.parse("market://details?id=com.leadplatform.kfarmers"));
											startActivity(marketLaunch);
											KfarmersAnalytics.onClick(KfarmersAnalytics.S_SPLASH, "Click_Version-Update", "true");
											finish();
										} else {
											KfarmersAnalytics.onClick(KfarmersAnalytics.S_SPLASH, "Click_Version-Update", "false");
											finish();
										}
									}
								});
							} else {
								versionCheckState = true;
								Log.d(TAG, "VIT] 버전체크 트루");
								checkUserData();
							}
							break;
						default:
							UiController.showDialog(mContext, R.string.dialog_unknown_error, new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
								}
							});
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					UiController.showDialog(mContext, R.string.dialog_unknown_error, new CustomDialogListener() {
						@Override
						public void onDialog(int type) {
						}
					});
				}
			}
		});
	}

	public void getToken() {
		String id = "";
		String pw = "";

		UserDb user = DbController.queryCurrentUser(mContext);
		if (user != null && user.getAutoLoginFlag() == 1) {
			id = user.getUserID();
			pw = user.getUserPwDecrypt();
		}

		SnipeApiController.getToken(id, pw, new SnipeResponseListener(mContext) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				switch (Code) {
					case 600:
						DbController.updateApiToken(mContext, content);
						SnipeApiController.setToken(content);
						finishTimeOutHandler.sendEmptyMessageDelayed(FINISH_TIME_OUT_MSG, FINISH_TIME_OUT_SEC);
						Log.d(TAG, "VIT] 타임아웃 메세지 전달");
						break;
					default:
						UiController.showDialog(mContext, R.string.dialog_unknown_error);
						break;
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
			}
		});
	}
}
