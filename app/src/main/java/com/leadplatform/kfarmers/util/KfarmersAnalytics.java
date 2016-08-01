package com.leadplatform.kfarmers.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;
import com.leadplatform.kfarmers.BuildConfig;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.snipe.CartItemJson;
//
public class KfarmersAnalytics {
    private final static String TAG = "KfarmersAnalytics";

    public static final String S_SPLASH = "스플래쉬";

    public static final String S_MAIN = "메인";

    public static final String S_HOME = "홈";

    public static final String S_STROY_LIST = "이야기 리스트";
    public static final String S_STROY_DETAIL = "이야기 상세";

    public static final String S_STORY_USER_DETAIL = "생생밥상 상세";

    public static final String S_REVIEW_LIST = "리뷰 리스트";
    public static final String S_REVIEW_DETAIL = "리뷰 상세";

    public static final String S_PRODUCT_LIST = "장터 리스트";
    public static final String S_PRODUCT_DETAIL = "장터 상세";

    public static final String S_PRODUCT_OPTION = "상품 옵션";

    public static final String S_EVENT_LIST = "서포터즈 리스트";
    public static final String S_EVENT_DETAIL = "서포터즈 상세";

    public static final String S_SEARCH_MAIN = "검색";

    public static final String S_WRITE = "글쓰기";
    public static final String S_WRITE_SNS = "SNS 가져오기";
    public static final String S_WRITE_MODIFY = "글수정";

    public static final String S_MYPAGE_FARMER = "농부 마이페이지";
    public static final String S_MYPAGE_VILLAGE = "체험 마이페이지";
    public static final String S_MYPAGE_USER = "소비자 마이페이지";
    public static final String S_MYPAGE_NONMEMBER = "비회원 마이페이지";


    public static final String S_EDIT_FARMER = "농부 정보수정";
    public static final String S_EDIT_VILLAGE = "체험 정보수정";
    public static final String S_EDIT_USER = "소비자 정보수정";

    public static final String S_PRODUCER_COMMUNICATION_LIST  = "생산자 소통 리스트";
    public static final String S_PRODUCER_COMMUNICATION_DETAIL  = "생산자 소통 상세";

    public static final String S_ORDER_LIST = "주문내역 리스트";
    public static final String S_ORDER_DETAIL= "주문내역 상세";

    public static final String S_POINT_LIST = "포인트 리스트";

    public static final String S_CART = "장바구니";

    public static final String S_PRODUCT_MANAGE_LIST = "상품관리 리스트";
    public static final String S_PRODUCT_MANAGE_ADD = "상품관리 등록";
    public static final String S_PRODUCT_MANAGE_EDIT = "상품관리 수정";

    public static final String S_FAVORITE = "관심있는";
    public static final String S_FAVORITE_FARM = "관심있는 회원";

    public static final String S_SHIP_MANAGE_LIST = "출하일정 리스트";
    public static final String S_SHIP_MANAGE_EDIT = "출하일정 수정";

    public static final String S_EXPERIENCE_MANAGE_LIST = "체험정보 리스트";
    public static final String S_EXPERIENCE_MANAGE_EDIT = "체험정보 수정";

    public static final String S_RECIPE_LIST = "레시피 리스트";
    public static final String S_RECIPE_DETAIL= "레시피 상세";

    public static final String S_IMPRESSIVE = "인상깊은";

    public static final String S_Terms = "이용약관";

    public static final String S_SERVICEINFO = "서비스 안내";

    public static final String S_COMPANY = "사업자 정보";

    public static final String S_SETTING = "설정";

    public static final String S_INVITE = "친구초대";

    public static final String S_SNS_CONNECT = "SNS 연결";

    public static final String S_FARMER_SHOP = "농장숍";

    public static final String S_PLAN_SHOP = "기획전숍";

    public static final String S_REPLY = "댓글";

    public static final String S_PAYMENT = "주문하기";

    public static final String S_PAYMENT_WEB = "PG결제";

    public static final String S_FARM = "농장";

    public static final String S_FARMMAP = "농장지도";

