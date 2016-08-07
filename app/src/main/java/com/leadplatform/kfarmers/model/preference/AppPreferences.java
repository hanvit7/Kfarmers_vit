package com.leadplatform.kfarmers.model.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class AppPreferences {
    private static final String FILE_NAME = "kFarmers";
    private static final String PREFERENCES_DATA = "PREFERENCES_DATA";

    private static SharedPreferences mSharedPreference = null;
    private static Editor mEditor = null;

    private static void initSharedPreferences(Context context) {
        if (mSharedPreference == null) {
            mSharedPreference = context.getSharedPreferences(FILE_NAME, Activity.MODE_PRIVATE);
        }
        if (mEditor == null) {
            mEditor = mSharedPreference.edit();
        }
    }

    private static AppPreferenceData getData() {
        return gsonFromJson(getSettingData());
    }

    private static void setData(AppPreferenceData data) {
        setSettingData(gsonToJson(data));
    }

    private static String getSettingData() {
        if (mSharedPreference == null)
            return null;

        return mSharedPreference.getString(PREFERENCES_DATA, initData());
    }

    private static void setSettingData(String data) {
        if (mSharedPreference == null)
            return;
        if (mEditor == null)
            return;

        mEditor.putString(PREFERENCES_DATA, data);
        mEditor.commit();
    }

    public static void clearData() {
        if (mSharedPreference == null)
            return;
        if (mEditor == null)
            return;

        mEditor.clear().commit();
    }

    private static AppPreferenceData gsonFromJson(String json) {
        AppPreferenceData data = new AppPreferenceData();
        ObjectMapper mapper = new ObjectMapper();

        try {
            data = mapper.readValue(json, AppPreferenceData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private static String gsonToJson(AppPreferenceData weather) {
        String data = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            data = mapper.writeValueAsString(weather);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private static String initData() {
        AppPreferenceData data = new AppPreferenceData();
        return gsonToJson(data);
    }

    public static void setLogin(Context context, boolean flag) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.login = flag;
        setData(data);
    }

    public static boolean getLogin(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.login;
    }

    public static void setLatitude(Context context, double latitude) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.latitude = latitude;
        setData(data);
    }

    public static double getLatitude(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.latitude;
    }

    public static void setLongitude(Context context, double longitude) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.longitude = longitude;
        setData(data);
    }

    public static double getLongitude(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.longitude;
    }

    public static void setGcmEnable(Context context, boolean enable) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.gcmEnable = enable;
        setData(data);
    }

    public static boolean getGcmEnable(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.gcmEnable;
    }

    public static void setGcmRegistrationId(Context context, String registrationId) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.gcmRegistrationId = registrationId;
        setData(data);
    }

    public static String getGcmRegistrationId(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.gcmRegistrationId;
    }

    public static void setGcmAppVersion(Context context, int appVersion) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.gcmAppVersion = appVersion;
        setData(data);
    }

    public static int getGcmAppVersion(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.gcmAppVersion;
    }

    public static void setGcmSend(Context context, boolean send) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.isGcmSend = send;
        setData(data);
    }

    public static boolean getGcmSend(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.isGcmSend;
    }

    public static void setPushData(Context context, String push) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.pushData.add(push);
        setData(data);
    }

    public static void setPushAllData(Context context, ArrayList<String> push) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.pushData = push;
        setData(data);
    }

    public static ArrayList<String> getPushData(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.pushData;
    }

    public static void setEventData(Context context, String event) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        data.eventData.add(event);
        setData(data);
    }

    public static ArrayList<String> getEventData(Context context) {
        initSharedPreferences(context);
        AppPreferenceData data = getData();
        return data.eventData;
    }
}
