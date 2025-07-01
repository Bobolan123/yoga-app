package com.example.yogaapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnViewClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnViewClasses = findViewById(R.id.btnViewClasses);
        Button btnSyncToCloud = findViewById(R.id.btnSyncToCloud);

        btnViewClasses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClassListActivity.class);
            startActivity(intent);
        });

        btnSyncToCloud.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                FirebaseSyncHelper syncHelper = new FirebaseSyncHelper(this);
                syncHelper.uploadAllClasses(() -> {
                    Toast.makeText(this, "Upload complete!", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
