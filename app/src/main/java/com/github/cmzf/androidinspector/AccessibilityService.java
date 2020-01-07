package com.github.cmzf.androidinspector;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String TAG = AccessibilityService.class.getCanonicalName();

    private static AccessibilityService instance;

    public static AccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        instance = this;
        Log.v(TAG, "onServiceConnected: " + getServiceInfo().toString());
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        instance = this;
        Log.v(TAG, "onAccessibilityEvent: " + event);
    }

    @Override
    public void onInterrupt() {

    }

}
