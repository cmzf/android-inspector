package com.github.cmzf.androidinspector;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUESR_SCREEN_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.setMainActivity(this);
        Global.setMainApplication(this.getApplication());

        Global.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                String ip = Utils.getWifiIpAddress();
                String message = MessageFormat.format((ip.equals("") ? "" : "http://{0}:{1}\n- or -\n") +
                        "adb forward tcp:{1} tcp:{1}\nhttp://localhost:{1}", ip, getServerPortView().getText());

                runOnUiThread(() -> {
                    getInspectorUrlView().setText(message);
                });

                Global.getMainHandler().postDelayed(this, 5000);
            }
        });
    }

    private TextView getInspectorUrlView() {
        return this.findViewById(R.id.inspectorUrl);
    }

    private TextView getServerPortView() {
        return this.findViewById(R.id.serverPort);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.inspectorUrl:
                copyInspectorUrl(view);
                break;
            case R.id.serverToggler:
                toggleInspectorServer(view);
                break;
            case R.id.checkVersion:
                forkMeOnGithub(view);
                break;
        }
    }

    private void forkMeOnGithub(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/cmzf/android-inspector")));
    }

    private void delayToggleButton(Button button, String text) {
        button.setEnabled(false);
        button.setBackgroundColor(Color.parseColor(text.toLowerCase().equals("start") ? "#33CC33" : "#CC3333"));
        button.getBackground().setAlpha(100);

        Global.getMainHandler().post(new Runnable() {
            private float delay = 3.0f;
            private float step = 0.1f;

            @Override
            public void run() {
                MainActivity.this.runOnUiThread(() -> {
                    button.setText(String.format("%.1f", delay));
                    delay -= step;
                    if (delay > 0) {
                        Global.getMainHandler().postDelayed(this, (long) (step * 1000 / 2)); // half for ui update
                    } else {
                        Global.getMainHandler().post(() -> runOnUiThread(() -> {
                            button.setEnabled(true);
                            button.setText(text);
                            button.getBackground().setAlpha(255);
                        }));
                    }
                });
            }
        });
    }

    private void toggleInspectorServer(View view) {
        Button button = (Button) view;
        TextView portView = getServerPortView();
        TextView urlView = getInspectorUrlView();

        if (button.getText().toString().toLowerCase().equals("start")) {
            int port;
            try {
                port = Integer.valueOf(String.valueOf(portView.getText()));
                if (port < 1024 || port > 65535) {
                    throw new Exception();
                }
            } catch (Exception e) {
                Toast.makeText(this, "port not allowed!", Toast.LENGTH_LONG).show();
                return;
            }

            portView.setEnabled(false);
            urlView.setVisibility(View.VISIBLE);

            Global.getInspectorServer().startServer(port);
            Global.getScreenCaptureService().startProjection(REQUESR_SCREEN_CAPTURE);

            delayToggleButton(button, "STOP");
        } else {
            portView.setEnabled(true);
            urlView.setVisibility(View.INVISIBLE);

            Global.getInspectorServer().stopServer();
            Global.getScreenCaptureService().stopProjection();

            delayToggleButton(button, "START");
        }
    }

    private void copyInspectorUrl(View view) {
        Utils.setClipBoard(getInspectorUrlView().getText());
        Toast.makeText(this, "copied!", Toast.LENGTH_LONG).show();
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
