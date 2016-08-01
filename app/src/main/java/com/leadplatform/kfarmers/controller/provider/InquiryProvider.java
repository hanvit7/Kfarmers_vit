package com.leadplatform.kfarmers.controller.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

/**
 * Created by PC on 2015-05-04.
 */
public class InquiryProvider extends ContentProvider {

    public static final String URI = "com.leadplatform.kfarmers.controlle.provider.inquiry";

    public static final Uri CONTENT_URI = Uri.parse("content://" + URI +"/");

    public static final String INQUIRY_INSERT ="INSERT";
    public static final String INQUIRY_GET ="GET";
    public static final String INQUIRY_CHECK ="CHECK";

    private String mIdx = "";
    private String mIsShow = "false";

    public String getIdx() {
        return mIdx;
    }

    public void setIdx(String idx) {
        mIdx = idx;
    }

    public String getmIsShow() {
        return mIsShow;
    }

    public void setmIsShow(String mIsShow) {
        this.mIsShow = mIsShow;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        List<String> reqValue = uri.getPathSegments();

        if(reqValue.size() > 0) {
            String serviceType = reqValue.get(0);
            if(serviceType.equals(INQUIRY_INSERT)){
                if(values != null && values.containsKey("idx"))
                {
                    String idx = (String) values.get("idx");
                    setIdx(idx);
                    getContext().getContentResolver().notifyChange(InquiryProvider.CONTENT_URI, null);
                }
            }else if(serviceType.equals(INQUIRY_CHECK)){

                String isShow = "false";
                if(values != null && values.containsKey("show"))
                {
                    isShow = getmIsShow();
                    String show = (String) values.get("show");
                    setmIsShow(show);
                }
                return Uri.parse(CONTENT_URI + "/" + isShow);
            }
            else if(serviceType.equals(INQUIRY_GET)){
                return Uri.parse(CONTENT_URI + "/" + getIdx());
            }
        }
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
