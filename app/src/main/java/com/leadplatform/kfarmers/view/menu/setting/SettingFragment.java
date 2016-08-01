package com.leadplatform.kfarmers.view.menu.setting;

import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.Session;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.address.AddressActivity;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseMenuFragmentActivity;
import com.leadplatform.kfarmers.view.login.KakaoLoginHelper;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;
import com.leadplatform.kfarmers.view.menu.MenuActivity;
import com.leadplatform.kfarmers.view.menu.invite.InviteActivity;
import com.leadplatform.kfarmers.view.menu.notice.FarmNoticeActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class SettingFragment extends BaseFragment
{
    public static final String TAG = "SettingFragment";

    private TextView versionText, loginText,loginTextMode;
    private RelativeLayout logoutBtn;
    private LinearLayout noticeBtn, serviceBtn, serviceCenterBtn, snsBtn, qaBtn,addressBtn,inviteBtn,orderListBtn;
    private CheckBox noticeCheck, eventCheck, releaseCheck, commentCheck;
    private ImageView profileImage;

    /***************************************************************/
    // Override
    /***************************************************************/
    
    public static SettingFragment newInstance()
    {
        final SettingFragment f = new SettingFragment();
        return f;
    }
    
    /*@Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_setting);

        initContentView(savedInstanceState);
    }*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	final View v = inflater.inflate(R.layout.activity_setting, container, false);
    	
    	profileImage = (ImageView) v.findViewById(R.id.profileImage);
    	/*loginText = (TextView) v.findViewById(R.id.setting_login_text);
        loginTextMode= (TextView) v.findViewById(R.id.setting_login_text_mode);
        logoutBtn = (RelativeLayout) v.findViewById(R.id.setting_logout_btn);
        noticeBtn = (LinearLayout) v.findViewById(R.id.setting_notice_btn);
        serviceBtn = (LinearLayout) v.findViewById(R.id.setting_service_btn);*/
        serviceCenterBtn = (LinearLayout) v.findViewById(R.id.setting_service_center_btn);
        inviteBtn = (LinearLayout) v.findViewById(R.id.setting_invite_btn);
        snsBtn = (LinearLayout) v.findViewById(R.id.setting_sns_btn);
        // qaBtn = (LinearLayout) findViewById(R.id.setting_qa_btn);
        // noticeCheck = (CheckBox) findViewById(R.id.setting_notice_alarm);
        // eventCheck = (CheckBox) findViewById(R.id.setting_event_alarm);
        // releaseCheck = (CheckBox) findViewById(R.id.setting_release_alarm);
        commentCheck = (CheckBox) v.findViewById(R.id.setting_comment_alarm);
        versionText = (TextView) v.findViewById(R.id.current_version);

        /*addressBtn = (LinearLayout) v.findViewById(R.id.setting_address);

        orderListBtn = (LinearLayout) v.findViewById(R.id.setting_order_list);*/

        versionText.setText("v" + CommonUtil.AndroidUtil.getAppVersion(getSherlockActivity()));

        if (AppPreferences.getLogin(getSherlockActivity()))
        {
        	try
        	{
        		addressBtn.setVisibility(View.VISIBLE);
        		profileImage.setVisibility(View.VISIBLE);
                orderListBtn.setVisibility(View.VISIBLE);
                loginTextMode.setVisibility(View.VISIBLE);
        		
	        	DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
	    				.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 60) / 2)).build();
	        	
				String profile = DbController.queryProfileContent(getSherlockActivity());
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				if (!PatternUtil.isEmpty(profileJson.ProfileImage)) {
	    			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(profileJson.ProfileImage, profileImage, options);
	    		}
				
				if(profileJson.Type.equals("U"))
				{
					snsBtn.setVisibility(View.GONE);
					loginText.setText(profileJson.Name);
                    loginTextMode.setText("내 정보 관리");
				}
				else
				{
					loginText.setText(profileJson.Farm);
                    loginTextMode.setText("내 농장 관리");

					snsBtn.setVisibility(View.VISIBLE);
					snsBtn.setOnClickListener(new ViewOnClickListener()
			        {
			            @Override
			            public void viewOnClick(View v)
			            {
			                onSNSBtnClicked();
			                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
			            }
			        });
				}
        	}catch (Exception e) {
				e.printStackTrace();
			}
        }
        else
        {
            orderListBtn.setVisibility(View.GONE);
        	addressBtn.setVisibility(View.GONE);
        	snsBtn.setVisibility(View.GONE);
        	profileImage.setVisibility(View.GONE);
            loginText.setText(R.string.LoginTitle);
            loginTextMode.setVisibility(View.GONE);
        }

        logoutBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (AppPreferences.getLogin(getSherlockActivity()))
                {
                    //onLogoutBtnClicked();
                	getSherlockActivity().startActivity(new Intent(getSherlockActivity(),MenuActivity.class));
                }
                else
                {
                    Intent intent = new Intent(getSherlockActivity(), LoginActivity.class);
                    startActivity(intent);
                }
                
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });

        noticeBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onNoticeBtnClicked();
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });

        serviceBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onServiceBtnClicked();
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });

        serviceCenterBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onServiceCenterBtnClicked();
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });
        
        addressBtn.setOnClickListener(new ViewOnClickListener() {
			
			@Override
			public void viewOnClick(View v) {

				Intent intent = new Intent(getSherlockActivity(), AddressActivity.class);
                startActivity(intent);
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
			}
		});

        inviteBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getSherlockActivity(), InviteActivity.class);
                startActivity(intent);
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });

        orderListBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(getSherlockActivity(), OrderActivity.class);
                startActivity(intent);
                ((BaseMenuFragmentActivity) getSherlockActivity()).toggle();
            }
        });


        // qaBtn.setOnClickListener(new ViewOnClickListener()
        // {
        // @Override
        // public void viewOnClick(View v)
        // {
        // onQABtnClicked();
        // }
        // });

        
        commentCheck.setChecked(AppPreferences.getGcmEnable(getSherlockActivity()));
        
        commentCheck.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                AppPreferences.setGcmEnable(getSherlockActivity(), isChecked);

                //if (isChecked && AppPreferences.getLogin(SettingActivity.this))
               // {
                    String regID = AppPreferences.getGcmRegistrationId(getSherlockActivity());
                    if (!TextUtils.isEmpty(regID))
                    {
                        CenterController.setDeviceAddress(regID, "Android", (AppPreferences.getGcmEnable(getSherlockActivity())) ? "Y" : "N",
                                new CenterResponseListener(getSherlockActivity())
                                {
                                    @Override
                                    public void onSuccess(int Code, String content)
                                    {
                                        try
                                        {
                                            switch (Code)
                                            {
                                                case 0000:
                                                    break;

                                                default:
                                                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                                    break;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                        }
                                    }
                                });
                    }

                //}
            }
        });
    	
		return v;
    	
    }

    private void onLogoutBtnClicked()
    {
        logout();   
    }

    private void onNoticeBtnClicked()
    {
        Intent intent = new Intent(getSherlockActivity(), FarmNoticeActivity.class);
        intent.putExtra("userType", "G");
        intent.putExtra("userIndex", "");
        intent.putExtra("noticeIndex", "");
        startActivity(intent);
    }

    private void onServiceBtnClicked()
    {
      /*  SettingServiceFragment fragment = SettingServiceFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, SettingServiceFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();*/
    	Intent intent = new Intent(getSherlockActivity(), SettingServiceActivity.class);
        startActivity(intent);
    }

    private void onServiceCenterBtnClicked()
    {
        CommonUtil.AndroidUtil.actionDial(getSherlockActivity(), getResources().getString(R.string.setting_service_center_phone));
    }

    private void onSNSBtnClicked()
    {
        if (AppPreferences.getLogin(getSherlockActivity()))
        {
            Intent intent = new Intent(getSherlockActivity(), SettingSNSActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(getSherlockActivity(), LoginActivity.class);
            startActivity(intent);
        }

    }

    private void onQABtnClicked()
    {

    }

    private void logout()
    {
    	String regId = AppPreferences.getGcmRegistrationId(getSherlockActivity());
        
        CenterController.logout(regId,new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            AppPreferences.setLogin(getSherlockActivity(), false);
                            UserDb user = DbController.queryCurrentUser(getSherlockActivity());
                            if (user != null)
                            {
                                DbController.updateUser(getSherlockActivity(), user);
                                
                                if(user.getOpenLoginType() != null && user.getOpenLoginType().equals(NaverLoginHelper.Naver))
                				{
                                	NaverLoginHelper.naverLogout(getSherlockActivity());
                				}
                                else if(user.getOpenLoginType() != null && user.getOpenLoginType().equals(KakaoLoginHelper.Kakao))
                				{
                                	KakaoLoginHelper.kakaoLogout(getSherlockActivity());
                				}
                            }
                            DbController.updateCurrentUser(getSherlockActivity(), "");
                            AppPreferences.setGcmSend(getSherlockActivity(), false);
                            DbController.clearDb(getSherlockActivity());
                            
                            Session.initializeSession(getSherlockActivity(), null);
                            Session.getCurrentSession().close(null);

                            
                            //runMainActivity();
                            //finish();
                            break;

                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }
}
