package com.milieur.openglandroid.graphics;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.milieur.openglandroid.main.MainActivity;


public class Graphics extends GLSurfaceView {

    private Activity activity;

    public Graphics(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }

    public void init(Activity activity) {
        this.activity = activity;
        this.setRenderer(new GraphicsRenderer(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d("TOUCH EVENT", "THE SCREEN WAS TOUCHED");
        return true;
    }

    public void initFailed(){
        activity.finish();
    }

    public MainActivity getActivity() {
        return (MainActivity) activity;
    }

}
