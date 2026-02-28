package com.example.optimarket;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<com.example.optimarket.ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    // Constructor
    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    // ViewHolder:xml'deki ögeleri bağlar
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, cost, profit, stock, discount, category;
        Button editButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            cost = itemView.findViewById(R.id.productCost);
            profit = itemView.findViewById(R.id.productProfit);
            stock = itemView.findViewById(R.id.productStock);
            discount = itemView.findViewById(R.id.productDiscount);
            category = itemView.findViewById(R.id.productCategory);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.name.setText("Name: " + p.getName());
        holder.price.setText("Price: " + String.format("%.2f", p.getPrice()) + "$");
        holder.cost.setText("Cost: " + String.format("%.2f", p.getCost()) + "$");
        holder.profit.setText("Profit: " + String.format("%.2f", p.getProfit()) + "$");
        holder.stock.setText("Stock: " + p.getStock());
        holder.discount.setText("Discount: " + String.format("%.2f", p.getDiscountAmount()) + "$");
        holder.category.setText("Category: " + p.getCategory());

        holder.editButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Update Price & Discount");

            LinearLayout layout = new LinearLayout(v.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            // Price Row Layout
            LinearLayout priceRow = new LinearLayout(v.getContext());
            priceRow.setOrientation(LinearLayout.HORIZONTAL);

// Sabit yazı: Current Price:
            TextView priceLabel = new TextView(v.getContext());
            priceLabel.setText("Current Price: ");
            priceLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            priceRow.addView(priceLabel);

// Fiyat girişi
            EditText priceInput = new EditText(v.getContext());
            priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            priceInput.setText(String.format("%.2f", p.getPrice()));
            priceInput.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            priceRow.addView(priceInput);

            layout.addView(priceRow);

// ------------------------------

// Discount Row Layout
            LinearLayout discountRow = new LinearLayout(v.getContext());
            discountRow.setOrientation(LinearLayout.HORIZONTAL);

// Sabit yazı: Discount:
            TextView discountLabel = new TextView(v.getContext());
            discountLabel.setText("Discount: ");
            discountLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            discountRow.addView(discountLabel);

// Discount girişi
            EditText discountInput = new EditText(v.getContext());
            discountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            discountInput.setText(String.format("%.2f", p.getDiscountAmount()));
            discountInput.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            discountRow.addView(discountInput);

            layout.addView(discountRow);


            builder.setView(layout);

            builder.setPositiveButton("Update", (dialog, which) -> {
                try {
                    String priceStr = priceInput.getText().toString().trim();
                    String discountStr = discountInput.getText().toString().trim();

                    if (priceStr.isEmpty() || discountStr.isEmpty()) {
                        Toast.makeText(v.getContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double newPrice = Double.parseDouble(priceStr);
                    double newDiscount = Double.parseDouble(discountStr);

                    // Negatif değer kontrolü
                    if (newPrice < 0 || newDiscount < 0) {
                        Toast.makeText(v.getContext(), "Price and discount cannot be negative!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ürün güncelleniyor
                    Product selectedProduct = productList.get(holder.getAdapterPosition());
                    selectedProduct.setPrice(newPrice);
                    selectedProduct.setDiscountAmount(newDiscount);
                    selectedProduct.calculateProfit(); // Kar yeniden hesaplanır

                    // DATABASE'E GÜNCELLE
                    DatabaseHelper dbHelper = new DatabaseHelper(v.getContext());
                    boolean updateResult = dbHelper.updateProduct(selectedProduct);

                    if (updateResult) {
                        Toast.makeText(v.getContext(),
                                selectedProduct.getName() + " updated successfully!",
                                Toast.LENGTH_SHORT).show();
                        notifyItemChanged(holder.getAdapterPosition()); // Ekranı yenile
                    } else {
                        Toast.makeText(v.getContext(),
                                "An error occurred during update!",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(v.getContext(), "Please enter valid numbers!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
