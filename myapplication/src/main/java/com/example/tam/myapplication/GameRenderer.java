package com.example.tam.myapplication;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tam on 15/08/28.
 */

public class GameRenderer implements GLSurfaceView.Renderer {
    public static final String sVertexShaderSource =
            "uniform mat4 vpMatrix;" +
                    "uniform mat4 wMatrix;" +
                    "attribute vec3 position;" +
                    "void main() {" +
                    "  gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
                    "}";

    public static final String sFragmentShaderSource =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
                    "}";

    private int mProgramId;
    private float[] mViewAndProjectionMatrix = new float[16];
    private long mFrameCount = 0;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, sVertexShaderSource);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, sFragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);

        mProgramId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramId, vertexShader);
        GLES20.glAttachShader(mProgramId, fragmentShader);
        GLES20.glLinkProgram(mProgramId);
        GLES20.glUseProgram(mProgramId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.orthoM(projectionMatrix, 0, -width / 2f, width / 2f, -height / 2, height / 2, 0f, 2f);
        Matrix.multiplyMM(mViewAndProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float length = 100f;
        float left = -length / 2f;
        float right = length / 2f;
        float top = -length / 2f;
        float bottom = length / 2f;

        float[] vertices = new float[]{
                left, top, 0f,
                left, bottom, 0f,
                right, bottom, 0f,

                right, bottom, 0f,
                right, top, 0f,
                left, top, 0f
        };

        short[] indices = new short[]{
                0, 1, 2,
                3, 4, 5
        };

        FloatBuffer vertexBuffer = BufferUtil.convert(vertices);
        ShortBuffer indexBuffer = BufferUtil.convert(indices);

        float[] worldMatrix = new float[16];
        Matrix.setIdentityM(worldMatrix, 0);
        Matrix.rotateM(worldMatrix, 0, (float) mFrameCount / 2f, 0, 0, 1);

        int attLoc1 = GLES20.glGetAttribLocation(mProgramId, "position");
        int uniLoc1 = GLES20.glGetUniformLocation(mProgramId, "vpMatrix");
        int uniLoc2 = GLES20.glGetUniformLocation(mProgramId, "wMatrix");
        GLES20.glEnableVertexAttribArray(attLoc1);

        GLES20.glVertexAttribPointer(attLoc1, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0);
        GLES20.glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(attLoc1);

        mFrameCount++;
    }
}