package com.example.athaman.barzahlen;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by keone on 8/3/2016.
 */
public class GetRawData extends AsyncTask<String, Void, String> {
    public static final String LOG_TAG = "GetRawData";
    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //If the input is errored somehow return null to avoid breaking a http connection
        if (strings == null) {
            return null;
        }

        //convert the URL String into a URL, make a HTTP connection and connect it then get an
        //input stream. If the stream isn't null create a buffer and a reader to parse the input stream.
        try {
            URL url = new URL(strings[0]);
            Log.e(LOG_TAG, "Trying to connect to " + strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();
            if (is == null) {
                return null;
            }

            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            return buffer.toString();


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error " + e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error Closing stream", e);
                }
            }
        }
        return null;
    }

}
