package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.McryptUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
//서버에 데이터 요청? 토큰에 대한 내용 많음
public class SnipeApiController {
    private final static String TAG = "SnipeApiController";
    private static final String BASE_URL = Constants.KFARMERS_SNIPE_FULL_DOMAIN + "/api/";

    private static String CLIENT_TOKEN = ""; // Token

    private static int expiredTokenRetryCnt = 0;
    private static String backupUrl;
    private static RequestParams backupParams;
    private static SnipeResponseListener backupCenterResponseListener;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static String getClientToken() {
        return CLIENT_TOKEN;
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private static void initHttpClientParams() {
        client.setTimeout(30000);
    }

    public static void setToken(String token) {//토큰 설정
        CLIENT_TOKEN = token;
        if (token != null)
            McryptUtil.setKey(McryptUtil.makeKey(token));//키(암호화)생성
    }

    private static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (client != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, " POST Url = " + url);
                Log.d(TAG, " POST Params = " + params.toString());
            }

            expiredTokenRetryCnt = 0;
            backupUrl = url;
            backupParams = params;

            if (CLIENT_TOKEN != null && !CLIENT_TOKEN.isEmpty()) {
                params.put("token", CLIENT_TOKEN);
            }

            backupCenterResponseListener = (SnipeResponseListener) responseHandler;

