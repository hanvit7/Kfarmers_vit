package com.leadplatform.kfarmers.model.database;

import android.database.Cursor;
import android.util.Log;

import com.leadplatform.kfarmers.util.DataCryptUtil;

//sqLite user database 객체화 게터 세터
public class UserDb {
    private final static String TAG = "UserDb";
    private int id;
    private String userID;
    private String userPW;
    private double latitude = 0;
    private double longitude = 0;
    private int autoLoginFlag = 0;
    private int currentUserFlag = 0;
    private int naverFlag = 0;
    private int daumFlag = 0;
    private int tistoryFlag = 0;
    private int facebookFlag = 0;
    private int twitterFlag = 0;
    private int kakaoFlag = 0;
    private String facebookToken;
    private String twitterToken;
    private String twitterSecret;
    private String daumToken;
    private String daumSecret;

    private String naverCategoryNo;
    private String naverCategoryName;

    private String tistoryBlogId;
    private String tistoryLoginId;
    private String tistoryLoginPw;
    private String temporaryDiary;
    private String profileContent;
    private String blogNaver;
    private String blogDaum;
    private String blogTstory;
    private String snsKakaoCh;

    private int snsNaverUse = 0;
    private int snsDaumUse = 0;
    private int snsTistoryUse = 0;
    private int snsFacebookUse = 0;
    private int snsTwitterUse = 0;
    private int snsKakaoUse = 0;

    private String openLoginType;
    private String apiToken;

    public UserDb(String userID, String userPW, boolean autoLoginFlag, boolean currentUserFlag, String openLoginType, String apiToken) {
        Log.d(TAG, "VIT] kf계정, 카카오톡, 네이버 로그인 시 UserDb 생성");
        this.userID = userID;
        this.userPW = userPW;
        this.autoLoginFlag = (autoLoginFlag) ? 1 : 0;
        this.currentUserFlag = (currentUserFlag) ? 1 : 0;
        this.latitude = 0;
        this.latitude = 0;
        this.naverFlag = 0;
        this.daumFlag = 0;
        this.tistoryFlag = 0;
        this.facebookFlag = 0;
        this.twitterFlag = 0;
        this.kakaoFlag = 0;
        this.facebookToken = null;
        this.twitterToken = null;
        this.twitterSecret = null;
        this.daumToken = null;
        this.daumSecret = null;

        this.naverCategoryNo = null;
        this.naverCategoryName = null;

        this.tistoryBlogId = null;
        this.tistoryLoginId = null;
        this.tistoryLoginPw = null;
        this.temporaryDiary = null;
        this.profileContent = null;
        this.blogNaver = null;
        this.blogDaum = null;
        this.blogTstory = null;
        this.snsKakaoCh = null;

        this.snsNaverUse = 0;
        this.snsDaumUse = 0;
        this.snsTistoryUse = 0;
        this.snsFacebookUse = 0;
        this.snsTwitterUse = 0;
        this.snsKakaoUse = 0;

        this.openLoginType = openLoginType;
        this.apiToken = apiToken;
    }

