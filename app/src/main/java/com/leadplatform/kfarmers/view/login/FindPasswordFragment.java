package com.leadplatform.kfarmers.view.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class FindPasswordFragment extends BaseFragment
{
    public static final String TAG = "FindPasswordFragment";

    private final int DISPLAY_TYPE_EMAIL = 0;
    private final int DISPLAY_TYPE_PHONE = 1;

    private int displayType = DISPLAY_TYPE_EMAIL;
    private LinearLayout findEmailLayout, findPhoneLayout;
    private TextView emailSelectText, phoneSelectText;
    private EditText findEmailIdEdit, findEmailNameEdit, findEmailEmailEdit, findPhoneIdEdit, findPhoneNameEdit, findPhonePhoneEdit,
            findPhoneAuthEdit;
    private Button findEmailBtn, findPhoneBtn, findPhoneAuthBtn;

    public static FindPasswordFragment newInstance()
    {
        final FindPasswordFragment f = new FindPasswordFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_find_password, container, false);

        findEmailLayout = (LinearLayout) v.findViewById(R.id.findEmailLayout);
        findPhoneLayout = (LinearLayout) v.findViewById(R.id.findPhoneLayout);
        emailSelectText = (TextView) v.findViewById(R.id.emailSelectText);
        phoneSelectText = (TextView) v.findViewById(R.id.phoneSelectText);
        findEmailIdEdit = (EditText) v.findViewById(R.id.findEmailIdEdit);
        findEmailNameEdit = (EditText) v.findViewById(R.id.findEmailNameEdit);
        findEmailEmailEdit = (EditText) v.findViewById(R.id.findEmailEmailEdit);
        findPhoneNameEdit = (EditText) v.findViewById(R.id.findPhoneNameEdit);
        findPhoneIdEdit = (EditText) v.findViewById(R.id.findPhoneIdEdit);
        findPhonePhoneEdit = (EditText) v.findViewById(R.id.findPhonePhoneEdit);
        findPhoneAuthEdit = (EditText) v.findViewById(R.id.findPhoneAuthEdit);
        findEmailBtn = (Button) v.findViewById(R.id.findEmailBtn);
        findPhoneBtn = (Button) v.findViewById(R.id.findPhoneBtn);
        findPhoneAuthBtn = (Button) v.findViewById(R.id.findPhoneAuthBtn);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        displayInitScreen(displayType);

        emailSelectText.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (displayType != DISPLAY_TYPE_EMAIL)
                {
                    displayType = DISPLAY_TYPE_EMAIL;
                    displayInitScreen(displayType);
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_pw-type", "이메일");
            }
        });

        phoneSelectText.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (displayType != DISPLAY_TYPE_PHONE)
                {
                    displayType = DISPLAY_TYPE_PHONE;
                    displayInitScreen(displayType);
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_pw-type", "휴대폰번호");
            }
        });

        findEmailBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof FindAccountActivity)
                {
                    String id = findEmailIdEdit.getText().toString();
                    String name = findEmailNameEdit.getText().toString();
                    String email = findEmailEmailEdit.getText().toString();

                    ((FindAccountActivity) getSherlockActivity()).onFindPwFromEmailClicked(id, name, email);

                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_pw-Find", "이메일");
                }
            }
        });

        findPhoneBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof FindAccountActivity)
                {
                    String id = findPhoneIdEdit.getText().toString();
                    String name = findPhoneNameEdit.getText().toString();
                    String phone = findPhonePhoneEdit.getText().toString();

                    ((FindAccountActivity) getSherlockActivity()).onFindPwFromPhoneClicked(id, name, phone);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_Auth", "비밀번호");
                }
            }
        });

        findPhoneAuthBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof FindAccountActivity)
                {
                    String auth = findPhoneAuthEdit.getText().toString();

                    ((FindAccountActivity) getSherlockActivity()).onFindPwFromPhoneAuthClicked(auth);

                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOST, "Click_pw-Find", "휴대폰번호");
                }
            }
        });

    }

    private void displayInitScreen(int type)
    {
        displayInitSelectBtn(type);
        displayInitEmailContent();
        displayInitPhoneContent();

        if (type == DISPLAY_TYPE_EMAIL)
        {
            findEmailLayout.setVisibility(View.VISIBLE);
            findPhoneLayout.setVisibility(View.GONE);
        }
        else if (type == DISPLAY_TYPE_PHONE)
        {
            findEmailLayout.setVisibility(View.GONE);
            findPhoneLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayInitSelectBtn(int type)
    {
        if (type == DISPLAY_TYPE_EMAIL)
        {
            emailSelectText.setTextColor(getResources().getColor(android.R.color.white));
            emailSelectText.setBackgroundResource(R.drawable.find_account_tab2_left_on);
            phoneSelectText.setTextColor(getResources().getColor(R.color.CommonConcept));
            phoneSelectText.setBackgroundResource(R.drawable.find_account_tab2_right_off);
        }
        else if (type == DISPLAY_TYPE_PHONE)
        {
            emailSelectText.setTextColor(getResources().getColor(R.color.CommonConcept));
            emailSelectText.setBackgroundResource(R.drawable.find_account_tab2_left_off);
            phoneSelectText.setTextColor(getResources().getColor(android.R.color.white));
            phoneSelectText.setBackgroundResource(R.drawable.find_account_tab2_right_on);
        }
    }

    private void displayInitEmailContent()
    {
        findEmailIdEdit.setText("");
        findEmailNameEdit.setText("");
        findEmailEmailEdit.setText("");
    }

    private void displayInitPhoneContent()
    {
        findPhoneIdEdit.setText("");
        findPhoneNameEdit.setText("");
        findPhoneAuthEdit.setText("");
        try {
        	findPhonePhoneEdit.setText(CommonUtil.AndroidUtil.getPhoneNumber(getSherlockActivity()));
		} catch (Exception e) {}
        
    }
}
