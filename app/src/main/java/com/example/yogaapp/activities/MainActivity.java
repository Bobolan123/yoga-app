package com.example.yogaapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.R;
import com.example.yogaapp.database.FirebaseSyncHelper;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cardManageClasses;
    private LinearLayout cardSyncData;
    private ProgressDialog progressDialog;

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
        cardSyncData.setOnClickListener(v -> {
            Log.d("MainActivity", "Cloud sync button clicked");
            performCloudSync();
        });
    }

    private void navigateToClassManagement() {
        Intent navigationIntent = new Intent(MainActivity.this, ClassListActivity.class);
        startActivity(navigationIntent);
    }

    private void performCloudSync() {
        Log.d("MainActivity", "performCloudSync() called");
        if (checkNetworkConnectivity()) {
            Log.d("MainActivity", "Network connectivity OK, starting sync");
            showLoadingDialog();
            FirebaseSyncHelper cloudSyncHelper = new FirebaseSyncHelper(this);
            cloudSyncHelper.uploadAllClasses(new FirebaseSyncHelper.SyncCallback() {
                @Override
                public void onComplete() {
                    Log.d("MainActivity", "Sync completed successfully");
                    hideLoadingDialog();
                    showUserMessage("Data synchronization completed successfully!");
                }

                @Override
                public void onError(String errorMessage) {
                    Log.d("MainActivity", "Sync failed: " + errorMessage);
                    hideLoadingDialog();
                    showUserMessage("Sync failed: " + errorMessage);
                }
            });
        } else {
            Log.d("MainActivity", "No network connectivity");
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

    private void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Synchronizing data...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
