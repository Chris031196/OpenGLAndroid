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
    private long lastTime;

    private int program;
    private FloatBuffer vBuffer;
    private FloatBuffer nBuffer;
    private FloatBuffer cBuffer;

    private int mvpLoc;
    private int lightLoc;
    private int mLoc;
    private int vLoc;
    private int pLoc;

    private float[] projMat = new float[16];
    private float[] viewMat = new float[16];
    private float[] rotMat = new float[16];
    private float rotX = 1.0f;
    private float rotY = 1.0f;

    private float resistance = 0.01f;

    public GraphicsRenderer(Graphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
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
        lightLoc = GLES20.glGetUniformLocation(program, "light_position");
        mLoc = GLES20.glGetUniformLocation(program, "m");
        vLoc = GLES20.glGetUniformLocation(program, "v");
        pLoc = GLES20.glGetUniformLocation(program, "p");

        Matrix.setIdentityM(rotMat, 0);
        GLES20.glUniform1fv(lightLoc, 1, new float[] {0.0f, 3.0f, 3.0f}, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        Matrix.frustumM(projMat, 0, -ratio, ratio, -1f, 1f, 1f, 10f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        if(rotX != 0f && rotY != 0f) {
            float[] curRot = new float[16];
            float length = (float) Math.sqrt(rotX * rotX + rotY * rotY) / 50f;
            Matrix.setRotateM(curRot, 0, length, -rotY / length, -rotX / length, 0.0f);

            //calculate rpm
            long time = SystemClock.uptimeMillis() - lastTime;
            lastTime = SystemClock.uptimeMillis();
            float perMin = 60000f / (float) time;
            float rpm =  perMin * length / 360f;
            graphics.getActivity().setRPM(rpm);

            rotX *= 1f - resistance;
            rotY *= 1f - resistance;
            Matrix.multiplyMM(rotMat, 0, curRot, 0, rotMat, 0);
        }

        float[] mvpMat = new float[16];

        Matrix.setLookAtM(viewMat, 0, 0f, 3f, 3f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mvpMat, 0, projMat, 0, viewMat, 0);
        Matrix.multiplyMM(mvpMat, 0, mvpMat, 0, rotMat, 0);

        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMat, 0);
        GLES20.glUniformMatrix4fv(mLoc, 1, false, rotMat, 0);
        GLES20.glUniformMatrix4fv(vLoc, 1, false, viewMat, 0);
        GLES20.glUniformMatrix4fv(pLoc, 1, false, projMat, 0);

        //drawing
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vBuffer);

        GLES20.glEnableVertexAttribArray(1);
        GLES20.glVertexAttribPointer(1, 3, GLES20.GL_FLOAT, false, 0, cBuffer);

        GLES20.glEnableVertexAttribArray(2);
        GLES20.glVertexAttribPointer(2, 3, GLES20.GL_FLOAT, false, 0, nBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
        GLES20.glDisableVertexAttribArray(2);
    }

    public void setRotation(float x, float y) {
        this.rotX += x;
        this.rotY += y;
    }

    public void setResistance(float res) {
        this.resistance = res;
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
        ByteBuffer bbv = ByteBuffer.allocateDirect(4 * 36 * 3);
        bbv.order(ByteOrder.nativeOrder());
        vBuffer = bbv.asFloatBuffer();
        vBuffer.put( new float[]{
                // RIGHT
                 1f, -1f,  1f,
                 1f, -1f, -1f,
                 1f,  1f, -1f,

                 1f, -1f,  1f,
                 1f,  1f, -1f,
                 1f,  1f,  1f,
                // FRONT
                 1f, -1f,  1f,
                -1f,  1f,  1f,
                -1f, -1f,  1f,

                 1f, -1f,  1f,
                 1f,  1f,  1f,
                -1f,  1f,  1f,
                // LEFT
                -1f, -1f,  1f,
                -1f,  1f,  1f,
                -1f,  1f, -1f,

                -1f, -1f,  1f,
                -1f,  1f, -1f,
                -1f, -1f, -1f,
                // BACK
                -1f, -1f, -1f,
                -1f,  1f, -1f,
                 1f,  1f, -1f,

                -1f, -1f, -1f,
                 1f,  1f, -1f,
                 1f, -1f, -1f,
                // TOP
                 1f,  1f,  1f,
                 1f,  1f, -1f,
                -1f,  1f,  1f,

                -1f,  1f,  1f,
                 1f,  1f, -1f,
                -1f,  1f, -1f,
                // BOTTOM
                -1f, -1f,  1f,
                 1f, -1f, -1f,
                 1f, -1f,  1f,

                -1f, -1f,  1f,
                -1f, -1f, -1f,
                 1f, -1f, -1f,
        });

        vBuffer.position(0);


        ByteBuffer bbn = ByteBuffer.allocateDirect(4 * 36 * 3);
        bbn.order(ByteOrder.nativeOrder());
        nBuffer = bbn.asFloatBuffer();
        nBuffer.put( new float[]{
                // RIGHT
                 1f,  0f,  0f,
                 1f,  0f,  0f,
                 1f,  0f,  0f,

                 1f,  0f,  0f,
                 1f,  0f,  0f,
                 1f,  0f,  0f,
                // FRONT
                 0f,  0f,  1f,
                 0f,  0f,  1f,
                 0f,  0f,  1f,

                 0f,  0f,  1f,
                 0f,  0f,  1f,
                 0f,  0f,  1f,
                // LEFT
                -1f,  0f,  0f,
                -1f,  0f,  0f,
                -1f,  0f,  0f,

                -1f,  0f,  0f,
                -1f,  0f,  0f,
                -1f,  0f,  0f,
                // BACK
                 0f,  0f, -1f,
                 0f,  0f, -1f,
                 0f,  0f, -1f,

                 0f,  0f, -1f,
                 0f,  0f, -1f,
                 0f,  0f, -1f,
                // TOP
                 0f,  1f,  0f,
                 0f,  1f,  0f,
                 0f,  1f,  0f,

                 0f,  1f,  0f,
                 0f,  1f,  0f,
                 0f,  1f,  0f,
                // BOTTOM
                 0f, -1f,  0f,
                 0f, -1f,  0f,
                 0f, -1f,  0f,

                 0f, -1f,  0f,
                 0f, -1f,  0f,
                 0f, -1f,  0f,
        });

        nBuffer.position(0);


        ByteBuffer bbc = ByteBuffer.allocateDirect(4 * 36 * 3);
        bbc.order(ByteOrder.nativeOrder());
        cBuffer = bbc.asFloatBuffer();
        cBuffer.put( new float[]{
                // RIGHT
                1f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 0f,

                1f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 0f,
                // FRONT
                0f, 1f, 0f,
                0f, 1f, 0f,
                0f, 1f, 0f,

                0f, 1f, 0f,
                0f, 1f, 0f,
                0f, 1f, 0f,
                // LEFT
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,

                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
                // BACK
                0.5f, 0.5f, 0f,
                0.5f, 0.5f, 0f,
                0.5f, 0.5f, 0f,

                0.5f, 0.5f, 0f,
                0.5f, 0.5f, 0f,
                0.5f, 0.5f, 0f,
                // TOP
                0.5f, 0f, 0.5f,
                0.5f, 0f, 0.5f,
                0.5f, 0f, 0.5f,

                0.5f, 0f, 0.5f,
                0.5f, 0f, 0.5f,
                0.5f, 0f, 0.5f,
                // BOTTOM
                0f, 0.5f, 0.5f,
                0f, 0.5f, 0.5f,
                0f, 0.5f, 0.5f,

                0f, 0.5f, 0.5f,
                0f, 0.5f, 0.5f,
                0f, 0.5f, 0.5f,
        });

        cBuffer.position(0);

        return true;
    }

}
