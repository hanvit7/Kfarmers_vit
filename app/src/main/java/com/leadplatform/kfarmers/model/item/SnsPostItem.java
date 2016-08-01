package com.leadplatform.kfarmers.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SnsPostItem implements Serializable{

    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String TAG = "tag";

    private boolean isFaceBook;
    private boolean isNaver;
    private boolean isKakao;
    private ArrayList<String> images;
    private HashMap<String,String> faceBookData;
    private HashMap<String,String> naverData;
    private HashMap<String,String> kakaoData;

    public boolean isFaceBook() { return isFaceBook; }

    public void setIsFaceBook(boolean isFaceBook) { this.isFaceBook = isFaceBook; }

    public boolean isNaver() {
        return isNaver;
    }

    public void setIsNaver(boolean isNaver) {
        this.isNaver = isNaver;
    }

    public boolean isKakao() {
        return isKakao;
    }

    public void setIsKakao(boolean isKakao) {
        this.isKakao = isKakao;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public HashMap<String, String> getNaverData() {
        return naverData;
    }

    public void setNaverData(HashMap<String, String> naverData) {
        this.naverData = naverData;
    }

    public HashMap<String, String> getKakaoData() {
        return kakaoData;
    }

    public void setKakaoData(HashMap<String, String> kakaoData) {
        this.kakaoData = kakaoData;
    }

    public HashMap<String, String> getFaceBookData() {
        return faceBookData;
    }

    public void setFaceBookData(HashMap<String, String> faceBookData) {
        this.faceBookData = faceBookData;
    }
}


