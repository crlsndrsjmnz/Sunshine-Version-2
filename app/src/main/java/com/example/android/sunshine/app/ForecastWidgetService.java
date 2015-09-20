package com.example.android.sunshine.app;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.data.WeatherContract;

public class ForecastWidgetService extends IntentService {

    static final int COL_WEATHER_ID = 0;
    static final int COL_MAX_TEMP = 1;

    private static final String LOG_TAG = ForecastWidgetService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ForecastWidgetService() {
        super(ForecastWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Create an Intent to launch ExampleActivity
            Intent widgetIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, widgetIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.forecast_widget);
            views.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);

            updateWidgetInfo(views);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    private void updateWidgetInfo(RemoteViews views) {
        String locationSetting = Utility.getPreferredLocation(this);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis());

        Cursor cursor = getContentResolver().query(
                weatherForLocationUri,
                new String[]{WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP},
                null,
                null,
                sortOrder);
        try {
            boolean isMetric = Utility.isMetric(this);

            if (cursor.moveToFirst()) {
                views.setImageViewResource(R.id.widget_item_icon, Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_ID)));
                views.setTextViewText(R.id.widget_high_textview, Utility.formatTemperature(this, cursor.getDouble(COL_MAX_TEMP), isMetric));
            } else {
                Log.e(LOG_TAG, "&&&&&&&&&&&&&&&&&&&&&& CURSOR EMPTY!!");
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
    }

}
