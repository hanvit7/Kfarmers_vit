package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.model.parcel.BlogListData;
import com.leadplatform.kfarmers.model.parcel.FarmNewsFooterFilter;
import com.leadplatform.kfarmers.model.parcel.EditFarmerData;
import com.leadplatform.kfarmers.model.parcel.EditGeneralData;
import com.leadplatform.kfarmers.model.parcel.EditVillageData;
import com.leadplatform.kfarmers.model.parcel.FavoriteListData;
import com.leadplatform.kfarmers.model.parcel.JoinFarmerData;
import com.leadplatform.kfarmers.model.parcel.JoinGeneralData;
import com.leadplatform.kfarmers.model.parcel.JoinVillageData;
import com.leadplatform.kfarmers.model.parcel.LoginData;
import com.leadplatform.kfarmers.model.parcel.NoticeListData;
import com.leadplatform.kfarmers.model.parcel.RecommendListData;
import com.leadplatform.kfarmers.model.parcel.ReleaseFarmerData;
import com.leadplatform.kfarmers.model.parcel.ReleaseVillageData;
import com.leadplatform.kfarmers.model.parcel.ReplyListData;
import com.leadplatform.kfarmers.model.parcel.RowsData;
import com.leadplatform.kfarmers.model.parcel.SearchListData;
import com.leadplatform.kfarmers.model.parcel.StoryListData;
import com.leadplatform.kfarmers.model.parcel.WriteDiaryData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.Installation;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;

//서버로 데이터 얻기위해 post
public class CenterController {
    private final static String TAG = "CenterController";
    private static final String BASE_URL = Constants.KFARMERS_FULL_DOMAIN + "/API/V2/"; // 신서버

    private static int expiredSessionRetryCnt = 0;
    private static String backupUrl;
    private static RequestParams backupParams;
    private static CenterResponseListener backupCenterResponseListener;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private static void initHttpClientParams() {
        client.setTimeout(30000);
    }

    public static void cancel(Context context, boolean mayInterruptIfRunning) {
        if (client != null) {
            client.cancelRequests(context, mayInterruptIfRunning);
        }
    }

