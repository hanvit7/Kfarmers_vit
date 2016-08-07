package com.leadplatform.kfarmers.model.parcel;

public class FarmNewsFooterFilter {
    public static final String KEY = "DiaryListData";

    private String Farmer;
    private String Category1;
    private String Category2;
    private int OldIndex;
    private String OldDate;
    private int Auth;
    private int ReleaseDate2Month;
    private String Address;
    private int Distance;
    private double Latitude;
    private double Longitude;
    private boolean Impressive;
    private String Search;
    private boolean initFlag;
    private String Verification;

    public FarmNewsFooterFilter() {
        Farmer = null;
        Category1 = null;
        Category2 = null;
        OldIndex = 0;
        Auth = 0;
        ReleaseDate2Month = 0;
        Address = null;
        Distance = 0;
        Latitude = 0;
        Longitude = 0;
        Impressive = false;
        Search = null;
        initFlag = false;
        Verification = null;
    }

    public String getFarmer() {
        return Farmer;
    }

    public void setFarmer(String farmer) {
        Farmer = farmer;
    }

    public String getCategory1() {
        return Category1;
    }

    public void setCategory1(String category1) {
        Category1 = category1;
    }

    public String getCategory2() {
        return Category2;
    }

    public void setCategory2(String category2) {
        Category2 = category2;
    }

    public int getOldIndex() {
        return OldIndex;
    }

    public void setOldIndex(int oldIndex) {
        OldIndex = oldIndex;
    }

    public String getOldDate() {
        return OldDate;
    }

    public void setOldDate(String oldDate) {
        OldDate = oldDate;
    }

    public int getAuth() {
        return Auth;
    }

    public void setAuth(int auth) {
        Auth = auth;
    }

    public int getReleaseDate2Month() {
        return ReleaseDate2Month;
    }

    public void setReleaseDate2Month(int month) {
        ReleaseDate2Month = month;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getDistance() {
        return Distance;
    }

    public void setDistance(int distance) {
        Distance = distance;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public boolean isImpressive() {
        return Impressive;
    }

    public void setImpressive(boolean impressive) {
        Impressive = impressive;
    }

    public String getSearch() {
        return Search;
    }

    public void setSearch(String search) {
        Search = search;
    }

    public boolean isInitFlag() {
        return initFlag;
    }

    public void setInitFlag(boolean initFlag) {
        this.initFlag = initFlag;
    }

    public String getVerification() {
        return Verification;
    }

    public void setVerification(String verification) {
        Verification = verification;
    }
}
