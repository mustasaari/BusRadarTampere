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

public class FetchRoute extends AsyncTask<Void,Void,String> {

    MapsActivity mainActivity;
    InputStream inputStream = null;
    String routeURL = "";

    public FetchRoute(MapsActivity maps, String routeURL) {

        this.mainActivity = maps;
        this.routeURL = routeURL;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result = "";

        Log.d("BRT", "kaynnistyisko");

        try {
            URL url = new URL(routeURL);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();

            Log.d("BRT", "URL : " +routeURL);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String text = "";

            while (text != null) {
                text = bufferedReader.readLine();

                Log.d("BT", "" +text);
                result += text;
            }
            Log.d("BRT", "result hommattu");
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

    @Override
    protected void onPostExecute(String result) {
        Log.d("BRT", "routen piirto");
        //mainActivity.clearMyMap();

        JSONObject jsonObject;
        JSONArray json2;
        JSONObject json3;
        JSONArray json4;
        JSONObject json5;
        JSONObject json6;


        String route = "";
        Log.d("BRT", "result tulostus : " +result);

        try {
            jsonObject = new JSONObject(result);
            json2 = jsonObject.getJSONArray("body");
            json3 = json2.getJSONObject(0);
            json4 = json3.getJSONArray("calls");

            for (int i = 0; i < json4.length(); i++) {
                json5 = json4.getJSONObject(i);
                json6 = json5.getJSONObject("stopPoint");
                String[] latlonArray = json6.getString("location").split(",");
                Log.d("BRT", "latitude : " + latlonArray[0]);
                Log.d("BRT", "longitude : " +latlonArray[1]);
                Double lat = Double.parseDouble(latlonArray[0]);
                Double lon = Double.parseDouble(latlonArray[1]);

                mainActivity.drawBusStop(lat, lon);
            }

            Log.d("BRT","tulsota route : " +json4.toString());
        } catch (JSONException e) {
            Log.d("BRT", "json exeption");
            e.printStackTrace();
        }

        super.onPostExecute(result);

    }
}
