package com.github.cmzf.androidinspector;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

public class Global {
    private static MainActivity mainActivity;
    private static Application mainApplication;
    private static Handler mainHandler;
    private static HandlerThread mainHandlerThread;

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

    public static Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(getMainHandlerThread().getLooper());
        }
        return mainHandler;
    }

    public static HandlerThread getMainHandlerThread() {
        if (mainHandlerThread == null) {
            mainHandlerThread = new HandlerThread("mainHandlerThread");
            mainHandlerThread.start();
        }
        return mainHandlerThread;
    }
}
