package com.leadplatform.kfarmers.view.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import com.leadplatform.kfarmers.R;

public class LimitListView extends ListView
{
    private int mHeight = -1;

    public LimitListView(Context paramContext)
    {
        super(paramContext);
    }

    public LimitListView(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        setAttrs(paramContext, paramAttributeSet);
    }

    public LimitListView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        setAttrs(paramContext, paramAttributeSet);
    }

    private void setAttrs(Context paramContext, AttributeSet paramAttributeSet)
    {
        TypedArray typedArray = paramContext.obtainStyledAttributes(paramAttributeSet, R.styleable.LimitView);
        mHeight = typedArray.getDimensionPixelSize(0, -1);
        typedArray.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ((mHeight > 0) && (getMeasuredHeight() > mHeight)) {
            setMeasuredDimension(getMeasuredWidth(), mHeight);
        }
    }
}


