package com.github.cmzf.androidinspector;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

class Utils {

    private static WifiInfo getWifiInfo() {
        WifiManager wifiManager = (WifiManager) Global.getMainActivity().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    public static String getWifiIpAddress() {
        final int ip = getWifiInfo().getIpAddress();
        if (ip == 0) {
            return "";
        }
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    public static void setClipBoard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) Global.getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(Global.getMainApplication().getPackageName(), text);
        clipboard.setPrimaryClip(clip);
    }

    public static void ensureAccessibilityServiceEnabled(Runnable enterCallback, Runnable exitCallback) {
        long checkInterval = 2000;
        String appName = Global.getMainActivity().getString(R.string.app_name);

        final Runnable exitWrapper = new Runnable() {
            @Override
            public void run() {
                if (Global.getAccessibilityService() == null) {
                    Global.toast("accessibility disconnected");
                    exitCallback.run();
                } else {
                    Global.getMainHandler().postDelayed(this, checkInterval);
                }
            }
        };
        final Runnable enterWrapper = new Runnable() {
            @Override
            public void run() {
                if (Global.getAccessibilityService() != null) {
                    Global.toast("accessibility connected");
                    enterCallback.run();
                    Global.getMainHandler().postDelayed(exitWrapper, checkInterval);
                } else {
                    Global.getMainHandler().postDelayed(this, checkInterval);
                    Global.toast("turn on accessibility for " + appName, Toast.LENGTH_SHORT);
                }
            }
        };

        long enterDelay = 0;
        if (Global.getAccessibilityService() == null) {
            Global.toast("need accessibility service");
            Global.getMainHandler().postDelayed(Utils::gotoAccessibilityService, 2000);
            enterDelay = 3000;
        }
        Global.getMainHandler().postDelayed(enterWrapper, enterDelay);
    }

    private static void gotoAccessibilityService() {
        final Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(Global.getMainActivity(), 0, intent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

}
