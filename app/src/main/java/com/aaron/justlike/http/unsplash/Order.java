package com.aaron.justlike.http.unsplash;

public enum Order {

    LATEST("latest"),
    OLDEST("oldest"),
    POPULAR("popular");

    private String order;

    Order(String order) {
        this.order = order;
    }

    public String getOrder() {
        return order;
    }
}