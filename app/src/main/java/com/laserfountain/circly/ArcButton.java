package com.laserfountain.circly;

import com.laserfountain.framework.Input.TouchEvent;

public class ArcButton {

    public int x;
    public int y;
    public int xradius;
    public int yradius;
    public String text;

    public ArcButton(String text, int x, int y, int xradius, int yradius) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.xradius = xradius;
        this.yradius = yradius;
    }

    public boolean inBounds(TouchEvent event, int drawerHeight) {
        return event.y <= (y - drawerHeight) && Math.pow(event.x - x, 2)/Math.pow(xradius, 2) + Math.pow(event.y - (y - drawerHeight), 2)/Math.pow(yradius, 2) < 1;
    }
}