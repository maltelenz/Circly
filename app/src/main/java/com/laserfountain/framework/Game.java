package com.laserfountain.framework;

import android.content.Context;

import com.laserfountain.circly.Screen;

public interface Game {

    Input getInput();

    Graphics getGraphics();

    void setScreen(Screen screen);

    Screen getCurrentScreen();

    Screen getInitScreen();

    Context getContext();

    int scaleX(int in);

    int scaleY(int in);

    int scale(int in);

    float scaleY(float in);

    float scale(float in);

    /**
     * Locks the screen in portrait mode.
     */
    void lockOrientationPortrait();

}
