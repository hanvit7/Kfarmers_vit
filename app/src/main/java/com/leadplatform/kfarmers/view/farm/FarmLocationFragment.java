package com.leadplatform.kfarmers.view.farm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class FarmLocationFragment extends BaseFragment
{
    public static final String TAG = "FarmLocationFragment";

    private String farmLatitude, farmLongitude, farmAddress;
    private TextView addressText;
    private GoogleMap mGoogleMap;

    public static FarmLocationFragment newInstance(String farmLatitude, String farmLongitude, String farmAddress)
    {
        final FarmLocationFragment f = new FarmLocationFragment();

        final Bundle args = new Bundle();
        args.putString("farmLatitude", farmLatitude);
        args.putString("farmLongitude", farmLongitude);
        args.putString("farmAddress", farmAddress);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_FARMMAP);

        if (getArguments() != null)
        {
            farmLatitude = getArguments().getString("farmLatitude");
            farmLongitude = getArguments().getString("farmLongitude");
            farmAddress = getArguments().getString("farmAddress");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_farm_location, container, false);

        addressText = (TextView) v.findViewById(R.id.addressText);
        mGoogleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView)).getMap();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        addressText.setText(farmAddress);

        LatLng farmLocation = new LatLng(Double.valueOf(farmLatitude), Double.valueOf(farmLongitude));

        CameraUpdate center = CameraUpdateFactory.newLatLng(farmLocation);
        mGoogleMap.moveCamera(center);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mGoogleMap.animateCamera(zoom);
        MarkerOptions markerOptions = new MarkerOptions().position(farmLocation);
        mGoogleMap.addMarker(markerOptions).showInfoWindow();
    }

}
