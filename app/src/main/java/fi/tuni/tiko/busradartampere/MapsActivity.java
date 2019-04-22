package fi.tuni.tiko.busradartampere;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Console;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Main-class of program
 *
 * This program fetches bus information from tampereen joukkoliikenne avoin data Journeys API
 * and draws location of busses and routes on google map. User can also see their own location
 * as red dor when GPS-information is available. Shown lines can be filtered from FilterActivity.
 *
 * @author Mikko Mustasaari
 * @version 2019.0422
 * @since 1.0
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Arrays for storing marker-data and references to map and messagereceiver.
     */

    private GoogleMap mMap;
    BroadcastReceiver mMessageReceiver;
    ArrayList<Marker> markers = new ArrayList<>();
    ArrayList<Marker> markersRoute = new ArrayList<>();
    ArrayList<String> allLines = new ArrayList<>();
    ArrayList<Polyline> polyLines = new ArrayList<>();

    /**
     * States for applicetion lifecycle. Stop when minimized and faster first fetch.
     */

    boolean linesAdded = false;
    boolean firstUpdate = true;
    boolean appRunning = false;

    /**
     * Bus image data
     */

    Bitmap busImageNorth;
    Bitmap busImageWest;
    Bitmap busImageEast;
    Bitmap busImageSouth;
    Bitmap busImageNW;
    Bitmap busImageNE;
    Bitmap busImageSW;
    Bitmap busImageSE;
    Bitmap busImageStopped;
    Bitmap busstop;
    Bitmap userLocation;

    /**
     * Shared preferences
     */

    SharedPreferences sharedpreferences;

    /**
     * GPS-location
     */

    LocationManager locationManager;
    Listener locationListener;
    Marker userLocationMarker;

    /*
     * Oncreate is initial setup of application.
     * Setup images. Setup map. Setup messagereceiver that updates bus locations.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //comment for github test
        busImageStopped = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stopped);
        busImageNorth = BitmapFactory.decodeResource(getResources(), R.drawable.bus_up);
        busImageWest = BitmapFactory.decodeResource(getResources(), R.drawable.bus_left);
        busImageEast = BitmapFactory.decodeResource(getResources(), R.drawable.bus_right);
        busImageSouth = BitmapFactory.decodeResource(getResources(), R.drawable.bus_down);
        busImageNW = BitmapFactory.decodeResource(getResources(), R.drawable.bus_nw);
        busImageNE = BitmapFactory.decodeResource(getResources(), R.drawable.bus_ne);
        busImageSE = BitmapFactory.decodeResource(getResources(), R.drawable.bus_se);
        busImageSW = BitmapFactory.decodeResource(getResources(), R.drawable.bus_sw);
        busstop = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
        userLocation = BitmapFactory.decodeResource(getResources(), R.drawable.user);

        sharedpreferences = getSharedPreferences("fi.tuni.tiko.busradartampere", Context.MODE_PRIVATE);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        appRunning = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Extract data included in the Intent
                boolean restart = intent.getBooleanExtra("restart", false);
                if (restart) {
                    linesAdded = true;
                    fetchAgain();
                }
                else {
                    Double lon = intent.getDoubleExtra("lon", 0);
                    Double lat = intent.getDoubleExtra("lat", 0);
                    String line = intent.getStringExtra("line");
                    String vehicleRef = intent.getStringExtra("vehicleref");
                    String routeURL = intent.getStringExtra("routeurl");


                    String lineFilter = sharedpreferences.getString(line, "show");

                    if (lineFilter.equals("show")) {
                        addBusMarker(lat, lon, line, vehicleRef, routeURL);
                    }
                    else {
                        for (Marker marker : markers) {
                            if (marker.getTitle().equals(line)) {
                                markers.remove(marker);
                                marker.remove();
                                break;
                            }
                        }
                    }

                    if (!linesAdded) {
                        if (!allLines.contains(line)) {
                            allLines.add(line);
                        }
                    }

                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));

        FetchBusLocations process = new FetchBusLocations(this);
        process.execute();
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

        //Set map style
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        //Marker onclick listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                BusTagObject tag = (BusTagObject) marker.getTag();
                if (tag != null) {
                    drawRoute(tag.getRouteURL());
                }
                //hide info window and disable center to marker
                return true;
            }
        });

        // Create latitude longitude location with coordinates of Tampere
        LatLng tampere = new LatLng(61.49911, 23.78712);

        //Set camera to Tampere location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tampere, 10f));
        mMap.setMaxZoomPreference(100f);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        //Try get GPS location
        getGPSLocation();
    }

    /*
     * Add new marker for bus
     *
     * @param lat latitude of bus
     * @param lon longitude of bus
     * @param line vehicle line name
     * @param vehicleRef unique vehicle id
     * @param routeURL url for fetching route information if marker is clicked
     */

    public void addBusMarker(Double lat, Double lon, String line, String vehicleRef, String routeURL) {

        double oldLat = 0;
        double oldLon = 0;

        //if vehicle is alredy found in markers array, removes old marker and draws new one becouse direction has possibly changed
        for (Marker x : markers) {
            if (x.getSnippet().equals(vehicleRef)) {
                oldLat = x.getPosition().latitude;
                oldLon = x.getPosition().longitude;
                markers.remove(x);
                x.remove();
                break;
            }
        }

        //calculate vehicle direction based on old and new position
        double directionLat = lat - oldLat;
        double directionLon = lon - oldLon;
        double degrees = Math.atan2(directionLon , directionLat);
        int degrees2 = (int) Math.toDegrees(degrees);

        //Start drawing new marker
        LatLng newPoint = new LatLng(lat, lon);
        Marker marker = mMap.addMarker(new MarkerOptions().position(newPoint).title(line).anchor(0.5f,0.5f));

        //Set icon of marker
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(textAsBitmap(line, 50, degrees2)));
        marker.setSnippet(vehicleRef);

        //Assing BusTagObject as tag for marke containing routeURL
        BusTagObject tag = new BusTagObject(routeURL);
        marker.setTag(tag);

        //Finally add marker to array
        markers.add(marker);
    }

    /*
     * Draw bus bitmap based on direction and line name
     *
     * @param text to write on bus icon
     * @param textSize size of text to be drawn
     * param degrees calculated direction of bus
     *
     */

    public Bitmap textAsBitmap(String text, float textSize, int degrees) {

        //long time = System.nanoTime();  //Debug for how long bitmap drawing takes

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        Bitmap busImage = getBusDirectionImage(degrees);
        Bitmap busImageScaled = Bitmap.createScaledBitmap(busImage, 140, 140, false);

        Canvas canvas = new Canvas(busImageScaled);
        canvas.drawText(text, 70, 86, paint);

        //long endtime = System.nanoTime();     //Debug for how long bitmap drawing takes
        //long finaltime = endtime - time;      //Debug for how long bitmap drawing takes
        //Log.d("BTT", "Delay : " +finaltime ); //Debug for how long bitmap drawing takes

        return busImageScaled;
    }

    /*
     * Return correct Bus bitmap based on direction
     * @param direction in degrees
     * @return correct bitmap based on direction
     */

    public Bitmap getBusDirectionImage(int direction) {

        if (direction == 0) {
            return busImageStopped;
        }
        else if (direction >= -23 && direction <= 23) {
            return busImageNorth;
        }
        else if (direction >= 23 && direction <= 68) {
            return busImageNE;
        }
        else if (direction >= 69 && direction <= 113) {
            return busImageEast;
        }
        else if (direction >= 114 && direction <= 158) {
            return busImageSE;
        }
        else if (direction <= -24 && direction >= -68) {
            return busImageNW;
        }
        else if (direction <= -69 && direction >= -113) {
            return busImageWest;
        }
        else if (direction <= -114 && direction >= -158) {
            return busImageSW;
        }
        else {
            return busImageSouth;
        }
    }

    /*
     * Start service that does timing for marker placement.
     *
     * @param busdata data containing all locations of busses
     *
     */
    public void startUpdateMarkers(String busData) {

        Intent i = new Intent(this, UpdateMarkers.class);

        //Delays for drawing busses. First drawing cycle is faster
        int delay;
        if (firstUpdate) {
            firstUpdate = false;
            delay = 0;
        }
        else {
            delay = 100;
        }

        i.putExtra("busdata", busData);
        i.putExtra("delay", delay);
        startService(i);
    }

    /*
     * Method for restarting the fetching process
     */

    public void fetchAgain() {

        if (appRunning) {
            FetchBusLocations process = new FetchBusLocations(this);
            process.execute();
        }
    }

    /*
     * Method called when bus marker is clicked.
     * Starts process of fetching route and clears old lines from map.
     *
     * @param url to route info
     */

    public void drawRoute(String url) {
        clearMarkers();
        FetchRoute process = new FetchRoute(this, url);
        process.execute();
    }

    /*
     * Method fromdrawing locations of bus stops and route
     * Bus stops are not currently drawn
     * @param lat latitude of bus stop
     * @param lon longitude of bus stop
     */

    public void drawBusStop(Double lat, Double lon) {

        //Lines for drawing bus stops but probably takes too much resources
        //LatLng tampere = new LatLng(lat, lon);
        //Marker marker = mMap.addMarker(new MarkerOptions().position(tampere).title("bus stop").anchor(0.5f,0.5f));
        //marker.setIcon(BitmapDescriptorFactory.fromBitmap(busstop));
        //markersRoute.add(marker);

        int myBlue = Color.parseColor("#CF175DB9");

        //Draw route with lines
        if (polyLines.size() > 0) {
            Polyline prevLine = polyLines.get(polyLines.size() -1);
            List<LatLng> points = prevLine.getPoints();
            LatLng last = points.get(1);

            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(last, new LatLng(lat, lon))
                    .width(15)
                    .color(myBlue));
            polyLines.add(line);
        }
        else {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lat, lon), new LatLng(lat, lon))
                    .width(10)
                    .color(myBlue));
            polyLines.add(line);
        }
    }

    /*
     * Clear route from map
     */

    public void clearMarkers() {
        for (Marker marker : markersRoute) {
            marker.remove();
        }
        for (Polyline poly : polyLines) {
            poly.remove();
        }
        polyLines.clear();
    }

    /*
     * Called when filter button on main activity is pressed
     */

    public void launchFilterActivity(View view) {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putStringArrayListExtra("allLines", allLines);
        startActivity(intent);
    }

    /*
     * lifecycle methods for stopping downloads and faster first drawing
     */

    protected void onResume() {
        super.onResume();
        appRunning = true;
        firstUpdate = true;
        fetchAgain();
    }

    /*
     * lifecycle methods for stopping downloads and faster first drawing
     */

    public void onPause() {
        super.onPause();
        appRunning = false;
    }

    /*
     * User GPS location permissions. If permission not found ask for permissions. If not create locationlistener.
     */

    public void getGPSLocation(){
        //Log.d("BRT", "getGPS");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //Log.d("BRT", "permissions checked");
            //    ActivityCompat#requestPermissions
            String[] listOfPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, listOfPermissions , 0);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        else {
            //Log.d("BRT", "else kaynnistyy");
            locationListener = new Listener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener );
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        }
    }

    /*
     * GPS location listener inner-class
     * Draws user on map when GPS-location is updated.
     * Will not show user until GPS is found
     */

    class Listener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            if (userLocationMarker == null) {
                Bitmap userImageScaled = Bitmap.createScaledBitmap(userLocation, 120, 120, false);
                userLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are here").anchor(0.5f,0.5f));
                userLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(userImageScaled));
            }
            else {
                //Log.d("BRT", "location changed longitude " +location.getLongitude());
                //Log.d("BRT", "location changed latitude " +location.getLatitude());
                userLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
