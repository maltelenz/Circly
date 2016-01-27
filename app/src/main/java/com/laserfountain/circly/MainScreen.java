package com.laserfountain.circly;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input.TouchEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    private static final int MAX_TOUCHES = 360;
    private static final int TOUCH_DIFF = 8;
    private static final int CORNER_COST = 150;
    private static final int MAX_CORNERS = 100;
    private int SMALL_CIRCLE_RADIUS;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int CIRCLE_RADIUS;
    private static final float SHRINK_INTERVAL = 50;
    private static final float SAVE_INTERVAL = 500;

    private final Button showBuildingsButton;
    private final Button hideBuildingsButton;
    private final BuyButton flatSeatBuildingButton;
    private final BuyButton angledSeatBuildingButton;

    private final BuyButton cornerUpgradeButton;

    int buildingsHeight;

    private int touches;
    private int multiplier;

    private float clicks;
    private ArrayList<Building> buildings;
    private int corners;


    private boolean buildingsShown;
    private float timeUntilShrink;
    private float timeUntilSave;
    private float rotation;
    private double extra;

    Paint arcPainter;
    private final Paint circlePainter;
    private double baseClick;

    public MainScreen(Game game, Context context) {
        super(game);
        SCREEN_WIDTH = game.getGraphics().getWidth();
        SCREEN_HEIGHT = game.getGraphics().getHeight();

        CIRCLE_RADIUS = game.scaleX(320);
        SMALL_CIRCLE_RADIUS = game.scaleX(10);
        touches = 1;
        multiplier = 1;

        clicks = game.getPoints();

        buildings = game.getBuildings();

        corners = game.getCorners();

        updateExtra();

        buildingsShown = false;

        int buildingsButtonWidth = game.scaleX(400);
        int buildingsButtonHeight = game.scaleY(100);
        buildingsHeight = game.scaleY(250);

        showBuildingsButton = new Button("\u2303",
                SCREEN_WIDTH / 2 - buildingsButtonWidth / 2,
                SCREEN_HEIGHT - buildingsButtonHeight,
                SCREEN_WIDTH / 2 + buildingsButtonWidth / 2,
                SCREEN_HEIGHT);

        hideBuildingsButton = new Button("\u2304",
                SCREEN_WIDTH / 2 - buildingsButtonWidth / 2,
                SCREEN_HEIGHT - buildingsButtonHeight - 2 * buildingsHeight,
                SCREEN_WIDTH / 2 + buildingsButtonWidth / 2,
                SCREEN_HEIGHT - 2 * buildingsHeight);

        flatSeatBuildingButton = new BuyButton("AutoTouch",
                0,
                SCREEN_HEIGHT - 2 * buildingsHeight,
                buildingsHeight,
                SCREEN_HEIGHT - buildingsHeight,
                buildings.get(0).getOwned(),
                buildings.get(0).getCost()
        );

        angledSeatBuildingButton = new BuyButton("Rotator",
                buildingsHeight,
                SCREEN_HEIGHT - 2 * buildingsHeight,
                2 * buildingsHeight,
                SCREEN_HEIGHT - buildingsHeight,
                buildings.get(1).getOwned(),
                buildings.get(1).getCost()
        );

        cornerUpgradeButton = new BuyButton("Edges",
                0,
                SCREEN_HEIGHT - buildingsHeight,
                buildingsHeight,
                SCREEN_HEIGHT,
                corners,
                getCornerCost()
        );

        arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.STROKE);
        arcPainter.setStrokeWidth(10);
        arcPainter.setAntiAlias(true);

        circlePainter = new Paint();
        circlePainter.set(arcPainter);
        circlePainter.setStyle(Paint.Style.FILL);
    }

    private int getCornerCost() {
        return (int) Math.round(Math.pow(corners, 3) * CORNER_COST);
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {

                if (!buildingsShown && showBuildingsButton.inBounds(event)) {
                    // Expand the buildings tab
                    buildingsShown = true;
                    continue;
                }

                if (buildingsShown) {
                    if (hideBuildingsButton.inBounds(event)) {
                        // Hide the buildings tab
                        buildingsShown = false;
                        continue;
                    }
                    if (flatSeatBuildingButton.inBounds(event)) {
                        clicks = buildings.get(0).buy(clicks);
                        flatSeatBuildingButton.setNumber(buildings.get(0).getOwned());
                        flatSeatBuildingButton.setCost(buildings.get(0).getCost());
                        updateExtra();
                        continue;
                    }
                    if (angledSeatBuildingButton.inBounds(event)) {
                        clicks = buildings.get(1).buy(clicks);
                        angledSeatBuildingButton.setNumber(buildings.get(1).getOwned());
                        angledSeatBuildingButton.setCost(buildings.get(1).getCost());
                        updateExtra();
                        continue;
                    }
                    if (cornerUpgradeButton.inBounds(event)) {
                        if (cornerUpgradeButton.cost < clicks && corners < MAX_CORNERS) {
                            clicks -= cornerUpgradeButton.cost;
                            corners++;
                            cornerUpgradeButton.setNumber(corners);
                            cornerUpgradeButton.setCost(getCornerCost());
                            updateExtra();
                        }
                        continue;
                    }
                }

                clicks += multiplier * baseClick;
                if (touches >= MAX_TOUCHES) {
                    if (multiplier != 5) {
                        multiplier++;
                        touches = 0;
                    }
                } else {
                    touches += TOUCH_DIFF;
                }
            }
        }

        timeUntilShrink -= deltaTime;
        if (timeUntilShrink < 0) {
            timeUntilShrink = SHRINK_INTERVAL;
            if (touches <= 0) {
                if (multiplier != 1) {
                    touches = MAX_TOUCHES;
                    multiplier = Math.max(multiplier - 1, 1);
                }
            } else {
                touches = Math.max(touches - TOUCH_DIFF, 0);
            }
        }

        timeUntilSave -= deltaTime;
        if (timeUntilSave < 0) {
            timeUntilSave = SAVE_INTERVAL;
            saveGame();
        }

        clicks += extra * deltaTime;

    }

    private void saveGame() {
        game.updatePoints(clicks);
        game.updateBuildings(buildings);
        game.updateCorners(corners);
    }

    @Override
    public void paint(float deltaTime) {
        drawGame(deltaTime);
    }

    private void drawGame(float deltaTime) {
        Graphics g = game.getGraphics();
        g.clearScreen(ColorPalette.background);

        int intclicks = Math.round(clicks);
        g.drawString(Integer.toString(intclicks), SCREEN_WIDTH / 2, SCREEN_HEIGHT / 10);
        DecimalFormat df = new DecimalFormat("#.##");
        g.drawString(df.format(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 20);

        rotation = (rotation + ((float) touches) / MAX_TOUCHES * deltaTime * 8) % 360;

        switch (multiplier) {
            case 1:
                circlePainter.setColor(ColorPalette.circlePurple);
                break;
            case 2:
                circlePainter.setColor(ColorPalette.circleRed);
                break;
            case 3:
                circlePainter.setColor(ColorPalette.circleOrange);
                break;
            case 4:
                circlePainter.setColor(ColorPalette.circleYellow);
                break;
            case 5:
                circlePainter.setColor(ColorPalette.circleTeal);
                break;
        }

        g.drawNgon(
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT / 2.5,
                CIRCLE_RADIUS,
                corners,
                circlePainter,
                rotation
        );

        g.drawStringCentered("+" + df.format(multiplier * baseClick));

        drawArc(g, ColorPalette.circlePurple, 1);
        drawArc(g, ColorPalette.circleRed, 2);
        drawArc(g, ColorPalette.circleOrange, 3);
        drawArc(g, ColorPalette.circleYellow, 4);
        drawArc(g, ColorPalette.circleTeal, 5);

        if (buildingsShown) {
            g.drawButton(hideBuildingsButton);
            g.drawRect(0, SCREEN_HEIGHT - 2 * buildingsHeight, SCREEN_WIDTH, 2 * buildingsHeight, ColorPalette.drawer);
            g.drawBuyButton(flatSeatBuildingButton);
            g.drawBuyButton(angledSeatBuildingButton);
            g.drawBuyButton(cornerUpgradeButton);
        } else {
            g.drawButton(showBuildingsButton);
        }
    }

    private void drawArc(Graphics g, int color, int comp) {
        float percent;
        if (multiplier < comp) {
            return;
        } else if (multiplier > comp) {
            percent = 1;
        } else {
            percent = ((float) touches) / MAX_TOUCHES;
        }

        int extraRadius =  2 * SMALL_CIRCLE_RADIUS * (comp + 2);
        arcPainter.setColor(color);
        g.drawArc(
                new RectF(
                        SCREEN_WIDTH / 2 - CIRCLE_RADIUS - extraRadius,
                        (int) Math.round(SCREEN_HEIGHT / 2.5 - CIRCLE_RADIUS - extraRadius),
                        SCREEN_WIDTH / 2 + CIRCLE_RADIUS + extraRadius,
                        (int) Math.round(SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + extraRadius)
                ),
                percent,
                arcPainter);
    }

    private void updateExtra() {
        extra = 0;
        baseClick = (1 + (corners - 3) * 0.1);
        for (Building b: buildings) {
            extra += b.getEffect() * b.getOwned() * baseClick;
        }
        saveGame();
    }

    @Override
    public void pause() {
        saveGame();
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean backButton() {
        if (buildingsShown) {
            buildingsShown = false;
            return false;
        } else {
            saveGame();
            return true;
        }
    }
}