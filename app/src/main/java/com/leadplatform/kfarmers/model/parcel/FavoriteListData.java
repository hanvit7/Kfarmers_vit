package com.leadplatform.kfarmers.model.parcel;

public class FavoriteListData
{
    public static final String KEY = "FavoriteListData";

    private int OldIndex;
    private boolean initFlag;

    public FavoriteListData()
    {
        OldIndex = 0;
        initFlag = false;
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
