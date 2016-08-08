package com.leadplatform.kfarmers.custom.button;

import android.view.View;

import com.leadplatform.kfarmers.custom.CustomViewUtil;

public abstract class ViewOnClickListener implements android.view.View.OnClickListener {
    public abstract void viewOnClick(View v);

    @Override
    public void onClick(View v) {
        if (CustomViewUtil.Button.isRepeatClick()) {//중복클릭방지
            return;
        }
        viewOnClick(v);
    }
}
