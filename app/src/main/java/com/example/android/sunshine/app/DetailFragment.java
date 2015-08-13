package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by carlosjimenez on 8/3/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FORECAST_URI = "URI";
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_WEATHER_SHORT_DESC = 9;
    static final int COL_WEATHER_HUMIDITY = 10;
    static final int COL_WEATHER_PRESSURE = 11;
    static final int COL_WEATHER_WIND_SPEED = 12;
    static final int COL_WEATHER_WIND_DEGREES = 13;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int WEATHER_LOADER_ID = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES

    };
    Uri mUri;
    TextView mTvDateToday;
    TextView mTvDate;
    TextView mTvHigh;
    TextView mTvLow;
    TextView mForecastDescription;
    TextView mTvHumidity;
    TextView mTvWind;
    TextView mTvPressure;
    ImageView mIcon;
    WindDirectionView mWindDirectionView;

    ShareActionProvider mShareActionProvider;
    private String mForecastStr;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri) {
        DetailFragment f = new DetailFragment();

        Bundle args = new Bundle();
        args.putParcelable(FORECAST_URI, uri);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mUri = savedInstanceState.getParcelable(FORECAST_URI);
            getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mUri = arguments.getParcelable(FORECAST_URI);
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        //WindDirectionView windView = new WindDirectionView(getActivity());

        mTvDateToday = ((TextView) rootView.findViewById(R.id.list_item_date_description));
        mTvDate = ((TextView) rootView.findViewById(R.id.list_item_date));
        mTvHigh = ((TextView) rootView.findViewById(R.id.list_item_high_textview));
        mTvLow = ((TextView) rootView.findViewById(R.id.list_item_low_textview));
        mForecastDescription = ((TextView) rootView.findViewById(R.id.list_item_forecast_textview));
        mTvHumidity = ((TextView) rootView.findViewById(R.id.list_item_humidity));
        mTvWind = ((TextView) rootView.findViewById(R.id.list_item_wind));
        mTvPressure = ((TextView) rootView.findViewById(R.id.list_item_pressure));
        mWindDirectionView = (WindDirectionView) rootView.findViewById(R.id.list_item_wind_icon);

        mIcon = ((ImageView) rootView.findViewById(R.id.list_item_icon));

        //getActivity().setContentView(mCustomDrawableView);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FORECAST_URI, mUri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.

        if (mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null);
        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //mForecastStr = convertCursorRowToUXFormat(data);
        Context context = getActivity();

        if (data != null && data.moveToNext()) {
            long date = data.getLong(COL_WEATHER_DATE);
            mTvDateToday.setText(Utility.getDayName(context, date));
            mTvDate.setText(Utility.getFormattedMonthDay(context, date));

            boolean isMetric = Utility.isMetric(context);
            String metricDesc = isMetric ? " Celsius" : " Fahrenheit";

            String tvHighText = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            mTvHigh.setText(tvHighText);
            mTvHigh.setContentDescription(tvHighText + metricDesc);

            String tvLowText = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mTvLow.setText(tvLowText);
            mTvLow.setContentDescription(tvLowText + metricDesc);

            mForecastDescription.setText(data.getString(COL_WEATHER_SHORT_DESC));

            mTvHumidity.setText(
                    context.getString(
                            R.string.format_humidity,
                            data.getDouble(COL_WEATHER_HUMIDITY)));

            float windDegrees = data.getFloat(COL_WEATHER_WIND_DEGREES);
            mTvWind.setText(
                    Utility.getFormattedWind(
                            context,
                            data.getFloat(COL_WEATHER_WIND_SPEED),
                            windDegrees));

            if (Build.VERSION.SDK_INT < 11) {
                RotateAnimation animation = new RotateAnimation(0, windDegrees);
                animation.setDuration(100);
                animation.setFillAfter(true);
                mWindDirectionView.startAnimation(animation);
            } else {
                mWindDirectionView.setRotation(windDegrees);
            }

            mTvPressure.setText(
                    context.getString(
                            R.string.format_pressure,
                            data.getDouble(COL_WEATHER_PRESSURE)));

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            mIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            mIcon.setContentDescription(data.getString(COL_WEATHER_SHORT_DESC) + " icon");
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
        }
    }
}
