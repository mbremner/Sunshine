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
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {


    public ForecastFragment() {
    }

    private FetchWeatherTask fetch = new FetchWeatherTask() ;
    public String[] forecasts;
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
        if (id == R.id.action_refresh){

            getData();
            return true;
        }
        if (id == R.id.action_view_location){
            showLocation();
        }
        if (id == R.id.action_history) {
            Intent openSettings = new Intent(getActivity() , HistoryActivity.class );
            startActivity(openSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getData(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lat = sharedPreferences.getString(getString(R.string.pref_lat_key ) , getString(R.string.pref_lat_default));
        String lon = sharedPreferences.getString(getString(R.string.pref_lon_key ) ,  getString(R.string.pref_lon_default));
        fetch.execute(lat , lon );
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ArrayList<String> formatedForecasts = new ArrayList(7);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, formatedForecasts);
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

        public class FetchWeatherTask extends AsyncTask < String , Void , String[]> {

            @Override
            protected void onPostExecute(String[] forecastsIn){
                if (forecastsIn != null){
                    adapter.clear();
                    for (int i = 0 ; i < forecastsIn.length ; i++){
                        adapter.add(forecastsIn[i]);
                    }
                }
                fetch = new FetchWeatherTask();

            }



            private String getReadableDateString(long time){
                // Because the API returns a unix timestamp (measured in seconds),
                // it must be converted to milliseconds in order to be converted to valid date.
                Date date = new Date(time * 1000);
                SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
                return format.format(date).toString();
            }

            /**
             * Prepare the weather high/lows for presentation.
             */
            private String formatHighLows(double high, double low) {
                // For presentation, assume the user doesn't care about tenths of a degree.

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String unit = sharedPreferences.getString(getString(R.string.pref_unit_key ) , getString(R.string.pref_unit_default));
                long roundedHigh = 0;
                long roundedLow = 0;
                if (unit.contentEquals("3")){
                    roundedHigh = Math.round(high + 273.15 );
                    roundedLow = Math.round(low + 273.15);
                }else if (unit.contentEquals("2") ){
                    roundedHigh = Math.round(high * (9/5) + 32 );
                    roundedLow = Math.round(low * (9/5) + 32 );
                }else{
                    roundedHigh = Math.round(high);
                    roundedLow = Math.round(low);
                }


                String highLowStr = (roundedHigh + "/" + roundedLow);
                return highLowStr;
            }

            /**
             * Take the String representing the complete forecast in JSON Format and
             * pull out the data we need to construct the Strings needed for the wireframes.
             *
             * Fortunately parsing is easy:  constructor takes the JSON string and converts it
             * into an Object hierarchy for us.
             */
            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String OWM_LIST = "list";
                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DATETIME = "dt";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                String[] resultStrs = new String[numDays];
                for(int i = 0; i < weatherArray.length(); i++) {
                    // For now, using the format "Day, description, hi/low"
                    String day;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherArray.getJSONObject(i);

                    // The date/time is returned as a long.  We need to convert that
                    // into something human-readable, since most people won't read "1400356800" as
                    // "this saturday".
                    long dateTime = dayForecast.getLong(OWM_DATETIME);
                    day = getReadableDateString(dateTime);

                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }

                return resultStrs;
            }

            @Override
            protected String[] doInBackground( String... location ) {

                String[] forecasts = null;
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String forecastJsonStr = null;

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
                    builder.appendPath("forecast");
                    builder.appendPath("daily");
                    builder.appendQueryParameter("lat", location[0]);
                    builder.appendQueryParameter("lon", location[1]);
                    builder.appendQueryParameter("mode", "json");
                    builder.appendQueryParameter( "units" , "metric" );
                    builder.appendQueryParameter( "cnt" , "7");
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
                        forecastJsonStr = null;
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
                        forecastJsonStr = null;
                    }
                    forecastJsonStr = buffer.toString();


                } catch (
                        IOException e
                        )

                {
                    Log.e("PlaceholderFragment", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    forecastJsonStr = null;
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

                try{

                  forecasts = this.getWeatherDataFromJson(forecastJsonStr, 7);

                }catch (JSONException j ){

                }


                return forecasts;
            }
        }

}


