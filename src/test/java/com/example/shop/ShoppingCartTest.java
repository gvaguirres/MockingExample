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
}
