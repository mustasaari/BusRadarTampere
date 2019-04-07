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

public class FetchBusLocations extends AsyncTask<Void, Void, String> {

    String data = "";
    InputStream inputStream = null;

    MapsActivity mainActivity;

    public FetchBusLocations(MapsActivity a) {
        mainActivity = a;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result = "";

        Log.d("BRT", "LATAUS ALKAA");

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
                //if (text != null) {
                //if (text.contains("lineRef")) {
                //    Log.d("BT", " " +text);
                //}
                Log.d("BT", "" +text);
                result += text;
                //}
                //if (text.contains("lineRef") && !text.equals("")) {
                //Log.d("BT", "" + text);
                //data = data + text;
                //}
                //curl -X GET http://data.itsfactory.fi/journeys/api/1/vehicle-activity?lineRef=80
            }
            //int myChar;
            //while ((myChar = inputStream.read() ) != -1) {
            //result += (char) myChar;
            //Log.d("BT", "" +(char) myChar);
            //}
            Log.d("BT", "result hommattu");

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

        Log.d("BT", "tekeekohan tämän");
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("BT", "entä tämän");
        //mainActivity.clearMyMap();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //mainActivity.startUpdateMarkers(jsonObject);

        //mainActivity.fetchAgain();
        mainActivity.startUpdateMarkers(result);

        super.onPostExecute(result);
    }
}

