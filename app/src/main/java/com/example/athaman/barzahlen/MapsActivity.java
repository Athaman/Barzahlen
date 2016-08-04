package com.example.athaman.barzahlen;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    public static final String LOG_TAG = "MAP";
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    //User location defaults to Berlin in case they refuse location services.
    private static LatLng sUserLocation = new LatLng(52.525123, 13.369649);
    private static Polyline line;
    private LocationRequest mLocationRequest;
    private EditText editSearch;
    private ImageButton btnRefresh;
    private ImageButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        editSearch = (EditText) findViewById(R.id.search);
        btnRefresh = (ImageButton) findViewById(R.id.refresh);
        btnSearch = (ImageButton) findViewById(R.id.searchButton);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This just sets more markers for the new screen. Could delete old markers for
                //performance or leave them for convenience of user to scroll back. Possibly could
                //set a max marker variable and overwrite old ones.
                findNearbyStores();
            }
        });
        //Build a Google API client including the Location Services.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        //check if there is an instance of the google API connected and disconnect if there is.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Connect to the api client.
        mGoogleApiClient.connect();


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Directions directions = new Directions(sUserLocation, marker.getPosition());
                directions.execute();

                return true;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //once connected to the client get the last known user location, update the camera, and
        //set the nearby store markers.
        getLocation();
        updateCamera(sUserLocation);
        findNearbyStores();

    }

    @Override
    public void onConnectionSuspended(int i) {
        //reconnect to the api client if it drops
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO - handle connection failures

    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results) {
        if (reqCode == 1) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                updateCamera(sUserLocation);
                findNearbyStores();
            } else {
                //If the user denies permission warn them that it's fundamental to the app and ask again.
                Toast.makeText(this, "Your location is pretty important for this app.", Toast.LENGTH_SHORT).show();
                getLocation();
            }
        }
    }

    private boolean getLocation() {
        //Check for permissions to use the access fine location and ask for it if not found
        int permCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            //if user permission is set properly check the last known location
            try {
                Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                sUserLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                mMap.setMyLocationEnabled(true);
                mLocationRequest = new LocationRequest().setInterval(1000)
                        .setFastestInterval(5000)
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setSmallestDisplacement(1F);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                return true;

            } catch (SecurityException e) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }

        }
        return false;
    }

    private void updateCamera(LatLng location) {

        //updates the camera to a given location and a fixed zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

    }


    public static void drawLocations(List<LatLng> locations) {
        //this takes the locations passed back from the set stores function and adds markers
        //to the map for each location in the list.
        for (LatLng location : locations) {
            mMap.addMarker(new MarkerOptions().position(location));
        }
    }

    private void findNearbyStores() {
        //creates an instance of the Stores class based on the boundaries of the map displayed on
        //screen at the time. The Stores class calls back to drawLocations to display markers.
        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;
        Stores stores = new Stores(curScreen);
        stores.execute();
    }

    public static void drawDirections(List<LatLng> directions) {
        //if the user already has directions displayed delete them.
        if (line != null) {
            line.remove();
        }

        PolylineOptions direction = new PolylineOptions()
                .add(sUserLocation)
                .addAll(directions);
        line = mMap.addPolyline(direction);
    }

    //disconnect from API on pause for battery life.
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if already connected (following onCreate) if not then connect to google API
        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        sUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void search(){

    }


}
