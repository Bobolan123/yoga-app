package com.example.yogaapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cardManageClasses;
    private LinearLayout cardSyncData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardManageClasses = findViewById(R.id.cardManageClasses);
        cardSyncData = findViewById(R.id.cardSyncData);
    }

    private void setupClickListeners() {
        cardManageClasses.setOnClickListener(v -> navigateToClassManagement());
        cardSyncData.setOnClickListener(v -> performCloudSync());
    }

    private void navigateToClassManagement() {
        Intent navigationIntent = new Intent(MainActivity.this, ClassListActivity.class);
        startActivity(navigationIntent);
    }

    private void performCloudSync() {
        if (checkNetworkConnectivity()) {
            FirebaseSyncHelper cloudSyncHelper = new FirebaseSyncHelper(this);
            cloudSyncHelper.uploadAllClasses(() -> {
                showUserMessage("Data synchronization completed successfully!");
            });
        } else {
            showUserMessage("Network connection required for synchronization");
        }
    }

    private boolean checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
