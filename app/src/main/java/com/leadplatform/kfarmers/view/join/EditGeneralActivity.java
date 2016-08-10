package com.leadplatform.kfarmers.view.join;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.model.parcel.EditGeneralData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class EditGeneralActivity extends BaseFragmentActivity
{
    public static final String TAG = "EditGeneralActivity";

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
            EditGeneralFragment fragment = EditGeneralFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, EditGeneralFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.EditTitleUser);
        initActionBarHomeBtn();
    }

    public void initActionBarRightBtnComplete()
    {
        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_complete);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                EditGeneralFragment fragment = (EditGeneralFragment) getSupportFragmentManager().findFragmentByTag(EditGeneralFragment.TAG);
                if (fragment != null)
                    onCompletBtnClicked(fragment.makeCompleteBtnData());
            }
        });
    }
    /***************************************************************/
    // Method
    /***************************************************************/
    public void onCompletBtnClicked(EditGeneralData data)
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_USER, "Click_CompletEdit", null);
        boolean userPwFlag = false;

        UiController.hideSoftKeyboard(mContext);
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

        if (!CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()))
        {
            userPwFlag = true;
            if (!CommonUtil.PatternUtil.isValidPw(data.getUserOldPw()))
            {
                UiController.showDialog(mContext, R.string.dialog_invalid_pw);
                return;
            }
        }

        if (!CommonUtil.PatternUtil.isEmpty(data.getUserNewPW()))
        {
            userPwFlag = true;
            if (!CommonUtil.PatternUtil.isValidPw(data.getUserNewPW()))
            {
                UiController.showDialog(mContext, R.string.dialog_invalid_pw);
                return;
            }
        }

        if (userPwFlag && (CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()) || CommonUtil.PatternUtil.isEmpty(data.getUserNewPW())))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
            return;
        }

        CenterController.editGeneral(data, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            UiController.showDialog(mContext, R.string.dialog_success_edit, new CustomDialogListener()
                            {
                                @Override
                                public void onDialog(int type)
                                {
                                	requestUserProfile();
                                }
                            });
                            break;
                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_phone);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_email);
                            break;
                        case 1006:
                            UiController.showDialog(mContext, R.string.dialog_invalid_old_pw);
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
                            DbController.updateProfileContent(mContext, content);
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
}
