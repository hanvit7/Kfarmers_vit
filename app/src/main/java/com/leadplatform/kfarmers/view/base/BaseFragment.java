package com.leadplatform.kfarmers.view.base;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;

public class BaseFragment extends SherlockFragment
{
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
}
