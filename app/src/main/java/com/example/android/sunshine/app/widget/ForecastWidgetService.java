package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

public class ForecastWidgetService extends IntentService {

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

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ForecastWidgetService() {
        super(ForecastWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int weatherId, weatherArtResourceId, widgetWidth;
        boolean isMetric;
        String description, formattedMaxTemperature, formattedMinTemperature;
        double maxTemp, minTemp;

        String locationSetting = Utility.getPreferredLocation(this);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis());

        Cursor data = getContentResolver().query(
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        if (data == null)
            return;

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        try {
            weatherId = data.getInt(INDEX_WEATHER_ID);
            isMetric = Utility.isMetric(this);
            weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
            description = data.getString(INDEX_SHORT_DESC);
            maxTemp = data.getDouble(INDEX_MAX_TEMP);
            minTemp = data.getDouble(INDEX_MIN_TEMP);
            formattedMaxTemperature = Utility.formatTemperature(this, maxTemp, isMetric);
            formattedMinTemperature = Utility.formatTemperature(this, minTemp, isMetric);
        } finally {
            if (!data.isClosed())
                data.close();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ForecastWidgetProvider.class));

        for (int widgetId : appWidgetIds) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                widgetWidth = getWidgetWidthFromOptions(appWidgetManager, widgetId);
            } else {
                widgetWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            }

            // Create an Intent to launch ExampleActivity
            Intent widgetIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, widgetIntent, 0);

            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;

            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.forecast_widget_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.forecast_widget;
            } else {
                layoutId = R.layout.forecast_widget_small;
            }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

            views.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);

            views.setImageViewResource(R.id.widget_item_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }

            views.setTextViewText(R.id.widget_high_textview, formattedMaxTemperature);
            views.setTextViewText(R.id.widget_low_textview, formattedMinTemperature);
            views.setTextViewText(R.id.widget_description_textview, description);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_item_icon, description);
    }
}
