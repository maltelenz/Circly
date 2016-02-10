package com.laserfountain.circly;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.laserfountain.framework.Game;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Input.TouchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainScreen extends Screen {
    private static final int MAX_TOUCHES = 360;
    private static final int TOUCH_DIFF = 8;
    private static final int BONUS_NGON_TIME = 500;
    private static final float SNACK_TIME = 300;

    private int SNACK_HEIGHT;
    private int SMALL_CIRCLE_RADIUS;
    private int BONUS_NGON_RADIUS;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int CIRCLE_RADIUS;

    private static final float SHRINK_INTERVAL = 5;
    private static final float SAVE_INTERVAL = 500;

    private final ArcButton showBuildingsButton;
    private final ArcButton hideBuildingsButton;

    private final Upgrade cornerUpgrade;

    private BonusNGon bonusNGon;

    int buildingsHeight;

    private int touches;
    private int multiplier;

    private double clicks;
    private ArrayList<Building> buildings;

    private boolean buildingsShown;
    private float timeUntilShrink;
    private float timeUntilSave;
    private float timeLeftOnBonus;
    private float rotation;
    private double extra;

    Paint arcPainter;
    private final Paint circlePainter;
    private double baseClick;
    private double cornerEffect;
    private Paint multiplierPaint;
    private Paint snackTextPaint;

    private ArrayList<String> snacks;
    private float timeUntilNextSnack;
    private int drawerHeight;

    public MainScreen(Game game, Context context) {
        super(game);
        SCREEN_WIDTH = game.getGraphics().getWidth();
        SCREEN_HEIGHT = game.getGraphics().getHeight();

        CIRCLE_RADIUS = game.scaleX(320);
        SMALL_CIRCLE_RADIUS = game.scaleX(10);
        BONUS_NGON_RADIUS = game.scaleX(100);
        SNACK_HEIGHT = game.scaleY(144);

        touches = 1;
        multiplier = 1;

        clicks = game.getPoints();

        buildings = game.getBuildings();

        buildingsShown = false;

        int buildingsButtonWidth = game.scaleX(200);
        int buildingsButtonHeight = game.scaleY(120);
        buildingsHeight = game.scaleY(250);
        drawerHeight = 4 * buildingsHeight;

        showBuildingsButton = new ArcButton("\u2303",
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT,
                buildingsButtonWidth,
                buildingsButtonHeight
        );

        hideBuildingsButton = new ArcButton("\u2304",
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT - drawerHeight,
                buildingsButtonWidth,
                buildingsButtonHeight
        );

        for (int i = 0; i < buildings.size(); i++) {
            buildings.get(i).setArea(
                    i * buildingsHeight,
                    SCREEN_HEIGHT - 3 * buildingsHeight,
                    (i + 1) * buildingsHeight,
                    SCREEN_HEIGHT - 2 * buildingsHeight);
        }

        cornerUpgrade = new Upgrade(
                Upgrade.UpgradeType.Edges,
                game.getCorners(),
                0,
                SCREEN_HEIGHT - buildingsHeight,
                buildingsHeight,
                SCREEN_HEIGHT
        );

        bonusNGon = null;

        arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.STROKE);
        arcPainter.setStrokeWidth(10);
        arcPainter.setAntiAlias(true);

        circlePainter = new Paint();
        circlePainter.set(arcPainter);
        circlePainter.setStyle(Paint.Style.FILL);

        this.multiplierPaint = new Paint();

        float fontSize = game.scale(45);

        multiplierPaint.setTextSize(fontSize);
        multiplierPaint.setTextAlign(Paint.Align.CENTER);
        multiplierPaint.setAntiAlias(true);
        multiplierPaint.setColor(ColorPalette.mediumText);
        multiplierPaint.setTypeface(Typeface.DEFAULT_BOLD);

        snackTextPaint = new Paint();
        snackTextPaint.setTextSize(game.scaleY(42));
        snackTextPaint.setTextAlign(Paint.Align.CENTER);
        snackTextPaint.setAntiAlias(true);
        snackTextPaint.setColor(ColorPalette.lightText);
        snackTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        snacks = new ArrayList<>();

        updateExtra();
    }

    @Override
    public void update(float deltaTime) {
        boolean buttonTriggered = false;
        boolean touchedsomething = false;
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {
                touchedsomething = true;

                if (!buildingsShown && showBuildingsButton.inBounds(event)) {
                    // Expand the buildings tab
                    buildingsShown = true;
                    buttonTriggered = true;
                    continue;
                }

                if (bonusNGon != null && bonusNGon.inBounds(event)) {
                    bonusNGon = null;
                    Random randomGenerator = new Random();
                    double bonusClicks = extra * 100 * randomGenerator.nextInt(100);
                    clicks += bonusClicks;
                    String text = Integer.toString((int) Math.round(bonusClicks)) + " bonus!";
                    addSnack(text);
                    buttonTriggered = true;
                    continue;
                }

                if (buildingsShown) {
                    if (hideBuildingsButton.inBounds(event)) {
                        // Hide the buildings tab
                        buildingsShown = false;
                        buttonTriggered = true;
                        continue;
                    }
                    for (Building b : buildings) {
                        if (b.inBounds(event)) {
                            clicks = b.buy(clicks);
                            updateExtra();
                            buttonTriggered = true;
                        }
                        if (b.inUpgradeBounds(event)) {
                            clicks = b.buyUpgrade(clicks);
                            updateExtra();
                            buttonTriggered = true;
                        }
                    }
                    if (cornerUpgrade.inBounds(event)) {
                        clicks = cornerUpgrade.buy(clicks);
                        updateExtra();
                        buttonTriggered = true;
                        continue;
                    }
                }

                clicks += multiplier * baseClick;
                touches += TOUCH_DIFF;
                if (touches >= MAX_TOUCHES) {
                    if (multiplier != 5) {
                        multiplier++;
                        touches -= MAX_TOUCHES;
                    } else {
                        touches = MAX_TOUCHES;
                    }
                }
            }
        }

        if (!buttonTriggered && touchedsomething) {
            buildingsShown = false;
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
                touches = Math.round(Math.max(touches - ((float)TOUCH_DIFF)/10, 0));
            }
        }

        if (bonusNGon == null) {
            Random randomGenerator = new Random();
            if (randomGenerator.nextInt(10000) == 0) {
                // Show a new bonus ngon
                bonusNGon = new BonusNGon(
                        BONUS_NGON_RADIUS + randomGenerator.nextInt(SCREEN_WIDTH - 2 * BONUS_NGON_RADIUS),
                        BONUS_NGON_RADIUS + randomGenerator.nextInt(SCREEN_HEIGHT - 2 * BONUS_NGON_RADIUS),
                        BONUS_NGON_RADIUS);
                timeLeftOnBonus = BONUS_NGON_TIME;
            }
        } else {
            timeLeftOnBonus -= deltaTime;
            if (timeLeftOnBonus < 0) {
                bonusNGon = null;
            }
        }

        timeUntilSave -= deltaTime;
        if (timeUntilSave < 0) {
            timeUntilSave = SAVE_INTERVAL;
            saveGame();
        }

        clicks += extra * deltaTime;

    }

    private void addSnack(String text) {
        snacks.add(text);
        if (snacks.size() == 1) {
            timeUntilNextSnack = SNACK_TIME;
        }
    }

    private void saveGame() {
        game.updatePoints(clicks);
        game.updateBuildings(buildings);
        game.updateCorners(cornerUpgrade.getOwned());
    }

    @Override
    public void paint(float deltaTime) {
        drawGame(deltaTime);
    }

    private void drawGame(float deltaTime) {
        Graphics g = game.getGraphics();
        g.clearScreen(ColorPalette.background);

        g.drawString(NumberFormatter.formatDouble(clicks), SCREEN_WIDTH / 2, SCREEN_HEIGHT / 10);
        g.drawString(NumberFormatter.formatDouble(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(200));
        g.drawString("(+" + NumberFormatter.formatDouble(cornerEffect * 100) + "%)", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(275), multiplierPaint);

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
                cornerUpgrade.getOwned(),
                circlePainter,
                rotation
        );

        g.drawStringCentered("+" + NumberFormatter.formatDouble(multiplier * baseClick));

        drawArc(g, ColorPalette.circlePurple, 1);
        drawArc(g, ColorPalette.circleRed, 2);
        drawArc(g, ColorPalette.circleOrange, 3);
        drawArc(g, ColorPalette.circleYellow, 4);
        drawArc(g, ColorPalette.circleTeal, 5);

        if (bonusNGon != null) {
            g.drawBonusNGon(bonusNGon, Math.max(cornerUpgrade.getOwned(), 3));
        }

        if (buildingsShown) {
            g.drawArcButton(hideBuildingsButton);
            g.drawRect(0, SCREEN_HEIGHT - drawerHeight, SCREEN_WIDTH, drawerHeight, ColorPalette.drawer);

            g.drawString(NumberFormatter.formatDouble(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT - 3 * buildingsHeight - game.scaleX(125));
            g.drawString("(+" + NumberFormatter.formatDouble(cornerEffect * 100) + "%)", SCREEN_WIDTH / 2, SCREEN_HEIGHT - 3 * buildingsHeight - game.scaleX(50), multiplierPaint);

            for (Building b : buildings) {
                g.drawBuildingButton(b, clicks);
            }
            g.drawBuyButton(cornerUpgrade, clicks);
        } else {
            g.drawArcButton(showBuildingsButton);
        }

        drawSnack(g, deltaTime);
    }

    private void drawArc(Graphics g, int color, int comp) {
        arcPainter.setColor(ColorPalette.circleInactive);
        int extraRadius =  2 * SMALL_CIRCLE_RADIUS * (comp + 2);
        g.drawArc(
                new RectF(
                        SCREEN_WIDTH / 2 - CIRCLE_RADIUS - extraRadius,
                        (int) Math.round(SCREEN_HEIGHT / 2.5 - CIRCLE_RADIUS - extraRadius),
                        SCREEN_WIDTH / 2 + CIRCLE_RADIUS + extraRadius,
                        (int) Math.round(SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + extraRadius)
                ),
                1,
                arcPainter);

        float percent;
        arcPainter.setColor(color);
        if (multiplier < comp) {
            return;
        } else if (multiplier > comp) {
            percent = 1;
        } else {
            percent = ((float) touches) / MAX_TOUCHES;
        }

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

    private void drawSnack(Graphics g, float deltaTime) {
        timeUntilNextSnack -= deltaTime;
        if (timeUntilNextSnack < 0 && !snacks.isEmpty()) {
            snacks.remove(0);
            timeUntilNextSnack = SNACK_TIME;
        }
        if (!snacks.isEmpty()) {
            float distance = Math.min(SNACK_TIME - timeUntilNextSnack, timeUntilNextSnack);
            int yOffset = 0;
            if (distance < SNACK_TIME/10) {
                yOffset = Math.round(SNACK_HEIGHT * (1 - distance/(SNACK_TIME/10)));
            }
            g.drawRect(0, SCREEN_HEIGHT - SNACK_HEIGHT + yOffset, SCREEN_WIDTH + 1, SCREEN_HEIGHT + yOffset, ColorPalette.snackBackground);
            g.drawString(snacks.get(0), SCREEN_WIDTH / 2, SCREEN_HEIGHT - SNACK_HEIGHT / 2 + yOffset, snackTextPaint);
        }
    }

    private void updateExtra() {
        extra = 0;
        for (Building b: buildings) {
            extra += b.getUpgradeEffect() * b.getEffect() * b.getOwned();
        }
        cornerEffect = (cornerUpgrade.getOwned() - 1) * 0.1;
        baseClick = 1 + cornerEffect;
        extra = extra * baseClick;
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