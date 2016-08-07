package com.leadplatform.kfarmers.view.sns;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.json.DaumCategoryJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment;

import net.daum.mf.oauth.MobileOAuthLibrary;
import net.daum.mf.oauth.OAuthError;

import java.util.ArrayList;

public class SnsDaumFragment extends BaseFragment {
    public static final String TAG = "SnsDaumFragment";
    public static final String DAUM_CLIENT_ID = "405346064575943911";

    TextView mTextView;
    EditText mEditText;

    ArrayList<DaumCategoryJson> daumCategoryJsons;
    ArrayList<String> mCategoryStr;

    private int categoryIndex = 0;

    public static SnsDaumFragment newInstance() {
        final SnsDaumFragment f = new SnsDaumFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileOAuthLibrary.getInstance().initialize(getActivity(), DAUM_CLIENT_ID);  // OAuth 라이브러리 초기화.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sns_daum, container, false);

        mTextView = (TextView) v.findViewById(R.id.categoryText);

        mEditText = (EditText) v.findViewById(R.id.editId);

        RelativeLayout verify = (RelativeLayout) v.findViewById(R.id.daumLayout);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // access token 발급받기.
                MobileOAuthLibrary.getInstance().authorize(getSherlockActivity(), oAuthListener);
            }
        });

        Button profile = (Button) v.findViewById(R.id.categoryBtn);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiController.showProgressDialog(getActivity());
                // oauth 2.0 을 지원하는 profile API 사용하기
                String id = mEditText.getText().toString().trim();
                MobileOAuthLibrary.getInstance().requestResourceWithPath(getActivity(), oAuthListener, "/blog/v1/" + id + "/categories.json");
            }
        });

        return v;
    }

    MobileOAuthLibrary.OAuthListener oAuthListener = new MobileOAuthLibrary.OAuthListener() {

        @Override
        public void onAuthorizeSuccess() {
        }

        @Override
        public void onAuthorizeFail(OAuthError.OAuthErrorCodes oAuthErrorCodes, String s) {
        }

        @Override
        public void onRequestResourceSuccess(String response) {
            UiController.hideProgressDialog(getActivity());

            try {
                JsonNode jsonNode = JsonUtil.parseTree(response).findPath("items");
                daumCategoryJsons = (ArrayList<DaumCategoryJson>) JsonUtil.jsonToArrayObject(jsonNode, DaumCategoryJson.class);

                mCategoryStr = new ArrayList<String>();
                for (DaumCategoryJson categoryJson : daumCategoryJsons) {
                    mCategoryStr.add(categoryJson.name);
                }

                onCategoryBtnClicked(categoryIndex, new DialogFragment.OnCloseCategoryDialogListener() {
                    @Override
                    public void onDialogSelected(int subMenuType, int position) {
                        categoryIndex = position;
                        mTextView.setText("선택한 카테고리 : " + daumCategoryJsons.get(position).name);
                        //DbController.updateNaverCategory(getSherlockActivity(), mArrayList.get(position).categoryNo, mCategoryStr.get(position));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRequestResourceFail(OAuthError.OAuthErrorCodes errorCode, String errorMessage) {
            UiController.hideProgressDialog(getActivity());
            mTextView.append("onRequestResourceFail : " + errorMessage + "\n");
            if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorInvalidToken)) {
                // access token 이 없거나 만료처리된 경우 or 401 에러
                // authorize() 를 통해 다시 access token을 발급 받아야함.
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorInvalidResourceRequest)) {
                // 서버와 통신중 400 에러가 발생한 경우
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorInsufficientScope)) {
                // 서버와 통신중 403 에러가 발생한 경우
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorServiceNotFound)) {
                // 서버와 통신중 404 에러가 발생한 경우
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorNetwork)) {
                // 현재 휴대폰의 네트워크를 이용할 수 없는 경우
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorServer)) {
                // 서버쪽에서 에러가 발생하는 경우
                // 서버 페이지에 문제가 있는 경우이므로 api 담당자와 얘기해야함.
            } else if (errorCode.equals(OAuthError.OAuthErrorCodes.OAuthErrorUnknown)) {
                // 서버와 통신중 그 외 알수 없는 에러가 발생한 경우.
            }
        }
    };

    public void onCategoryBtnClicked(int categoryIndex, DialogFragment.OnCloseCategoryDialogListener onCloseCategoryDialogListener) {
        DialogFragment fragment = DialogFragment.newInstance(
                0,
                categoryIndex,
                mCategoryStr.toArray(new String[mCategoryStr.size()]),
                "");
        fragment.setOnCloseCategoryDialogListener(onCloseCategoryDialogListener);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, DialogFragment.TAG);
    }
}
