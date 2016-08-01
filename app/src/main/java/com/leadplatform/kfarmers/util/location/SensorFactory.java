package com.leadplatform.kfarmers.util.location;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class SensorFactory
{
    // //////////////////////////////////////////////////////////////////////
    // Member fields
    // //////////////////////////////////////////////////////////////////////

    private static SensorFactory mInstance = null;

    private LocationManager mLocationManager = null;

    private ArrayList<ISensorListener> mListListeners = null;

    private static String mGPSProvider = LocationManager.GPS_PROVIDER;
    private static String mNetProvider = LocationManager.NETWORK_PROVIDER;
    private static String mPassiveProvider = LocationManager.PASSIVE_PROVIDER;

    private LocationListener mLocationListener = null;
    private SensorEventListener mSensorEventListener = null;
    private SensorManager mSensorManager = null;
    private Application mAppContext = null;
    private LastLocation lastLocation = null;
    private boolean blockNetProvider = false;

    class LastLocation
    {
        Location location = null;
        long updateTime = 0;
        String provider = "";

        public LastLocation()
        {
        }

        public Location getLocation()
        {
            return location;
        }

        public boolean setLocation(String provider, Location location)
        {
            long currentTime = System.currentTimeMillis();

            long gap = currentTime - updateTime;

            if (this.provider.equals(provider) || (gap >= 1000))
            {
                if (!this.provider.equals(provider))
                {
                    if (this.provider.equals(LocationManager.GPS_PROVIDER))
                    {
                        if (gap < 5000)
                        {
                            return false;
                        }
                    }

                    this.provider = provider;
                }

                updateTime = currentTime;
                this.location = location;

                return true;
            }

            return false;
        }

        public long getUpdatedTime()
        {
            return updateTime;
        }

    }

    // //////////////////////////////////////////////////////////////////////
    // Member methods
    // //////////////////////////////////////////////////////////////////////

    public static synchronized SensorFactory getInstance(Application context)
    {
        if (mInstance == null)
        {
            mInstance = new SensorFactory(context);
        }
        return mInstance;
    }

    private SensorFactory(Application context)
    {
        mAppContext = context;
    }

    private void setLocationManager()
    {
        mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private void loadMyLocation()
    {
//        if (!enableGpsProvider(mAppContext))
//        {
//            return;
//        }

        if (mLocationManager == null)
        {
            setLocationManager();
        }

        if (isGPSProviderEnabled())
        {
            // GPS ���� üũ�� �ϱ� ���� �߰�
            mLocationManager.addGpsStatusListener(gpsStatusListener);

            mLocationManager.requestLocationUpdates(mGPSProvider, 1000, 1, gpsListener);

            if (isPassiveProviderEnabled())
            {
                mLocationManager.requestLocationUpdates(mPassiveProvider, 1000, 1, passiveListener);
            }
        }

        if (isNetProviderEnabled())
        {
            mLocationManager.requestLocationUpdates(mNetProvider, 1000, 1, netListener);
        }
    }

    private boolean isGPSProviderEnabled()
    {
        if (mLocationManager == null)
        {
            setLocationManager();
        }
        return mLocationManager.isProviderEnabled(mGPSProvider);
    }

    private boolean isNetProviderEnabled()
    {
        if (mLocationManager == null)
        {
            setLocationManager();
        }
        return mLocationManager.isProviderEnabled(mNetProvider);
    }

    private boolean isPassiveProviderEnabled()
    {
        if (mLocationListener == null)
        {
            setLocationManager();
        }

        return mLocationManager.isProviderEnabled(mPassiveProvider);
    }

    public void destoryInstance()
    {
        mLocationManager.removeGpsStatusListener(gpsStatusListener);
        mLocationManager.removeUpdates(gpsListener);
        mLocationManager.removeUpdates(passiveListener);
        mLocationManager.removeUpdates(netListener);

        if (mLocationListener != null)
        {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationListener = null;
        }

        if (mSensorEventListener != null)
        {
            mSensorManager.unregisterListener(mSensorEventListener);
            mSensorEventListener = null;
        }

        if (mSensorManager != null)
        {
            mSensorManager = null;
        }

        if (mInstance != null)
        {
            mInstance.mAppContext = null;
            mInstance = null;
        }
    }

    public void addListener(ISensorListener listener)
    {
        if (mListListeners == null)
        {
            mListListeners = new ArrayList<ISensorListener>();
        }

        if (listener != null && mListListeners.size() <= 0)
        {
            loadMyLocation();
        }

        mListListeners.add(listener);
    }

    public int getListenerCount()
    {
        if (mListListeners != null)
        {
            return mListListeners.size();
        }

        return 0;
    }

    public void removeListener(ISensorListener listener)
    {
        if (mListListeners != null)
        {
            mListListeners.remove(listener);
            if (mListListeners.size() <= 0)
            {
                destoryInstance();
            }
        }
    }

    public Location getLastKnownLocation()
    {
        if (mLocationManager == null)
        {
            setLocationManager();
        }

        // GPS ��ġ�� ������ Network �� �̿��� ��ġ�� �����´�.
        Location loc = mLocationManager.getLastKnownLocation(mGPSProvider);

        if (loc == null /*&& isGPSProviderEnabled()*/)
        {
            if (isNetProviderEnabled())
            {
                loc = mLocationManager.getLastKnownLocation(mNetProvider);
            }

            if (loc == null && isPassiveProviderEnabled())
            {
                loc = mLocationManager.getLastKnownLocation(mPassiveProvider);
            }
        }

        return loc;
    }

    public static boolean enableLocationProvider(Context context)
    {
        boolean gps_enable = false; 
        boolean net_enable = false;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        gps_enable = lm.isProviderEnabled(mGPSProvider);

        net_enable = lm.isProviderEnabled(mNetProvider);

        Log.e("SensorFactory", "++ enableLocationProvider() gps_enable = " + gps_enable + ", net_enalbe = " + net_enable);

        return (gps_enable || net_enable);
    }

    public static boolean enableGpsProvider(Context context)
    {
        boolean gps_enable = false;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        gps_enable = lm.isProviderEnabled(mGPSProvider);

        // LOG.debug("++ enableGpsProvider() gps_enable = " + gps_enable);

        return (gps_enable);
    }


   Listener gpsStatusListener = new Listener()
    {
        @Override
        public void onGpsStatusChanged(int event)
        {
            // LOG.debug("gps statue : "+event);

            // GPS ���¸� ���� �޴´�.
            GpsStatus status = mLocationManager.getGpsStatus(null);

            if (event == GpsStatus.GPS_EVENT_STOPPED)
            {
                // GPS �̺�Ʈ�� ���߸� network provider �� ����Ѵ�.
                blockNetProvider = false;
            }
            else
            {
                Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

                int j = 0;

                while (satellites.hasNext())
                {
                    GpsSatellite satellite = satellites.next();

                    if (satellite.usedInFix())
                    {
                        j++;
                    }
                }

                // usedInFix ������ ������ 4�� �̻��̸� ��ȣ�� �����̹Ƿ�, network provider �� ���´�.
                // 4�� �̸��̸� �������� ��ġ�� ���� �� �����Ƿ�, network provider �� ����Ѵ�.
                if (j >= 4)
                {
                    blockNetProvider = true;
                }
                else
                {
                    blockNetProvider = false;
                }

                // LOG.debug("satellite count: "+i+", "+j);
            }
        }
    };


    LocationListener gpsListener = new LocationListener()
    {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onLocationChanged(Location location)
        {
            if (location != null)
            {
                if (lastLocation == null)
                {
                    lastLocation = new LastLocation();
                }

                if (lastLocation.setLocation(LocationManager.GPS_PROVIDER, location))
                {
                    for (ISensorListener listener : mListListeners)
                    {
                        listener.updateLocation(lastLocation.getLocation());
                    }
                }
            }
        }
    };

    LocationListener netListener = new LocationListener()
    {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onLocationChanged(Location location)
        {
            // LOG.debug("satellite network onLocationchanged()");
            if (blockNetProvider)
                return; // GPS ���°� ���Ƽ� Network provider �� ����� �ʿ䰡 ���� ��� �ƿ� ���ƹ�����.

            if (location != null)
            {
                if (lastLocation == null)
                {
                    lastLocation = new LastLocation();
                }

                if (lastLocation.setLocation(LocationManager.NETWORK_PROVIDER, location))
                {
                    for (ISensorListener listener : mListListeners)
                    {
                        listener.updateLocation(lastLocation.getLocation());
                    }
                }
            }
        }
    };

    LocationListener passiveListener = new LocationListener()
    {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onLocationChanged(Location location)
        {
            // LOG.debug("satellite network onLocationchanged()");
            if (blockNetProvider)
                return; // GPS ���°� ���Ƽ� Network provider �� ����� �ʿ䰡 ���� ��� �ƿ� ���ƹ�����.

            if (location != null)
            {
                if (lastLocation == null)
                {
                    lastLocation = new LastLocation();
                }

                if (lastLocation.setLocation(LocationManager.PASSIVE_PROVIDER, location))
                {
                    for (ISensorListener listener : mListListeners)
                    {
                        listener.updateLocation(lastLocation.getLocation());
                    }
                }
            }
        }
    };
}
