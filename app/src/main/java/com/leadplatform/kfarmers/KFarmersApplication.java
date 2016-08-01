package com.leadplatform.kfarmers;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.model.AppState;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.Installation;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
//앱구동상태 정의
public class KFarmersApplication extends MultiDexApplication {//multidex:함수의 수가 65535개가 넘으면 오류나는것을 막기위해 사용
	private final static String TAG = "KFarmersApplication";
	public static AppState appState = AppState.NONE;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "VIT] onCreate : " + TAG);

		if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyFlashScreen().penaltyDropBox().penaltyLog().build());
		}

		KfarmersAnalytics.init(getApplicationContext());//KfarmersAnalytics 초기화

		SnipeApiController.setToken(DbController.queryApiToken(getApplicationContext()));//user 데이터 베이스에서 ApiToken 받아옴
		FacebookSdk.sdkInitialize(getApplicationContext());//페이스북 sdk 초기화
		Installation.id(getApplicationContext());//??
		initImageLoader(getApplicationContext());//이미지 로더 생성
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		Log.d(TAG, "VIT] attachBaseContext");
		MultiDex.install(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG, "VIT] onTerminate");
		appState = AppState.NONE;
		ImageLoader.getInstance().destroy();
		KfarmersAnalytics.destroy();
	}

	private static void initImageLoader(Context context) {
		Log.d(TAG, "VIT] initImageLoader : " + TAG);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				.build();

		ImageLoader.getInstance().init(config);

		File dir = ImageLoader.getInstance().getDiskCache().getDirectory();
		CommonUtil.FileUtil.delFolderFile(dir);
		long size = CommonUtil.FileUtil.getFolderSize(dir);
		if (size / 1048576 > 200) {
			ImageLoader.getInstance().clearDiskCache();
		}
		ImageLoader.getInstance().clearMemoryCache();
	}

//    public static ImageLoader getImageLoader() {
//        return ImageLoader.getInstance();
//    }
}
