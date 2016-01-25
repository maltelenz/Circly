package com.laserfountain.framework;

import android.content.Context;

import com.laserfountain.circly.Building;
import com.laserfountain.circly.Screen;

import java.util.ArrayList;

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

    void updatePoints(float points);

    ArrayList<Building> getBuildings();

    float getPoints();

    /**
     * Locks the screen in portrait mode.
     */
    void lockOrientationPortrait();

    void updateBuildings(ArrayList<Building> buildings);
}
