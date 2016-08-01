package com.leadplatform.kfarmers.model.parcel;

import java.util.ArrayList;

public class WriteDiaryData
{
    public static final String KEY = "WriteDiaryData";

    private int category;
    private String blogTitle;
    private boolean bAlign;
    private String blogTag;
    private boolean bNaver;
    private boolean bTistory;
    private boolean bDaum;
    private boolean bFacebook;
    private boolean bTwitter;
    private boolean bKakao;
    private String weather;
    private String temperature;
    private String humidity;
    private String date;
    private String from;
    private String boardType;
    private ArrayList<RowsData> rows;
    private ArrayList<String> images;

    public int getCategory()
    {
        return category;
    }

    public void setCategory(int category)
    {
        this.category = category;
    }

    public String getBlogTitle()
    {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle)
    {
        this.blogTitle = blogTitle;
    }

    public boolean isbAlign()
    {
        return bAlign;
    }

    public void setbAlign(boolean bAlign)
    {
        this.bAlign = bAlign;
    }

    public String getBlogTag()
    {
        return blogTag;
    }

    public void setBlogTag(String blogTag)
    {
        this.blogTag = blogTag;
    }

    public boolean isbNaver()
    {
        return bNaver;
    }

    public void setbNaver(boolean bNaver)
    {
        this.bNaver = bNaver;
    }

    public boolean isbKakao()
    {
        return bKakao;
    }

    public void setbKakao(boolean bKakao)
    {
        this.bKakao = bKakao;
    }

    public boolean isbTistory()
    {
        return bTistory;
    }

    public void setbTistory(boolean bTistory)
    {
        this.bTistory = bTistory;
    }

    public boolean isbDaum()
    {
        return bDaum;
    }

    public void setbDaum(boolean bDaum)
    {
        this.bDaum = bDaum;
    }

    public boolean isbFacebook()
    {
        return bFacebook;
    }

    public void setbFacebook(boolean bFacebook)
    {
        this.bFacebook = bFacebook;
    }

    public boolean isbTwitter()
    {
        return bTwitter;
    }

    public void setbTwitter(boolean bTwitter)
    {
        this.bTwitter = bTwitter;
    }

    public String getWeather()
    {
        return weather;
    }

    public void setWeather(String weather)
    {
        this.weather = weather;
    }

    public String getTemperature()
    {
        return temperature;
    }

    public void setTemperature(String temperature)
    {
        this.temperature = temperature;
    }

    public String getHumidity()
    {
        return humidity;
    }

    public void setHumidity(String humidity)
    {
        this.humidity = humidity;
    }

    public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public ArrayList<RowsData> getRows()
    {
        return rows;
    }

    public void setRows(ArrayList<RowsData> rows)
    {
        this.rows = rows;
    }

    public ArrayList<String> getImages()
    {
        return images;
    }

    public void setImages(ArrayList<String> images)
    {
        this.images = images;
    }
}
