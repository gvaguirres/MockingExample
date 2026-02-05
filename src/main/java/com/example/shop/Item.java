package com.example.shop;

public class Item {

    private String id;
    private double price;
    private int quantity;

    public Item(String id, double price, int quantity) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }
    public double getPrice() {
        return price;
    }
    public int getQuantity() {
        return quantity;
    }



}
