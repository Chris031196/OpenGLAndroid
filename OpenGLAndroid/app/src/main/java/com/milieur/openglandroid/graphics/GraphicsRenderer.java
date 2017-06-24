package com.milieur.openglandroid.graphics;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chris on 24.06.2017.
 */

public class GraphicsRenderer implements GLSurfaceView.Renderer {

    private int program;
    private Graphics graphics;

    public GraphicsRenderer(Graphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20 gl2 = (GLES20) gl;
        gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl2.glEnable(GL10.GL_DEPTH_TEST);
        gl2.glDepthFunc(GL10.GL_LESS);
        gl2.glEnable(GL10.GL_CULL_FACE);

        if(!initShaders()){

        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
        gl.glLoadIdentity();                        // reset the matrix to its default state
        gl.glFrustumf(-ratio, ratio, -1, 1, 2, 10);  // apply the projection matrix
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    public boolean initShaders() {
        String vertexShaderCode = "";
        String fragmentxShaderCode = "";
        try {
            //read vertex shader code
            BufferedReader reader = new BufferedReader(new FileReader("vertexShader.vs"));
            String line = "";
            while((line = reader.readLine()) != null) {
                vertexShaderCode += line;
            }

            //read fragment shader code
            reader = new BufferedReader(new FileReader("fragmentShader.fs"));
            line = "";
            while((line = reader.readLine()) != null) {
                fragmentxShaderCode += line;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }


        if(vertexShaderCode.length() > 1 && fragmentxShaderCode.length() > 1) {

            //create vertex shader
            int vShaderLoc = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vShaderLoc, vertexShaderCode);
            GLES20.glCompileShader(vShaderLoc);
            int[] result = { 1 };
            GLES20.glGetShaderiv(vShaderLoc, GLES20.GL_COMPILE_STATUS, result, 0);
            if(result[0] != GLES20.GL_TRUE) {
                Log.e("ERROR", GLES20.glGetShaderInfoLog(vShaderLoc));
                GLES20.glDeleteShader(vShaderLoc);
                return false;
            }

            //create fragment shader
            int fShaderLoc = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fShaderLoc, fragmentxShaderCode);
            GLES20.glCompileShader(fShaderLoc);
            int[] result2 = { 1 };
            GLES20.glGetShaderiv(fShaderLoc, GLES20.GL_COMPILE_STATUS, result2, 0);
            if(result[0] != GLES20.GL_TRUE) {
                Log.e("ERROR", GLES20.glGetShaderInfoLog(fShaderLoc));
                GLES20.glDeleteShader(vShaderLoc);
                GLES20.glDeleteShader(fShaderLoc);
                return false;
            }

            //create program
            program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vShaderLoc);
            GLES20.glAttachShader(program, fShaderLoc);
            GLES20.glLinkProgram(program);
            int[] result3 = { 1 };
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, result3, 0);
            if(result3[0] != GLES20.GL_TRUE) {
                Log.e("ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteShader(vShaderLoc);
                GLES20.glDeleteShader(fShaderLoc);
                GLES20.glDeleteProgram(program);
                return false;
            }
        }

        return true;
    }

}
