package com.example.silahianp1916.mymapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean notTrackingMyLocation = true;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static int MY_LOC_ZOOM_FACTOR = 17;

    private ArrayList<Circle> circles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //add marker on the map that shows your place of birth "born here" when tapped
        LatLng laJolla = new LatLng(32.8328, -117.2713);
        mMap.addMarker(new MarkerOptions().position(laJolla).title("born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(laJolla));

/*        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed Fine permission check");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed Course permission check");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);
        }
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            mMap.setMyLocationEnabled(true);
        }*/


        locationSearch = (EditText) findViewById(R.id.editText_addr);
        gotMyLocationOneTime = false;
        getLocation();

        //Add View button and method (Change View) to switch between
        //satellite and map views

    }

    public void changeView(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            Log.d("MyMapsApp", "changeView: change to satellite view");
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            Log.d("MyMapsApp", "changeView: change to normal view");
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    }

    public void onSearch(View v) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use LocationManaager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "onSearch: location= " + location);
        Log.d("MyMapsApp", "onSearch: location= " + provider);

        LatLng userLocation = null;
        try {
            //Check the last known location, need to specifically list the privider(netweork or gps)
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: location manager not null");
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + myLocation.getLongitude());
                    Toast.makeText(this, "Userlog: " + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_LONG).show();

                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is: " + myLocation.getLatitude() + myLocation.getLongitude());
                    Toast.makeText(this, "Userlog: " + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_LONG).show();

                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null!!");
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Exception on getLastKnownLocation ");
        }

        if (!location.matches("")) {
            //Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);
            try {

                //Get a list of Addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));
                Log.d("MyMapsApp", "created addressList ");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());

                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare() + address.getAddressLine(i)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }


    //Method getLocation to place marker at current location
    public void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }
            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: Netowrk is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: no provider is enabled");
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    //launch locationListenrGPS
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMaps", "getLocation: GPS Updates");
                    Toast.makeText(this, "Tracking is on", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught exception");
            e.printStackTrace();
        }
    }

    //LocationListener is an anoymous inner class
    //Setup for call backs from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp", "LocationListnerNetwork:location changed");
            dropAmarker(LocationManager.NETWORK_PROVIDER);



            //check if doing one time via onMapReadu, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime = true;

            } else {
                //if here then tracking so relaunch request for network
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "LocationListnerNetwork: status change");

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp", "LocationListnerGps:location changed");
            dropAmarker(LocationManager.GPS_PROVIDER);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (gotMyLocationOneTime == true) {
                locationManager.removeUpdates(this);
            }

        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle extras) {
            Log.d("MyMaps", "locationListenerGPS: onStatusChanged utilized and working");
            Toast.makeText(getApplicationContext(), "LocationListenerGPS onStatusChanged", Toast.LENGTH_SHORT).show();

            //Switch/case statement
            switch (i) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "LocationProvider is available");
                    Toast.makeText(getApplicationContext(), "LocationProvider is available", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "LocationProvider out of service");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Toast.makeText(getApplicationContext(), "LocationProvider is out of service", Toast.LENGTH_SHORT).show();
                    break;


                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "LocationProvider is temporarily unavailable");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;


                default:
                    Log.d("MyMaps", "LocationProvider default");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                    break;
                //switch
                //case locationProvider.Available
                //print out log d
                //break
                //case Locationprovider. out of service:
                //enable network updates
                //break
                //case LocationProvider.Temporarily unavailable:
                //enable both network and gps
                //break
                //default both network and gps

            }
        }


        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



    public void dropAmarker(String provider){
        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            myLocation = locationManager.getLastKnownLocation(provider);
        }
        if (myLocation == null) {
            //display a message via log.d and/or toast
            Log.d("MyMaps", "myLocation is null");
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            if(provider == LocationManager.GPS_PROVIDER) {
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(1));
                Circle circle1 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(4)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2));
                Circle circle2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(8)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2));
                mMap.animateCamera(update);
                Log.d("MyMaps", "Marker Dropper for GPS accessed");
            }
            else {
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2));
                Circle circle1 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(4)
                        .strokeColor(Color.RED)
                        .strokeWidth(2));
                Circle circle2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(8)
                        .strokeColor(Color.RED)
                        .strokeWidth(2));
                mMap.animateCamera(update);
                Log.d("MyMaps", "Marker Dropper for NETWORK accessed");
            }
            /**else if(isNetworkEnabled){
             Circle circle = mMap.addCircle(new CircleOptions()
             .center(userLocation)
             .radius(1)
             .strokeColor(Color.BLACK)
             .strokeWidth(2));
             mMap.animateCamera(update);
             Log.d("MyMaps", "Marker Dropper for Network accessed");
             };**/

            mMap.animateCamera(update);
            //if(locationManager != null){
            //if checkSelfPermission fails
            //return
            //myLocation = locationManager.getLastKnownLocation(provider)
            //LatLng userLocation = null
            //if(myLocation == null) print log or toast message
            //else
            // userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude)
            //CameraUpdate = CameraUpdatefactory.newLatLng(userLocation,My_Loc_zoom)
            //if (provider == LocationManager.gps_provider)
            //add circle for 2 outer rings (red)
            //    .center(userLocation)
            //    radius(1)
            //.strokecolor(Color.RED)
            //.strokeWidth(2)
            //.fillColor(Color.RED)
            //else add cirrcle with 2 outer rings(blue
            //mMap.animateCamera(update
        }
    }
    public void trackMyLocation(View view){



        if(notTrackingMyLocation){
            getLocation();
            notTrackingMyLocation = false;
        }else{
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps);
            notTrackingMyLocation= true;
        }

    }
    public void clearMarkers(View view){
        mMap.clear();
    }






}