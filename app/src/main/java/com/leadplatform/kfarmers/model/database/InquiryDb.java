package com.leadplatform.kfarmers.model.database;

import android.database.Cursor;

public class InquiryDb
{
    public int id;
    public String idx;
    public String chat_idx;
    public String read;

    public InquiryDb()
    {

    }

    public InquiryDb(Cursor cursor)
    {
        if (cursor != null)
        {
            this.id = cursor.getInt(cursor.getColumnIndex(DataBases.Inquiry._ID));
            this.idx = cursor.getString(cursor.getColumnIndex(DataBases.Inquiry.IDX));
            this.chat_idx = cursor.getString(cursor.getColumnIndex(DataBases.Inquiry.CHAT_IDX));
            this.read = cursor.getString(cursor.getColumnIndex(DataBases.Inquiry.READ));
        }
    }
}
