package com.example.optimarket;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = loadProducts();

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);
    }

    // Veritabanından ürünleri çekiyoruz
    private List<Product> loadProducts() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        return dbHelper.getAllProducts();
    }
}