    public UserDb(Cursor cursor) {//커서는 데이터 베이스와 관련있는듯
        if (cursor != null) {
            Log.d(TAG, "VIT] 기존정보 UserDb로 복사사");
            this.id = cursor.getInt(cursor.getColumnIndex(DataBases.User._ID));
            this.userID = cursor.getString(cursor.getColumnIndex(DataBases.User.USER_ID));
            this.userPW = cursor.getString(cursor.getColumnIndex(DataBases.User.USER_PW));
            this.autoLoginFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.AUTO_LOGIN_FLAG));
            this.currentUserFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.CURRENT_USER_FLAG));
            this.latitude = cursor.getDouble(cursor.getColumnIndex(DataBases.User.LATITUDE));
            this.longitude = cursor.getDouble(cursor.getColumnIndex(DataBases.User.LONGITUDE));
            this.naverFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.NAVER_FLAG));
            this.daumFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.DAUM_FLAG));
            this.tistoryFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.TISTORY_FLAG));
            this.facebookFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.FACEBOOK_FLAG));
            this.twitterFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.TWITTER_FLAG));
            this.kakaoFlag = cursor.getInt(cursor.getColumnIndex(DataBases.User.KAKAO_FLAG));
            this.facebookToken = cursor.getString(cursor.getColumnIndex(DataBases.User.FACEBOOK_TOKEN));
            this.twitterToken = cursor.getString(cursor.getColumnIndex(DataBases.User.TWITTER_TOKEN));
            this.twitterSecret = cursor.getString(cursor.getColumnIndex(DataBases.User.TWITTER_SECRET));
            this.daumToken = cursor.getString(cursor.getColumnIndex(DataBases.User.DAUM_TOKEN));
            this.daumSecret = cursor.getString(cursor.getColumnIndex(DataBases.User.DAUM_SECRET));

            this.naverCategoryNo = cursor.getString(cursor.getColumnIndex(DataBases.User.NAVER_CATEGORY_NO));
            this.naverCategoryName = cursor.getString(cursor.getColumnIndex(DataBases.User.NAVER_CATEGORY_NAME));

            this.tistoryBlogId = cursor.getString(cursor.getColumnIndex(DataBases.User.TISTORY_BLOG_ID));
            this.tistoryLoginId = cursor.getString(cursor.getColumnIndex(DataBases.User.TISTORY_LOGIN_ID));
            this.tistoryLoginPw = cursor.getString(cursor.getColumnIndex(DataBases.User.TISTORY_LOGIN_PW));
            this.temporaryDiary = cursor.getString(cursor.getColumnIndex(DataBases.User.TEMPORARY_DIARY));
            this.profileContent = cursor.getString(cursor.getColumnIndex(DataBases.User.PROFILE_CONTENT));
            this.blogNaver = cursor.getString(cursor.getColumnIndex(DataBases.User.BLOG_NAVER));
            this.blogDaum = cursor.getString(cursor.getColumnIndex(DataBases.User.BLOG_DAUM));
            this.blogTstory = cursor.getString(cursor.getColumnIndex(DataBases.User.BLOG_TSTORY));
            this.snsKakaoCh = cursor.getString(cursor.getColumnIndex(DataBases.User.SNS_KAKAO_CH));
            this.openLoginType = cursor.getString(cursor.getColumnIndex(DataBases.User.OPEN_LOGIN_TYPE));
            this.apiToken = cursor.getString(cursor.getColumnIndex(DataBases.User.API_TOKEN));

            this.snsNaverUse = cursor.getInt(cursor.getColumnIndex(DataBases.User.SNS_NAVER_USE));
            this.snsDaumUse = cursor.getInt(cursor.getColumnIndex(DataBases.User.SNS_DAUM_USE));
            this.snsTistoryUse = cursor.getInt(cursor.getColumnIndex(DataBases.User.SNS_TISTORY_USE));
            this.snsFacebookUse = cursor.getInt(cursor.getColumnIndex(DataBases.User.SNS_FACEBOOK_USE));
            this.snsKakaoUse = cursor.getInt(cursor.getColumnIndex(DataBases.User.SNS_KAKAO_USE));
        }
    }

    public int getId() {
        return id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserPW() {
        return userPW;
    }

    public String getUserPwDecrypt() {
        String pw = "";
        try {
            pw = DataCryptUtil.decrypt(DataCryptUtil.dataK, userPW);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pw;
    }

    public void setUserPW(String userPW) {
        this.userPW = userPW;
    }

    public int getAutoLoginFlag() {
        return autoLoginFlag;
    }

    public void setAutoLoginFlag(int autoLoginFlag) {
        this.autoLoginFlag = autoLoginFlag;
    }

    public int getCurrentUserFlag() {
        return currentUserFlag;
    }

    public void setCurrentUserFlag(int currentUserFlag) {
        this.currentUserFlag = currentUserFlag;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNaverFlag() {
        return naverFlag;
    }

    public void setNaverFlag(int naverFlag) {
        this.naverFlag = naverFlag;
    }

    public int getDaumFlag() {
        return daumFlag;
    }

    public void setDaumFlag(int daumFlag) {
        this.daumFlag = daumFlag;
    }

    public int getTistoryFlag() {
        return tistoryFlag;
    }

    public void setTistoryFlag(int tistoryFlag) {
        this.tistoryFlag = tistoryFlag;
    }

    public int getFacebookFlag() {
        return facebookFlag;
    }

    public void setFacebookFlag(int facebookFlag) {
        this.facebookFlag = facebookFlag;
    }

    public int getTwitterFlag() {
        return twitterFlag;
    }

    public void setTwitterFlag(int twitterFlag) {
        this.twitterFlag = twitterFlag;
    }

    public int getKakaoFlag() {
        return kakaoFlag;
    }

    public void setKakaoFlag(int kakaoFlag) {
        this.kakaoFlag = kakaoFlag;
    }

    public String getFacebookToken() {
        return facebookToken;
    }

    public void setFacebookToken(String facebookToken) {
        this.facebookToken = facebookToken;
    }

    public String getTwitterToken() {
        return twitterToken;
    }

    public void setTwitterToken(String twitterToken) {
        this.twitterToken = twitterToken;
    }

    public String getTwitterSecret() {
        return twitterSecret;
    }

    public void setTwitterSecret(String twitterSecret) {
        this.twitterSecret = twitterSecret;
    }

    public String getDaumToken() {
        return daumToken;
    }

    public void setDaumToken(String daumToken) {
        this.daumToken = daumToken;
    }

    public String getDaumSecret() {
        return daumSecret;
    }

    public void setDaumSecret(String daumSecret) {
        this.daumSecret = daumSecret;
    }

    public String getNaverCategoryNo() {
        return naverCategoryNo;
    }

    public void setNaverCategoryNo(String naverCategoryNo) {
        this.naverCategoryNo = naverCategoryNo;
    }

    public String getNaverCategoryName() {
        return naverCategoryName;
    }

    public void setNaverCategoryName(String naverCategoryName) {
        this.naverCategoryName = naverCategoryName;
    }

    public String getTistoryBlogId() {
        return tistoryBlogId;
    }

    public void setTistoryBlogId(String tistoryBlogId) {
        this.tistoryBlogId = tistoryBlogId;
    }

    public String getTistoryLoginId() {
        return tistoryLoginId;
    }

    public void setTistoryLoginId(String tistoryLoginId) {
        this.tistoryLoginId = tistoryLoginId;
    }

    public String getTistoryLoginPw() {
        return tistoryLoginPw;
    }

    public void setTistoryLoginPw(String tistoryLoginPw) {
        this.tistoryLoginPw = tistoryLoginPw;
    }

    public String getTemporaryDiary() {
        return temporaryDiary;
    }

    public void setTemporaryDiary(String temporaryDiary) {
        this.temporaryDiary = temporaryDiary;
    }

    public String getProfileContent() {
        return profileContent;
    }

    public void setProfileContent(String profileContent) {
        this.profileContent = profileContent;
    }

    public String getBlogNaver() {
        return blogNaver;
    }

    public void setBlogNaver(String blogNaver) {
        this.blogNaver = blogNaver;
    }

    public String getBlogDaum() {
        return blogDaum;
    }

    public void setBlogDaum(String blogDaum) {
        this.blogDaum = blogDaum;
    }

    public String getBlogTstory() {
        return blogTstory;
    }

    public void setBlogTstory(String blogTstory) {
        this.blogTstory = blogTstory;
    }

    public String getSnsKakaoCh() {
        return snsKakaoCh;
    }

    public void setSnsKakaoCh(String snsKakaoCh) {
        this.snsKakaoCh = snsKakaoCh;
    }

    public String getOpenLoginType() {
        return openLoginType;
    }

    public void setOpenLoginType(String openLoginType) {
        this.openLoginType = openLoginType;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getSnsNaverUse() {
        return snsNaverUse;
    }

    public int getSnsDaumUse() {
        return snsDaumUse;
    }

    public int getSnsTistoryUse() {
        return snsTistoryUse;
    }

    public int getSnsFacebookUse() {
        return snsFacebookUse;
    }

    public int getSnsTwitterUse() {
        return snsTwitterUse;
    }

    public int getSnsKakaoUse() {
        return snsKakaoUse;
    }

    public void setSnsKakaoUse(int snsKakaoUse) {
        this.snsKakaoUse = snsKakaoUse;
    }
}
