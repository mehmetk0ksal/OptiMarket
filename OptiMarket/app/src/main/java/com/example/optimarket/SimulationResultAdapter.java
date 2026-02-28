package com.example.optimarket;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SimulationResultAdapter extends RecyclerView.Adapter<SimulationResultAdapter.ResultViewHolder> {

    private List<SimulationActivity.SimulationResult> results;

    public SimulationResultAdapter(List<SimulationActivity.SimulationResult> results) {
        this.results = results;
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView productName, category, quantitySold, unitPrice, totalRevenue, remainingStock, salesShare, profitIndicator;

        public ResultViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.resultProductName);
            category = itemView.findViewById(R.id.resultCategory);
            quantitySold = itemView.findViewById(R.id.resultQuantitySold);
            unitPrice = itemView.findViewById(R.id.resultUnitPrice);
            totalRevenue = itemView.findViewById(R.id.resultTotalRevenue);
            remainingStock = itemView.findViewById(R.id.resultRemainingStock);
            salesShare = itemView.findViewById(R.id.resultDemandRate); // Reusing existing TextView
            profitIndicator = itemView.findViewById(R.id.profitIndicator);
        }
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simulation_result_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        SimulationActivity.SimulationResult result = results.get(position);

        holder.productName.setText(result.getProductName());
        holder.category.setText(result.getCategory().toUpperCase());
        holder.quantitySold.setText(String.valueOf(result.getQuantitySold()));
        holder.unitPrice.setText("$" + String.format("%.2f", result.getUnitPrice()));
        holder.totalRevenue.setText("$" + String.format("%.2f", result.getTotalRevenue()));
        holder.remainingStock.setText(String.valueOf(result.getRemainingStock()));

        // Satılanlardan toplam ürünlerden satış oranını yaz
        holder.salesShare.setText(result.getSalesShare());

        // Satışları renklendirerek daha güzel bir arayüz tasarladık
        String shareText = result.getSalesShare();
        if (shareText.contains("Top Seller")) {
            holder.salesShare.setTextColor(Color.parseColor("#FF4444")); // Red for top sellers
        } else if (shareText.contains("High Performer")) {
            holder.salesShare.setTextColor(Color.parseColor("#FF6600")); // Orange for high performers
        } else if (shareText.contains("Good Seller")) {
            holder.salesShare.setTextColor(Color.parseColor("#44AA44")); // Green for good sellers
        } else if (shareText.contains("Average Seller")) {
            holder.salesShare.setTextColor(Color.parseColor("#2196F3")); // Blue for average
        } else if (shareText.contains("Low Performer")) {
            holder.salesShare.setTextColor(Color.parseColor("#FFAA00")); // Yellow for low performers
        } else if (shareText.contains("Poor Seller")) {
            holder.salesShare.setTextColor(Color.parseColor("#FF9800")); // Amber for poor sellers
        } else {
            holder.salesShare.setTextColor(Color.parseColor("#888888")); // Gray for no sales
        }

        // Orana göre en çok satanları demand üzerinden gösteriyoruz
        double revenue = result.getTotalRevenue();
        String shareCategory = result.getSalesShare();

        if (shareCategory.contains("Top Seller") && revenue >= 15.0) {
            holder.profitIndicator.setText("🏆 STAR");
            holder.profitIndicator.setTextColor(Color.parseColor("#FFD700")); // Gold
        } else if (shareCategory.contains("High Performer") && revenue >= 10.0) {
            holder.profitIndicator.setText("🔥 HOT");
            holder.profitIndicator.setTextColor(Color.parseColor("#FF4444")); // Red
        } else if (shareCategory.contains("Good Seller") || revenue >= 8.0) {
            holder.profitIndicator.setText("📈 GOOD");
            holder.profitIndicator.setTextColor(Color.parseColor("#44AA44")); // Green
        } else if (shareCategory.contains("Average Seller") || revenue >= 5.0) {
            holder.profitIndicator.setText("📊 OK");
            holder.profitIndicator.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else if (revenue > 0) {
            holder.profitIndicator.setText("📉 LOW");
            holder.profitIndicator.setTextColor(Color.parseColor("#FFAA00")); // Orange
        } else {
            holder.profitIndicator.setText("❌ NONE");
            holder.profitIndicator.setTextColor(Color.parseColor("#888888")); // Gray
        }

        // Stock durumunu uyaran mesajlar
        if (result.isDeleted()) {
            holder.remainingStock.setTextColor(Color.parseColor("#FF4444"));
            holder.remainingStock.setText("0 (SOLD OUT)");
        } else if (result.getRemainingStock() == 0) {
            holder.remainingStock.setTextColor(Color.parseColor("#FF4444"));
            holder.remainingStock.setText("0 (OUT)");
        } else if (result.getRemainingStock() <= 3) {
            holder.remainingStock.setTextColor(Color.parseColor("#FF6600"));
            holder.remainingStock.setText(result.getRemainingStock() + " (CRITICAL)");
        } else if (result.getRemainingStock() <= 5) {
            holder.remainingStock.setTextColor(Color.parseColor("#FFAA00"));
            holder.remainingStock.setText(result.getRemainingStock() + " (LOW)");
        } else if (result.getRemainingStock() <= 10) {
            holder.remainingStock.setTextColor(Color.parseColor("#2196F3"));
            holder.remainingStock.setText(String.valueOf(result.getRemainingStock()));
        } else {
            holder.remainingStock.setTextColor(Color.parseColor("#44AA44"));
            holder.remainingStock.setText(String.valueOf(result.getRemainingStock()));
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}