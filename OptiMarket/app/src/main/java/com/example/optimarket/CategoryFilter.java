package com.example.optimarket;

public class CategoryFilter implements ProductFilter {
    private String category;

    public CategoryFilter(String category) {
        this.category = category;
    }

    @Override
    public boolean filter(Product product) {
        return product.getCategory().equalsIgnoreCase(category);
    }
}
