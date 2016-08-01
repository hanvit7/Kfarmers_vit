package com.leadplatform.kfarmers.model.tag;

public class LikeTag
{
    public String index;
    public int position;
    public String boardType;

    public LikeTag(String index, int position)
    {
        this.index = index;
        this.position = position;
    }

    public LikeTag(String index, int position, String boardType)
    {
        this.index = index;
        this.position = position;
        this.boardType = boardType;
    }
}
