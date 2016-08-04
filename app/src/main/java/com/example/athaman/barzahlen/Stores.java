package com.example.athaman.barzahlen;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by keone on 8/2/2016.
 */
public class Stores {

    private static final String LOG_TAG = Stores.class.getSimpleName();
    private static final String RAW_URL = "https://www.barzahlen.de/";
    private LatLngBounds mBoundary;


    public Stores(LatLngBounds boundary) {
        mBoundary = boundary;
    }

    public void execute() {
        String url = makeURL();
        DownloadStoreData downloadRawData = new DownloadStoreData();
        downloadRawData.execute(url);
    }

    private String makeURL() {
        LatLng northEast = mBoundary.northeast;
        LatLng southWest = mBoundary.southwest;
        String southWestLat = String.valueOf(southWest.latitude);
        String southWestLng = String.valueOf(southWest.longitude);
        String northEastLat = String.valueOf(northEast.latitude);
        String northEastLng = String.valueOf(northEast.longitude);

        return RAW_URL + "filialfinder/get_stores?map_bounds=((" + southWestLat + ","
                + southWestLng + "),(" + northEastLat + "," + northEastLng + "))";
    }

    private ArrayList<LatLng> processResults(String webData) {
        ArrayList<LatLng> stores = new ArrayList<>();
        if (webData != null) {
            try {
                JSONArray locations = new JSONArray(webData);
                for (int i = 0; i < locations.length(); i++) {
                    JSONObject store = locations.getJSONObject(i);
                    double lat = store.getDouble("lat");
                    double lng = store.getDouble("lng");
                    stores.add(new LatLng(lat, lng));
                }
                return stores;
            } catch (JSONException e) {
                //TODO handle this.
                Log.e(LOG_TAG, "Something went horribly wrong processing the JSON");
            }
        }
        return null;
    }

    public class DownloadStoreData extends GetRawData {

        @Override
        protected String doInBackground(String... urls){
            return super.doInBackground(urls);
        }

        @Override
        protected void onPostExecute(String jsonString) {
            ArrayList<LatLng> stores = processResults(jsonString);
            MapsActivity.drawLocations(stores);
        }
    }

}


