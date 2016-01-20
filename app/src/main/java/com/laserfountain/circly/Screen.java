package com.laserfountain.circly;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;

public class Screen {
    protected final Game game;

    private int levelIndicatorRadius;
    private int levelIndicatorPadding;
    protected int progressBarHeight;

    public Screen(Game game) {
        this.game = game;

        levelIndicatorRadius = game.scale(100);
        levelIndicatorPadding = game.scale(50);
        progressBarHeight = game.scaleY(30);
    }

    public void update(float deltaTime) {
    }

    public void paint(float deltaTime) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
    }

    public boolean backButton() {
        return true;
    }

    protected void drawGameProgressOverlay(boolean playing, boolean finished) {
        int xPosition = game.getGraphics().getWidth() - levelIndicatorPadding - levelIndicatorRadius;
        int yPosition = levelIndicatorRadius + levelIndicatorPadding + progressBarHeight;

        drawGameProgressOverlay(xPosition, yPosition, playing);
    }

    /**
     * Draws the progress overlay for the complete game (number of levels finished)
     * @param drawIndicator if currently playing a level
     */
    protected void drawGameProgressOverlay(int xPosition, int yPosition, boolean drawIndicator) {
        Graphics g = game.getGraphics();

    }

}
