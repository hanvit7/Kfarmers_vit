package com.leadplatform.kfarmers.view.menu.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class SettingServiceFragment extends BaseFragment
{
    public static final String TAG = "SettingServiceFragment";
    
    public static SettingServiceFragment newInstance()
    {
        final SettingServiceFragment f = new SettingServiceFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_setting_service, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
}
