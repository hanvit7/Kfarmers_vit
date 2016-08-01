package com.leadplatform.kfarmers.model.holder;

import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.view.base.DynamicImageViewBackground;

public class StoryListHolder
{
    public RelativeLayout rootLayout;
    public ImageView Profile;
    public TextView Farmer;
    public TextView Date;
    public TextView Description;
    public DynamicImageViewBackground dynamicViewBg;
    public ViewPager imageViewPager;
    public TextView LikeText;
    public TextView ReplyText;
    public RelativeLayout Like;
    public RelativeLayout Reply;
    public RelativeLayout Share;
}
