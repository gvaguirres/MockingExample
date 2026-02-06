package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items =  new ArrayList<>();
    private Discount discount;

    public void addItem(Item newItem){

        if ( newItem.getQuantity() > 0) {
            for (Item existingItem : items) {
                if (existingItem.getId().equals(newItem.getId())) {
                    int updatedQuantity = existingItem.getQuantity() + newItem.getQuantity();
                    existingItem.setQuantity(updatedQuantity);

                    return;
                }
            }
            this.items.add(newItem);
        }
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

        if (discount != null && discount.getPercentage() <= 100 && discount.getPercentage() > 0) {
            return discount.applyDiscount(totalPrice);
        }

        return totalPrice;

    }



}
