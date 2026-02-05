package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items =  new ArrayList<>();
    private Discount discount;

    public void addItem(Item item){
        this.items.add(item);
    }
    public List<Item> getItems() {
        return items;
    }
    public void removeItem(Item item){
        this.items.remove(item);
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public double getTotalPrice(){
        double totalPrice = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        if (discount != null) {
            return discount.applyDiscount(totalPrice);
        }

        return totalPrice;

    }



}
