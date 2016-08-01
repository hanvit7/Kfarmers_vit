package com.leadplatform.kfarmers.model.parcel;

public class StoryListData
{
    public static final String KEY = "StoryListData";

    private String UserIndex;
    private int OldIndex;
    private boolean initFlag;

    public StoryListData()
    {
        UserIndex = null;
        OldIndex = 0;
        initFlag = false;
    }

    public String getUserIndex()
    {
        return UserIndex;
    }

    public void setUserIndex(String userIndex)
    {
        UserIndex = userIndex;
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
