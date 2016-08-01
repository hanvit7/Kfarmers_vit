package com.leadplatform.kfarmers.custom.dialog;

import android.content.DialogInterface;

public abstract class DialogOnClickListener implements android.content.DialogInterface.OnClickListener
{
    public abstract void dialogOnClick(DialogInterface dialog, int which);

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        dialogOnClick(dialog, which);
    }
}
