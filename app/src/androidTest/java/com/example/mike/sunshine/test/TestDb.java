

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

        package com.example.mike.sunshine.test;
        import android.content.ContentValues;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.test.AndroidTestCase;
        import android.util.Log;

        import com.example.mike.sunshine.Data.WeatherContract.WeatherEntry;
        import com.example.mike.sunshine.Data.WeatherContract.LocationEntry;
        import com.example.mike.sunshine.Data.WeatherDbHelper;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }
    public void testInsertReadDb() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;
        String date = "20141205";
        double degrees = 1.1;
        double humidity = 1.2;
        double pressure = 1.3;
        int maxTemp = 75;
        int minTemp = 65;
        String Description = "Asteroids";
        double wind = 5.5;
        int weatherID = 321;


        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_LATITUDE, testLatitude);
        values.put(LocationEntry.COLUMN_LONGITUDE, testLongitude);


        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry.COLUMN_ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_LATITUDE,
                LocationEntry.COLUMN_LONGITUDE
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_LATITUDE));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_LONGITUDE));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);
        }else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, date);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, degrees);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, maxTemp);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, minTemp);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, Description);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, wind);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherID);


        locationRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Specify which columns you want.
        String[] columns2 = {

        WeatherEntry.COLUMN_LOC_KEY,
        WeatherEntry.COLUMN_DATETEXT,
        WeatherEntry.COLUMN_DEGREES,
        WeatherEntry.COLUMN_HUMIDITY,
        WeatherEntry.COLUMN_PRESSURE,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_WIND_SPEED,
        WeatherEntry.COLUMN_WEATHER_ID
        };


        Cursor cursor2 = db.query(
                WeatherEntry.TABLE_NAME,  // Table to Query
                columns2,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor2.moveToFirst()) {

            Integer locIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY);
            String loc = cursor.getString(locIndex);
            Log.v("locIndex", loc);

            Integer dateIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);

            String dateTest = cursor.getString(dateIndex);


            Integer degIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES);
            double degTest = cursor.getDouble(degIndex);
            Log.v("degIndex", degIndex.toString());

            int humIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            double humTest = cursor.getDouble(humIndex);

            int preIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            double preTest = cursor.getDouble(preIndex);

            int maxIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            int maxTest = cursor.getInt(maxIndex);

            int minIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            int minTest = cursor.getInt(minIndex);

            int desIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
            String desTest = cursor.getString(desIndex);

            int windIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            double windTest = cursor.getDouble(windIndex);

            int weatherIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
            int weatherTest = cursor.getInt(weatherIndex);

            assertEquals(loc, locationRowId);
            assertEquals(dateTest, date);
            assertEquals(degTest, degrees);
            assertEquals(humTest, humidity);
            assertEquals(preTest, pressure);
            assertEquals(maxTest, maxTemp);
            assertEquals(minTest, minTemp);
            assertEquals(desTest, Description);
            assertEquals(windTest, wind);
            assertEquals(weatherTest, weatherID);
            dbHelper.close();

        }else{
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }
    }
}