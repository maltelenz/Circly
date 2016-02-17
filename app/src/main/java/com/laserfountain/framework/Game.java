package com.laserfountain.framework;

import android.content.Context;

import com.laserfountain.circly.Building;
import com.laserfountain.circly.Screen;
import com.laserfountain.circly.Upgrade;

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

    void updatePoints(double points);

    void updateBonuses(int corners);

    void updateBuildings(ArrayList<Building> buildings);

    void updateTimePlayed(double timePlayed);

    void updateUpgrades(ArrayList<Upgrade> upgrades);

    double getPoints();

    ArrayList<Building> getBuildings();

    ArrayList<Upgrade> getUpgrades();

    int getBonuses();

    double getTimePlayed();

    /**
     * Locks the screen in portrait mode.
     */
    void lockOrientationPortrait();

}
