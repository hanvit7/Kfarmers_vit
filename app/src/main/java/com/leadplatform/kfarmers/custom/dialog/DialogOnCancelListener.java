package com.leadplatform.kfarmers.custom.dialog;

import android.content.DialogInterface;

public abstract class DialogOnCancelListener implements android.content.DialogInterface.OnCancelListener
{
    public abstract void dialogOnCancel(DialogInterface dialog);

    @Override
    public void onCancel(DialogInterface dialog)
    {
        dialogOnCancel(dialog);
    }
}
