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
import com.leadplatform.kfarmers.model.parcel.EditFarmerData;
import com.leadplatform.kfarmers.model.parcel.EditGeneralData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import org.apache.http.Header;

public class EditFarmerActivity extends BaseFragmentActivity
{
    public static final String TAG = "EditFarmerActivity";

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
            EditFarmerFragment fragment = EditFarmerFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, fragment, EditFarmerFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.EditTitleFarmer);
        initActionBarHomeBtn();
    }

    public void initActionBarRightBtnNext()
    {
        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_next);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                EditFarmerFragment fragment = (EditFarmerFragment) getSupportFragmentManager().findFragmentByTag(EditFarmerFragment.TAG);
                if (fragment != null)
                    onNextBtnClicked(fragment.makeNextBtnData(), fragment.makeUserInfoData(),fragment.mBankMaster.getText().toString(),fragment.mBankName.getText().toString(),fragment.mBankNo.getText().toString(),fragment.mLicenseeNo.getText().toString(),fragment.mLicenseeSellNo.getText().toString());
            }
        });
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
                EditFarmerNextFragment fragment = (EditFarmerNextFragment) getSupportFragmentManager().findFragmentByTag(EditFarmerNextFragment.TAG);
                if (fragment != null)
                    onCompletBtnClicked(fragment.makeCompleteBtnData());
            }
        });
    }

    /***************************************************************/
    // Method
    /***************************************************************/

    /**
     * <p>XXX - XX - XXXXX 형식의 사업자번호 앞,중간, 뒤 문자열 3개 입력 받아 유효한 사업자번호인지 검사.</p>
     *
     *
     * @param   3자리 사업자앞번호 문자열 , 2자리 사업자중간번호 문자열, 5자리 사업자뒷번호 문자열
     * @return  유효한 사업자번호인지 여부 (True/False)
     */
    public static boolean checkCompNumber(String no) {

        String compNumber = no;

        int hap = 0;
        int temp = 0;
        int check[] = {1,3,7,1,3,7,1,3,5};  //사업자번호 유효성 체크 필요한 수

        if(compNumber.length() != 10)    //사업자번호의 길이가 맞는지를 확인한다.
            return false;

        for(int i=0; i < 9; i++){
            if(compNumber.charAt(i) < '0' || compNumber.charAt(i) > '9')  //숫자가 아닌 값이 들어왔는지를 확인한다.
                return false;

            hap = hap + (Character.getNumericValue(compNumber.charAt(i)) * check[temp]); //검증식 적용
            temp++;
        }

        hap += (Character.getNumericValue(compNumber.charAt(8))*5)/10;

        if ((10 - (hap%10))%10 == Character.getNumericValue(compNumber.charAt(9))) //마지막 유효숫자와 검증식을 통한 값의 비교
            return true;
        else
            return false;
    }

    public void onNextBtnClicked(final EditGeneralData data, final String userInfo, String bankMaster,String bankName,String bankNo,String licenseeNo,String licenseeSellNo)
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_NextEdit", null);

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

        String bankAccount = null;

        if(bankMaster.trim().isEmpty() && bankName.trim().isEmpty() && bankNo.trim().isEmpty())
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_bank_accrount);
            return;
        } else if(!bankMaster.trim().isEmpty() && !bankName.trim().isEmpty() && !bankNo.trim().isEmpty())
        {
            bankAccount = bankMaster.trim() + ":" + bankName.trim() + ":" + bankNo.trim();
        } else if(!bankMaster.trim().isEmpty() || !bankName.trim().isEmpty() || !bankNo.trim().isEmpty())
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_bank_accrount);
            return;
        }

        if (userPwFlag && (CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()) || CommonUtil.PatternUtil.isEmpty(data.getUserNewPW())))
        {
            UiController.showDialog(mContext, R.string.dialog_invalid_pw);
            return;
        }


        CenterController.editFarmer(data,bankAccount,licenseeNo,licenseeSellNo, new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            EditFarmerNextFragment fragment = EditFarmerNextFragment.newInstance(userInfo);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.add(R.id.fragment_container, fragment, EditFarmerNextFragment.TAG);
                            ft.addToBackStack(null);
                            ft.commit();
                            requestUserProfile(false);
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

    public void onCompletBtnClicked(EditFarmerData data)
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EDIT_FARMER, "Click_CompletEdit", null);

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

        CenterController.editFarmerNext(data, new CenterResponseListener(this)
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
                                    requestUserProfile(true);
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

    private void requestUserProfile(final boolean isFinish)
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
                            break;

                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            break;
                    }

                    if(isFinish)
                    {
                        finish();
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    if(isFinish)
                    {
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                if(isFinish)
                {
                    finish();
                }
            }
        });
    }
}
