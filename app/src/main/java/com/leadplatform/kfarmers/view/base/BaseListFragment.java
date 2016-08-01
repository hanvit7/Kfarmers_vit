package com.leadplatform.kfarmers.view.base;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListFragment;

public class BaseListFragment extends SherlockListFragment
{
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
}
