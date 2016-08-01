package com.leadplatform.kfarmers.util.kakao;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.json.DiaryDetailJson;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.json.FarmerInfoJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.StoryDetailJson;
import com.leadplatform.kfarmers.model.json.StoryListJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.common.ShareDialogFragment;

import org.apache.http.protocol.HTTP;

import java.util.Hashtable;
import java.util.Map;

public class KaKaoController
{
    public static final String LINK_BASE_URL = Constants.KFARMERS_FULL_M_DOMAIN+"/%s";

    public static void onShareBtnClicked(Context context, String object, String tag)
    {
        final String[] menu = { "카카오톡", "카카오 스토리" };
        ShareDialogFragment fragment = ShareDialogFragment.newInstance(menu, object, tag);
        FragmentTransaction ft = ((SherlockFragmentActivity) context).getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, ShareDialogFragment.TAG);
    }

    public static void sendKakaotalk(final Context context, final DiaryDetailJson data)
    {
        try
        {
/*            final KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }*/

            CenterController.getFarmerInfo(data.FarmerIndex, new CenterResponseListener(context)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                FarmerInfoJson farmerInfo = (FarmerInfoJson) JsonUtil.jsonToObject(root.findPath("Row").toString(),
                                        FarmerInfoJson.class);
                                if (farmerInfo != null)
                                {
                                    RowJson textRow = null;
                                    RowJson imgRow = null;
                                    for (RowJson row : data.Rows)
                                    {
                                        if (textRow == null && row.Type.equals("Text"))
                                        {
                                            textRow = row;
                                        }
                                        else if (imgRow == null && row.Type.equals("Image"))
                                        {
                                        	imgRow = row;
                                        }
                                    }

                                    KakaoLinker.sendKakaoTalkLink(context, textRow.Value,imgRow.Value, "Diary", data.Diary);
                                    /*kakaoLink.openKakaoLink(context, String.format(LINK_BASE_URL, farmerInfo.ID) + "/" + data.Diary, textRow.Value,
                                            context.getPackageName(),
                                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Farm,
                                            HTTP.UTF_8);*/
                                }
                                break;

                            default:
                                UiController.showDialog(context, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(context, R.string.dialog_unknown_error);
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaotalk(final Fragment fragment, final DiaryListJson data)
    {
        final Context context = fragment.getActivity();
        try
        {
            KakaoLinker.sendKakaoTalkLink(context, data.Description,data.ProductImage1, "Diary", data.Diary);
            /*final KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }*//**//*

            CenterController.getFarmerInfo(data.FarmerIndex, new CenterResponseListener(context)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                FarmerInfoJson farmerInfo = (FarmerInfoJson) JsonUtil.jsonToObject(root.findPath("Row").toString(),
                                        FarmerInfoJson.class);
                                if (farmerInfo != null)
                                {
                                    *//**//*kakaoLink.openKakaoLink(fragment, String.format(LINK_BASE_URL, farmerInfo.ID) + "/" + data.Diary,
                                            data.Description, context.getPackageName(),
                                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Farm,
                                            HTTP.UTF_8);*//**//*
                                	KakaoLinker.sendKakaoTalkLink(context, data.Description,data.ProductImage1, "Diary", data.Diary);
                                }
                                break;

                            default:
                                UiController.showDialog(context, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(context, R.string.dialog_unknown_error);
                    }
                }
            });*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaotalk(final Context context, final StoryListJson data)
    {
        try
        {
            KakaoLinker.sendKakaoTalkLink(context, data.Description,data.Image, "Stroy", data.DiaryIndex);
            /*final KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            kakaoLink.openKakaoLink(fragment, data.Description, context.getPackageName(),
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Nickname, HTTP.UTF_8);*/

            //KakaoLinker.sendKakaoTalkLink(context, data.Description,Constants.KFARMERS_FULL_M_DOMAIN+"/CustomerImage/20141021141211_5445eb2bb2dfe_0.jpg", "null", "null");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaotalk(final Fragment fragment, final StoryListJson data)
    {
        final Context context = fragment.getActivity();
        try
        {
            KakaoLinker.sendKakaoTalkLink(context, data.Description,data.Image, "Stroy", data.DiaryIndex);
            /*final KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            kakaoLink.openKakaoLink(fragment, data.Description, context.getPackageName(),
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Nickname, HTTP.UTF_8);*/

            //KakaoLinker.sendKakaoTalkLink(context, data.Description,Constants.KFARMERS_FULL_M_DOMAIN+"/CustomerImage/20141021141211_5445eb2bb2dfe_0.jpg", "null", "null");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaotalk(final Context context, final StoryDetailJson data)
    {
        try
        {
            if (data != null)
            {
                RowJson textRow = null;
                RowJson imgRow = null;
                for (RowJson row : data.Rows)
                {
                    if (textRow == null && row.Type.equals("Text"))
                    {
                        textRow = row;
                    }
                    else if (imgRow == null && row.Type.equals("Image"))
                    {
                        imgRow = row;
                    }
                }

                KakaoLinker.sendKakaoTalkLink(context, textRow.Value,imgRow.Value, "Diary", data.Diary);
                                    /*kakaoLink.openKakaoLink(context, String.format(LINK_BASE_URL, farmerInfo.ID) + "/" + data.Diary, textRow.Value,
                                            context.getPackageName(),
                                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Farm,
                                            HTTP.UTF_8);*/
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaotalk(Fragment fragment, String userId, String idx, String title, String desc)
    {
        Context context = fragment.getActivity();
        try
        {
           /* KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            kakaoLink.openKakaoLink(fragment, String.format(LINK_BASE_URL, userId) + "/" + idx, desc, context.getPackageName(), context
                    .getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, title, HTTP.UTF_8);*/
        	
        	KakaoLinker.sendKakaoTalkLink(context, desc,Constants.KFARMERS_FULL_M_DOMAIN+"/CustomerImage/20141021141211_5445eb2bb2dfe_0.jpg", "null", "null");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(final Context context, final DiaryDetailJson data)
    {
        try
        {
            final StoryLink story = StoryLink.getLink(context);
            if (!story.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            CenterController.getFarmerInfo(data.FarmerIndex, new CenterResponseListener(context)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                FarmerInfoJson farmerInfo = (FarmerInfoJson) JsonUtil.jsonToObject(root.findPath("Row").toString(),
                                        FarmerInfoJson.class);
                                if (farmerInfo != null)
                                {
                                    RowJson textRow = null;
                                    for (RowJson row : data.Rows)
                                    {
                                        if (row.Type.equals("Text"))
                                        {
                                            textRow = row;
                                            break;
                                        }
                                    }

                                    RowJson imageRow = null;
                                    for (RowJson row : data.Rows)
                                    {
                                        if (row.Type.equals("Image"))
                                        {
                                            imageRow = row;
                                            break;
                                        }
                                    }

                                    Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
                                    urlInfoAndroid.put("title", data.Farm + "(" + data.CategoryName + ")");
                                    urlInfoAndroid.put("desc", farmerInfo.Introduction);
                                    urlInfoAndroid.put("imageurl", new String[] { imageRow.Value });
                                    urlInfoAndroid.put("type", "article");

                                    story.openKakaoLink(context,
                                            textRow.Value + "\n\n" + data.Farm + " 이야기\n" + String.format(LINK_BASE_URL, farmerInfo.ID) + "/"
                                                    + data.Diary, context.getPackageName(),
                                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, "대표농부 "
                                                    + data.FarmerName, HTTP.UTF_8, urlInfoAndroid);
                                }
                                break;

                            default:
                                UiController.showDialog(context, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(context, R.string.dialog_unknown_error);
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(final Fragment fragment, final DiaryListJson data)
    {
        final Context context = fragment.getActivity();
        try
        {
            final StoryLink story = StoryLink.getLink(context);
            if (!story.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            CenterController.getFarmerInfo(data.FarmerIndex, new CenterResponseListener(context)
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                FarmerInfoJson farmerInfo = (FarmerInfoJson) JsonUtil.jsonToObject(root.findPath("Row").toString(),
                                        FarmerInfoJson.class);
                                if (farmerInfo != null)
                                {
                                    Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
                                    urlInfoAndroid.put("title", data.Farm + "(" + data.CategoryName + ")");
                                    urlInfoAndroid.put("desc", farmerInfo.Introduction);
                                    urlInfoAndroid.put("imageurl", new String[] { data.ProductImage1 });
                                    urlInfoAndroid.put("type", "article");

                                    story.openKakaoLink(fragment,
                                            data.Description + "\n\n" + data.Farm + " 이야기\n" + String.format(LINK_BASE_URL, farmerInfo.ID) + "/"
                                                    + data.Diary, context.getPackageName(),
                                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, "대표농부 "
                                                    + data.FarmerName, HTTP.UTF_8, urlInfoAndroid);
                                }
                                break;

                            default:
                                UiController.showDialog(context, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(context, R.string.dialog_unknown_error);
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(final Context context, final StoryListJson data)
    {
        try
        {
            final StoryLink story = StoryLink.getLink(context);
            if (!story.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
            urlInfoAndroid.put("title", data.Nickname);
            urlInfoAndroid.put("desc", data.Description);
            urlInfoAndroid.put("imageurl", new String[] { data.ProductImage1 });
            urlInfoAndroid.put("type", "article");

            story.openKakaoLink(context,
                    data.Description + "\n\n" + data.Nickname + " 이야기\n", context.getPackageName(),
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Nickname, HTTP.UTF_8, urlInfoAndroid);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(final Fragment fragment, final StoryListJson data)
    {
        final Context context = fragment.getActivity();
        try
        {
            final StoryLink story = StoryLink.getLink(context);
            if (!story.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
            urlInfoAndroid.put("title", data.Nickname);
            urlInfoAndroid.put("desc", data.Description);
            urlInfoAndroid.put("imageurl", new String[] { data.ProductImage1 });
            urlInfoAndroid.put("type", "article");

            story.openKakaoLink(fragment,
                    data.Description + "\n\n" + data.Nickname + " 이야기\n", context.getPackageName(),
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Nickname, HTTP.UTF_8, urlInfoAndroid);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(final Context context, final StoryDetailJson data)
    {
        try
        {
            final StoryLink story = StoryLink.getLink(context);
            if (!story.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (data != null)
            {
                RowJson textRow = null;
                for (RowJson row : data.Rows)
                {
                    if (row.Type.equals("Text"))
                    {
                        textRow = row;
                        break;
                    }
                }

                RowJson imageRow = null;
                for (RowJson row : data.Rows)
                {
                    if (row.Type.equals("Image"))
                    {
                        imageRow = row;
                        break;
                    }
                }

                String tag = "";
                if(data.BlogTag != null && !data.BlogTag.trim().isEmpty()) tag = data.BlogTag;

                Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
                urlInfoAndroid.put("title", data.Nickname);
                urlInfoAndroid.put("desc", tag);
                urlInfoAndroid.put("imageurl", new String[] { imageRow.Value });
                urlInfoAndroid.put("type", "article");

                story.openKakaoLink(context,
                        textRow.Value + "\n\n" + data.Nickname + " 이야기\n", context.getPackageName(),
                        context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, data.Nickname, HTTP.UTF_8, urlInfoAndroid);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendKakaostory(Fragment fragment, String userId, String idx, String title, String desc, String farm, String farmer,
            String introduction, String imageurl)
    {
        Context context = fragment.getActivity();
        try
        {
            StoryLink story = StoryLink.getLink(context);
            if (story.isAvailableIntent())
            {
                Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
                urlInfoAndroid.put("title", title);
                urlInfoAndroid.put("desc", introduction);
                urlInfoAndroid.put("imageurl", new String[] { imageurl });
                urlInfoAndroid.put("type", "article");

                story.openKakaoLink(fragment, desc + "\n\n" + farm + " 이야기\n" + String.format(LINK_BASE_URL, userId) + "/" + idx,
                        context.getPackageName(), context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName, "대표농부 "
                                + farmer, HTTP.UTF_8, urlInfoAndroid);
            }
            else
            {
                Toast.makeText(context.getApplicationContext(), "카카오스토리가 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*public static void sendKakaolink(Fragment fragment)
    {
        Context context = fragment.getActivity();
        try
        {
            KakaoLink kakaoLink = KakaoLink.getLink(context);

            if (!kakaoLink.isAvailableIntent())
            {
                Toast.makeText(context.getApplicationContext(), "카카오톡이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<Map<String, String>> arrMetaInfo = new ArrayList<Map<String, String>>();
            Map<String, String> metaInfoArray = new Hashtable<String, String>(1);
            metaInfoArray.put("os", "android");
            metaInfoArray.put("devicetype", "phone");
            metaInfoArray.put("installurl", "market://details?id=com.leadplatform.kfarmers");// com.leadplatform.kfarmers
            metaInfoArray.put("executeurl", "app://kfarmers.kr");
            arrMetaInfo.add(metaInfoArray);

            kakaoLink.openKakaoAppLink(fragment, String.format(LINK_BASE_URL, ""),
                    "나와 가족이 먹는 농산물이 어디서, 어떻게 재배되는지 궁금하지 않으세요? 이제부터 실시간으로 확인하실 수 있습니다.", context.getPackageName(), context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), 0).versionName, "K파머스", HTTP.UTF_8, arrMetaInfo);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

    private void getFarmData(final Context context, String userType, String userIndex)
    {
        // CenterController.getFarm(userType, userIndex, new CenterResponseListener(context)
        // {
        // @Override
        // public void onSuccess(int Code, String content)
        // {
        // try
        // {
        // switch (Code)
        // {
        // case 0000:
        // JsonNode root = JsonUtil.parseTree(content);
        // FarmJson farmData = (FarmJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), FarmJson.class);
        // if (farmData != null)
        // {
        // displayFarmHeaderView(farmData);
        // getListDiary(makeListDiaryData(true, 0));
        // }
        // break;
        //
        // default:
        // UiController.showDialog(context, R.string.dialog_unknown_error);
        // break;
        // }
        // }
        // catch (Exception e)
        // {
        // UiController.showDialog(context, R.string.dialog_unknown_error);
        // }
        // }
        // });
    }
}
