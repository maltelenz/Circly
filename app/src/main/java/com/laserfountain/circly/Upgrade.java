package com.laserfountain.circly;

public class Upgrade extends BuyButton{

    public enum UpgradeType {
        Edges
    }

    private UpgradeType utype;
    private int owned;
    private int cost;

    public Upgrade(UpgradeType utype, int x0, int y0, int x1, int y1) {
        super("", x0, y0, x1, y1);
        owned = 1;
        this.utype = utype;
        switch (utype) {
            case Edges:
                cost = 150;
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

    @Override
    public void increaseOwned() {
        owned++;
    }

    public int getOwned() {
        return owned;
    }

    public int getCost() {
        return (int) Math.round(Math.pow(owned, 3) + cost);
    }

    @Override
    public String getText() {
        return getTypeString();
    }

    public String getTypeString() {
        return utype.name();
    }
}
