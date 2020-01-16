package com.github.cmzf.androidinspector;

import android.app.Application;

public class Global {
    private static MainActivity mainActivity;
    private static Application mainApplication;

    public static Application getMainApplication() {
        return mainApplication;
    }

    public static void setMainApplication(Application mainApplication) {
        Global.mainApplication = mainApplication;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        Global.mainActivity = mainActivity;
    }

    public static AccessibilityService getAccessibilityService() {
        return AccessibilityService.getInstance();
    }

    public static ScreenCaptureService getScreenCaptureService() {
        return ScreenCaptureService.getInstance();
    }

    public static InspectorServer getInspectorServer() {
        return InspectorServer.getInstance();
    }
}
