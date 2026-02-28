package com.example.optimarket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;

public class BuyProductActivity extends AppCompatActivity implements OnBuyClickListener {

    private RecyclerView recyclerView;
    private BuyProductAdapter adapter;
    private DatabaseHelper databaseHelper;
    private double currentBalance;
    private TextView currentBalance_Text;
    private List<Product> originalProductList; // Tüm ürünleri burada tutacağız

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_product);

        currentBalance = getIntent().getDoubleExtra("balance", 0.0); //Maindeki parayı çekme

        currentBalance_Text = findViewById(R.id.textCurrentBalance);
        currentBalance_Text.setText(String.format("Balance: $%.2f", currentBalance));

        recyclerView = findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText lowerBoundEditText = findViewById(R.id.lowerBoundEditText);
        EditText upperBoundEditText = findViewById(R.id.upperBoundEditText);
        findViewById(R.id.priceFilterButton).setOnClickListener(v -> {
            String lowerText = lowerBoundEditText.getText().toString().trim();
            String upperText = upperBoundEditText.getText().toString().trim();

            if (lowerText.isEmpty() || upperText.isEmpty()) {
                Toast.makeText(this, "Please enter both bounds!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lower = Double.parseDouble(lowerText);
                double upper = Double.parseDouble(upperText);

                PriceProductFilter filter = new PriceProductFilter();
                filter.setLowerBound(lower);
                filter.setUpperBound(upper);

                List<Product> filteredList = new ArrayList<>();
                for (Product product : originalProductList) {
                    if (filter.filter(product)) {
                        filteredList.add(product);
                    }
                }

                adapter = new BuyProductAdapter(this, filteredList, this);
                recyclerView.setAdapter(adapter);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers!", Toast.LENGTH_SHORT).show();
            }
        });

        databaseHelper = new DatabaseHelper(this);

        List<DatabaseHelper.CatalogProduct> catalogProducts = databaseHelper.getCatalogProducts();
        originalProductList = new ArrayList<>();
        ProductCreator creator = new ProductCreator();

        for (DatabaseHelper.CatalogProduct cp : catalogProducts) {
            Product product = creator.CreateProduct(cp.getCategory());
            if (product != null) {
                product.setName(cp.getName());
                product.setCost(cp.getCost());
                product.setStock(0);
                product.setPrice(cp.getCost() * 1.2); // Opsiyonel: Satış fiyatını önden belirle
                originalProductList.add(product);
            }
        }

        EditText categoryEditText = findViewById(R.id.categoryEditText);
        findViewById(R.id.categoryFilterButton).setOnClickListener(v -> {
            String categoryInput = categoryEditText.getText().toString().trim();

            if (categoryInput.isEmpty()) {
                Toast.makeText(this, "Please enter a category!", Toast.LENGTH_SHORT).show();
                return;
            }

            CategoryFilter filter = new CategoryFilter(categoryInput);
            List<Product> filteredList = new ArrayList<>();
            for (Product product : originalProductList) {
                if (filter.filter(product)) {
                    filteredList.add(product);
                }
            }

            adapter = new BuyProductAdapter(this, filteredList, this);
            recyclerView.setAdapter(adapter);
        });

        findViewById(R.id.clearFilterButton).setOnClickListener(v -> {
            adapter = new BuyProductAdapter(this, originalProductList, this);
            recyclerView.setAdapter(adapter);

            // İstersen kullanıcı arayüzündeki metin kutularını da temizleyebilirsin:
            lowerBoundEditText.setText("");
            upperBoundEditText.setText("");
            categoryEditText.setText("");

            Toast.makeText(this, "Filters cleared. Showing all products.", Toast.LENGTH_SHORT).show();
        });

        adapter = new BuyProductAdapter(this, originalProductList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updatedBalance", currentBalance);
        setResult(Activity.RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    public void onBuyClick(Product product, int ignoredQuantity) {
        Log.d("BUY_DEBUG", "Buying: " + product.getName() + ", category: " + product.getCategory());
        // Miktar girme dialogu
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(product.getName() + " - How many do you want to buy?");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Buy", (dialog, which) -> {
            try {
                String inputText = input.getText().toString().trim();
                if (inputText.isEmpty()) {
                    Toast.makeText(this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(inputText);
                if (quantity <= 0) {
                    Toast.makeText(this, "The amount must be greater than 0!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double totalPrice = product.getCost() * quantity;

                if (totalPrice <= currentBalance) {
                    // Bakiyeyi düş
                    currentBalance -= totalPrice;

                    // Ürünün veritabanında olup olmadığını kontrol et
                    if (databaseHelper.isProductExists(product.getName())) {
                        // Mevcut ürünü güncelle
                        // Önce mevcut stok miktarını al
                        List<Product> existingProducts = databaseHelper.getAllProducts();
                        Product existingProduct = null;
                        for (Product p : existingProducts) {
                            if (p.getName().equals(product.getName())) {
                                existingProduct = p;
                                break;
                            }
                        }

                        if (existingProduct != null) {
                            existingProduct.setStock(existingProduct.getStock() + quantity);
                            existingProduct.calculateProfit();
                            databaseHelper.updateProduct(existingProduct);
                        }
                    } else {
                        // Yeni ürünü ekle
                        product.setStock(quantity);
                        // Satış fiyatını maliyet fiyatının üzerine %20 kar ile hesapla (veya istediğin formülü kullan)
                        product.setPrice(product.getCost() * 1.2); // %20 kar marjı
                        product.calculateProfit();

                        boolean addResult = databaseHelper.addProduct(product);
                        if (!addResult) {
                            Toast.makeText(this, "An error occurred while adding the product to the database!", Toast.LENGTH_SHORT).show();
                            // Bakiyeyi geri yükle
                            currentBalance += totalPrice;
                            currentBalance_Text.setText(String.format("Balance: $%.2f", currentBalance));
                            return;
                        }
                    }

                    Toast.makeText(this,
                            quantity +" "+ product.getName() + "' purchased. Remaining balance: $" + String.format("%.2f", currentBalance),
                            Toast.LENGTH_LONG).show();
                    currentBalance_Text.setText(String.format("Balance: $%.2f", currentBalance));

                } else {
                    Toast.makeText(this,
                            "Insufficient balance! The amount you need: $" + String.format("%.2f", totalPrice) + ", You have: $" + String.format("%.2f", currentBalance),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}