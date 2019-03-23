package fi.tuni.tiko.busradartampere;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateMarkers extends IntentService {

    JSONObject busData;

    public UpdateMarkers() {
        super("UpdateMarkers");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateMarkers(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {


        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            try {
                busData = new JSONObject(bundle.getString("busdata"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject json1;
        JSONArray json2;
        JSONObject json3;
        JSONObject json4;
        JSONObject json5;

        try {
            json1 = busData;
            json2 = json1.getJSONArray("body");
            for (int i = 0; i < json2.length(); i++) {
                json3 = json2.getJSONObject(i);
                json4 = json3.getJSONObject("monitoredVehicleJourney");
                json5 = json4.getJSONObject("vehicleLocation");
                String line = json4.getString("journeyPatternRef");
                String vehicleRef = json4.getString("vehicleRef");
                Double lon = json5.getDouble("longitude");
                Double lat = json5.getDouble("latitude");
                sendMessage(lat, lon, line, vehicleRef);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendMessageToStartOver();

    }

    private void sendMessage(Double lat, Double lon, String line, String vehicleRef) {
        Intent intent = new Intent("my-event");
        // add data
        //Log.d("BT", "ollaanko taalla33333");
        intent.putExtra("restart", false);
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        intent.putExtra("line", line);
        intent.putExtra("vehicleref", vehicleRef);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToStartOver() {
        Intent intent = new Intent("my-event");
        intent.putExtra("restart", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}