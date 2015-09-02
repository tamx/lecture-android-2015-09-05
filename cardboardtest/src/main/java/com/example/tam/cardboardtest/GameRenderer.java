package com.example.tam.cardboardtest;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by tam on 15/08/28.
 */

public class GameRenderer implements CardboardView.StereoRenderer {
    public static final String sVertexShaderSource =
            "uniform mat4 vpMatrix;" +
                    "uniform mat4 wMatrix;" +
                    "attribute vec3 position;"
                    + "attribute vec4 a_Color;        \n"
                    + "varying vec4 v_Color;          \n" +
                    "void main() {" +
                    "   v_Color = a_Color;          \n" +
                    "gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
                    "}";

    public static final String sFragmentShaderSource =
            "precision mediump float;       \n"
                    + "varying vec4 v_Color;          \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_FragColor = v_Color;     \n"
                    + "}                              \n";
    private final static double R = 100;
    private final static double r = 20;
    private final static double PI = 3.141592653589793238;
    float[] vertices;
    short[] indices;
    float[] colors;
    private int mProgramId;
    private float[] mViewAndProjectionMatrix = new float[16];
    private long mFrameCount = 0;

    public GameRenderer() {
        ArrayList<Float> tmpv = new ArrayList<Float>();
        ArrayList<Float> tmpc = new ArrayList<Float>();
        int step = 5;
        for (int t = 0; t < 360; t += step) {
            for (int p = 0; p < 360; p += step) {
                float ox = torusx(t, p);
                float oy = torusy(t, p);
                float oz = torusz(t, p);

                float x1 = torusx(t + step, p);
                float y1 = torusy(t + step, p);
                float z1 = torusz(t + step, p);

                float x2 = torusx(t, p + step);
                float y2 = torusy(t, p + step);
                float z2 = torusz(t, p + step);

                tmpv.add(Float.valueOf(ox));
                tmpv.add(Float.valueOf(oy));
                tmpv.add(Float.valueOf(oz));

                tmpv.add(Float.valueOf(x1));
                tmpv.add(Float.valueOf(y1));
                tmpv.add(Float.valueOf(z1));

                tmpv.add(Float.valueOf(x2));
                tmpv.add(Float.valueOf(y2));
                tmpv.add(Float.valueOf(z2));

                pushColor(tmpc, ox, oy, oz, x1, y1, z1, x2, y2, z2, false);

                float dx = torusx(t + step, p + step);
                float dy = torusy(t + step, p + step);
                float dz = torusz(t + step, p + step);

                tmpv.add(Float.valueOf(dx));
                tmpv.add(Float.valueOf(dy));
                tmpv.add(Float.valueOf(dz));

                tmpv.add(Float.valueOf(x1));
                tmpv.add(Float.valueOf(y1));
                tmpv.add(Float.valueOf(z1));

                tmpv.add(Float.valueOf(x2));
                tmpv.add(Float.valueOf(y2));
                tmpv.add(Float.valueOf(z2));

                pushColor(tmpc, dx, dy, dz, x1, y1, z1, x2, y2, z2, true);
            }
        }
        vertices = new float[tmpv.size()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = tmpv.get(i).floatValue();
        }
        indices = new short[vertices.length / 3];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
        }
        colors = new float[tmpc.size() * 3];
        for (int i = 0; i < colors.length; i += 4 * 3) {
            colors[i + 0] = tmpc.get(i / 3 + 0).floatValue();
            colors[i + 1] = tmpc.get(i / 3 + 1).floatValue();
            colors[i + 2] = tmpc.get(i / 3 + 2).floatValue();
            colors[i + 3] = tmpc.get(i / 3 + 3).floatValue();
            colors[i + 4] = tmpc.get(i / 3 + 0).floatValue();
            colors[i + 5] = tmpc.get(i / 3 + 1).floatValue();
            colors[i + 6] = tmpc.get(i / 3 + 2).floatValue();
            colors[i + 7] = tmpc.get(i / 3 + 3).floatValue();
            colors[i + 8] = tmpc.get(i / 3 + 0).floatValue();
            colors[i + 9] = tmpc.get(i / 3 + 1).floatValue();
            colors[i + 10] = tmpc.get(i / 3 + 2).floatValue();
            colors[i + 11] = tmpc.get(i / 3 + 3).floatValue();
        }
    }

    private void pushColor(ArrayList<Float> tmpc, float ox, float oy, float oz, float x1, float y1, float z1, float x2, float y2, float z2, boolean reverse) {
        // vector1
        float v1x = x1 - ox;
        float v1y = y1 - oy;
        float v1z = z1 - oz;
        // vector2
        float v2x = x2 - ox;
        float v2y = y2 - oy;
        float v2z = z2 - oz;
        // normal vector
        float nx = v1y * v2z - v1z * v2y;
        float ny = v1z * v2x - v1x * v2z;
        float nz = v1x * v2y - v1y * v2x;
        if (reverse) {
            nx *= -1;
            ny *= -1;
            nz *= -1;
        }
        // unit normal vector
        float scalar = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        float ux = nx / scalar;
        float uy = ny / scalar;
        float uz = nz / scalar;

        float bright = ux * 1.0f + uy * 1.0f + uz * 1.0f;
        bright /= Math.sqrt(3);
        bright = bright * 0.3f + 0.7f;
        tmpc.add(Float.valueOf(0.0f));
        tmpc.add(Float.valueOf(bright));
        tmpc.add(Float.valueOf(bright));
        tmpc.add(Float.valueOf(1.0f));
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.orthoM(projectionMatrix, 0, -width / 2f, width / 2f, -height / 2, height / 2, -150f, 150f);
        Matrix.multiplyMM(mViewAndProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
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
        GLES20.glBindAttribLocation(mProgramId, 0, "a_Color");
        GLES20.glLinkProgram(mProgramId);
        GLES20.glUseProgram(mProgramId);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        mFrameCount++;
    }

    private float torusx(int t, int p) {
        double t2 = (PI * (double) t) / ((double) 180);
        double p2 = (PI * (double) p) / ((double) 180);
        Log.d("", "p2: " + p2);
        double x = R * Math.cos(t2) + r * Math.cos(p2) * Math.cos(t2);
        return (float) x;
    }

    private float torusy(int t, int p) {
        double t2 = (PI * (double) t) / ((double) 180);
        double p2 = (PI * (double) p) / ((double) 180);
        double y = R * Math.sin(t2) + r * Math.cos(p2) * Math.sin(t2);
        return (float) y;
    }

    private float torusz(int t, int p) {
        double p2 = (PI * (double) p) / ((double) 180);
        double z = r * Math.sin(p2);
        return (float) z;
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 背景色
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);  // バッファのクリア

        float length = 100f;
        float left = -length / 2f;
        float right = length / 2f;
        float top = -length / 2f;
        float bottom = length / 2f;

//        float[] vertices = new float[]{
//                left, top, -00f,
//                left, bottom, -00f,
//                right, bottom, -00f,
//
//                right, bottom, -00f,
//                right, top, -00f,
//                left, top, -00f
//        };
//
//        short[] indices = new short[]{
//                0, 1, 2,
//                3, 4, 5
//        };

        FloatBuffer vertexBuffer = BufferUtil.convert(vertices);
        ShortBuffer indexBuffer = BufferUtil.convert(indices);
        FloatBuffer colorBuffer = BufferUtil.convert(colors);

        float[] worldMatrix = new float[16];
        Matrix.setIdentityM(worldMatrix, 0);
        Matrix.rotateM(worldMatrix, 0, (float) mFrameCount / 2f, 0, 1, 0);

        int attLoc1 = GLES20.glGetAttribLocation(mProgramId, "position");
        int uniLoc1 = GLES20.glGetUniformLocation(mProgramId, "vpMatrix");
        int uniLoc2 = GLES20.glGetUniformLocation(mProgramId, "wMatrix");
        int mColorHandle = GLES20.glGetAttribLocation(mProgramId, "a_Color");

        GLES20.glEnableVertexAttribArray(attLoc1);
        GLES20.glVertexAttribPointer(attLoc1, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);  // ポインタと色属性を結び付ける
        GLES20.glEnableVertexAttribArray(mColorHandle);  // 色属性有効

        float[] mViewMatrix = new float[16];
        Matrix.multiplyMM(mViewMatrix, 0, eye.getEyeView(), 0, mViewAndProjectionMatrix, 0);
        GLES20.glUniformMatrix4fv(uniLoc1, 1, false, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(attLoc1);
    }
}