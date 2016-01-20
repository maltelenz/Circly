package com.laserfountain.framework.implementation;

import android.view.View.OnTouchListener;

import com.laserfountain.framework.Input.TouchEvent;

import java.util.List;

public interface TouchHandler extends OnTouchListener {
    boolean isTouchDown(int pointer);

    int getTouchX(int pointer);

    int getTouchY(int pointer);

    List<TouchEvent> getTouchEvents();
}
