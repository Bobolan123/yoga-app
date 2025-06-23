package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnViewClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // layout with btnViewClasses

        Button btnViewClasses = findViewById(R.id.btnViewClasses);
        btnViewClasses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClassListActivity.class);
            startActivity(intent);
        });
    }
}
