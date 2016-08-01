package com.leadplatform.kfarmers.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.*;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.parcel.LoginData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.DataCryptUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.join.*;

public class LoginActivity extends BaseFragmentActivity
{
    public static final String TAG = "LoginActivity";

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            LoginData data = new LoginData();
            data.setId("");
            data.setPw("");
            data.setbAuto(true);

            LoginFragment fragment = LoginFragment.newInstance(data);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, fragment, LoginFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.LoginTitle);
    }

    /***************************************************************/
    // Display
    /***************************************************************/

    /***************************************************************/
    // Method
    /***************************************************************/
    public void onFindTextClicked()
    {
        startActivity(new Intent(LoginActivity.this, FindAccountActivity.class));
    }

    public void onLoginBtnClicked(final LoginData data)
    {
        if (data.getId() == null && data.getId().trim().length() == 0)
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_id);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPw(data.getPw()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
            return;
        }

        UiController.hideSoftKeyboard(mContext);
        CenterController.login(data, new CenterResponseListener(this)
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
                                                    DbController.insertUser(mContext, new UserDb(data.getId(), data.getPw(), data.isbAuto(), true, "", ""));
                                                } else {
                                                    String pw = "";
                                                    try {
                                                        pw = DataCryptUtil.encrypt(DataCryptUtil.dataK, data.getPw());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    user.setUserID(data.getId());
                                                    user.setUserPW(pw);
                                                    user.setAutoLoginFlag(data.isbAuto() ? 1 : 0);
                                                    user.setOpenLoginType("");
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
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_fail_login);
                            break;
                        case 1004:
                            /*UiController.showDialog(mContext,R.string.dialog_wait_approve);

                            AppPreferences.setLogin(LoginActivity.this, true);
                            user = DbController.queryUser(LoginActivity.this, data.getId());
                            if (user == null)
                            {
                                DbController.insertUser(LoginActivity.this, new UserDb(data.getId(), data.getPw(), data.isbAuto(), true,""));
                            }
                            else
                            {
                                user.setUserID(data.getId());
                                user.setUserPW(data.getPw());
                                user.setAutoLoginFlag(data.isbAuto() ? 1 : 0);
                                user.setOpenLoginType("");
                                DbController.updateUser(LoginActivity.this, user);
                            }
                            DbController.updateCurrentUser(LoginActivity.this, data.getId());
                            requestUserProfile();*/

                            UiController.showDialog(mContext, R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
                                    new CustomDialogListener()
                                    {
                                        @Override
                                        public void onDialog(int type)
                                        {
                                            if (type == UiDialog.DIALOG_POSITIVE_LISTENER)
                                            {
                                                CommonUtil.AndroidUtil.actionDial(LoginActivity.this, getResources().getString(R.string.setting_service_center_phone));
                                            }
//                                            else
//                                            {
//                                                finish();
//                                            }
                                        }
                                    });
                            // UiController.showDialog(mContext, R.string.dialog_wait_approve);
                            break;
                        case 1005:
                            UiController.showDialog(mContext, R.string.dialog_expired_date);
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

    public void onJoinFarmerBtnClicked()
    {
        UiController.hideSoftKeyboard(mContext);
        JoinFarmerDialogFragment fragment = JoinFarmerDialogFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, JoinFarmerDialogFragment.TAG);
    }


    public void onJoiGeneralBtnClicked()
    {
        UiController.hideSoftKeyboard(mContext);
        JoinGeneralDialogFragment fragment = JoinGeneralDialogFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, JoinGeneralDialogFragment.TAG);
    }

    public void onJoinDialogGeneralClicked()
    {
        startActivity(new Intent(mContext, JoinGeneralActivity.class));
    }

    public void onJoinDialogFarmerClicked()
    {
        startActivity(new Intent(mContext, JoinFarmerActivity.class));
    }

    public void onJoinDialogVillageClicked()
    {
        startActivity(new Intent(mContext, JoinVillageActivity.class));
    }

    private void requestUserProfile()
    {
        CenterController.getProfile(new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            DbController.updateProfileContent(LoginActivity.this, content);
                            requestDeviceAddress();
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

    private void requestDeviceAddress()
    {
        String regID = AppPreferences.getGcmRegistrationId(this);
        if (!TextUtils.isEmpty(regID))
        {
            CenterController.setDeviceAddress(regID, "Android", (AppPreferences.getGcmEnable(this)) ? "Y" : "N", new CenterResponseListener(this)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                //runMainActivity();
                                finish();
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
            runMainActivity();
            finish();
        }
    }
}
