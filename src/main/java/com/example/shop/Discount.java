package com.example.shop;

public class Discount {
    private int percentage;

    public Discount(int percentage) {
        this.percentage = percentage;
    }

    public double applyDiscount(double totalPrice){
        double result = totalPrice - (totalPrice * ((double) this.percentage /100));
        return Math.max(0, result);
    }

    public int getPercentage() {
        return percentage;
    }
}
