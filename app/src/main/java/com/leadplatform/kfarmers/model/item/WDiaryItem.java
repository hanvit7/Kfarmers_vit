package com.leadplatform.kfarmers.model.item;

public class WDiaryItem {
    public static final int TEXT_TYPE = 0;
    public static final int PICTURE_TYPE = 1;

    private int type;
    private String textContent;
    private String pictureContent;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getPictureContent() {
        return pictureContent;
    }

    public void setPictureContent(String pictureContent) {
        this.pictureContent = pictureContent;
    }
}
