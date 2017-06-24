package com.milieur.openglandroid.main;

import android.app.Activity;
import android.os.Bundle;

import com.milieur.openglandroid.R;
import com.milieur.openglandroid.graphics.Graphics;

import java.io.InputStream;


public class MainActivity extends Activity {

    private Graphics graphics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        graphics = new Graphics(this.getApplicationContext());
        graphics.init(this);
        this.setContentView(graphics);
        super.onCreate(savedInstanceState);
    }

    public InputStream getVertexShaderCode() {
        return getResources().openRawResource(R.raw.vertex_shader);
    }

    public InputStream getFragmentShaderCode() {
        return getResources().openRawResource(R.raw.fragment_shader);
    }
}
