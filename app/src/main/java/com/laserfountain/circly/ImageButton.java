package com.laserfountain.circly;

import com.laserfountain.framework.Input.TouchEvent;
import com.laserfountain.framework.implementation.AndroidImage;

public class ImageButton {

    public int x0;
    public int y0;
    public int x1;
    public int y1;
    public String str;
    public AndroidImage img;

    public ImageButton(AndroidImage img, String str, int x0, int y0, int x1, int y1) {
        this.img = img;
        this.str = str;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public boolean inBounds(TouchEvent event) {
        return event.x > x0 && event.x < x1 && event.y > y0 && event.y < y1;
    }
}
