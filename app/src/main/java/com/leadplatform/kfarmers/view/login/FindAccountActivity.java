package com.leadplatform.kfarmers.view.login;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FindAccountActivity extends BaseFragmentActivity
{
    public static final String TAG = "FindAccountActivity";

    private FragmentTabHost fragmentTabHost;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_find_account);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_LOST);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View tabView1 = layoutInflater.inflate(R.layout.view_tab_id, null);
        View tabView2 = layoutInflater.inflate(R.layout.view_tab_password, null);

        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("FIND_ID").setIndicator(tabView1), FindIdFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("FIND_PASSWORD").setIndicator(tabView2), FindPasswordFragment.class, null);
        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        fragmentTabHost.getTabWidget().setStripEnabled(false);

        fragmentTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_Tab", tabId.equals("FIND_ID") ? "아이디" : "비밀번호");
            }
        });
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.LoginTextFind);
    }

    public void onCheckOpenIdEmail(final String name, final String email)
    {
    	if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidEmail(email))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_email);
            return;
        }

        UiController.hideSoftKeyboard(mContext);

        CenterController.checkOpenIdEmail(name, email, new CenterResponseListener(mContext) {
            @Override
            public void onSuccess(int Code, String content) {
                JsonNode jsonNode = null;
                try {
                    jsonNode = JsonUtil.parseTree(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (Code) {
                    case 0:
                        String str = "사용하시는 계정은 " + jsonNode.get("Data").get("Row").textValue() + " 계정 입니다." +
                                "\n\n" + jsonNode.get("Data").get("Row").textValue() + "로 로그인 해주세요.";
                        UiController.showDialog(mContext, str);
                        break;

                    case 1:
                        onFindIdFromEmailClicked(name, email);
                        break;
                }
            }
        });

        /*TokenApiController.checkOpenIdEmail(name, email, new TokenResponseListener(mContext)
        {
        	public void onSuccess(int Code, String content) 
        	{
        		JsonNode jsonNode = null;
				try {
					jsonNode = JsonUtil.parseTree(content);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		
        		switch (Code) {
				case 0:
					String str = "사용하시는 계정은 "+ jsonNode.get("rstNm").textValue() + " 입니다." +
							"\n\n"+jsonNode.get("rstNm").textValue()+" 계정으로 로그인 해주세요.";
					UiController.showDialog(mContext,str);	
					break;

				case 1:
					onFindIdFromEmailClicked(name,email);					
					break;
				}
        	}
        });*/
    }
    
    public void onCheckOpenIdPhone(final String name, final String phone)
    {
    	if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPhone(CommonUtil.PatternUtil.convertPhoneFormat(phone)))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
            return;
        }

        CenterController.checkOpenIdPhone(name, CommonUtil.PatternUtil.convertPhoneFormat(phone), new CenterResponseListener(mContext) {
            public void onSuccess(int Code, String content)
            {
                JsonNode jsonNode = null;
                try {
                    jsonNode = JsonUtil.parseTree(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (Code) {
                    case 0:
                        String str = "사용하시는 계정은 " + jsonNode.get("Data").get("Row").textValue() + " 계정 입니다." +
                                "\n\n" + jsonNode.get("Data").get("Row").textValue() + "로 로그인 해주세요.";
                        UiController.showDialog(mContext,str);
                        break;
                    case 1:
                        onFindIdFromPhoneClicked(name, phone);
                        break;
                }
            }
        });

        /*TokenApiController.checkOpenIdPhone(name, CommonUtil.PatternUtil.convertPhoneFormat(phone), new TokenResponseListener(mContext)
        {
        	public void onSuccess(int Code, String content) 
        	{
        		JsonNode jsonNode = null;
				try {
					jsonNode = JsonUtil.parseTree(content);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		
        		switch (Code) {
				case 0:
					String str = "사용하시는 계정은 "+ jsonNode.get("rstNm").textValue() + " 입니다." +
							"\n\n"+jsonNode.get("rstNm").textValue()+" 계정으로 로그인 해주세요.";
					UiController.showDialog(mContext,str);	
					break;
				case 1:
					onFindIdFromPhoneClicked(name,phone);
					break;
				}
        	}
        });*/
    }
    
    
    public void onFindIdFromEmailClicked(String name, String email)
    {
        if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidEmail(email))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_email);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findIdFromEmail(name, email, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_find_id_email, new CustomDialogListener()
                            {                                
                                @Override
                                public void onDialog(int type)
                                {
                                    finish();
                                }
                            });
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_name);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_email);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_unknown_id);
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

    public void onFindIdFromPhoneClicked(String name, String phone)
    {
        if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPhone(CommonUtil.PatternUtil.convertPhoneFormat(phone)))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findIdFromPhone(name, CommonUtil.PatternUtil.convertPhoneFormat(phone), new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_find_auth);
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_name);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_unknown_id);
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

    public void onFindIdFromPhoneAuthClicked(String auth)
    {
        if (CommonUtil.PatternUtil.isEmpty(auth))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_auth);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findIdFromPhoneAuth(auth, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                        	
                        	String id = JsonUtil.parseTree(content).get("Data").get("Row").get("ID").textValue();
                        	String meg = "아이디는 " + id + " 입니다.";
                        	UiController.showDialog(mContext,meg);
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_auth);
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

    public void onFindPwFromEmailClicked(String id, String name, String email)
    {
        if (!CommonUtil.PatternUtil.isValidId(id))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_id);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidEmail(email))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_email);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findPwFromEmail(id, name, email, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_find_pw_email, new CustomDialogListener()
                            {                                
                                @Override
                                public void onDialog(int type)
                                {
                                    finish();
                                }
                            });
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_id);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_name);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_invalid_email);
                            break;
                        case 1004:
                            UiController.showDialog(mContext, R.string.dialog_unknown_id);
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

    public void onFindPwFromPhoneClicked(String id, String name, String phone)
    {
        if (!CommonUtil.PatternUtil.isValidId(id))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_id);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidName(name))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPhone(CommonUtil.PatternUtil.convertPhoneFormat(phone)))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findPwFromPhone(id, name, CommonUtil.PatternUtil.convertPhoneFormat(phone), new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_find_auth);
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_id);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_name);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
                            break;
                        case 1004:
                            UiController.showDialog(mContext, R.string.dialog_fail_find);
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

    public void onFindPwFromPhoneAuthClicked(String auth)
    {
        if (CommonUtil.PatternUtil.isEmpty(auth))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_auth);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.findPwFromPhoneAuth(auth, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                        	String pw = JsonUtil.parseTree(content).get("Data").get("Row").get("PW").textValue();
                        	String meg = "비밀번호는 " + pw + " 입니다.";
                        	UiController.showDialog(mContext,meg);
                            /*UiController.showDialog(mContext, R.string.dialog_success_find_pw_phone, new CustomDialogListener()
                            {                                
                                @Override
                                public void onDialog(int type)
                                {
                                    finish();
                                }
                            });*/
                            break;
                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_auth);
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
}
