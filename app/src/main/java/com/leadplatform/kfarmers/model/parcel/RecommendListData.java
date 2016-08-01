package com.leadplatform.kfarmers.model.parcel;

public class RecommendListData
{
    public static final String KEY = "RecommendListData";

    private String Type;
    private String OldIndex;
    private boolean initFlag;

    public RecommendListData()
    {
        Type = null;
        OldIndex = null;
        initFlag = false;
    }

    public String getType()
    {
        return Type;
    }

    public void setType(String type)
    {
        Type = type;
    }

    public String getOldIndex()
    {
        return OldIndex;
    }

    public void setOldIndex(String oldIndex)
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
