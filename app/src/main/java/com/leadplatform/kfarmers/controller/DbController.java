package com.leadplatform.kfarmers.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.leadplatform.kfarmers.model.database.DataBases;
import com.leadplatform.kfarmers.model.database.InquiryDb;
import com.leadplatform.kfarmers.model.database.SqlDbHelper;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.util.DataCryptUtil;

import java.util.ArrayList;

//sqlite? db 컨트롤러
public class DbController {
    private final static String TAG = "DbController";
    private static SQLiteDatabase sqliteDatabase;

    private static void open(Context context) {
        if (sqliteDatabase == null) {
            Log.d(TAG, "VIT] User, Inquiry 데이터 베이스 open");
            SqlDbHelper sqliteHelper = new SqlDbHelper(context, DataBases.DATABASE_NAME, null, DataBases.DATABASE_VERSION, DataBases.DATABASE_TABLES, DataBases.DATABASE_CREATES);
            sqliteDatabase = sqliteHelper.getWritableDatabase();
        }
    }

    private static void close() {
        if (sqliteDatabase != null) {
            Log.d(TAG, "VIT] User, Inquiry 데이터 베이스 close");
            sqliteDatabase.close();
            sqliteDatabase = null;
        }
    }

    private static long insert(String table, String nullColumnHack, ContentValues values) {
        if (sqliteDatabase != null) {
            Log.d(TAG, "VIT] " + table + " 데이터 베이스 insert");
            return sqliteDatabase.insert(table, nullColumnHack, values);
        }
        return -1;
    }

