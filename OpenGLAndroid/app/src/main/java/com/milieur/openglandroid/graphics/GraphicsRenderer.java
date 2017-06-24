package com.milieur.openglandroid.graphics;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GraphicsRenderer implements GLSurfaceView.Renderer {

    private Graphics graphics;

    private int program;
    private int vBuffer;
    private int fBuffer;
    private int vMatrixLoc;
    private int pMatrixLoc;

    public GraphicsRenderer(Graphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glLineWidth(5.0f);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GL10.GL_LESS);
        GLES20.glEnable(GL10.GL_CULL_FACE);

        if(!initShaders()){
            graphics.initFailed();
        }
        if(!initVertexBuffer()){
            graphics.initFailed();
        }

        int[] res = { 0 };
        GLES20.glGenFramebuffers(1, res, 0);
        fBuffer = res[0];

        vMatrixLoc = GLES20.glGetUniformLocation(program, "view_matrix");
        pMatrixLoc = GLES20.glGetUniformLocation(program, "proj_matrix");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        float[] pMat = new float[16];
        Matrix.perspectiveM(pMat, 0, 70, ratio, 1, 10);
        GLES20.glUniformMatrix4fv(pMatrixLoc, 1, false, pMat, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fBuffer);
        GLES20.glUseProgram(program);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] vMat = new float[16];
        Matrix.setLookAtM(vMat, 0, 4, 4, 4, 0, 0, 0, 0, 1, 0);
        GLES20.glUniformMatrix4fv(vMatrixLoc, 1, false, vMat, 0);


        //drawing
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vBuffer);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 2);

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
        int[] result = {0};
        GLES20.glGenBuffers(1, result, 0);
        if(result[0] == 0) {
            Log.e("ERROR", "Couldn't create vertex buffer!");
            return false;
        }
        vBuffer = result[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vBuffer);
        FloatBuffer b = FloatBuffer.wrap(new float[]{
                         0.5f, 0.0f, 0.0f,
                          -0.5f, 0.0f, 0.0f} );
       // b.put( new float[]{
       //         0.5f, 0.0f, 0.0f,
       //          -0.5f, 0.0f, 0.0f
               // 0.5f, 0.5f, 0.5f,
               // 0.5f, -0.5f, 0.5f,
               // -0.5f, -0.5f, 0.5f,
               // -0.5f, 0.5f, 0.5f,

              //  0.5f, 0.5f, -0.5f,
               // 0.5f, -0.5f, -0.5f,
                //-0.5f, -0.5f, -0.5f,
               // -0.5f, 0.5f, -0.5f
       // });

        b.rewind();

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, b.capacity() * 4, b, GLES20.GL_STATIC_DRAW);

        return true;
    }

}
