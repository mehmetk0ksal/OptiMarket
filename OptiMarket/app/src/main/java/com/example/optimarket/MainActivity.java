package com.example.optimarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private double userBalance = 5000.0; // Başlangıç bakiyesi
    private TextView balanceTextView;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceTextView = findViewById(R.id.textBalance);
        updateBalanceText(userBalance);

        Button productlistbutton = findViewById(R.id.productListButton);
        productlistbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        Button addproductbutton = findViewById(R.id.buyProductButton);
        addproductbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BuyProductActivity.class);
                intent.putExtra("balance", userBalance);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        Button stockmanagementbutton = findViewById(R.id.simButton);
        stockmanagementbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SimulationActivity.class);
                intent.putExtra("balance", userBalance);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }

    private void updateBalanceText(double balance) {
        balanceTextView.setText(String.format("Balance: $%.2f", balance));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            double updatedBalance = data.getDoubleExtra("updatedBalance", 0.0);
            userBalance = updatedBalance;
            updateBalanceText(updatedBalance);
        }
    }
}