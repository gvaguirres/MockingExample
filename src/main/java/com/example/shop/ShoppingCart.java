package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items =  new ArrayList<>();

    public void addItem(Item item){
        this.items.add(item);
    }
    public List<Item> getItems() {
        return items;
    }
    public void removeItem(Item item){
        this.items.remove(item);
    }


}
