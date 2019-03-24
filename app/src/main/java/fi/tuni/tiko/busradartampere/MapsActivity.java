package fi.tuni.tiko.busradartampere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    BroadcastReceiver mMessageReceiver;
    ArrayList<Marker> markers = new ArrayList<>();

    Bitmap busImageNorth;
    Bitmap busImageWest;
    Bitmap busImageEast;
    Bitmap busImageSouth;
    Bitmap busImageNW;
    Bitmap busImageNE;
    Bitmap busImageSW;
    Bitmap busImageSE;
    Bitmap busImageStopped;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d("BT", "ollaanko taalla55555555555");
                // Extract data included in the Intent
                boolean restart = intent.getBooleanExtra("restart", false);
                if (restart) {
                    fetchAgain();
                }
                else {
                    Double lon = intent.getDoubleExtra("lon", 0);
                    Double lat = intent.getDoubleExtra("lat", 0);
                    String line = intent.getStringExtra("line");
                    String vehicleRef = intent.getStringExtra("vehicleref");
                    //Log.d("BT", "Got message: " + message);
                    addBusMarker(lat, lon, line, vehicleRef);
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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("BT", "Marker clikced");
                return false;
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng tampere = new LatLng(61.49911, 23.78712);
        mMap.addMarker(new MarkerOptions().position(tampere).title("Marker in Sydney :O"));

        //line draw example
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(61.0, 23.0), new LatLng(62.0, 24.0))
                .width(5)
                .color(Color.RED));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(tampere));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tampere, 10f));
        mMap.setMaxZoomPreference(100f);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public void addBusMarker(Double lat, Double lon, String line, String vehicleRef) {

        double oldLat = 0;
        double oldLon = 0;

        for (Marker x : markers) {
            if (x.getSnippet().equals(vehicleRef)) {
                oldLat = x.getPosition().latitude;
                oldLon = x.getPosition().longitude;
                markers.remove(x);
                x.remove();
                break;
                //markers.remove(x);
            }
        }

        Log.d("BTT", "array size : " +markers.size());

        double directionLat = lat - oldLat;
        double directionLon = lon - oldLon;
        double degrees = Math.atan2(directionLon , directionLat);
        int degrees2 = (int) Math.toDegrees(degrees);

        Log.d("BT", "Degrees " +degrees2);
        Log.d("BT", " " +directionLat);
        Log.d("BT", " " +directionLon);

        LatLng newPoint = new LatLng(lat, lon);     //TOIMIVA!
        Marker marker = mMap.addMarker(new MarkerOptions().position(newPoint).title(line).anchor(0.5f,0.5f));//TOIMIVA!
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(textAsBitmap(line, 50, degrees2)));     // TOIMIVA   //SETICON KAATAA???
        marker.setSnippet(vehicleRef);
        markers.add(marker);
    }

    public Bitmap textAsBitmap(String text, float textSize, int degrees) {

        long time = System.nanoTime();  //start timing

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        Bitmap busImage = getBusDirectionImage(degrees);
        Bitmap busImageScaled = Bitmap.createScaledBitmap(busImage, 140, 140, false);

        Canvas canvas = new Canvas(busImageScaled);
        canvas.drawText(text, 70, 86, paint);

        long endtime = System.nanoTime();
        long finaltime = endtime - time;
        Log.d("BTT", "Delay : " +finaltime );   //end timing

        return busImageScaled;
    }

    public Bitmap getBusDirectionImage(int direction) {

        Bitmap busImage;

        if (direction == 0) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stopped);
            return busImageStopped;
        }
        else if (direction >= -23 && direction <= 23) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_up);
            return busImageNorth;
        }
        else if (direction >= 23 && direction <= 68) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_ne);
            return busImageNE;
        }
        else if (direction >= 69 && direction <= 113) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_right);
            return busImageEast;
        }
        else if (direction >= 114 && direction <= 158) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_se);
            return busImageSE;
        }
        else if (direction <= -24 && direction >= -68) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_nw);
            return busImageNW;
        }
        else if (direction <= -69 && direction >= -113) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_left);
            return busImageWest;
        }
        else if (direction <= -114 && direction >= -158) {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_sw);
            return busImageSW;
        }
        else {
            //busImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_down);
            return busImageSouth;
        }
    }

    //start service that does timing for marker placement
    public void startUpdateMarkers(String busData) {
        Log.d("BT", "START SERVICE");
        Intent i = new Intent(this, UpdateMarkers.class);
        i.putExtra("busdata", busData);
        startService(i);
    }

    //restart fetching process
    public void fetchAgain() {
        FetchBusLocations process = new FetchBusLocations(this);
        process.execute();
    }
}
