



//Kullanılmıyor





package com.example.optimarket;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel {
    private static AdminPanel instance = new AdminPanel(); // direkt örnek yaratılıyor
    private List<Product> productList;

    private AdminPanel() {
        productList = new ArrayList<>();
    }

    public static AdminPanel getInstance() {
        return instance;
    }

    public void addProduct(Product product) {productList.add(product);}

    public void removeProduct(Product product) {productList.remove(product);}

    public void applyDiscount(Product product, double discountAmount) {
        product.setDiscountAmount(discountAmount);
    }

    public void addStock(Product product, int quantity) {
        product.setStock(product.getStock() + quantity);
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void createAndAddProduct(Context context, String name, double price, double cost, int stock, String category) {
        ProductCreator productCreator = new ProductCreator();
        Product product = productCreator.CreateProduct(category);
        product.setName(name);
        product.setPrice(price);
        product.setCost(cost);
        product.setStock(stock);
        product.setDiscountAmount(0);
        productList.add(product);

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.addProduct(product);

        // Logcat'den (sol alt) kontrol için
        Log.d("DB_PRODUCT_ADD", "Ürün eklendi: " +
                "Name: " + name +
                ", Price: " + price +
                ", Cost: " + cost +
                ", Stock: " + stock +
                ", Category: " + category);
    }
}
