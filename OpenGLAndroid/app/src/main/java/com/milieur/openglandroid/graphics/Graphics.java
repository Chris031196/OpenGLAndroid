package com.milieur.openglandroid.graphics;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Chris on 24.06.2017.
 */

public class Graphics extends GLSurfaceView {

    public Graphics(Context context) {
        super(context);
        this.setRenderer(new GraphicsRenderer(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        return true;
    }

}
