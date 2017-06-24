package com.milieur.openglandroid.main;

import android.app.Activity;
import android.os.Bundle;

import com.milieur.openglandroid.graphics.Graphics;

/**
 * Created by Chris on 24.06.2017.
 */

public class MainActivity extends Activity {

    private Graphics graphics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        graphics = new Graphics(this.getApplicationContext());
        this.setContentView(graphics);
    }
}