    private static int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        if (sqliteDatabase != null) {
            Log.d(TAG, "VIT] " + table + " 데이터 베이스 update");
            return sqliteDatabase.update(table, values, whereClause, whereArgs);
        }
        return -1;
    }

    private static Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        if (sqliteDatabase != null) {
            return sqliteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
        return null;
    }

    private static int delete(String table, String whereClause, String[] whereArgs) {
        if (sqliteDatabase != null) {
            Log.d(TAG, "VIT] " + table + " 데이터 베이스 delete");
            return sqliteDatabase.delete(table, whereClause, whereArgs);
        }
        return -1;
    }

    /**
     * User
     */
    public static long insertUser(Context context, UserDb user) {
        Log.d(TAG, "VIT] 신규 유저 생성");
        String pw = "";
        try {
            pw = DataCryptUtil.encrypt(DataCryptUtil.dataK, user.getUserPW());//패스워드 암호화
        } catch (Exception e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(DataBases.User.USER_ID, user.getUserID());
        values.put(DataBases.User.USER_PW, pw);
        values.put(DataBases.User.AUTO_LOGIN_FLAG, user.getAutoLoginFlag());
        values.put(DataBases.User.CURRENT_USER_FLAG, user.getCurrentUserFlag());
        values.put(DataBases.User.OPEN_LOGIN_TYPE, user.getOpenLoginType());

        open(context);
        long id = insert(DataBases.User._TABLENAME, null, values);
        close();

        return id;
    }

    public static int updateUser(Context context, UserDb user) {
        ContentValues values = new ContentValues();
        values.put(DataBases.User.USER_ID, user.getUserID());
        values.put(DataBases.User.USER_PW, user.getUserPW());
        values.put(DataBases.User.AUTO_LOGIN_FLAG, user.getAutoLoginFlag());
        values.put(DataBases.User.CURRENT_USER_FLAG, user.getCurrentUserFlag());
        values.put(DataBases.User.LATITUDE, user.getLatitude());
        values.put(DataBases.User.LONGITUDE, user.getLongitude());
        values.put(DataBases.User.NAVER_FLAG, user.getNaverFlag());
        values.put(DataBases.User.DAUM_FLAG, user.getDaumFlag());
        values.put(DataBases.User.TISTORY_FLAG, user.getTistoryFlag());
        values.put(DataBases.User.FACEBOOK_FLAG, user.getFacebookFlag());
        values.put(DataBases.User.TWITTER_FLAG, user.getTwitterFlag());
        values.put(DataBases.User.KAKAO_FLAG, user.getKakaoFlag());
        values.put(DataBases.User.FACEBOOK_TOKEN, user.getFacebookToken());
        values.put(DataBases.User.TWITTER_TOKEN, user.getTwitterToken());
        values.put(DataBases.User.TWITTER_SECRET, user.getTwitterSecret());
        values.put(DataBases.User.DAUM_TOKEN, user.getDaumToken());
        values.put(DataBases.User.DAUM_SECRET, user.getDaumSecret());

        values.put(DataBases.User.NAVER_CATEGORY_NO, user.getNaverCategoryNo());
        values.put(DataBases.User.NAVER_CATEGORY_NAME, user.getNaverCategoryName());

        values.put(DataBases.User.TISTORY_BLOG_ID, user.getTistoryBlogId());
        values.put(DataBases.User.TISTORY_LOGIN_ID, user.getTistoryLoginId());
        values.put(DataBases.User.TISTORY_LOGIN_PW, user.getTistoryLoginPw());
        values.put(DataBases.User.TEMPORARY_DIARY, user.getTemporaryDiary());
        values.put(DataBases.User.PROFILE_CONTENT, user.getProfileContent());
        values.put(DataBases.User.BLOG_NAVER, user.getBlogNaver());
        values.put(DataBases.User.BLOG_DAUM, user.getBlogDaum());
        values.put(DataBases.User.BLOG_TSTORY, user.getBlogTstory());
        values.put(DataBases.User.SNS_KAKAO_CH, user.getSnsKakaoCh());
        values.put(DataBases.User.OPEN_LOGIN_TYPE, user.getOpenLoginType());
        values.put(DataBases.User.API_TOKEN, user.getApiToken());

        values.put(DataBases.User.SNS_NAVER_USE, user.getSnsNaverUse());
        values.put(DataBases.User.SNS_DAUM_USE, user.getSnsDaumUse());
        values.put(DataBases.User.SNS_TISTORY_USE, user.getSnsTistoryUse());
        values.put(DataBases.User.SNS_FACEBOOK_USE, user.getSnsFacebookUse());
        values.put(DataBases.User.SNS_TWITTER_USE, user.getSnsTwitterUse());
        values.put(DataBases.User.SNS_KAKAO_USE, user.getSnsKakaoUse());

        String whereClause = DataBases.User.USER_ID + "=?";
        String[] whereArgs = {String.valueOf(user.getUserID())};

        open(context);
        int ret = update(DataBases.User._TABLENAME, values, whereClause, whereArgs);
        close();

        return ret;
    }

    public static void updateCurrentUser(Context context, String userID) {
        ArrayList<UserDb> userList = queryCurrentUserList(context);
        if (userList != null) {
            for (UserDb user : userList) {
                user.setCurrentUserFlag(0);
                updateUser(context, user);
            }
        }

        UserDb newUser = queryUser(context, userID);
        if (newUser != null) {
            newUser.setCurrentUserFlag(1);
            updateUser(context, newUser);
        }
    }

    public static UserDb queryCurrentUser(Context context) {
        UserDb user = null;
        String selection = DataBases.User.CURRENT_USER_FLAG + "=?";
        String[] selectionArgs = {String.valueOf(1)};

        open(context);
        Cursor cursor = query(DataBases.User._TABLENAME, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                user = new UserDb(cursor);
            }
            cursor.close();
        }
        close();

        return user;
    }

    public static ArrayList<UserDb> queryCurrentUserList(Context context) {
        ArrayList<UserDb> userList = new ArrayList<UserDb>();
        String selection = DataBases.User.CURRENT_USER_FLAG + "=?";
        String[] selectionArgs = {String.valueOf(1)};

        open(context);
        Cursor cursor = query(DataBases.User._TABLENAME, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                userList.add(new UserDb(cursor));
            }
            cursor.close();
        }
        close();

        return userList;
    }

    public static UserDb queryUser(Context context, String userID) {
        UserDb user = null;
        String selection = DataBases.User.USER_ID + "=?";
        String[] selectionArgs = {userID};

        open(context);
        Cursor cursor = query(DataBases.User._TABLENAME, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = new UserDb(cursor);
            }
            cursor.close();
        }
        close();

        return user;
    }

    public static boolean queryNaverFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getNaverFlag() == 1);
        }
        return false;
    }

    public static int updateNaverFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setNaverFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static int updateNaverCategory(Context context, String categoryNo, String categoryName) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setNaverCategoryNo(categoryNo);
            user.setNaverCategoryName(categoryName);

            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean queryDaumFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getDaumFlag() == 1);
        }
        return false;
    }

    public static int updateDaumFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setDaumFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static int updateDaumSession(Context context, String token, String secret) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setDaumToken(token);
            user.setDaumSecret(secret);
            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean queryTistoryFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getTistoryFlag() == 1);
        }
        return false;
    }

    public static int updateTistoryFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setTistoryFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static int updateTistorySession(Context context, String blogId, String loginId, String loginPw) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setTistoryBlogId(blogId);
            user.setTistoryLoginId(loginId);
            user.setTistoryLoginPw(loginPw);
            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean queryFaceBookFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getFacebookFlag() == 1);
        }
        return false;
    }

    public static int updateFaceBookFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setFacebookFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static int updateFaceBookSession(Context context, String token) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setFacebookToken(token);
            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean queryTwitterFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getTwitterFlag() == 1);
        }
        return false;
    }

    public static int updateTwitterFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setTwitterFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean queryKakaoFlag(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getKakaoFlag() == 1);
        }
        return false;
    }

    public static int updateKakaoFlag(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setKakaoFlag(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static int updateTwitterSession(Context context, String token, String secret) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setTwitterToken(token);
            user.setTwitterSecret(secret);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryTemporaryDiary(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getTemporaryDiary();
        }
        return null;
    }

    public static int updateTemporaryDiary(Context context, String diary) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setTemporaryDiary(diary);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryProfileContent(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getProfileContent();
        }
        return null;
    }

    public static int updateProfileContent(Context context, String profile) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setProfileContent(profile);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryBlogNaver(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getBlogNaver();
        }
        return null;
    }

    public static int updateBlogNaver(Context context, String id) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setBlogNaver(id);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryBlogDaum(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getBlogDaum();
        }
        return null;
    }

    public static int updateBlogDaum(Context context, String id) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setBlogDaum(id);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryBlogTstory(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getBlogTstory();
        }
        return null;
    }

    public static int updateBlogTstory(Context context, String id) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setBlogTstory(id);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String querySnsKakaoCh(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return user.getSnsKakaoCh();
        }
        return null;
    }

    public static int updateSnsKakaoCh(Context context, String id) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setSnsKakaoCh(id);
            return updateUser(context, user);
        }
        return -1;
    }

    public static String queryApiToken(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            Log.d(TAG, "VIT] user 데이터 베이스에서 ApiToken 받아옴");
            return user.getApiToken();
        }
        return "";
    }

    public static int updateApiToken(Context context, String token) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setApiToken(token);
            return updateUser(context, user);
        }
        return -1;
    }

    public static boolean querySnsKakaoUse(Context context) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            return (user.getSnsKakaoUse() == 1);
        }
        return false;
    }

    public static int updateSnsKakaoUse(Context context, boolean flag) {
        UserDb user = queryCurrentUser(context);
        if (user != null) {
            user.setSnsKakaoUse(flag ? 1 : 0);
            return updateUser(context, user);
        }
        return -1;
    }

    public static void clearDb(Context context) {
        open(context);
        delete(DataBases.User._TABLENAME, null, null);
        delete(DataBases.Inquiry._TABLENAME, null, null);
    }

    /**************************************************************
     * ** Inquiry
     **************************************************************/
    public static InquiryDb queryInquiry(Context context, String idx) {
        InquiryDb inquiryDb = null;
        String selection = DataBases.Inquiry.IDX + "=?";
        String[] selectionArgs = {idx};

        open(context);
        Cursor cursor = query(DataBases.Inquiry._TABLENAME, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                inquiryDb = new InquiryDb(cursor);
            }
            cursor.close();
        }
        close();

        return inquiryDb;
    }

    public static int queryInquiryNoReadCount(Context context) {
        int count = 0;
        String selection = DataBases.Inquiry.READ + "=?";
        String[] selectionArgs = {"F"};

        open(context);
        Cursor cursor = query(DataBases.Inquiry._TABLENAME, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        close();

        return count;
    }

    public static long insertInquiry(Context context, InquiryDb inquiryDb) {
        ContentValues values = new ContentValues();
        values.put(DataBases.Inquiry.IDX, inquiryDb.idx);
        values.put(DataBases.Inquiry.CHAT_IDX, inquiryDb.chat_idx);
        values.put(DataBases.Inquiry.READ, inquiryDb.read);

        open(context);
        long id = insert(DataBases.Inquiry._TABLENAME, null, values);
        close();

        return id;
    }

    public static int updateInquiry(Context context, InquiryDb inquiryDb) {
        ContentValues values = new ContentValues();

        values.put(DataBases.Inquiry.CHAT_IDX, inquiryDb.chat_idx);
        values.put(DataBases.Inquiry.READ, inquiryDb.read);

        String whereClause = DataBases.Inquiry.IDX + "=?";
        String[] whereArgs = {String.valueOf(inquiryDb.idx)};

        open(context);
        int ret = update(DataBases.Inquiry._TABLENAME, values, whereClause, whereArgs);
        close();

        return ret;
    }
}
