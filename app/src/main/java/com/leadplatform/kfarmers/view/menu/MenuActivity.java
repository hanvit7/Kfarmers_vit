package com.leadplatform.kfarmers.view.menu;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.Session;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;

import org.apache.http.Header;

public class MenuActivity extends BaseFragmentActivity {
	public static final String TAG = "MenuActivity";

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_base);

		if (AppPreferences.getLogin(this)) {

			try 
			{
				String profile = DbController.queryProfileContent(this);
				JsonNode root = JsonUtil.parseTree(profile);
				String type = root.findValue("Type").textValue();
				
				if (type.equals("F")) 
				{
					MenuFarmerFragment fragment = MenuFarmerFragment.newInstance(profile);
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.add(R.id.fragment_container, fragment, MenuFarmerFragment.TAG);
					ft.commit();	
				}
				else if (type.equals("V")) 
				{
					MenuVillageFragment fragment = MenuVillageFragment.newInstance(profile);
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.add(R.id.fragment_container, fragment, MenuVillageFragment.TAG);
					ft.commit();
				}
				else
				{
					MenuUserFragment fragment = MenuUserFragment.newInstance(profile);
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.add(R.id.fragment_container, fragment, MenuUserFragment.TAG);
					ft.commit();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else
		{
			finish();
		}
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText(R.string.mypage_title);
		initActionBarHomeBtn();
	}
	
	public void logout()
    {
		String regId = AppPreferences.getGcmRegistrationId(mContext);
        
        CenterController.logout(regId,new CenterResponseListener(mContext)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            AppPreferences.setLogin(mContext, false);
                            UserDb user = DbController.queryCurrentUser(mContext);
                            if (user != null)
                            {
                                DbController.updateUser(mContext, user);
                                
                                /*if(user.getOpenLoginType() != null && user.getOpenLoginType().equals(NaverLoginHelper.Naver))
                				{
                                	NaverLoginHelper.naverLogout(mContext);
                				}
                                else if(user.getOpenLoginType() != null && user.getOpenLoginType().equals(KakaoLoginHelper.Kakao))
                				{
                                	KakaoLoginHelper.kakaoLogout(mContext);
                				}*/
                            }
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }

                            NaverLoginHelper.naverLogout(mContext);

                            Session.initializeSession(mContext, null);
                            Session.getCurrentSession().close(null);

                            DbController.updateCurrentUser(mContext, "");
                            AppPreferences.setGcmSend(mContext, false);
                            DbController.clearDb(mContext);

                            SnipeApiController.tokenExpired(new SnipeResponseListener(mContext) {
                                @Override
                                public void onSuccess(int Code, String content, String error) {
                                    getToken();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                                    super.onFailure(statusCode, headers, content, error);
                                    getToken();
                                }
                            });

                            break;

                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void getToken()
    {
        String id ="";
        String pw ="";

        UserDb user = DbController.queryCurrentUser(mContext);
        if (user != null && user.getAutoLoginFlag() == 1)
        {
            id = user.getUserID();
            pw = user.getUserPwDecrypt();
        }

        SnipeApiController.getToken(id,pw,new SnipeResponseListener(mContext) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                switch (Code)
                {
                    case 600:
                        DbController.updateApiToken(mContext,content);
                        SnipeApiController.setToken(content);
                        runMainActivity();
                        finish();
                        break;
                    default:
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                        DbController.updateApiToken(mContext,content);
                        SnipeApiController.setToken(content);
                        runMainActivity();
                        finish();
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
