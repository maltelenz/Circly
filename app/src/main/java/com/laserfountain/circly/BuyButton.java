package com.laserfountain.circly;

public class BuyButton extends Button{

    public int cost;
    public int number;

    public BuyButton(String text, int x0, int y0, int x1, int y1, int number, int cost) {
        super(text, x0, y0, x1, y1);
        this.number = number;
        this.cost = cost;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
