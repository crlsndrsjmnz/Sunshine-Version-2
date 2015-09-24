package com.example.android.sunshine.app.muzei;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;

/**
 * Created by carlosjimenez on 9/24/15.
 */
public class WeatherMuzeiSource extends MuzeiArtSource {

    private static final String[] MUZEI_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;

    private static final String LOG_TAG = WeatherMuzeiSource.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public WeatherMuzeiSource() {
        super(WeatherMuzeiSource.class.getName());
    }

    @Override
    protected void onUpdate(int reason) {

        int weatherId;
        String description, weatherArtResourceUrl;

        String locationSetting = Utility.getPreferredLocation(this);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis());

        Cursor data = getContentResolver().query(
                weatherForLocationUri,
                MUZEI_COLUMNS,
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
            description = data.getString(INDEX_SHORT_DESC);
            weatherArtResourceUrl = Utility.getImageUrlForWeatherCondition(weatherId);

            if (weatherArtResourceUrl != null && !weatherArtResourceUrl.isEmpty())
                publishArtwork(new Artwork.Builder()
                        .imageUri(Uri.parse(weatherArtResourceUrl))
                        .title(description)
                        .byline(locationSetting)
                        .viewIntent(new Intent(this, MainActivity.class))
                        .build());

        } finally {
            if (!data.isClosed())
                data.close();
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        if (intent != null &&
                SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction()) &&
                isEnabled()) {
            onUpdate(UPDATE_REASON_OTHER);
        }
    }


}