    private static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (client != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, " POST Url = " + url);
                Log.d(TAG, " POST Params = " + params.toString());
            }

            expiredSessionRetryCnt = 0;
            backupUrl = url;
            backupParams = params;
            backupCenterResponseListener = (CenterResponseListener) responseHandler;

            initHttpClientParams();
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }
    }

    private static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (client != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, " POST Url = " + url);
                Log.d(TAG, " POST Params = " + params.toString());
            }

            expiredSessionRetryCnt = 0;
            backupUrl = url;
            backupParams = params;
            backupCenterResponseListener = (CenterResponseListener) responseHandler;

            initHttpClientParams();
            client.post(context, getAbsoluteUrl(url), params, responseHandler);
        }
    }

    public static void expiredSession(final Context context) {
        if (expiredSessionRetryCnt == 0) {
            UserDb user = DbController.queryCurrentUser(context);
            if (user != null && user.getAutoLoginFlag() == 1) {
                RequestParams params = new RequestParams();
                params.put("ID", user.getUserID());
                params.put("PW", user.getUserPwDecrypt());

                if (client != null) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, " POST Url = " + "Login.php");
                        Log.e(TAG, " POST Params = " + params.toString());
                    }
                    expiredSessionRetryCnt++;
                    initHttpClientParams();
                    client.post(getAbsoluteUrl("Login.php"), params, new ExpiredSessionListener(context));
                    return;
                }
            } else {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return;
            }
        }

        if (BuildConfig.DEBUG) {
            Log.e(TAG, " expiredSession() ==  ERROR !!");
            Log.e(TAG, " expiredSession() ==  expiredSessionRetryCnt = " + expiredSessionRetryCnt);
        }
        backupCenterResponseListener.onFailure(-1, null, null, new Throwable());
        backupCenterResponseListener.onFinish();
    }

    private static class ExpiredSessionListener extends AsyncHttpResponseHandler {
        private Context context;

        public ExpiredSessionListener(Context context) {
            this.context = context;
        }

        @Override
        public void onStart() {
            UiController.showProgressDialog(context);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] content) {
            if (backupUrl != null && backupParams != null && backupCenterResponseListener != null) {
                try {
                    final JsonNode root = JsonUtil.parseTree(new String(content));
                    final String code = root.path("Code").textValue();
                    if (code.equals("0000")) {
                        AppPreferences.setLogin(context, true);

                        // 세션연장 성공. 기존 서비스 재요청.
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, " ExpiredSessionListener() ==  SUCCESS !!");
                        }

                        UserDb user = DbController.queryCurrentUser(context);

                        SnipeApiController.getToken(user.getUserID(), user.getUserPwDecrypt(), new SnipeResponseListener(context) {
                            @Override
                            public void onSuccess(int Code, String content, String error) {
                                try {
                                    switch (Code) {
                                        case 600: {
                                            DbController.updateApiToken(context, content);
                                            SnipeApiController.setToken(content);

                                            if (client != null) {
                                                if (BuildConfig.DEBUG) {
                                                    Log.e(TAG, " POST Url = " + backupUrl);
                                                    Log.e(TAG, " POST Params = " + backupParams.toString());
                                                }
                                                initHttpClientParams();
                                                client.post(getAbsoluteUrl(backupUrl), backupParams, backupCenterResponseListener);
                                            }

                                            break;
                                        }
                                        default: {
                                            UiController.showDialog(context, R.string.dialog_unknown_error);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, " ExpiredSessionListener() ==  ERROR !!");
                        }
                        backupCenterResponseListener.onSuccess(Integer.valueOf(code), new String(content));
                        backupCenterResponseListener.onFinish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFinish() {
            UiController.hideProgressDialog(context);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, " ExpiredSessionListener() ==  FAIL !!");
            }

            if (backupCenterResponseListener != null) {
                backupCenterResponseListener.onFailure(statusCode, headers, content, error);
                backupCenterResponseListener.onFinish();
            }
        }
    }

    public static void version(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        post("Version.php", params, responseHandler);
    }

    public static void login(final LoginData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ID", data.getId());
        params.put("PW", data.getPw());

        post("Login.php", params, responseHandler);
    }

    public static void logout(final String pushKey, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (!pushKey.isEmpty()) {
            params.put("regId", pushKey);
        }
        post("Logout.php", params, responseHandler);
    }

    public static void findIdFromEmail(final String name, final String email, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Name", name);
        params.put("Email", email);

        post("FindIDFromEmail.php", params, responseHandler);
    }

    public static void findIdFromPhone(final String name, final String phone, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Name", name);
        params.put("Phone", phone);

        post("FindIDFromPhone1.php", params, responseHandler);
    }

    public static void findIdFromPhoneAuth(final String auth, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Auth", auth);

        post("FindIDFromPhone2.php", params, responseHandler);
    }

    public static void findPwFromEmail(final String id, final String name, final String email, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ID", id);
        params.put("Name", name);
        params.put("Email", email);

        post("FindPWFromEmail.php", params, responseHandler);
    }

    public static void findPwFromPhone(final String id, final String name, final String phone, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ID", id);
        params.put("Name", name);
        params.put("Phone", phone);

        post("FindPWFromPhone1.php", params, responseHandler);
    }

    public static void findPwFromPhoneAuth(final String auth, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Auth", auth);

        post("FindPWFromPhone2.php", params, responseHandler);
    }

    public static void checkID(final String id, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ID", id);

        post("CheckID.php", params, responseHandler);
    }

    public static void joinGeneral(final JoinGeneralData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            if (!CommonUtil.PatternUtil.isEmpty(data.getProfile()))
                params.put("Profile", new File(data.getProfile()));

            params.put("Name", data.getName());
            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");
            params.put("ID", data.getUserID());
            params.put("PW", data.getUserPW());

            post("JoinUser.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void joinFarmer(final JoinFarmerData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            params.put("Profile", new File(data.getProfile()));
            params.put("Name", data.getName());
            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");
            params.put("ID", data.getUserID());
            params.put("PW", data.getUserPW());
            params.put("Farm", data.getFarm());
            params.put("Address", data.getAddress());

            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[0]))
                params.put("Image1", new File(data.getImagePath()[0]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[1]))
                params.put("Image2", new File(data.getImagePath()[1]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[2]))
                params.put("Image3", new File(data.getImagePath()[2]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[3]))
                params.put("Image4", new File(data.getImagePath()[3]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[4]))
                params.put("Image5", new File(data.getImagePath()[4]));

            params.put("Category", data.getCategory());
            params.put("Introduction", data.getIntroduction());

            post("JoinFarmer.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void joinVillage(final JoinVillageData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            params.put("Profile", new File(data.getProfile()));
            params.put("Name", data.getName());
            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");
            params.put("ID", data.getUserID());
            params.put("PW", data.getUserPW());
            params.put("Farm", data.getFarm());
            params.put("Address", data.getAddress());

            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[0]))
                params.put("Image1", new File(data.getImagePath()[0]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[1]))
                params.put("Image2", new File(data.getImagePath()[1]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[2]))
                params.put("Image3", new File(data.getImagePath()[2]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[3]))
                params.put("Image4", new File(data.getImagePath()[3]));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImagePath()[4]))
                params.put("Image5", new File(data.getImagePath()[4]));

            params.put("Category", data.getCategory());
            params.put("Introduction", data.getIntroduction());

            post("JoinVillage.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void weather(final double latitude, final double longitude, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Latitude", String.valueOf(latitude));
        params.put("Longitude", String.valueOf(longitude));

        post("GetSky.php", params, responseHandler);
    }

    public static void writeDiary(final WriteDiaryData data, CenterResponseListener responseHandler) {
        try {
            int rowIndex = 0;
            int imageIndex = 0;
            RequestParams params = new RequestParams();

            params.put("Category", String.valueOf(data.getCategory()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTitle()))
                params.put("BlogTitle", data.getBlogTitle());
            params.put("BlogAlign", data.isbAlign() ? "C" : "L");

            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTag()))
                params.put("BlogTag", data.getBlogTag());

            params.put("SNSNaverBlog", "N");
            params.put("SNSTistory", data.isbTistory() ? "Y" : "N");
            params.put("SNSDaumBlog", data.isbDaum() ? "Y" : "N");
            params.put("SNSFacebook", data.isbFacebook() ? "Y" : "N");
            params.put("SNSTwitter", data.isbTwitter() ? "Y" : "N");

            if (!CommonUtil.PatternUtil.isEmpty(data.getWeather()))
                params.put("Sky", data.getWeather());
            if (!CommonUtil.PatternUtil.isEmpty(data.getTemperature()))
                params.put("Temperature", data.getTemperature().replace("℃", ""));
            if (!CommonUtil.PatternUtil.isEmpty(data.getHumidity()))
                params.put("Humidity", data.getHumidity().replace("%", ""));
            if (!CommonUtil.PatternUtil.isEmpty(data.getDate()))
                params.put("RegistrationDate", data.getDate());
            if (!CommonUtil.PatternUtil.isEmpty(data.getFrom()))
                params.put("From", data.getFrom());

            for (RowsData item : data.getRows()) {
                params.put("Rows[" + rowIndex + "][Type]", item.Type);
                if (item.Type.equals("Text")) {
                    params.put("Rows[" + rowIndex + "][Value]", item.Value);
                } else {
                    params.put("Rows[" + rowIndex + "][Value]", String.valueOf(imageIndex++));
                }
                rowIndex++;
            }

            imageIndex = 0;
            for (String path : data.getImages()) {
                params.put("Images[" + imageIndex++ + "]", new File(path));
            }

            post("WriteDiary.php", params, responseHandler);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            responseHandler.onFinish();
            responseHandler.onCancel();
        }
    }

    public static void editDiary(final String diary, final WriteDiaryData data, CenterResponseListener responseHandler) {
        try {
            int rowIndex = 0;
            int imageIndex = 0;
            RequestParams params = new RequestParams();

            params.put("Index", diary);
            params.put("Category", String.valueOf(data.getCategory()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTitle()))
                params.put("BlogTitle", data.getBlogTitle());
            params.put("BlogAlign", data.isbAlign() ? "C" : "L");

            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTag()))
                params.put("BlogTag", data.getBlogTag());

            params.put("SNSNaverBlog", data.isbNaver() ? "Y" : "N");
            params.put("SNSTistory", data.isbTistory() ? "Y" : "N");
            params.put("SNSDaumBlog", data.isbDaum() ? "Y" : "N");
            params.put("SNSFacebook", data.isbFacebook() ? "Y" : "N");
            params.put("SNSTwitter", data.isbTwitter() ? "Y" : "N");

            if (!CommonUtil.PatternUtil.isEmpty(data.getWeather()))
                params.put("Sky", data.getWeather());
            if (!CommonUtil.PatternUtil.isEmpty(data.getTemperature()))
                params.put("Temperature", data.getTemperature().replace("℃", ""));
            if (!CommonUtil.PatternUtil.isEmpty(data.getHumidity()))
                params.put("Humidity", data.getHumidity().replace("%", ""));

            for (RowsData item : data.getRows()) {
                params.put("Rows[" + rowIndex + "][Type]", item.Type);
                if (item.Type.equals("Text")) {
                    params.put("Rows[" + rowIndex + "][Value]", item.Value);
                } else {
                    params.put("Rows[" + rowIndex + "][Value]", String.valueOf(imageIndex++));
                }
                rowIndex++;
            }

            imageIndex = 0;
            for (String path : data.getImages()) {
                if (path.contains("http")) {

                    File file = ImageLoader.getInstance().getDiskCache().get(path);
                    String pathfile = file.getAbsolutePath();
                    File newFile = new File(pathfile + ".jpg");
                    file.renameTo(newFile);
                    params.put("Images[" + imageIndex++ + "]", newFile);
                } else {
                    params.put("Images[" + imageIndex++ + "]", new File(path));
                }
            }

            post("EditDiary.php", params, responseHandler);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeNotice(final WriteDiaryData data, CenterResponseListener responseHandler) {
        try {
            int rowIndex = 0;
            int imageIndex = 0;
            RequestParams params = new RequestParams();

            params.put("BlogTitle", data.getBlogTitle());

            for (RowsData item : data.getRows()) {
                params.put("Rows[" + rowIndex + "][Type]", item.Type);
                if (item.Type.equals("Text")) {
                    params.put("Rows[" + rowIndex + "][Value]", item.Value);
                } else {
                    params.put("Rows[" + rowIndex + "][Value]", String.valueOf(imageIndex++));
                }
                rowIndex++;
            }

            imageIndex = 0;
            for (String path : data.getImages()) {
                params.put("Images[" + imageIndex++ + "]", new File(path));
            }

            post("WriteNotice.php", params, responseHandler);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getFarmNewsList(final FarmNewsFooterFilter data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getFarmer()))
                params.put("Farmer", data.getFarmer());
            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory1()))
                params.put("Category1", data.getCategory1());
            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory2()))
                params.put("Category2", data.getCategory2());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
            if (data.getAuth() != 0)
                params.put("Auth", String.valueOf(data.getAuth()));
            if (data.getReleaseDate2Month() != 0)
                params.put("ReleaseDate2Month", String.valueOf(data.getReleaseDate2Month()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getAddress()))
                params.put("Address", data.getAddress());
            if (data.getDistance() != 0 && data.getLatitude() != 0 && data.getLongitude() != 0) {
                params.put("Distance", String.valueOf(data.getDistance()));
                params.put("Latitude", String.valueOf(data.getLatitude()));
                params.put("Longitude", String.valueOf(data.getLongitude()));
            }

            params.put("OldDate", String.valueOf(data.getOldDate()));

            if (data.isImpressive())
                params.put("RegisterSNSFanPage", "Y");

            if (!CommonUtil.PatternUtil.isEmpty(data.getSearch()))
                params.put("Search", data.getSearch());

            if (data.getVerification() != null) {
                if (data.getVerification().equals("Y")) {
                    params.put("Verification", "Y");
                } else {
                    params.put("Verification", "N");
                }
            }
        }

        post("GetListDiary.php", params, responseHandler);
    }

    public static void getFarmNewsList(Context context, final FarmNewsFooterFilter data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getFarmer()))
                params.put("Farmer", data.getFarmer());
            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory1()))
                params.put("Category1", data.getCategory1());
            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory2()))
                params.put("Category2", data.getCategory2());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
            if (data.getAuth() != 0)
                params.put("Auth", String.valueOf(data.getAuth()));
            if (data.getReleaseDate2Month() != 0)
                params.put("ReleaseDate2Month", String.valueOf(data.getReleaseDate2Month()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getAddress()))
                params.put("Address", data.getAddress());
            if (data.getDistance() != 0 && data.getLatitude() != 0 && data.getLongitude() != 0) {
                params.put("Distance", String.valueOf(data.getDistance()));
                params.put("Latitude", String.valueOf(data.getLatitude()));
                params.put("Longitude", String.valueOf(data.getLongitude()));
            }

            params.put("OldDate", String.valueOf(data.getOldDate()));

            if (data.isImpressive())
                params.put("RegisterSNSFanPage", "Y");

            if (!CommonUtil.PatternUtil.isEmpty(data.getSearch()))
                params.put("Search", data.getSearch());

            if (data.getVerification() != null) {
                if (data.getVerification().equals("Y")) {
                    params.put("Verification", "Y");
                } else {
                    params.put("Verification", "N");
                }
            }
        }

        post(context, "GetListDiary.php", params, responseHandler);//이야기
    }

    public static void getListDiaryHome(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetDiaryMain.php", params, responseHandler);
    }

    public static void getSearchListDiary(final SearchListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getSearch()))
                params.put("Search", data.getSearch());
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }
        post("GetListSearchDiary.php", params, responseHandler);
    }

    public static void getDetailDiary(final String data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Diary", data);

        post("GetViewDiary.php", params, responseHandler);
    }

    public static void getConsumerStory(final String data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Diary", data);

        post("GetViewConsumer.php", params, responseHandler);
    }

    public static void getNormalStory(final String data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Diary", data);

        post("GetViewFreshTableDiary.php", params, responseHandler);
    }

    public static void snsNaverBlog(final String loginId, final String apiPw, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("LoginID", loginId);
        params.put("APIPW", apiPw);

        post("SetSNSNaverBlogMetaWeblog.php", params, responseHandler);
    }

    public static void snsTistory(final String apiUrl, final String blogId, final String loginId, final String loginPw,
                                  CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("APIURL", apiUrl);
        params.put("BlogID", blogId);
        params.put("LoginID", loginId);
        params.put("LoginPW", loginPw);

        post("SetSNSTistoryMetaWeblog.php", params, responseHandler);
    }

    public static void snsDaumBlog(final String token, final String secret, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Token", token);
        params.put("Secret", token);

        post("SetSNSDaumBlog.php", params, responseHandler);
    }

    public static void snsTwitter(final String token, final String secret, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Token", token);
        params.put("Secret", token);

        post("SetSNSTwitter.php", params, responseHandler);
    }

    public static void recommendList(final RecommendListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getType()))
                params.put("Type", data.getType());

            if (!CommonUtil.PatternUtil.isEmpty(data.getType()))
                params.put("Offset", data.getOldIndex());
        }

        post("GetListRecommendation.php", params, responseHandler);
    }

    public static void getCategory(final String id, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ID", id);

        post("GetCategory.php", params, responseHandler);
    }

    public static void getReleaseFarmer(final String category, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", category);

        post("GetViewReleaseFarmer.php", params, responseHandler);
    }

    public static void editReleaseFarmer(final ReleaseFarmerData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("Category", data.getCategory());
            if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseDateStart()))
                params.put("ReleaseDateStart", data.getReleaseDateStart());
            if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseDateEnd()))
                params.put("ReleaseDateEnd", data.getReleaseDateEnd());
            params.put("Alway", (data.isAlways() == true) ? "Y" : "N");
            params.put("Finish", (data.isFinish() == true) ? "Y" : "N");
            if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseNote()))
                params.put("ReleaseNote", data.getReleaseNote());
        }

        post("WriteReleaseFarmer.php", params, responseHandler);
    }

    public static void getReleaseVillage(final String category, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", category);

        post("GetViewReleaseFarmer.php", params, responseHandler);
    }

    public static void editReleaseVillage(final ReleaseVillageData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            try {
                params.put("Category", data.getCategory());
                if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseDateStart()))
                    params.put("ReleaseDateStart", data.getReleaseDateStart());
                if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseDateEnd()))
                    params.put("ReleaseDateEnd", data.getReleaseDateEnd());
                params.put("Alway", (data.isAlways() == true) ? "Y" : "N");
                params.put("Finish", (data.isFinish() == true) ? "Y" : "N");
                if (!CommonUtil.PatternUtil.isEmpty(data.getPrice()))
                    params.put("Price", data.getPrice());
                if (!CommonUtil.PatternUtil.isEmpty(data.getReleaseNote()))
                    params.put("ReleaseNote", data.getReleaseNote());

                int imageIndex = 0;
                for (String path : data.getImagePath()) {
                    if (!CommonUtil.PatternUtil.isEmpty(path))
                        params.put("Images[" + imageIndex++ + "]", new File(path));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        post("WriteReleaseVillage.php", params, responseHandler);
    }

    public static void getListConsumer(final StoryListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserIndex()))
                params.put("UserIndex", data.getUserIndex());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListConsumer.php", params, responseHandler);
    }

    public static void getListInterview(final StoryListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserIndex()))
                params.put("UserIndex", data.getUserIndex());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListDirect.php", params, responseHandler);
    }

    public static void getListFresh(final StoryListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserIndex()))
                params.put("UserIndex", data.getUserIndex());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }
        post("GetListFreshTableDiary.php", params, responseHandler);
    }

    public static void getProfile(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetProfile.php", params, responseHandler);//프로파일 요청
    }

    public static void setProfilePayAgreement(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("SetProfilePayAgreement.php", params, responseHandler);
    }

    public static void getGeneralUser(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetUser.php", params, responseHandler);
    }

    public static void getFarmerUser(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetFarmer.php", params, responseHandler);
    }

    public static void getVillageUser(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetVillage.php", params, responseHandler);
    }

    public static void editGeneral(final EditGeneralData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            if (!CommonUtil.PatternUtil.isEmpty(data.getProfile()))
                params.put("Profile", new File(data.getProfile()));

            params.put("Name", data.getName());
            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");

            if (!CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()))
                params.put("OldPW", data.getUserOldPw());
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserNewPW()))
                params.put("NewPW", data.getUserNewPW());

            post("EditUser.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editFarmer(final EditGeneralData data, String bankAccount, String licenseeNo, String licenseeSellNo, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            if (!CommonUtil.PatternUtil.isEmpty(data.getProfile()))
                params.put("Profile", new File(data.getProfile()));

            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");

            if (!CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()))
                params.put("OldPW", data.getUserOldPw());
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserNewPW()))
                params.put("NewPW", data.getUserNewPW());

            if (bankAccount != null && !bankAccount.isEmpty())
                params.put("BankAccount", bankAccount);

            if (licenseeNo != null && !licenseeNo.isEmpty())
                params.put("BusinessLicensee", licenseeNo);

            if (licenseeSellNo != null && !licenseeSellNo.isEmpty())
                params.put("OnlineMarketing", licenseeSellNo);

            post("EditFarmerStep1.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editFarmerNext(final EditFarmerData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            params.put("Farm", data.getFarm());
            params.put("Address", data.getAddress());

            if (!CommonUtil.PatternUtil.isEmpty(data.getImage1()))
                params.put("Image1", new File(data.getImage1()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage2()))
                params.put("Image2", new File(data.getImage2()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage3()))
                params.put("Image3", new File(data.getImage3()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage4()))
                params.put("Image4", new File(data.getImage4()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage5()))
                params.put("Image5", new File(data.getImage5()));

            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory()))
                params.put("Category", data.getCategory());
            params.put("Introduction", data.getIntroduction());

            post("EditFarmerStep2.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editVillage(final EditGeneralData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            if (!CommonUtil.PatternUtil.isEmpty(data.getProfile()))
                params.put("Profile", new File(data.getProfile()));

            params.put("Phone", data.getConvertPhone());
            params.put("Email", data.getEmail());
            params.put("EmailFlag", (data.isEmailFlag()) ? "Y" : "N");
            params.put("PhoneFlag", (data.isPhoneFlag()) ? "Y" : "N");

            if (!CommonUtil.PatternUtil.isEmpty(data.getUserOldPw()))
                params.put("OldPW", data.getUserOldPw());
            if (!CommonUtil.PatternUtil.isEmpty(data.getUserNewPW()))
                params.put("NewPW", data.getUserNewPW());

            post("EditVillageStep1.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editVillageNext(final EditVillageData data, CenterResponseListener responseHandler) {
        try {
            RequestParams params = new RequestParams();

            params.put("Farm", data.getFarm());
            params.put("Address", data.getAddress());

            if (!CommonUtil.PatternUtil.isEmpty(data.getImage1()))
                params.put("Image1", new File(data.getImage1()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage2()))
                params.put("Image2", new File(data.getImage2()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage3()))
                params.put("Image3", new File(data.getImage3()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage4()))
                params.put("Image4", new File(data.getImage4()));
            if (!CommonUtil.PatternUtil.isEmpty(data.getImage5()))
                params.put("Image5", new File(data.getImage5()));

            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory()))
                params.put("Category", data.getCategory());
            params.put("Introduction", data.getIntroduction());

            post("EditVillageStep2.php", params, responseHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getListFarmerNotice(final NoticeListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("Farmer", String.valueOf(data.getUserIndex()));
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListNoticeFarmer.php", params, responseHandler);
    }

    public static void getListVillageNotice(final NoticeListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("Village", String.valueOf(data.getUserIndex()));
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListNoticeVillage.php", params, responseHandler);
    }

    public static void getListAppNotice(final NoticeListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        post("GetListNotice.php", params, responseHandler);
    }

    public static void getChildFarmerNotice(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("GetViewNoticeFarmer.php", params, responseHandler);
    }

    public static void getChildVillageNotice(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("GetViewNoticeVillage.php", params, responseHandler);
    }

    public static void deleteNotice(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteNotice.php", params, responseHandler);
    }

    public static void getReleaseTodayList(final String category, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Category", category);

        post("GetReleaseToday.php", params, responseHandler);
    }

    public static void getReleaseTodayCategory(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        post("GetReleaseTodayCount.php", params, responseHandler);
    }

    public static void getListFavorite(final FavoriteListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListFavorite.php", params, responseHandler);
    }

    public static void getListFavoriteFarm(String type, String index, String oldIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Type", type);
        params.put("Index", index);
        params.put("OldIndex", oldIndex);

        post("GetListFavoriteFarm.php", params, responseHandler);
    }

    public static void getFarm(final String userType, final String userIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("TargetType", userType);
        params.put("TargetIndex", userIndex);

        post("GetFarm.php", params, responseHandler);
    }

    public static void likeNoMemberDiary(String diary, Context context, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("DiaryIndex", diary);
        params.put("DiaryType", "F");
        params.put("DeviceAddress", Installation.id(context));
        params.put("DeviceType", "android");
        post("LikeDiary.php", params, responseHandler);
    }

    public static void likeDiary(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("LikeFarmer.php", params, responseHandler);
    }

    public static void likeUserDiary(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("LikeUser.php", params, responseHandler);
    }

    public static void deleteDiary(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteDiary.php", params, responseHandler);
    }

    public static void deleteConsumer(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteConsumer.php", params, responseHandler);
    }

    public static void deleteInterview(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteDirect.php", params, responseHandler);
    }

    public static void deleteNormal(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteFreshTableDiary.php", params, responseHandler);
    }

    public static void setFavorite(final String userType, final String userIndex, final String type, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("TargetType", userType);
        params.put("TargetIndex", userIndex);
        params.put("Type", type);

        post("SetFavorite.php", params, responseHandler);
    }

    public static void getListNormalReply(final ReplyListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("DiaryIndex", data.getDiaryIndex());
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }

        post("GetListReplyUser.php", params, responseHandler);
    }

    public static void getListFarmerReply(final ReplyListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("DiaryIndex", data.getDiaryIndex());
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }

        post("GetListReplyFarmer.php", params, responseHandler);
    }

    public static void getListVillageReply(final ReplyListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            params.put("DiaryIndex", data.getDiaryIndex());
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }

        post("GetListReplyVillage.php", params, responseHandler);
    }

    public static void getListReviewReply(final ReplyListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        if (data != null) {
            params.put("DiaryIndex", data.getDiaryIndex());
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }

        post("GetListReplyReviews.php", params, responseHandler);
    }

    public static void getListAdminReply(final ReplyListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (data.getOffset() != 0)
                params.put("Offset", String.valueOf(data.getOffset()));
        }

        post("GetListReplySession.php", params, responseHandler);
    }

    public static void deleteComment(final String boardType, final String commentIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("BoardType", boardType);
        params.put("CommentIndex", commentIndex);

        post("DeleteComment.php", params, responseHandler);
    }

    public static void deleteCommentReview(final String boardType, final String commentIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("BoardType", boardType);
        params.put("CommentIndex", commentIndex);

        post("DeleteCommentReviews.php", params, responseHandler);
    }

    public static void writeCommentReview(String[] BoardType, String[] DiaryIndex, String[] ParentCommentIndex, String[] Description, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        String sendStr = "";
        for (String str : BoardType) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("BoardType", sendStr);

        sendStr = "";
        for (String str : DiaryIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("DiaryIndex", sendStr);

        sendStr = "";
        for (String str : ParentCommentIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("ParentCommentIndex", sendStr);

        sendStr = "";
        for (String str : Description) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }

        params.put("Description", sendStr);

        post("WriteCommentReviews.php", params, responseHandler);
    }

    public static void writeComment(String[] BoardType, String[] DiaryIndex, String[] ParentCommentIndex, String[] Description, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        String sendStr = "";
        for (String str : BoardType) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("BoardType", sendStr);

        sendStr = "";
        for (String str : DiaryIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("DiaryIndex", sendStr);

        sendStr = "";
        for (String str : ParentCommentIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("ParentCommentIndex", sendStr);

        sendStr = "";
        for (String str : Description) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }

        params.put("Description", sendStr);

        post("WriteComment.php", params, responseHandler);
    }

    public static void writeCommentUser(String[] BoardType, String[] DiaryIndex, String[] ParentCommentIndex, String[] Description, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        String sendStr = "";
        for (String str : BoardType) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("BoardType", sendStr);

        sendStr = "";
        for (String str : DiaryIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("DiaryIndex", sendStr);

        sendStr = "";
        for (String str : ParentCommentIndex) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }
        params.put("ParentCommentIndex", sendStr);

        sendStr = "";
        for (String str : Description) {
            if (!sendStr.equals("")) {
                sendStr += "&nbsp;";
            }
            sendStr += str;
        }

        params.put("Description", sendStr);

        post("WriteCommentMultiple.php", params, responseHandler);
    }

    public static void getListBlog(final BlogListData data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (data != null) {
            if (!CommonUtil.PatternUtil.isEmpty(data.getFarmerIndex()))
                params.put("FarmerIndex", data.getFarmerIndex());
            if (!CommonUtil.PatternUtil.isEmpty(data.getCategory()))
                params.put("Category", data.getCategory());
            if (data.getOldIndex() != 0)
                params.put("OldIndex", String.valueOf(data.getOldIndex()));
        }

        post("GetListPost.php", params, responseHandler);
    }

    public static void deleteBlog(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeletePost.php", params, responseHandler);
    }

    public static void getFarmerInfo(final String farmerIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("FarmerIndex", farmerIndex);

        post("GetKakaoFarmer.php", params, responseHandler);
    }

    public static void writePost(final String category, final String blogtype, final String address, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Category", category);
        params.put("BlogType", blogtype);
        params.put("PostingAddress", address);

        post("WritePost.php", params, responseHandler);
    }

    public static void setDeviceAddress(final String DeviceAddress, final String DeviceType, final String UseFlag,
                                        CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("DeviceAddress", DeviceAddress);
        params.put("DeviceType", DeviceType);
        params.put("UseFlag", UseFlag);

        post("SetDeviceAddress.php", params, responseHandler);
    }

    public static void getListDiaryByCategory2(final String category2, String id, int limit, String oldIndex, String oldDate, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Category2", category2);
        params.put("ID", id);
        params.put("Limit", String.valueOf(limit));
        params.put("OldIndex", oldIndex);
        params.put("OldDate", oldDate);

        post("GetListDiaryByCategory2.php", params, responseHandler);
    }

    public static void getKakaoStoryCh(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(url, responseHandler);
    }

    public static void getListNoticeType(String type, String target, String oldIndex, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("Type", type);
        params.put("Target", target);
        params.put("OldIndex", oldIndex);
        post("GetListNoticeType.php", params, responseHandler);
    }

    public static void getDetailNotice(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("GetViewNotice.php", params, responseHandler);
    }

    public static void getDiaryList(final String diary, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("Diary", diary);
        post("GetFarmerDiaryGetIdxList.php", params, responseHandler);
    }

    public static void checkOpenIdEmail(String name, String email, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("Name", name);
        params.put("Email", email);

        post("CheckOpenIdByEmail.php", params, responseHandler);
    }

    public static void checkOpenIdPhone(String name, String phone, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("Name", name);
        params.put("Phone", phone);

        post("CheckOpenIdByPhone.php", params, responseHandler);
    }

    public static void openIdLogin(String toekn, String uid, String type, String name, String phone, String email, String profile, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("AccessToken", toekn);
        params.put("LoginId", uid);
        params.put("LoginCategory", type);
        params.put("Name", name);
        params.put("Phone", phone);
        params.put("Email", email);
        params.put("ProfileImage", profile);

        post("CheckOpenIdLogin.php", params, responseHandler);
    }

    public static void getAddress(CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("GetListDelivery.php", params, responseHandler);
    }

    public static void addAddress(AddressJson addressJson, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("ShippingName", addressJson.getShippingName());
        params.put("PhoneNo", addressJson.getPhoneNo());
        params.put("ZipCode", addressJson.getZipCode());
        params.put("Address", addressJson.getAddress());
        params.put("Address2", addressJson.getAddress2());
        params.put("ZipCodeCategory", addressJson.getZipCodeCategory());

        post("WriteDelivery.php", params, responseHandler);
    }

    public static void delAddress(AddressJson addressJson, CenterResponseListener responseHandler) {
        if (addressJson.getIdx() == null) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("Idx", addressJson.getIdx());
        post("DeleteDelivery.php", params, responseHandler);
    }

    public static void leaveMember(String id, String type, String regId, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("ID", id);
        params.put("Type", type);
        params.put("RegId", regId);

        post("Leave.php", params, responseHandler);
    }

    public static void getUserStoryList(String oldIndex, String limit, String keyword, String userIndex, String isImpressive, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("OldIndex", oldIndex);
        params.put("Keyword", keyword);
        params.put("UserIndex", userIndex);
        params.put("UserIndex", userIndex);
        params.put("Register", isImpressive);
        params.put("Limit", limit);
        post("GetListDirect.php", params, responseHandler);
    }

    public static void getUserStoryDetail(final String data, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Diary", data);
        post("GetViewDirect.php", params, responseHandler);
    }

    public static void writeUserStroye(final WriteDiaryData data, CenterResponseListener responseHandler) {
        try {
            int rowIndex = 0;
            int imageIndex = 0;
            RequestParams params = new RequestParams();

            params.put("BoardType", data.getBoardType());

            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTitle()))
                params.put("BlogTitle", data.getBlogTitle());
            params.put("BlogAlign", data.isbAlign() ? "C" : "L");

            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTag()))
                params.put("BlogTag", data.getBlogTag());

            params.put("SNSNaverBlog", data.isbNaver() ? "Y" : "N");
            params.put("SNSTistory", data.isbTistory() ? "Y" : "N");
            params.put("SNSDaumBlog", data.isbDaum() ? "Y" : "N");
            params.put("SNSFacebook", data.isbFacebook() ? "Y" : "N");
            params.put("SNSTwitter", data.isbTwitter() ? "Y" : "N");

            for (RowsData item : data.getRows()) {
                params.put("Rows[" + rowIndex + "][Type]", item.Type);
                if (item.Type.equals("Text")) {
                    params.put("Rows[" + rowIndex + "][Value]", item.Value);
                } else {
                    params.put("Rows[" + rowIndex + "][Value]", String.valueOf(imageIndex++));
                }
                rowIndex++;
            }

            imageIndex = 0;
            for (String path : data.getImages()) {
                params.put("Images[" + imageIndex++ + "]", new File(path));
            }

            post("WriteDirect.php", params, responseHandler);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editUserStory(final String diary, final WriteDiaryData data, CenterResponseListener responseHandler) {
        try {
            int rowIndex = 0;
            int imageIndex = 0;
            RequestParams params = new RequestParams();

            params.put("Index", diary);
            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTitle()))
                params.put("BlogTitle", data.getBlogTitle());
            params.put("BlogAlign", data.isbAlign() ? "C" : "L");

            if (!CommonUtil.PatternUtil.isEmpty(data.getBlogTag()))
                params.put("BlogTag", data.getBlogTag());

            params.put("SNSNaverBlog", data.isbNaver() ? "Y" : "N");
            params.put("SNSTistory", data.isbTistory() ? "Y" : "N");
            params.put("SNSDaumBlog", data.isbDaum() ? "Y" : "N");
            params.put("SNSFacebook", data.isbFacebook() ? "Y" : "N");
            params.put("SNSTwitter", data.isbTwitter() ? "Y" : "N");

            for (RowsData item : data.getRows()) {
                params.put("Rows[" + rowIndex + "][Type]", item.Type);
                if (item.Type.equals("Text")) {
                    params.put("Rows[" + rowIndex + "][Value]", item.Value);
                } else {
                    params.put("Rows[" + rowIndex + "][Value]", String.valueOf(imageIndex++));
                }
                rowIndex++;
            }

            imageIndex = 0;
            for (String path : data.getImages()) {
                if (path.contains("http")) {

                    File file = ImageLoader.getInstance().getDiskCache().get(path);
                    String pathfile = file.getAbsolutePath();
                    File newFile = new File(pathfile + ".jpg");
                    file.renameTo(newFile);
                    params.put("Images[" + imageIndex++ + "]", newFile);
                } else {
                    params.put("Images[" + imageIndex++ + "]", new File(path));
                }
            }

            post("EditDirect.php", params, responseHandler);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUserStory(final String index, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("Index", index);

        post("DeleteDirect.php", params, responseHandler);
    }

    public static void getDailyLifeList(String oldDate, CenterResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("OldDate", oldDate);
        post("GetListDaily.php", params, responseHandler);
    }

}