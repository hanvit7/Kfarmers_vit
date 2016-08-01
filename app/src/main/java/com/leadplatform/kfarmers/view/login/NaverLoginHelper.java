package com.leadplatform.kfarmers.view.login;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.CommonApiController;
import com.leadplatform.kfarmers.controller.CommonResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.parcel.LoginData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.AndroidUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.DataCryptUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.xml.JSONObject;
import com.leadplatform.kfarmers.util.xml.XML;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.nhn.android.naverlogin.data.OAuthResponse;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;

/**
 * <br/>
 * OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제, <br/>
 * 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 * 
 * @author naver
 * 
 */

public class NaverLoginHelper
{
	public static String Naver = "naver";

	/**
	 * client 정보를 넣어준다.
	 */
	private static String OAUTH_CLIENT_ID = "ZZIpXeYPUHMGeCIhpK3t";
	private static String OAUTH_CLIENT_SECRET = "tRteSQQF3_";
	private static String OAUTH_CLIENT_NAME = "K파머스";
	private static String OAUTH_CALLBACK_URL = "com.leadplatform.kfarmers.view.sns.SnsNaverLoginActivity";

	private static OAuthLogin mOAuthLoginInstance;
	private static Context mContext;
	private static OpenIdLoginListener openIdLoginListener;
	
	
	public static void NaverLogin(Context context , OpenIdLoginListener listener) {
		
		mContext = context;
		openIdLoginListener = listener;
		initData(mContext);
		mOAuthLoginInstance.startOauthLoginActivity((Activity)mContext, mOAuthLoginHandler);
	}
	
	public static void NaverBlogLogin(Context context , OAuthLoginHandler listener) {
		mContext = context;
		initData(mContext);
		mOAuthLoginInstance.startOauthLoginActivity((Activity)mContext,listener);
	}
	
