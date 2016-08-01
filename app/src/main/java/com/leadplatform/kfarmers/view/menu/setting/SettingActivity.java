package com.leadplatform.kfarmers.view.menu.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.menu.invite.InviteActivity;
import com.leadplatform.kfarmers.view.menu.notice.FarmNoticeActivity;

public class SettingActivity extends BaseFragmentActivity
{
    public static final String TAG = "SettingActivity";

    private TextView versionText;
    private LinearLayout noticeBtn, serviceCenterBtn, snsBtn, addressBtn, inviteBtn;
    private Button logoutBtn,leaveBtn;
    private CheckBox commentCheck;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_setting);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SETTING, null);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        noticeBtn = (LinearLayout) findViewById(R.id.setting_notice_btn);
        serviceCenterBtn = (LinearLayout) findViewById(R.id.setting_service_center_btn);
        snsBtn = (LinearLayout) findViewById(R.id.setting_sns_btn);
        //addressBtn = (LinearLayout) findViewById(R.id.setting_address_btn);
        commentCheck = (CheckBox) findViewById(R.id.setting_comment_alarm);
        versionText = (TextView) findViewById(R.id.current_version);
        inviteBtn = (LinearLayout) findViewById(R.id.setting_invite_btn);
        logoutBtn = (Button) findViewById(R.id.logout);
        leaveBtn = (Button) findViewById(R.id.leave);

        leaveBtn.setVisibility(View.GONE);

        if (AppPreferences.getLogin(this)) {
            logoutBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            logoutBtn.setVisibility(View.GONE);
        }

        logoutBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_LogOut", null);
                ((BaseFragmentActivity) mContext).logout();
            }
        });

        leaveBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                    ((BaseFragmentActivity) mContext).leaveMember();
            }
        });

        versionText.setText("v" + CommonUtil.AndroidUtil.getAppVersion(this));

        /*addressBtn.setOnClickListener(new ViewOnClickListener() {

            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AddressActivity.class);
                startActivity(intent);
            }
        });*/

        noticeBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_Notice", null);
                onNoticeBtnClicked();
            }
        });

        inviteBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_Invite", null);
                Intent intent = new Intent(SettingActivity.this, InviteActivity.class);
                startActivity(intent);
            }
        });

        serviceCenterBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_ServiceCenter", null);
                onServiceCenterBtnClicked();
            }
        });

        snsBtn.setVisibility(View.GONE);

        if (AppPreferences.getLogin(SettingActivity.this))
        {
            try
            {
                String profile = DbController.queryProfileContent(SettingActivity.this);
                JsonNode root = JsonUtil.parseTree(profile);
                ProfileJson profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);

                if(profileJson.Type.equals("U") || profileJson.TemporaryPermissionFlag.equals("N"))
                {
                    snsBtn.setVisibility(View.GONE);
                }
                else
                {
                    if(profileJson.PermissionFlag.equals("N")) {
                        snsBtn.setOnClickListener(new ViewOnClickListener()
                        {
                            @Override
                            public void viewOnClick(View v)
                            {
                                UiController.showDialog(mContext, R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
                                        new CustomDialogListener() {
                                            @Override
                                            public void onDialog(int type) {
                                                if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                                    CommonUtil.AndroidUtil.actionDial(mContext, getResources().getString(R.string.setting_service_center_phone));
                                                }
                                            }
                                        });
                            }
                        });
                    } else {
                        snsBtn.setOnClickListener(new ViewOnClickListener()
                        {
                            @Override
                            public void viewOnClick(View v)
                            {
                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_Sns", null);
                                onSnsBtnClicked();
                            }
                        });
                    }
                    snsBtn.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){}
        }

        commentCheck.setChecked(AppPreferences.getGcmEnable(mContext));
        
        commentCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SETTING, "Click_Comment", isChecked == true ? "true":"false");

                AppPreferences.setGcmEnable(SettingActivity.this, isChecked);

                //if (isChecked && AppPreferences.getLogin(SettingActivity.this))
                // {
                String regID = AppPreferences.getGcmRegistrationId(SettingActivity.this);
                if (!TextUtils.isEmpty(regID)) {
                    CenterController.setDeviceAddress(regID, "Android", (AppPreferences.getGcmEnable(SettingActivity.this)) ? "Y" : "N",
                            new CenterResponseListener(SettingActivity.this) {
                                @Override
                                public void onSuccess(int Code, String content) {
                                    try {
                                        switch (Code) {
                                            case 0000:
                                                break;

                                            default:
                                                UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                                break;
                                        }
                                    } catch (Exception e) {
                                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                    }
                                }
                            });
                }

                //}
            }
        });
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.setting_title);
        initActionBarHomeBtn();
    }
    private void onNoticeBtnClicked()
    {
        Intent intent = new Intent(this, FarmNoticeActivity.class);
        intent.putExtra("userType", "G");
        intent.putExtra("userIndex", "");
        intent.putExtra("noticeIndex", "");
        startActivity(intent);
    }

    private void onServiceBtnClicked()
    {
        SettingServiceFragment fragment = SettingServiceFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, SettingServiceFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void onServiceCenterBtnClicked()
    {
        CommonUtil.AndroidUtil.actionDial(this, getResources().getString(R.string.setting_service_center_phone));
    }

    private void onSnsBtnClicked()
    {
        if (AppPreferences.getLogin(SettingActivity.this))
        {
            Intent intent = new Intent(this, SettingSNSActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}