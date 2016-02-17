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
                effect = 0.1;
                cost = 500;
                break;
            case Rotator:
                effect = 3;
                cost = 7500;
                break;
            case SuperSpin:
                effect = 90;
                cost = 200000;
                break;
            case TouchManiac:
                effect = 3000;
                cost = 80000000;
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

    public double buyUpgrade(double currency) {
        if (upgradePossible(currency)) {
            // We need to calculate cost before increasing number, or it will become more expensive.
            double currencyAfter = currency - getUpgradeCost();
            increaseUpgrades();
            return currencyAfter;
        }
        return currency;
    }

    public void increaseUpgrades() {
        upgrades++;
    }

    public int getUpgrades() {
        return upgrades;
    }

    public double getEffect() {
        return effect;
    }

    public double getUpgradeEffect() {
        return Math.pow((upgrades * 0.02 + 1), 8);
    }

    public double getCost() {
        return Math.round(Math.pow(owned, 2.1) * cost / 100 + cost);
    }

    public double getUpgradeCost() {
        return Math.round(Math.pow(upgrades + 1, 2.1) * cost/300 + cost);
    }

    public boolean upgradePossible(double currency) {
        return upgradeAllowed() && currency > getUpgradeCost();
    }

    public boolean upgradeAllowed() {
        return upgrades < 10 && owned >= (upgrades + 1) * 5;
    }

    public boolean buyAllowed() {
        return true;
    }

    public String getTypeString() {
        return btype.name();
    }
}
