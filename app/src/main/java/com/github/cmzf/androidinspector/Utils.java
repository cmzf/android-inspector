package com.github.cmzf.androidinspector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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
}
