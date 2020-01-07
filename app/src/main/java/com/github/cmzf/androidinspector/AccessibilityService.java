package com.github.cmzf.androidinspector;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String TAG = AccessibilityService.class.getCanonicalName();

    private static AccessibilityService instance;
    private volatile AccessibilityNodeInfo eventRootInActiveWindow;

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

        new Thread(() -> {
            AccessibilityNodeInfo eventRootNode = super.getRootInActiveWindow();
            if (eventRootNode != null) {
                eventRootInActiveWindow = eventRootNode;
            }
        }).start();
    }

    public String getCurrentPackage() {
        AccessibilityNodeInfo root = getRootUiObject();
        return root != null ? String.valueOf(root.getPackageName()) : "";
    }

    @Override
    public void onInterrupt() {

    }

    public AccessibilityNodeInfo getRootUiObject() {
        AccessibilityNodeInfo root = eventRootInActiveWindow;
        if (root == null) {
            root = super.getRootInActiveWindow();
        }
        return root;
    }

}
