package com.milieur.openglandroid.graphics;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.milieur.openglandroid.main.MainActivity;


public class Graphics extends GLSurfaceView {

    private Activity activity;

    private float lastX;
    private float lastY;

    private GraphicsRenderer renderer;

    public Graphics(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }

    public void init(Activity activity) {
        this.activity = activity;
        this.renderer = new GraphicsRenderer(this);
        this.setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = e.getX();
                lastY = e.getY();
                renderer.setResistance(0.1f);
                break;

            case MotionEvent.ACTION_MOVE:
                float rotX = lastX - e.getX();
                float rotY = lastY - e.getY();

                renderer.setResistance(0.1f);
                renderer.setRotation(rotX, rotY);
                lastX = e.getX();
                lastY = e.getY();
                break;

            case MotionEvent.ACTION_UP:
                rotX = lastX - e.getX();
                rotY = lastY - e.getY();
                renderer.setResistance(0.001f);
                renderer.setRotation(rotX, rotY);
                lastX = e.getX();
                lastY = e.getY();
        }
        return true;
    }

    public void initFailed(){
        activity.finish();
    }

    public MainActivity getActivity() {
        return (MainActivity) activity;
    }

}
