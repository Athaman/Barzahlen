package com.example.athaman.barzahlen;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by keone on 8/3/2016.
 */
public class Directions {

    public static final String LOG_TAG = "Directions";

    private static final String BASE_URL= "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String MODE = "&mode=walking";
    private static final String KEY = "&key=AIzaSyChohoOE2Ew3p7n42rPfzjVIe4DgGJjr2s";
    private String mOrigin = "origin=";
    private String mDestination = "&destination=";

    public Directions(LatLng origin, LatLng destination){
        mOrigin += getLatLngCoords(origin);
        mDestination += getLatLngCoords(destination);
    }

    //make the final URL, initiate the get directions class and run it.
    public void execute(){
        String url = setUrl();
        GetDirectionData downloadRawData = new GetDirectionData();
        downloadRawData.execute(url);
    }

    //converts an input LatLng into a string of the coords comma separated.
    private String getLatLngCoords(LatLng loc){
        Double lat = loc.latitude;
        Double lng = loc.longitude;
        return lat.toString() + "," + lng.toString();
    }

    private String setUrl(){
       return BASE_URL  + mOrigin  + mDestination  + MODE + KEY;
    }

    //takes in the result of the download in a raw JSON String
    private ArrayList<LatLng> processResults(String jsonString){
        Log.d("JSON", jsonString);
        ArrayList<LatLng> navPoints = new ArrayList<LatLng>();
        if (jsonString != null) {
            try {
                //Turn the String into a JSON object then fetch the "steps" information
                JSONObject directions = new JSONObject(jsonString);
                JSONArray routes =  directions.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

                //iterate through the steps and save their end locations as LatLngs
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    JSONObject end = step.getJSONObject("end_location");
                    double lat = end.getDouble("lat");
                    double lng = end.getDouble("lng");
                    navPoints.add(new LatLng(lat, lng));
                }
                return navPoints;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "something went horribly wrong with your JSON");
            }
        }
        return null;

    }

    public class GetDirectionData extends GetRawData{

        @Override
        protected String doInBackground(String... urls){
            return super.doInBackground(urls);
        }

        @Override
        protected void onPostExecute(String jsonString){
            ArrayList<LatLng> navPoints = processResults(jsonString);
            MapsActivity.drawDirections(navPoints);
        }
    }

}
