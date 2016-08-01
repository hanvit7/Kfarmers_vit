package com.leadplatform.kfarmers.view.join;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.model.parcel.JoinGeneralData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;

public class JoinGeneralActivity extends BaseFragmentActivity
{
    public static final String TAG = "JoinGeneralActivity";

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
            JoinGeneralFragment fragment = JoinGeneralFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, JoinGeneralFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.JoinTitleUser);
    }

    public void initActionBarRightBtnComplete()
    {
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_complete);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                JoinGeneralFragment fragment = (JoinGeneralFragment) getSupportFragmentManager().findFragmentByTag(JoinGeneralFragment.TAG);
                if (fragment != null)
                    onCompletBtnClicked(fragment.makeCompleteBtnData());
            }
        });
    }

    /***************************************************************/
    // Method
    /***************************************************************/
    public void onDuplicateBtnClicked(String id)
    {
        if (!CommonUtil.PatternUtil.isValidEmail(id))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_email);
            return;
        }

        CenterController.checkID(id, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            JoinGeneralFragment fragment = (JoinGeneralFragment) getSupportFragmentManager().findFragmentByTag(
                                    JoinGeneralFragment.TAG);
                            if (fragment != null)
                            {
                                fragment.displaySuccessCheckID();
                            }
                            break;

                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_id);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_duplicate_id);
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

    public void onCompletBtnClicked(JoinGeneralData data)
    {
        if (!data.isAgreeFlag())
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_terms);
            return;
        }
        
        /*if (CommonUtil.PatternUtil.isEmpty(data.getProfile()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_profile);
            return;
        }*/

        if (!CommonUtil.PatternUtil.isValidName(data.getName()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_name);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPhone(data.getConvertPhone()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidEmail(data.getEmail()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_email);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidEmail(data.getUserID()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_id);
            return;
        }

        if (!CommonUtil.PatternUtil.isValidPw(data.getUserPW()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
            return;
        }

        if (!data.getUserPW().equalsIgnoreCase(data.getConfirmPw()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_confirm_pw);
            return;
        }

        if (!data.isDuplicateFlag())
        {
            UiController.showDialog(mContext, R.string.dialog_run_duplicate_id);
            return;
        }

        CenterController.joinGeneral(data, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_join, new CustomDialogListener()
                            {
                                @Override
                                public void onDialog(int type)
                                {
                                    Intent intent = new Intent(JoinGeneralActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            break;
                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_name);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_invalid_email);
                            break;
                        case 1006:
                            UiController.showDialog(mContext, R.string.dialog_invalid_id);
                            break;
                        case 1007:
                            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
                            break;
                        case 1008:
                            UiController.showDialog(mContext, R.string.dialog_duplicate_id);
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
