package com.github.cmzf.androidinspector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InspectorServer.getInstance().start();
        AccessibilityService.setAppContext(this);
    }
}
