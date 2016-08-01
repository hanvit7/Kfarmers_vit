package com.leadplatform.kfarmers.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.leadplatform.kfarmers.util.DataCryptUtil;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;

public class SqlDbHelper extends SQLiteOpenHelper
{
    private String[] databaseTables = null;
    private String[] databaseCreates = null;

    private Context mContext = null;

    public SqlDbHelper(Context context, String databaseName, CursorFactory factory, int databaseVersion, String[] databaseTables,
            String[] databaseCreates)
    {
        super(context, databaseName, factory, databaseVersion);
        this.databaseTables = databaseTables;
        this.databaseCreates = databaseCreates;

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        if (databaseCreates != null)
        {
            for (String create : databaseCreates)
            {
                db.execSQL(create);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    	switch (oldVersion) {
    		case 1:
    			db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.BLOG_NAVER + " text");
    			db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.BLOG_DAUM + " text");
    			db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.BLOG_TSTORY + " text");
    			db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_KAKAO_CH + " text");
			case 2:
				db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.OPEN_LOGIN_TYPE + " text");
			case 3:
				db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.NAVER_CATEGORY_NO + " text");
				db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.NAVER_CATEGORY_NAME + " text");
				db.execSQL("update user set naver_flag = 0");
                try {
                    NaverLoginHelper.naverLogout(mContext);
                }
                catch (Exception e)
                {}
			case 4:
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.API_TOKEN + " text");

                try {
                    UserDb user = null;
                    String selection = DataBases.User.CURRENT_USER_FLAG + "=?";
                    String[] selectionArgs = { String.valueOf(1) };

                    String selectionDel = DataBases.User.CURRENT_USER_FLAG + "!=?";
                    db.delete(DataBases.User._TABLENAME,selectionDel,selectionArgs);

                    Cursor cursor = db.query(DataBases.User._TABLENAME, null, selection, selectionArgs, null, null, null);

                    if (cursor != null)
                    {
                        if (cursor.getCount() > 0)
                        {
                            cursor.moveToFirst();
                            user = new UserDb(cursor);
                        }
                        cursor.close();
                    }
                    if (user != null)
                    {
                        String pw = DataCryptUtil.encrypt(DataCryptUtil.dataK, user.getUserPW());
                        ContentValues cv = new ContentValues();
                        cv.put(DataBases.User.USER_PW,pw);

                        String selectionUpdate = DataBases.User.USER_ID + "=?";
                        String[] selectionUpdateArgs = { user.getUserID() };
                        db.update(DataBases.User._TABLENAME,cv,selectionUpdate,selectionUpdateArgs);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            case 5:
				    db.execSQL(DataBases.Inquiry._CREATE);
			case 6:
                    db.execSQL("update user set facebook_flag = 0");
                    db.execSQL("update user set daum_flag = 0");
                    try{
                        FacebookSdk.sdkInitialize(mContext);
                        LoginManager.getInstance().logOut();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
			case 7:
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_NAVER_USE + " integer null default 0");
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_DAUM_USE + " integer null default 0");
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_TISTORY_USE + " integer null default 0");
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_FACEBOOK_USE + " integer null default 0");
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_TWITTER_USE + " integer null default 0");
                db.execSQL("ALTER TABLE " + DataBases.User._TABLENAME + " ADD COLUMN " + DataBases.User.SNS_KAKAO_USE + " integer null default 0");
			case 8:
				
			case 9:
				
			case 10:
				break;
			default:
				if (databaseTables != null)
		        {
		            for (String table : databaseTables)
		            {
		                db.execSQL("DROP TABLE IF EXISTS " + table);
		            }
		        }
		        onCreate(db);
		        break;
		}
    	
/*        if (databaseTables != null)
        {
            for (String table : databaseTables)
            {
                db.execSQL("DROP TABLE IF EXISTS " + table);
            }
        }
        onCreate(db);*/
    }
}
