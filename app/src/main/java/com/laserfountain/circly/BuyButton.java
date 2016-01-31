package com.laserfountain.circly;

public abstract class BuyButton extends Button{

    public BuyButton(String text, int x0, int y0, int x1, int y1) {
        super(text, x0, y0, x1, y1);
    }

    public float buy(float currency) {
        if (currency > getCost()) {
            // We need to calculate cost before increasing number, or it will become more expensive.
            float currencyAfter = currency - getCost();
            increaseOwned();
            return currencyAfter;
        }
        return currency;
    }

    public abstract void increaseOwned();
    public abstract int getOwned();
    public abstract int getCost();
    public abstract String getText();
}
