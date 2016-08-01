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
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.model.parcel.EditGeneralData;
import com.leadplatform.kfarmers.model.parcel.EditVillageData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class EditVillageActivity extends BaseFragmentActivity
{
    public static final String TAG = "EditVillageActivity";

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
            EditVillageFragment fragment = EditVillageFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, fragment, EditVillageFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.EditTitleVillage);
        initActionBarHomeBtn();
    }

    public void initActionBarRightBtnNext()
    {
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_next);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                EditVillageFragment fragment = (EditVillageFragment) getSupportFragmentManager().findFragmentByTag(EditVillageFragment.TAG);
                if (fragment != null)
                    onNextBtnClicked(fragment.makeNextBtnData(), fragment.makeUserInfoData());
            }
        });
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
                EditVillageNextFragment fragment = (EditVillageNextFragment) getSupportFragmentManager().findFragmentByTag(EditVillageNextFragment.TAG);
                if (fragment != null)
                    onCompletBtnClicked(fragment.makeCompleteBtnData());
            }
        });
    }
    /***************************************************************/
    // Method
    /***************************************************************/
    public void onNextBtnClicked(final EditGeneralData data, final String userInfo)
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_NextEdit", null);

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

        CenterController.editVillage(data, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            EditVillageNextFragment fragment = EditVillageNextFragment.newInstance(userInfo);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.add(R.id.fragment_container, fragment, EditVillageNextFragment.TAG);
                            ft.addToBackStack(null);
                            ft.commit();
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

    public void onCompletBtnClicked(EditVillageData data)
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_VILLAGE, "Click_CompletEdit", null);

        if (CommonUtil.PatternUtil.isEmpty(data.getFarm()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_farm);
            return;
        }

        if (CommonUtil.PatternUtil.isEmpty(data.getAddress()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_address);
            return;
        }

        if (CommonUtil.PatternUtil.isEmpty(data.getIntroduction()))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_introduction);
            return;
        }

        CenterController.editVillageNext(data, new CenterResponseListener(this)
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
                                    finish();
                                }
                            });
                            break;
                        case 1001:
                            UiController.showDialog(mContext, R.string.dialog_invalid_farm);
                            break;
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_address);
                            break;
                        case 1003:
                            UiController.showDialog(mContext, R.string.dialog_invalid_introduction);
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
