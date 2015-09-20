package com.example.android.sunshine.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class ForecastWidgetProvider extends AppWidgetProvider {

    public static final String FORECAST_WIDGET_UPDATE = "com.example.android.sunshine.app.FORECAST_WIDGET_UPDATE";
    public static final String FORECAST_WIDGET_REFRESH = "com.example.android.sunshine.app.FORECAST_WIDGET_REFRESH";
    private static final String LOG_TAG = ForecastWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (appWidgetIds != null) {
            int N = appWidgetIds.length;
            if (N == 1) {
                appWidgetId = appWidgetIds[0];
            }
        }

        Intent intent = new Intent(context, ForecastWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction(FORECAST_WIDGET_UPDATE);
        context.startService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, ForecastWidgetProvider.class));

        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (appWidgetIds != null) {
            int N = appWidgetIds.length;
            if (N == 1) {
                appWidgetId = appWidgetIds[0];
            }
        }

        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Intent intentService = new Intent(context, ForecastWidgetService.class);
            intentService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intentService.setAction(FORECAST_WIDGET_REFRESH);
            context.startService(intentService);
        }
    }
}
