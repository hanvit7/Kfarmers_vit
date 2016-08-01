package com.leadplatform.kfarmers.view.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;



public class DynamicImageView extends ImageView
{
	double ratio = 0.75;
	
    public DynamicImageView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        String ratio = context.obtainStyledAttributes( attrs, R.styleable.CustomView).getString(R.styleable.CustomView_viewRatio);
       
        if(!PatternUtil.isEmpty(ratio))
        {
    	   this.ratio = Double.valueOf(ratio);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final Drawable d = this.getDrawable();

        if (d != null)
        {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            final int width = MeasureSpec.getSize(widthMeasureSpec);
//            final int height = (int) Math.ceil(width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
            final int height = (int) Math.ceil(width * ratio);
            this.setMeasuredDimension(width, height);
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
