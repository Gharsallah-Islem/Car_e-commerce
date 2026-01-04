package com.example.Backend.entity;

/**
 * Enum representing types of user activities tracked for recommendations
 */
public enum ActivityType {
    /** User viewed a product page */
    VIEW,

    /** User added product to cart */
    ADD_TO_CART,

    /** User removed product from cart */
    REMOVE_FROM_CART,

    /** User purchased the product */
    PURCHASE,

    /** User searched for products */
    SEARCH,

    /** User clicked on a recommendation */
    RECOMMENDATION_CLICK
}
