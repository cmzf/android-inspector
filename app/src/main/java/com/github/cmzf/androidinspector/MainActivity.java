package com.github.cmzf.androidinspector;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUESR_SCREEN_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.setMainActivity(this);
        Global.setMainApplication(this.getApplication());

        Global.getInspectorServer().startServer(8080);
        Global.getScreenCaptureService().startProjection(REQUESR_SCREEN_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Global.getScreenCaptureService().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Global.getScreenCaptureService().stopProjection();
    }
}
