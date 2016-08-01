package com.leadplatform.kfarmers.model.preference;

import java.util.ArrayList;

public class AppPreferenceData
{
    public boolean login = false;
    public double latitude = 0;
    public double longitude = 0;    
    public boolean gcmEnable = true;
    public int gcmAppVersion = 0;
    public String gcmRegistrationId = "";
    public boolean isGcmSend = false;
    public ArrayList<String> pushData = new ArrayList<String>();
    public ArrayList<String> eventData = new ArrayList<String>();
}
