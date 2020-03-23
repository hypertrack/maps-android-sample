# HyperTrack Maps-sample for Android SDKs

![License](https://img.shields.io/github/license/hypertrack/maps-android-sample.svg)

[HyperTrack](https://www.hypertrack.com) lets you add live location tracking to your mobile app.
Live location is made available along with ongoing activity, tracking controls and tracking outage with reasons.
This repo contains an complex example of implementation HyperTrack libraries and shows how they work together.

## Create HyperTrack Account

[Sign up](https://dashboard.hypertrack.com/signup) for HyperTrack and 
get your publishable key from the [Setup page](https://dashboard.hypertrack.com/setup).

## Clone maps-sample

Clone the app and import the source of either `maps-java` folder using your favorite Android IDE.

### Set your Publishable Key

Open the Quickstart project inside the workspace and set your Publishable Key (from [Setup page](https://dashboard.hypertrack.com/setup)) inside the placeholder
in the [`MainActivity.java`](https://github.com/hypertrack/maps-android-sample/blob/master/maps-java/src/main/java/com/hypertrack/maps/MapsActivity.java#L35) file.

### Customize sample
You can do customizations with `GoogleMapConfig` you need for google maps. 
```
    GoogleMapConfig.Builder mapConfigBuilder = GoogleMapConfig.newBuilder(this);

    GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfigBuilder.build());
    hyperTrackMap = HyperTrackMap.getInstance(this, mapAdapter)
```
Or you can customize with simple style
```
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        ...
        <item name="hyperTrackMapStyle">@style/HyperTrackMap</item>
    </style>

    <style name="HyperTrackMap">
        <item name="myLocationIcon">@drawable/icon_drive_base_transparent</item>
        <item name="myLocationBearingIcon">@drawable/icondrive</item>
        <item name="tripDestinationIcon">@drawable/ic_destination_marker</item>
        <item name="tripRouteColor">@android:color/black</item>
        <item name="tripCompletedOriginIcon">@drawable/ic_source_marker</item>
        <item name="tripCompletedDestinationIcon">@drawable/ic_destination_marker</item>
        <item name="tripCompletedRouteColor">@android:color/black</item>
    </style>
```

## Dashboard

Once your app is running, go to the [dashboard](https://dashboard.hypertrack.com/devices) where you can see a list of all your devices and their live location with ongoing activity on the map.

## Documentation
For detailed documentation of the APIs, customizations and what all you can build using HyperTrack, please visit the official [docs](https://hypertrack.com/docs/references/#references-apis).

- [HyperTrack Quickstart](https://github.com/hypertrack/quickstart-android)
- [HyperTrack Views Sample](https://github.com/hypertrack/views-android)
- [HyperTrack Maps SDK](https://github.com/hypertrack/sdk-maps-google-android)

## Support
Join our [Slack community](https://join.slack.com/t/hypertracksupport/shared_invite/enQtNDA0MDYxMzY1MDMxLTdmNDQ1ZDA1MTQxOTU2NTgwZTNiMzUyZDk0OThlMmJkNmE0ZGI2NGY2ZGRhYjY0Yzc0NTJlZWY2ZmE5ZTA2NjI) for instant responses. You can also email us at help@hypertrack.com.

