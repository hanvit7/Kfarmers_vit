package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.util.Log;

import com.leadplatform.kfarmers.BuildConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

public class CommonApiController {
	private final static String TAG = "CommonApiController";

	private static AsyncHttpClient client = new AsyncHttpClient();

	private static void initHttpClientParams() {
		client.setTimeout(30000);
	}

	private static void post(String url, RequestParams params, HashMap<String, String> header, AsyncHttpResponseHandler responseHandler) {
		if (client != null) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, " POST Url = " + url);
				Log.e(TAG, " POST Params = " + params.toString());
			}

			initHttpClientParams();

			for (String key : header.keySet()) {
				client.addHeader(key, header.get(key));
			}
			client.post(url, params, responseHandler);
		}
	}

	public static void getNaverCategory(String id, String token, CommonResponseListener responseHandler) {
		RequestParams params = new RequestParams();
		params.put("blogId", id);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("Authorization", "Bearer  " + token);

		post("https://openapi.naver.com/blog/listCategory.json", params, hashMap, responseHandler);
	}

	public static void writeNaverBlog(Context context, String title, String contents, ArrayList<File> files, String token, CommonResponseListener responseHandler) {
		RequestParams params = new RequestParams();
		params.put("blogId", DbController.queryBlogNaver(context));

		params.put("title", title);

		params.put("contents", contents);
		params.put("categoryNo", DbController.queryCurrentUser(context).getNaverCategoryNo());

		params.addAll(files);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("Authorization", "Bearer  " + token);

		post("https://openapi.naver.com/blog/writePost.json", params, hashMap, responseHandler);
	}

	public static void getSearchJibunAddress(String url, String key, String search, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("searchSe", "dong");
		params.put("srchwrd", search);
		params.put("serviceKey", URLDecoder.decode(key));

		initHttpClientParams();

		client.get(url, params, responseHandler);
	}
}
