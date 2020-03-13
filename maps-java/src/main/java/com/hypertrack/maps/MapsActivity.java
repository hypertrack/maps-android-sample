package com.hypertrack.maps;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String HYPERTRACK_PUB_KEY = "5TEemY4gE1rMucM-TpSV8-GT7Ia6gGm9yuSr3ljp9_qF_9jgs2IjHD25rTQrIw6qwWn1YfAsgcLhIrpyrKrs8A";

    private String viewsDeviceID = "79E48CE0-35D8-4844-8D7B-A173D3F1328B";

    private HyperTrack hyperTrack;
    private HyperTrackViews hyperTrackViews;
    private HyperTrackMap hyperTrackMap;

    private TextView trackingStatus;
    private TextView trackingDeviceId;
    private TextView viewingDeviceId;

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
        initUi();

        hyperTrack = HyperTrack.getInstance(this, HYPERTRACK_PUB_KEY);
        hyperTrackViews = HyperTrackViews.getInstance(this, HYPERTRACK_PUB_KEY);

        if (viewsDeviceID.isEmpty()) viewsDeviceID = hyperTrack.getDeviceID();

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
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        hyperTrackViews.subscribeToDeviceUpdates(viewsDeviceID, this);

        GoogleMapConfig mapConfig = GoogleMapConfig.newBuilder(this).build();
        GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfig);
        hyperTrackMap = HyperTrackMap.getInstance(this, mapAdapter)
                .bind(new GpsLocationProvider(this))
                .bind(hyperTrackViews, viewsDeviceID);
        hyperTrackMap.moveToMyLocation();

        trackingDeviceId.setText(hyperTrack.getDeviceID());
        viewingDeviceId.setText(viewsDeviceID);
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

    private void initUi() {
        trackingStatus = findViewById(R.id.tracking_status);
        trackingDeviceId = findViewById(R.id.tracking_device_id);
        viewingDeviceId = findViewById(R.id.viewing_device_id);

        View.OnClickListener copyToClipboard = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", ((TextView) view).getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MapsActivity.this, clip.getDescription().getLabel(), Toast.LENGTH_SHORT).show();
            }
        };
        trackingDeviceId.setOnClickListener(copyToClipboard);
        viewingDeviceId.setOnClickListener(copyToClipboard);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        hyperTrackMap.destroy();
        hyperTrackViews.stopAllUpdates();
    }
}
