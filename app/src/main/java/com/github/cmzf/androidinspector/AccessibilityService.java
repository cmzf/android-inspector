package com.github.cmzf.androidinspector;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Objects;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String TAG = AccessibilityService.class.getCanonicalName();

    private static AccessibilityService instance;
    private static Context appContext;
    private volatile AccessibilityNodeInfo eventRootInActiveWindow;
    private volatile String currentActivity = "";

    public static void setAppContext(Context context) {
        appContext = context;
    }

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

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            setCurrentActivity(Objects.toString(event.getPackageName(), ""),
                    Objects.toString(event.getClassName(), ""));
        }

        new Thread(() -> {
            AccessibilityNodeInfo eventRootNode = super.getRootInActiveWindow();
            if (eventRootNode != null) {
                eventRootInActiveWindow = eventRootNode;
            }
        }).start();
    }

    private void setCurrentActivity(String pkgName, String clsName) {
        if (appContext == null || clsName.startsWith("android.view.") || clsName.startsWith("android.widget.")) {
            return;
        }
        try {
            ComponentName componentName = new ComponentName(pkgName, clsName);
            currentActivity = appContext.getPackageManager().getActivityInfo(componentName, 0).name;
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
    }

    public String getCurrentPackage() {
        UiObject root = getRootUiObject();
        return root != null ? root.getPkg() : "";
    }

    public String getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onInterrupt() {

    }

    public UiObject getRootUiObject() {
        AccessibilityNodeInfo root = eventRootInActiveWindow;
        if (root == null) {
            root = super.getRootInActiveWindow();
        }
        return UiObject.wrap(root);
    }

}
