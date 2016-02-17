package com.laserfountain.circly;

public class Upgrade extends BuyButton{

    public enum UpgradeType {
        Edges,
        AutoRotator,
        TouchPercent
    }

    private UpgradeType utype;
    private int cost;
    private int max;
    private int ownedExponent;

    public Upgrade(UpgradeType utype, int x0, int y0, int x1, int y1) {
        super("", x0, y0, x1, y1);
        owned = 1;
        this.utype = utype;
        switch (utype) {
            case Edges:
                cost = 150;
                max = 150;
                ownedExponent = 3;
                break;
            case AutoRotator:
                cost = 4000;
                max = 5;
                ownedExponent = 5;
                break;
            case TouchPercent:
                cost = 7000;
                max = 5;
                ownedExponent = 10;
                break;
        }
    }

    public Upgrade(UpgradeType utype, int owned) {
        this(utype, owned, 0, 0, 0, 0);
    }

    public Upgrade(UpgradeType utype, int owned, int x0, int y0, int x1, int y1) {
        this(utype, x0, y0, x1, y1);
        this.owned = owned;
    }

    public void setArea(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public UpgradeType getUpgradeType() {
        return utype;
    }

    public double getCost() {
        return (int) Math.round(Math.pow((double) owned, ownedExponent) + cost);
    }

    @Override
    public boolean buyAllowed() {
        return owned < max;
    }

    public int getMax() {
        return max;
    }

    public String getTypeString() {
        return utype.name();
    }
}
