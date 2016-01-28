package com.laserfountain.circly;

public class Building {
    public enum BuildingType {
        FlatSeat,
        AngledSeat
    }

    private BuildingType btype;
    private int owned;
    private double effect;
    private int cost;

    public Building(BuildingType btype) {
        owned = 0;
        this.btype = btype;
        switch (btype) {
            case FlatSeat:
                effect = 0.001;
                cost = 500;
                break;
            case AngledSeat:
                effect = 0.03;
                cost = 5000;
                break;
        }
    }

    public Building(BuildingType btype, int owned) {
        this(btype);
        this.owned = owned;
    }

    public float buy(float currency) {
        if (currency > cost) {
            owned++;
            return currency - cost;
        }
        return currency;
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

    public String getTypeString() {
        return btype.name();
    }
}
