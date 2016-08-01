package com.leadplatform.kfarmers.model.parcel;

public class ReplyListData
{
    public static final String KEY = "ReplyListData";

    private String DiaryIndex;
    private int Offset;
    private boolean initFlag;

    public ReplyListData()
    {
        DiaryIndex = null;
        Offset = 0;
        initFlag = false;
    }

    public String getDiaryIndex()
    {
        return DiaryIndex;
    }

    public void setDiaryIndex(String diaryIndex)
    {
        DiaryIndex = diaryIndex;
    }

    public int getOffset()
    {
        return Offset;
    }

    public void setOffset(int offset)
    {
        Offset = offset;
    }

    public boolean isInitFlag()
    {
        return initFlag;
    }

    public void setInitFlag(boolean initFlag)
    {
        this.initFlag = initFlag;
    }

}
