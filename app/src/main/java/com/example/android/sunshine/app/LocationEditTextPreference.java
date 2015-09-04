package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class LocationEditTextPreference extends EditTextPreference implements TextWatcher {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    EditText mEditText;
    private int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference, 0, 0);

        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);

            Log.d(LOG_TAG, "################################# mMinLength: " + mMinLength);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        mEditText = getEditText();
        mEditText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        Dialog d = getDialog();

        if (d instanceof AlertDialog) {
            AlertDialog alertDialog = (AlertDialog) d;
            Button btOkButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (s.length() >= mMinLength)
                btOkButton.setEnabled(true);
            else
                btOkButton.setEnabled(false);
        }
    }

}
