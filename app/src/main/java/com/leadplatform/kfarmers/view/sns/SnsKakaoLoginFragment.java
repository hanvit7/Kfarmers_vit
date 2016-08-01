/**
 * Copyright 2014 Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leadplatform.kfarmers.view.sns;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kakao.AuthType;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.kakao.widget.LoginButton;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.view.base.BaseFragment;

/**
 * 샘플에서 사용하게 될 로그인 페이지 세션을 오픈한 후 action을 override해서 사용한다.
 * 
 * @author MJ
 */
public class SnsKakaoLoginFragment extends BaseFragment
{
    public static final String TAG = "SnsKakaoLoginFragment";

    private LoginButton loginButton;
    private final SessionCallback mySessionCallback = new MySessionStatusCallback();

    public static SnsKakaoLoginFragment newInstance()
    {
        final SnsKakaoLoginFragment f = new SnsKakaoLoginFragment();
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
        final View v = inflater.inflate(R.layout.fragment_sns_kakao, container, false);

        loginButton = (LoginButton) v.findViewById(R.id.com_kakao_login);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        // 세션을 초기화 한다
        if (Session.initializeSession(getSherlockActivity(), mySessionCallback,AuthType.KAKAO_TALK))
        {
            // 1. 세션을 갱신 중이면, 프로그레스바를 보이거나 버튼을 숨기는 등의 액션을 취한다
            loginButton.setVisibility(View.GONE);
        }
        else if (Session.getCurrentSession().isOpened())
        {
            // 2. 세션이 오픈된된 상태이면, 다음 activity로 이동한다.
            onSessionOpened();
        }
        else
        {
        	loginButton.setVisibility(View.VISIBLE);
        }
        // 3. else 로그인 창이 보인다.
    }

    private class MySessionStatusCallback implements SessionCallback
    {
        /**
         * 세션이 오픈되었으면 가입페이지로 이동 한다.
         */
        @Override
        public void onSessionOpened()
        {
            // 프로그레스바를 보이고 있었다면 중지하고 세션 오픈후 보일 페이지로 이동
            SnsKakaoLoginFragment.this.onSessionOpened();
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
            // 프로그레스바를 보이고 있었다면 중지하고 세션 오픈을 못했으니 다시 로그인 버튼 노출.
            loginButton.setVisibility(View.VISIBLE);
            DbController.updateKakaoFlag(getActivity(), false);
        }

    }

    protected void onSessionOpened()
    {
        Log.e(TAG, "====== onSessionOpened =====");
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, SnsKakaoSignupFragment.newInstance(), SnsKakaoSignupFragment.TAG);
        ft.commitAllowingStateLoss();
    }
}
