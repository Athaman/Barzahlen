package com.example.athaman.barzahlen;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keone on 8/2/2016.
 */
public class Stores {

    public static final String LOG_TAG = Stores.class.getSimpleName();
    private String mRawUrl = "https://www.barzahlen.de/filialfinder/get_stores?map_bounds=((";
    private String mURL;
    private LatLngBounds mBoundary;
    private List<LatLng> mStores;


    public Stores(LatLngBounds boundary) {
        mBoundary = boundary;
        mURL = mRawUrl + boundary.northeast + "," + boundary.southwest + ")";
    }

    public void execute() {
        setUrl();
        DownloadStoreData downloadRawData = new DownloadStoreData();
        downloadRawData.execute(mURL);
    }

    private void setUrl() {
        mURL = mRawUrl;
        LatLng northEast = mBoundary.northeast;
        LatLng southWest = mBoundary.southwest;
        Double southWestLat = southWest.latitude;
        Double southWestLng = southWest.longitude;
        Double northEastLat = northEast.latitude;
        Double northEastLng = northEast.longitude;
        mURL += southWestLat.toString() + "," + southWestLng.toString() + "),("
                + northEastLat.toString() + "," + northEastLng.toString() + "))";
    }

    private void processResults(String webData) {
        mStores = new ArrayList<>();
        if (webData != null) {
            try {
                JSONArray locations = new JSONArray(webData);
                for (int i = 0; i < locations.length(); i++) {
                    JSONObject store = locations.getJSONObject(i);
                    double lat = store.getDouble("lat");
                    double lng = store.getDouble("lng");
                    mStores.add(new LatLng(lat, lng));
                }
            } catch (JSONException e) {
                //TODO handle this.
                Log.e(LOG_TAG, "Something went horribly wrong processing the JSON");
            }
        }
    }

    public class DownloadStoreData extends GetRawData {

        @Override
        protected String doInBackground(String... strings){
            return super.doInBackground(strings);
        }

        @Override
        protected void onPostExecute(String webData) {
            processResults(webData);
            MapsActivity.drawLocations(mStores);
        }
    }

}


