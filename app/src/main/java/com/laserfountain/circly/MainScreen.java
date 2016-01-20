package com.laserfountain.circly;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input.TouchEvent;
import com.laserfountain.framework.implementation.AndroidImage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    private final Context context;

    private int SMALL_CIRCLE_RADIUS;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int CIRCLE_RADIUS;
    private static final float SHRINK_INTERVAL = 30;

    private final Button showBuildingsButton;
    private final Button hideBuildingsButton;
    private final ImageButton flatSeatBuildingButton;
    private final ImageButton angledSeatBuildingButton;
    int buildingsHeight;

    private ArrayList<Building> buildings;

    private int touches;
    private int multiplier;
    private double clicks;

    private boolean buildingsShown;
    private float timeUntilShrink;
    private double extra;

    private final int godModeMultiplier = 1;

    public MainScreen(Game game, Context context) {
        super(game);
        SCREEN_WIDTH = game.getGraphics().getWidth();
        SCREEN_HEIGHT = game.getGraphics().getHeight();

        CIRCLE_RADIUS = game.scaleX(320);
        SMALL_CIRCLE_RADIUS = game.scaleX(65);
        touches = 1;
        multiplier = 1;
        clicks = 0;

        buildings = new ArrayList<>();
        buildings.add(new Building(Building.BuildingType.FlatSeat));
        buildings.add(new Building(Building.BuildingType.AngledSeat));

        buildingsShown = false;

        int buildingsButtonWidth = game.scaleX(400);
        int buildingsButtonHeight = game.scaleY(100);
        buildingsHeight = game.scaleY(300);

        showBuildingsButton = new Button("\u2303",
                game.getGraphics().getWidth() / 2 - buildingsButtonWidth / 2,
                game.getGraphics().getHeight() - buildingsButtonHeight - buildingsHeight / 2,
                game.getGraphics().getWidth() / 2 + buildingsButtonWidth / 2,
                game.getGraphics().getHeight() - buildingsHeight / 2);

        hideBuildingsButton = new Button("\u2304",
                game.getGraphics().getWidth() / 2 - buildingsButtonWidth / 2,
                game.getGraphics().getHeight() - buildingsButtonHeight - buildingsHeight,
                game.getGraphics().getWidth() / 2 + buildingsButtonWidth / 2,
                game.getGraphics().getHeight() - buildingsHeight);

        this.context = context;
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
                0, SCREEN_HEIGHT - buildingsHeight, buildingsHeight, SCREEN_HEIGHT
        );

        angledSeatBuildingButton = new ImageButton(
                angledSeatImage,
                buildingsHeight, SCREEN_HEIGHT - buildingsHeight, 2 * buildingsHeight, SCREEN_HEIGHT
        );

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
                        // Expand the buildings tab
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
                }

                clicks += multiplier * godModeMultiplier;
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

        clicks += extra * deltaTime;

    }

    @Override
    public void paint(float deltaTime) {
        drawGame(deltaTime);

        drawGameProgressOverlay(false, false);
    }

    private void drawGame(float deltaTime) {
        Graphics g = game.getGraphics();
        g.clearScreen(ColorPalette.background);

        int intclicks = (int)Math.round(clicks);
        g.drawString(Integer.toString(intclicks), SCREEN_WIDTH/2, SCREEN_HEIGHT / 10);
        DecimalFormat df = new DecimalFormat("#.##");
        g.drawString(df.format(extra * 100) + "/s", SCREEN_WIDTH/2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 3);

        Paint arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.FILL);
        arcPainter.setAntiAlias(true);
        g.drawCircle(
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT / 2.5,
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
                    SCREEN_HEIGHT / 2.5 + (CIRCLE_RADIUS + SMALL_CIRCLE_RADIUS * 1.2) * Math.sin(i * Math.PI/6),
                    SMALL_CIRCLE_RADIUS,
                    arcPainter);
        }

        if (buildingsShown) {
            g.drawButton(hideBuildingsButton);
            g.drawRectNoFill(0, SCREEN_HEIGHT - buildingsHeight, SCREEN_WIDTH, buildingsHeight, ColorPalette.laser);
            g.drawImageButton(flatSeatBuildingButton, 0, buildings.get(0).getOwned());
            g.drawImageButton(angledSeatBuildingButton, 0, buildings.get(1).getOwned());
        } else {
            g.drawButton(showBuildingsButton);
            g.drawRectNoFill(0, SCREEN_HEIGHT - buildingsHeight / 2, SCREEN_WIDTH, buildingsHeight, ColorPalette.laser);
            g.drawImageButton(flatSeatBuildingButton, buildingsHeight / 2, buildings.get(0).getOwned());
            g.drawImageButton(angledSeatBuildingButton, buildingsHeight / 2, buildings.get(1).getOwned());
        }
    }

    private void updateExtra() {
        extra = 0;
        for (Building b: buildings) {
            extra += b.getEffect() * b.getOwned() * godModeMultiplier;
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