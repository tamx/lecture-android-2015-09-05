package com.example.tam.cardboard;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by tam on 15/09/01.
 */
public class GameSurfaceView extends GLSurfaceView {
    private static final int OPENGL_ES_VERSION = 2;

    public GameSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(OPENGL_ES_VERSION);
        setRenderer(new GameRenderer());
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
}
