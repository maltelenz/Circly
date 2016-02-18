package com.laserfountain.circly;

import com.laserfountain.framework.Game;

public class Screen {
    protected final Game game;

    public Screen(Game game) {
        this.game = game;
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
}
