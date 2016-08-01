package com.leadplatform.kfarmers.view.join;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.UserProfile;
import com.kakao.exception.KakaoException;
import com.kakao.widget.LoginButton;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.KakaoLoginHelper;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;
import com.leadplatform.kfarmers.view.login.OpenIdLoginListener;

public class JoinGeneralDialogFragment extends BaseDialogFragment
{
    public static final String TAG = "JoinGeneralDialogFragment";

    private View mLoginView;
    private LoginButton loginButton;
    private RelativeLayout mEmailLogin;
    private final SessionCallback mySessionCallback = new MySessionStatusCallback();

    private Button cancelBtn;

    public static JoinGeneralDialogFragment newInstance()
    {
        final JoinGeneralDialogFragment f = new JoinGeneralDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_join_general_dialog, container, false);

        cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        mLoginView = v.findViewById(R.id.loginView);
        mEmailLogin = (RelativeLayout) v.findViewById(R.id.EmailLogin);

        mEmailLogin.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_General_Email", null);
                ((LoginActivity)getActivity()).onJoinDialogGeneralClicked();
            }
        });

        RelativeLayout naverBtn = (RelativeLayout) v.findViewById(R.id.naverLayout);

        naverBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Naver", null);
                mLoginView.setVisibility(View.VISIBLE);
                NaverLoginHelper.NaverLogin(getActivity(), new OpenIdLoginListener() {
                    @Override
                    public void onResult(boolean isSuccess, String content) {
                        if (isSuccess) {
                            NaverLoginHelper.openIdRegister(getActivity(), content, new OpenIdLoginListener() {

                                @Override
                                public void onResult(boolean isSuccess, String content) {

                                    if (isSuccess) {
                                        ((BaseFragmentActivity) getActivity()).runMainActivity();
                                        getActivity().finish();
                                    } else {
                                        UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
                                            @Override
                                            public void onDialog(int type) {
                                            }
                                        });
                                        mLoginView.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            mLoginView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        loginButton = (LoginButton) v.findViewById(R.id.com_kakao_login);
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // 세션을 초기화 한다
        if (Session.initializeSession(getSherlockActivity(), mySessionCallback))
        {
            UiController.showProgressDialog(getSherlockActivity());
            mLoginView.setVisibility(View.VISIBLE);
        }
        else if (Session.getCurrentSession().isOpened())
        {
            mLoginView.setVisibility(View.VISIBLE);
            onSessionOpened();

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        /*generalBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    ((LoginActivity) getSherlockActivity()).onJoinDialogGeneralClicked();
                }
                dismiss();
            }
        });

        farmerBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    ((LoginActivity) getSherlockActivity()).onJoinDialogFarmerClicked();
                }
                dismiss();
            }
        });

        villageBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    ((LoginActivity) getSherlockActivity()).onJoinDialogVillageClicked();
                }
                dismiss();
            }
        });*/

        cancelBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                dismiss();
            }
        });
    }

    private class MySessionStatusCallback implements SessionCallback
    {
        /**
         * 세션이 오픈되었으면 가입페이지로 이동 한다.
         */
        @Override
        public void onSessionOpened()
        {
            mLoginView.setVisibility(View.VISIBLE);
            UiController.hideProgressDialog(getSherlockActivity());
            JoinGeneralDialogFragment.this.onSessionOpened();
        }

        /**
         * 세션이 삭제되었으니 로그인 화면이 보여야 한다.
         *
         * @param exception
         *            에러가 발생하여 close가 된 경우 해당 exception
         */
        @Override
        public void onSessionClosed(final KakaoException exception)
        {
            mLoginView.setVisibility(View.GONE);
            UiController.hideProgressDialog(getSherlockActivity());
            DbController.updateKakaoFlag(getActivity(), false);
        }
    }

    protected void onSessionOpened()
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Kakao", null);
        KakaoLoginHelper.requestMe(new OpenIdLoginListener() {

            @Override
            public void onResult(boolean isSuccess, String content) {

                if (isSuccess) {
                    KakaoLoginHelper.openIdRegister(getSherlockActivity(), UserProfile.loadFromCache(), new OpenIdLoginListener() {

                        @Override
                        public void onResult(boolean isSuccess, String content) {
                            if (isSuccess) {
                                ((BaseFragmentActivity) getActivity()).runMainActivity();
                                getActivity().finish();
                            } else {
                                UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
                                    @Override
                                    public void onDialog(int type) {
                                    }
                                });
                                mLoginView.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
                        @Override
                        public void onDialog(int type) {
                        }
                    });
                    mLoginView.setVisibility(View.GONE);
                }
            }
        });
    }
}
