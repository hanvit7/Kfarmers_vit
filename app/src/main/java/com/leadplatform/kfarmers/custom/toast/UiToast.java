package com.leadplatform.kfarmers.custom.toast;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

public class UiToast
{
    public static void show(Context context, int imageResId)
    {
        ImageView img = new ImageView(context);
        img.setImageResource(imageResId);
 
        final Toast toast = new Toast(context);
        toast.setView(img);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                toast.cancel();
            }
        }, 500);
    }
}
