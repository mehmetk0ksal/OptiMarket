package com.example.optimarket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView statusText, balanceText;
    private ProgressBar progressBar;
    private Button startSimulationButton;
    private RecyclerView resultsRecyclerView;
    private SimulationResultAdapter resultsAdapter;
    private List<SimulationResult> simulationResults;

    private double currentBalance;
    private double totalRevenue = 0;
    private int totalItemsSold = 0;
    private int deletedProductsCount = 0; // Silinen ürün sayısı
    private int unsellableProductsCount = 0; // Satılamayan ürün sayısı (fiyat nedeniyle)

    private Handler handler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        initializeViews();

        databaseHelper = new DatabaseHelper(this);
        currentBalance = getIntent().getDoubleExtra("balance", 0.0);
        balanceText.setText("Current Balance: $" + String.format("%.2f", currentBalance));

        simulationResults = new ArrayList<>();
        setupRecyclerView();

        startSimulationButton.setOnClickListener(v -> startSimulation());
    }

    // UI elemanlarını başlat
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        balanceText = findViewById(R.id.balanceText);
        progressBar = findViewById(R.id.progressBar);
        startSimulationButton = findViewById(R.id.startSimulationButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
    }

    // RecyclerView'ı kurulum
    private void setupRecyclerView() {
        resultsAdapter = new SimulationResultAdapter(simulationResults);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(resultsAdapter);
    }

    // Simülasyonu başlat
    private void startSimulation() {
        List<Product> allProducts = databaseHelper.getAllProducts();

        if (allProducts.isEmpty()) {
            Toast.makeText(this, "No products found for sale!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stokta olan ürünleri filtrele
        List<Product> availableProducts = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getStock() > 0) {
                availableProducts.add(product);
            }
        }

        if (availableProducts.isEmpty()) {
            Toast.makeText(this, "No products in stock!", Toast.LENGTH_SHORT).show();
            return;
        }

        // UI'ı simülasyon moduna geçir
        startSimulationButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Starting weekly sales simulation...");
        resultsRecyclerView.setVisibility(View.GONE);

        // Simülasyon verilerini sıfırla
        simulationResults.clear();
        totalRevenue = 0;
        totalItemsSold = 0;
        deletedProductsCount = 0;
        unsellableProductsCount = 0;

        simulateWeek(availableProducts, 0);
    }

    // Haftalık simülasyon işlemi
    private void simulateWeek(List<Product> products, int currentStep) {
        final int totalSteps = products.size() * 3;

        if (currentStep >= totalSteps) {
            finishSimulation();
            return;
        }

        int progress = (currentStep * 100) / totalSteps;
        progressBar.setProgress(progress);

        // Durum mesajları
        String[] statusMessages = {
                "Analyzing customer traffic...",
                "Calculating product demands...",
                "Processing sales transactions...",
                "Updating stocks...",
                "Finalizing revenue calculations..."
        };

        statusText.setText(statusMessages[currentStep % statusMessages.length]);

        // Her 3. adımda bir ürün satışı simüle et
        if (currentStep % 3 == 2) {
            int productIndex = currentStep / 3;
            if (productIndex < products.size()) {
                simulateProductSales(products.get(productIndex));
            }
        }

        // Sonraki adıma geç (rastgele gecikme ile)
        handler.postDelayed(() -> simulateWeek(products, currentStep + 1),
                300 + random.nextInt(500));
    }

    // Ürün satış simülasyonu
    private void simulateProductSales(Product product) {
        // Fiyat kontrolü: Eğer fiyat maliyetin 3 katından fazlaysa satış yapma
        double priceToleranceRatio = product.getPrice() / product.getCost();
        if (priceToleranceRatio > 3.0) {
            // Ürün çok pahalı, satış imkansız
            unsellableProductsCount++;

            // Satılmayan ürün için sonuç ekle
            SimulationResult result = new SimulationResult(
                    product.getName(),
                    product.getCategory(),
                    0, // Hiç satılmadı
                    product.getPrice() - product.getDiscountAmount(),
                    0.0, // Hiç gelir yok
                    product.getStock(),
                    0,
                    false,
                    true // Çok pahalı olduğu için satılamadı
            );
            simulationResults.add(result);
            return;
        }

        // Kar marjını hesapla
        double profitMargin = (product.getPrice() - product.getCost()) / product.getCost();
        double discountEffect = product.getDiscountAmount() / product.getPrice();

        // Temel talep oranı
        double baseDemand = 0.7;

        // Kar marjına göre etki hesaplama
        double marginEffect;
        if (profitMargin <= 0.1) {
            marginEffect = 0.4; // Düşük kar marjı, yüksek talep
        } else if (profitMargin <= 0.2) {
            marginEffect = 0.2;
        } else if (profitMargin <= 0.5) {
            marginEffect = 0.0; // Orta kar marjı
        } else if (profitMargin <= 1.0) {
            marginEffect = -0.2;
        } else {
            marginEffect = -0.4; // Yüksek kar marjı, düşük talep
        }

        // İndirim etkisi
        double finalDiscountEffect = discountEffect * 0.5;

        // Rastgele faktör
        double randomFactor = (random.nextDouble() - 0.5) * 0.6;

        // Son talep hesaplama
        double finalDemand = Math.max(0.1, Math.min(1.0,
                baseDemand + marginEffect + finalDiscountEffect + randomFactor));

        // Satılabilecek maksimum miktar
        int maxSellable = (int) Math.ceil(product.getStock() * finalDemand);
        int quantitySold = Math.max(0, Math.min(maxSellable, product.getStock()));

        // Satış miktarına rastgelelik ekle
        if (quantitySold > 0) {
            double quantityRandomness = 0.8 + (random.nextDouble() * 0.4);
            quantitySold = (int) Math.max(1, quantitySold * quantityRandomness);
            quantitySold = Math.min(quantitySold, product.getStock());
        }

        // Satış işlemi
        if (quantitySold > 0) {
            // Birim fiyat (indirimli)
            double unitPrice = product.getPrice() - product.getDiscountAmount();
            double revenue = quantitySold * unitPrice;

            // Stok güncelleme
            int newStock = product.getStock() - quantitySold;
            product.setStock(newStock);

            // Stok tükenirse ürünü sil
            if (newStock <= 0) {
                try {
                    long productId = getProductId(product);
                    boolean deleted = databaseHelper.deleteProduct(productId);
                    if (deleted) {
                        deletedProductsCount++;
                        System.out.println("Product deleted: " + product.getName() + " (Stock: 0)");
                    }
                } catch (Exception e) {
                    System.out.println("Product could not be deleted, updated as stock 0: " + product.getName());
                    databaseHelper.updateProduct(product);
                }
            } else {
                // Ürünü güncelle
                databaseHelper.updateProduct(product);
            }

            // Toplam değerleri güncelle
            totalRevenue += revenue;
            totalItemsSold += quantitySold;
            currentBalance += revenue;

            // Sonuç objesi oluştur
            SimulationResult result = new SimulationResult(
                    product.getName(),
                    product.getCategory(),
                    quantitySold,
                    unitPrice,
                    revenue,
                    newStock,
                    quantitySold,
                    newStock <= 0,
                    false // Fiyat sorunu yok
            );
            simulationResults.add(result);
        } else {
            // Hiç satılmayan ürün için sonuç ekle
            SimulationResult result = new SimulationResult(
                    product.getName(),
                    product.getCategory(),
                    0,
                    product.getPrice() - product.getDiscountAmount(),
                    0.0,
                    product.getStock(),
                    0,
                    false,
                    false // Fiyat sorunu yok, sadece talep yok
            );
            simulationResults.add(result);
        }
    }

    // Ürün ID'sini getir
    private long getProductId(Product product) {
        return databaseHelper.getProductIdByName(product.getName());
    }

    // Simülasyonu tamamla
    private void finishSimulation() {
        // Satılan oranlarını hesapla ürünler satıldıktan sonra.
        for (SimulationResult result : simulationResults) {
            result.calculateSalesShare(totalItemsSold);
        }

        progressBar.setVisibility(View.GONE);
        statusText.setText("Simulation completed!");
        balanceText.setText("New Balance: $" + String.format("%.2f", currentBalance));

        // Özet bilgileri göster
        TextView summaryText = findViewById(R.id.summaryText);
        summaryText.setVisibility(View.VISIBLE);
        summaryText.setText(String.format(
                "📊 WEEKLY SALES SUMMARY\n" +
                        "💰 Total Revenue: $%.2f\n" +
                        "📦 Products Sold: %d items\n" +
                        "🏪 Active Products: %d types\n" +
                        "🗑️ Out Products: %d types\n" +
                        "❌ Unsellable Products: %d types",
                totalRevenue, totalItemsSold, simulationResults.size(),
                deletedProductsCount, unsellableProductsCount
        ));

        resultsRecyclerView.setVisibility(View.VISIBLE);
        resultsAdapter.notifyDataSetChanged();

        startSimulationButton.setEnabled(true);
        startSimulationButton.setText("Start New Simulation");

        // Tamamlanma mesajı
        String toastMessage = String.format("Simulation completed! Revenue: $%.2f", totalRevenue);
        if (deletedProductsCount > 0) {
            toastMessage += String.format("\n%d products deleted due to stock depletion!", deletedProductsCount);
        }
        if (unsellableProductsCount > 0) {
            toastMessage += String.format("\n%d products too expensive to sell!", unsellableProductsCount);
        }

        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    // Sonuç ile bitir
    private void finishWithResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updatedBalance", currentBalance);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }

    // Simülasyon sonucu sınıfı
    public static class SimulationResult {
        private String productName;
        private String category;
        private int quantitySold;
        private double unitPrice;
        private double totalRevenue;
        private int remainingStock;
        private double salesSharePercentage;
        private String salesShareDescription;
        private boolean isDeleted;
        private boolean isTooExpensive; // Çok pahalı olduğu için satılamayan ürünler

        public SimulationResult(String productName, String category, int quantitySold,
                                double unitPrice, double totalRevenue, int remainingStock,
                                int quantitySoldForShare, boolean isDeleted, boolean isTooExpensive) {
            this.productName = productName;
            this.category = category;
            this.quantitySold = quantitySold;
            this.unitPrice = unitPrice;
            this.totalRevenue = totalRevenue;
            this.remainingStock = remainingStock;
            this.isDeleted = isDeleted;
            this.isTooExpensive = isTooExpensive;
        }

        // Satış payını hesapla
        public void calculateSalesShare(int totalItemsSold) {
            if (totalItemsSold > 0) {
                this.salesSharePercentage = (double) quantitySold / totalItemsSold * 100;
                this.salesShareDescription = getSalesShareDescription(salesSharePercentage);
            } else {
                this.salesSharePercentage = 0;
                this.salesShareDescription = "No Sales";
            }
        }

        // Satış payı açıklaması
        private String getSalesShareDescription(double sharePercentage) {
            if (sharePercentage >= 25.0) {
                return "Top Seller";
            } else if (sharePercentage >= 15.0) {
                return "High Performer";
            } else if (sharePercentage >= 10.0) {
                return "Good Seller";
            } else if (sharePercentage >= 5.0) {
                return "Average Seller";
            } else if (sharePercentage >= 2.0) {
                return "Low Performer";
            } else if (sharePercentage > 0) {
                return "Poor Seller";
            } else {
                return "No Sales";
            }
        }

        // Getter metodları
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public int getQuantitySold() { return quantitySold; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getRemainingStock() { return remainingStock; }
        public boolean isDeleted() { return isDeleted; }
        public boolean isTooExpensive() { return isTooExpensive; }

        public String getSalesShare() {
            return String.format("%.1f%% - %s", salesSharePercentage, salesShareDescription);
        }

        public String getStockStatus() {
            if (isTooExpensive) {
                return "TOO EXPENSIVE - UNSELLABLE";
            }
            return isDeleted ? "OUT OF STOCK - DELETED" : String.valueOf(remainingStock);
        }
    }
}