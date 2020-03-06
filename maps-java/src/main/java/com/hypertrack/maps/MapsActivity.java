package com.hypertrack.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.maps.google.widget.GoogleMapConfig;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingStateObserver;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.Location;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.GpsLocationProvider;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.sdk.views.maps.TripSubscription;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DeviceUpdatesHandler {

    private static final String HYPERTRACK_PUB_KEY = "YOUR_KEY_HERE";

    private TextView trackingStatus;

    private HyperTrack hyperTrack;
    private HyperTrackViews hyperTrackViews;
    private HyperTrackMap hyperTrackMap;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra(TrackingStateObserver.EXTRA_KEY_CODE_, 0);
            switch (code) {
                case TrackingStateObserver.EXTRA_EVENT_CODE_START:
                    onTrackingStarted();
                case TrackingStateObserver.EXTRA_EVENT_CODE_STOP:
                    onTrackingStopped();
                    break;
                default:
                    // Some critical error in SDK.
                    break;
            }
        }
    };

    public final Map<String, TripSubscription> tripSubscriptionsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        trackingStatus = findViewById(R.id.tracking_status);

        hyperTrack = HyperTrack.getInstance(this, HYPERTRACK_PUB_KEY);
        hyperTrackViews = HyperTrackViews.getInstance(this, HYPERTRACK_PUB_KEY);

        hyperTrack.requestPermissionsIfNecessary();
        if (hyperTrack.isRunning()) {
            onTrackingStarted();
        } else {
            onTrackingStopped();
        }
        registerReceiver(broadcastReceiver, new IntentFilter(TrackingStateObserver.ACTION_TRACKING_STATE));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMapConfig mapConfig = GoogleMapConfig.newBuilder(this).build();
        GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfig);
        hyperTrackMap = HyperTrackMap.getInstance(this, mapAdapter)
                .bind(new GpsLocationProvider(this))
                .bind(hyperTrackViews, hyperTrack.getDeviceID());

        hyperTrackViews.subscribeToDeviceUpdates(hyperTrack.getDeviceID(), this);

        hyperTrackMap.setLocationUpdatesListener(new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                hyperTrackMap.moveToMyLocation();
                hyperTrackMap.setLocationUpdatesListener(null);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        });
    }

    private void onTrackingStarted() {
        trackingStatus.setText(getString(R.string.active));
        trackingStatus.setActivated(true);
    }

    private void onTrackingStopped() {
        trackingStatus.setText(getString(R.string.inactive));
        trackingStatus.setActivated(false);
    }

    @Override
    public void onLocationUpdateReceived(@NonNull Location location) {
    }

    @Override
    public void onBatteryStateUpdateReceived(int i) {
    }

    @Override
    public void onStatusUpdateReceived(@NonNull StatusUpdate statusUpdate) {
    }

    @Override
    public void onTripUpdateReceived(@NonNull Trip trip) {

        boolean isNewTrip = !tripSubscriptionsMap.containsKey(trip.getTripId());
        boolean isActive = !trip.getStatus().equals("completed");

        if (isActive) {
            if (isNewTrip) {
                TripSubscription tripSubscription = hyperTrackMap.subscribeTrip(trip.getTripId());
                tripSubscriptionsMap.put(trip.getTripId(), tripSubscription);
                hyperTrackMap.moveToTrip(trip);
            }
        } else {
            if (!isNewTrip) {
                tripSubscriptionsMap.remove(trip.getTripId()).remove();
            }
        }

    }

    @Override
    public void onError(@NonNull Exception e, @NonNull String s) {
    }

    @Override
    public void onCompleted(@NonNull String s) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        hyperTrackMap.destroy();
        hyperTrackViews.stopAllUpdates();
    }
}