            client.setUserAgent(System.getProperty("http.agent"));
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }
    }

    public static void expiredToken(final Context context) {
        if (expiredTokenRetryCnt == 0) {

            RequestParams params = new RequestParams();
            UserDb user = DbController.queryCurrentUser(context);
            if (user != null && user.getAutoLoginFlag() == 1) {
                params.put("id", user.getUserID());
                params.put("password", user.getUserPwDecrypt());
            }

            if (client != null) {
                expiredTokenRetryCnt++;
                initHttpClientParams();
                client.setUserAgent(System.getProperty("http.agent"));
                client.post(getAbsoluteUrl("token"), params, new ExpiredTokenListener(context));
                return;
            }
        }

        if (BuildConfig.DEBUG) {
            Log.e(TAG, " expiredSession() ==  ERROR !!");
            Log.e(TAG, " expiredSession() ==  expiredSessionRetryCnt = " + expiredTokenRetryCnt);
        }
        backupCenterResponseListener.onFailure(-1, null, null, new Throwable());
        backupCenterResponseListener.onFinish();
    }

    private static class ExpiredTokenListener extends AsyncHttpResponseHandler {
        private Context context;

        public ExpiredTokenListener(Context context) {
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
                    final String code = root.path("code").asText();
                    final String error = root.path("error").asText();
                    final String data = root.path("data").asText();

                    if (code.equals("600")) {
                        // 토큰연장 성공. 기존 서비스 재요청.
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, " ExpiredTokenListener() ==  SUCCESS !!");
                        }

                        if (client != null) {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, " POST Url = " + backupUrl);
                                Log.e(TAG, " POST Params = " + backupParams.toString());
                            }

                            DbController.updateApiToken(context, data);
                            CLIENT_TOKEN = data;

                            if (backupUrl.equals("token")) {
                                backupUrl = null;
                                backupParams = null;
                            } else {
                                RequestParams tempParams = new RequestParams();
                                backupParams.remove("token");
                                try {
                                    HashMap<String, String> oriHashMap = new HashMap<>();
                                    for (String key : backupParams.getUrlParams().keySet()) {
                                        oriHashMap.put(key, McryptUtil.decryptString(backupParams.getUrlParams().get(key)));
                                    }

                                    McryptUtil.setKey(McryptUtil.makeKey(data));

                                    for (String key : oriHashMap.keySet()) {
                                        tempParams.put(key, McryptUtil.enc(oriHashMap.get(key)));
                                    }

                                    backupParams = tempParams;
                                    backupParams.add("token", CLIENT_TOKEN);
                                    initHttpClientParams();
                                    client.post(getAbsoluteUrl(backupUrl), backupParams, backupCenterResponseListener);

                                } catch (Exception e) {
                                    Log.e(TAG, " ExpiredTokenListener() ==  ERROR = change");
                                    McryptUtil.setKey(McryptUtil.makeKey(data));
                                }
                            }
                        }
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, " ExpiredTokenListener() ==  ERROR !!");
                        }
                        backupCenterResponseListener.onSuccess(Integer.valueOf(code), data, error);
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

    public static void getToken(String id, String pw, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        if (id != null && !id.isEmpty()) {
            params.put("id", id);
            params.put("password", pw);
        }
        post("token", params, responseHandler);
    }

    public static void tokenExpired(SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("token/expire", params, responseHandler);
    }

    public static void getProductListKeyword(String keyword, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("limit", McryptUtil.enc("9999"));
        params.put("keyword", McryptUtil.enc(keyword));
        params.put("bankaccount", McryptUtil.enc("T"));
        post("product/list", params, responseHandler);
    }

    public static void getProductListRecomend(String limit, String found, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("limit", McryptUtil.enc(limit));
        params.put("found", McryptUtil.enc(found));
        params.put("foodcategory", McryptUtil.enc(String.valueOf(0)));
        params.put("recommend", McryptUtil.enc("T"));

        post("product/list", params, responseHandler);//홈화면
    }

    public static void getFarmShopProductList(String limit, String index, String category, String id, String foodCategory, boolean isRecommend, String statusData, String type, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("limit", McryptUtil.enc(limit));
        params.put("index", McryptUtil.enc(index));
        params.put("category", McryptUtil.enc(category));
        params.put("id", McryptUtil.enc(id));
        params.put("foodcategory", McryptUtil.enc(foodCategory));
        params.put("bankaccount", McryptUtil.enc("T"));

        if (isRecommend) {
            params.put("recommend", McryptUtil.enc("T"));
        }
        if (statusData != null) {
            params.put("display", McryptUtil.enc(statusData));
        }

        if (type != null) {
            params.put("verification", McryptUtil.enc(type));
        }

        post("product/list", params, responseHandler);
    }

    public static void getProductList(String limit, String index, String category, String id, String foodCategory, boolean isRecommend, String statusData, String type, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("limit", McryptUtil.enc(limit));
        params.put("index", McryptUtil.enc(index));
        params.put("category", McryptUtil.enc(category));
        params.put("id", McryptUtil.enc(id));
        params.put("foodcategory", McryptUtil.enc(foodCategory));

        if (isRecommend) {
            params.put("recommend", McryptUtil.enc("T"));
        }
        if (statusData != null) {
            params.put("display", McryptUtil.enc(statusData));
        }
        if (type != null) {
            params.put("verification", McryptUtil.enc(type));
        }

        post("product/list", params, responseHandler);//장터, 농장블로그
    }

    public static void getProductItem(JSONObject jsonObject, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("item", McryptUtil.enc(jsonObject.toString()));
        post("product/item", params, responseHandler);//장터 본문
    }

    public static void getProductVideo(String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        post("product/video", params, responseHandler);
    }

    public static void getProductPlan(String page, String limit, String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        params.put("exhibition", McryptUtil.enc(id.toString()));
        post("product/detail", params, responseHandler);
    }

    public static void InsertProduct(String id, String product, String option, HashMap<String, String> images, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("product", McryptUtil.enc(product.toString()));
        params.put("option", McryptUtil.enc(option.toString()));

        for (String key : images.keySet()) {
            params.put("images[" + key + "]", McryptUtil.enc(images.get(key)));
        }
        post("product/register", params, responseHandler);
    }

    public static void updateProduct(String id, String product, String option, HashMap<String, String> images, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("product", McryptUtil.enc(product.toString()));
        params.put("option", McryptUtil.enc(option.toString()));

        for (String key : images.keySet()) {
            params.put("images[" + key + "]", McryptUtil.enc(images.get(key)));
        }

        post("product/update", params, responseHandler);
    }

    public static void delProduct(String id, String item, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("item", McryptUtil.enc(item.toString()));
        post("product/delete", params, responseHandler);
    }

    public static void insertCartItem(String id, String buy_direct, JSONArray jsonArray, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("id", McryptUtil.enc(id));
        params.put("buy_direct", McryptUtil.enc(buy_direct));
        params.put("item", McryptUtil.enc(jsonArray.toString()));

        post("cart/item", params, responseHandler);
    }

    public static void updateCartItem(String id, String buy_direct, JSONArray jsonArray, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("id", McryptUtil.enc(id));
        params.put("buy_direct", McryptUtil.enc(buy_direct));
        params.put("item", McryptUtil.enc(jsonArray.toString()));

        post("cart/update", params, responseHandler);
    }

    public static void deleteCartItem(String id, JSONArray jsonArray, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();

        params.put("id", McryptUtil.enc(id));
        params.put("item", McryptUtil.enc(jsonArray.toString()));

        post("cart/delete", params, responseHandler);
    }

    public static void getCartItem(String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));

        post("cart/list", params, responseHandler);
    }

    public static void getCheckOutInfo(String id, JSONObject jsonObject, String buyDirect, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        params.put("item", McryptUtil.enc(jsonObject.toString()));
        params.put("buy_direct", McryptUtil.enc(buyDirect));

        post("order/checkout", params, responseHandler);
    }

    public static void callCheckOutOrder(String id, JSONObject sender, JSONObject receiver, JSONObject info, JSONObject item, JSONObject tax, String buy_direct, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        params.put("sender", McryptUtil.enc(sender.toString()));
        params.put("receiver", McryptUtil.enc(receiver.toString()));
        params.put("info", McryptUtil.enc(info.toString()));
        params.put("item", McryptUtil.enc(item.toString()));
        params.put("tax", McryptUtil.enc(tax.toString()));
        params.put("buy_direct", McryptUtil.enc(buy_direct));

        post("order/call", params, responseHandler);
    }

    public static void getOrderItem(String orderNo, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("order", McryptUtil.enc(orderNo.toString()));
        post("order/item", params, responseHandler);
    }

    public static void getOrderList(String id, String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        post("mypage/order/list", params, responseHandler);
    }

    public static void getOrderDetail(String id, String orderNo, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("order", McryptUtil.enc(orderNo.toString()));
        post("mypage/order/item", params, responseHandler);
    }

    public static void getOrderConfirm(String id, String orderNo, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("order", McryptUtil.enc(orderNo.toString()));
        post("mypage/order/status", params, responseHandler);
    }

    public static void getPointList(String id, String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("page", McryptUtil.enc(page));
        params.put("limit", McryptUtil.enc(limit));
        post("mypage/point/list", params, responseHandler);
    }

    public static void getEventList(String page, String limit, String in_progress, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        params.put("in_progress", McryptUtil.enc(in_progress.toString()));
        post("event/list", params, responseHandler);//홈화면, 서포터즈
    }

    public static void getEventDetail(String idx, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("event", McryptUtil.enc(idx.toString()));
        post("event/item", params, responseHandler);
    }

    public static void getEventComment(String idx, String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("event", McryptUtil.enc(idx.toString()));
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        post("event/comment", params, responseHandler);
    }

    public static void insertEventJoin(String idx, String id, String hp, String nickName, String comment, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("event", McryptUtil.enc(idx.toString()));
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("hp", McryptUtil.enc(hp.toString()));
        params.put("nickname", McryptUtil.enc(nickName.toString()));
        params.put("comment", McryptUtil.enc(comment.toString()));

        post("event/join", params, responseHandler);
    }

    public static void getEventidentify(String idx, String id, String hp, String identify, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("event", McryptUtil.enc(idx.toString()));
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("hp", McryptUtil.enc(hp.toString()));
        params.put("identify", McryptUtil.enc(identify.toString()));

        post("event/identify", params, responseHandler);
    }

    public static void checkChatRoom(String senderId, String receiverId, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("sender", McryptUtil.enc(senderId.toString()));
        params.put("receiver", McryptUtil.enc(receiverId.toString()));

        post("mypage/inquire/chat", params, responseHandler);
    }

    public static void insertChat(String chat, String member, String message, String type, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("chat", McryptUtil.enc(chat.toString()));
        params.put("id", McryptUtil.enc(member.toString()));
        params.put("message_type", McryptUtil.enc(type.toString()));
        params.put("message", McryptUtil.enc(message.toString()));

        post("mypage/inquire/message", params, responseHandler);
    }

    public static void getChatList(String member, String limit, String page, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(member.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        params.put("page", McryptUtil.enc(page.toString()));

        post("mypage/inquire/list", params, responseHandler);
    }

    public static void getChatItem(String chat, String member, String limit, String index, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("chat", McryptUtil.enc(chat.toString()));
        params.put("id", McryptUtil.enc(member.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        params.put("index", McryptUtil.enc(index.toString()));

        post("mypage/inquire/item", params, responseHandler);
    }

    public static void getChatManager(SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("mypage/inquire/manager", params, responseHandler);
    }

    public static void getEvaluationLIst(String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));
        post("evaluation/list", params, responseHandler);
    }

    public static void getEvaluationValid(String place, String item, String code, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("place", McryptUtil.enc(place.toString()));
        params.put("item", McryptUtil.enc(item.toString()));
        params.put("code", McryptUtil.enc(code.toString()));

        post("evaluation/valid", params, responseHandler);
    }

    public static void getEvaluationItem(String place, String item, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("place", McryptUtil.enc(place.toString()));
        params.put("item", McryptUtil.enc(item.toString()));

        post("evaluation/item", params, responseHandler);
    }

    public static void getReviewItems(String id, String code, String order, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("code", McryptUtil.enc(code.toString()));
        if (order != null && !order.isEmpty())
            params.put("order", McryptUtil.enc(order.toString()));

        post("reviews/item", params, responseHandler);
    }

    public static void getReviewList(String code, String recommend, String id, String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("code", McryptUtil.enc(code.toString()));
        params.put("recommend", McryptUtil.enc(recommend.toString()));
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));

        post("reviews/list", params, responseHandler);//장터 본문
    }

    public static void addReview(String reviews, String order, String division, String id, String code, String comment, String rating, HashMap<String, String> newImages, HashMap<String, String> image_del, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        if (reviews != null && !reviews.isEmpty()) {
            params.put("reviews", McryptUtil.enc(reviews.toString()));
        }
        params.put("division", McryptUtil.enc(division.toString()));
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("code", McryptUtil.enc(code.toString()));
        params.put("comment", McryptUtil.enc(comment.toString()));
        params.put("rating", McryptUtil.enc(rating.toString()));
        if (order != null && !order.isEmpty())
            params.put("order", McryptUtil.enc(order.toString()));

        for (String key : newImages.keySet()) {
            params.put("images[" + key + "]", McryptUtil.enc(newImages.get(key)));
        }

        for (String key : image_del.keySet()) {
            params.put("image_del[" + key + "]", McryptUtil.enc(image_del.get(key)));
        }
        post("reviews/evaluation", params, responseHandler);
    }

    public static void getRecipeList(String page, String limit, String code, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));

        if (code != null && !code.isEmpty()) params.put("code", McryptUtil.enc(code.toString()));

        post("recipe/list", params, responseHandler);//홈화면, 레시피
    }

    public static void getRecipeDetail(String recipe, String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("recipe", McryptUtil.enc(recipe.toString()));
        params.put("id", McryptUtil.enc(id.toString()));

        post("recipe/item", params, responseHandler);
    }

    public static void setRecipeRecommend(String recipe, String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("recipe", McryptUtil.enc(recipe.toString()));
        params.put("id", McryptUtil.enc(id.toString()));

        post("recipe/recommend", params, responseHandler);
    }

    public static void setRecipeReply(String recipe, String page, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("recipe", McryptUtil.enc(recipe.toString()));
        params.put("page", McryptUtil.enc(page.toString()));
        params.put("limit", McryptUtil.enc(limit.toString()));

        post("recipe/comment/list", params, responseHandler);
    }

    public static void addRecipeReply(String recipe, String id, String comment, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("recipe", McryptUtil.enc(recipe.toString()));
        params.put("id", McryptUtil.enc(id.toString()));
        params.put("comment", McryptUtil.enc(comment.toString()));

        post("recipe/comment", params, responseHandler);
    }

    public static void getUserUsePoint(String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        post("mypage/point/provide", params, responseHandler);
    }

    public static void getProductExhibition(SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("page", McryptUtil.enc("1"));
        params.put("limit", McryptUtil.enc("99"));
        post("product/exhibition", params, responseHandler);//장터
    }

    public static void getOrderGeneralList(String id, String index, String limit, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        params.put("index", McryptUtil.enc(limit));
        params.put("limit", McryptUtil.enc(limit));

        post("mypage/order/gonggu/list", params, responseHandler);
    }

    public static void getOrderGeneralDetail(String member, String order, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("member", McryptUtil.enc(member));
        params.put("order", McryptUtil.enc(order));
        post("mypage/order/gonggu/item", params, responseHandler);
    }

    public static void getOrderGeneralStatus(String member, String order, String status, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("member", McryptUtil.enc(member));
        params.put("order", McryptUtil.enc(order));
        params.put("status", McryptUtil.enc(status));
        post("mypage/order/gonggu/status", params, responseHandler);
    }

    public static void getOrderGeneralDelivery(String member, String order, String company, String code, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("member", McryptUtil.enc(member));
        params.put("order", McryptUtil.enc(order));
        params.put("company", McryptUtil.enc(company));
        params.put("code", McryptUtil.enc(code));
        post("mypage/order/gonggu/delivery", params, responseHandler);
    }

    public static void getDeliveryCompany(SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("mypage/delivery", params, responseHandler);
    }

    public static void getAppVersion(SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        post("version", params, responseHandler);
    }

    public static void farmMultiMgs(String id, String msg, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        params.put("message", McryptUtil.enc(msg));
        post("inquire/multi/message", params, responseHandler);
    }

    public static void farmMultiMgsCount(String id, SnipeResponseListener responseHandler) {
        RequestParams params = new RequestParams();
        params.put("id", McryptUtil.enc(id));
        post("inquire/multi/count", params, responseHandler);
    }


}
