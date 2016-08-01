package com.leadplatform.kfarmers.custom;


public class CustomViewUtil
{
    public static class Button
    {
        public static final long DEFAULT_CLICK_EVENT_INTERVAL = 300L;
        private static long CLICK_EVENT_INTERVAL = DEFAULT_CLICK_EVENT_INTERVAL;
        private static long mLastClickEventTime = 0L;

        public static synchronized boolean isRepeatClick()
        {
            boolean isRepeatClick = true;
            long lNow = System.currentTimeMillis();

            if (lNow - mLastClickEventTime > CLICK_EVENT_INTERVAL)
            {
                isRepeatClick = false;
            }
            mLastClickEventTime = lNow;

            return isRepeatClick;
        }
        
        public static void setClickIntervalTime(long time)
        {
            CLICK_EVENT_INTERVAL = time;
        }
    }
}
