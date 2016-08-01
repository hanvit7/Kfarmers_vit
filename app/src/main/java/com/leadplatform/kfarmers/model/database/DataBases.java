package com.leadplatform.kfarmers.model.database;

import android.provider.BaseColumns;

public final class DataBases
{
    public static final String DATABASE_NAME = "kFarmers.db";
    public static final int DATABASE_VERSION = 8;
    public static final String[] DATABASE_TABLES = { User._TABLENAME, Inquiry._TABLENAME };
    public static final String[] DATABASE_CREATES = { User._CREATE , Inquiry._CREATE };
    
    public static final class User implements BaseColumns
    {
        public static final String USER_ID = "user_id";
        public static final String USER_PW = "user_pw";
        public static final String CURRENT_USER_FLAG = "current_user_flag";
        public static final String AUTO_LOGIN_FLAG = "auto_login_flag";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String NAVER_FLAG = "naver_flag";
        public static final String DAUM_FLAG = "daum_flag";
        public static final String TISTORY_FLAG = "tistory_flag";
        public static final String FACEBOOK_FLAG = "facebook_flag";
        public static final String TWITTER_FLAG = "twitter_flag";
        public static final String KAKAO_FLAG = "kakao_flag";
        public static final String FACEBOOK_TOKEN = "facebook_token";
        public static final String TWITTER_TOKEN = "twitter_token";
        public static final String TWITTER_SECRET = "twitter_secret";
        public static final String DAUM_TOKEN = "daum_token";
        public static final String DAUM_SECRET = "daum_secret";
        
        public static final String NAVER_CATEGORY_NO = "naver_category_no";
        public static final String NAVER_CATEGORY_NAME = "naver_category_name";
        
        public static final String TISTORY_BLOG_ID = "tistory_blog_id";
        public static final String TISTORY_LOGIN_ID = "tistory_login_id";
        public static final String TISTORY_LOGIN_PW = "tistory_login_pw";
        public static final String TEMPORARY_DIARY = "temporary_diary";
        public static final String PROFILE_CONTENT = "profile_content";
        public static final String BLOG_NAVER = "blog_naver";
        public static final String BLOG_DAUM = "blog_daum";
        public static final String BLOG_TSTORY = "blog_tstory";
        public static final String SNS_KAKAO_CH = "sns_kakao_ch";
        public static final String OPEN_LOGIN_TYPE = "onpen_login_type";

        public static final String SNS_NAVER_USE = "sns_naver_use";
        public static final String SNS_DAUM_USE = "use_daum_use";
        public static final String SNS_TISTORY_USE = "use_tistory_use";
        public static final String SNS_FACEBOOK_USE = "use_facebook_use";
        public static final String SNS_TWITTER_USE = "use_twitter_use";
        public static final String SNS_KAKAO_USE = "use_kakao_use";

        public static final String API_TOKEN = "api_token";

        public static final String _TABLENAME = "user";
        public static final String _CREATE = 
                "create table if not exists " + _TABLENAME +"("
                        + _ID +" integer primary key autoincrement , "
                        + USER_ID +" text not null unique, "
                        + USER_PW +" text not null , "
                        + AUTO_LOGIN_FLAG +" integer null default 0 , "
                        + CURRENT_USER_FLAG +" integer null default 0 ,"
                        + LATITUDE + " real null default 0 ,"
                        + LONGITUDE + " real null default 0 ,"
                        + NAVER_FLAG + " integer null default 0 , "
                        + DAUM_FLAG + " integer null default 0 , "
                        + TISTORY_FLAG + " integer null default 0 , "
                        + FACEBOOK_FLAG + " integer null default 0 , "
                        + TWITTER_FLAG + " integer null default 0 , "
                        + KAKAO_FLAG + " integer null default 0 , "
                        + FACEBOOK_TOKEN +" text , "
                        + TWITTER_TOKEN +" text , "
                        + TWITTER_SECRET +" text , "
                        + DAUM_TOKEN +" text , "
                        + DAUM_SECRET +" text , "
                        /*+ NAVER_LOGIN_ID +" text , "
                        + NAVER_API_PW +" text , "*/
                        + NAVER_CATEGORY_NO +" text , "
                        + NAVER_CATEGORY_NAME +" text , "
                        + TISTORY_BLOG_ID + " text ,"
                        + TISTORY_LOGIN_ID + " text ,"
                        + TISTORY_LOGIN_PW + " text ,"
                        + TEMPORARY_DIARY + " text ,"
                        //+ PROFILE_CONTENT +" text );";
                        + PROFILE_CONTENT + " text ,"
                        + BLOG_NAVER + " text ,"
                        + BLOG_DAUM + " text ,"
                        + BLOG_TSTORY + " text ,"
                        + SNS_KAKAO_CH + " text ,"
                        + OPEN_LOGIN_TYPE + " text ,"
                        + SNS_NAVER_USE + " integer null default 0 , "
                        + SNS_DAUM_USE + " integer null default 0 , "
                        + SNS_TISTORY_USE + " integer null default 0 , "
                        + SNS_FACEBOOK_USE + " integer null default 0 , "
                        + SNS_TWITTER_USE + " integer null default 0 , "
                        + SNS_KAKAO_USE + " integer null default 0 , "
                        + API_TOKEN +" text );";
    }

    public static final class Inquiry implements BaseColumns
    {
        public static final String _TABLENAME = "inquiry";

        public static final String IDX = "idx";
        public static final String CHAT_IDX = "chat_idx";
        public static final String READ = "read";

        public static final String _CREATE =
                "create table if not exists " + _TABLENAME +"("
                        + _ID +" integer primary key autoincrement , "
                        + IDX +" text not null unique , "
                        + CHAT_IDX +" text , "
                        + READ +" text not null );";
    }
}
