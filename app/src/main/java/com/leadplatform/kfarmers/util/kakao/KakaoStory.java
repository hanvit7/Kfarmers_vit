package com.leadplatform.kfarmers.util.kakao;

import android.content.Context;
import android.util.Log;

import com.kakao.APIErrorResult;
import com.kakao.BasicKakaoStoryPostParamBuilder;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoStoryHttpResponseHandler;
import com.kakao.KakaoStoryService;
import com.kakao.KakaoStoryService.StoryType;
import com.kakao.MyStoryInfo;
import com.kakao.NoteKakaoStoryPostParamBuilder;
import com.kakao.PhotoKakaoStoryPostParamBuilder;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.leadplatform.kfarmers.controller.DbController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class KakaoStory {

	public static void requestStoryPost(final Context context,
			final String text, final ArrayList<File>imageUrl,
			final MyKakaoStoryHttpResponseHandler<MyStoryInfo> listener) {

		if (Session.initializeSession(context, new SessionCallback() {
			@Override
			public void onSessionOpened() {
				if (imageUrl != null && imageUrl.size()>0)
					requestMultiUpload(context, text, imageUrl, listener);
					//requestUpload(context, text, imageUrl, listener);
				else
					requestPost(context, text, null, listener);
			}
			@Override
			public void onSessionClosed(KakaoException exception) {
				Log.e("KakaoStory", "====== onSessionClosed =====");
				listener.onHttpSessionClosedFailure(null);
                DbController.updateKakaoFlag(context, false);
				// UiController.showDialog(context,R.string.dialog_error_unknown_kakao);
			}
		})) {
			//listener.onHttpSessionClosedFailure(null);
			// UiController.showDialog(context,R.string.dialog_error_unknown_kakao);
		} else if (Session.getCurrentSession().isOpened()) {
			if (imageUrl != null && imageUrl.size()>0)
				//requestUpload(context, text, imageUrl, listener);
				requestMultiUpload(context, text, imageUrl, listener);
			else
				requestPost(context, text, null, listener);
		}
	}
	
	private static void requestMultiUpload(final Context context, final String text,
			final ArrayList<File> imageUrl,
			final MyKakaoStoryHttpResponseHandler<MyStoryInfo> listener) {
		
		try {
			KakaoStoryService.requestMultiUpload(new KakaoStoryHttpResponseHandler<String[]>() {
				
				@Override
				protected void onHttpSuccess(String[] resultObj) {
					requestPost(context,text,resultObj,listener);
				}
				
				@Override
				protected void onHttpSessionClosedFailure(APIErrorResult errorResult) {
					listener.onHttpSessionClosedFailure(errorResult);
				}
				
				@Override
				protected void onNotKakaoStoryUser() {
					listener.onNotKakaoStoryUser();
				}
				
				@Override
				protected void onFailure(APIErrorResult errorResult) {
					listener.onFailure(errorResult);
				}
			},imageUrl);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			listener.onFailure(null);
		}
	}
	
	private static void requestPost(final Context context,final String text,
			final String[] resultObj,
			final MyKakaoStoryHttpResponseHandler<MyStoryInfo> listener) {
		
		BasicKakaoStoryPostParamBuilder builder; 
		StoryType storyType;
		
		if(null != resultObj)
		{
			builder = new PhotoKakaoStoryPostParamBuilder(resultObj).setContent(text);
			storyType = StoryType.PHOTO;
		}
		else
		{
			builder = new NoteKakaoStoryPostParamBuilder(text);
			storyType = StoryType.NOTE;
		}
		
		builder.setShareable(true);
		
		try {
			KakaoStoryService.requestPost(storyType, listener, builder.build());
		} catch (KakaoParameterException e) {
			e.printStackTrace();
			listener.onFailure(null);
		}
	}

	/*private static void requestUpload(final Context context, final String text,
			final String imageUrl,
			final MyKakaoStoryHttpResponseHandler<Void> listener) {
		try {
			UiController.showProgressDialog(context);
			KakaoStoryService.requestUpload(
					new KakaoStoryHttpResponseHandler<KakaoStoryUpload>() {
						@Override
						protected void onHttpSuccess(KakaoStoryUpload resultObj) {
							requestPost(context, text, resultObj.getUrl(),
									listener);
						}

						@Override
						protected void onHttpSessionClosedFailure(
								APIErrorResult errorResult) {
							UiController.hideProgressDialog(context);
							listener.onHttpSessionClosedFailure(errorResult);
							// UiController.showDialog(context,
							// R.string.dialog_error_unknown_kakao);
						}

						@Override
						protected void onNotKakaoStoryUser() {
							UiController.hideProgressDialog(context);
							listener.onNotKakaoStoryUser();
							// UiController.showDialog(context,
							// R.string.dialog_error_unknown_kakao);
						}

						@Override
						protected void onFailure(APIErrorResult errorResult) {
							UiController.hideProgressDialog(context);
							listener.onFailure(errorResult);
							// UiController.showDialog(context,
							// R.string.dialog_error_unknown_kakao);
						}
					}, new File(imageUrl));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			UiController.hideProgressDialog(context);
			listener.onFailure(null);
			// UiController.showDialog(context,
			// R.string.dialog_error_unknown_kakao);
		}
	}

	private static void requestPost(final Context context, final String text,
			final String imageUrl,
			final MyKakaoStoryHttpResponseHandler<Void> listener) {
		final KakaoStoryPostParamBuilder postParamBuilder = new KakaoStoryPostParamBuilder(
				PERMISSION.PUBLIC);
		postParamBuilder.setContent(text);
		postParamBuilder.setAndroidExecuteParam(null);
		postParamBuilder.setIOSExecuteParam(null);
		if (imageUrl != null)
			postParamBuilder.setImageURL(imageUrl);

		Bundle parameters = null;
		try {
			UiController.showProgressDialog(context);
			parameters = postParamBuilder.build();
			KakaoStoryService.requestPost(listener, parameters);
		} catch (KakaoParameterException e) {
			e.printStackTrace();
			UiController.hideProgressDialog(context);
			listener.onFailure(null);
			// UiController.showDialog(context,
			// R.string.dialog_error_unknown_kakao);
		}
	}*/

}
