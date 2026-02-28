package com.example.optimarket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "OptiMarket.db";
    private static final int DATABASE_VERSION = 5;

    // Mevcut tablo
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "category TEXT NOT NULL," +
            "price REAL NOT NULL," +
            "cost REAL NOT NULL," +
            "profit REAL," +
            "discountAmount REAL DEFAULT 0," +
            "stock INTEGER DEFAULT 0" +
            ")";

    // Hazır ürün kataloğu tablosu
    private static final String CREATE_TABLE_PRODUCT_CATALOG = "CREATE TABLE product_catalog (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "cost REAL NOT NULL," +
            "category TEXT NOT NULL" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_PRODUCTS);
            db.execSQL(CREATE_TABLE_PRODUCT_CATALOG);
            Log.d("DB_HELPER", "products and product_catalog tables created");

            // Örnek ürünleri kataloğa ekle
            insertInitialCatalogProducts(db);
        } catch (Exception e) {
            Log.e("DB_HELPER", "Table creation error: " + e.getMessage());
        }
    }

    private void insertInitialCatalogProducts(SQLiteDatabase db) {
        try {
            // Drinks
            addCatalogProduct(db, "Coke", 1.5, "drinks");
            addCatalogProduct(db, "Orange Juice", 2.0, "drinks");
            addCatalogProduct(db, "Mineral Water", 1.0, "drinks");

            // Food of Animal Origin
            addCatalogProduct(db, "Chicken Breast", 5.0, "food of animal origin");
            addCatalogProduct(db, "Salmon Fillet", 7.5, "food of animal origin");
            addCatalogProduct(db, "Eggs", 2.0, "food of animal origin");

            // Fruits and Vegetables
            addCatalogProduct(db, "Apple", 0.75, "fruits and vegetables");
            addCatalogProduct(db, "Broccoli", 1.5, "fruits and vegetables");
            addCatalogProduct(db, "Carrot", 0.5, "fruits and vegetables");

            // Household Items
            addCatalogProduct(db, "Detergent", 3.5, "household items");
            addCatalogProduct(db, "Toilet Paper", 4.0, "household items");
            addCatalogProduct(db, "Dish Soap", 2.5, "household items");

            // SelfCare
            addCatalogProduct(db, "Shampoo", 4.0, "selfcare");
            addCatalogProduct(db, "Toothpaste", 1.5, "selfcare");
            addCatalogProduct(db, "Hand Sanitizer", 3.0, "selfcare");

            // Snacks
            addCatalogProduct(db, "Potato Chips", 1.0, "snacks");
            addCatalogProduct(db, "Chocolate Bar", 1.5, "snacks");
            addCatalogProduct(db, "Popcorn", 1.2, "snacks");

            // Staple Food
            addCatalogProduct(db, "Rice", 2.0, "staple food");
            addCatalogProduct(db, "Pasta", 1.8, "staple food");
            addCatalogProduct(db, "Bread", 1.0, "staple food");

            Log.d("DB_HELPER", "Catalog products added successfully");
        } catch (Exception e) {
            Log.e("DB_HELPER", "Error while adding catalog products: " + e.getMessage());
        }
    }

    private void addCatalogProduct(SQLiteDatabase db, String name, double cost, String category) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("cost", cost);
        values.put("category", category);
        long result = db.insert("product_catalog", null, values);
        if (result == -1) {
            Log.e("DB_HELPER", "Failed to add catalog product: " + name);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB_HELPER", "Database upgrading: " + oldVersion + " -> " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS product_catalog");
        onCreate(db);
    }

    // Ana ürün ekleme fonksiyonu - geliştirilmiş hata kontrolü ile
    public boolean addProduct(Product product) {
        if (product == null) {
            Log.e("DB_HELPER", "Product cannot be null");
            return false;
        }

        // Gerekli alanları kontrol et
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            Log.e("DB_HELPER", "Product name cannot be empty");
            return false;
        }

        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            Log.e("DB_HELPER", "Category cannot be empty");
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", product.getName().trim());
            values.put("category", product.getCategory().trim());
            values.put("price", product.getPrice());
            values.put("cost", product.getCost());

            // Profit hesapla
            double profit = product.getPrice() - product.getCost();
            values.put("profit", profit);

            // Varsayılan değerler
            values.put("discountAmount", product.getDiscountAmount() != 0 ? product.getDiscountAmount() : 0.0);
            values.put("stock", product.getStock() != 0 ? product.getStock() : 0);

            long id = db.insert("products", null, values);

            if (id == -1) {
                Log.e("DB_HELPER", "Failed to add product: " + product.getName());
                return false;
            } else {
                Log.d("DB_HELPER", "Product added successfully - ID: " + id + ", Name: " + product.getName());
                return true;
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "Product add error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Ürün varlığını kontrol etme
    public boolean isProductExists(String productName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM products WHERE name = ?", new String[]{productName});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e("DB_HELPER", "Product existence check error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return false;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM products ORDER BY name", null);

            Log.d("DB_HELPER", "Total product count: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    try {
                        String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                        ProductCreator creator = new ProductCreator();
                        Product product = creator.CreateProduct(category);

                        if (product != null) {
                            product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                            product.setCost(cursor.getDouble(cursor.getColumnIndexOrThrow("cost")));
                            product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                            product.setDiscountAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("discountAmount")));
                            product.calculateProfit();
                            productList.add(product);
                        } else {
                            Log.w("DB_HELPER", "Could not create product for category: " + category);
                        }
                    } catch (Exception e) {
                        Log.e("DB_HELPER", "Product read error: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_HELPER", "Error fetching products: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        Log.d("DB_HELPER", "Returned product count: " + productList.size());
        return productList;
    }

    public List<CatalogProduct> getCatalogProducts() {
        List<CatalogProduct> catalogList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM product_catalog ORDER BY category, name", null);

            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    double cost = cursor.getDouble(cursor.getColumnIndexOrThrow("cost"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    catalogList.add(new CatalogProduct(name, cost, category));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_HELPER", "Error fetching catalog products: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return catalogList;
    }

    public class CatalogProduct {
        private String name;
        private double cost;
        private String category;

        public CatalogProduct(String name, double cost, String category) {
            this.name = name;
            this.cost = cost;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public double getCost() {
            return cost;
        }

        public String getCategory() {
            return category;
        }
    }

    public boolean updateProduct(Product product) {
        if (product == null || product.getName() == null) {
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("stock", product.getStock());
            values.put("discountAmount", product.getDiscountAmount());
            values.put("price", product.getPrice());
            values.put("cost", product.getCost());
            values.put("profit", product.getPrice() - product.getCost());

            int rowsAffected = db.update("products", values, "name = ?", new String[]{product.getName()});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DB_HELPER", "Product update error: " + e.getMessage());
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    public long getProductIdByName(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("products", new String[]{"id"},
                "name = ?", new String[]{productName},
                null, null, null);

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return id;
    }

    public boolean deleteProduct(long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("products", "id = ?", new String[]{String.valueOf(productId)});
        db.close();
        return result > 0;
    }

    public void resetDatabase() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS products");
            db.execSQL("DROP TABLE IF EXISTS product_catalog");
            onCreate(db);
            Log.d("DB_HELPER", "Database has been reset");
        } catch (Exception e) {
            Log.e("DB_HELPER", "Database reset error: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
    }

    // Debug için - tüm ürünleri logla
    public void debugPrintAllProducts() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM products", null);
            Log.d("DB_HELPER", "=== ALL PRODUCTS ===");
            Log.d("DB_HELPER", "Total products: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    Log.d("DB_HELPER", "Product: " + name + ", Category: " + category + ", Price: " + price);
                } while (cursor.moveToNext());
            }
            Log.d("DB_HELPER", "===================");
        } catch (Exception e) {
            Log.e("DB_HELPER", "Debug print error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }
}
