package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mSinglePaneLayout;
    private Context mContext;
    private Cursor mCursor;
    private TextView mEmptyView;
    private ItemClickListener mItemClickListener;

    public ForecastAdapter(Context context, ItemClickListener itemClickListener, TextView emptyView) {
        mContext = context;
        mEmptyView = emptyView;
        mItemClickListener = itemClickListener;
    }

    public void setSinglePaneLayout(boolean singlePaneLayout) {
        this.mSinglePaneLayout = singlePaneLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mSinglePaneLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public Cursor getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public boolean isEmpty() {
        return getItemCount() > 0 ? false : true;
    }

    public void swapCursor(Cursor c) {
        mCursor = c;
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        notifyDataSetChanged();
    }

    @Override
    public ForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Choose the layout type
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            // TODO: Determine layoutId from viewType
            if (viewType == VIEW_TYPE_TODAY)
                layoutId = R.layout.list_item_forecast_today;
            else
                layoutId = R.layout.list_item_forecast;

            View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);

            return new ViewHolder(view, mItemClickListener);

        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        mCursor.moveToPosition(position);

        int viewType = getItemViewType(position);

        // Read weather icon ID from cursor
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        // Use placeholder image for now

        int defaultImage = 0;
        if (viewType == VIEW_TYPE_TODAY)
            defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
        else
            defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);

        if (Utility.usingLocalGraphics(mContext))
            viewHolder.mIconView.setImageResource(defaultImage);
        else
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .into(viewHolder.mIconView);

        String weatherDescription = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.mIconView.setContentDescription(mContext.getString(
                R.string.a11y_forecast_icon,
                weatherDescription));

        viewHolder.mDateView.setText(Utility.getFriendlyDayString(mContext, mCursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        viewHolder.mDate = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        viewHolder.mDescriptionView.setText(weatherDescription);
        viewHolder.mDescriptionView.setContentDescription(mContext.getString(
                R.string.a11y_forecast,
                weatherDescription));

        boolean isMetric = Utility.isMetric(mContext);
        String metricDesc = isMetric ? " Celsius" : " Fahrenheit";

        String tvHighText = Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
        viewHolder.mHighTempView.setText(tvHighText);
        viewHolder.mHighTempView.setContentDescription(mContext.getString(
                R.string.a11y_high_temp,
                tvHighText,
                metricDesc));

        String tvLowText = Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);
        viewHolder.mLowTempView.setText(Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
        viewHolder.mLowTempView.setContentDescription(mContext.getString(
                R.string.a11y_low_temp,
                tvLowText,
                metricDesc));
    }

    public interface ItemClickListener {
        void onClick(long date, ViewHolder viewHolder);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;
        public long mDate;
        private ItemClickListener mListener;

        public ViewHolder(View view, ItemClickListener listener) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            mListener = listener;
            mDate = 0;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(mDate, this);
        }
    }
}