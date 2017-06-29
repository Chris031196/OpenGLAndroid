package com.milieur.openglandroid.main;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milieur.openglandroid.R;
import com.milieur.openglandroid.graphics.Graphics;

import java.io.InputStream;


public class MainActivity extends Activity {

    private Graphics graphics;
    private TextView rpmView;
    private float rpm;
    private boolean running;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        running = true;
        graphics = new Graphics(this.getApplicationContext());
        graphics.init(this);
        this.setContentView(graphics);

        rpmView = new TextView(getApplicationContext());
        rpmView.setTextColor(Color.BLACK);
        rpmView.setBackgroundColor(Color.WHITE);
        rpmView.setText("0.0 RPM");
        this.addContentView(rpmView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running) {
                    updateRPM();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        super.onCreate(savedInstanceState);
    }

    public InputStream getVertexShaderCode() {
        return getResources().openRawResource(R.raw.vertex_shader);
    }

    public InputStream getFragmentShaderCode() {
        return getResources().openRawResource(R.raw.fragment_shader);
    }

    public void setRPM(final float rpm) {
        this.rpm = rpm;

    }

    private void updateRPM() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rpmView.setText(rpm + " RPM");
            }
        });
    }

    @Override
    public void onDestroy() {
        this.running = false;
        super.onDestroy();
    }
}
