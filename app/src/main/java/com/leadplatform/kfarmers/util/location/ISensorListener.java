package com.leadplatform.kfarmers.util.location;

import android.location.Location;

public interface ISensorListener
{
    public void updateLocation(Location location);
    public void updateOrientation(int azimuth, int pitch, int roll);
}
