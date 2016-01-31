package com.laserfountain.circly;

public class Building extends BuyButton{

    public enum BuildingType {
        AutoTouch,
        Rotator,
        SuperSpin
    }

    private BuildingType btype;
    private int owned;
    private double effect;
    private int cost;

    public Building(BuildingType btype, int x0, int y0, int x1, int y1) {
        super("", x0, y0, x1, y1);
        owned = 0;
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
        }
    }

    public Building(BuildingType btype, int owned) {
        this(btype, owned, 0, 0, 0, 0);
    }

    public Building(BuildingType btype, int owned, int x0, int y0, int x1, int y1) {
        this(btype, x0, y0, x1, y1);
        this.owned = owned;
    }

    public void setArea(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public void increaseOwned() {
        owned++;
    }

    public int getOwned() {
        return owned;
    }

    public double getEffect() {
        return effect;
    }

    public int getCost() {
        return (int) Math.round(Math.pow(owned, 1.9) + cost);
    }

    @Override
    public String getText() {
        return getTypeString();
    }

    public String getTypeString() {
        return btype.name();
    }
}
