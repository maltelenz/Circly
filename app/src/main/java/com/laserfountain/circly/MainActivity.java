package com.laserfountain.circly;

import com.laserfountain.framework.implementation.AndroidGame;

public class MainActivity extends AndroidGame {
    @Override
    public Screen getInitScreen() {
        return new MainScreen(this);
    }
}
