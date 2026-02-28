package com.example.optimarket;

public class ProductCreator {
    public Product CreateProduct(String category){
        switch (category.toLowerCase()) {
            case "drinks":
                return new Drinks();
            case "food of animal origin":
                return new FoodOfAnimalOrigin();
            case "fruits and vegetables":
                return new FruitsAndVegetables();
            case "household items":
                return new HouseholdItems();
            case "selfcare":
                return new SelfCare();
            case "snacks":
                return new Snacks();
            case "staple food":
                return new StapleFood();
            default:
                return null;
        }
    }
}