	public static OAuthLogin initData(Context mContext) {
		mOAuthLoginInstance = OAuthLogin.getInstance();
		mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID,OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_CALLBACK_URL);
		return mOAuthLoginInstance;
	}
	
	/**
	 * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는
	 * 걸 알 수 있다.
	 */
	private static OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
		@Override
		public void run(boolean success) {
			if (success) 
			{
				FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();
				LoginFragment fragment = (LoginFragment) fm.findFragmentByTag(LoginFragment.TAG);
				if (fragment != null) {
					fragment.loginView.setVisibility(View.VISIBLE);
				}
				new RequestApiTask().execute();
			} else {
				openIdLoginListener.onResult(false, "");
			}
		}
    };


	public static class RequestApiTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {}

		@Override
		protected String doInBackground(Void... params) {
			String url = "https://apis.naver.com/nidlogin/nid/getUserProfile.xml";
			String at = mOAuthLoginInstance.getAccessToken(mContext);
			return mOAuthLoginInstance.requestApi(mContext, at, url);
		}

		protected void onPostExecute(String content) 
		{
			openIdLoginListener.onResult(true, content);
		}
	}
	
	public static void openIdRegister(Context context, String content,OpenIdLoginListener listener)
	{
		mContext = context;
		openIdLoginListener = listener;
		initData(mContext);
		
		JSONObject jsonObject = XML.toJSONObject(content);
		
		jsonObject = jsonObject.getJSONObject("data");

		if(jsonObject != null && jsonObject.getJSONObject("result").get("resultcode").equals("00"))
		{
			String phone = AndroidUtil.getPhoneNumber(mContext);
			if(phone == null)
			{
				phone="";
			}
			else
			{
				phone = CommonUtil.PatternUtil.convertPhoneFormat(phone);
			}
			
			jsonObject = jsonObject.getJSONObject("response");
			
			String name = "";
			if(PatternUtil.isEmpty(jsonObject.getString("nickname").trim()))
			{
				name = "NoName";	
			}
			else 
			{
				name = jsonObject.getString("nickname");	
			}


            CenterController.openIdLogin(mOAuthLoginInstance.getAccessToken(mContext),
                    jsonObject.getString("enc_id"),
                    Naver,
                    name,
                    phone,
                    jsonObject.getString("email"),
                    jsonObject.getString("profile_image"),
                    new CenterResponseListener(mContext)
                    {
                        @Override
                        public void onSuccess(int Code, String content) {
                            super.onSuccess(Code, content);
                            if(Code == 0)
                            {
                                try
                                {
                                    JsonNode root = JsonUtil.parseTree(content);
                                    root = root.get("Data");
                                    LoginData data = new LoginData();
                                    data.setId(root.get("ID").textValue());
                                    data.setPw(root.get("PW").textValue());
                                    data.setbAuto(true);

                                    kFarmersLogin(data);
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                    openIdLoginListener.onResult(false,"");
                                }
                            }
                            else
                            {
                                openIdLoginListener.onResult(false,"");
                            }
                        }
                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers, byte[] content,
                                              Throwable error) {
                            super.onFailure(statusCode, headers, content, error);
                            openIdLoginListener.onResult(false, "");
                        }
                    }
            );
			
			/*TokenApiController.openIdLogin(mOAuthLoginInstance.getAccessToken(mContext),
					jsonObject.getString("enc_id"),
					Naver,
					name,
					phone,
					jsonObject.getString("email"),
					jsonObject.getString("profile_image"),
					new TokenResponseListener(mContext){
						@Override
						public void onSuccess(int Code, String content) {
							super.onSuccess(Code, content);
							if(Code == TokenResponseListener.SUCCESS)
							{
								try 
								{
									JsonNode root = JsonUtil.parseTree(content);
									LoginData data = new LoginData();
									data.setId(root.get("id").textValue());
									data.setPw(root.get("pw").textValue());
									data.setbAuto(true);

									kFarmersLogin(data);
								} catch (Exception e)
								{
									e.printStackTrace();
									openIdLoginListener.onResult(false,"");
								}
							}
							else
							{
								openIdLoginListener.onResult(false,"");
							}
						}
						@Override
						public void onFailure(int statusCode,
								Header[] headers, byte[] content,
								Throwable error) {
							super.onFailure(statusCode, headers, content, error);
							openIdLoginListener.onResult(false,"");
						}});*/
		}
	}
	
	 public static void kFarmersLogin(final LoginData data)
	 {
        CenterController.login(data, new CenterResponseListener(mContext)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:

                            SnipeApiController.getToken(data.getId(),data.getPw(),new SnipeResponseListener(mContext) {
                                @Override
                                public void onSuccess(int Code, String content, String error) {
                                    try {
                                        switch (Code) {
                                            case 600:
                                            {
                                                AppPreferences.setLogin(mContext, true);
                                                UserDb user = DbController.queryUser(mContext, data.getId());
                                                if (user == null) {
                                                    DbController.insertUser(mContext, new UserDb(data.getId(), data.getPw(),true, true,Naver,""));
                                                } else {
                                                    String pw = "";
                                                    try {
                                                        pw = DataCryptUtil.encrypt(DataCryptUtil.dataK, data.getPw());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    user.setUserID(data.getId());
                                                    user.setUserPW(pw);
                                                    user.setAutoLoginFlag(1);
                                                    user.setOpenLoginType(Naver);
                                                    user.setApiToken("");
                                                    DbController.updateUser(mContext, user);
                                                }
                                                DbController.updateCurrentUser(mContext, data.getId());
                                                DbController.updateApiToken(mContext, content);
                                                SnipeApiController.setToken(content);
                                                requestUserProfile();
                                                break;
                                            }
                                            default: {
                                                UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_id);
                            openIdLoginListener.onResult(false,"");
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
                            openIdLoginListener.onResult(false,"");
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_unknown_id);
                            openIdLoginListener.onResult(false,"");
                            break;
                        case 1004:
                            UiController.showDialog(mContext, R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
                                    new CustomDialogListener()
                                    {
                                        @Override
                                        public void onDialog(int type)
                                        {
                                            if (type == UiDialog.DIALOG_POSITIVE_LISTENER)
                                            {
                                                CommonUtil.AndroidUtil.actionDial(mContext, mContext.getResources().getString(R.string.setting_service_center_phone));
                                            }
                                        }
                                    });
                            openIdLoginListener.onResult(false,"");
                            break;
                        case 1005:
                            UiController.showDialog(mContext, R.string.dialog_expired_date);
                            openIdLoginListener.onResult(false,"");
                            break;
                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            openIdLoginListener.onResult(false,"");
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    openIdLoginListener.onResult(false,"");
                }
            }
        });
	 }
	 
    private static void requestUserProfile()
    {
        CenterController.getProfile(new CenterResponseListener(mContext)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                        	AppPreferences.setLogin(mContext, true);
							DbController.updateProfileContent(mContext, content);
                            requestDeviceAddress();
                            openIdLoginListener.onResult(true,content);
                            break;

                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            openIdLoginListener.onResult(false,"");
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    openIdLoginListener.onResult(false,"");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers,
            		byte[] content, Throwable error) {
            	super.onFailure(statusCode, headers, content, error);
            	openIdLoginListener.onResult(false,"");
            }
        });
    }
    
    private static void requestDeviceAddress()
    {
        String regID = AppPreferences.getGcmRegistrationId(mContext);
        if (!TextUtils.isEmpty(regID))
        {
            CenterController.setDeviceAddress(regID, "Android", (AppPreferences.getGcmEnable(mContext)) ? "Y" : "N", new CenterResponseListener(mContext)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                /*runMainActivity();
                                finish();*/
                            	/*((BaseFragmentActivity)mContext).runMainActivity();
								((Activity)mContext).finish();*/
                                break;

                            default:
                                UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    }
                }
            });
        }
        else
        {
            /*runMainActivity();
            finish();*/
        }
    }
    
    public static void naverLogout(Context context)
    {
        mContext = context;
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                initData(mContext);
                mOAuthLoginInstance.logoutAndDeleteToken(mContext);
                return null;
            }
        };
        asyncTask.execute();
    }
    
    public static OAuthLoginState naverState(Context context)
    {
    	mContext = context;
    	initData(mContext);
    	return mOAuthLoginInstance.getState(mContext);
    }
    
    public static void naverTokenRefresh(Context context,OpenIdLoginListener listener)
    {
    	mContext = context;
    	initData(mContext);
    	openIdLoginListener = listener;
    	new RefreshTokenTask().execute();
    }
    
    public static void naverUserInfo(Context context,OpenIdLoginListener listener)
    {
    	mContext = context;
    	initData(mContext);
    	openIdLoginListener = listener;
    	new RequestApiTask().execute();
    }
    
    public static void naverCategory(Context context,String id, CommonResponseListener commonResponseListener)
    {
    	mContext = context;
    	initData(mContext);
    	CommonApiController.getNaverCategory(id, mOAuthLoginInstance.getAccessToken(mContext),commonResponseListener);
    }
    
    public static void naverBlogWrite(Context context, String title, String contents,ArrayList<File> files,CommonResponseListener commonResponseListener)
    {
    	mContext = context;
    	initData(mContext);
    	CommonApiController.writeNaverBlog(context,title, contents, files, mOAuthLoginInstance.getAccessToken(mContext),commonResponseListener);
    }
    
    public class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			initData(mContext);
			OAuthResponse res = mOAuthLoginInstance.logoutAndDeleteToken(mContext);
			return null;
		}
		
		protected void onPostExecute(Void v) {}
	}

	public static class RefreshTokenTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			initData(mContext);
			return mOAuthLoginInstance.refreshAccessToken(mContext);
		}
		protected void onPostExecute(String res) {
			openIdLoginListener.onResult(true, "");
		}
	}
	
	public static void clearNaverDb(Context context)
	{
		mContext = context;
		DbController.updateNaverCategory(mContext, null,null);
		DbController.updateNaverFlag(mContext, false);
		DbController.updateBlogNaver(mContext, null);
		
	}
	
	
}