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
    private static final float SUPERSPEED_TIME = 1000;
    private static final double SUPERSPEED_EFFECT = 8;
    private static final double SUPERTOUCH_EFFECT = 30;


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

    private final ArcButton showStatsButton;
    private final ArcButton hideStatsButton;

    private final Upgrade cornerUpgrade;

    private BonusNGon bonusNGon;

    int buildingsHeight;

    private int touches;
    private int multiplier;

    private double clicks;
    private int bonusesCaught;
    private double timePlayed;

    private ArrayList<Building> buildings;

    private boolean buildingsShown;
    private boolean statsShown;

    private float timeUntilShrink;
    private float timeUntilSave;
    private float timeLeftOnBonus;
    private boolean superSpeedActive;
    private boolean superTouchActive;
    private float timeLeftOnSuper;
    private float rotation;
    private double extra;

    Paint arcPainter;
    private final Paint circlePainter;
    private double baseClick;
    private double cornerEffect;
    private Paint multiplierPaint;
    private Paint snackTextPaint;
    private Paint statsTextPaint;

    private ArrayList<String> snacks;
    private float timeUntilNextSnack;

    private int buildingDrawerHeight;
    private int statsDrawerHeight;

    int drawerHeight;


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

        bonusesCaught = game.getBonuses();
        timePlayed = game.getTimePlayed();

        buildingsShown = false;
        statsShown = false;

        int drawerButtonRadius = SCREEN_WIDTH / 6;
        int drawerButtonHeight = game.scaleY(120);
        buildingsHeight = game.scaleY(250);
        buildingDrawerHeight = 4 * buildingsHeight;

        statsDrawerHeight = game.scaleY(500);

        showBuildingsButton = new ArcButton("\u2303",
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        hideBuildingsButton = new ArcButton("\u2304",
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        showStatsButton = new ArcButton("\u2303",
                SCREEN_WIDTH * 5 / 6,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        hideStatsButton = new ArcButton("\u2304",
                SCREEN_WIDTH * 5 / 6,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        for (int i = 0; i < buildings.size(); i++) {
            buildings.get(i).setArea(
                    i * buildingsHeight + 2,
                    SCREEN_HEIGHT - 3 * buildingsHeight + 2,
                    (i + 1) * buildingsHeight - 2,
                    SCREEN_HEIGHT - 2 * buildingsHeight - 2);
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
        superSpeedActive = false;
        superTouchActive = false;
        timeLeftOnSuper = 0;

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

        statsTextPaint = new Paint();
        statsTextPaint.set(snackTextPaint);
        statsTextPaint.setTextAlign(Paint.Align.LEFT);
        statsTextPaint.setColor(ColorPalette.darkText);

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

                if (bonusNGon != null && bonusNGon.inBounds(event)) {
                    bonusNGon = null;
                    bonusesCaught++;
                    Random randomGenerator = new Random();
                    int selector = randomGenerator.nextInt(100);
                    if (selector < 70) {
                        double bonusClicks = extra * 100 * randomGenerator.nextInt(100);
                        clicks += bonusClicks;
                        String text = Integer.toString((int) Math.round(bonusClicks)) + " bonus!";
                        addSnack(text);
                    } else if (selector < 85) {
                        superTouchActive = true;
                        superSpeedActive = false;
                        timeLeftOnSuper = SUPERSPEED_TIME;
                        updateExtra();
                        String text = "Super Touch: Touches x " + NumberFormatter.formatDouble(SUPERTOUCH_EFFECT) + "!";
                        addSnack(text);
                    } else {
                        superTouchActive = false;
                        superSpeedActive = true;
                        timeLeftOnSuper = SUPERSPEED_TIME;
                        updateExtra();
                        String text = "Super speed: Auto touches x " + NumberFormatter.formatDouble(SUPERSPEED_EFFECT) + "!";
                        addSnack(text);
                    }
                    buttonTriggered = true;
                    continue;
                }

                if (!buildingsShown && showBuildingsButton.inBounds(event, drawerHeight)) {
                    // Expand the buildings tab
                    buildingsShown = true;
                    statsShown = false;
                    buttonTriggered = true;
                    continue;
                }

                if (!statsShown && showStatsButton.inBounds(event, drawerHeight)) {
                    // Expand the stats tab
                    buildingsShown = false;
                    statsShown = true;
                    buttonTriggered = true;
                    continue;
                }

                if (buildingsShown) {
                    if (hideBuildingsButton.inBounds(event, drawerHeight)) {
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

                if (statsShown) {
                    if (hideStatsButton.inBounds(event, drawerHeight)) {
                        // Hide the stats tab
                        statsShown = false;
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
            statsShown = false;
        }

        if (buildingsShown) {
            drawerHeight = buildingDrawerHeight;
        } else if (statsShown) {
            drawerHeight = statsDrawerHeight;
        } else {
            drawerHeight = 0;
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
            if (randomGenerator.nextInt(5000) == 0) {
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

        timeLeftOnSuper -= deltaTime;
        if (timeLeftOnSuper < 0) {
            superTouchActive = false;
            superSpeedActive = false;
            updateExtra();
        }

        timeUntilSave -= deltaTime;
        if (timeUntilSave < 0) {
            timeUntilSave = SAVE_INTERVAL;
            saveGame();
        }

        clicks += extra * deltaTime;
        timePlayed += deltaTime;
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
        game.updateBonuses(bonusesCaught);
        game.updateTimePlayed(timePlayed);
    }

    @Override
    public void paint(float deltaTime) {
        drawGame(deltaTime);
    }

    private void drawGame(float deltaTime) {
        Graphics g = game.getGraphics();
        if (superSpeedActive) {
            g.clearScreen(ColorPalette.bonus);
        } else {
            g.clearScreen(ColorPalette.background);
        }

        g.drawString(NumberFormatter.formatDouble(clicks), SCREEN_WIDTH / 2, SCREEN_HEIGHT / 10);
        g.drawString(NumberFormatter.formatDouble(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(200));
        g.drawString("(+" + NumberFormatter.formatDouble(cornerEffect * 100) + "%)", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(275), multiplierPaint);

        rotation = (rotation + ((float) touches) / MAX_TOUCHES * deltaTime * 8) % 360;

        if (superTouchActive) {
            circlePainter.setColor(ColorPalette.bonus);
        } else {
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

        if (buildingsShown) {
            g.drawArcButton(hideBuildingsButton, drawerHeight);
            g.drawRect(0, SCREEN_HEIGHT - buildingDrawerHeight, SCREEN_WIDTH, buildingDrawerHeight, ColorPalette.drawer);
            g.drawRect(0, SCREEN_HEIGHT - buildingDrawerHeight, SCREEN_WIDTH, buildingsHeight, ColorPalette.box);

            g.drawString(NumberFormatter.formatDouble(extra * 100) + "/s", SCREEN_WIDTH / 2, SCREEN_HEIGHT - 3 * buildingsHeight - game.scaleX(125));
            g.drawString("(+" + NumberFormatter.formatDouble(cornerEffect * 100) + "%)", SCREEN_WIDTH / 2, SCREEN_HEIGHT - 3 * buildingsHeight - game.scaleX(50), multiplierPaint);

            for (Building b : buildings) {
                g.drawBuildingButton(b, clicks);
            }
            g.drawBuyButton(cornerUpgrade, clicks);
        } else {
            g.drawArcButton(showBuildingsButton, drawerHeight);
        }

        if (statsShown) {
            g.drawArcButton(hideStatsButton, drawerHeight);
            g.drawRect(0, SCREEN_HEIGHT - statsDrawerHeight, SCREEN_WIDTH, statsDrawerHeight, ColorPalette.box);

            g.drawString("Touches per second: " + NumberFormatter.formatDouble(extra * 100) + "/s",
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + game.scaleX(50),
                    statsTextPaint);
            g.drawString("Multiplier: " + NumberFormatter.formatDouble(cornerEffect * 100) + "%",
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + game.scaleX(100),
                    statsTextPaint);
            g.drawString("Edges: " + NumberFormatter.formatInt(cornerUpgrade.getOwned()),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + game.scaleX(150),
                    statsTextPaint);
            g.drawString("Bonuses caught: " + NumberFormatter.formatInt(bonusesCaught),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + game.scaleX(200),
                    statsTextPaint);
            g.drawString("Time played: " + NumberFormatter.formatDoubleTime(timePlayed/100),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + game.scaleX(250),
                    statsTextPaint);

        } else {
            g.drawArcButton(showStatsButton, drawerHeight);
        }

        drawSnack(g, deltaTime);

        if (bonusNGon != null) {
            g.drawBonusNGon(bonusNGon, Math.max(cornerUpgrade.getOwned(), 3));
        }
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
        if (superSpeedActive) {
            extra = extra * SUPERSPEED_EFFECT;
        }
        if (superTouchActive) {
            baseClick = baseClick * SUPERTOUCH_EFFECT;
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