package com.leadplatform.kfarmers.view.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
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
import com.leadplatform.kfarmers.model.parcel.LoginData;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class LoginFragment extends BaseFragment
{
    public static final String TAG = "LoginFragment";

    private LoginData loginData;
    private EditText idEdit;
    private EditText pwEdit;
    private CheckBox autoCheckBox;
    private TextView findText;
    private Button loginBtn;

    public View loginView;

    private Button mFarmerJoin;
    private Button mGeneralJoin;


    private LoginButton loginButton;
    private final SessionCallback mySessionCallback = new MySessionStatusCallback();

    public static LoginFragment newInstance(LoginData data)
    {
        final LoginFragment f = new LoginFragment();

        final Bundle args = new Bundle();
        args.putParcelable(LoginData.KEY, data);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            loginData = getArguments().getParcelable(LoginData.KEY);
        }

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_LOGIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_login, container, false);

        idEdit = (EditText) v.findViewById(R.id.ID);
        pwEdit = (EditText) v.findViewById(R.id.PW);
        autoCheckBox = (CheckBox) v.findViewById(R.id.Auto);
        findText = (TextView) v.findViewById(R.id.Find);
        //findText.setPaintFlags(findText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loginBtn = (Button) v.findViewById(R.id.Login);


        loginView = v.findViewById(R.id.loginView);

        mFarmerJoin = (Button) v.findViewById(R.id.FarmerJoin);
        mGeneralJoin = (Button) v.findViewById(R.id.GeneralJoin);

        mFarmerJoin.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                ((LoginActivity)getActivity()).onJoinFarmerBtnClicked();
            }
        });

        mGeneralJoin.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                ((LoginActivity)getActivity()).onJoiGeneralBtnClicked();
            }
        });
        
        RelativeLayout naverBtn = (RelativeLayout) v.findViewById(R.id.naverLayout);

        naverBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Naver", null);
				NaverLoginHelper.NaverLogin(getActivity(), new OpenIdLoginListener() {
					@Override
					public void onResult(boolean isSuccess, String content) {
						if(isSuccess)
						{
							NaverLoginHelper.openIdRegister(getActivity(), content, new OpenIdLoginListener() {

								@Override
								public void onResult(boolean isSuccess, String content) {

									if(isSuccess)
									{
										//((BaseFragmentActivity)getActivity()).runMainActivity();
										getActivity().finish();
									}
									else
									{
										UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
											@Override
											public void onDialog(int type) {}
										});
										loginView.setVisibility(View.GONE);
									}
								}
							});
						}
						else
						{
							loginView.setVisibility(View.GONE);
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
        	loginView.setVisibility(View.VISIBLE);
        }
        else if (Session.getCurrentSession().isOpened())
        {
        	loginView.setVisibility(View.VISIBLE);
        	onSessionOpened();

        }
    }

    private class MySessionStatusCallback implements SessionCallback
    {
        /**
         * 세션이 오픈되었으면 가입페이지로 이동 한다.
         */
        @Override
        public void onSessionOpened()
        {
        	loginView.setVisibility(View.VISIBLE);
        	UiController.hideProgressDialog(getSherlockActivity());
        	LoginFragment.this.onSessionOpened();
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
        	loginView.setVisibility(View.GONE);
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

				if(isSuccess)
				{
					KakaoLoginHelper.openIdRegister(getSherlockActivity(), UserProfile.loadFromCache(), new OpenIdLoginListener() {

						@Override
						public void onResult(boolean isSuccess, String content) {
							if(isSuccess)
							{
								//((BaseFragmentActivity)getActivity()).runMainActivity();
								getActivity().finish();
							}
							else
							{
								UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
									@Override
									public void onDialog(int type) {}
								});
								loginView.setVisibility(View.GONE);
							}
						}
					});
				}
				else
				{
					UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
						@Override
						public void onDialog(int type) {}
					});
					loginView.setVisibility(View.GONE);
				}
			}
		});
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        idEdit.setText(loginData.getId());
        pwEdit.setText(loginData.getPw());
        autoCheckBox.setChecked(loginData.isbAuto());

        autoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_AutoLogin", isChecked == true ? "true":"false");
            }
        });

        findText.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Find", null);
                    ((LoginActivity) getSherlockActivity()).onFindTextClicked();
                }
            }
        });

        loginBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Login", null);

                    loginData.setId(idEdit.getText().toString());
                    loginData.setPw(pwEdit.getText().toString());
                    loginData.setbAuto(autoCheckBox.isChecked());

                    ((LoginActivity) getSherlockActivity()).onLoginBtnClicked(loginData);
                }
            }
        });

    }
}