    public static final String S_FARMINFO = "농장정보";

    public static final String S_LOGIN = "로그인";

    public static final String S_LOST = "계정찾기";

    public static final String S_PUSH = "푸시";

    public static final String S_INQUIRY_LIST = "문의 리스트";
    public static final String S_INQUIRY_DETAIL = "문의 상세";

    private static KfarmersAnalytics mKfarmersAnalytics;
    private Tracker mTracker;

    public KfarmersAnalytics(Context paramContext) {
        GoogleAnalytics.getInstance(paramContext).setDryRun(BuildConfig.DEBUG);
        mTracker = GoogleAnalytics.getInstance(paramContext).newTracker(R.xml.global_tracker);
        mTracker.enableAdvertisingIdCollection(true);
    }

    public static void destroy() {
        mKfarmersAnalytics = null;
    }

    public static void init(Context context) {
        if (BuildConfig.DEBUG) {
            mKfarmersAnalytics = null;
            return;
        }
        mKfarmersAnalytics = new KfarmersAnalytics(context);
        Log.d(TAG, "VIT] init : " + TAG);
    }

    public static void onScreen(String param) {
        if (mKfarmersAnalytics == null)
            return;
        mKfarmersAnalytics.onScreenSend(param, null);
    }

    public static void onScreen(String param1, String param2) {
        if (mKfarmersAnalytics == null)
            return;
        mKfarmersAnalytics.onScreenSend(param1, param2);
    }

    private void onScreenSend(String param1, String param2) {
        if (this.mTracker == null)
            return;
        this.mTracker.setScreenName(param1);
        HitBuilders.ScreenViewBuilder screenViewBuilder = new HitBuilders.ScreenViewBuilder();
        if (param2 != null) {
            Product localProduct = new Product();
            localProduct.setName(param2);
            screenViewBuilder.addProduct(localProduct);
        }
        this.mTracker.send(screenViewBuilder.build());
    }

    public static void onClick(String param1, String param2, String param3) {
        if (mKfarmersAnalytics == null)
            return;
        mKfarmersAnalytics.onClickSend(param1, param2, param3);
    }

    private void onClickSend(String param1, String param2, String param3) {
        if (this.mTracker == null)
            return;
        HitBuilders.EventBuilder localEventBuilder = new HitBuilders.EventBuilder();
        if (param1 != null)
            localEventBuilder.setCategory(param1);
        if (param2 != null)
            localEventBuilder.setAction(param2);
        if (param3 != null)
            localEventBuilder.setLabel(param3);
        this.mTracker.send(localEventBuilder.build());
    }

    public static void onEcommerce(String orderNo,double price) {
        if (mKfarmersAnalytics == null)
            return;
        mKfarmersAnalytics.onEcommerceSend(orderNo, price);
    }

    private void onEcommerceSend(String orderNo,double price) {
        if (this.mTracker == null)
            return;
        HitBuilders.TransactionBuilder transactionBuilder = new HitBuilders.TransactionBuilder();
        transactionBuilder.setTransactionId(orderNo)
                .setAffiliation("KFarmers")
                .setRevenue(price)
                .setTax(0)
                .setShipping(0)
                .setCurrencyCode("KRW");
        this.mTracker.send(transactionBuilder.build());
    }

    public static void onEcommerceItem(String orderNo,CartItemJson itemJson) {
        if (mKfarmersAnalytics == null)
            return;
        mKfarmersAnalytics.onEcommerceItemSend(orderNo, itemJson);
    }

    private void onEcommerceItemSend(String orderNo,CartItemJson itemJson) {
        if (this.mTracker == null)
            return;
        HitBuilders.ItemBuilder transactionBuilder = new HitBuilders.ItemBuilder();
        transactionBuilder.setTransactionId(orderNo)
                .setName(itemJson.option_name)
                .setSku(itemJson.idx)
                .setCategory("")
                .setPrice(Double.valueOf(itemJson.buyprice))
                .setQuantity(Long.valueOf(itemJson.cnt))
                .setCurrencyCode("KRW");
        this.mTracker.send(transactionBuilder.build());
    }

}
