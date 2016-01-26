package com.laserfountain.circly;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.RectF;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input.TouchEvent;
import com.laserfountain.framework.implementation.AndroidImage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    private static final int MAX_TOUCHES = 360;
    private static final int TOUCH_DIFF = 8;
    private static final int CORNER_COST = 1000;
    private static final int MAX_CORNERS = 4;
    private int SMALL_CIRCLE_RADIUS;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int CIRCLE_RADIUS;
    private static final float SHRINK_INTERVAL = 50;
    private static final float SAVE_INTERVAL = 500;

    private final Button showBuildingsButton;
    private final Button hideBuildingsButton;
    private final ImageButton flatSeatBuildingButton;
    private final ImageButton angledSeatBuildingButton;

    private final Button cornerUpgradeButton;

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
        SMALL_CIRCLE_RADIUS = game.scaleX(65);
        touches = 1;
        multiplier = 1;

        clicks = game.getPoints();

        buildings = game.getBuildings();

        corners = game.getCorners();

        updateExtra();

        buildingsShown = false;

        int buildingsButtonWidth = game.scaleX(400);
        int buildingsButtonHeight = game.scaleY(100);
        buildingsHeight = game.scaleY(300);

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

        AndroidImage flatSeatImage = new AndroidImage(
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_airline_seat_flat_black_48dp),
                Graphics.ImageFormat.RGB565
        );

        AndroidImage angledSeatImage = new AndroidImage(
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_airline_seat_flat_angled_black_48dp),
                Graphics.ImageFormat.RGB565
        );

        flatSeatBuildingButton = new ImageButton(
                flatSeatImage,
                0, SCREEN_HEIGHT - 2 * buildingsHeight, buildingsHeight, SCREEN_HEIGHT - buildingsHeight
        );

        angledSeatBuildingButton = new ImageButton(
                angledSeatImage,
                buildingsHeight, SCREEN_HEIGHT - 2 * buildingsHeight, 2 * buildingsHeight, SCREEN_HEIGHT - buildingsHeight
        );

        cornerUpgradeButton = new Button("X",
                0,
                SCREEN_HEIGHT - buildingsHeight,
                buildingsHeight,
                SCREEN_HEIGHT);

        arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.STROKE);
        arcPainter.setStrokeWidth(10);
        arcPainter.setAntiAlias(true);

        circlePainter = new Paint();
        circlePainter.set(arcPainter);
        circlePainter.setStyle(Paint.Style.FILL);
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
                        updateExtra();
                        continue;
                    }
                    if (angledSeatBuildingButton.inBounds(event)) {
                        clicks = buildings.get(1).buy(clicks);
                        updateExtra();
                        continue;
                    }
                    if (cornerUpgradeButton.inBounds(event)) {
                        int cost = corners * CORNER_COST;
                        if (cost < clicks && corners < MAX_CORNERS) {
                            clicks -= cost;
                            corners ++;
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
        g.drawString(df.format(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 3);

        rotation = (rotation + ((float) touches) / MAX_TOUCHES * deltaTime * 8) % 360;

        switch (multiplier) {
            case 1:
                circlePainter.setColor(ColorPalette.circlePurple);
                arcPainter.setColor(ColorPalette.circleRed);
                break;
            case 2:
                circlePainter.setColor(ColorPalette.circleRed);
                arcPainter.setColor(ColorPalette.circleOrange);
                break;
            case 3:
                circlePainter.setColor(ColorPalette.circleOrange);
                arcPainter.setColor(ColorPalette.circleYellow);
                break;
            case 4:
                circlePainter.setColor(ColorPalette.circleYellow);
                arcPainter.setColor(ColorPalette.circleTeal);
                break;
            case 5:
                circlePainter.setColor(ColorPalette.circleTeal);
                arcPainter.setColor(ColorPalette.circleTeal);
                break;
        }

        switch (corners) {
            case 3:
                g.drawTriangle(
                        SCREEN_WIDTH / 2,
                        SCREEN_HEIGHT / 2.5,
                        CIRCLE_RADIUS,
                        circlePainter,
                        rotation
                );
                break;
            case 4:
                g.drawRect(
                        SCREEN_WIDTH / 2,
                        SCREEN_HEIGHT / 2.5,
                        CIRCLE_RADIUS,
                        circlePainter,
                        rotation
                );
                break;
        }

        g.drawStringCentered("+" + df.format(multiplier * baseClick));

        g.drawArc(
            new RectF(
                    SCREEN_WIDTH / 2 - CIRCLE_RADIUS - SMALL_CIRCLE_RADIUS,
                    (int) Math.round(SCREEN_HEIGHT / 2.5 - CIRCLE_RADIUS - SMALL_CIRCLE_RADIUS),
                    SCREEN_WIDTH / 2 + CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS,
                    (int) Math.round(SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS)
                    ),
                ((float) touches) / MAX_TOUCHES,
            arcPainter);

        if (buildingsShown) {
            g.drawButton(hideBuildingsButton);
            g.drawRect(0, SCREEN_HEIGHT - 2 * buildingsHeight, SCREEN_WIDTH, 2 * buildingsHeight, ColorPalette.drawer);
            g.drawImageButton(flatSeatBuildingButton, buildings.get(0).getOwned());
            g.drawImageButton(angledSeatBuildingButton, buildings.get(1).getOwned());
            g.drawButton(cornerUpgradeButton);
        } else {
            g.drawButton(showBuildingsButton);
        }
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