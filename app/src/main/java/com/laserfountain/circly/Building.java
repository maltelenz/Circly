package com.laserfountain.circly;

public class Building {
    enum BuildingType {
        FlatSeat,
        AngledSeat
    };

    private int owned;
    private double effect;
    private int cost;

    public Building(BuildingType btype) {
        owned = 0;
        switch (btype) {
            case FlatSeat:
                effect = 0.001;
                cost = 500;
                break;
            case AngledSeat:
                effect = 0.01;
                cost = 5000;
                break;
        }
    }

    public double buy(double currency) {
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
        return cost;
    }
}
