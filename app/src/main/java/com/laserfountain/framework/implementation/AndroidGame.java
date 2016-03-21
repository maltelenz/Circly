package com.laserfountain.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;


import com.laserfountain.circly.Building;
import com.laserfountain.circly.Upgrade;
import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input;
import com.laserfountain.circly.R;
import com.laserfountain.circly.Screen;

import java.util.ArrayList;

public abstract class AndroidGame extends Activity implements Game, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final int REQUEST_ACHIEVEMENTS = 0;
    private static final double LEADERBOARD_DIVIDER = 1000000;
    AndroidFastRenderView renderView;
    Graphics graphics;
    Input input;
    Screen screen;
    private boolean signedIn;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;
    boolean mExplicitSignOut = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        lockOrientationPortrait();

        Bitmap frameBuffer = getFrameBuffer();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);

        input = new AndroidInput(this, renderView);
        screen = getInitScreen();
        setContentView(renderView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        signedIn = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mExplicitSignOut) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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
    public void onConnected(Bundle connectionHint) {
        signedIn = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        signedIn = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    public void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    public void signOutClicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        signedIn = false;
        mExplicitSignOut = true;
        mGoogleApiClient.disconnect();
    }

    @Override
    public void achievementsClicked() {
        startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                REQUEST_ACHIEVEMENTS);
    }


    @Override
    public void leaderboardClicked(double clicks) {
        String leaderboardID = getContext().getString(R.string.leaderboard_touches);

        Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardID, Math.round(clicks / LEADERBOARD_DIVIDER));

        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderboardID), 0);
    }


    public boolean signedIn() {
        return signedIn;
    }

    @Override
    public void unlockAchievement(String string) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(string, true);
        editor.commit();
        Games.Achievements.unlock(mGoogleApiClient, string);
    }

    @Override
    public void setAchievementSteps(String string, int nr) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(string, nr);
        editor.commit();
        Games.Achievements.setSteps(mGoogleApiClient, string, nr);
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

    private int getFrameBufferWidth() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private int getFrameBufferHeight() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    private Bitmap getFrameBuffer() {
        Log.d("FB Width", Integer.toString(getFrameBufferWidth()));
        Log.d("FB Height", Integer.toString(getFrameBufferHeight()));
        return Bitmap.createBitmap(getFrameBufferWidth(), getFrameBufferHeight(), Config.RGB_565);
    }

    @Override
    public void updatePoints(double points) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.points_achieved), Double.toString(points));
        editor.commit();
    }

    @Override
    public void updateBonuses(int corners) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.bonuses_caught), corners);
        editor.commit();
    }

    @Override
    public void updateTimePlayed(double timePlayed) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.time_played), (int) Math.round(timePlayed));
        editor.commit();
    }

    @Override
    public void updateBuildings(ArrayList<Building> buildings) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        for (Building b : buildings) {
            editor.putInt(b.getTypeString(), b.getOwned());
            editor.putInt(b.getTypeString() + "Upgrade", b.getUpgrades());
        }
        editor.commit();
    }

    @Override
    public void updateUpgrades(ArrayList<Upgrade> upgrades) {
        SharedPreferences preferences = getLevelPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        for (Upgrade b : upgrades) {
            editor.putInt(b.getTypeString(), b.getOwned());
        }
        editor.commit();
    }

    @Override
    public ArrayList<Building> getBuildings() {
        SharedPreferences preferences = getLevelPreferences();
        ArrayList<Building> buildings = new ArrayList<>();
        for (Building.BuildingType btype : Building.BuildingType.values()) {
            buildings.add(new Building(
                    btype,
                    preferences.getInt(btype.name(), 0),
                    preferences.getInt(btype.name() + "Upgrade", 0)));
        }
        return buildings;
    }

    @Override
    public ArrayList<Upgrade> getUpgrades() {
        SharedPreferences preferences = getLevelPreferences();
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        for (Upgrade.UpgradeType btype : Upgrade.UpgradeType.values()) {
            upgrades.add(new Upgrade(
                    btype,
                    preferences.getInt(btype.name(), 0)));
        }
        return upgrades;
    }


    @Override
    public int getBonuses() {
        SharedPreferences preferences = getLevelPreferences();
        return preferences.getInt(getString(R.string.bonuses_caught), 0);
    }

    @Override
    public double getTimePlayed() {
        SharedPreferences preferences = getLevelPreferences();
        return preferences.getInt(getString(R.string.time_played), 0);
    }

    @Override
    public double getPoints() {
        SharedPreferences preferences = getLevelPreferences();
        return Double.parseDouble(preferences.getString(getString(R.string.points_achieved), "0"));
    }

    @Override
    public boolean getAchievement(String string) {
        SharedPreferences preferences = getLevelPreferences();
        return preferences.getBoolean(string, false);
    }

    @Override
    public int getIncrementalAchievement(String string) {
        SharedPreferences preferences = getLevelPreferences();
        return preferences.getInt(string, 0);
    }

}