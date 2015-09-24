package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.android.sunshine.app.DetailFragment;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 9/22/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

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
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_DATE
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;
    private static final int INDEX_DATE = 4;

    private static final String LOG_TAG = DetailWidgetRemoteViewsFactory.class.getSimpleName();
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

        if (mCursor != null) {
            mCursor.close();
        }

        String locationSetting = Utility.getPreferredLocation(mContext);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        final long token = Binder.clearCallingIdentity();
        try {
            mCursor = mContext.getContentResolver().query(
                    weatherForLocationUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
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

        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        int weatherId, weatherArtResourceId, widgetWidth;
        boolean isMetric;
        String description, formattedMaxTemperature, formattedMinTemperature, formattedDate;
        double maxTemp, minTemp;
        long date;

        weatherId = mCursor.getInt(INDEX_WEATHER_ID);
        isMetric = Utility.isMetric(mContext);


        weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        Bitmap weatherIconBitmap = null;
        if (!Utility.usingLocalGraphics(mContext)) {
            String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
                    mContext, weatherId);
            try {
                weatherIconBitmap = Glide.with(mContext)
                        .load(weatherArtResourceUrl)
                        .asBitmap()
                        .error(weatherArtResourceId)
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(LOG_TAG, "Error retrieving large icon from " + weatherArtResourceUrl, e);
            }
        }

        description = mCursor.getString(INDEX_SHORT_DESC);
        maxTemp = mCursor.getDouble(INDEX_MAX_TEMP);
        minTemp = mCursor.getDouble(INDEX_MIN_TEMP);
        formattedMaxTemperature = Utility.formatTemperature(mContext, maxTemp, isMetric);
        formattedMinTemperature = Utility.formatTemperature(mContext, minTemp, isMetric);
        date = mCursor.getLong(INDEX_DATE);
        formattedDate = Utility.getFriendlyDayString(mContext, date, true);

        String locationSetting = Utility.getPreferredLocation(mContext);

        // Create an Intent to launch ExampleActivity
        final Intent widgetIntent = new Intent();

        Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, date);

        Bundle args = new Bundle();
        args.putParcelable(DetailFragment.FORECAST_URI, dateUri);
        widgetIntent.putExtras(args);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list);

        //views.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);
        views.setOnClickFillInIntent(R.id.widget_frame, widgetIntent);

        if (weatherIconBitmap != null)
            views.setImageViewBitmap(R.id.widget_item_icon, weatherIconBitmap);
        else
            views.setImageViewResource(R.id.widget_item_icon, weatherArtResourceId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views, description);
        }

        views.setTextViewText(R.id.widget_high_textview, formattedMaxTemperature);
        views.setTextViewText(R.id.widget_low_textview, formattedMinTemperature);
        views.setTextViewText(R.id.widget_description_textview, description);
        views.setTextViewText(R.id.widget_date_textview, formattedDate);

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
        if (mCursor.moveToPosition(position))
            return mCursor.getLong(INDEX_WEATHER_ID);
        return position;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_item_icon, description);
    }
}
