package fi.tuni.tiko.busradartampere;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Asynctask class for fetching route information
 *
 * @author Mikko Mustasaari
 * @version 2019.0422
 * @since 1.0
 */

public class FetchRoute extends AsyncTask<Void,Void,String> {

    MapsActivity mainActivity;
    InputStream inputStream = null;
    String routeURL = "";

    /*
     * Constructor
     * @param reference to mainActivity
     * @param routeURL to fetch route information from
     */

    public FetchRoute(MapsActivity maps, String routeURL) {

        this.mainActivity = maps;
        this.routeURL = routeURL;
    }

    /*
     * Fetch route information and add it to result stinr until done
     */

    @Override
    protected String doInBackground(Void... voids) {

        String result = "";

        try {
            URL url = new URL(routeURL);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();

            //Log.d("BRT", "Route URL : " +routeURL);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String text = "";

            while (text != null) {
                text = bufferedReader.readLine();
                result += text;
            }
            if (inputStream != null) {
                inputStream.close();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Method for extracting needed information from fetched data
     *
     * @param result fetched data from doInBackground-method
     */

    @Override
    protected void onPostExecute(String result) {

        JSONObject jsonObject;
        JSONArray json2;
        JSONObject json3;
        JSONArray json4;
        JSONObject json5;
        JSONObject json6;

        String route = "";

        try {
            jsonObject = new JSONObject(result);
            json2 = jsonObject.getJSONArray("body");
            json3 = json2.getJSONObject(0);
            json4 = json3.getJSONArray("calls");

            for (int i = 0; i < json4.length(); i++) {
                json5 = json4.getJSONObject(i);
                json6 = json5.getJSONObject("stopPoint");
                String[] latlonArray = json6.getString("location").split(",");
                Double lat = Double.parseDouble(latlonArray[0]);
                Double lon = Double.parseDouble(latlonArray[1]);

                mainActivity.drawBusStop(lat, lon);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onPostExecute(result);

    }
}
