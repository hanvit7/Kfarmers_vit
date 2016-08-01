package com.leadplatform.kfarmers.view.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class WrappingSlidingDrawer extends SlidingDrawer {

    private final float MINICART_HEIGHT = 270.0f;


    int heightSize = 0;

    public WrappingSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int orientation = attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
        mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
        mVertical = (orientation == SlidingDrawer.ORIENTATION_VERTICAL);

        heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MINICART_HEIGHT, getResources().getDisplayMetrics());
    }

    public WrappingSlidingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);

        int orientation = attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
        mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
        mVertical = (orientation == SlidingDrawer.ORIENTATION_VERTICAL);

        heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MINICART_HEIGHT, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        //int heightSpecMode = MeasureSpec.AT_MOST;
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = getHandle();
        final View content = getContent();
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            content.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightSpecMode));
            heightSpecSize = handle.getMeasuredHeight() + mTopOffset + content.getMeasuredHeight();

            if(content.getMeasuredHeight()>heightSize)
            {
                int cheight = Math.min(heightSpecSize, heightSize);
                content.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(cheight, heightSpecMode));
                heightSpecSize = handle.getMeasuredHeight() + mTopOffset + content.getMeasuredHeight();
            }
            widthSpecSize = content.getMeasuredWidth();
            if (handle.getMeasuredWidth() > widthSpecSize) widthSpecSize = handle.getMeasuredWidth();
        }
        else {
            int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
            getContent().measure(MeasureSpec.makeMeasureSpec(width, widthSpecMode), heightMeasureSpec);
            widthSpecSize = handle.getMeasuredWidth() + mTopOffset + content.getMeasuredWidth();
            heightSpecSize = content.getMeasuredHeight();
            if (handle.getMeasuredHeight() > heightSpecSize) heightSpecSize = handle.getMeasuredHeight();
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);



    }

    private boolean mVertical;
    private int mTopOffset;
}