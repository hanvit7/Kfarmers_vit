package com.leadplatform.kfarmers.view.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;

public class DynamicFrameLayout extends FrameLayout
{
	double ratio = 1.15;
	
    public DynamicFrameLayout(Context context)
    {
        super(context);
    }

    public DynamicFrameLayout(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        String ratio = context.obtainStyledAttributes( attrs, R.styleable.CustomView).getString(R.styleable.CustomView_viewRatio);
        
        if(!PatternUtil.isEmpty(ratio))
        {
     	   this.ratio = Double.valueOf(ratio);
        }
    }

    public DynamicFrameLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        
        String ratio = context.obtainStyledAttributes( attrs, R.styleable.CustomView).getString(R.styleable.CustomView_viewRatio);
        
        if(!PatternUtil.isEmpty(ratio))
        {
     	   this.ratio = Double.valueOf(ratio);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = (int) Math.ceil(width * ratio);
        this.setMeasuredDimension(width, height);

        int wspec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hspec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++)
        {
            View v = getChildAt(i);
            v.measure(wspec, hspec);
        }
        
        
    }
}
