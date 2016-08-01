package com.leadplatform.kfarmers.util.kakao;

import android.content.Context;

import com.kakao.AppActionBuilder;
import com.kakao.AppActionInfoBuilder;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoTalkLinkMessageBuilder;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.ImageUtil;

import java.io.File;

public class KakaoLinker {

    public static final String CUSTOM_TYPE = "type";
    public static final String CUSTOM_INDEX = "execute";

    public static void sendKakaoTalkLink(Context context, String text, String img, String type, String idx) {
        try {
            String param = CUSTOM_TYPE + "=" + type + "&" + CUSTOM_INDEX + "=" + idx;

            KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
            KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            kakaoTalkLinkMessageBuilder.addText(text);

            if(img != null) {
                String path = ImageUtil.getFileAbsolutePath(img);
                File file = new File(path);
                if (file.length() / 1024 > 480 || file.length() == 0) {
                    img = Constants.KFARMERS_FULL_DOMAIN + "/kakao_default.png";
                }
                kakaoTalkLinkMessageBuilder.addImage(img, 800, 600);
            }
            kakaoTalkLinkMessageBuilder.addAppButton(context.getString(R.string.Kakao_link_btn), new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam(param).build()).build());
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), context);
        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }
    }

    public static void sendKakaoTalkLinkInvite(Context context) {

        try {
            KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
            KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            kakaoTalkLinkMessageBuilder.addText(context.getString(R.string.inviteSnsText));
            kakaoTalkLinkMessageBuilder.addImage(Constants.KFARMERS_FULL_DOMAIN + "/kakao_default.png", 800, 600);
            kakaoTalkLinkMessageBuilder.addAppButton(context.getString(R.string.Kakao_link_btn), new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().build()).build());
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), context);
        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }

    }
}
