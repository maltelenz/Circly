package com.laserfountain.circly;

import com.laserfountain.framework.Input;

public class BonusNGon {
    public int x;
    public int y;
    public int radius;

    public BonusNGon(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean inBounds(Input.TouchEvent event) {
        return Math.sqrt(Math.pow(event.x - x, 2) + Math.pow(event.y -y, 2)) < radius;
    }
}
