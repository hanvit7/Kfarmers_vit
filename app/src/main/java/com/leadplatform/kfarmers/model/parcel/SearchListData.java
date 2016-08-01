package com.leadplatform.kfarmers.model.parcel;

public class SearchListData
{
    public static final String KEY = "SearchListData";

    private String Search;
    private int Offset;
    private boolean initFlag;

    public SearchListData()
    {
        Search = null;
        Offset = 0;
        initFlag = false;
    }

    public String getSearch()
    {
        return Search;
    }

    public void setSearch(String search)
    {
        Search = search;
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
