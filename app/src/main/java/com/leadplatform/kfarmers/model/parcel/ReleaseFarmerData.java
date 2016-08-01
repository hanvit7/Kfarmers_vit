package com.leadplatform.kfarmers.model.parcel;

public class ReleaseFarmerData
{
    public static final String KEY = "ReleaseFarmerData";

    private String Category;
    private String ReleaseDateStart;
    private String ReleaseDateEnd;
    private boolean always;
    private boolean finish;
    private String ReleaseNote;

    public ReleaseFarmerData()
    {
        Category = null;
        ReleaseDateStart = null;
        ReleaseDateEnd = null;
        always = false;
        finish = false;
        ReleaseNote = null;
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

    public String getReleaseNote()
    {
        return ReleaseNote;
    }

    public void setReleaseNote(String releaseNote)
    {
        ReleaseNote = releaseNote;
    }
}
