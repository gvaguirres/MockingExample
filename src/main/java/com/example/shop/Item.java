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
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getId() {
        return id;
    }

}
