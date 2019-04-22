package fi.tuni.tiko.busradartampere;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Asynctask class for fetching bus locations
 *
 * @author Mikko Mustasaari
 * @version 2019.0422
 * @since 1.0
 */

public class FetchBusLocations extends AsyncTask<Void, Void, String> {

    /*
     * Stores received data as String
     */

    String data = "";

    InputStream inputStream = null;

    /*
     * References MapsActivity
     */

    MapsActivity mainActivity;

    /*
     * Constructor
     */

    public FetchBusLocations(MapsActivity a) {
        mainActivity = a;
    }

    /*
     * Fetch json information from Journeys API
     * Add information to result String line by line until everything is fecthed
     */

    @Override
    protected String doInBackground(Void... voids) {

        String result = "";

        try {
            //URL url = new URL("http://data.itsfactory.fi/journeys/api/1/vehicle-activity");
            //URL url = new URL("http://data.itsfactory.fi/journeys/api/1/vehicle-activity?lineRef=80");
            URL url = new URL("http://data.itsfactory.fi/journeys/api/1/vehicle-activity?exclude-fields=monitoredVehicleJourney.onwardCalls,recordedAtTime");

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String text = "";

            while (text != null) {
                text = bufferedReader.readLine();
                result += text;

                //curl -X GET http://data.itsfactory.fi/journeys/api/1/vehicle-activity?lineRef=80
            }

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * Return result
     */

    @Override
    protected void onPostExecute(String result) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mainActivity.startUpdateMarkers(result);

        super.onPostExecute(result);
    }
}

