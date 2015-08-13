package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by jimeneca on 2015/08/12.
 */
public class WindDirectionView extends View {

    private final String LOG_TAG = WindDirectionView.class.getSimpleName();

    Paint mPaint;
    Bitmap mBitmap;
    String mWindSpeedDir;

    public WindDirectionView(Context context) {
        super(context);
        init();
    }

    public WindDirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindDirectionView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        init();
    }

    public void setWindSpeedDirection(String windSpeedDir) {
        this.mWindSpeedDir = windSpeedDir;
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(8f);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);

        mBitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);

        canvas.drawLine(5, 30, 30, 5, mPaint);
        canvas.drawLine(30, 5, 55, 30, mPaint);
        canvas.drawLine(30, 2, 30, 75, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        event.getText().add(mWindSpeedDir);
        return true;
    }
}
