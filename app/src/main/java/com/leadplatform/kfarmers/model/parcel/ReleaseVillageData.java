package com.leadplatform.kfarmers.model.parcel;

public class ReleaseVillageData
{
    public static final String KEY = "ReleaseVillageData";

    private String Category;
    private String ReleaseDateStart;
    private String ReleaseDateEnd;
    private boolean always;
    private boolean finish;
    private String Price;
    private String ReleaseNote;
    private String[] imagePath;

    public ReleaseVillageData()
    {
        Category = null;
        ReleaseDateStart = null;
        ReleaseDateEnd = null;
        always = false;
        finish = false;
        Price = null;
        ReleaseNote = null;
        imagePath = null;
    }

    public String getCategory()
    {
        return Category;
    }

    public void setCategory(String category)
    {
        Category = category;
    }

    public String getReleaseDateStart()
    {
        return ReleaseDateStart;
    }

    public void setReleaseDateStart(String releaseDateStart)
    {
        ReleaseDateStart = releaseDateStart;
    }

    public String getReleaseDateEnd()
    {
        return ReleaseDateEnd;
    }

    public void setReleaseDateEnd(String releaseDateEnd)
    {
        ReleaseDateEnd = releaseDateEnd;
    }

    public boolean isAlways()
    {
        return always;
    }

    public void setAlways(boolean always)
    {
        this.always = always;
    }

    public boolean isFinish()
    {
        return finish;
    }

    public void setFinish(boolean finish)
    {
        this.finish = finish;
    }

    public String getPrice()
    {
        return Price;
    }

    public void setPrice(String price)
    {
        Price = price;
    }

    public String getReleaseNote()
    {
        return ReleaseNote;
    }

    public void setReleaseNote(String releaseNote)
    {
        ReleaseNote = releaseNote;
    }

    public String[] getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String[] imagePath)
    {
        this.imagePath = imagePath;
    }

}
