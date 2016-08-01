package com.leadplatform.kfarmers.view.menu.invite;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KakaoLinker;
import com.leadplatform.kfarmers.view.base.BaseFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class InviteSnsFragment extends BaseFragment {

    public static final int TYPE_KAKAO = 1;
    public static final int TYPE_BAND = 2;

    int type;

    public static InviteSnsFragment newInstance() {
        final InviteSnsFragment f = new InviteSnsFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            type = getArguments().getInt("Type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = null;

        if (type == TYPE_KAKAO) {
            v = inflater.inflate(R.layout.fragment_invite_kakao, container, false);
            Button kakao = (Button) v.findViewById(R.id.kakao);
            kakao.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_INVITE, "Click_Invite", "카카오톡");
                    KakaoLinker.sendKakaoTalkLinkInvite(getActivity());
                }
            });
        } else if (type == TYPE_BAND) {
            v = inflater.inflate(R.layout.fragment_invite_band, container, false);
            Button band = (Button) v.findViewById(R.id.band);
            band.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_INVITE, "Click_Invite", "네이버밴드");
                    try {
                        PackageManager manager = getActivity().getPackageManager();
                        PackageInfo pack = manager.getPackageInfo("com.nhn.android.band".toLowerCase(), PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException e) {
                        // 밴드앱 설치되지 않은 경우 구글 플레이 설치페이지로 이동
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.band"));
                        startActivity(intent);
                        return;
                    }

                    String serviceDomain = "www.kfarmers.kr"; //  연동 서비스 도메인
                    String encodedText = getString(R.string.inviteSnsText); // 글 본문 (utf-8 urlencoded)
                    Uri uri = null;
                    try {
                        uri = Uri.parse("bandapp://create/post?text=" + URLEncoder.encode(encodedText, "utf-8") + "&route=" + serviceDomain);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}

