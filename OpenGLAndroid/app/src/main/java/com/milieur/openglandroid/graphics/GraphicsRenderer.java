package com.milieur.openglandroid.graphics;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GraphicsRenderer implements GLSurfaceView.Renderer {

    private Graphics graphics;

    private int program;
    private FloatBuffer vBuffer;
    private int mvpLoc;
    private float[] projMat = new float[16];
    private float[] viewMat = new float[16];
    private float[] rotMat = new float[16];

    public GraphicsRenderer(Graphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.6f, 0.9f, 1.0f);
        GLES20.glLineWidth(20.0f);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GL10.GL_LESS);
        GLES20.glEnable(GL10.GL_CULL_FACE);

        if(!initShaders()){
            graphics.initFailed();
        }
        if(!initVertexBuffer()){
            graphics.initFailed();
        }

        mvpLoc = GLES20.glGetUniformLocation(program, "mvp");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        Matrix.frustumM(projMat, 0, -ratio, ratio, -1f, 1f, 1f, 10f);
        GLES20.glUniformMatrix4fv(program, 1, true, viewMat, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(rotMat, 0, angle, 0, 0, -1.0f);
        float[] mvpMat = new float[16];
        Matrix.multiplyMM(mvpMat, 0, viewMat, 0, rotMat, 0);

        Matrix.setLookAtM(viewMat, 0, 3f, 3f, 3f, 0f, 0f, 0f, 0f, 1f, 0f);
        GLES20.glUniformMatrix4fv(program, 1, true, viewMat, 0);

        //drawing
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 4);

        GLES20.glDisableVertexAttribArray(0);
    }

    private boolean initShaders() {
        String vertexShaderCode = "";
        String fragmentxShaderCode = "";
        try {
            //read vertex shader code
            BufferedReader reader = new BufferedReader(new InputStreamReader(graphics.getActivity().getVertexShaderCode()));
            String line = "";
            while((line = reader.readLine()) != null) {
                vertexShaderCode += line +"\n";
            }

            //read fragment shader code
            reader = new BufferedReader(new InputStreamReader(graphics.getActivity().getFragmentShaderCode()));
            line = "";
            while((line = reader.readLine()) != null) {
                fragmentxShaderCode += line + "\n";
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
            int[] result = { 0 };
            GLES20.glGetShaderiv(vShaderLoc, GLES20.GL_COMPILE_STATUS, result, 0);
            if(result[0] != GLES20.GL_TRUE) {
                System.err.println(GLES20.glGetShaderInfoLog(vShaderLoc));
                GLES20.glDeleteShader(vShaderLoc);
                return false;
            }

            //create fragment shader
            int fShaderLoc = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fShaderLoc, fragmentxShaderCode);
            GLES20.glCompileShader(fShaderLoc);
            int[] result2 = { 0 };
            GLES20.glGetShaderiv(fShaderLoc, GLES20.GL_COMPILE_STATUS, result2, 0);
            if(result2[0] != GLES20.GL_TRUE) {
                System.err.println(GLES20.glGetShaderInfoLog(fShaderLoc));
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
                System.err.println(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteShader(vShaderLoc);
                GLES20.glDeleteShader(fShaderLoc);
                GLES20.glDeleteProgram(program);
                return false;
            }
        }
        return true;
    }

    private boolean initVertexBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(4* 12);
        bb.order(ByteOrder.nativeOrder());
        vBuffer = bb.asFloatBuffer();
        vBuffer.put( new float[]{
                0f, 1f, 0f,
                -1f, -1f, 0f,
                1f, -1f, 0f });

        vBuffer.position(0);

        return true;
    }

}
