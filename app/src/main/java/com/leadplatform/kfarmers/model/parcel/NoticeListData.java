package com.leadplatform.kfarmers.model.parcel;

public class NoticeListData
{
    public static final String KEY = "NoticeListData";

    private String userIndex;
    private String userType;
    private int OldIndex;
    private boolean initFlag;

    public NoticeListData()
    {
        userIndex = null;
        OldIndex = 0;
        initFlag = false;
    }

    public String getUserIndex()
    {
        return userIndex;
    }

    public void setUserIndex(String userIndex)
    {
        this.userIndex = userIndex;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public int getOldIndex()
    {
        return OldIndex;
    }

    public void setOldIndex(int oldIndex)
    {
        OldIndex = oldIndex;
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
