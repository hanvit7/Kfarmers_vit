package com.leadplatform.kfarmers.model.parcel;

public class BlogListData
{
    public static final String KEY = "BlogListData";

    private String FarmerIndex;
    private String Category;
    private int OldIndex;
    private boolean initFlag;

    public BlogListData()
    {
        FarmerIndex = null;
        Category = null;
        OldIndex = 0;
        initFlag = false;
    }

    public String getFarmerIndex()
    {
        return FarmerIndex;
    }

    public void setFarmerIndex(String farmerIndex)
    {
        FarmerIndex = farmerIndex;
    }

    public String getCategory()
    {
        return Category;
    }

    public void setCategory(String category)
    {
        Category = category;
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
