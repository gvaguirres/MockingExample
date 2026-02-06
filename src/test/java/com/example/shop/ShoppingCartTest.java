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

    @Test
    @DisplayName("Should apply discount to the shopping cart")
    void applyDiscountToShoppingCart(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);
        Item pants = new Item("2", 200.0, 1);
        Discount discount = new Discount(50);

        cart.addItem(shirt);
        cart.addItem(pants);
        cart.setDiscount(discount);

        assertThat(cart.getTotalPrice()).isEqualTo(150);

    }

    @Test
    @DisplayName("Should update the quantity of an item when new item added in the cart is the same as the existing")
    void manageQuantityUpdate(){
        ShoppingCart cart = new ShoppingCart();
        Item item1 = new Item("A1", 100.0, 1);
        Item item2 = new Item("A1", 100.0, 2);

        cart.addItem(item1);
        cart.addItem(item2);

        int totalQuantity = cart.getItems().getFirst().getQuantity();

        assertThat(cart.getItems()).hasSize(1);
        assertThat(totalQuantity).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return total price 0 when shopping cart is empty")
    void totalPriceShouldBeZeroWhenShoppingCartIsEmpty(){
        ShoppingCart cart = new ShoppingCart();

        cart.getTotalPrice();

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should not colapse when removing an item that is not in the cart")
    void removeItemThatDoesNotExist(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);

        cart.removeItem(shirt);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should return same total price when discount is more than 100%")
    void discountShouldNotBeMoreThan100(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 1000.0, 1);
        Discount discount = new Discount(110);

        cart.addItem(shirt);
        cart.setDiscount(discount);

        assertThat(cart.getTotalPrice()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Should return same total price when discount is a negative number")
    void discountShouldNotBeNegative(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);
        Discount discount = new Discount(-50);

        cart.addItem(shirt);
        cart.setDiscount(discount);

        assertThat(cart.getTotalPrice()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should apply discount even when adding more items to the cart after the first item")
    void shouldApplyDiscountWhenAddingMoreItemsAfter(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, 1);
        Item pants = new Item("2", 200.0, 1);
        Item hat = new Item("3", 300.0, 2);
        Discount discount = new Discount(50);

        cart.addItem(shirt);
        cart.setDiscount(discount);
        cart.addItem(pants);
        cart.addItem(hat);

        assertThat(cart.getTotalPrice()).isEqualTo(450.0);
    }

    @Test
    @DisplayName("Should not add any item when the quantity is negative")
    void notAddItemsWhenQuantityOfItemsIsNegative(){
        ShoppingCart cart = new ShoppingCart();
        Item shirt = new Item("1", 100.0, -1);
        Item pants = new Item("2", 200.0, -1);

        cart.addItem(shirt);
        cart.addItem(pants);

        assertThat(cart.getItems()).isEmpty();
    }

}
