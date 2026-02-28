package com.example.optimarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BuyProductAdapter extends RecyclerView.Adapter<BuyProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnBuyClickListener listener;

    // Constructor
    public BuyProductAdapter(BuyProductActivity buyProductActivity, List<Product> productList, OnBuyClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    // ViewHolder: xml'deki ögeleri bağlar
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, category;
        Button buyButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productCatalogName);
            price = itemView.findViewById(R.id.productCatalogPrice);
            category = itemView.findViewById(R.id.productCatalogCategory);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalog_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.name.setText(product.getName());
        holder.price.setText("Price: $" + product.getCost());
        holder.category.setText("Category: " + product.getCategory());

        holder.buyButton.setOnClickListener(v -> listener.onBuyClick(product, position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}