package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShoppingCartTest {

    @Test
    @DisplayName("Should increase the number of items in the cart when adding products")
    void addItemToShoppingCart(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);

        cart.addItem(shirt);

        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should decrease the number of items in the cart when removing products")
    void removeItemInShoppingCart(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);
        Item shirt2 = new Item("2", 200.0, 1);

        cart.addItem(shirt);
        cart.addItem(shirt2);
        cart.removeItem(shirt2);

        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should calculate the total price of the shopping cart")
    void getTotalPriceOfShoppingCart(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 2);
        Item pants = new Item("2", 200.0, 1);
        Item hat  = new Item("3", 300.0, 2);

        cart.addItem(shirt);
        cart.addItem(pants);
        cart.addItem(hat);

        assertThat(cart.getTotalPrice()).isEqualTo(1000.0);
    }
}
