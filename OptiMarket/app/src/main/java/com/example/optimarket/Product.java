package com.example.optimarket;

public abstract class Product {
    private String name;
    private double originalPrice;  // Orijinal fiyat
    private double price;          // İndirimli fiyat
    private double cost;
    private double profit;
    private int stock;
    private double discountAmount;

    // Constructors
    public Product() {
        this.discountAmount = 0.0;
        this.profit = 0.0;
    }

    public Product(String name, double price) {
        this.name = name;
        this.originalPrice = price;
        this.price = price;
        this.discountAmount = 0.0;
        this.profit = 0.0;
    }

    public Product(String name, double price, double cost, int stock) {
        this.name = name;
        this.originalPrice = price;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.discountAmount = 0.0;
        calculateProfit();
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    // Price methods
    public void setPrice(double price) {
        this.originalPrice = price;
        this.price = price - discountAmount; // İndirim varsa uygula
        calculateProfit();
    }
    public double getPrice() {
        return price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setCost(double cost) {
        this.cost = cost;
        calculateProfit();
    }
    public double getCost() {
        return cost;
    }

    // kar hesabı
    public void calculateProfit() {
        this.profit = this.price - this.cost;
    }
    public double getProfit() {
        return profit;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
    public int getStock() {
        return stock;
    }

    // Stok işlemleri için yardımcı metodlar
    public boolean isInStock() {
        return stock > 0;
    }

    public void decreaseStock(int amount) {
        if (stock >= amount) {
            this.stock -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }

    // Discount methods
    public void setDiscountAmount(double discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        if (discountAmount > originalPrice) {
            throw new IllegalArgumentException("Discount cannot exceed original price");
        }

        this.discountAmount = discountAmount;
        this.price = this.originalPrice - discountAmount;
        calculateProfit();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    // İndirim yüzdesi hesaplama
    public double getDiscountPercentage() {
        if (originalPrice == 0) return 0;
        return (discountAmount / originalPrice) * 100;
    }

    // İndirimli mi kontrol et
    public boolean hasDiscount() {
        return discountAmount > 0;
    }

    public abstract String getCategory();

    // toString method for debugging
    @Override
    public String toString() {
        return String.format("Product{name='%s', price=%.2f, originalPrice=%.2f, cost=%.2f, profit=%.2f, stock=%d, discount=%.2f, category='%s'}",
                name, price, originalPrice, cost, profit, stock, discountAmount, getCategory());
    }
}