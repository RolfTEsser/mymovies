package de.floresse.mymovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;


public class StringPreference extends Preference { 
	
    public interface OnStringPreferenceClickedListener {
        void onStringPreferenceClicked();
    }
    
    public void setOnStringPreferenceClickedListener(OnStringPreferenceClickedListener listener) {
        if (listener != null) {
            mOnStringPreferenceClickedListener = listener;
        }
    }
    
    private OnStringPreferenceClickedListener mOnStringPreferenceClickedListener = null;
    
    @SuppressWarnings("unused")
	private static final String TAG = "StringPreference";

    public StringPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StringPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public StringPreference(Context context) {
        this(context, null);
    }

    /**
     * use this function to set the new String.
     * 
     * @param String The new Value to store in the SharedPreferences.
     */
    public void saveString(String newValue) {
        if (callChangeListener(newValue != null ? newValue : "")) {
            onSaveString(newValue);
        }
    }
    
    @Override
    protected void onClick() {
        if (mOnStringPreferenceClickedListener != null) {
            mOnStringPreferenceClickedListener.onStringPreferenceClicked();
        }        
    }

    protected void onSaveString(String newValue) {
        persistString(newValue);
    }

    protected String onRestoreString() {
        final String oldValue = getPersistedString(null);
        return !TextUtils.isEmpty(oldValue) ? oldValue : null;
    }
    
    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        
        /*
         * This method is normally to make sure the internal state and UI
         * matches either the persisted value or the default value. Since we
         * don't show the current value in the UI (until the dialog is opened)
         * and we don't keep local state, if we are restoring the persisted
         * value we don't need to do anything.
         */
        if (restorePersistedValue) {
            return;
        }
        
        // If we are setting to the default value, we should persist it.
        if (!TextUtils.isEmpty(defaultValue)) {
            onSaveString(defaultValue);
        }
    }

}
