package com.laserfountain.circly;

import android.graphics.Paint;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input.TouchEvent;

import java.util.List;

public class MainScreen extends Screen {
    private int SMALL_CIRCLE_RADIUS;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int CIRCLE_RADIUS;
    private static final float SHRINK_INTERVAL = 30;

    private int touches;
    private int multiplier;
    private float timeUntilShrink;

    private int clicks;

    public MainScreen(Game game) {
        super(game);
        SCREEN_WIDTH = game.getGraphics().getWidth();
        SCREEN_HEIGHT = game.getGraphics().getHeight();

        CIRCLE_RADIUS = game.scaleX(320);
        SMALL_CIRCLE_RADIUS = game.scaleX(65);
        touches = 1;
        multiplier = 1;
        clicks = 0;
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {
                clicks += multiplier * 1;
                if (touches == 12) {
                    if (multiplier != 5) {
                        multiplier++;
                        touches = 1;
                    }
                } else {
                    touches++;
                }
            }
        }
        timeUntilShrink -= deltaTime;
        if (timeUntilShrink < 0) {
            timeUntilShrink = SHRINK_INTERVAL;
            if (touches == 1) {
                if (multiplier != 1) {
                    touches = 12;
                    multiplier = Math.max(multiplier - 1, 1);
                }
            } else {
                touches--;
            }
        }
    }

    @Override
    public void paint(float deltaTime) {
        drawGame(deltaTime);

        drawGameProgressOverlay(false, false);
    }

    private void drawGame(float deltaTime) {
        Graphics g = game.getGraphics();
        g.clearScreen(ColorPalette.background);

        g.drawString(Integer.toString(clicks), SCREEN_WIDTH/2, SCREEN_HEIGHT/10);

        Paint arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.FILL);
        arcPainter.setAntiAlias(true);
        g.drawCircle(
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT / 2,
                CIRCLE_RADIUS,
                arcPainter
        );

        g.drawStringCentered("+" + Integer.toString(multiplier));

        switch (multiplier) {
            case 1:
                arcPainter.setColor(ColorPalette.circlePurple);
                break;
            case 2:
                arcPainter.setColor(ColorPalette.circleRed);
                break;
            case 3:
                arcPainter.setColor(ColorPalette.circleOrange);
                break;
            case 4:
                arcPainter.setColor(ColorPalette.circleYellow);
                break;
            case 5:
                arcPainter.setColor(ColorPalette.circleTeal);
                break;
        }
        for (int i = 0; i < touches; i++) {
            g.drawCircle(
                    SCREEN_WIDTH / 2 + (CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 1.2) * Math.cos(i * Math.PI/6),
                    SCREEN_HEIGHT / 2 + (CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 1.2) * Math.sin(i * Math.PI/6),
                    SMALL_CIRCLE_RADIUS,
                    arcPainter);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean backButton() {
        return true;
    }
}