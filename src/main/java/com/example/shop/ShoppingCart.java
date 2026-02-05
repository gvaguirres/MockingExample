package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items =  new ArrayList<>();
    private double totalPrice;

    public void addItem(Item item){
        this.items.add(item);
    }
    public List<Item> getItems() {
        return items;
    }
    public void removeItem(Item item){
        this.items.remove(item);
    }
    public double getTotalPrice(){
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

    }



}
