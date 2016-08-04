package com.example.athaman.barzahlen;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keone on 8/3/2016.
 */
public class Directions {

    public static final String LOG_TAG = "Directions";

    private static final String sBaseURL = "https://maps.googleapis.com/maps/api/directions/json?";
    private String mOrigin = "origin=";
    private String mDestination = "&destination=";
    private String mUrl;
    private static String mode = "&mode=walking";
    private static String key = "&key=AIzaSyChohoOE2Ew3p7n42rPfzjVIe4DgGJjr2s";
    private List<LatLng> mNavPoints;

    public Directions(LatLng origin, LatLng destination){
        this.mOrigin += getLatLngCoords(origin);
        this.mDestination += getLatLngCoords(destination);
    }

    //make the final URL, initiate the get directions class and run it.
    public void execute(){
        setUrl();
        GetDirectionData downloadRawData = new GetDirectionData();
        downloadRawData.execute(mUrl);
    }

    //converts an input LatLng into a string of the coords comma separated.
    private String getLatLngCoords(LatLng loc){
        Double lat = loc.latitude;
        Double lng = loc.longitude;
        return lat.toString() + "," + lng.toString();
    }

    private void setUrl(){
        mUrl = sBaseURL  + mOrigin  + mDestination  + mode + key;
    }

    //takes in the result of the download in a raw JSON String
    private void processResults(String webData){
        mNavPoints = new ArrayList<LatLng>();
        if (webData != null) {
            try {
                //Turn the String into a JSON object then fetch the "steps" information
                JSONObject directions = new JSONObject(webData);
                JSONArray routes =  directions.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

                //iterate through the steps and save their end locations as LatLngs
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    JSONObject end = step.getJSONObject("end_location");
                    double lat = end.getDouble("lat");
                    double lng = end.getDouble("lng");
                    mNavPoints.add(new LatLng(lat, lng));
                }
            } catch (JSONException e) {
                //TODO handle this.
                Log.e(LOG_TAG, "something went horribly wrong with your JSON");
            }
        }

    }

    public class GetDirectionData extends GetRawData{

        @Override
        protected String doInBackground(String... urls){
            return super.doInBackground(urls);
        }

        @Override
        protected void onPostExecute(String webData){
            processResults(webData);
            MapsActivity.drawDirections(mNavPoints);
        }
    }

}
