package com.leadplatform.kfarmers.view.login;

import android.content.Context;
import android.text.TextUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.*;
import com.kakao.helper.Logger;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.*;
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
import org.apache.http.Header;

/**
 * <br/>
 * OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제, <br/>
 * 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 * 
 * @author naver
 * 
 */

public class KakaoLoginHelper
{
	public static String Kakao = "kakao";



	private static Context mContext;
	private static OpenIdLoginListener openIdLoginListener;
	
	 /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    public static void requestMe(final OpenIdLoginListener listener) {
        UserManagement.requestMe(new MeResponseCallback() {

            @Override
            protected void onSuccess(final UserProfile userProfile) {
                Logger.getInstance().d("UserProfile : " + userProfile);
                userProfile.saveUserToCache();
                
                listener.onResult(true, "");
            }

            @Override
            protected void onNotSignedUp() {
                //showSignup();
            	listener.onResult(false, "");
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                //redirectLoginActivity();
            	listener.onResult(false, "");
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.getInstance().d(message);
                //redirectLoginActivity();
                listener.onResult(false, "");
            }
        });
    }
	
	public static void openIdRegister(Context context, UserProfile profile,OpenIdLoginListener listener)
	{
		mContext = context;
		openIdLoginListener = listener;


		
		if(profile != null)
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
			
			String profil = "";
			
			if(profile.getThumbnailImagePath()!= null)
			{
				profil = profile.getThumbnailImagePath();	
			}
			else if(profile.getProfileImagePath()!= null)
			{
				profil = profile.getProfileImagePath();	
			}
			
			
			String name = "";
			if(PatternUtil.isEmpty(profile.getNickname().trim()))
			{
				name = "NoName";	
			}
			else 
			{
				name = profile.getNickname();
			}

            CenterController.openIdLogin(Session.getCurrentSession().getAccessToken(),
                    profile.getId()+"",
                    Kakao,
                    name,
                    phone,
                    "",
                    profil,
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
			
			/*TokenApiController.openIdLogin(Session.getCurrentSession().getAccessToken(),
					profile.getId()+"",
					Kakao,
					name,
					phone,
					"",
					profil,
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
                                                    DbController.insertUser(mContext, new UserDb(data.getId(), data.getPw(),true, true,Kakao,""));
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
                                                    user.setOpenLoginType(Kakao);
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
                            DbController.updateKakaoFlag(mContext, true);
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
    
    public static void kakaoLogout(Context context)
    {
    	mContext = context;
    	Session.initializeSession(mContext, null);
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
            }

            @Override
            protected void onFailure(final APIErrorResult apiErrorResult) {
            }
        });
    }
    
}