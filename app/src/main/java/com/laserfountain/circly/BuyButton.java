package com.laserfountain.circly;

public abstract class BuyButton extends Button{

    public BuyButton(String text, int x0, int y0, int x1, int y1) {
        super(text, x0, y0, x1, y1);
    }

    protected int owned;

    public double buy(double currency) {
        if (buyPossible(currency)) {
            // We need to calculate cost before increasing number, or it will become more expensive.
            double currencyAfter = currency - getCost();
            increaseOwned();
            return currencyAfter;
        }
        return currency;
    }

    public void increaseOwned() {
        owned++;
    }

    public int getOwned() {
        return owned;
    }

    public String getText() {
        return getTypeString();
    }

    public boolean buyPossible(double currency) {
        return buyAllowed() && currency > getCost();
    }

    public abstract boolean buyAllowed();
    public abstract double getCost();
    public abstract String getTypeString();
}
