package com.leadplatform.kfarmers.view.base;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class BaseDialogFragment extends SherlockDialogFragment
{
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
}
