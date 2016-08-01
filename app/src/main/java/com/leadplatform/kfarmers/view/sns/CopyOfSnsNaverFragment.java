package com.leadplatform.kfarmers.view.sns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class CopyOfSnsNaverFragment extends BaseFragment
{
    public static final String TAG = "SnsNaverFragment";

    private EditText naverIdEdit, naverApiEdit;
    private Button nextBtn;

    public static CopyOfSnsNaverFragment newInstance()
    {
        final CopyOfSnsNaverFragment f = new CopyOfSnsNaverFragment();
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
        final View v = inflater.inflate(R.layout.fragment_sns_naver, container, false);

        naverIdEdit = (EditText) v.findViewById(R.id.naverIdEdit);
        naverApiEdit = (EditText) v.findViewById(R.id.naverApiEdit);
        nextBtn = (Button) v.findViewById(R.id.nextBtn);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        nextBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                String id = naverIdEdit.getText().toString();
                String api = naverApiEdit.getText().toString();
                onSnsNaverBtnClicked(id, api);
            }
        });
    }

    public void onSnsNaverBtnClicked(final String id, final String api)
    {
        if (CommonUtil.PatternUtil.isEmpty(id))
        {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_id);
            return;
        }

        if (CommonUtil.PatternUtil.isEmpty(api))
        {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_api);
            return;
        }

        UiController.hideSoftKeyboard(getSherlockActivity());
        CenterController.snsNaverBlog(id, api, new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            //DbController.updateNaverSession(getSherlockActivity(), id, api);
                            ((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
                            break;

                        case 1001:
                        case 1002:
                        case 1003:
                            ((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
                            break;

                        default:
                            ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                            break;
                    }
                }
                catch (Exception e)
                {
                    ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                }
            }
        });
    }
}
