package com.example.android.sunshine.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by Carlos on 9/22/2015.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DetailWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class DetailWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;
    private static final String LOG_TAG = ForecastWidgetService.class.getSimpleName();
    private Context mContext;
    private int mAppWidgetId;
    private Cursor mCursor;

    public DetailWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        String locationSetting = Utility.getPreferredLocation(mContext);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis());

        mCursor = mContext.getContentResolver().query(
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        int weatherId, weatherArtResourceId, widgetWidth;
        boolean isMetric;
        String description, formattedMaxTemperature, formattedMinTemperature;
        double maxTemp, minTemp;

        if (mCursor.moveToPosition(position)) {
            weatherId = mCursor.getInt(INDEX_WEATHER_ID);
            isMetric = Utility.isMetric(mContext);
            weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
            description = mCursor.getString(INDEX_SHORT_DESC);
            maxTemp = mCursor.getDouble(INDEX_MAX_TEMP);
            minTemp = mCursor.getDouble(INDEX_MIN_TEMP);
            formattedMaxTemperature = Utility.formatTemperature(mContext, maxTemp, isMetric);
            formattedMinTemperature = Utility.formatTemperature(mContext, minTemp, isMetric);
        } else {
            return null;
        }

        // Create an Intent to launch ExampleActivity
        Intent widgetIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, widgetIntent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(mContext.getPackageName(), layoutId);

        views.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);

        views.setImageViewResource(R.id.widget_item_icon, weatherArtResourceId);
        views.setTextViewText(R.id.widget_high_textview, formattedMaxTemperature);
        views.setTextViewText(R.id.widget_low_textview, formattedMinTemperature);
        views.setTextViewText(R.id.widget_description_textview, description);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
