package com.leadplatform.kfarmers.util.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.model.preference.AppPreferences;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GcmController {
	private final static String TAG = "GcmController";

	private static final String EXTRA_MESSAGE = "message";
	// private static final String SHARED_PREFERENCES_NAME = "gcm_db";
	// private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String SENDER_ID = "682651738427";

	private Context context;
	private GoogleCloudMessaging gcm;
	private String regId;
	private AtomicInteger msgId = new AtomicInteger();

	public void checkPlayService(Context context) {
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		this.context = context;
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this.context);
			
			regId = getRegistrationId(this.context);

			if (regId.isEmpty()) {
				registerInBackground();
			}
			else
			{
				//if(!AppPreferences.getGcmSend(this.context))
				//{
					sendRegistrationIdToBackend();
				//}
			}
		} else {
			Log.e(TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users to download the APK from the Google Play Store or enable it in the device's
	 * system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, (FragmentActivity) context, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				//Log.e("MainActivity.java | checkPlayService", "|This device is not supported.|");
				((FragmentActivity) context).finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regId = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					// You should send the registration ID to your server over HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your app.
					// The request to your server should be authenticated if your app
					// is using accounts.
					
					

					// For this demo: we don't need to send it because the device
					// will send upstream messages to a server that echo back the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				//Log.e("MainActivity.java | onPostExecute", "|" + msg + "|");
				sendRegistrationIdToBackend();
			}
		}.execute(null, null, null);
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	// private SharedPreferences getGCMPreferences(Context context)
	// {
	// // This sample app persists the registration ID in shared preferences, but
	// // how you store the regID in your app is up to you.
	// return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	// }

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {
		// final SharedPreferences prefs = getGCMPreferences(context);
		// String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		String registrationId = AppPreferences.getGcmRegistrationId(context);
		if (registrationId.isEmpty()) {
			Log.e(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		// int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int registeredVersion = AppPreferences.getGcmAppVersion(context);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.e(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send messages to your app. Not needed for this demo since the device sends upstream messages to a server
	 * that echoes back the message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		String regID = AppPreferences.getGcmRegistrationId(context);
        if (!TextUtils.isEmpty(regID))
        {
            CenterController.setDeviceAddress(regID, "Android", (AppPreferences.getGcmEnable(context)) ? "Y" : "N",
                    new CenterResponseListener(context)
                    {
                        @Override
                        public void onSuccess(int Code, String content)
                        {
                            try
                            {
                                switch (Code)
                                {
                                    case 0000:
                                    		AppPreferences.setGcmSend(context, true);
                                        break;

                                    default:
                                    		AppPreferences.setGcmSend(context, false);
                                        break;
                                }
                            }
                            catch (Exception e)
                            {
                            	AppPreferences.setGcmSend(context, false);
                            }
                        }
                    });
        }
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		// final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.e(TAG, "Saving regId on app version " + appVersion);
		// SharedPreferences.Editor editor = prefs.edit();
		// editor.putString(PROPERTY_REG_ID, regId);
		// editor.putInt(PROPERTY_APP_VERSION, appVersion);
		// editor.commit();

		AppPreferences.setGcmRegistrationId(context, regId);
		AppPreferences.setGcmAppVersion(context, appVersion);
	}

	/*public void sendGCM(Context context, String title, String message) {
		String API_KEY = "682651738427";
		String regId = AppPreferences.getGcmRegistrationId(context);

		Sender sender = new Sender(API_KEY);

		// 전송하고 싶은 메세지.. Client 에서 번들에서 꺼낼때 message 로 꺼내시면 됩니다.
		Message msg = new Message.Builder().addData("message", "test....테스트").build();

		// 5는 재시도 횟수
		Result result = null;
		try {
			result = sender.send(msg, regId, 5);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result.getMessageId() != null) {
			System.out.println("Success");
		} else {
			String error = result.getErrorCodeName();
			System.out.println("Error : " + error);
		}
	}*/
}
