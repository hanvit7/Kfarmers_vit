package com.leadplatform.kfarmers.view.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.ImageView;

import com.leadplatform.kfarmers.R;

public class DynamicImageViewBackground extends ImageView
{
    private Context context;

    public DynamicImageViewBackground(Context context)
    {
        super(context);
        this.context = context;
    }

    public DynamicImageViewBackground(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final Drawable d = this.getBackground();

        if (d != null)
        {
            Point point = new Point();
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            display.getRealSize(point);
//            display.getSize(point);
            final int margin = ((int) context.getResources().getDimension(R.dimen.image_view_margin) * 2);
            final int screenWidth = point.x - margin;
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            final int height = (int) Math.ceil(screenWidth * 0.75);
            this.setMeasuredDimension(width, height);
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
