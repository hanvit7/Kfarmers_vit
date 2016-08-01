package com.leadplatform.kfarmers.util;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public abstract class SimpleLocationListener implements LocationListener
{
    public abstract void onLocationSuccess(Location location);
    public abstract void onLocationError();

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            onLocationSuccess(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }
}
