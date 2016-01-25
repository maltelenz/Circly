package com.laserfountain.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.laserfountain.circly.Building;
import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input;
import com.laserfountain.circly.R;
import com.laserfountain.circly.Screen;

import java.util.ArrayList;

public abstract class AndroidGame extends Activity implements Game {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Input input;
    Screen screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        lockOrientationPortrait();

        Bitmap frameBuffer = getFrameBuffer();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);

        input = new AndroidInput(this, renderView);
        screen = getInitScreen();
        setContentView(renderView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        screen.resume();
        // Create a new frame buffer, because the screen may be rotated
        Bitmap framebuffer = getFrameBuffer();
        renderView.resume(framebuffer);
        graphics = new AndroidGraphics(getAssets(), framebuffer);
    }

    @Override
    public void onPause() {
        super.onPause();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    @Override
    public void onBackPressed() {
        if(screen.backButton()) {
            // If the screen says we can exit, use default implementation and exit.
            super.onBackPressed();
        }
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    private SharedPreferences getLevelPreferences() {
        return this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public int scaleX(int in) {
        return graphics.scaleX(in);
    }

    public int scaleY(int in) {
        return graphics.scaleY(in);
    }

    public int scale(int in) {
        return graphics.scale(in);
    }

    public float scaleY(float in) {
        return graphics.scaleY(in);
    }

    public float scale(float in) {
        return graphics.scale(in);
    }

    @Override
    public void lockOrientationPortrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @SuppressWarnings("deprecation")
    private int getFrameBufferWidth() {
        return getWindowManager().getDefaultDisplay().getWidth();
    }
    @SuppressWarnings("deprecation")
    private int getFrameBufferHeight() {
        return getWindowManager().getDefaultDisplay().getHeight();
    }

    private Bitmap getFrameBuffer() {
        Log.d("FB Width", Integer.toString(getFrameBufferWidth()));
        Log.d("FB Height", Integer.toString(getFrameBufferHeight()));
        return Bitmap.createBitmap(getFrameBufferWidth(), getFrameBufferHeight(), Config.RGB_565);
    }

    @Override
    public void updatePoints(float points) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(getString(R.string.points_achieved), points);
        editor.commit();
    }

    @Override
    public void updateBuildings(ArrayList<Building> buildings) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        for (Building b : buildings) {
            editor.putInt(b.getTypeString(), b.getOwned());
        }
        editor.commit();
    }

    @Override
    public ArrayList<Building> getBuildings() {
        SharedPreferences preferences = getLevelPreferences();
        ArrayList<Building> buildings = new ArrayList<>();
        for (Building.BuildingType btype : Building.BuildingType.values()) {
            buildings.add(new Building(btype, preferences.getInt(btype.name(), 0)));
        }
        return buildings;
    }

    @Override
    public float getPoints() {
        SharedPreferences preferences = getLevelPreferences();
        return preferences.getFloat(getString(R.string.points_achieved), 0);
    }
}