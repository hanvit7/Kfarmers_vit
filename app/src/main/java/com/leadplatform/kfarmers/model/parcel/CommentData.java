package com.leadplatform.kfarmers.model.parcel;

public class CommentData
{
    public static final String KEY = "CommentData";

    private String BoardType;
    private String DiaryIndex;
    private String ParentCommentIndex;
    private String Description;

    public CommentData()
    {
        BoardType = null;
        DiaryIndex = null;
        ParentCommentIndex = "0";
        Description = null;
    }

    public String getBoardType()
    {
        return BoardType;
    }

    public void setBoardType(String boardType)
    {
        BoardType = boardType;
    }

    public String getDiaryIndex()
    {
        return DiaryIndex;
    }

    public void setDiaryIndex(String diaryIndex)
    {
        DiaryIndex = diaryIndex;
    }

    public String getParentCommentIndex()
    {
        return ParentCommentIndex;
    }

    public void setParentCommentIndex(String parentCommentIndex)
    {
        ParentCommentIndex = parentCommentIndex;
    }

    public String getDescription()
    {
        return Description;
    }

    public void setDescription(String description)
    {
        Description = description;
    }

}
