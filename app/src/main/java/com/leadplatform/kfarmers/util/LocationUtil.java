package com.leadplatform.kfarmers.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.leadplatform.kfarmers.BuildConfig;

import java.util.List;

public class LocationUtil
{
    private static final String TAG = "LocationUtil";
    public final static long LOCATION_FINDER_MAX_TIME = 5 * 60 * 1000;
    public final static float LOCATION_FINDER_MIN_ACCURACY = 100.0f;

    private final Context context;
    private final LocationManager locationManager;
    private Criteria criteria;
    private long maxTime;
    private float minAccuracy;

    public LocationUtil(Context context)
    {
        this.context = context;

        // Get Android's location manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false); // 고도
        criteria.setBearingRequired(false); // 방향
        criteria.setSpeedRequired(false); // 속도
        criteria.setCostAllowed(true); // 비용

        setMaxTime(LOCATION_FINDER_MAX_TIME);
        setMinAccuracy(LOCATION_FINDER_MIN_ACCURACY);
    }

    public Context getContext()
    {
        return context;
    }

    public long getMaxTime()
    {
        return maxTime;
    }

    public void setMaxTime(long time)
    {
        maxTime = time;
    }

    public float getMinAccuracy()
    {
        return minAccuracy;
    }

    public void setMinAccuracy(float accuracy)
    {
        minAccuracy = accuracy;
    }

    public Criteria getCriteria()
    {
        return criteria;
    }

    public LocationUtil setCriteria(Criteria criteria)
    {
        this.criteria = criteria;
        return this;
    }

    public Location getLastBestKnownLocation()
    {
        assert (locationManager != null);

        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestFreshness = Long.MAX_VALUE;

        List<String> matchingProviders = locationManager.getAllProviders();
        long currentTime = System.currentTimeMillis();
        for (String provider : matchingProviders)
        {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null)
            {
                float accuracy = location.getAccuracy();
                long freshness = currentTime - location.getTime();

                if ((freshness < bestFreshness && accuracy < bestAccuracy) || bestResult == null)
                {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestFreshness = freshness;
                }
            }
        } 

        if (bestFreshness > getMaxTime() || bestAccuracy > getMinAccuracy())
        {
            bestResult = null;
        }

        if (bestResult != null)
        {
            if (BuildConfig.DEBUG)
                Log.d(TAG, String.format("Best Known location from %s (freshness: %d secs ago, accuracy: %.2f m)", bestResult.getProvider(),
                        bestFreshness / 1000, bestAccuracy));
        }

        return bestResult;
    }

    public String requestLocationUpdate(LocationListener locationListener)
    {
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null)
        {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "LastKnownLocation isn't good enough, requesting a single update");

            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        }
        return provider;
    }

    public void removeLocationUpdate(LocationListener locationListener)
    {
        locationManager.removeUpdates(locationListener);
    }

    public void simpleCurrentLocation(SimpleLocationListener locationListener)
    {
        Location location = getLastBestKnownLocation();
        if (location == null)
        {
            String provider = requestLocationUpdate(locationListener);
            if(provider == null)
            {
                locationListener.onLocationError();
            }
        }
        else
        {
            locationListener.onLocationSuccess(location);
        }
    }
}
