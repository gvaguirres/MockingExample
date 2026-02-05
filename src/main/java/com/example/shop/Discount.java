package com.example.shop;

public class Discount {
    private int percentage;

    public Discount(int percentage) {
        this.percentage = percentage;
    }
    public double applyDiscount(double totalPrice){
        return totalPrice - (totalPrice * ((double) this.percentage /100));
    }
}
