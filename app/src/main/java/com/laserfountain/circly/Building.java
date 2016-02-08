package com.laserfountain.circly;

import com.laserfountain.framework.Input;

public class Building extends BuyButton{

    public enum BuildingType {
        AutoTouch,
        Rotator,
        SuperSpin,
        TouchManiac
    }

    private BuildingType btype;
    private int owned;
    private double effect;
    private int cost;
    private int upgrades;

    public int ux0;
    public int uy0;
    public int ux1;
    public int uy1;

    public Building(BuildingType btype, int x0, int y0, int x1, int y1) {
        super("", x0, y0, x1, y1);
        owned = 0;
        upgrades = 0;
        this.btype = btype;
        switch (btype) {
            case AutoTouch:
                effect = 0.001;
                cost = 500;
                break;
            case Rotator:
                effect = 0.03;
                cost = 5000;
                break;
            case SuperSpin:
                effect = 0.9;
                cost = 100000;
                break;
            case TouchManiac:
                effect = 30;
                cost = 40000000;
                break;
        }

        setUpgradeArea();
    }

    public Building(BuildingType btype, int owned, int upgrades) {
        this(btype, owned, upgrades, 0, 0, 0, 0);
    }

    public Building(BuildingType btype, int owned, int upgrades, int x0, int y0, int x1, int y1) {
        this(btype, x0, y0, x1, y1);
        this.owned = owned;
        this.upgrades = upgrades;
    }

    public void setArea(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        setUpgradeArea();
    }

    private void setUpgradeArea() {
        ux0 = x0;
        uy0 = y1 + 1;
        ux1 = x1;
        uy1 = uy0 + (y1 - y0);
    }

    public boolean inUpgradeBounds(Input.TouchEvent event) {
        return event.x > ux0 && event.x < ux1 && event.y > uy0 && event.y < uy1;
    }

    public float buyUpgrade(float currency) {
        if (upgradePossible(currency)) {
            // We need to calculate cost before increasing number, or it will become more expensive.
            float currencyAfter = currency - getUpgradeCost();
            increaseUpgrades();
            return currencyAfter;
        }
        return currency;
    }

    @Override
    public void increaseOwned() {
        owned++;
    }

    public void increaseUpgrades() {
        upgrades++;
    }

    public int getOwned() {
        return owned;
    }

    public int getUpgrades() {
        return upgrades;
    }

    public double getEffect() {
        return effect;
    }

    public double getUpgradeEffect() {
        return Math.pow((upgrades * 0.02 + 1), 12);
    }

    public int getCost() {
        return (int) Math.round(Math.pow(owned, 1.1) * cost/100 + cost);
    }

    public int getUpgradeCost() {
        return (int) Math.round(Math.pow(upgrades + 1, 2.1) * cost/300 + cost);
    }

    public boolean upgradePossible(float currency) {
        if (upgrades < 10 && owned > (upgrades + 1) * 5) {
            return currency > getUpgradeCost();
        }
        return false;
    }

    @Override
    public String getText() {
        return getTypeString();
    }

    public String getTypeString() {
        return btype.name();
    }
}
