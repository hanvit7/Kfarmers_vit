package com.leadplatform.kfarmers.util;

import android.content.Context;
import android.os.AsyncTask;

import com.leadplatform.kfarmers.view.common.ImageSelectorFragment;

public class AsyncTaskVO {

    public Context c;
    public AsyncTask task;
    public ImageSelectorFragment.PhotoData photoData = null;

    public static AsyncTaskVO newInstance(Context c, AsyncTask task, ImageSelectorFragment.PhotoData photoData) {
        AsyncTaskVO vo = new AsyncTaskVO();
        vo.c = c;
        vo.task = task;
        vo.photoData = photoData;
        return vo;
    }
}
