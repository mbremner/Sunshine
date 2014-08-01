package com.example.mike.sunshine;

/**
 * Created by Mike on 17/07/2014.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * A placeholder fragment containing a simple view.
 */
public class HistoryFragment extends Fragment {

    LinkedList<String> weather = new LinkedList<String>();

    public HistoryFragment() {
    }

    LinkedList<FetchWeatherTask> taskList = new LinkedList<FetchWeatherTask>();




    private ArrayAdapter<String> adapter;

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        inflater.inflate(R.menu.forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_show){

            showData();
            return true;
        }
        if (id == R.id.action_view_location){
            //showLocation();
        }

        return super.onOptionsItemSelected(item);
    }

    public void getData(){

        int startTime = 1402491600;
        for (int k = 0; k < 30; k++) {

            int morningStartTime = startTime + (k * 86400);
            int morningEndTime = morningStartTime + 3600;
            Integer useStartTime = 0;
            Integer useEndTime = 0;


            for(int l = 0 ; l < 2 ; l++){
                taskList.add(new FetchWeatherTask());
                if (l == 1){
                    useStartTime = morningStartTime + 18000;
                    useEndTime = morningEndTime + 18000;
                }else{
                    useStartTime = morningStartTime ;
                    useEndTime = morningEndTime ;
                }
                taskList.getLast().execute(useStartTime.toString(), useEndTime.toString());

            }
        }


    }


    public void showData(){
        if (weather != null){
            adapter.clear();
            for (int i = 0 ; i < weather.size() ; i++){
                adapter.add(weather.get(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ArrayList<String> formattedHistory = new ArrayList(60);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, formattedHistory);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapter.getItem( i );
                //Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                Intent openDetail = new Intent(getActivity() , DetailActivity.class );
                openDetail.putExtra(Intent.EXTRA_TEXT , text );
                startActivity(openDetail);
            }
        });
        return rootView;

    }

    /*
    public void showLocation(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lat = sharedPreferences.getString(getString(R.string.pref_lat_key ) , getString(R.string.pref_lat_default));
        String lon = sharedPreferences.getString(getString(R.string.pref_lon_key ) ,  getString(R.string.pref_lon_default));
        String text =  ("geo:" +  lat + "," + lon + "?q=" + lat + "," + lon + "(Weather)"   );
        //Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        Uri out = Uri.parse(text);
        //Toast.makeText(getActivity(), out.toString(), Toast.LENGTH_SHORT).show();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, out);
        //mapIntent.setData(out);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
    */

    public class FetchWeatherTask extends AsyncTask <String, Void ,String[]> {

        @Override
        protected void onPostExecute(String[] weathers){


            //fetch = new FetchWeatherTask();
            weather.add(weathers[0]) ;
        }



        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d h:m");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double temp, double wind) {
            // For presentation, assume the user doesn't care about tenths of a degree.

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
            long roundedHigh = 0;
            long roundedLow = 0; /*
                if (unit.contentEquals("3")){
                    roundedHigh = Math.round(high + 273.15 );
                    roundedLow = Math.round(low + 273.15);
                }else if (unit.contentEquals("2") ){
                    roundedHigh = Math.round(high * (9/5) + 32 );
                    roundedLow = Math.round(low * (9/5) + 32 );
                }else{*/
            roundedHigh = Math.round(temp - 273.15);
            roundedLow = Math.round(wind * 3.6);
            //}


            String highLowStr = (   roundedHigh + "°c  Wind: " + roundedLow + "km/h");
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String historyJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAIN = "main";
            final String OWM_WIND = "wind";
            final String OWM_WIND_SPEED = "speed";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject historyJson = new JSONObject(historyJsonStr);
            JSONArray weatherArray = historyJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[weatherArray.length()];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayHistory = weatherArray.getJSONObject(i);
                //Log.v("tag","1");

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayHistory.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);
                // Log.v("tag","2");
                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayHistory.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                // Log.v("tag","3");
                JSONObject windObject = dayHistory.getJSONObject(OWM_WIND);
                double wind = windObject.getDouble(OWM_WIND_SPEED);
                // Log.v("tag","4");
                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayHistory.getJSONObject(OWM_MAIN);
                double high = temperatureObject.getDouble(OWM_TEMPERATURE);
                // Log.v("tag","5");

                highAndLow = formatHighLows(high , wind);
                resultStrs[i] = day + "  " + description + " " + highAndLow;
                // Log.v("TAG", resultStrs[i] );
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground( String... time ) {


            String[] result = null;


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String historyJsonStr = null ;


            try

            {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                Uri.Builder builder = new Uri.Builder();


                builder.scheme("http");
                builder.authority("api.openweathermap.org");
                builder.appendPath("data");
                builder.appendPath("2.5");
                builder.appendPath("history");
                builder.appendPath("city");
                builder.appendQueryParameter("q", "fredericton");
                builder.appendQueryParameter("start", time[0]);
                builder.appendQueryParameter("end", time[1]);
                builder.appendQueryParameter("mode", "json");
                builder.appendQueryParameter("units", "metric");
                // builder.appendQueryParameter( "cnt" , "1");
                Uri uri = builder.build();

                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    historyJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    historyJsonStr = null;
                }
                historyJsonStr = buffer.toString();


            } catch (
                    IOException e
                    )

            {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                historyJsonStr = null;
            } finally

            {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {

                result = this.getWeatherDataFromJson(historyJsonStr);

            } catch (JSONException j) {
                //Log.v("TAG", "Bad");

            }





            return result;
        }
    }

}


