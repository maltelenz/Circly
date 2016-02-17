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

    private ArcButton showUpgradesButton;
    private ArcButton showBuildingsButton;
    private ArcButton showStatsButton;

    private BonusNGon bonusNGon;

    private int buyButtonHeight;
    private int drawerBoxHeight;

    private int touches;
    private int multiplier;

    private double clicks;
    private int bonusesCaught;
    private double timePlayed;
    private int edgesOwned;
    private int maxEdges;

    private ArrayList<Building> buildings;
    private ArrayList<Upgrade> upgrades;

    private boolean upgradesShown;
    private boolean buildingsShown;
    private boolean statsShown;

    private float timeUntilShrink;
    private float timeUntilSave;
    private float timeLeftOnBonus;
    private float timeLeftOnSuper;
    private boolean superSpeedActive;
    private boolean superTouchActive;
    private float rotation;
    private double extra;

    private double baseClick;
    private double perTouch;
    private double cornerEffect;

    private Paint arcPainter;
    private Paint circlePaint;
    private Paint multiplierPaint;
    private Paint snackTextPaint;
    private Paint statsTextPaint;

    private ArrayList<String> snacks;
    private float timeUntilNextSnack;

    private int upgradeDrawerHeight;
    private int buildingDrawerHeight;
    private int statsDrawerHeight;

    private int drawerHeight;

    private Context context;

    public MainScreen(Game game, Context context) {
        super(game);
        this.context = context;

        initializeDimensions();

        touches = 1;
        multiplier = 1;

        initializeFromSave();

        hideDrawer();

        initializeDrawerButtons();

        for (int i = 0; i < buildings.size(); i++) {
            buildings.get(i).setArea(
                    i * buyButtonHeight + 2,
                    SCREEN_HEIGHT - buildingDrawerHeight + drawerBoxHeight + 2,
                    (i + 1) * buyButtonHeight - 2,
                    SCREEN_HEIGHT - buildingDrawerHeight + drawerBoxHeight + buyButtonHeight - 2);
        }

        for (int i = 0; i < upgrades.size(); i++) {
            upgrades.get(i).setArea(
                    i * buyButtonHeight + 2,
                    SCREEN_HEIGHT - upgradeDrawerHeight + drawerBoxHeight + 2,
                    (i + 1) * buyButtonHeight - 2,
                    SCREEN_HEIGHT - upgradeDrawerHeight + drawerBoxHeight + buyButtonHeight - 2);
        }

        bonusNGon = null;
        superSpeedActive = false;
        superTouchActive = false;
        timeLeftOnSuper = 0;

        initializePainters();

        snacks = new ArrayList<>();

        updateExtra();
    }

    private void initializeDimensions() {
        SCREEN_WIDTH = game.getGraphics().getWidth();
        SCREEN_HEIGHT = game.getGraphics().getHeight();

        CIRCLE_RADIUS = game.scaleX(320);
        SMALL_CIRCLE_RADIUS = game.scaleX(10);
        BONUS_NGON_RADIUS = game.scaleX(100);
        SNACK_HEIGHT = game.scaleY(144);

        buyButtonHeight = game.scaleY(250);
        drawerBoxHeight = game.scaleY(250);

        upgradeDrawerHeight = drawerBoxHeight + 2 * buyButtonHeight;
        buildingDrawerHeight = drawerBoxHeight + 2 * buyButtonHeight;
        statsDrawerHeight = drawerBoxHeight + game.scaleY(500);
    }

    private void initializeDrawerButtons() {
        int drawerButtonRadius = SCREEN_WIDTH / 6;
        int drawerButtonHeight = game.scaleY(120);

        showUpgradesButton = new ArcButton(
                SCREEN_WIDTH / 6,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        showBuildingsButton = new ArcButton(
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );

        showStatsButton = new ArcButton(
                SCREEN_WIDTH * 5 / 6,
                SCREEN_HEIGHT,
                drawerButtonRadius,
                drawerButtonHeight
        );
    }

    private void initializePainters() {
        arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.circleGreen);
        arcPainter.setStyle(Paint.Style.STROKE);
        arcPainter.setStrokeWidth(10);
        arcPainter.setAntiAlias(true);

        circlePaint = new Paint();
        circlePaint.set(arcPainter);
        circlePaint.setStyle(Paint.Style.FILL);

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
    }

    private void initializeFromSave() {
        clicks = game.getPoints();

        buildings = game.getBuildings();
        upgrades = game.getUpgrades();

        bonusesCaught = game.getBonuses();
        timePlayed = game.getTimePlayed();
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {

                if (bonusNGon != null && bonusNGon.inBounds(event)) {
                    activateBonus();
                    continue;
                }

                if (!upgradesShown && showUpgradesButton.inBounds(event, drawerHeight)) {
                    showUpgradeDrawer();
                    continue;
                }

                if (!buildingsShown && showBuildingsButton.inBounds(event, drawerHeight)) {
                    showBuildingDrawer();
                    continue;
                }

                if (!statsShown && showStatsButton.inBounds(event, drawerHeight)) {
                    showStatsDrawer();
                    continue;
                }

                if (buildingsShown) {
                    if (showBuildingsButton.inBounds(event, drawerHeight)) {
                        hideDrawer();
                        continue;
                    }
                    for (Building b : buildings) {
                        if (b.inBounds(event)) {
                            clicks = b.buy(clicks);
                            updateExtra();
                        }
                        if (b.inUpgradeBounds(event)) {
                            clicks = b.buyUpgrade(clicks);
                            updateExtra();
                        }
                    }
                }

                if (upgradesShown) {
                    if (showUpgradesButton.inBounds(event, drawerHeight)) {
                        hideDrawer();
                        continue;
                    }
                    for (Upgrade b : upgrades) {
                        if (b.inBounds(event)) {
                            clicks = b.buy(clicks);
                            updateExtra();
                        }
                    }
                }

                if (statsShown) {
                    if (showStatsButton.inBounds(event, drawerHeight)) {
                        hideDrawer();
                        continue;
                    }
                }

                clicks += multiplier * perTouch;
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

    private void showStatsDrawer() {
        hideDrawer();
        statsShown = true;
        drawerHeight = statsDrawerHeight;
    }

    private void showBuildingDrawer() {
        hideDrawer();
        buildingsShown = true;
        drawerHeight = buildingDrawerHeight;
    }

    private void showUpgradeDrawer() {
        hideDrawer();
        upgradesShown = true;
        drawerHeight = upgradeDrawerHeight;
    }

    private void hideDrawer() {
        upgradesShown = false;
        buildingsShown = false;
        statsShown = false;
        drawerHeight = 0;
    }

    private void activateBonus() {
        bonusNGon = null;
        bonusesCaught++;
        Random randomGenerator = new Random();
        int selector = randomGenerator.nextInt(100);
        if (selector < 70) {
            double bonusClicks = extra * 100 * randomGenerator.nextInt(100);
            clicks += bonusClicks;
            String text = NumberFormatter.formatDouble(bonusClicks) + " " +
                    context.getString(R.string.bonus_exclamation);
            addSnack(text);
        } else if (selector < 85) {
            superTouchActive = true;
            superSpeedActive = false;
            timeLeftOnSuper = SUPERSPEED_TIME;
            updateExtra();
            String text = context.getString(R.string.super_touch) + " " +
                    NumberFormatter.formatDouble(SUPERTOUCH_EFFECT) + context.getString(R.string.exclamation);
            addSnack(text);
        } else {
            superTouchActive = false;
            superSpeedActive = true;
            timeLeftOnSuper = SUPERSPEED_TIME;
            updateExtra();
            String text = context.getString(R.string.super_speed) + " " +
                    NumberFormatter.formatDouble(SUPERSPEED_EFFECT) + context.getString(R.string.exclamation);
            addSnack(text);
        }
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
        game.updateUpgrades(upgrades);
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
        g.drawString(
                NumberFormatter.formatDouble(extra * 100) +
                        context.getString(R.string.per_second),
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(200)
        );
        g.drawString(
                context.getString(R.string.start_paren_plus) +
                        NumberFormatter.formatDouble(cornerEffect * 100) +
                        context.getString(R.string.percent_end_paren),
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT / 2.5 + CIRCLE_RADIUS + game.scaleX(275),
                multiplierPaint
        );

        rotation = (rotation + ((float) touches) / MAX_TOUCHES * deltaTime * 8) % 360;

        if (superTouchActive) {
            circlePaint.setColor(ColorPalette.bonus);
        } else {
            switch (multiplier) {
                case 1:
                    circlePaint.setColor(ColorPalette.circlePurple);
                    break;
                case 2:
                    circlePaint.setColor(ColorPalette.circleRed);
                    break;
                case 3:
                    circlePaint.setColor(ColorPalette.circleOrange);
                    break;
                case 4:
                    circlePaint.setColor(ColorPalette.circleYellow);
                    break;
                case 5:
                    circlePaint.setColor(ColorPalette.circleTeal);
                    break;
            }
        }

        if (edgesOwned == maxEdges) {
            g.drawCircle(
                    SCREEN_WIDTH / 2,
                    SCREEN_HEIGHT / 2.5,
                    CIRCLE_RADIUS,
                    circlePaint
            );
        } else {
            g.drawNgon(
                    SCREEN_WIDTH / 2,
                    SCREEN_HEIGHT / 2.5,
                    CIRCLE_RADIUS,
                    edgesOwned,
                    circlePaint,
                    rotation
            );
        }

        g.drawStringCentered("+" + NumberFormatter.formatDouble(multiplier * perTouch));

        drawArc(g, ColorPalette.circlePurple, 1);
        drawArc(g, ColorPalette.circleRed, 2);
        drawArc(g, ColorPalette.circleOrange, 3);
        drawArc(g, ColorPalette.circleYellow, 4);
        drawArc(g, ColorPalette.circleTeal, 5);

        g.drawArcButton(showBuildingsButton, drawerHeight, buildingsShown);
        g.drawArcButton(showStatsButton, drawerHeight, statsShown);
        g.drawArcButton(showUpgradesButton, drawerHeight, statsShown);

        g.drawRect(0, SCREEN_HEIGHT - drawerHeight, SCREEN_WIDTH, drawerHeight, ColorPalette.drawer);
        g.drawRect(0, SCREEN_HEIGHT - drawerHeight, SCREEN_WIDTH, drawerBoxHeight, ColorPalette.box);
        g.drawLine(0, SCREEN_HEIGHT - drawerHeight, SCREEN_WIDTH, SCREEN_HEIGHT - drawerHeight, ColorPalette.black);

        g.drawString(
                NumberFormatter.formatDouble(extra * 100) +
                        context.getString(R.string.per_second),
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT - drawerHeight + game.scaleY(100)
        );
        g.drawString(
                context.getString(R.string.start_paren_plus) +
                        NumberFormatter.formatDouble(cornerEffect * 100) +
                        context.getString(R.string.percent_end_paren),
                SCREEN_WIDTH / 2,
                SCREEN_HEIGHT - drawerHeight + game.scaleY(175),
                multiplierPaint
        );

        if (buildingsShown) {
            for (Building b : buildings) {
                g.drawBuildingButton(b, clicks);
            }
        }

        if (upgradesShown) {
            for (Upgrade b : upgrades) {
                g.drawBuyButton(b, clicks);
            }
        }

        if (statsShown) {
            g.drawString(context.getString(R.string.edges_colon) + " " + NumberFormatter.formatInt(edgesOwned),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + drawerBoxHeight + game.scaleX(50),
                    statsTextPaint);
            g.drawString(context.getString(R.string.bonuses_caught_colon) + " " + NumberFormatter.formatInt(bonusesCaught),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + drawerBoxHeight + game.scaleX(100),
                    statsTextPaint);
            g.drawString(context.getString(R.string.time_played_colon) + " " + NumberFormatter.formatDoubleTime(timePlayed/100),
                    game.scaleX(25),
                    SCREEN_HEIGHT - statsDrawerHeight + drawerBoxHeight + game.scaleX(150),
                    statsTextPaint);
        }

        drawSnack(g, deltaTime);

        if (bonusNGon != null) {
            g.drawBonusNGon(bonusNGon, Math.max(edgesOwned, 3));
        }
    }

    public Upgrade getUpgrade(Upgrade.UpgradeType type) {
        for (Upgrade b: upgrades) {
            if (b.getUpgradeType() == type) {
                return b;
            }
        }
        return null;
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
        edgesOwned = getUpgrade(Upgrade.UpgradeType.Edges).getOwned() + 1;
        maxEdges = getUpgrade(Upgrade.UpgradeType.Edges).getMax();
        cornerEffect = (edgesOwned - 1) * 0.1;
        baseClick = 1 + cornerEffect;
        extra = extra * baseClick;
        if (superSpeedActive) {
            extra = extra * SUPERSPEED_EFFECT;
        }
        perTouch = baseClick + getUpgrade(Upgrade.UpgradeType.TouchPercent).getOwned() * extra;
        if (superTouchActive) {
            perTouch = perTouch * SUPERTOUCH_EFFECT;
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